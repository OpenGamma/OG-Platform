/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.greeks;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * 
 */
public class GreekResultCollectionTest {

  @Test
  public void testHashCode() {

    final GreekResultCollection grc1 = new GreekResultCollection();
    final GreekResultCollection grc2 = new GreekResultCollection();

    assertTrue(grc1.hashCode() == grc2.hashCode());

    grc1.put(Greek.DELTA, new SingleGreekResult(1.));
    assertFalse(grc1.hashCode() == grc2.hashCode());

    grc2.put(Greek.DELTA, new SingleGreekResult(1.));
    assertTrue(grc1.hashCode() == grc2.hashCode());

    grc1.put(Greek.GAMMA, new SingleGreekResult(1.));
    grc1.put(Greek.GAMMA, new SingleGreekResult(2.));

    assertFalse(grc1.hashCode() == grc2.hashCode());
  }

}
