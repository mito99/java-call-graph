import neo4j, { Driver, Session } from "neo4j-driver";
import { MethodNode } from "./types";

// Neo4j ドライバーの初期化
const driver: Driver = neo4j.driver(
  "bolt://localhost:7687",
  neo4j.auth.basic("neo4j", "password")
);

export function getSession(): Session {
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
  methodDigest: string
): Promise<MethodNode[]> {
  const query = `
        MATCH (m:Method)-[:CALLS]->(called:Method)
        WHERE m.digest = $methodDigest
        RETURN 
          called.name as methodName, 
          called.descriptor as descriptor, 
          called.accessModifier as accessModifier,
          called.digest as methodDigest
    `;

  const result = await session.run(query, { methodDigest });
  return result.records.map((record) => {
    const methodName = record.get("methodName");
    const descriptor = record.get("descriptor");
    const accessModifier = record.get("accessModifier");
    const methodDigest = record.get("methodDigest");
    return {
      methodName: methodName,
      descriptor: descriptor,
      accessModifier: accessModifier,
      methodDigest: methodDigest,
    };
  }) as MethodNode[];
}
