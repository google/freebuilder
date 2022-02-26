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

import static org.inferred.freebuilder.processor.source.feature.GuavaLibrary.GUAVA;
import static org.junit.Assume.assumeTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;
import org.inferred.freebuilder.FreeBuilder;
import org.inferred.freebuilder.processor.FeatureSets;
import org.inferred.freebuilder.processor.NamingConvention;
import org.inferred.freebuilder.processor.Processor;
import org.inferred.freebuilder.processor.source.SourceBuilder;
import org.inferred.freebuilder.processor.source.feature.FeatureSet;
import org.inferred.freebuilder.processor.source.testing.BehaviorTester;
import org.inferred.freebuilder.processor.source.testing.ParameterizedBehaviorTestFactory;
import org.inferred.freebuilder.processor.source.testing.ParameterizedBehaviorTestFactory.Shared;
import org.inferred.freebuilder.processor.source.testing.TestBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Parameterized.UseParametersRunnerFactory;

@RunWith(Parameterized.class)
@UseParametersRunnerFactory(ParameterizedBehaviorTestFactory.class)
public class RequiredMapperMethodTest {

  @SuppressWarnings("unchecked")
  @Parameters(name = "{0}, checked={1}, {2}, {3}")
  public static Iterable<Object[]> parameters() {
    List<ElementFactory> types = ElementFactory.TYPES_WITH_EXTRA_PRIMITIVES;
    List<Boolean> checked = ImmutableList.of(false, true);
    List<NamingConvention> conventions = Arrays.asList(NamingConvention.values());
    List<FeatureSet> features = FeatureSets.ALL;
    return () ->
        Lists.cartesianProduct(types, checked, conventions, features).stream()
            .map(List::toArray)
            .iterator();
  }

  @Rule public final ExpectedException thrown = ExpectedException.none();
  @Shared public BehaviorTester behaviorTester;

  private final ElementFactory property;
  private final boolean checked;
  private final NamingConvention convention;
  private final FeatureSet features;
  private final SourceBuilder dataType;

  public RequiredMapperMethodTest(
      ElementFactory property, boolean checked, NamingConvention convention, FeatureSet features) {
    this.property = property;
    this.checked = checked;
    this.convention = convention;
    this.features = features;

    dataType =
        SourceBuilder.forTesting()
            .addLine("package com.example;")
            .addLine("@%s", FreeBuilder.class)
            .addLine("public interface DataType {")
            .addLine("  %s %s;", property.unwrappedType(), convention.get("property"))
            .addLine("")
            .addLine("  class Builder extends DataType_Builder {");
    if (checked) {
      dataType
          .addLine(
              "    @Override public Builder %s(%s property) {",
              convention.set("property"), property.unwrappedType())
          .addLine("      if (!(%s)) {", property.validation("property"))
          .addLine(
              "        throw new IllegalArgumentException(\"%s\");",
              property.errorMessage("property"))
          .addLine("      }")
          .addLine("      return super.%s(property);", convention.set("property"))
          .addLine("    }");
    }
    dataType.addLine("  }").addLine("}");
  }

  @Test
  public void mapReplacesValueToBeReturnedFromGetter() {
    behaviorTester
        .with(new Processor(features))
        .with(dataType)
        .with(
            testBuilder()
                .addLine("DataType value = new DataType.Builder()")
                .addLine("    .%s(%s)", convention.set("property"), property.example(0))
                .addLine(
                    "    .mapProperty(a -> (%s) (a + %s))",
                    property.unwrappedType(), property.example(1))
                .addLine("    .build();")
                .addLine(
                    "assertEquals((%s) (%s + %s), value.%s);",
                    property.unwrappedType(),
                    property.example(0),
                    property.example(1),
                    convention.get("property"))
                .build())
        .runTest();
  }

  @Test
  public void mapThrowsNpeIfMapperIsNull() {
    thrown.expect(NullPointerException.class);
    behaviorTester
        .with(new Processor(features))
        .with(dataType)
        .with(
            testBuilder()
                .addLine("new DataType.Builder()")
                .addLine("    .%s(%s)", convention.set("property"), property.example(0))
                .addLine("    .mapProperty(null);")
                .build())
        .runTest();
  }

  @Test
  public void mapThrowsNpeIfMapperIsNullEvenIfPropertyUnset() {
    thrown.expect(NullPointerException.class);
    behaviorTester
        .with(new Processor(features))
        .with(dataType)
        .with(testBuilder().addLine("new DataType.Builder().mapProperty(null);").build())
        .runTest();
  }

  @Test
  public void mapThrowsNpeIfMapperReturnsNull() {
    thrown.expect(NullPointerException.class);
    behaviorTester
        .with(new Processor(features))
        .with(dataType)
        .with(
            testBuilder()
                .addLine("new DataType.Builder()")
                .addLine("    .%s(%s)", convention.set("property"), property.example(0))
                .addLine("    .mapProperty(a -> (%s) null);", property.type())
                .build())
        .runTest();
  }

