/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.sensitivity;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ValueGreekTest {
  private static final ValueGreek GREEK = new ValueGreek(Greek.GAMMA);

  @Test(expectedExceptions = IllegalArgumentException.class)
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
