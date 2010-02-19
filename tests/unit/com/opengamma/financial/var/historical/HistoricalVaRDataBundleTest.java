/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var.historical;

import org.junit.Test;

import com.opengamma.financial.var.historical.HistoricalVaRDataBundle;

/**
 * @author emcleod
 * 
 */
public class HistoricalVaRDataBundleTest {

  @Test(expected = IllegalArgumentException.class)
  public void testConstructor() {
    new HistoricalVaRDataBundle(null);
  }
}
