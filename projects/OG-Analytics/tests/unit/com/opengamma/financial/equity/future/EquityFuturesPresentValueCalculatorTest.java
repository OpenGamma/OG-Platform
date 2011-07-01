/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity.future;

import static org.testng.AssertJUnit.assertEquals;

import com.opengamma.financial.equity.future.derivative.EquityIndexDividendFuture;
import com.opengamma.util.money.Currency;

import org.testng.annotations.Test;

/**
 * Checks the wiring of the EquityFuturesPresentValueCalculator
 */
public class EquityFuturesPresentValueCalculatorTest {

  private static final EquityFuturesPresentValueCalculator PVC = EquityFuturesPresentValueCalculator.getInstance();

  @Test
  public void testEquityIndexDividendFuture() {
    final double settlement = 1.45;
    final double fixing = 1.44;
    final EquityIndexDividendFuture eidf = new EquityIndexDividendFuture(fixing, settlement, 95., Currency.JPY, 10);

    final double currentPrice = 100.0;
    double pv = PVC.visitEquityIndexDividendFuture(eidf, currentPrice);
    assertEquals(50.0, pv, 1e-12);

  }
}
