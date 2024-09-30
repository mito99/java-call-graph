package com.github.mito99.javacallgraph.bytecode;

import static org.assertj.core.api.Assertions.assertThat; // 追記

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import lombok.SneakyThrows;

public class MtClassPoolTest {

  private final static String TEST_DIR = "src/test/resources/module";

  @Test
  void クラス名を取得するテスト() {
    final var pool = MtClassPool.getDefault();
    final var classInfo = pool.getClass("java.lang.String");
    assertThat(classInfo.getClassName()).isEqualTo("java.lang.String");
  }

  @Test
  @SneakyThrows
  void モジュールを取得するテスト() {
    final var modulePath = Path.of(TEST_DIR, "commons-io-2.17.0");
    final var module = MtClassPool.getModule(modulePath, "commons-io-2.17.0", "jar");
    assertThat(module.getName()).isEqualTo("commons-io-2.17.0");
    assertThat(module.getType()).isEqualTo("jar");
    assertThat(module.getClasses()).hasSize(335);
  }

  @Test
  void モジュールからクラスを取得するテスト() {
    final var modulePath = Path.of(TEST_DIR, "commons-io-2.17.0");
    final var module = MtClassPool.getModule(modulePath, "commons-io-2.17.0", "jar");
    final var classInfo = module.getClass("org.apache.commons.io.FileUtils");
    assertThat(classInfo.getClassName()).isEqualTo("org.apache.commons.io.FileUtils");

    final var method = classInfo.getMethod("copyFile", "java.io.File", "java.io.File");
    final var calledMethods = method.getCalledMethods();
    assertThat(calledMethods).hasSize(1);
  }
}
