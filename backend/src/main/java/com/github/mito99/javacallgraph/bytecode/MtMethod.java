package com.github.mito99.javacallgraph.bytecode;

import java.util.List;
import java.util.stream.Stream;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.MethodInfo;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.val;

@ToString
@RequiredArgsConstructor
public class MtMethod implements MtCallable {

  private final MtClass classInfo;
  private final CtMethod ctMethod;

  @Override
  public String getName() {
    return this.ctMethod.getName();
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

  @SneakyThrows
  public String[] getParameterTypes() {
    return Stream.of(this.ctMethod.getParameterTypes())
        .map(CtClass::getName)
        .toArray(String[]::new);
  }

  @Override
  @SneakyThrows
  public List<MtCallable> getCalledMethods() {
    val mtClassPool = this.classInfo.getMtClassPool();
    return MtCallable.getCalledMethods(mtClassPool, this.getMethodInfo());
  }

  private MethodInfo getMethodInfo() {
    return this.ctMethod.getMethodInfo();
  }

  @Override
  public String getPackageName() {
    return this.classInfo.getPackageName();
  }

  @Override
  public MtClass getClassInfo() {
    return this.classInfo;
  }
}
