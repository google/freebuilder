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
package org.inferred.freebuilder.processor.property;

import static org.inferred.freebuilder.processor.BuilderMethods.addAllMethod;
import static org.inferred.freebuilder.processor.BuilderMethods.addMethod;
import static org.inferred.freebuilder.processor.BuilderMethods.clearMethod;
import static org.inferred.freebuilder.processor.BuilderMethods.getter;
import static org.inferred.freebuilder.processor.BuilderMethods.mutator;
import static org.inferred.freebuilder.processor.BuilderMethods.removeMethod;
import static org.inferred.freebuilder.processor.BuilderMethods.setComparatorMethod;
import static org.inferred.freebuilder.processor.model.ModelUtils.erasesToAnyOf;
import static org.inferred.freebuilder.processor.model.ModelUtils.maybeDeclared;
import static org.inferred.freebuilder.processor.model.ModelUtils.maybeUnbox;
import static org.inferred.freebuilder.processor.model.ModelUtils.needsSafeVarargs;
import static org.inferred.freebuilder.processor.model.ModelUtils.overrides;
import static org.inferred.freebuilder.processor.model.ModelUtils.upperBound;
import static org.inferred.freebuilder.processor.property.MergeAction.appendingToCollections;
import static org.inferred.freebuilder.processor.source.FunctionalType.consumer;
import static org.inferred.freebuilder.processor.source.FunctionalType.functionalTypeAcceptedByMethod;
import static org.inferred.freebuilder.processor.source.feature.GuavaLibrary.GUAVA;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.TreeSet;
import java.util.stream.BaseStream;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.inferred.freebuilder.processor.Datatype;
import org.inferred.freebuilder.processor.Declarations;
import org.inferred.freebuilder.processor.excerpt.CheckedNavigableSet;
import org.inferred.freebuilder.processor.source.Excerpt;
import org.inferred.freebuilder.processor.source.FunctionalType;
import org.inferred.freebuilder.processor.source.PreconditionExcerpts;
import org.inferred.freebuilder.processor.source.SourceBuilder;
import org.inferred.freebuilder.processor.source.Type;
import org.inferred.freebuilder.processor.source.Variable;

/** {@link PropertyCodeGenerator} providing fluent methods for {@link SortedSet} properties. */
class SortedSetProperty extends PropertyCodeGenerator {

  static class Factory implements PropertyCodeGenerator.Factory {

    @Override
    public Optional<SortedSetProperty> create(Config config) {
      DeclaredType type = maybeDeclared(config.getProperty().getType()).orElse(null);
      if (!erasesToAnyOf(type, SortedSet.class, ImmutableSortedSet.class)) {
        return Optional.empty();
      }

      TypeMirror elementType = upperBound(config.getElements(), type.getTypeArguments().get(0));
      Optional<TypeMirror> unboxedType = maybeUnbox(elementType, config.getTypes());
      boolean needsSafeVarargs = needsSafeVarargs(unboxedType.orElse(elementType));
      boolean overridesAddMethod = hasAddMethodOverride(config, unboxedType.orElse(elementType));
      boolean overridesVarargsAddMethod =
          hasVarargsAddMethodOverride(config, unboxedType.orElse(elementType));

      FunctionalType mutatorType =
          functionalTypeAcceptedByMethod(
              config.getBuilder(),
              mutator(config.getProperty()),
              consumer(
                  wildcardSuperSortedSet(elementType, config.getElements(), config.getTypes())),
              config.getElements(),
              config.getTypes());

      return Optional.of(
          new SortedSetProperty(
              config.getDatatype(),
              config.getProperty(),
              elementType,
              unboxedType,
              mutatorType,
              needsSafeVarargs,
              overridesAddMethod,
              overridesVarargsAddMethod));
    }

    private static boolean hasAddMethodOverride(Config config, TypeMirror elementType) {
      return overrides(
          config.getBuilder(), config.getTypes(), addMethod(config.getProperty()), elementType);
    }

    private static boolean hasVarargsAddMethodOverride(Config config, TypeMirror elementType) {
      return overrides(
          config.getBuilder(),
          config.getTypes(),
          addMethod(config.getProperty()),
          config.getTypes().getArrayType(elementType));
    }

    private static TypeMirror wildcardSuperSortedSet(
        TypeMirror elementType, Elements elements, Types types) {
      TypeElement setType = elements.getTypeElement(SortedSet.class.getName());
      return types.getWildcardType(null, types.getDeclaredType(setType, elementType));
    }
  }

