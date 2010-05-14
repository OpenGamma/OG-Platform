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

/**
 *
 */
public class PositionGreekResultTest {

  @Test(expected = IllegalArgumentException.class)
  public void testSingleResultConstructor() {
    new SinglePositionGreekResult(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMultipleResultWithNull() {
    new MultiplePositionGreekResult(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMultipleResultWithEmptyMap() {
    new MultiplePositionGreekResult(Collections.<String, Double> emptyMap());
  }

  @Test
  public void test() {
    final PositionGreekResult<Double> single = new SinglePositionGreekResult(3.4);
    assertFalse(single.isMultiValued());
    assertEquals(single.getResult(), 3.4, 0);
    final Map<String, Double> map = Collections.<String, Double> singletonMap("A", 1.2);
    final PositionGreekResult<Map<String, Double>> multiple = new MultiplePositionGreekResult(map);
    assertTrue(multiple.isMultiValued());
    assertEquals(map, multiple.getResult());
  }
}
