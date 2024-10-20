package com.github.mito99.javacallgraph.bytecode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javassist.ClassPath;
import javassist.ClassPool;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;
import com.google.common.collect.Maps;

@Slf4j
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
  public void appendClassPath(ClassPath path) {
    this.classPool.appendClassPath(path);
  }

  @SneakyThrows
  public Optional<MtClass> getClass(String className) {
    val mtConfig = MtConfig.getInstance();
    val includeClassesRegex = mtConfig.getIncludeClassesRegex();
    if (className.matches(includeClassesRegex)) {
      val classFile = this.classPool.getOrNull(className);
      return classFile == null ? Optional.empty() : Optional.of(new MtClass(this, classFile));
    }
    return Optional.empty();
  }

  @SneakyThrows
  public MtClass getClassOrThrow(String className) {
    val mtClassOptional = getClass(className);
    return mtClassOptional
        .orElseThrow(() -> new IllegalArgumentException("Class not found: " + className));
  }

  @SneakyThrows
  public static MtModule getModule(Path path, String moduleName, String moduleType) {
    val classPool = MtClassPool.getDefault();
    classPool.appendClassPath(path.toString());

    val jarFilePaths = MtConfig.getInstance().getJarFilePaths();
    jarFilePaths.forEach(jarFilePath -> {
      val jarFilePathStr = jarFilePath.toString();
      log.info("Append ClassPath: " + jarFilePathStr);
      classPool.appendClassPath(jarFilePathStr);
    });

    val dir = path.toFile();
    if (!dir.exists()) {
      throw new IllegalArgumentException("Module directory not found: " + path);
    }

    val classes =
        Files.walk(path).filter(Files::isRegularFile).filter(p -> p.toString().endsWith(".class"))
            .filter(p -> !p.toString().contains("module-info.class"))
            .filter(p -> !p.toString().contains("package-info.class")).map(p -> {
              val relativePath = path.relativize(p).toString();
              val className = relativePath.replace("/", ".").replace(".class", "");
              return new ClassInfo(className, classPool.getClass(className));
            }).filter(p -> p.mtClass.isPresent())
            .map(p -> Maps.immutableEntry(p.className, p.mtClass.get()))
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    return new MtModule(moduleName, moduleType, classes);
  }

  @AllArgsConstructor
  static class ClassInfo {
    String className;
    Optional<MtClass> mtClass;
  }
}
