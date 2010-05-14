/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.greeks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class MultipleGreekResultTest {

  @Test(expected = IllegalArgumentException.class)
  public void testNull() {
    new MultipleGreekResult(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmpty() {
    new MultipleGreekResult(Collections.<String, Double> emptyMap());
  }

  public void test() {
    final Map<String, Double> map = new HashMap<String, Double>();
    map.put("A", 1.);
    map.put("B", 2.);
    final MultipleGreekResult result = new MultipleGreekResult(map);
    assertTrue(result.isMultiValued());
    assertEquals(map, result.getResult());
  }
}
