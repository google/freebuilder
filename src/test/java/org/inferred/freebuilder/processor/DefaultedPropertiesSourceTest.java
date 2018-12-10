/*
 * Copyright 2015 Google Inc. All rights reserved.
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
package org.inferred.freebuilder.processor;

import static org.inferred.freebuilder.processor.GeneratedTypeSubject.assertThat;
import static org.inferred.freebuilder.processor.NamingConvention.BEAN;
import static org.inferred.freebuilder.processor.NamingConvention.PREFIXLESS;
import static org.inferred.freebuilder.processor.util.ClassTypeImpl.INTEGER;
import static org.inferred.freebuilder.processor.util.ClassTypeImpl.STRING;
import static org.inferred.freebuilder.processor.util.FunctionalType.unaryOperator;
import static org.inferred.freebuilder.processor.util.PrimitiveTypeImpl.INT;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import org.inferred.freebuilder.processor.util.QualifiedName;
import org.inferred.freebuilder.processor.util.feature.GuavaLibrary;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Set;

@RunWith(JUnit4.class)
public class DefaultedPropertiesSourceTest {

  @Test
  public void test_noGuava() {
    assertThat(builder(BEAN)).generates(
        "/** Auto-generated superclass of {@link Person.Builder}, "
            + "derived from the API of {@link Person}. */",
        "abstract class Person_Builder {",
        "",
        "  /** Creates a new builder using {@code value} as a template. */",
        "  public static Person.Builder from(Person value) {",
        "    return new Person.Builder().mergeFrom(value);",
        "  }",
        "",
        "  private String name;",
        "  private int age;",
        "",
        "  /**",
        "   * Sets the value to be returned by {@link Person#getName()}.",
        "   *",
        "   * @return this {@code Builder} object",
        "   * @throws NullPointerException if {@code name} is null",
        "   */",
        "  public Person.Builder setName(String name) {",
        "    this.name = Objects.requireNonNull(name);",
        "    return (Person.Builder) this;",
        "  }",
        "",
        "  /**",
        "   * Replaces the value to be returned by {@link Person#getName()} by applying"
            + " {@code mapper} to it",
        "   * and using the result.",
        "   *",
        "   * @return this {@code Builder} object",
        "   * @throws NullPointerException if {@code mapper} is null or returns null",
        "   */",
        "  public Person.Builder mapName(UnaryOperator<String> mapper) {",
        "    return setName(mapper.apply(getName()));",
        "  }",
        "",
        "  /** Returns the value that will be returned by {@link Person#getName()}. */",
        "  public String getName() {",
        "    return name;",
        "  }",
        "",
        "  /**",
        "   * Sets the value to be returned by {@link Person#getAge()}.",
        "   *",
        "   * @return this {@code Builder} object",
        "   */",
        "  public Person.Builder setAge(int age) {",
        "    this.age = age;",
        "    return (Person.Builder) this;",
        "  }",
        "",
        "  /**",
        "   * Replaces the value to be returned by {@link Person#getAge()} by applying"
            + " {@code mapper} to it",
        "   * and using the result.",
        "   *",
        "   * @return this {@code Builder} object",
        "   * @throws NullPointerException if {@code mapper} is null or returns null",
        "   */",
        "  public Person.Builder mapAge(UnaryOperator<Integer> mapper) {",
        "    return setAge(mapper.apply(getAge()));",
        "  }",
        "",
        "  /** Returns the value that will be returned by {@link Person#getAge()}. */",
        "  public int getAge() {",
        "    return age;",
        "  }",
        "",
        "  /** Sets all property values using the given {@code Person} as a template. */",
        "  public Person.Builder mergeFrom(Person value) {",
        "    Person_Builder _defaults = new Person.Builder();",
        "    if (!Objects.equals(value.getName(), _defaults.getName())) {",
        "      setName(value.getName());",
        "    }",
        "    if (value.getAge() != _defaults.getAge()) {",
        "      setAge(value.getAge());",
        "    }",
        "    return (Person.Builder) this;",
        "  }",
        "",
        "  /**",
        "   * Copies values from the given {@code Builder}. "
            + "Does not affect any properties not set on the",
        "   * input.",
        "   */",
        "  public Person.Builder mergeFrom(Person.Builder template) {",
        "    Person_Builder _defaults = new Person.Builder();",
        "    if (!Objects.equals(template.getName(), _defaults.getName())) {",
        "      setName(template.getName());",
        "    }",
        "    if (template.getAge() != _defaults.getAge()) {",
        "      setAge(template.getAge());",
        "    }",
        "    return (Person.Builder) this;",
        "  }",
        "",
        "  /** Resets the state of this builder. */",
        "  public Person.Builder clear() {",
        "    Person_Builder _defaults = new Person.Builder();",
        "    name = _defaults.name;",
        "    age = _defaults.age;",
        "    return (Person.Builder) this;",
        "  }",
        "",
        "  /** Returns a newly-created {@link Person} based on the contents of the "
            + "{@code Builder}. */",
        "  public Person build() {",
        "    return new Person_Builder.Value(this);",
        "  }",
        "",
        "  /**",
        "   * Returns a newly-created partial {@link Person} for use in unit tests. "
            + "State checking will not",
        "   * be performed.",
        "   *",
        "   * <p>Partials should only ever be used in tests. "
            + "They permit writing robust test cases that won't",
        "   * fail if this type gains more application-level constraints "
            + "(e.g. new required fields) in",
        "   * future. If you require partially complete values in production code, "
            + "consider using a Builder.",
        "   */",
        "  public Person buildPartial() {",
        "    return new Person_Builder.Partial(this);",
        "  }",
        "",
        "  private static final class Value extends Person {",
        "    private final String name;",
        "    private final int age;",
        "",
        "    private Value(Person_Builder builder) {",
        "      this.name = builder.name;",
        "      this.age = builder.age;",
        "    }",
        "",
        "    @Override",
        "    public String getName() {",
        "      return name;",
        "    }",
        "",
        "    @Override",
        "    public int getAge() {",
        "      return age;",
        "    }",
        "",
        "    @Override",
        "    public boolean equals(Object obj) {",
        "      if (!(obj instanceof Person_Builder.Value)) {",
        "        return false;",
        "      }",
        "      Person_Builder.Value other = (Person_Builder.Value) obj;",
        "      return Objects.equals(name, other.name) && age == other.age;",
        "    }",
        "",
        "    @Override",
        "    public int hashCode() {",
        "      return Objects.hash(name, age);",
        "    }",
        "",
        "    @Override",
        "    public String toString() {",
        "      return \"Person{name=\" + name + \", age=\" + age + \"}\";",
        "    }",
        "  }",
        "",
        "  private static final class Partial extends Person {",
        "    private final String name;",
        "    private final int age;",
        "",
        "    Partial(Person_Builder builder) {",
        "      this.name = builder.name;",
        "      this.age = builder.age;",
        "    }",
        "",
        "    @Override",
        "    public String getName() {",
        "      return name;",
        "    }",
        "",
        "    @Override",
        "    public int getAge() {",
        "      return age;",
        "    }",
        "",
        "    @Override",
        "    public boolean equals(Object obj) {",
        "      if (!(obj instanceof Person_Builder.Partial)) {",
        "        return false;",
        "      }",
        "      Person_Builder.Partial other = (Person_Builder.Partial) obj;",
        "      return Objects.equals(name, other.name) && age == other.age;",
        "    }",
        "",
        "    @Override",
        "    public int hashCode() {",
        "      return Objects.hash(name, age);",
        "    }",
        "",
        "    @Override",
        "    public String toString() {",
        "      return \"partial Person{name=\" + name + \", age=\" + age + \"}\";",
        "    }",
        "  }",
        "}");
  }

  @Test
  public void test_guava() {
    assertThat(builder(BEAN)).given(GuavaLibrary.AVAILABLE).generates(
        "/** Auto-generated superclass of {@link Person.Builder}, "
            + "derived from the API of {@link Person}. */",
        "abstract class Person_Builder {",
        "",
        "  /** Creates a new builder using {@code value} as a template. */",
        "  public static Person.Builder from(Person value) {",
        "    return new Person.Builder().mergeFrom(value);",
        "  }",
        "",
        "  private String name;",
        "  private int age;",
        "",
        "  /**",
        "   * Sets the value to be returned by {@link Person#getName()}.",
        "   *",
        "   * @return this {@code Builder} object",
        "   * @throws NullPointerException if {@code name} is null",
        "   */",
        "  public Person.Builder setName(String name) {",
        "    this.name = Preconditions.checkNotNull(name);",
        "    return (Person.Builder) this;",
        "  }",
        "",
        "  /**",
        "   * Replaces the value to be returned by {@link Person#getName()} "
            + "by applying {@code mapper} to it",
        "   * and using the result.",
        "   *",
        "   * @return this {@code Builder} object",
        "   * @throws NullPointerException if {@code mapper} is null or returns null",
        "   */",
        "  public Person.Builder mapName(UnaryOperator<String> mapper) {",
        "    return setName(mapper.apply(getName()));",
        "  }",
        "",
        "  /** Returns the value that will be returned by {@link Person#getName()}. */",
        "  public String getName() {",
        "    return name;",
        "  }",
        "",
        "  /**",
        "   * Sets the value to be returned by {@link Person#getAge()}.",
        "   *",
        "   * @return this {@code Builder} object",
        "   */",
        "  public Person.Builder setAge(int age) {",
        "    this.age = age;",
        "    return (Person.Builder) this;",
        "  }",
        "",
        "  /**",
        "   * Replaces the value to be returned by {@link Person#getAge()} "
            + "by applying {@code mapper} to it",
        "   * and using the result.",
        "   *",
        "   * @return this {@code Builder} object",
        "   * @throws NullPointerException if {@code mapper} is null or returns null",
        "   */",
        "  public Person.Builder mapAge(UnaryOperator<Integer> mapper) {",
        "    return setAge(mapper.apply(getAge()));",
        "  }",
        "",
        "  /** Returns the value that will be returned by {@link Person#getAge()}. */",
        "  public int getAge() {",
        "    return age;",
        "  }",
        "",
        "  /** Sets all property values using the given {@code Person} as a template. */",
        "  public Person.Builder mergeFrom(Person value) {",
        "    Person_Builder _defaults = new Person.Builder();",
        "    if (!Objects.equals(value.getName(), _defaults.getName())) {",
        "      setName(value.getName());",
        "    }",
        "    if (value.getAge() != _defaults.getAge()) {",
        "      setAge(value.getAge());",
        "    }",
        "    return (Person.Builder) this;",
        "  }",
        "",
        "  /**",
        "   * Copies values from the given {@code Builder}. "
            + "Does not affect any properties not set on the",
        "   * input.",
        "   */",
        "  public Person.Builder mergeFrom(Person.Builder template) {",
        "    Person_Builder _defaults = new Person.Builder();",
        "    if (!Objects.equals(template.getName(), _defaults.getName())) {",
        "      setName(template.getName());",
        "    }",
        "    if (template.getAge() != _defaults.getAge()) {",
        "      setAge(template.getAge());",
        "    }",
        "    return (Person.Builder) this;",
        "  }",
        "",
        "  /** Resets the state of this builder. */",
        "  public Person.Builder clear() {",
        "    Person_Builder _defaults = new Person.Builder();",
        "    name = _defaults.name;",
        "    age = _defaults.age;",
        "    return (Person.Builder) this;",
        "  }",
        "",
        "  /** Returns a newly-created {@link Person} based on the contents of the "
            + "{@code Builder}. */",
        "  public Person build() {",
        "    return new Person_Builder.Value(this);",
        "  }",
        "",
        "  /**",
        "   * Returns a newly-created partial {@link Person} for use in unit tests. "
            + "State checking will not",
        "   * be performed.",
        "   *",
        "   * <p>Partials should only ever be used in tests. "
            + "They permit writing robust test cases that won't",
        "   * fail if this type gains more application-level constraints "
            + "(e.g. new required fields) in",
        "   * future. If you require partially complete values in production code, "
            + "consider using a Builder.",
        "   */",
        "  @VisibleForTesting()",
        "  public Person buildPartial() {",
        "    return new Person_Builder.Partial(this);",
        "  }",
        "",
        "  private static final class Value extends Person {",
        "    private final String name;",
        "    private final int age;",
        "",
        "    private Value(Person_Builder builder) {",
        "      this.name = builder.name;",
        "      this.age = builder.age;",
        "    }",
        "",
        "    @Override",
        "    public String getName() {",
        "      return name;",
        "    }",
        "",
        "    @Override",
        "    public int getAge() {",
        "      return age;",
        "    }",
        "",
        "    @Override",
        "    public boolean equals(Object obj) {",
        "      if (!(obj instanceof Person_Builder.Value)) {",
        "        return false;",
        "      }",
        "      Person_Builder.Value other = (Person_Builder.Value) obj;",
        "      return Objects.equals(name, other.name) && age == other.age;",
        "    }",
        "",
        "    @Override",
        "    public int hashCode() {",
        "      return Objects.hash(name, age);",
        "    }",
        "",
        "    @Override",
        "    public String toString() {",
        "      return \"Person{name=\" + name + \", age=\" + age + \"}\";",
        "    }",
        "  }",
        "",
        "  private static final class Partial extends Person {",
        "    private final String name;",
        "    private final int age;",
        "",
        "    Partial(Person_Builder builder) {",
        "      this.name = builder.name;",
        "      this.age = builder.age;",
        "    }",
        "",
        "    @Override",
        "    public String getName() {",
        "      return name;",
        "    }",
        "",
        "    @Override",
        "    public int getAge() {",
        "      return age;",
        "    }",
        "",
        "    @Override",
        "    public boolean equals(Object obj) {",
        "      if (!(obj instanceof Person_Builder.Partial)) {",
        "        return false;",
        "      }",
        "      Person_Builder.Partial other = (Person_Builder.Partial) obj;",
        "      return Objects.equals(name, other.name) && age == other.age;",
        "    }",
        "",
        "    @Override",
        "    public int hashCode() {",
        "      return Objects.hash(name, age);",
        "    }",
        "",
        "    @Override",
        "    public String toString() {",
        "      return \"partial Person{name=\" + name + \", age=\" + age + \"}\";",
        "    }",
        "  }",
        "}");
  }

  @Test
  public void test_guava_toBuilder() {
    GeneratedBuilder generatedType = builder(BEAN, Option.WITH_TO_BUILDER_METHOD);
    assertThat(generatedType).given(GuavaLibrary.AVAILABLE).generates(
        "/** Auto-generated superclass of {@link Person.Builder}, "
            + "derived from the API of {@link Person}. */",
        "abstract class Person_Builder {",
        "",
        "  /** Creates a new builder using {@code value} as a template. */",
        "  public static Person.Builder from(Person value) {",
        "    return new Person.Builder().mergeFrom(value);",
        "  }",
        "",
        "  private String name;",
        "  private int age;",
        "",
        "  /**",
        "   * Sets the value to be returned by {@link Person#getName()}.",
        "   *",
        "   * @return this {@code Builder} object",
        "   * @throws NullPointerException if {@code name} is null",
        "   */",
        "  public Person.Builder setName(String name) {",
        "    this.name = Preconditions.checkNotNull(name);",
        "    return (Person.Builder) this;",
        "  }",
        "",
        "  /**",
        "   * Replaces the value to be returned by {@link Person#getName()} "
            + "by applying {@code mapper} to it",
        "   * and using the result.",
        "   *",
        "   * @return this {@code Builder} object",
        "   * @throws NullPointerException if {@code mapper} is null or returns null",
        "   */",
        "  public Person.Builder mapName(UnaryOperator<String> mapper) {",
        "    return setName(mapper.apply(getName()));",
        "  }",
        "",
        "  /** Returns the value that will be returned by {@link Person#getName()}. */",
        "  public String getName() {",
        "    return name;",
        "  }",
        "",
        "  /**",
        "   * Sets the value to be returned by {@link Person#getAge()}.",
        "   *",
        "   * @return this {@code Builder} object",
        "   */",
        "  public Person.Builder setAge(int age) {",
        "    this.age = age;",
        "    return (Person.Builder) this;",
        "  }",
        "",
        "  /**",
        "   * Replaces the value to be returned by {@link Person#getAge()} "
            + "by applying {@code mapper} to it",
        "   * and using the result.",
        "   *",
        "   * @return this {@code Builder} object",
        "   * @throws NullPointerException if {@code mapper} is null or returns null",
        "   */",
        "  public Person.Builder mapAge(UnaryOperator<Integer> mapper) {",
        "    return setAge(mapper.apply(getAge()));",
        "  }",
        "",
        "  /** Returns the value that will be returned by {@link Person#getAge()}. */",
        "  public int getAge() {",
        "    return age;",
        "  }",
        "",
        "  /** Sets all property values using the given {@code Person} as a template. */",
        "  public Person.Builder mergeFrom(Person value) {",
        "    Person_Builder _defaults = new Person.Builder();",
        "    if (!Objects.equals(value.getName(), _defaults.getName())) {",
        "      setName(value.getName());",
        "    }",
        "    if (value.getAge() != _defaults.getAge()) {",
        "      setAge(value.getAge());",
        "    }",
        "    return (Person.Builder) this;",
        "  }",
        "",
        "  /**",
        "   * Copies values from the given {@code Builder}. "
            + "Does not affect any properties not set on the",
        "   * input.",
        "   */",
        "  public Person.Builder mergeFrom(Person.Builder template) {",
        "    Person_Builder _defaults = new Person.Builder();",
        "    if (!Objects.equals(template.getName(), _defaults.getName())) {",
        "      setName(template.getName());",
        "    }",
        "    if (template.getAge() != _defaults.getAge()) {",
        "      setAge(template.getAge());",
        "    }",
        "    return (Person.Builder) this;",
        "  }",
        "",
        "  /** Resets the state of this builder. */",
        "  public Person.Builder clear() {",
        "    Person_Builder _defaults = new Person.Builder();",
        "    name = _defaults.name;",
        "    age = _defaults.age;",
        "    return (Person.Builder) this;",
        "  }",
        "",
        "  /** Returns a newly-created {@link Person} based on the contents of the "
            + "{@code Builder}. */",
        "  public Person build() {",
        "    return new Person_Builder.Value(this);",
        "  }",
        "",
        "  /**",
        "   * Returns a newly-created partial {@link Person} for use in unit tests. "
            + "State checking will not",
        "   * be performed.",
        "   *",
        "   * <p>The builder returned by a partial's {@link Person#toBuilder() toBuilder} method "
            + "overrides",
        "   * {@link Person.Builder#build() build()} to return another partial. This allows for "
            + "robust tests",
        "   * of modify-rebuild code.",
        "   *",
        "   * <p>Partials should only ever be used in tests. "
            + "They permit writing robust test cases that won't",
        "   * fail if this type gains more application-level constraints "
            + "(e.g. new required fields) in",
        "   * future. If you require partially complete values in production code, "
            + "consider using a Builder.",
        "   */",
        "  @VisibleForTesting()",
        "  public Person buildPartial() {",
        "    return new Person_Builder.Partial(this);",
        "  }",
        "",
        "  private static final class Value extends Person {",
        "    private final String name;",
        "    private final int age;",
        "",
        "    private Value(Person_Builder builder) {",
        "      this.name = builder.name;",
        "      this.age = builder.age;",
        "    }",
        "",
        "    @Override",
        "    public String getName() {",
        "      return name;",
        "    }",
        "",
        "    @Override",
        "    public int getAge() {",
        "      return age;",
        "    }",
        "",
        "    @Override",
        "    public Person.Builder toBuilder() {",
        "      return new Person.Builder().mergeFrom(this);",
        "    }",
        "",
        "    @Override",
        "    public boolean equals(Object obj) {",
        "      if (!(obj instanceof Person_Builder.Value)) {",
        "        return false;",
        "      }",
        "      Person_Builder.Value other = (Person_Builder.Value) obj;",
        "      return Objects.equals(name, other.name) && age == other.age;",
        "    }",
        "",
        "    @Override",
        "    public int hashCode() {",
        "      return Objects.hash(name, age);",
        "    }",
        "",
        "    @Override",
        "    public String toString() {",
        "      return \"Person{name=\" + name + \", age=\" + age + \"}\";",
        "    }",
        "  }",
        "",
        "  private static final class Partial extends Person {",
        "    private final String name;",
        "    private final int age;",
        "",
        "    Partial(Person_Builder builder) {",
        "      this.name = builder.name;",
        "      this.age = builder.age;",
        "    }",
        "",
        "    @Override",
        "    public String getName() {",
        "      return name;",
        "    }",
        "",
        "    @Override",
        "    public int getAge() {",
        "      return age;",
        "    }",
        "",
        "    private static class PartialBuilder extends Person.Builder {",
        "      @Override",
        "      public Person build() {",
        "        return buildPartial();",
        "      }",
        "    }",
        "",
        "    @Override",
        "    public Person.Builder toBuilder() {",
        "      Person.Builder builder = new PartialBuilder();",
        "      builder.setName(name);",
        "      builder.setAge(age);",
        "      return builder;",
        "    }",
        "",
        "    @Override",
        "    public boolean equals(Object obj) {",
        "      if (!(obj instanceof Person_Builder.Partial)) {",
        "        return false;",
        "      }",
        "      Person_Builder.Partial other = (Person_Builder.Partial) obj;",
        "      return Objects.equals(name, other.name) && age == other.age;",
        "    }",
        "",
        "    @Override",
        "    public int hashCode() {",
        "      return Objects.hash(name, age);",
        "    }",
        "",
        "    @Override",
        "    public String toString() {",
        "      return \"partial Person{name=\" + name + \", age=\" + age + \"}\";",
        "    }",
        "  }",
        "}");
  }

  @Test
  public void test_prefixless() {
    assertThat(builder(PREFIXLESS)).given(GuavaLibrary.AVAILABLE).generates(
        "/** Auto-generated superclass of {@link Person.Builder}, "
            + "derived from the API of {@link Person}. */",
        "abstract class Person_Builder {",
        "",
        "  /** Creates a new builder using {@code value} as a template. */",
        "  public static Person.Builder from(Person value) {",
        "    return new Person.Builder().mergeFrom(value);",
        "  }",
        "",
        "  private String name;",
        "  private int age;",
        "",
        "  /**",
        "   * Sets the value to be returned by {@link Person#name()}.",
        "   *",
        "   * @return this {@code Builder} object",
        "   * @throws NullPointerException if {@code name} is null",
        "   */",
        "  public Person.Builder name(String name) {",
        "    this.name = Preconditions.checkNotNull(name);",
        "    return (Person.Builder) this;",
        "  }",
        "",
        "  /**",
        "   * Replaces the value to be returned by {@link Person#name()} by applying"
            + " {@code mapper} to it and",
        "   * using the result.",
        "   *",
        "   * @return this {@code Builder} object",
        "   * @throws NullPointerException if {@code mapper} is null or returns null",
        "   */",
        "  public Person.Builder mapName(UnaryOperator<String> mapper) {",
        "    return name(mapper.apply(name()));",
        "  }",
        "",
        "  /** Returns the value that will be returned by {@link Person#name()}. */",
        "  public String name() {",
        "    return name;",
        "  }",
        "",
        "  /**",
        "   * Sets the value to be returned by {@link Person#age()}.",
        "   *",
        "   * @return this {@code Builder} object",
        "   */",
        "  public Person.Builder age(int age) {",
        "    this.age = age;",
        "    return (Person.Builder) this;",
        "  }",
        "",
        "  /**",
        "   * Replaces the value to be returned by {@link Person#age()} by applying"
            + " {@code mapper} to it and",
        "   * using the result.",
        "   *",
        "   * @return this {@code Builder} object",
        "   * @throws NullPointerException if {@code mapper} is null or returns null",
        "   */",
        "  public Person.Builder mapAge(UnaryOperator<Integer> mapper) {",
        "    return age(mapper.apply(age()));",
        "  }",
        "",
        "  /** Returns the value that will be returned by {@link Person#age()}. */",
        "  public int age() {",
        "    return age;",
        "  }",
        "",
        "  /** Sets all property values using the given {@code Person} as a template. */",
        "  public Person.Builder mergeFrom(Person value) {",
        "    Person_Builder _defaults = new Person.Builder();",
        "    if (!Objects.equals(value.name(), _defaults.name())) {",
        "      name(value.name());",
        "    }",
        "    if (value.age() != _defaults.age()) {",
        "      age(value.age());",
        "    }",
        "    return (Person.Builder) this;",
        "  }",
        "",
        "  /**",
        "   * Copies values from the given {@code Builder}. "
            + "Does not affect any properties not set on the",
        "   * input.",
        "   */",
        "  public Person.Builder mergeFrom(Person.Builder template) {",
        "    Person_Builder _defaults = new Person.Builder();",
        "    if (!Objects.equals(template.name(), _defaults.name())) {",
        "      name(template.name());",
        "    }",
        "    if (template.age() != _defaults.age()) {",
        "      age(template.age());",
        "    }",
        "    return (Person.Builder) this;",
        "  }",
        "",
        "  /** Resets the state of this builder. */",
        "  public Person.Builder clear() {",
        "    Person_Builder _defaults = new Person.Builder();",
        "    name = _defaults.name;",
        "    age = _defaults.age;",
        "    return (Person.Builder) this;",
        "  }",
        "",
        "  /** Returns a newly-created {@link Person} based on the contents of the "
            + "{@code Builder}. */",
        "  public Person build() {",
        "    return new Person_Builder.Value(this);",
        "  }",
        "",
        "  /**",
        "   * Returns a newly-created partial {@link Person} for use in unit tests. "
            + "State checking will not",
        "   * be performed.",
        "   *",
        "   * <p>Partials should only ever be used in tests. "
            + "They permit writing robust test cases that won't",
        "   * fail if this type gains more application-level constraints "
            + "(e.g. new required fields) in",
        "   * future. If you require partially complete values in production code, "
            + "consider using a Builder.",
        "   */",
        "  @VisibleForTesting()",
        "  public Person buildPartial() {",
        "    return new Person_Builder.Partial(this);",
        "  }",
        "",
        "  private static final class Value extends Person {",
        "    private final String name;",
        "    private final int age;",
        "",
        "    private Value(Person_Builder builder) {",
        "      this.name = builder.name;",
        "      this.age = builder.age;",
        "    }",
        "",
        "    @Override",
        "    public String name() {",
        "      return name;",
        "    }",
        "",
        "    @Override",
        "    public int age() {",
        "      return age;",
        "    }",
        "",
        "    @Override",
        "    public boolean equals(Object obj) {",
        "      if (!(obj instanceof Person_Builder.Value)) {",
        "        return false;",
        "      }",
        "      Person_Builder.Value other = (Person_Builder.Value) obj;",
        "      return Objects.equals(name, other.name) && age == other.age;",
        "    }",
        "",
        "    @Override",
        "    public int hashCode() {",
        "      return Objects.hash(name, age);",
        "    }",
        "",
        "    @Override",
        "    public String toString() {",
        "      return \"Person{name=\" + name + \", age=\" + age + \"}\";",
        "    }",
        "  }",
        "",
        "  private static final class Partial extends Person {",
        "    private final String name;",
        "    private final int age;",
        "",
        "    Partial(Person_Builder builder) {",
        "      this.name = builder.name;",
        "      this.age = builder.age;",
        "    }",
        "",
        "    @Override",
        "    public String name() {",
        "      return name;",
        "    }",
        "",
        "    @Override",
        "    public int age() {",
        "      return age;",
        "    }",
        "",
        "    @Override",
        "    public boolean equals(Object obj) {",
        "      if (!(obj instanceof Person_Builder.Partial)) {",
        "        return false;",
        "      }",
        "      Person_Builder.Partial other = (Person_Builder.Partial) obj;",
        "      return Objects.equals(name, other.name) && age == other.age;",
        "    }",
        "",
        "    @Override",
        "    public int hashCode() {",
        "      return Objects.hash(name, age);",
        "    }",
        "",
        "    @Override",
        "    public String toString() {",
        "      return \"partial Person{name=\" + name + \", age=\" + age + \"}\";",
        "    }",
        "  }",
        "}");
  }

  private enum Option {
    WITH_TO_BUILDER_METHOD;
  }

  private static GeneratedBuilder builder(NamingConvention convention, Option... options) {
    Set<Option> optionSet = ImmutableSet.copyOf(options);
    QualifiedName person = QualifiedName.of("com.example", "Person");
    QualifiedName generatedBuilder = QualifiedName.of("com.example", "Person_Builder");

    Datatype datatype = new Datatype.Builder()
        .setBuilder(person.nestedType("Builder").withParameters())
        .setExtensible(true)
        .setBuilderFactory(BuilderFactory.NO_ARGS_CONSTRUCTOR)
        .setBuilderSerializable(false)
        .setGeneratedBuilder(generatedBuilder.withParameters())
        .setHasToBuilderMethod(optionSet.contains(Option.WITH_TO_BUILDER_METHOD))
        .setInterfaceType(false)
        .setPartialType(generatedBuilder.nestedType("Partial").withParameters())
        .setPropertyEnum(generatedBuilder.nestedType("Property").withParameters())
        .setType(person.withParameters())
        .setValueType(generatedBuilder.nestedType("Value").withParameters())
        .build();
    Property name = new Property.Builder()
        .setAllCapsName("NAME")
        .setBoxedType(STRING)
        .setCapitalizedName("Name")
        .setFullyCheckedCast(true)
        .setGetterName((convention == BEAN) ? "getName" : "name")
        .setName("name")
        .setType(STRING)
        .setUsingBeanConvention(convention == BEAN)
        .build();
    Property age = new Property.Builder()
        .setAllCapsName("AGE")
        .setBoxedType(INTEGER)
        .setCapitalizedName("Age")
        .setFullyCheckedCast(true)
        .setGetterName((convention == BEAN) ? "getAge" : "age")
        .setName("age")
        .setType(INT)
        .setUsingBeanConvention(convention == BEAN)
        .build();
    return new GeneratedBuilder(datatype, ImmutableMap.of(
        name, new DefaultProperty(datatype, name, true, unaryOperator(STRING)),
        age, new DefaultProperty(datatype, age, true, unaryOperator(INTEGER))));
  }
}
