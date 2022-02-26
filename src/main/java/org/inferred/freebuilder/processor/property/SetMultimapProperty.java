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

import static org.inferred.freebuilder.processor.BuilderMethods.clearMethod;
import static org.inferred.freebuilder.processor.BuilderMethods.getter;
import static org.inferred.freebuilder.processor.BuilderMethods.mutator;
import static org.inferred.freebuilder.processor.BuilderMethods.putAllMethod;
import static org.inferred.freebuilder.processor.BuilderMethods.putMethod;
import static org.inferred.freebuilder.processor.BuilderMethods.removeAllMethod;
import static org.inferred.freebuilder.processor.BuilderMethods.removeMethod;
import static org.inferred.freebuilder.processor.model.ModelUtils.erasesToAnyOf;
import static org.inferred.freebuilder.processor.model.ModelUtils.maybeDeclared;
import static org.inferred.freebuilder.processor.model.ModelUtils.maybeUnbox;
import static org.inferred.freebuilder.processor.model.ModelUtils.overrides;
import static org.inferred.freebuilder.processor.model.ModelUtils.upperBound;
import static org.inferred.freebuilder.processor.property.MergeAction.appendingToCollections;
import static org.inferred.freebuilder.processor.source.FunctionalType.consumer;
import static org.inferred.freebuilder.processor.source.FunctionalType.functionalTypeAcceptedByMethod;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.inferred.freebuilder.processor.Datatype;
import org.inferred.freebuilder.processor.Declarations;
import org.inferred.freebuilder.processor.excerpt.CheckedSetMultimap;
import org.inferred.freebuilder.processor.source.Excerpt;
import org.inferred.freebuilder.processor.source.FunctionalType;
import org.inferred.freebuilder.processor.source.SourceBuilder;
import org.inferred.freebuilder.processor.source.Variable;

/** {@link PropertyCodeGenerator} providing fluent methods for {@link SetMultimap} properties. */
class SetMultimapProperty extends PropertyCodeGenerator {

  static class Factory implements PropertyCodeGenerator.Factory {

    @Override
    public Optional<SetMultimapProperty> create(Config config) {
      Property property = config.getProperty();
      DeclaredType type = maybeDeclared(property.getType()).orElse(null);
      if (!erasesToAnyOf(type, SetMultimap.class, ImmutableSetMultimap.class)) {
        return Optional.empty();
      }

      TypeMirror keyType = upperBound(config.getElements(), type.getTypeArguments().get(0));
      TypeMirror valueType = upperBound(config.getElements(), type.getTypeArguments().get(1));
      Optional<TypeMirror> unboxedKeyType = maybeUnbox(keyType, config.getTypes());
      Optional<TypeMirror> unboxedValueType = maybeUnbox(valueType, config.getTypes());
      boolean overridesPutMethod =
          hasPutMethodOverride(
              config, unboxedKeyType.orElse(keyType), unboxedValueType.orElse(valueType));

      FunctionalType mutatorType =
          functionalTypeAcceptedByMethod(
              config.getBuilder(),
              mutator(property),
              consumer(setMultimap(keyType, valueType, config.getElements(), config.getTypes())),
              config.getElements(),
              config.getTypes());

      return Optional.of(
          new SetMultimapProperty(
              config.getDatatype(),
              property,
              overridesPutMethod,
              keyType,
              unboxedKeyType,
              valueType,
              unboxedValueType,
              mutatorType));
    }

    private static boolean hasPutMethodOverride(
        Config config, TypeMirror keyType, TypeMirror valueType) {
      return overrides(
          config.getBuilder(),
          config.getTypes(),
          putMethod(config.getProperty()),
          keyType,
          valueType);
    }

    private static TypeMirror setMultimap(
        TypeMirror keyType, TypeMirror valueType, Elements elements, Types types) {
      TypeElement setMultimapType = elements.getTypeElement(SetMultimap.class.getName());
      return types.getDeclaredType(setMultimapType, keyType, valueType);
    }
  }

  private final boolean overridesPutMethod;
  private final TypeMirror keyType;
  private final Optional<TypeMirror> unboxedKeyType;
  private final TypeMirror valueType;
  private final Optional<TypeMirror> unboxedValueType;
  private final FunctionalType mutatorType;

