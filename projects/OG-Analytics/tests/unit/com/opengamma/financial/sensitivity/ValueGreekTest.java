/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.sensitivity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.opengamma.financial.greeks.Greek;

/**
 * 
 */
public class ValueGreekTest {
  private static final ValueGreek GREEK = new ValueGreek(Greek.GAMMA);

  @Test(expected = IllegalArgumentException.class)
  public void testNullGreek() {
    new ValueGreek(null);
  }

  @Test
  public void test() {
    final ValueGreek greek1 = new ValueGreek(Greek.GAMMA);
    final ValueGreek greek2 = new ValueGreek(Greek.GAMMA_BLEED);
    assertEquals(GREEK.getUnderlyingGreek(), Greek.GAMMA);
    assertEquals(GREEK, greek1);
    assertFalse(GREEK.equals(greek2));
    assertEquals(GREEK.hashCode(), greek1.hashCode());
    assertTrue(GREEK.equals(GREEK));
    assertFalse(GREEK.equals(null));
    assertFalse(GREEK.equals(Greek.GAMMA));
  }
}
