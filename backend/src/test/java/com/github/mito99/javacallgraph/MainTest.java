package com.github.mito99.javacallgraph;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class MainTest {

  @Nested
  public class MainCommandTest {
    @Test
    void testMain() {
      Main.main(new String[] {
          "register",
          "-m", "commons-io",
          "-t", "jar",
          "-d", "src/test/resources/module/commons-io-2.17.0"
      });
    }
  }

  @Nested
  public class DeleteCommandTest {

    @Test
    void testDeleteCommand() {
      Main.main(new String[] {
          "delete"
      });
    }
  }
}
