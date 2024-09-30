package com.github.mito99.javacallgraph.bytecode;

import java.util.Map;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Builder
@ToString
@RequiredArgsConstructor
public class MtModule {

  @Getter
  private final String name;
  @Getter
  private final String type;
  @Getter
  private final Map<String, MtClass> classes;

  public MtClass getClass(String className) {
    return this.classes.get(className);
  }
}
