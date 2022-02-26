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
package org.inferred.freebuilder.processor.source.feature;

import static org.inferred.freebuilder.processor.source.feature.SourceLevel.SOURCE_LEVEL;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SourceLevelTest {

  @Test
  public void java8() {
    assertEquals(SourceLevel.JAVA_8, sourceLevelFrom(SourceVersion.RELEASE_8));
  }

  private static SourceLevel sourceLevelFrom(SourceVersion version) {
    ProcessingEnvironment env = mock(ProcessingEnvironment.class);
    when(env.getSourceVersion()).thenReturn(version);
    return SOURCE_LEVEL.forEnvironment(env, null);
  }
}
