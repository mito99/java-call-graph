package com.github.mito99.javacallgraph.database;

import java.util.List;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.Values;
import com.github.mito99.javacallgraph.bytecode.MtCallable;
import com.github.mito99.javacallgraph.bytecode.MtClass;
import com.github.mito99.javacallgraph.bytecode.MtModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class GraphDbRepository {

  private final GraphDbSession session;
  private final String projectPrefix = "JCG";

  public void registerModule(MtModule module) {
    log.info("Registering module: {}", module.getName());

    this.session.writeTransaction(tx -> {
      tx.run("MERGE (m:" + projectPrefix + ":Module {name: $name, type: $type})",
          Values.parameters("name", module.getName(), "type", module.getType()));
    });

    registerClasses(session, module.getName(), List.copyOf(module.getClasses().values()));
  }

  public void createIndexes() {
    this.session.writeTransaction(tx -> {
      tx.run("CREATE INDEX class_hashcode_index IF NOT EXISTS FOR (n:Class) ON (n.hashCode)");
      tx.run("CREATE INDEX method_hashcode_index IF NOT EXISTS FOR (n:Method) ON (n.hashCode)");
    });
  }

  public void dropIndexes() {
    this.session.writeTransaction(tx -> {
      tx.run("DROP INDEX class_hashcode_index IF EXISTS");
      tx.run("DROP INDEX method_hashcode_index IF EXISTS");
    });
  }

  public void registerClasses(GraphDbSession session, String moduleName, List<MtClass> classes) {
    final var totalClasses = classes.size();
    for (var i = 0; i < totalClasses; i++) {
      final var clazz = classes.get(i);
      log.info("Registering class: {} ({}/{})", clazz.getClassName(), i + 1, totalClasses);

      session.writeTransaction(tx -> {
        upsertClass(tx, clazz);
        createModuleClassRelationship(tx, moduleName, clazz);
        registerClassMethods(tx, clazz);
      });
    }
  }

  private void upsertClass(Transaction tx, MtClass clazz) {
    tx.run("MERGE (c:" + projectPrefix + ":Class {hashCode: $hashCode}) "
        + "ON CREATE SET c.name = $name, c.package = $package, c.accessModifier = $accessModifier",
        Values.parameters("name", clazz.getSimpleName(), "package", clazz.getPackageName(),
            "accessModifier", clazz.getAccessModifier(), "hashCode", clazz.getHashCodeString()));
  }

  private void createModuleClassRelationship(Transaction tx, String moduleName, MtClass clazz) {
    tx.run(
        "MATCH (m:" + projectPrefix + ":Module {name: $moduleName}) " + "MATCH (c:" + projectPrefix
            + ":Class {hashCode: $hashCode}) " + "MERGE (m)-[:HAS]->(c)",
        Values.parameters("moduleName", moduleName, "hashCode", clazz.getHashCodeString()));
  }

  private void registerClassMethods(Transaction tx, MtClass clazz) {
    final var constructors = clazz.getConstructors();
    for (var constructor : constructors) {
      upsertMethod(tx, constructor);
      createClassMethodRelationship(tx, clazz.getClassName(), constructor);
      registerCalledMethods(tx, constructor);
    }

    final var methods = clazz.getMethods().values();
    for (var methodList : methods) {
      for (MtCallable method : methodList) {
        upsertMethod(tx, method);
        createClassMethodRelationship(tx, clazz.getClassName(), method);
        registerCalledMethods(tx, method);
      }
    }
  }

  private void createClassMethodRelationship(Transaction tx, String classHashCodeString,
      MtCallable method) {
    tx.run(
        "MATCH (c:" + projectPrefix + ":Class {hashCode: $classHashCode}) " + "MATCH (m:"
            + projectPrefix + ":Method {hashCode: $hashCode}) " + "MERGE (c)-[:HAS]->(m)",
        Values.parameters("classHashCode", classHashCodeString, "hashCode",
            method.getHashCodeString()));
  }

  private void registerCalledMethods(Transaction tx, MtCallable method) {
    final var calledMethods = method.getCalledMethods();
    for (var calledMethod : calledMethods) {
      final var calledClass = calledMethod.getClassInfo();
      upsertClass(tx, calledClass);
      upsertMethod(tx, calledMethod);
      createClassMethodRelationship(tx, calledClass.getHashCodeString(), calledMethod);
      createCallRelationship(tx, method, calledMethod);
    }
  }

  private void upsertMethod(Transaction tx, MtCallable calledMethod) {
    tx.run("MERGE (m:" + projectPrefix + ":Method {hashCode: $hashCode}) "
        + "ON CREATE SET m.name = $name, m.class = $class, m.descriptor = $descriptor, m.package = $package, m.accessModifier = $accessModifier",
        Values.parameters("name", calledMethod.getName(), "class", calledMethod.getClassName(),
            "descriptor", calledMethod.getDescriptor(), "package", calledMethod.getPackageName(),
            "accessModifier", calledMethod.getAccessModifier(), "hashCode",
            calledMethod.getHashCodeString()));
  }

  private void createCallRelationship(Transaction tx, MtCallable method, MtCallable calledMethod) {
    tx.run(
        "MATCH (m1:" + projectPrefix + ":Method {hashCode: $hashCode1}) " + "MATCH (m2:"
            + projectPrefix + ":Method {hashCode: $hashCode2}) " + "MERGE (m1)-[:CALLS]->(m2)",
        Values.parameters("hashCode1", method.getHashCodeString(), "hashCode2",
            calledMethod.getHashCodeString()));
  }

  public void deleteAllNodes() {
    var hasMoreNodes = true;
    final var batchSize = 2000;
    while (hasMoreNodes) {
      log.info("Deleting nodes in batch of {}", batchSize);
      hasMoreNodes = this.session.writeTransaction(tx -> {
        var result = tx.run(
            "MATCH (n) WITH n LIMIT $batchSize DETACH DELETE n RETURN COUNT(n) AS deletedCount",
            org.neo4j.driver.Values.parameters("batchSize", batchSize));
        int deletedCount = result.single().get("deletedCount").asInt();
        return deletedCount > 0;
      });
    }
  }
}
