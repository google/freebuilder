/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.inferred.freebuilder.processor.source;

import static com.google.common.truth.Truth.assertThat;
import static java.util.stream.Collectors.joining;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleTypeVisitor8;
import org.inferred.freebuilder.processor.model.MethodFinder;
import org.inferred.freebuilder.processor.source.testing.ModelRule;
import org.junit.ClassRule;
import org.junit.Test;

public class MethodFinderTest {

  @ClassRule public static ModelRule model = new ModelRule();

  abstract static class ClassOne {
    abstract void method();

    abstract void method(int x);

    abstract int method(double x);
  }

  @Test
  public void testNoInheritanceClass() {
    assertThat(methodsOn(ClassOne.class))
        .containsExactly(
            "void ClassOne::method()",
            "void ClassOne::method(int)",
            "int ClassOne::method(double)");
  }

  private interface InterfaceOne {
    void method();
  }

  @Test
  public void testNoInheritanceInterface() {
    assertThat(methodsOn(InterfaceOne.class)).containsExactly("void InterfaceOne::method()");
  }

  private abstract static class SingleInterface implements InterfaceOne {}

  @Test
  public void testSingleInterface() {
    assertThat(methodsOn(SingleInterface.class)).containsExactly("void InterfaceOne::method()");
  }

  private abstract static class SingleSuperclass extends ClassOne {}

  @Test
  public void testSingleSuperclassMethodInterface() {
    assertThat(methodsOn(SingleSuperclass.class))
        .containsExactly(
            "void ClassOne::method()",
            "void ClassOne::method(int)",
            "int ClassOne::method(double)");
  }

  interface InterfaceTwo extends InterfaceOne {}

  @Test
  public void testSimpleInterfaceHierarchy() {
    assertThat(methodsOn(InterfaceTwo.class)).containsExactly("void InterfaceOne::method()");
  }

  private abstract static class DiamondInheritance extends SingleInterface
      implements InterfaceTwo {}

  @Test
  public void testDiamondInheritance() {
    assertThat(methodsOn(DiamondInheritance.class)).containsExactly("void InterfaceOne::method()");
  }

  private interface InterfaceThree {
    void method();
  }

  private interface InterfaceFour {
    void method();
  }

  private abstract static class MultipleMethodsSameSignature
      implements InterfaceOne, InterfaceTwo, InterfaceThree, InterfaceFour {}

  @Test
  public void testMultipleMethodsSameSignature() {
    ImmutableList<String> methods = methodsOn(MultipleMethodsSameSignature.class);
    // When choosing between multiple unrelated interfaces defining the same method, pick any
    assertThat(methods)
        .containsAnyOf(
            "void InterfaceOne::method()",
            "void InterfaceThree::method()",
            "void InterfaceFour::method()");
    assertThat(methods).hasSize(1);
  }

  private abstract static class MultipleMethodsSameSignatureWithSuperclass extends ClassOne
      implements InterfaceOne {}

  @Test
  public void testMultipleMethodsSameSignatureWithSuperclass() {
    // When choosing between InterfaceOne::method and ClassOne::method, pick the concrete type.
    assertThat(methodsOn(MultipleMethodsSameSignatureWithSuperclass.class))
        .containsExactly(
            "void ClassOne::method()",
            "void ClassOne::method(int)",
            "int ClassOne::method(double)");
  }

  private interface MultipleMethodsSameSignatureRedeclared
      extends InterfaceOne, InterfaceTwo, InterfaceThree, InterfaceFour {
    @Override
    void method();
  }

  @Test
  public void testMultipleMethodsSameSignatureRedeclared() {
    ImmutableList<String> methods = methodsOn(MultipleMethodsSameSignatureRedeclared.class);
    // When choosing between multiple interfaces defining the same method, pick the most derived
    // one.
    assertThat(methods).containsExactly("void MultipleMethodsSameSignatureRedeclared::method()");
  }

  private abstract static class WideMethodsSuperclass {
    abstract Object doSomething(Integer x) throws IOException;
  }

  private static class NarrowMethodSubclass extends WideMethodsSuperclass {
    @Override
    Integer doSomething(Integer x) {
      throw new UnsupportedOperationException();
    }
  }

  @Test
  public void testSignatureNarrowing() {
    assertThat(methodsOn(NarrowMethodSubclass.class))
        .containsExactly("Integer NarrowMethodSubclass::doSomething(Integer)");
  }

  private interface Receiver<T> {
    void accept(T object);
  }

  private static class MySink implements Receiver<String> {
    @Override
    public void accept(String object) {
      throw new UnsupportedOperationException();
    }
  }

  @Test
  public void testGenericSignatureOverriding() {
    assertThat(methodsOn(MySink.class)).containsExactly("void MySink::accept(String)");
  }

  @Test
  public void testSkipsErrorTypes() {
    TypeElement testClass =
        model.newType(
            "package com.example;",
            "class TestClass extends MissingType implements OtherMissingType {",
            "  public int foo(short a);",
            "}");
    List<ErrorType> errorTypes = new ArrayList<>();

    List<String> methods =
        toStrings(MethodFinder.methodsOn(testClass, model.elementUtils(), errorTypes::add));

    assertThat(methods).containsExactly("int TestClass::foo(short)");
    assertThat(errorTypes).hasSize(2);
  }

  interface RedeclaresToString {
    @Override
    String toString();
  }

  @Test
  public void testIgnoredObjectMethodRedeclarations() {
    List<String> methods = methodsOn(RedeclaresToString.class);

    assertThat(methods).isEmpty();
  }

  class OverridesToString {
    @Override
    public String toString() {
      return "OverridesToString";
    }
  }

  @Test
  public void testKeepsObjectMethodOverrides() {
    List<String> methods = methodsOn(OverridesToString.class);

    assertThat(methods).containsExactly("String OverridesToString::toString()");
  }

  // Utility methods
  ///////////////////////////////////////////////////////////////////////////////////////////////

  private static ImmutableList<String> methodsOn(Class<?> cls) {
    return toStrings(
        MethodFinder.methodsOn(
            model.typeElement(cls),
            model.elementUtils(),
            errorType -> fail("Error type encountered: " + errorType)));
  }

  private static ImmutableList<String> toStrings(Iterable<? extends ExecutableElement> methods) {
    ImmutableList.Builder<String> resultBuilder = ImmutableList.builder();
    for (ExecutableElement method : methods) {
      resultBuilder.add(
          STRINGIFY.visit(method.getReturnType())
              + " "
              + method.getEnclosingElement().getSimpleName()
              + "::"
              + method.getSimpleName()
              + "("
              + variablesToStrings(method.getParameters()).stream().collect(joining(", "))
              + ")");
    }
    return resultBuilder.build();
  }

  private static ImmutableList<CharSequence> variablesToStrings(
      Iterable<? extends VariableElement> variables) {
    ImmutableList.Builder<CharSequence> resultBuilder = ImmutableList.builder();
    for (VariableElement variable : variables) {
      resultBuilder.add(STRINGIFY.visit(variable.asType()));
    }
    return resultBuilder.build();
  }

  private static final SimpleTypeVisitor8<CharSequence, ?> STRINGIFY =
      new SimpleTypeVisitor8<CharSequence, Void>() {

        @Override
        public Name visitDeclared(DeclaredType t, Void p) {
          return t.asElement().getSimpleName();
        }

        @Override
        protected String defaultAction(TypeMirror e, Void p) {
          return e.toString();
        }
      };
}
