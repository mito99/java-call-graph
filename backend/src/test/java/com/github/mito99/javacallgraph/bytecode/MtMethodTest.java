package com.github.mito99.javacallgraph.bytecode;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MtMethodTest {

  private MtClassPool classPool;

  @BeforeEach
  public void setUp() {
    this.classPool = MtClassPool.getDefault();
  }

  @Test
  void testGetDescriptor() {
    final var classInfo = this.classPool.getClass("example.debug.A");
    final var methods = classInfo.getMethods();
    assertThat(methods).hasSize(1);

    final var method = methods.get("hello").get(0);
    final var descriptor = method.getDescriptor();
    assertThat(descriptor).isEqualTo("()V");
  }

  @Test
  void testGetCalledMethods() {
    final var classInfo = this.classPool.getClass("example.debug.A");
    final var method = classInfo.getMethod("hello");
    final var calledMethods = method.getCalledMethods();
    assertThat(calledMethods).hasSize(2);
  }
}
