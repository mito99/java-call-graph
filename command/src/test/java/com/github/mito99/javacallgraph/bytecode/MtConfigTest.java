package com.github.mito99.javacallgraph.bytecode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.nio.file.Paths;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class MtConfigTest {

  @Test
  void ワイルドカード指定でJARファイルが取得できることを確認する() {
    var config = new MtConfig();
    config.setJarFilePaths(Arrays.asList(Paths.get("src/test/resources/module/libs/*.jar")));
    var jarFilePaths = config.getJarFilePaths();
    Arrays.sort(jarFilePaths, (o1, o2) -> o1.toString().compareTo(o2.toString()));

    assertEquals(2, jarFilePaths.length);
    assertThat(jarFilePaths[0].toString()).endsWith("text-a.jar");
    assertThat(jarFilePaths[1].toString()).endsWith("text-b.jar");
  }
}
