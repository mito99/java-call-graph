package com.github.mito99.javacallgraph.database;

import java.util.function.Consumer;
import java.util.function.Function;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.val;

public class GraphDbSession implements AutoCloseable {

  private final Session session;

  private GraphDbSession(Session session) {
    this.session = session;
  }

  public static GraphDbSession start() {
    val dotenv = Dotenv.configure().ignoreIfMissing().directory(".").load();

    val uri = dotenv.get("NEO4J_URI");
    val user = dotenv.get("NEO4J_USER");
    val password = dotenv.get("NEO4J_PASSWORD");
    val driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    val session = driver.session();
    return new GraphDbSession(session);
  }

  public void writeTransaction(Consumer<Transaction> action) {
    try (val tx = this.session.beginTransaction()) {
      action.accept(tx);
      tx.commit();
    }
  }

  public <T> T writeTransaction(Function<Transaction, T> action) {
    try (val tx = this.session.beginTransaction()) {
      val result = action.apply(tx);
      tx.commit();
      return result;
    }
  }

  public <T> T readTransaction(Function<Transaction, T> action) {
    try (val tx = this.session.beginTransaction()) {
      return action.apply(tx);
    }
  }

  @Override
  public void close() {
    this.session.close();
  }
}
