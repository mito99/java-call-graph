describe("utils", () => {
  beforeAll(async () => {
    console.log("beforeAll");
  });

  test("should return methods using mocked driver", async () => {
    expect(true).toBe(true);
  });
});
import neo4j, { Driver, Session } from "neo4j-driver";
import { getMethodsByClass } from "../../../src/lib/neo4j/utils";

// リアルなNeo4jインスタンスに対してテストを実行する場合.
describe("getMethodsByClass (Real Neo4j)", () => {
  let session: Session;
  let driver: Driver;

  beforeAll(async () => {
    // Neo4jがローカルで起動していることを確認してください。
    driver = neo4j.driver(
      "bolt://localhost:7687",
      neo4j.auth.basic("neo4j", "password")
    );
    session = driver.session();

    // テストデータを作成
    await session.run(`
      CREATE (c:Class {
        name: 'TestClass',
        package: 'org.apache.commons.io.function',
        descriptor: '()V',
        accessModifier: 'public'
      })
        -[:HAS]->
        (m:Method {
          name: 'testMethod',
          descriptor: '()V'
        })
      `);
  });

  afterAll(async () => {
    // テストデータの削除
    await session.run("MATCH (c:Class {name: 'TestClass'}) DETACH DELETE c");
    await driver.close();
  });

  test("should return methods for a given class name", async () => {
    const methods = await getMethodsByClass(session, "TestClass");
    expect(methods).toEqual([
      {
        methodName: "testMethod",
        className: "TestClass",
        packageName: "org.apache.commons.io.function",
        descriptor: "()V",
        accessModifier: "public",
      },
    ]);
  });

  test("should return an empty array if no methods are found", async () => {
    const methods = await getMethodsByClass(session, "NonExistentClass");
    expect(methods).toEqual([]);
  });
});
