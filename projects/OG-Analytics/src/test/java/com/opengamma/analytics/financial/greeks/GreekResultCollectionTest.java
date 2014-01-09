/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.greeks;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class GreekResultCollectionTest {

  @Test
  public void testHashCode() {

    final GreekResultCollection grc1 = new GreekResultCollection();
    final GreekResultCollection grc2 = new GreekResultCollection();

    assertTrue(grc1.hashCode() == grc2.hashCode());

    grc1.put(Greek.DELTA, 1.);
    assertFalse(grc1.hashCode() == grc2.hashCode());

    grc2.put(Greek.DELTA, 1.);
    assertTrue(grc1.hashCode() == grc2.hashCode());

    grc1.put(Greek.GAMMA, 1.);
    grc1.put(Greek.GAMMA, 2.);

    assertFalse(grc1.hashCode() == grc2.hashCode());
  }

  @Test
  public void testToString() {
    final GreekResultCollection grc1 = new GreekResultCollection();
    grc1.put(Greek.DELTA, 1.);

    final String grcToString = grc1.toString();
    assertNotNull(grcToString);
    assertTrue(grcToString.indexOf("GreekResultCollection") != -1);
    assertTrue(grcToString.indexOf(Greek.DELTA.toString()) != -1);
  }

  @Test
  public void testEquals() {
    final GreekResultCollection grc1 = new GreekResultCollection();
    assertTrue(grc1.equals(grc1));
    assertFalse(grc1.equals(null));
    assertFalse(grc1.equals("foo"));

    final GreekResultCollection grc2 = new GreekResultCollection();
    assertTrue(grc1.equals(grc2));

    grc1.put(Greek.DELTA, 1.);
    assertFalse(grc1.equals(grc2));

    grc2.put(Greek.DELTA, 1.);
    assertTrue(grc1.equals(grc2));

    grc1.put(Greek.GAMMA, 3.);
    grc2.put(Greek.GAMMA_BLEED, 3.);
    assertFalse(grc1.equals(grc2));
  }

}
