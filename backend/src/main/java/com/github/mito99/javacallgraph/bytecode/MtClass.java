package com.github.mito99.javacallgraph.bytecode;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javassist.CtClass;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.val;

@ToString
@RequiredArgsConstructor
public class MtClass {

  private final CtClass ctClass;

  public String getPackageName() {
    return this.ctClass.getPackageName();
  }

  public String getClassName() {
    return this.ctClass.getName();
  }

  public String getSimpleName() {
    return this.ctClass.getSimpleName();
  }

  public List<MtMethod> getMethods() {
    return Arrays.stream(this.ctClass.getDeclaredMethods())
        .map(method -> new MtMethod(this, method))
        .collect(Collectors.toList());
  }

  @SneakyThrows
  public MtMethod getMethod(String methodName, String methodDescriptor) {
    val ctMethod = this.ctClass.getMethod(methodName, methodDescriptor);
    return new MtMethod(this, ctMethod);
  }

  @SneakyThrows
  public MtMethod getMethod(String methodName, CtClass... parameterTypes) {
    val ctMethod = this.ctClass.getDeclaredMethod(methodName, parameterTypes);
    return new MtMethod(this, ctMethod);
  }

  @SneakyThrows
  public MtConstructor getConstructor(String descriptor) {
    val ctConstructor = this.ctClass.getConstructor(descriptor);
    return new MtConstructor(this, ctConstructor);
  }
}
