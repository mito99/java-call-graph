package com.github.mito99.javacallgraph.bytecode;

import java.util.List;

import javassist.CtMethod;
import javassist.bytecode.MethodInfo;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.val;

@ToString
@RequiredArgsConstructor
public class MtMethod implements MtCallable {

  private final static MtClassPool mtClassPool = MtClassPool.getDefault();

  private final MtClass classInfo;
  private final CtMethod ctMethod;

  public String getName() {
    return this.ctMethod.getName();
  }

  public String getClassName() {
    return this.classInfo.getClassName();
  }

  public String getDescriptor() {
    val methodInfo = this.getMethodInfo();
    return methodInfo.getDescriptor();
  }

  @SneakyThrows
  public List<MtCallable> getCalledMethods() {
    return MtCallable.getCalledMethods(mtClassPool, this.getMethodInfo());
  }

  private MethodInfo getMethodInfo() {
    return this.ctMethod.getMethodInfo();
  }
}
