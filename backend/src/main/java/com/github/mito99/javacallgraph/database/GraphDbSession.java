package com.github.mito99.javacallgraph.database;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.val;

public class GraphDbSession implements AutoCloseable {

  private final Session session;

  private GraphDbSession(Session session) {
    this.session = session;
  }

  public static GraphDbSession start() {
    val dotenv = Dotenv.load();
    val uri = dotenv.get("NEO4J_URI");
    val user = dotenv.get("NEO4J_USER");
    val password = dotenv.get("NEO4J_PASSWORD");
    val driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    val session = driver.session();
    return new GraphDbSession(session);
  }

  @Override
  public void close() {
    this.session.close();
  }
}
