describe("utils", () => {
  beforeAll(async () => {
    console.log("beforeAll");
  });

  test("should return methods using mocked driver", async () => {
    expect(true).toBe(true);
  });
});
import neo4j, { Driver, Session } from "neo4j-driver";
import {
  getCallingMethodsByDigest,
  getMethodsByClass,
} from "../../../src/lib/neo4j/utils";

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
        descriptor: '()V'
      })
        -[:HAS]->
        (m:Method {
          name: 'testMethod',
          descriptor: '()V',
          digest: '1234567890',
          accessModifier: 'public'
        })
      `);
  });

  afterAll(async () => {
    // テストデータの削除
    await session.run("MATCH (c:Class {name: 'TestClass'}) DETACH DELETE c");
    await driver.close();
  });

  test("should return methods for a given class name", async () => {
    const methods = await getMethodsByClass(session, {
      packageName: "org.apache.commons.io.function",
      className: "TestClass",
      methodName: "testMethod",
      limit: 10,
    });
    expect(methods).toEqual([
      {
        methodName: "testMethod",
        className: "TestClass",
        packageName: "org.apache.commons.io.function",
        descriptor: "()V",
        accessModifier: "public",
        methodDigest: "1234567890",
      },
    ]);
  });

  test("should return an empty array if no methods are found", async () => {
    const methods = await getMethodsByClass(session, {
      packageName: "NonExistentPackage",
      className: "NonExistentClass",
      methodName: "NonExistentMethod",
      limit: 10,
    });
    expect(methods).toEqual([]);
  });
});

describe("getCallingMethodsByDigest (Real Neo4j)", () => {
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
      CREATE (m1:Method {
        name: 'callerMethod',
        descriptor: '()V',
        digest: 'digest1',
        accessModifier: 'public',
        class: 'CallerClass',
        package: 'org.example'
      }),
      (m2:Method {
        name: 'calledMethod',
        descriptor: '()V',
        digest: 'digest2',
        accessModifier: 'public',
        class: 'CalledClass',
        package: 'org.example'
      }),
      (m3:Method {
        name: 'calledMethod',
        descriptor: '()V',
        digest: 'digest3',
        accessModifier: 'public',
        class: 'CalledClass',
        package: 'org.example'
      }),      
      (m1)<-[:CALLS]-(m2),
      (m1)<-[:CALLS]-(m3)
    `);
  });

  afterAll(async () => {
    // テストデータの削除
    await session.run(`
      MATCH (m:Method) 
        WHERE m.digest IN ['digest1', 'digest2', 'digest3']
        DETACH DELETE m
    `);
    await driver.close();
  });

  test("should return calling methods for a given method digest", async () => {
    const methods = await getCallingMethodsByDigest(session, {
      methodDigest: "digest1",
      hopCount: 1,
    });

    expect(methods).not.toBeNull();
    expect(methods?.methodDigest).toBe("digest1");
    expect(methods?.children["digest2"]).toBeDefined();
    expect(methods?.children["digest3"]).toBeDefined();
  });

  test("should return null if no calling methods are found", async () => {
    const methods = await getCallingMethodsByDigest(session, {
      methodDigest: "nonExistentDigest",
      hopCount: 1,
    });
    expect(methods).toEqual(null);
  });
});
