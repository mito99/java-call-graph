package com.github.mito99.javacallgraph.bytecode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class MtConfigTest {

  @Test
  void ワイルドカード指定でJARファイルが取得できることを確認する() {
    var config = new MtConfig();
    config.setJarFilePaths(Arrays.asList(Paths.get("src/test/resources/module/libs/*.jar")));
    var jarFilePaths = config.getJarFilePaths().stream().sorted().collect(Collectors.toList());

    assertEquals(2, jarFilePaths.size());
    assertThat(jarFilePaths.get(0).toString()).endsWith("text-a.jar");
    assertThat(jarFilePaths.get(1).toString()).endsWith("text-b.jar");
  }
}
