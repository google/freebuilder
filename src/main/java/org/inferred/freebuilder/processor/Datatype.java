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
package org.inferred.freebuilder.processor;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import org.inferred.freebuilder.FreeBuilder;
import org.inferred.freebuilder.processor.source.Excerpt;
import org.inferred.freebuilder.processor.source.QualifiedName;
import org.inferred.freebuilder.processor.source.SourceBuilder;
import org.inferred.freebuilder.processor.source.Type;
import org.inferred.freebuilder.processor.source.TypeClass;

/** Metadata about a user's datatype. */
@FreeBuilder
public abstract class Datatype {

  /** Standard Java methods that may be underridden. */
  public enum StandardMethod {
    TO_STRING,
    HASH_CODE,
    EQUALS
  }

  /** How compulsory the underride is. */
  public enum UnderrideLevel {
    /** There is no underride. */
    ABSENT,
    /** The underride can be overridden (viz. to respect Partials). */
    OVERRIDEABLE,
    /** The underride is declared final. */
    FINAL;
  }

  public enum Visibility implements Excerpt {
    PUBLIC(0, "PUBLIC", "public "),
    PROTECTED(1, "PROTECTED", "protected "),
    PACKAGE(2, "PACKAGE", ""),
    PRIVATE(3, "PRIVATE", "private ");

    private final int order;
    private final String name;
    private final String excerpt;

    Visibility(int order, String name, String excerpt) {
      this.order = order;
      this.name = name;
      this.excerpt = excerpt;
    }

    public static Visibility mostVisible(Visibility a, Visibility b) {
      return (a.order < b.order) ? a : b;
    }

    @Override
    public void addTo(SourceBuilder code) {
      code.add(excerpt);
    }

    @Override
    public String toString() {
      return name;
    }
  }

  /** Returns the type itself. */
  public abstract TypeClass getType();

  /** Returns true if the type is an interface. */
  public abstract boolean isInterfaceType();

  /** Returns the builder type that users will see. */
  public abstract Type getBuilder();

  /** Whether there is a package-visible, no-args constructor so we can subclass the Builder. */
  public abstract boolean isExtensible();

  /** Returns the builder factory mechanism the user has exposed, if any. */
  public abstract Optional<BuilderFactory> getBuilderFactory();

  /** Returns the builder class that should be generated. */
  public abstract TypeClass getGeneratedBuilder();

  /** Returns the value class that should be generated. */
  public abstract TypeClass getValueType();

  /** Returns the partial value class that should be generated. */
  public abstract TypeClass getPartialType();

  /** Returns the Rebuildable interface that should be generated, if any. */
  public abstract Optional<TypeClass> getRebuildableType();

  /** Returns the Property enum that may be generated. */
  public abstract TypeClass getPropertyEnum();

  public UnderrideLevel standardMethodUnderride(StandardMethod standardMethod) {
    UnderrideLevel underrideLevel = getStandardMethodUnderrides().get(standardMethod);
    return (underrideLevel == null) ? UnderrideLevel.ABSENT : underrideLevel;
  }

  public abstract ImmutableMap<StandardMethod, UnderrideLevel> getStandardMethodUnderrides();

  /** Returns whether the builder type should be serializable. */
  public abstract boolean isBuilderSerializable();

  /** Returns whether the value type has a toBuilder method that needs to be generated. */
  public abstract boolean getHasToBuilderMethod();

  /** Returns the build method to be generated. */
  public abstract NameAndVisibility getBuildMethod();

  /** Returns the partial build method to be generated. */
  public abstract NameAndVisibility getBuildPartialMethod();

  /** Returns the clear method to be generated. */
  public abstract NameAndVisibility getClearMethod();

  /** Returns the mergeFrom(Builder) method to be generated. */
  public abstract NameAndVisibility getMergeFromBuilderMethod();

  /** Returns the mergeFrom(Value) method to be generated. */
  public abstract NameAndVisibility getMergeFromValueMethod();

  /** Returns a list of annotations that should be applied to the generated builder class. */
  public abstract ImmutableList<Excerpt> getGeneratedBuilderAnnotations();

  /** Returns a list of annotations that should be applied to the generated value class. */
  public abstract ImmutableList<Excerpt> getValueTypeAnnotations();

  /** Returns the visibility of the generated value class. */
  public abstract Visibility getValueTypeVisibility();

  /** Returns a list of nested classes that should be added to the generated builder class. */
  public abstract ImmutableList<Excerpt> getNestedClasses();

  public Builder toBuilder() {
    return new Builder().mergeFrom(this);
  }

  /** Builder for {@link Datatype}. */
  public static class Builder extends Datatype_Builder {

    public Builder() {
      super.setBuildMethod(NameAndVisibility.of("build", Visibility.PUBLIC));
      super.setBuildPartialMethod(NameAndVisibility.of("buildPartial", Visibility.PUBLIC));
      super.setClearMethod(NameAndVisibility.of("clear", Visibility.PUBLIC));
      super.setMergeFromBuilderMethod(NameAndVisibility.of("mergeFrom", Visibility.PUBLIC));
      super.setMergeFromValueMethod(NameAndVisibility.of("mergeFrom", Visibility.PUBLIC));
      super.setValueTypeVisibility(Visibility.PRIVATE);
      super.setHasToBuilderMethod(false);
    }

    /**
     * Sets the value to be returned by {@link Datatype#getValueTypeVisibility()} to the most
     * visible of the current value and {@code visibility}. Will not decrease visibility.
     *
     * @return this {@code Builder} object
     * @throws NullPointerException if {@code visibility} is null
     */
    @Override
    public Builder setValueTypeVisibility(Visibility visibility) {
      return super.setValueTypeVisibility(
          Visibility.mostVisible(getValueTypeVisibility(), visibility));
    }

    /** Returns a newly-built {@link Datatype} based on the content of the {@code Builder}. */
    @Override
    public Datatype build() {
      Datatype datatype = super.build();
      QualifiedName generatedBuilder = datatype.getGeneratedBuilder().getQualifiedName();
      checkState(
          datatype.getValueType().getQualifiedName().enclosingType().equals(generatedBuilder),
          "%s not a nested class of %s",
          datatype.getValueType(),
          generatedBuilder);
      checkState(
          datatype.getPartialType().getQualifiedName().enclosingType().equals(generatedBuilder),
          "%s not a nested class of %s",
          datatype.getPartialType(),
          generatedBuilder);
      checkState(
          datatype.getPropertyEnum().getQualifiedName().enclosingType().equals(generatedBuilder),
          "%s not a nested class of %s",
          datatype.getPropertyEnum(),
          generatedBuilder);
      return datatype;
    }
  }
}