  private final TypeMirror elementType;
  private final Optional<TypeMirror> unboxedType;
  private final FunctionalType mutatorType;
  private final boolean needsSafeVarargs;
  private final boolean overridesAddMethod;
  private final boolean overridesVarargsAddMethod;

  SortedSetProperty(
      Datatype datatype,
      Property property,
      TypeMirror elementType,
      Optional<TypeMirror> unboxedType,
      FunctionalType mutatorType,
      boolean needsSafeVarargs,
      boolean overridesAddMethod,
      boolean overridesVarargsAddMethod) {
    super(datatype, property);
    this.elementType = elementType;
    this.unboxedType = unboxedType;
    this.mutatorType = mutatorType;
    this.needsSafeVarargs = needsSafeVarargs;
    this.overridesAddMethod = overridesAddMethod;
    this.overridesVarargsAddMethod = overridesVarargsAddMethod;
  }

  @Override
  public void addValueFieldDeclaration(SourceBuilder code) {
    code.addLine(
        "private final %s<%s> %s;",
        code.feature(GUAVA).isAvailable() ? ImmutableSortedSet.class : SortedSet.class,
        elementType,
        property.getField());
  }

  @Override
  public void addBuilderFieldDeclaration(SourceBuilder code) {
    code.addLine("private %s<%s> %s = null;", NavigableSet.class, elementType, property.getField());
  }

  @Override
  public void addBuilderFieldAccessors(SourceBuilder code) {
    addSetComparator(code);
    addAdd(code);
    addVarargsAdd(code);
    addSpliteratorAddAll(code);
    addStreamAddAll(code);
    addIterableAddAll(code);
    addRemove(code);
    addMutator(code);
    addClear(code);
    addGetter(code);
  }

  private void addSetComparator(SourceBuilder code) {
    code.addLine("")
        .addLine("/**")
        .addLine(
            " * Sets the comparator of the set to be returned from %s.",
            datatype.getType().javadocNoArgMethodLink(property.getGetterName()))
        .addLine(" *")
        .addLine(
            " * Pass in {@code null} to use the {@linkplain %s natural ordering}", Comparable.class)
        .addLine(" * of the elements.")
        .addLine(" *")
        .addLine(" * <p>If the set is accessed without calling this method first, the comparator")
        .addLine(" * will default to {@code null}, and cannot subsequently be changed.")
        .addLine(" * (Note that this immutability is an implementation detail that may change in")
        .addLine(" * future; it should not be relied on for correctness.)")
        .addLine(" *")
        .addLine(" * @return this {@code %s} object", datatype.getBuilder().getSimpleName())
        .addLine(" * @throws IllegalStateException if the set has been accessed at all,")
        .addLine(" *     whether by adding an element, setting the comparator, or calling")
        .addLine(" *     {@link #%s()}.", getter(property));
    code.addLine(" */")
        .addLine(
            "protected %s %s(%s<? super %s> comparator) {",
            datatype.getBuilder(), setComparatorMethod(property), Comparator.class, elementType)
        .add(
            PreconditionExcerpts.checkState(
                "%1$s == null", "Comparator already set for %1$s", property.getField()));
    if (code.feature(GUAVA).isAvailable()) {
      code.addLine("  if (comparator == null) {")
          .addLine("    %s = %s.of();", property.getField(), ImmutableSortedSet.class)
          .addLine("  } else {")
          .addLine(
              "    %s = new %s<%s>(comparator).build();",
              property.getField(), ImmutableSortedSet.Builder.class, elementType)
          .addLine("  }");
    } else {
      code.addLine("  %s = new %s<>(comparator);", property.getField(), TreeSet.class);
    }
    code.addLine("  return (%s) this;", datatype.getBuilder()).addLine("}");
  }

