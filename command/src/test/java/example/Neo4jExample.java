package example;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.val;

public class Neo4jExample {

  public static void main(String[] args) {
    val dotenv = Dotenv.load();
    val uri = dotenv.get("NEO4J_URI");
    val user = dotenv.get("NEO4J_USER");
    val password = dotenv.get("NEO4J_PASSWORD");

    // ドライバーを作成
    try (Driver driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
        Session session = driver.session()) {

      // データの登録
      registerData(session);

      // データの取得
      retrieveData(session);
    }
  }

  // データを登録するメソッド
  private static void registerData(Session session) {
    String className = "com.example.MyClass";
    String methodName = "myMethod";

    // Neo4jにクラスとメソッドをノードとして登録し、呼び出し関係を作成
    session.run("MERGE (c:Class {name: $className})",
        org.neo4j.driver.Values.parameters("className", className));

    session.run("MERGE (m:Method {name: $methodName})",
        org.neo4j.driver.Values.parameters("methodName", methodName));

    session.run("MATCH (c:Class {name: $className}), (m:Method {name: $methodName}) " +
        "MERGE (c)-[:HAS_METHOD]->(m)",
        org.neo4j.driver.Values.parameters("className", className, "methodName", methodName));

    System.out.println("Data registered successfully.");
  }

  // データを取得するメソッド
  private static void retrieveData(Session session) {
    Result result = session.run("MATCH (c:Class)-[:HAS_METHOD]->(m:Method) " +
        "RETURN c.name AS className, m.name AS methodName");

    // 取得した結果を表示
    while (result.hasNext()) {
      Record record = result.next();
      String className = record.get("className").asString();
      String methodName = record.get("methodName").asString();

      System.out.println("Class: " + className + " calls Method: " + methodName);
    }
  }
}
