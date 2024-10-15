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
  packageName: string,
  className: string,
  methodName: string
): Promise<MethodNode[]> {
  const where = [];
  if (packageName) {
    where.push(`c.package = $packageName`);
  }
  if (className) {
    where.push(`c.name = $className`);
  }
  if (methodName) {
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
          c.accessModifier as accessModifier
    `;

  const result = await session.run(query, {
    packageName,
    className,
    methodName,
  });
  return result.records.map((record) => {
    const methodName = record.get("methodName");
    const packageName = record.get("packageName");
    const className = record.get("className");
    const descriptor = record.get("descriptor");
    const accessModifier = record.get("accessModifier");
    return {
      methodName: methodName,
      className: className,
      packageName: packageName,
      descriptor: descriptor,
      accessModifier: accessModifier,
    };
  }) as MethodNode[];
}
