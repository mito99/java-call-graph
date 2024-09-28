package com.github.mito99.javacallgraph.bytecode;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import lombok.val;

public class MtMethodTest {

  private MtClassPool classPool;

  @BeforeEach
  void setUp() {
    this.classPool = MtClassPool.getDefault();
  }

  @Test
  void testGetDescriptor() {
    val classInfo = this.classPool.get("example.debug.A");
    val methods = classInfo.getMethods();
    assertThat(methods).hasSize(1);

    val method = methods.get(0);
    val descriptor = method.getDescriptor();
    assertThat(descriptor).isEqualTo("()V");
  }

  @Test
  void testGetCalledMethods() {
    val classInfo = this.classPool.get("example.debug.A");
    val method = classInfo.getMethod("hello");
    val calledMethods = method.getCalledMethods();
    assertThat(calledMethods).hasSize(2);
  }
}
