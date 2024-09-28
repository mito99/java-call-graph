package com.github.mito99.javacallgraph.bytecode;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import lombok.val;

public class MtClassPoolTest {

  @Test
  void testGet() {
    val pool = MtClassPool.getDefault();
    val classInfo = pool.get("java.lang.String");
    assertEquals("java.lang.String", classInfo.getClassName());
  }
}
