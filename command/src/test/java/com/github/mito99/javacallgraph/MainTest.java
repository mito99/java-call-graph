package com.github.mito99.javacallgraph;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class MainTest {

  @Nested
  public class RegisterCommandTest {
    @Test
    void testMain() {
      Main.main(new String[] {"register", "-m", "commons-io", "-t", "jar", "-d",
          "src/test/resources/module/commons-io-2.17.0"});
    }

    @Test
    void testMainWithInvalidModuleName() {
      Main.main(new String[] {"register", "-m", "commons-io", "-t", "jar", "-d",
          "src/test/resources/module/commons-io-2.17.0", "-i",
          "^org\\.apache\\.commons\\.io\\..*"});
    }
  }

  @Nested
  public class CleanupCommandTest {

    @Test
    void testCleanupCommand() {
      Main.main(new String[] {"cleanup"});
    }
  }

  @Nested
  public class SetupCommandTest {

    @Test
    void testSetupCommand() {
      Main.main(new String[] {"setup"});
    }
  }
}
