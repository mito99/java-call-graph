package com.github.mito99.javacallgraph.database;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.github.mito99.javacallgraph.bytecode.MtClass;
import com.github.mito99.javacallgraph.bytecode.MtModule;

public class GraphDbRepositoryTest {

  private GraphDbSession session;

  @BeforeEach
  public void setUp() {
    this.session = GraphDbSession.start();
  }

  @AfterEach
  public void tearDown() {
    this.session.close();
  }

  @Test
  void registerClasses() {

    // テスト対象のインスタンス化
    var repository = new GraphDbRepository(this.session);
    repository.deleteAllNodes();

    // ダミーデータの作成
    MtModule module = mock(MtModule.class);
    when(module.getName()).thenReturn("testModule");
    when(module.getType()).thenReturn("testType");
    List<MtClass> classes = List.of(mock(MtClass.class));
    when(module.getClasses()).thenReturn(Map.of("TestClass", classes.get(0)));
    when(classes.get(0).getClassName()).thenReturn("TestClass");
    when(classes.get(0).getPackageName()).thenReturn("testPackage");
    when(classes.get(0).getDigest()).thenReturn("testHashCode");

    // メソッドの呼び出し
    repository.registerClasses(this.session, "testModule", classes);

    // 検証
    var result = this.session.readTransaction(tx -> {
      return tx.run("MATCH (c:Class) RETURN c.name as name").list(r -> r.get("name").asString());
    });
    assertThat(result).containsExactly("TestClass");
  }
}