  @Test
  public void mapThrowsIllegalStateExceptionIfPropertyIsUnset() {
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("property not set");
    behaviorTester
        .with(new Processor(features))
        .with(dataType)
        .with(
            testBuilder()
                .addLine("new DataType.Builder().mapProperty(a -> %s);", property.example(0))
                .build())
        .runTest();
  }

  @Test
  public void mapDelegatesToSetterForValidation() {
    if (checked) {
      thrown.expect(IllegalArgumentException.class);
      thrown.expectMessage(property.errorMessage("property"));
    }
    behaviorTester
        .with(new Processor(features))
        .with(dataType)
        .with(
            testBuilder()
                .addLine("new DataType.Builder()")
                .addLine("    .%s(%s)", convention.set("property"), property.example(0))
                .addLine("    .mapProperty(a -> %s);", property.invalidExample())
                .build())
        .runTest();
  }

  @Test
  public void mapCanAcceptPrimitiveFunctionalInterface() {
    SourceBuilder customMapperType = SourceBuilder.forTesting();
    for (String line : dataType.toString().split("\n")) {
      if (line.contains("extends DataType_Builder")) {
        int insertOffset = line.indexOf('{') + 1;
        customMapperType
            .addLine("%s", line.substring(0, insertOffset))
            .addLine(
                "    @Override public Builder mapProperty(%s mapper) {",
                property.unboxedUnaryOperator())
            .addLine("      return super.mapProperty(mapper);")
            .addLine("    }")
            .addLine("%s", line.substring(insertOffset));
      } else {
        customMapperType.addLine("%s", line);
      }
    }
    behaviorTester
        .with(new Processor(features))
        .with(customMapperType)
        .with(
            testBuilder()
                .addLine("DataType value = new DataType.Builder()")
                .addLine("    .%s(%s)", convention.set("property"), property.example(0))
                .addLine(
                    "    .mapProperty(a -> (%s) (a + %s))",
                    property.unwrappedType(), property.example(1))
                .addLine("    .build();")
                .addLine(
                    "assertEquals((%s) (%s + %s), value.%s);",
                    property.unwrappedType(),
                    property.example(0),
                    property.example(1),
                    convention.get("property"))
                .build())
        .runTest();
  }

  @Test
  public void mapCanAcceptGenericFunctionalInterface() {
    SourceBuilder customMapperType = SourceBuilder.forTesting();
    for (String line : dataType.toString().split("\n")) {
      if (line.contains("extends DataType_Builder")) {
        int insertOffset = line.indexOf('{') + 1;
        customMapperType
            .addLine("%s", line.substring(0, insertOffset))
            .addLine(
                "    @Override public Builder mapProperty(%s<%s> mapper) {",
                UnaryOperator.class, property.type())
            .addLine("      return super.mapProperty(mapper);")
            .addLine("    }")
            .addLine("%s", line.substring(insertOffset));
      } else {
        customMapperType.addLine("%s", line);
      }
    }
    behaviorTester
        .with(new Processor(features))
        .with(customMapperType)
        .with(
            testBuilder()
                .addLine("DataType value = new DataType.Builder()")
                .addLine("    .%s(%s)", convention.set("property"), property.example(0))
                .addLine(
                    "    .mapProperty(a -> (%s) (a + %s))",
                    property.unwrappedType(), property.example(1))
                .addLine("    .build();")
                .addLine(
                    "assertEquals((%s) (%s + %s), value.%s);",
                    property.unwrappedType(),
                    property.example(0),
                    property.example(1),
                    convention.get("property"))
                .build())
        .runTest();
  }

  @Test
  public void mapCanAcceptOtherFunctionalInterface() {
    assumeGuavaAvailable();
    SourceBuilder customMapperType = SourceBuilder.forTesting();
    for (String line : dataType.toString().split("\n")) {
      if (line.contains("extends DataType_Builder")) {
        int insertOffset = line.indexOf('{') + 1;
        customMapperType
            .addLine("%s", line.substring(0, insertOffset))
            .addLine(
                "    @Override public Builder mapProperty(%1$s<%2$s, %2$s> mapper) {",
                com.google.common.base.Function.class, property.type())
            .addLine("      return super.mapProperty(mapper);")
            .addLine("    }")
            .addLine("%s", line.substring(insertOffset));
      } else {
        customMapperType.addLine("%s", line);
      }
    }
    behaviorTester
        .with(new Processor(features))
        .with(customMapperType)
        .with(
            testBuilder()
                .addLine("DataType value = new DataType.Builder()")
                .addLine("    .%s(%s)", convention.set("property"), property.example(0))
                .addLine(
                    "    .mapProperty(a -> (%s) (a + %s))",
                    property.unwrappedType(), property.example(1))
                .addLine("    .build();")
                .addLine(
                    "assertEquals((%s) (%s + %s), value.%s);",
                    property.unwrappedType(),
                    property.example(0),
                    property.example(1),
                    convention.get("property"))
                .build())
        .runTest();
  }

  private void assumeGuavaAvailable() {
    assumeTrue("Guava available", features.get(GUAVA).isAvailable());
  }

  private static TestBuilder testBuilder() {
    return new TestBuilder().addImport("com.example.DataType");
  }
}