  SetMultimapProperty(
      Datatype datatype,
      Property property,
      boolean overridesPutMethod,
      TypeMirror keyType,
      Optional<TypeMirror> unboxedKeyType,
      TypeMirror valueType,
      Optional<TypeMirror> unboxedValueType,
      FunctionalType mutatorType) {
    super(datatype, property);
    this.overridesPutMethod = overridesPutMethod;
    this.keyType = keyType;
    this.unboxedKeyType = unboxedKeyType;
    this.valueType = valueType;
    this.unboxedValueType = unboxedValueType;
    this.mutatorType = mutatorType;
  }

  @Override
  public void addValueFieldDeclaration(SourceBuilder code) {
    code.addLine(
        "private final %s<%s, %s> %s;",
        ImmutableSetMultimap.class, keyType, valueType, property.getField());
  }

  @Override
  public void addBuilderFieldDeclaration(SourceBuilder code) {
    code.addLine(
        "private final %1$s<%2$s, %3$s> %4$s = %1$s.create();",
        LinkedHashMultimap.class, keyType, valueType, property.getField());
  }

  @Override
  public void addBuilderFieldAccessors(SourceBuilder code) {
    addPut(code);
    addSingleKeyPutAll(code);
    addMultimapPutAll(code);
    addRemove(code);
    addRemoveAll(code);
    addMutate(code);
    addClear(code);
    addGetter(code);
  }

  private void addPut(SourceBuilder code) {
    code.addLine("")
        .addLine("/**")
        .addLine(" * Adds a {@code key}-{@code value} mapping to the multimap to be returned")
        .addLine(" * from %s.", datatype.getType().javadocNoArgMethodLink(property.getGetterName()))
        .addLine(
            " * If the multimap already contains this mapping, then {@code %s}",
            putMethod(property))
        .addLine(" * has no effect (only the previously added mapping is retained).")
        .addLine(" *")
        .addLine(" * @return this {@code %s} object", datatype.getBuilder().getSimpleName());
    if (!unboxedKeyType.isPresent() || !unboxedValueType.isPresent()) {
      code.add(" * @throws NullPointerException if ");
      if (unboxedKeyType.isPresent()) {
        code.add("{@code value}");
      } else if (unboxedValueType.isPresent()) {
        code.add("{@code key}");
      } else {
        code.add("either {@code key} or {@code value}");
      }
      code.add(" is null\n");
    }
    code.addLine(" */")
        .addLine(
            "public %s %s(%s key, %s value) {",
            datatype.getBuilder(),
            putMethod(property),
            unboxedKeyType.orElse(keyType),
            unboxedValueType.orElse(valueType));
    if (!unboxedKeyType.isPresent()) {
      code.addLine("  %s.checkNotNull(key);", Preconditions.class);
    }
    if (!unboxedValueType.isPresent()) {
      code.addLine("  %s.checkNotNull(value);", Preconditions.class);
    }
    code.addLine("  %s.put(key, value);", property.getField())
        .addLine("  return (%s) this;", datatype.getBuilder())
        .addLine("}");
  }

  private void addSingleKeyPutAll(SourceBuilder code) {
    code.addLine("")
        .addLine("/**")
        .addLine(" * Adds a collection of {@code values} with the same {@code key} to the")
        .addLine(
            " * multimap to be returned from %s, ignoring duplicate values",
            datatype.getType().javadocNoArgMethodLink(property.getGetterName()))
        .addLine(" * (only the first duplicate value is added).")
        .addLine(" *")
        .addLine(" * @return this {@code %s} object", datatype.getBuilder().getSimpleName());
    if (unboxedKeyType.isPresent()) {
      code.addLine(
          " * @throws NullPointerException if {@code values} is null or contains a"
              + " null element");
    } else {
      code.addLine(" * @throws NullPointerException if either {@code key} or {@code values} is")
          .addLine(" *     null, or if {@code values} contains a null element");
    }
    code.addLine(" */")
        .addLine(
            "public %s %s(%s key, %s<? extends %s> values) {",
            datatype.getBuilder(),
            putAllMethod(property),
            unboxedKeyType.orElse(keyType),
            Iterable.class,
            valueType)
        .addLine("  for (%s value : values) {", unboxedValueType.orElse(valueType))
        .addLine("    %s(key, value);", putMethod(property))
        .addLine("  }")
        .addLine("  return (%s) this;", datatype.getBuilder())
        .addLine("}");
  }

