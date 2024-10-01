package com.github.mito99.javacallgraph.bytecode;

import static com.github.mito99.javacallgraph.util.ThrowingFunction.tryFunction;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import lombok.Setter;

public class MtConfig {

  private static final MtConfig instance = new MtConfig();

  @Setter
  private String includeClassesRegex;

  @Setter
  private List<Path> jarFilePaths;

  public static MtConfig getInstance() {
    return instance;
  }

  public String getIncludeClassesRegex() {
    if (Objects.isNull(this.includeClassesRegex) || this.includeClassesRegex.isEmpty()) {
      return ".*";
    }
    return this.includeClassesRegex;
  }

  public URL[] getJarFilePaths() {
    if (this.jarFilePaths == null) {
      return new URL[0];
    }

    return jarFilePaths.stream().flatMap(tryFunction(p -> {
      final var dir = p.toAbsolutePath().getParent();
      final var file = p.getFileName();

      var matcherPattern = "glob:" + file;
      var matcher = FileSystems.getDefault().getPathMatcher(matcherPattern);
      System.out.println(matcher.matches(Paths.get("src/test/resources/module/libs/text-a.jar")));

      return Files.walk(dir).filter(Files::isRegularFile)
          .filter(p2 -> matcher.matches(p2.getFileName()));

    })).map(tryFunction(p -> {
      return p.toUri().toURL();
    })).toArray(URL[]::new);
  }
}

