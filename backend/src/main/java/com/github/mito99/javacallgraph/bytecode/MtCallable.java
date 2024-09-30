package com.github.mito99.javacallgraph.bytecode;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;

import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;
import lombok.SneakyThrows;
import lombok.val;

public interface MtCallable {

  String getPackageName();

  String getName();

  String getClassName();

  String getDescriptor();

  MtClass getClassInfo();

  List<MtCallable> getCalledMethods();

  @SneakyThrows
  static List<MtCallable> getCalledMethods(
      MtClassPool classPool, MethodInfo methodInfo) {
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
      val className = constPool.getMethodrefClassName(methodRefIndex);
      val calledClassInfoOptional = classPool.getClass(className);
      if (calledClassInfoOptional.isEmpty()) {
        continue;
      }
      val calledClassInfo = calledClassInfoOptional.get();

      val methodName = constPool.getMethodrefName(methodRefIndex);
      val methodDescriptor = constPool.getMethodrefType(methodRefIndex);

      val calledMethod = methodName.equals("<init>")
          ? calledClassInfo.getConstructor(methodDescriptor)
          : calledClassInfo.getMethod(methodName, methodDescriptor);

      if (calledMethod != null) {
        calledMethods.add(calledMethod);
      }
    }
    return calledMethods;
  }

  default String getHashCodeString() {
    val pkg = this.getPackageName();
    val className = this.getClassName();
    val name = this.getName();
    val desc = this.getDescriptor();
    val hashString = pkg + "/" + className + "/" + name + "/" + desc;
    return Base64.getEncoder().encodeToString(hashString.getBytes());
  }
}
