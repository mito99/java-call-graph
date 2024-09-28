package com.github.mito99.javacallgraph.bytecode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;
import lombok.SneakyThrows;
import lombok.val;

public interface MtCallable {

  String getName();

  String getClassName();

  String getDescriptor();

  List<MtCallable> getCalledMethods();

  @SneakyThrows
  static List<MtCallable> getCalledMethods(MtClassPool classPool, MethodInfo methodInfo) {
    val calledMethods = new ArrayList<MtCallable>();
    val constPool = methodInfo.getConstPool();
    CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
    if (codeAttribute == null) {
      return calledMethods;
    }

    val invokeOpcodeSet = new HashSet<>(
        List.of(
            Opcode.INVOKEVIRTUAL,
            Opcode.INVOKESTATIC,
            Opcode.INVOKESPECIAL,
            Opcode.INVOKEINTERFACE));

    CodeIterator codeIterator = codeAttribute.iterator();
    while (codeIterator.hasNext()) {
      int index = codeIterator.next();
      int opcode = codeIterator.byteAt(index);
      if (!invokeOpcodeSet.contains(opcode)) {
        continue;
      }

      int methodRefIndex = codeIterator.u16bitAt(index + 1);
      String className = constPool.getMethodrefClassName(methodRefIndex);
      MtClass calledClassInfo = classPool.get(className);

      String methodName = constPool.getMethodrefName(methodRefIndex);
      String methodDescriptor = constPool.getMethodrefType(methodRefIndex);

      MtCallable calledMethod = methodName.equals("<init>")
          ? calledClassInfo.getConstructor(methodDescriptor)
          : calledClassInfo.getMethod(methodName, methodDescriptor);

      if (calledMethod != null) {
        calledMethods.add(calledMethod);
      }
    }
    return calledMethods;
  }
}
