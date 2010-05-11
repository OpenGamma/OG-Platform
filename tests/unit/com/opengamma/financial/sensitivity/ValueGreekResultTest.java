/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.sensitivity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Map;

import org.junit.Test;

public class ValueGreekResultTest {

  @Test(expected = IllegalArgumentException.class)
  public void testSingleResultConstructor() {
    new SingleValueGreekResult(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMultipleResultWithNull() {
    new MultipleValueGreekResult(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMultipleResultWithEmptyMap() {
    new MultipleValueGreekResult(Collections.<String, Double> emptyMap());
  }

  @Test
  public void test() {
    final ValueGreekResult<Double> single = new SingleValueGreekResult(3.4);
    assertFalse(single.isMultiValued());
    assertEquals(single.getResult(), 3.4, 0);
    final Map<String, Double> map = Collections.<String, Double> singletonMap("A", 1.2);
    final ValueGreekResult<Map<String, Double>> multiple = new MultipleValueGreekResult(map);
    assertTrue(multiple.isMultiValued());
    assertEquals(map, multiple.getResult());
  }
}
