package com.github.mito99.javacallgraph.bytecode;

import java.util.Objects;

import lombok.Setter;
import lombok.experimental.Accessors;

public class MtConfig {

  private static final MtConfig instance = new MtConfig();

  @Setter
  @Accessors(chain = true)
  private String includeClassesRegex;

  public static MtConfig getInstance() {
    return instance;
  }

  public String getIncludeClassesRegex() {
    if (Objects.isNull(this.includeClassesRegex)
        || this.includeClassesRegex.isEmpty()) {
      return ".*";
    }
    return this.includeClassesRegex;
  }

}
