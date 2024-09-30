package com.github.mito99.javacallgraph.bytecode;

import java.util.List;

import javassist.CtConstructor;
import javassist.bytecode.MethodInfo;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.val;

@ToString
@RequiredArgsConstructor
public class MtConstructor implements MtCallable {

  private final static MtClassPool mtClassPool = MtClassPool.getDefault();

  private final MtClass classInfo;
  private final CtConstructor ctConstructor;

  @Override
  public String getName() {
    return this.ctConstructor.getName();
  }

  @Override
  public String getClassName() {
    return this.classInfo.getClassName();
  }

  @Override
  public String getDescriptor() {
    val methodInfo = this.getMethodInfo();
    return methodInfo.getDescriptor();
  }

  @Override
  @SneakyThrows
  public List<MtCallable> getCalledMethods() {
    return MtCallable.getCalledMethods(mtClassPool, this.getMethodInfo());
  }

  private MethodInfo getMethodInfo() {
    return this.ctConstructor.getMethodInfo();
  }

  @Override
  public String getPackageName() {
    return this.classInfo.getPackageName();
  }

  @Override
  public MtClass getClassInfo() {
    return this.classInfo;
  }

  @Override
  public int getModifiers() {
    return this.ctConstructor.getModifiers();
  }

}
