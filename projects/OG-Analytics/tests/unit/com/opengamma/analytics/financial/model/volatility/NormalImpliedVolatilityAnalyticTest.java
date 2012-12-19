/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility;

import static org.testng.AssertJUnit.fail;

import java.util.Random;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.NormalFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.NormalPriceFunction;

public class NormalImpliedVolatilityAnalyticTest {

  private static final NormalPriceFunction FUNCTION = new NormalPriceFunction();

  @Test
  public void testNearATM() {
    testRange(0.001, 2.0e-15, 1.9e-15);
  }

  @Test
  public void testNaturalRange() {
    testRange(3.0, 1.13e-10, 4.4e-13);
  }

  @Test
  public void testWideRange() {
    testRange(7.6, 0.021, 4.4e-13);
  }

  public void testRange(double dMax, double expectedMaxRelErrVol, double expectedMaxRelErrPrice) {

    NormalImpliedVolatility normalImpliedVol = new NormalImpliedVolatilityAnalytic();

    final double bpvol = 0.01;
    final double t = 10.0;
    final double f = 0.02;

    final double df = 0.90;

    double maxRelErrVol = -1.0;
    double maxRelErrPrice = -1.0;
    double dAtMaxErrVol = Double.NaN;
    double dAtMaxErrPrice = Double.NaN;
    Random rng = new Random();
    for (int i = 0; i < 500000; ++i) {
      double d = rng.nextDouble() * dMax;
      //d = 7.6954462;
      double k = f - d * bpvol * Math.sqrt(t);
      NormalFunctionData data = new NormalFunctionData(f, df, bpvol);
      EuropeanVanillaOption option = new EuropeanVanillaOption(k, t, true);
      final double expectedPrice = FUNCTION.getPriceFunction(option).evaluate(data);

      final double impliedBpvol = normalImpliedVol.getImpliedVolatility(data, option, expectedPrice);

      if (Double.isNaN(impliedBpvol)) {
        fail("NaN problem at Strike " + k + " d=" + d);
      }

      if (Double.isInfinite(impliedBpvol)) {
        fail("Infinite Problem at Strike " + k + " d=" + d);
      }

      NormalFunctionData newData = new NormalFunctionData(f, df, impliedBpvol);
      final double impliedPrice = FUNCTION.getPriceFunction(option).evaluate(newData);

      double relErrVol = Math.abs(bpvol - impliedBpvol) / bpvol;
      if (relErrVol > maxRelErrVol) {
        maxRelErrVol = relErrVol;
        dAtMaxErrVol = d;
      }

      double relErrPrice = Math.abs(impliedPrice - expectedPrice) / expectedPrice;
      if (relErrPrice > maxRelErrPrice) {
        maxRelErrPrice = relErrPrice;
        dAtMaxErrPrice = d;
      }

    }

    if (!(maxRelErrVol < expectedMaxRelErrVol)) {
        fail("Max Relative Error in vol failed at d="+dAtMaxErrVol);
    }
    if (!(maxRelErrPrice<expectedMaxRelErrPrice)) {
      fail("Max Relative Error in price failed at d="+dAtMaxErrPrice);
    }

  }

}
