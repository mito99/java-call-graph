package com.github.mito99.javacallgraph.bytecode;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import lombok.val;

public class MtClassTest {

  private MtClassPool classPool;

  @BeforeEach
  void setUp() {
    this.classPool = MtClassPool.getDefault();
  }

  @Test
  void testGET() {
    val classInfo = this.classPool.get("example.debug.A");
    assertThat(classInfo.getClassName()).isEqualTo("example.debug.A");
    assertThat(classInfo.getPackageName()).isEqualTo("example.debug");
    assertThat(classInfo.getSimpleName()).isEqualTo("A");
  }
}
