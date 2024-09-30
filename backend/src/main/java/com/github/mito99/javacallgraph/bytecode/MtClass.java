package com.github.mito99.javacallgraph.bytecode;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import javassist.CtClass;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.val;

@EqualsAndHashCode
@ToString
@RequiredArgsConstructor
public class MtClass {

  @Getter(AccessLevel.PACKAGE)
  private final MtClassPool mtClassPool;
  private final CtClass ctClass;

  @Getter(lazy = true)
  private final Map<String, List<MtMethod>> methods = lazyGetMethods();

  public String getPackageName() {
    return this.ctClass.getPackageName();
  }

  public String getClassName() {
    return this.ctClass.getName();
  }

  public String getSimpleName() {
    return this.ctClass.getSimpleName();
  }

  private Map<String, List<MtMethod>> lazyGetMethods() {
    return Arrays.stream(this.ctClass.getDeclaredMethods())
        .map(method -> new MtMethod(this, method))
        .collect(Collectors.groupingBy(MtMethod::getName));
  }

  public List<MtConstructor> getConstructors() {
    return Arrays.stream(this.ctClass.getConstructors())
        .map(constructor -> new MtConstructor(this, constructor))
        .collect(Collectors.toList());
  }

  @SneakyThrows
  public MtMethod getMethod(String methodName, String methodDescriptor) {
    val ctMethod = this.ctClass.getMethod(methodName, methodDescriptor);
    return new MtMethod(this, ctMethod);
  }

  @SneakyThrows
  public MtMethod getMethod(String methodName, String... parameterTypes) {
    return getMethods().get(methodName).stream()
        .filter(method -> {
          final var methodParameterTypes = method.getParameterTypes();
          return Arrays.equals(methodParameterTypes, parameterTypes);
        })
        .findFirst()
        .orElseThrow(() -> new NoSuchElementException("Method not found: " + methodName));
  }

  @SneakyThrows
  public MtConstructor getConstructor(String descriptor) {
    val ctConstructor = this.ctClass.getConstructor(descriptor);
    return new MtConstructor(this, ctConstructor);
  }

  public String getHashCodeString() {
    var hash = this.hashCode();
    var hashString = String.valueOf(hash);
    return Base64.getEncoder().encodeToString(hashString.getBytes());
  }
}
