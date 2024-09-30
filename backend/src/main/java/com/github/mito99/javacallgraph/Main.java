package com.github.mito99.javacallgraph;

import java.nio.file.Path;
import java.util.concurrent.Callable;

import com.github.mito99.javacallgraph.bytecode.MtClassPool;
import com.github.mito99.javacallgraph.database.GraphDbRepository;
import com.github.mito99.javacallgraph.database.GraphDbSession;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "javacallgraph", mixinStandardHelpOptions = true, description = "Java Call Graph", subcommands = {
    RegisterCommand.class,
    DeleteCommand.class,
})
public class Main implements Runnable {

  public static void main(String[] args) {
    int exitCode = new CommandLine(new Main()).execute(args);
    System.exit(exitCode);
  }

  @Override
  public void run() {
    System.out.println("Use one of the subcommands: register or delete.");
  }
}

@Command(name = "register", description = "Register module")
class RegisterCommand implements Callable<Integer> {
  @Option(names = { "-m", "--module" }, description = "Module name", required = true)
  private String moduleName;

  @Option(names = { "-t", "--type" }, description = "Module type", required = true)
  private String moduleType;

  @Option(names = { "-d", "--directory" }, description = "Directory path", required = true)
  private Path directoryPath;

  @Override
  public Integer call() {
    try (var session = GraphDbSession.start()) {
      final var module = MtClassPool.getModule(directoryPath, moduleName, moduleType);
      final var reqpos = new GraphDbRepository(session);
      reqpos.registerModule(module);
      return 0;
    } catch (Exception e) {
      System.err.println("An error occurred: " + e.getMessage());
      return 1;
    }
  }
}

@Command(name = "delete", description = "Delete all nodes")
class DeleteCommand implements Callable<Integer> {

  @Override
  public Integer call() {
    try (var session = GraphDbSession.start()) {
      final var reqpos = new GraphDbRepository(session);
      reqpos.deleteAllNodes();
      return 0;
    } catch (Exception e) {
      System.err.println("An error occurred: " + e.getMessage());
      return 1;
    }
  }
}