  private void addAdd(SourceBuilder code) {
    code.addLine("")
        .addLine("/**")
        .addLine(
            " * Adds {@code element} to the set to be returned from %s.",
            datatype.getType().javadocNoArgMethodLink(property.getGetterName()))
        .addLine(
            " * If the set already contains {@code element}, then {@code %s}", addMethod(property))
        .addLine(" * has no effect (only the previously added element is retained).")
        .addLine(" *")
        .addLine(" * @return this {@code %s} object", datatype.getBuilder().getSimpleName());
    if (!unboxedType.isPresent()) {
      code.addLine(" * @throws NullPointerException if {@code element} is null");
    }
    code.addLine(" */")
        .addLine(
            "public %s %s(%s element) {",
            datatype.getBuilder(), addMethod(property), unboxedType.orElse(elementType));
    addConvertToTreeSet(code);
    if (unboxedType.isPresent()) {
      code.addLine("  %s.add(element);", property.getField());
    } else {
      code.addLine("  %s.add(%s.requireNonNull(element));", property.getField(), Objects.class);
    }
    code.addLine("  return (%s) this;", datatype.getBuilder()).addLine("}");
  }

  private void addConvertToTreeSet(SourceBuilder code) {
    code.addLine("  if (%s == null) {", property.getField())
        .addLine("    // Use default comparator")
        .addLine("    %s = new %s<>();", property.getField(), TreeSet.class);
    if (code.feature(GUAVA).isAvailable()) {
      code.addLine(
              "  } else if (%s instanceof %s) {", property.getField(), ImmutableSortedSet.class)
          .addLine("    %1$s = new %2$s<>(%1$s);", property.getField(), TreeSet.class);
    }
    code.addLine("  }");
  }

  private void addVarargsAdd(SourceBuilder code) {
    code.addLine("")
        .addLine("/**")
        .addLine(" * Adds each element of {@code elements} to the set to be returned from")
        .addLine(
            " * %s, ignoring duplicate elements",
            datatype.getType().javadocNoArgMethodLink(property.getGetterName()))
        .addLine(" * (only the first duplicate element is added).")
        .addLine(" *")
        .addLine(" * @return this {@code %s} object", datatype.getBuilder().getSimpleName());
    if (!unboxedType.isPresent()) {
      code.addLine(" * @throws NullPointerException if {@code elements} is null or contains a")
          .addLine(" *     null element");
    }
    code.addLine(" */");
    if (needsSafeVarargs) {
      if (!overridesVarargsAddMethod) {
        code.addLine("@%s", SafeVarargs.class)
            .addLine("@%s({\"varargs\"})", SuppressWarnings.class);
      } else {
        code.addLine("@%s({\"unchecked\", \"varargs\"})", SuppressWarnings.class);
      }
    }
    code.add("public ");
    if (needsSafeVarargs && !overridesVarargsAddMethod) {
      code.add("final ");
    }
    code.add(
        "%s %s(%s... elements) {\n",
        datatype.getBuilder(), addMethod(property), unboxedType.orElse(elementType));
    Optional<Class<?>> arrayUtils = code.feature(GUAVA).arrayUtils(unboxedType.orElse(elementType));
    if (arrayUtils.isPresent()) {
      code.addLine("  return %s(%s.asList(elements));", addAllMethod(property), arrayUtils.get());
    } else {
      // Primitive type, Guava not available
      code.addLine("  for (%s element : elements) {", elementType)
          .addLine("    %s(element);", addMethod(property))
          .addLine("  }")
          .addLine("  return (%s) this;", datatype.getBuilder());
    }
    code.addLine("}");
  }

  private void addSpliteratorAddAll(SourceBuilder code) {
    addJavadocForAddAll(code);
    code.addLine(
            "public %s %s(%s<? extends %s> elements) {",
            datatype.getBuilder(), addAllMethod(property), Spliterator.class, elementType)
        .addLine("  elements.forEachRemaining(this::%s);", addMethod(property))
        .addLine("  return (%s) this;", datatype.getBuilder())
        .addLine("}");
  }

  private void addStreamAddAll(SourceBuilder code) {
    addJavadocForAddAll(code);
    code.addLine(
            "public %s %s(%s<? extends %s, ?> elements) {",
            datatype.getBuilder(), addAllMethod(property), BaseStream.class, elementType)
        .addLine("  return %s(elements.spliterator());", addAllMethod(property))
        .addLine("}");
  }

  private void addIterableAddAll(SourceBuilder code) {
    addJavadocForAddAll(code);
    addAccessorAnnotations(code);
    code.addLine(
            "public %s %s(%s<? extends %s> elements) {",
            datatype.getBuilder(), addAllMethod(property), Iterable.class, elementType)
        .addLine("  elements.forEach(this::%s);", addMethod(property))
        .addLine("  return (%s) this;", datatype.getBuilder())
        .addLine("}");
  }

