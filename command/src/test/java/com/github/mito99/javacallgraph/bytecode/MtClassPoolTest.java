package com.github.mito99.javacallgraph.bytecode;

import static org.assertj.core.api.Assertions.assertThat;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import lombok.SneakyThrows;
import lombok.val;

public class MtClassPoolTest {

  private final static String TEST_DIR = "src/test/resources/module";

  @Test
  void クラス名を取得するテスト() {
    val pool = MtClassPool.getDefault();
    val classInfo = pool.getClassOrThrow("java.lang.String");
    assertThat(classInfo.getClassName()).isEqualTo("java.lang.String");
  }

  @Test
  @SneakyThrows
  void モジュールを取得するテスト() {
    val modulePath = Paths.get(TEST_DIR).resolve("commons-io-2.17.0");
    val module = MtClassPool.getModule(modulePath, "commons-io-2.17.0", "jar");
    assertThat(module.getName()).isEqualTo("commons-io-2.17.0");
    assertThat(module.getType()).isEqualTo("jar");
    assertThat(module.getClasses()).hasSize(335);
  }

  @Test
  void モジュールからクラスを取得するテスト() {
    val modulePath = Paths.get(TEST_DIR).resolve("commons-io-2.17.0");
    val module = MtClassPool.getModule(modulePath, "commons-io-2.17.0", "jar");
    val classInfo = module.getClass("org.apache.commons.io.FileUtils");
    assertThat(classInfo.getClassName()).isEqualTo("org.apache.commons.io.FileUtils");

    val method = classInfo.getMethod("copyFile", "java.io.File", "java.io.File");
    val calledMethods = method.getCalledMethods();
    assertThat(calledMethods).hasSize(1);
  }
}
