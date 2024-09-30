package com.github.mito99.javacallgraph.bytecode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javassist.ClassPool;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

@RequiredArgsConstructor
public class MtClassPool {

  private final ClassPool classPool;

  public static MtClassPool getDefault() {
    return new MtClassPool(ClassPool.getDefault());
  }

  @SneakyThrows
  public void appendClassPath(String path) {
    this.classPool.appendClassPath(path);
  }

  @SneakyThrows
  public MtClass getClass(String className) {
    return new MtClass(this, this.classPool.get(className));
  }

  @SneakyThrows
  public static MtModule getModule(Path path, String moduleName, String moduleType) {
    val classPool = MtClassPool.getDefault();
    classPool.appendClassPath(path.toString());

    val dir = path.toFile();
    if (!dir.exists()) {
      throw new IllegalArgumentException("Module directory not found: " + path);
    }

    val classes = Files.walk(path).filter(Files::isRegularFile)
        .filter(p -> p.toString().endsWith(".class"))
        .filter(p -> !p.toString().contains("module-info.class"))
        .filter(p -> !p.toString().contains("package-info.class"))
        .map(p -> {
          val relativePath = path.relativize(p).toString();
          val className = relativePath.replace("/", ".")
              .replace(".class", "");
          return Map.entry(className, classPool.getClass(className));
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    return new MtModule(moduleName, moduleType, classes);
  }
}