  private void addJavadocForAddAll(SourceBuilder code) {
    code.addLine("")
        .addLine("/**")
        .addLine(" * Adds each element of {@code elements} to the set to be returned from")
        .addLine(
            " * %s, ignoring duplicate elements",
            datatype.getType().javadocNoArgMethodLink(property.getGetterName()))
        .addLine(" * (only the first duplicate element is added).")
        .addLine(" *")
        .addLine(" * @return this {@code %s} object", datatype.getBuilder().getSimpleName())
        .addLine(" * @throws NullPointerException if {@code elements} is null or contains a")
        .addLine(" *     null element")
        .addLine(" */");
  }

  private void addRemove(SourceBuilder code) {
    code.addLine("")
        .addLine("/**")
        .addLine(
            " * Removes {@code element} from the set to be returned from %s.",
            datatype.getType().javadocNoArgMethodLink(property.getGetterName()))
        .addLine(" * Does nothing if {@code element} is not a member of the set.")
        .addLine(" *")
        .addLine(" * @return this {@code %s} object", datatype.getBuilder().getSimpleName());
    if (!unboxedType.isPresent()) {
      code.addLine(" * @throws NullPointerException if {@code element} is null");
    }
    code.addLine(" */")
        .addLine(
            "public %s %s(%s element) {",
            datatype.getBuilder(), removeMethod(property), unboxedType.orElse(elementType));
    addConvertToTreeSet(code);
    if (unboxedType.isPresent()) {
      code.addLine("  %s.remove(element);", property.getField());
    } else {
      code.addLine("  %s.remove(%s.requireNonNull(element));", property.getField(), Objects.class);
    }
    code.addLine("  return (%s) this;", datatype.getBuilder()).addLine("}");
  }

  private void addMutator(SourceBuilder code) {
    code.addLine("")
        .addLine("/**")
        .addLine(
            " * Applies {@code mutator} to the set to be returned from %s.",
            datatype.getType().javadocNoArgMethodLink(property.getGetterName()))
        .addLine(" *")
        .addLine(" * <p>This method mutates the set in-place. {@code mutator} is a void")
        .addLine(" * consumer, so any value returned from a lambda will be ignored. Take care")
        .addLine(
            " * not to call pure functions, like %s.",
            Type.from(Collection.class).javadocNoArgMethodLink("stream"))
        .addLine(" *")
        .addLine(" * @return this {@code Builder} object")
        .addLine(" * @throws NullPointerException if {@code mutator} is null")
        .addLine(" */")
        .addLine(
            "public %s %s(%s mutator) {",
            datatype.getBuilder(), mutator(property), mutatorType.getFunctionalInterface());
    addConvertToTreeSet(code);
    if (overridesAddMethod) {
      code.addLine(
          "  mutator.%s(new %s<%s>(%s, this::%s));",
          mutatorType.getMethodName(),
          CheckedNavigableSet.TYPE,
          elementType,
          property.getField(),
          addMethod(property));
    } else {
      code.addLine(
              "  // If %s is overridden, this method will be updated to delegate to it",
              addMethod(property))
          .addLine("  mutator.%s(%s);", mutatorType.getMethodName(), property.getField());
    }
    code.addLine("  return (%s) this;", datatype.getBuilder()).addLine("}");
  }

  private void addClear(SourceBuilder code) {
    code.addLine("")
        .addLine("/**")
        .addLine(
            " * Clears the set to be returned from %s.",
            datatype.getType().javadocNoArgMethodLink(property.getGetterName()))
        .addLine(" *")
        .addLine(" * @return this {@code %s} object", datatype.getBuilder().getSimpleName())
        .addLine(" */")
        .addLine("public %s %s() {", datatype.getBuilder(), clearMethod(property));
    if (code.feature(GUAVA).isAvailable()) {
      code.addLine("  if (%s instanceof %s) {", property.getField(), ImmutableSortedSet.class)
          .addLine("    if (%s.isEmpty()) {", property.getField())
          .addLine("       // Do nothing")
          .addLine("    } else if (%s.comparator() != null) {", property.getField())
          .addLine(
              "      %1$s = new %2$s<%3$s>(%1$s.comparator()).build();",
              property.getField(), ImmutableSortedSet.Builder.class, elementType)
          .addLine("    } else {")
          .addLine("      %s = %s.of();", property.getField(), ImmutableSortedSet.class)
          .addLine("    }")
          .add("  } else ");
    }
    code.addLine("  if (%s != null) {", property.getField())
        .addLine("    %s.clear();", property.getField())
        .addLine("  }")
        .addLine("  return (%s) this;", datatype.getBuilder())
        .addLine("}");
  }

