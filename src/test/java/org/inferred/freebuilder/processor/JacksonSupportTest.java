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

import static com.google.common.collect.Iterables.getOnlyElement;
import static com.google.common.truth.Truth.assertThat;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import java.lang.annotation.Annotation;
import java.util.Map;
import org.inferred.freebuilder.processor.Analyser.CannotGenerateCodeException;
import org.inferred.freebuilder.processor.property.Property;
import org.inferred.freebuilder.processor.source.Excerpt;
import org.inferred.freebuilder.processor.source.SourceBuilder;
import org.inferred.freebuilder.processor.source.testing.MessagerRule;
import org.inferred.freebuilder.processor.source.testing.ModelRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link Analyser}. */
@RunWith(JUnit4.class)
public class JacksonSupportTest {

  @Rule public final ModelRule model = new ModelRule();
  @Rule public final MessagerRule messager = new MessagerRule();

  private Analyser analyser;

  @Before
  public void setup() {
    analyser = new Analyser(model.environment(), messager);
  }

  @Test
  public void noAnnotationAddedIfJsonDeserializeMissing() throws CannotGenerateCodeException {
    GeneratedBuilder builder =
        (GeneratedBuilder)
            analyser.analyse(
                model.newType(
                    "package com.example;",
                    "public interface DataType {",
                    "  int getFooBar();",
                    "  class Builder extends DataType_Builder {}",
                    "}"));

    Property property = getOnlyElement(builder.getGeneratorsByProperty().keySet());
    assertThat(property.getAccessorAnnotations()).named("property accessor annotations").isEmpty();
  }

  @Test
  public void jacksonAnnotationAddedWithExplicitName() throws CannotGenerateCodeException {
    // See also https://github.com/google/FreeBuilder/issues/68
    GeneratedBuilder builder =
        (GeneratedBuilder)
            analyser.analyse(
                model.newType(
                    "package com.example;",
                    "import " + JsonProperty.class.getName() + ";",
                    "@" + JsonDeserialize.class.getName() + "(builder = DataType.Builder.class)",
                    "public interface DataType {",
                    "  @JsonProperty(\"bob\") int getFooBar();",
                    "  class Builder extends DataType_Builder {}",
                    "}"));

    Property property = getOnlyElement(builder.getGeneratorsByProperty().keySet());
    assertPropertyHasAnnotation(property, JsonProperty.class, "@JsonProperty(\"bob\")");
  }

  @Test
  public void jacksonXmlAnnotationAddedWithExplicitName() throws CannotGenerateCodeException {
    // See also https://github.com/google/FreeBuilder/issues/68
    GeneratedBuilder builder =
        (GeneratedBuilder)
            analyser.analyse(
                model.newType(
                    "package com.example;",
                    "import " + JacksonXmlProperty.class.getName() + ";",
                    "@" + JsonDeserialize.class.getName() + "(builder = DataType.Builder.class)",
                    "public interface DataType {",
                    "  @JacksonXmlProperty(localName=\"b-ob\") int getFooBar();",
                    "  class Builder extends DataType_Builder {}",
                    "}"));

    Property property = getOnlyElement(builder.getGeneratorsByProperty().keySet());
    assertPropertyHasAnnotation(
        property, JacksonXmlProperty.class, "@JacksonXmlProperty(localName = \"b-ob\")");
  }

  @Test
  public void jacksonXmlTextAnnotationAdded() throws CannotGenerateCodeException {
    // See also https://github.com/google/FreeBuilder/issues/68
    GeneratedBuilder builder =
        (GeneratedBuilder)
            analyser.analyse(
                model.newType(
                    "package com.example;",
                    "import " + JacksonXmlText.class.getName() + ";",
                    "@" + JsonDeserialize.class.getName() + "(builder = DataType.Builder.class)",
                    "public interface DataType {",
                    "  @JacksonXmlText String getFooBar();",
                    "  class Builder extends DataType_Builder {}",
                    "}"));

    Property property = getOnlyElement(builder.getGeneratorsByProperty().keySet());
    assertPropertyHasAnnotation(property, JacksonXmlText.class, "@JacksonXmlText");
  }

  @Test
  public void jacksonAnnotationAddedWithImplicitName() throws CannotGenerateCodeException {
    // See also https://github.com/google/FreeBuilder/issues/90
    GeneratedBuilder builder =
        (GeneratedBuilder)
            analyser.analyse(
                model.newType(
                    "package com.example;",
                    "@" + JsonDeserialize.class.getName() + "(builder = DataType.Builder.class)",
                    "public interface DataType {",
                    "  int getFooBar();",
                    "  class Builder extends DataType_Builder {}",
                    "}"));

    Property property = getOnlyElement(builder.getGeneratorsByProperty().keySet());
    assertPropertyHasAnnotation(property, JsonProperty.class, "@JsonProperty(\"fooBar\")");
  }

  @Test
  public void jsonAnyGetterAnnotationDisablesImplicitProperty() throws CannotGenerateCodeException {
    GeneratedBuilder builder =
        (GeneratedBuilder)
            analyser.analyse(
                model.newType(
                    "package com.example;",
                    "@" + JsonDeserialize.class.getName() + "(builder = DataType.Builder.class)",
                    "public interface DataType {",
                    "  @" + JsonAnyGetter.class.getName(),
                    "  " + Map.class.getName() + "<Integer, String> getFooBar();",
                    "  class Builder extends DataType_Builder {}",
                    "}"));

    Property property = getOnlyElement(builder.getGeneratorsByProperty().keySet());
    assertThat(property.getAccessorAnnotations()).named("property accessor annotations").isEmpty();
  }

  private static void assertPropertyHasAnnotation(
      Property property, Class<? extends Annotation> annotationClass, String annotationString) {
    Excerpt annotationExcerpt =
        property.getAccessorAnnotations().stream()
            .filter(
                excerpt -> asCompilableString(excerpt).contains(annotationClass.getCanonicalName()))
            .findFirst()
            .orElse(null);
    assertThat(annotationExcerpt).named("property accessor annotations").isNotNull();
    assertThat(asString(annotationExcerpt)).contains(String.format("%s%n", annotationString));
  }

  private static String asCompilableString(Excerpt excerpt) {
    return SourceBuilder.forTesting()
        .addLine("package com.example;")
        .add(excerpt)
        .addLine(" interface Foo { }")
        .toString();
  }

  private static String asString(Excerpt excerpt) {
    return SourceBuilder.forTesting().add(excerpt).toString();
  }
}
