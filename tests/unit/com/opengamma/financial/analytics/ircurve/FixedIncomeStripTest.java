/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import org.junit.Test;

import com.opengamma.id.DomainSpecificIdentifier;
import com.opengamma.id.IdentificationDomain;

/**
 * 
 *
 * @author kirk
 */
public class FixedIncomeStripTest {
  
  @Test(expected=IllegalArgumentException.class)
  public void negativeYears() {
    new FixedIncomeStrip(-5, new DomainSpecificIdentifier(new IdentificationDomain(""), ""));
  }
  
  // TODO kirk 2009-12-30 -- Test everything else.

}
