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

  public void registerModule(MtModule module) {
    log.info("Registering module: {}", module.getName());

    this.session.writeTransaction(tx -> {
      tx.run("CREATE (m:Module {name: $name, type: $type})",
          Values.parameters(
              "name", module.getName(),
              "type", module.getType()));
      registerClasses(tx, module.getName(), List.copyOf(module.getClasses().values()));
    });
  }

  public void createIndexes() {
    this.session.writeTransaction(tx -> {
      tx.run("CREATE INDEX IF NOT EXISTS FOR (n:Class) ON (n.hashCode)");
      tx.run("CREATE INDEX IF NOT EXISTS FOR (n:Method) ON (n.hashCode)");
    });
  }

  public void dropIndexes() {
    this.session.writeTransaction(tx -> {
      tx.run("DROP INDEX IF EXISTS FOR (n:Class) ON (n.hashCode)");
      tx.run("DROP INDEX IF EXISTS FOR (n:Method) ON (n.hashCode)");
    });
  }

  public void registerClasses(Transaction tx, String moduleName, List<MtClass> classes) {
    final var totalClasses = classes.size();
    for (var i = 0; i < totalClasses; i++) {
      final var clazz = classes.get(i);
      log.info("Registering class: {} ({}/{})", clazz.getClassName(), i + 1, totalClasses);

      upsertClass(tx, clazz);
      createModuleClassRelationship(tx, moduleName, clazz);
      registerClassMethods(tx, clazz);
    }
  }

  private void upsertClass(Transaction tx, MtClass clazz) {
    tx.run("MERGE (c:Class {hashCode: $hashCode}) " +
        "ON CREATE SET c.name = $name, c.package = $package",
        Values.parameters(
            "name", clazz.getSimpleName(),
            "package", clazz.getPackageName(),
            "hashCode", clazz.getHashCodeString()));
  }

  private void createModuleClassRelationship(Transaction tx, String moduleName, MtClass clazz) {
    tx.run("MATCH (m:Module {name: $moduleName}) " +
        "MATCH (c:Class {hashCode: $hashCode}) " +
        "CREATE (m)-[:HAS]->(c)",
        Values.parameters(
            "moduleName", moduleName,
            "hashCode", clazz.getHashCodeString()));
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

  private void createClassMethodRelationship(Transaction tx, String classHashCodeString, MtCallable method) {
    tx.run(
        "MATCH (c:Class {hashCode: $classHashCode}) " +
            "MATCH (m:Method {hashCode: $hashCode}) " +
            "CREATE (c)-[:HAS]->(m)",
        Values.parameters(
            "classHashCode", classHashCodeString,
            "hashCode", method.getHashCodeString()));
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
    tx.run(
        "MERGE (m:Method {hashCode: $hashCode}) " +
            "ON CREATE SET m.name = $name, m.class = $class, m.descriptor = $descriptor, m.package = $package",
        Values.parameters(
            "name", calledMethod.getName(),
            "class", calledMethod.getClassName(),
            "descriptor", calledMethod.getDescriptor(),
            "package", calledMethod.getPackageName(),
            "hashCode", calledMethod.getHashCodeString()));
  }

  private void createCallRelationship(Transaction tx, MtCallable method, MtCallable calledMethod) {
    tx.run(
        "MATCH (m1:Method {hashCode: $hashCode1}) " +
            "MATCH (m2:Method {hashCode: $hashCode2}) " +
            "CREATE (m1)-[:CALLS]->(m2)",
        Values.parameters(
            "hashCode1", method.getHashCodeString(),
            "hashCode2", calledMethod.getHashCodeString()));
  }

  public void deleteAllNodes() {
    this.session.writeTransaction(tx -> {
      tx.run("MATCH (n) DETACH DELETE n");
    });
  }
}
