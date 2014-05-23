/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.future;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.equity.future.derivative.EquityIndexDividendFuture;
import com.opengamma.analytics.financial.future.MarkToMarketFuturesCalculator;
import com.opengamma.analytics.financial.simpleinstruments.pricing.SimpleFutureDataBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Checks the wiring of the EquityFuturesPresentValueCalculator
 */
@Test(groups = TestGroup.UNIT)
public class EquityFuturesPresentValueCalculatorTest {
  private static final MarkToMarketFuturesCalculator PVC = MarkToMarketFuturesCalculator.PresentValueCalculator.getInstance();

  @Test
  public void testEquityIndexDividendFuture() {
    final double settlement = 1.45;
    final double fixing = 1.44;
    final EquityIndexDividendFuture eidf = new EquityIndexDividendFuture(fixing, settlement, 95., Currency.JPY, 10);

    final double currentPrice = 100.0;
    final SimpleFutureDataBundle dataBundle = new SimpleFutureDataBundle(null, currentPrice, null, null, null);
    // FIXME Case - presentValue needs discounting..
    final double pv = eidf.accept(PVC, dataBundle);
    assertEquals(50.0, pv, 1e-12);

  }
}
