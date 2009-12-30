/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import org.junit.Test;

/**
 * 
 *
 * @author kirk
 */
public class NewFixedIncomeStripTest {
  
  @Test(expected=IllegalArgumentException.class)
  public void negativeYears() {
    new NewFixedIncomeStrip(-5, "");
  }
  
  // TODO kirk 2009-12-30 -- Test everything else.

}
