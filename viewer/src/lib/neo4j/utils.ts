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
  className: string
): Promise<MethodNode[]> {
  const query = `
        MATCH (c:Class {name: $className})-[:HAS]->(m:Method)
        RETURN m
    `;

  const result = await session.run(query, { className });
  return result.records
    .map((record) => record.get("m").properties)
    .map((method) => ({
      name: method.name,
      parameters: method.parameters,
      returnType: method.returnType,
    })) as MethodNode[];
}
