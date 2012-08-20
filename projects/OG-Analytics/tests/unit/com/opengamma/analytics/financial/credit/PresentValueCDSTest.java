/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

/**
 * 
 */
public class PresentValueCDSTest {
  private static final PresentValueCDS PRESENT_VALUE_CDS = new PresentValueCDS();

  @Test
  public void testCDS() {
    assertEquals(10000, PRESENT_VALUE_CDS.getPresentValueCDS(100.0), 1e-15);
  }

}
