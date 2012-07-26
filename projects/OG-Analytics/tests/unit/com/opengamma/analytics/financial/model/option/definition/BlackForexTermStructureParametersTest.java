/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.ObjectsPair;

/**
 * Test constructor and volatility provider for BlackForexTermStructureParameters.
 */
public class BlackForexTermStructureParametersTest {

  private static final Interpolator1D LINEAR_FLAT = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final double[] NODES = new double[] {0.01, 0.50, 1.00, 2.01, 5.00};
  private static final double[] VOL = new double[] {0.20, 0.25, 0.20, 0.15, 0.20};
  private static final InterpolatedDoublesCurve TERM_STRUCTURE_VOL = new InterpolatedDoublesCurve(NODES, VOL, LINEAR_FLAT, true);
  private static final ObjectsPair<Currency, Currency> CCY = new ObjectsPair<Currency, Currency>(Currency.EUR, Currency.USD);
  private static final BlackForexTermStructureParameters BLACK_TERM_STRUCTURE = new BlackForexTermStructureParameters(TERM_STRUCTURE_VOL, CCY);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCurve() {
    new BlackForexTermStructureParameters(null, CCY);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCcy() {
    new BlackForexTermStructureParameters(TERM_STRUCTURE_VOL, null);
  }

  @Test
  /**
   * Tests the class getters.
   */
  public void getter() {
    assertEquals("Black Forex Term Structure: getter", TERM_STRUCTURE_VOL, BLACK_TERM_STRUCTURE.getVolatilityCurve());
    assertEquals("Black Forex Term Structure: getter", CCY, BLACK_TERM_STRUCTURE.getCurrencyPair());
  }

  @Test
  /**
   * Tests the class getters.
   */
  public void getVolatility() {
    double[] t = new double[] {0.30, 2.54, 5.0, 10.1};
    for (int loopt = 0; loopt < t.length; loopt++) {
      assertEquals("Black Forex Term Structure: getVolatility", TERM_STRUCTURE_VOL.getYValue(t[loopt]), BLACK_TERM_STRUCTURE.getVolatility(t[loopt]));
    }
  }

}
