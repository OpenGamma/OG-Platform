/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import org.junit.Test;

import com.opengamma.id.UniqueIdentifier;

/**
 * Test FixedIncomeStrip.
 */
public class FixedIncomeStripTest {

  @Test(expected = IllegalArgumentException.class)
  public void negativeYears() {
    new FixedIncomeStrip(-5, UniqueIdentifier.of("Test", "A"));
  }

  // TODO kirk 2009-12-30 -- Test everything else.

}
