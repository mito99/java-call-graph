package com.github.mito99.javacallgraph.bytecode;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MtClassTest {

  private MtClassPool classPool;

  @BeforeEach
  public void setUp() {
    this.classPool = MtClassPool.getDefault();
  }

  @Test
  void testGET() {
    final var classInfo = this.classPool.getClassOrThrow("example.debug.A");
    assertThat(classInfo.getClassName()).isEqualTo("example.debug.A");
    assertThat(classInfo.getPackageName()).isEqualTo("example.debug");
    assertThat(classInfo.getSimpleName()).isEqualTo("A");
  }
}
