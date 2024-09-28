package com.github.mito99.javacallgraph.bytecode;

import javassist.ClassPool;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class MtClassPool {

  private final static MtClassPool DEFAULT = new MtClassPool(ClassPool.getDefault());

  private final ClassPool classPool;

  public static MtClassPool getDefault() {
    return DEFAULT;
  }

  @SneakyThrows
  public MtClass get(String className) {
    return new MtClass(this.classPool.get(className));
  }
}
