/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;

import com.opengamma.financial.equity.future.EquityIndexDividendFuture;
import com.opengamma.financial.equity.PresentValueCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;

/**
 * 
 */
public class PresentValueCalculatorTest {

  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private static final YieldCurveBundle CURVES = new YieldCurveBundle();

  @Test
  public void testEquityIndexDividendFuture() {
    final double settlement = 1.45;
    final double fixing = 1.44;
    final EquityIndexDividendFuture eidf = new EquityIndexDividendFuture(fixing, settlement, 0, 0, 0);
    
    double pv = PVC.visitEquityIndexDividendFuture(eidf, CURVES);
    assertEquals(42.0, pv, 1e-12);
    
    
  }
}
