package com.github.mito99.javacallgraph;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import com.github.mito99.javacallgraph.bytecode.MtClassPool;
import com.github.mito99.javacallgraph.bytecode.MtConfig;
import com.github.mito99.javacallgraph.database.GraphDbRepository;
import com.github.mito99.javacallgraph.database.GraphDbSession;
import lombok.val;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "javacallgraph", mixinStandardHelpOptions = true, description = "Java Call Graph",
    subcommands = {RegisterCommand.class, CleanupCommand.class, SetupCommand.class})
public class Main implements Runnable {

  public static void main(String[] args) {
    int exitCode = new CommandLine(new Main()).execute(args);
    System.exit(exitCode);
  }

  @Override
  public void run() {
    System.out.println("Use one of the subcommands: register or cleanup or setup.");
  }
}


@Command(name = "register", description = "Register module")
class RegisterCommand implements Callable<Integer> {
  @Option(names = {"-m", "--module"}, description = "Module name", required = true)
  private String moduleName;

  @Option(names = {"-t", "--type"}, description = "Module type", required = true)
  private String moduleType;

  @Option(names = {"-d", "--directory"}, description = "Directory path", required = true)
  private Path directoryPath;

  @Option(names = {"-i", "--include"}, description = "Include classes (正規表現で指定)")
  private String includeClassesRegex;

  @Option(names = {"-j", "--jar"}, description = "Jar file path(with glob pattern)", split = ",")
  private List<Path> jarFilePaths;

  @Override
  public Integer call() {
    MtConfig.getInstance().setIncludeClassesRegex(includeClassesRegex);
    MtConfig.getInstance().setJarFilePaths(jarFilePaths);

    try (val session = GraphDbSession.start()) {
      val module = MtClassPool.getModule(directoryPath, moduleName, moduleType);
      val reqpos = new GraphDbRepository(session);
      reqpos.registerModule(module);
      return 0;
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println("An error occurred: " + e.getMessage());
      return 1;
    }
  }
}


@Command(name = "cleanup", description = "Cleanup database")
class CleanupCommand implements Callable<Integer> {

  @Override
  public Integer call() {
    try (val session = GraphDbSession.start()) {
      val reqpos = new GraphDbRepository(session);
      reqpos.deleteAllNodes();
      reqpos.dropIndexes();
      return 0;
    } catch (Exception e) {
      System.err.println("An error occurred: " + e.getMessage());
      return 1;
    }
  }
}


@Command(name = "setup", description = "Setup database")
class SetupCommand implements Callable<Integer> {

  @Override
  public Integer call() {
    try (val session = GraphDbSession.start()) {
      val repo = new GraphDbRepository(session);
      repo.createIndexes();
      return 0;
    } catch (Exception e) {
      System.err.println("An error occurred: " + e.getMessage());
      return 1;
    }
  }
}