  private void addGetter(SourceBuilder code) {
    code.addLine("")
        .addLine("/**")
        .addLine(" * Returns an unmodifiable view of the set that will be returned by")
        .addLine(" * %s.", datatype.getType().javadocNoArgMethodLink(property.getGetterName()))
        .addLine(" * Changes to this builder will be reflected in the view.")
        .addLine(" */")
        .addLine("public %s<%s> %s() {", SortedSet.class, elementType, getter(property));
    addConvertToTreeSet(code);
    code.addLine("  return %s.unmodifiableSortedSet(%s);", Collections.class, property.getField())
        .addLine("}");
  }

  @Override
  public void addFinalFieldAssignment(SourceBuilder code, Excerpt finalField, String builder) {
    code.addLine("if (%s == null) {", property.getField().on(builder));
    if (code.feature(GUAVA).isAvailable()) {
      code.addLine("  %s = %s.of();", finalField, ImmutableSortedSet.class)
          .addLine(
              "} else if (%s instanceof %s) {",
              property.getField().on(builder), ImmutableSortedSet.class)
          .addLine(
              "  %s = (%s<%s>) %s;",
              finalField, ImmutableSortedSet.class, elementType, property.getField().on(builder))
          .addLine("} else {")
          .addLine(
              "  %s = %s.copyOfSorted(%s);",
              finalField, ImmutableSortedSet.class, property.getField().on(builder));
    } else {
      code.addLine(
              "  %s = %s.unmodifiableSortedSet(new %s<>());",
              finalField, Collections.class, TreeSet.class)
          .addLine("} else {")
          .addLine(
              "  %s = %s.unmodifiableSortedSet(new %s<>(%s));",
              finalField, Collections.class, TreeSet.class, property.getField().on(builder));
    }
    code.addLine("}");
  }

  @Override
  public void addAssignToBuilder(SourceBuilder code, Variable builder) {
    if (code.feature(GUAVA).isAvailable()) {
      code.addLine("%s = %s;", property.getField().on(builder), property.getField());
    } else {
      code.addLine(
          "%s = new %s<>(%s);",
          property.getField().on(builder), TreeSet.class, property.getField());
    }
  }

  @Override
  public void addMergeFromValue(SourceBuilder code, String value) {
    if (code.feature(GUAVA).isAvailable()) {
      code.addLine("if (%s instanceof %s", value, datatype.getValueType().getQualifiedName())
          .addLine("      && (%s == null", property.getField())
          .addLine("          || (%s instanceof %s ", property.getField(), ImmutableSortedSet.class)
          .addLine("              && %s.isEmpty()", property.getField())
          .addLine(
              "              && %s.equals(%s.comparator(), %s.%s().comparator())))) {",
              Objects.class, property.getField(), value, property.getGetterName())
          .addLine("  @%s(\"unchecked\")", SuppressWarnings.class)
          .addLine(
              "  %1$s<%2$s> _temporary = (%1$s<%2$s>) (%1$s<?>) %3$s.%4$s();",
              ImmutableSortedSet.class, elementType, value, property.getGetterName())
          .addLine("  %s = _temporary;", property.getField())
          .addLine("} else {");
    }
    code.addLine("%s(%s.%s());", addAllMethod(property), value, property.getGetterName());
    if (code.feature(GUAVA).isAvailable()) {
      code.addLine("}");
    }
  }

  @Override
  public void addMergeFromBuilder(SourceBuilder code, String builder) {
    Excerpt base = Declarations.upcastToGeneratedBuilder(code, datatype, builder);
    code.addLine("if (%s != null) {", property.getField().on(base))
        .addLine("  %s(%s);", addAllMethod(property), property.getField().on(base))
        .addLine("}");
  }

  @Override
  public Set<MergeAction> getMergeActions() {
    return ImmutableSet.of(appendingToCollections());
  }

  @Override
  public void addSetFromResult(SourceBuilder code, Excerpt builder, Excerpt variable) {
    code.addLine("%s.%s(%s);", builder, addAllMethod(property), variable);
  }

  @Override
  public void addClearField(SourceBuilder code) {
    code.addLine("%s();", clearMethod(property));
  }
}
