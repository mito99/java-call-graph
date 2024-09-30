package com.github.mito99.javacallgraph.bytecode;

public class MtHelper {

  private MtHelper() {
    throw new UnsupportedOperationException("Utility class");
  }

  static String getAccessModifier(int modifiers) {
    if (java.lang.reflect.Modifier.isPublic(modifiers)) {
      return "public";
    } else if (java.lang.reflect.Modifier.isProtected(modifiers)) {
      return "protected";
    } else if (java.lang.reflect.Modifier.isPrivate(modifiers)) {
      return "private";
    }
    return "package-private";
  }
}
