import neo4j, { Driver, Record, Session } from "neo4j-driver";
import { CallingMethodTree, MethodNode } from "./types";

export function getSession(): Session {
  const neo4jConfig = {
    uri: process.env.NEO4J_URI as string,
    username: process.env.NEO4J_USERNAME as string,
    password: process.env.NEO4J_PASSWORD as string,
  };

  const driver: Driver = neo4j.driver(
    neo4jConfig.uri,
    neo4j.auth.basic(neo4jConfig.username, neo4jConfig.password)
  );

  const session: Session = driver.session();
  return session;
}

export async function getMethodsByClass(
  session: Session,
  params: {
    packageName: string;
    className: string;
    methodName: string;
    limit: number;
  }
): Promise<MethodNode[]> {
  const where = [];
  if (params.packageName) {
    where.push(`c.package = $packageName`);
  }
  if (params.className) {
    where.push(`c.name = $className`);
  }
  if (params.methodName) {
    where.push(`m.name = $methodName`);
  }
  if (where.length === 0) {
    return [];
  }

  const query = `
        MATCH (c:Class)-[:HAS]->(m:Method)
        WHERE ${where.join(" AND ")}
        RETURN 
          m.name as methodName, 
          c.name as className, 
          c.package as packageName, 
          m.descriptor as descriptor, 
          m.accessModifier as accessModifier,
          m.digest as methodDigest
        LIMIT toInteger($limit)
    `;

  const result = await session.run(query, {
    packageName: params.packageName,
    className: params.className,
    methodName: params.methodName,
    limit: Math.trunc(params.limit),
  });
  return result.records.map((record) => {
    const methodName = record.get("methodName");
    const packageName = record.get("packageName");
    const className = record.get("className");
    const descriptor = record.get("descriptor");
    const accessModifier = record.get("accessModifier");
    const methodDigest = record.get("methodDigest");
    return {
      methodName: methodName,
      className: className,
      packageName: packageName,
      descriptor: descriptor,
      accessModifier: accessModifier,
      methodDigest: methodDigest,
    };
  }) as MethodNode[];
}

export async function getCallingMethodsByDigest(
  session: Session,
  params: {
    methodDigest: string;
    hopCount: number;
  }
): Promise<CallingMethodTree | null> {
  const query = `
    MATCH path = (m1:Method{digest: $methodDigest})
      <-[:CALLS*1..${params.hopCount}]-(m2:Method)
      OPTIONAL MATCH (m2) <-[:HAS*1..]-(md:Module)
    WHERE none(n in nodes(path)[2..-1] WHERE n = last(nodes(path)))
    WITH path, md,
      [n in nodes(path) WHERE n:Method | {
        methodName: n.name,
        descriptor: n.descriptor,
        accessModifier: n.accessModifier,
        methodDigest: n.digest,
        className: n.class,
        packageName: n.package,
        moduleName: coalesce(md.name, '')
      }] AS methods
    RETURN 
      methods
  `;
  const result = await session.run(query, {
    methodDigest: params.methodDigest,
  });

  if (result.records.length === 0) {
    return null;
  }

  const callingMethodNodes = mapCallingMethodNodes(result.records);
  return mergeCallingMethodTrees(callingMethodNodes);

  //-- 以下、ヘルパー関数
  function mapCallingMethodNodes(records: Record[]): CallingMethodTree[] {
    return records.map((record): CallingMethodTree => {
      const methodNodes = record.get("methods").map((method: MethodNode) => ({
        methodName: method.methodName,
        descriptor: method.descriptor,
        accessModifier: method.accessModifier,
        methodDigest: method.methodDigest,
        className: method.className,
        packageName: method.packageName,
        children: {},
      }));

      const [rootNode, ...restNodes] = methodNodes;
      restNodes.reduce(
        (
          acc: { [key: string]: CallingMethodTree },
          node: CallingMethodTree
        ) => {
          acc[node.methodDigest] = node;
          return node.children;
        },
        rootNode.children
      );
      return rootNode;
    });
  }

  function mergeCallingMethodTrees(
    callingMethodNodes: CallingMethodTree[]
  ): CallingMethodTree {
    const mergeTrees = (
      tree1: CallingMethodTree,
      tree2: CallingMethodTree
    ): CallingMethodTree => {
      if (!tree1) return tree2;
      if (!tree2) return tree1;

      if (tree1.children && tree2.children) {
        for (const key in tree2.children) {
          if (tree1.children[key]) {
            tree1.children[key] = mergeTrees(
              tree1.children[key],
              tree2.children[key]
            );
          } else {
            tree1.children[key] = tree2.children[key];
          }
        }
      } else if (tree2.children) {
        tree1.children = tree2.children;
      }
      return tree1;
    };

    return callingMethodNodes.reduce(
      (acc: CallingMethodTree, node: CallingMethodTree) => {
        mergeTrees(acc, node);
        return acc;
      },
      callingMethodNodes[0]
    );
  }
}
