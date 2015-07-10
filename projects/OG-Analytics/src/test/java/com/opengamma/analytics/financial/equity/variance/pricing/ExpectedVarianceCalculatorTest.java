/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.variance.pricing;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceConverter;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceDelta;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceLogMoneyness;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceMoneyness;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceStrike;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ExpectedVarianceCalculatorTest {
  private static final double TOL = 1e-10;
  private static final double SPOT = 80;
  private static final double DRIFT = 0.05;
  private static final ForwardCurve FORWARD_CURVE = new ForwardCurve(SPOT, DRIFT);
  private static final ExpectedVarianceStaticReplicationCalculator CALCULATOR = new ExpectedVarianceStaticReplicationCalculator(TOL);

  /**
   * Start with a Black volatility surface (strike) - which comes from a mixed log-normal model (hence we know the answer analytically) and convert the surface to ones
   * parameterised by moneyness, log-moneyness and (Black) delta, then check we recover the expected value using all four surfaces.
   */
 // @Test
  public void testMixedLogNormalVolSurface() {

    final double sigma1 = 0.2;
    final double sigma2 = 1.0;
    final double w = 0.9;

    final Function<Double, Double> surf = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... x) {
        final double t = x[0];
        final double k = x[1];
        @SuppressWarnings("synthetic-access")
        final double fwd = FORWARD_CURVE.getForward(t);
        final boolean isCall = k > fwd;
        final double price = w * BlackFormulaRepository.price(fwd, k, t, sigma1, isCall) + (1 - w) * BlackFormulaRepository.price(fwd, k, t, sigma2, isCall);
        if (price < 1e-100) {
          return sigma2;
        }
        return BlackFormulaRepository.impliedVolatility(price, fwd, k, t, isCall);
      }
    };

    final double expiry = 1.5;
    final double fwd = FORWARD_CURVE.getForward(expiry);

    final BlackVolatilitySurfaceStrike surfaceStrike = new BlackVolatilitySurfaceStrike(FunctionalDoublesSurface.from(surf));
    final BlackVolatilitySurfaceMoneyness surfaceMoneyness = BlackVolatilitySurfaceConverter.toMoneynessSurface(surfaceStrike, FORWARD_CURVE);
    final BlackVolatilitySurfaceLogMoneyness surfaceLogMoneyness = BlackVolatilitySurfaceConverter.toLogMoneynessSurface(surfaceStrike, FORWARD_CURVE);
    final BlackVolatilitySurfaceDelta surfaceDelta = BlackVolatilitySurfaceConverter.toDeltaSurface(surfaceStrike, FORWARD_CURVE);

    final double expected = w * sigma1 * sigma1 + (1 - w) * sigma2 * sigma2;
    final double strikeVal = CALCULATOR.getAnnualizedVariance(fwd, expiry, surfaceStrike);
    final double moneynessVal = CALCULATOR.getAnnualizedVariance(expiry, surfaceMoneyness);
    final double logMoneynessVal = CALCULATOR.getAnnualizedVariance(expiry, surfaceLogMoneyness);
    final double deltaVal = CALCULATOR.getAnnualizedVariance(fwd, expiry, surfaceDelta);

    assertEquals("strike", expected, strikeVal, TOL);
    assertEquals("moneyness", expected, moneynessVal, 200 * TOL); //TODO why do we loss a lot of accuracy
    assertEquals("log-moneyness", expected, logMoneynessVal, 200 * TOL);
    assertEquals("delta", expected, deltaVal, 100 * TOL);
  }

}
