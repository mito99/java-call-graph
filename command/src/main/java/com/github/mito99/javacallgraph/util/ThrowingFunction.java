package com.github.mito99.javacallgraph.util;

import java.util.function.Function;

@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Exception> {
  R apply(T t) throws E;

  public static <T, R, E extends Exception> Function<T, R> tryFunction(
      ThrowingFunction<T, R, E> f) {
    return t -> {
      try {
        return f.apply(t);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    };
  }
}
