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
package org.inferred.freebuilder.processor.source.testing;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;

import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import javax.tools.JavaFileObject;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link TestBuilder}. */
@RunWith(JUnit4.class)
@FixMethodOrder(NAME_ASCENDING)
public class TestBuilderTest {

  private final Multiset<String> seenNames = LinkedHashMultiset.create();

  @Test
  public void test1_UniqueNames() {
    JavaFileObject source1 = new TestBuilder().build().selectName(seenNames);
    assertThat(source1.toUri().toString())
        .endsWith(
            "/org/inferred/freebuilder/processor/source/testing/generatedcode/TestBuilderTest.java");
    JavaFileObject source2 = new TestBuilder().build().selectName(seenNames);
    assertThat(source2.toUri().toString()).endsWith("TestBuilderTest__2.java");
    assertEquals(2, seenNames.size());
  }

  public static class InnerClass {}

  @Test
  public void test2_InnerClassNames() {
    String result =
        new TestBuilder()
            .addLine("%s", InnerClass.class)
            .build()
            .selectName(seenNames)
            .getCharContent(false)
            .toString();
    assertThat(result)
        .contains("org.inferred.freebuilder.processor.source.testing.TestBuilderTest.InnerClass");
  }
}