  private void addMultimapPutAll(SourceBuilder code) {
    code.addLine("")
        .addLine("/**")
        .addLine(" * Adds each mapping in {@code multimap} to the multimap to be returned from")
        .addLine(
            " * %s, ignoring duplicate mappings",
            datatype.getType().javadocNoArgMethodLink(property.getGetterName()))
        .addLine(" * (only the first duplicate mapping is added).")
        .addLine(" *")
        .addLine(" * @return this {@code %s} object", datatype.getBuilder().getSimpleName())
        .addLine(" * @throws NullPointerException if {@code multimap} is null or contains a")
        .addLine(" *     null key or value")
        .addLine(" */");
    addAccessorAnnotations(code);
    code.addLine(
            "public %s %s(%s<? extends %s, ? extends %s> multimap) {",
            datatype.getBuilder(), putAllMethod(property), Multimap.class, keyType, valueType)
        .addLine(
            "  for (%s<? extends %s, ? extends %s<? extends %s>> entry",
            Entry.class, keyType, Collection.class, valueType)
        .addLine("      : multimap.asMap().entrySet()) {")
        .addLine(
            "    %s(entry.getKey(), entry.getValue());",
            putAllMethod(property), property.getCapitalizedName())
        .addLine("  }")
        .addLine("  return (%s) this;", datatype.getBuilder())
        .addLine("}");
  }

  private void addRemove(SourceBuilder code) {
    code.addLine("")
        .addLine("/**")
        .addLine(
            " * Removes a single key-value pair with the key {@code key} and the value"
                + " {@code value}")
        .addLine(
            " * from the multimap to be returned from %s.",
            datatype.getType().javadocNoArgMethodLink(property.getGetterName()))
        .addLine(" *")
        .addLine(" * @return this {@code %s} object", datatype.getBuilder().getSimpleName());
    if (!unboxedKeyType.isPresent() || !unboxedValueType.isPresent()) {
      code.add(" * @throws NullPointerException if ");
      if (unboxedKeyType.isPresent()) {
        code.add("{@code value}");
      } else if (unboxedValueType.isPresent()) {
        code.add("{@code key}");
      } else {
        code.add("either {@code key} or {@code value}");
      }
      code.add(" is null\n");
    }
    code.addLine(" */")
        .addLine(
            "public %s %s(%s key, %s value) {",
            datatype.getBuilder(),
            removeMethod(property),
            unboxedKeyType.orElse(keyType),
            unboxedValueType.orElse(valueType));
    if (!unboxedKeyType.isPresent()) {
      code.addLine("  %s.checkNotNull(key);", Preconditions.class);
    }
    if (!unboxedValueType.isPresent()) {
      code.addLine("  %s.checkNotNull(value);", Preconditions.class);
    }
    code.addLine("  %s.remove(key, value);", property.getField())
        .addLine("  return (%s) this;", datatype.getBuilder())
        .addLine("}");
  }

  private void addRemoveAll(SourceBuilder code) {
    code.addLine("")
        .addLine("/**")
        .addLine(" * Removes all values associated with the key {@code key} from the multimap to")
        .addLine(
            " * be returned from %s.",
            datatype.getType().javadocNoArgMethodLink(property.getGetterName()))
        .addLine(" *")
        .addLine(" * @return this {@code %s} object", datatype.getBuilder().getSimpleName());
    if (!unboxedKeyType.isPresent()) {
      code.add(" * @throws NullPointerException if {@code key} is null\n");
    }
    code.addLine(" */")
        .addLine(
            "public %s %s(%s key) {",
            datatype.getBuilder(), removeAllMethod(property), unboxedKeyType.orElse(keyType));
    if (!unboxedKeyType.isPresent()) {
      code.addLine("  %s.checkNotNull(key);", Preconditions.class);
    }
    code.addLine("  %s.removeAll(key);", property.getField())
        .addLine("  return (%s) this;", datatype.getBuilder())
        .addLine("}");
  }

