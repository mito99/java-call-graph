package example;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;
import lombok.val;

public class MethodInvocationAnalyzer {
  public static void main(String[] args) throws Exception {
    // 対象クラスのクラス名を指定
    val targetClassName = "example.debug.A"; // 解析したいクラスの完全修飾名

    // クラスプールを作成してクラスをロード
    ClassPool pool = ClassPool.getDefault();
    CtClass ctClass = pool.get(targetClassName);

    // クラス内のすべてのメソッドを取得
    for (CtMethod method : ctClass.getDeclaredMethods()) {
      System.out.println("Analyzing method: " + method.getName());

      // メソッドのバイトコードを取得
      MethodInfo methodInfo = method.getMethodInfo();
      CodeAttribute codeAttribute = methodInfo.getCodeAttribute();

      if (codeAttribute == null) {
        continue; // バイトコードがない場合（抽象メソッドなど）
      }

      // バイトコードを命令単位で解析
      CodeIterator iterator = codeAttribute.iterator();
      while (iterator.hasNext()) {
        int index = iterator.next();
        int opcode = iterator.byteAt(index);

        // メソッド呼び出しの命令を確認
        if (opcode == Opcode.INVOKEVIRTUAL || opcode == Opcode.INVOKESTATIC ||
            opcode == Opcode.INVOKESPECIAL || opcode == Opcode.INVOKEINTERFACE) {
          // 呼び出されているメソッドのクラス名とメソッド名を取得
          int methodRefIndex = iterator.u16bitAt(index + 1);
          ConstPool constPool = methodInfo.getConstPool();
          String className = constPool.getMethodrefClassName(methodRefIndex);
          String methodName = constPool.getMethodrefName(methodRefIndex);
          String methodDescriptor = constPool.getMethodrefType(methodRefIndex);

          System.out.println(" - Calls method: " + className + "." + methodName + methodDescriptor);
        }
      }
    }
  }
}