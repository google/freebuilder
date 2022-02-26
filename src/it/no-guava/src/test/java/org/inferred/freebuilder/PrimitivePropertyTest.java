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
package org.inferred.freebuilder;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Integration test for {@link org.inferred.freebuilder.FreeBuilder FreeBuilder}, using the {@link
 * PrimitiveProperty}.
 */
@RunWith(JUnit4.class)
public class PrimitivePropertyTest {

  @Test
  public void test() {
    PrimitiveProperty b = new PrimitiveProperty.Builder().setProperty(11).build();
    assertEquals(11, b.getProperty());
  }
}
