package com.github.mito99.javacallgraph.bytecode;

import static com.github.mito99.javacallgraph.util.ThrowingFunction.tryFunction;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Setter;
import lombok.val;

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

  public List<Path> getJarFilePaths() {
    if (this.jarFilePaths == null) {
      return new ArrayList<>();
    }

    return jarFilePaths.stream().flatMap(tryFunction(p -> {
      val dir = p.toAbsolutePath().getParent();
      val file = p.getFileName();

      val matcherPattern = "glob:" + file;
      val matcher = FileSystems.getDefault().getPathMatcher(matcherPattern);

      return Files.walk(dir).filter(Files::isRegularFile)
          .filter(p2 -> matcher.matches(p2.getFileName()));

    })).collect(Collectors.toList());
  }
}