  private void addMutate(SourceBuilder code) {
    code.addLine("")
        .addLine("/**")
        .addLine(
            " * Applies {@code mutator} to the multimap to be returned from %s.",
            datatype.getType().javadocNoArgMethodLink(property.getGetterName()))
        .addLine(" *")
        .addLine(" * <p>This method mutates the multimap in-place. {@code mutator} is a void")
        .addLine(" * consumer, so any value returned from a lambda will be ignored.")
        .addLine(" *")
        .addLine(" * @return this {@code Builder} object")
        .addLine(" * @throws NullPointerException if {@code mutator} is null")
        .addLine(" */")
        .addLine(
            "public %s %s(%s mutator) {",
            datatype.getBuilder(), mutator(property), mutatorType.getFunctionalInterface());
    if (overridesPutMethod) {
      code.addLine(
          "  mutator.%s(new %s<>(%s, this::%s));",
          mutatorType.getMethodName(),
          CheckedSetMultimap.TYPE,
          property.getField(),
          putMethod(property));
    } else {
      code.addLine(
              "  // If %s is overridden, this method will be updated to delegate to it",
              putMethod(property))
          .addLine("  mutator.%s(%s);", mutatorType.getMethodName(), property.getField());
    }
    code.addLine("  return (%s) this;", datatype.getBuilder()).addLine("}");
  }

  private void addClear(SourceBuilder code) {
    code.addLine("")
        .addLine("/**")
        .addLine(" * Removes all of the mappings from the multimap to be returned from")
        .addLine(" * %s.", datatype.getType().javadocNoArgMethodLink(property.getGetterName()))
        .addLine(" *")
        .addLine(" * @return this {@code %s} object", datatype.getBuilder().getSimpleName())
        .addLine(" */")
        .addLine("public %s %s() {", datatype.getBuilder(), clearMethod(property))
        .addLine("  %s.clear();", property.getField())
        .addLine("  return (%s) this;", datatype.getBuilder())
        .addLine("}");
  }

  private void addGetter(SourceBuilder code) {
    code.addLine("")
        .addLine("/**")
        .addLine(" * Returns an unmodifiable view of the multimap that will be returned by")
        .addLine(" * %s.", datatype.getType().javadocNoArgMethodLink(property.getGetterName()))
        .addLine(" * Changes to this builder will be reflected in the view.")
        .addLine(" */")
        .addLine(
            "public %s<%s, %s> %s() {", SetMultimap.class, keyType, valueType, getter(property))
        .addLine("  return %s.unmodifiableSetMultimap(%s);", Multimaps.class, property.getField())
        .addLine("}");
  }

  @Override
  public void addFinalFieldAssignment(SourceBuilder code, Excerpt finalField, String builder) {
    code.addLine(
        "%s = %s.copyOf(%s);",
        finalField, ImmutableSetMultimap.class, property.getField().on(builder));
  }

  @Override
  public void addAssignToBuilder(SourceBuilder code, Variable builder) {
    code.addLine("%s.putAll(%s);", property.getField().on(builder), property.getField());
  }

  @Override
  public void addMergeFromValue(SourceBuilder code, String value) {
    code.addLine("%s(%s.%s());", putAllMethod(property), value, property.getGetterName());
  }

  @Override
  public void addMergeFromBuilder(SourceBuilder code, String builder) {
    Excerpt base = Declarations.upcastToGeneratedBuilder(code, datatype, builder);
    code.addLine("%s(%s);", putAllMethod(property), property.getField().on(base));
  }

  @Override
  public Set<MergeAction> getMergeActions() {
    return ImmutableSet.of(appendingToCollections());
  }

  @Override
  public void addSetFromResult(SourceBuilder code, Excerpt builder, Excerpt variable) {
    code.addLine("%s.%s(%s);", builder, putAllMethod(property), variable);
  }

  @Override
  public void addClearField(SourceBuilder code) {
    code.addLine("%s.clear();", property.getField());
  }
}
