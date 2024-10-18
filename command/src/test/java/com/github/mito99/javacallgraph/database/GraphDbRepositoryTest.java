package com.github.mito99.javacallgraph.database;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.github.mito99.javacallgraph.bytecode.MtClass;
import com.github.mito99.javacallgraph.bytecode.MtModule;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.val;

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
  public void registerClasses() {

    // テスト対象のインスタンス化
    val repository = new GraphDbRepository(this.session);
    repository.deleteAllNodes();

    // ダミーデータの作成
    MtModule module = mock(MtModule.class);
    when(module.getName()).thenReturn("testModule");
    when(module.getType()).thenReturn("testType");
    List<MtClass> classes = ImmutableList.of(mock(MtClass.class));
    when(module.getClasses()).thenReturn(ImmutableMap.of("TestClass", classes.get(0)));
    when(classes.get(0).getClassName()).thenReturn("TestClass");
    when(classes.get(0).getPackageName()).thenReturn("testPackage");
    when(classes.get(0).getDigest()).thenReturn("testHashCode");

    // メソッドの呼び出し
    repository.registerClasses(this.session, "testModule", classes);

    // 検証
    val result = this.session.readTransaction(tx -> {
      return tx.run("MATCH (c:Class) RETURN c.name as name").list(r -> r.get("name").asString());
    });
    assertThat(result).containsExactly("TestClass");
  }
}
