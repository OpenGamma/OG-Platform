/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.option.DistributionFromImpliedVolatility;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.NormalFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.NormalPriceFunction;
import com.opengamma.analytics.financial.model.volatility.BlackImpliedVolatilityFormula;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class DistributionFromImpliedVolatilityTest {

  private static double F = 4.0;
  private static double VOL = 0.3;
  private static double NORMAL_VOL;
  private static double T = 2.5;
  private static double ROOT_T = Math.sqrt(T);
  private static double ROOT_2_PI = Math.sqrt(2 * Math.PI);
  private static ProbabilityDistribution<Double> BLACK;
  private static ProbabilityDistribution<Double> BACHELIER;
  private static ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);
  private static final BlackImpliedVolatilityFormula BLACK_IMPLIED_VOL = new BlackImpliedVolatilityFormula();
  private static final NormalPriceFunction NORMAL_PRICE_FUNCTION = new NormalPriceFunction();
  private static Function1D<Double, Double> FLAT = new Function1D<Double, Double>() {

    @SuppressWarnings("synthetic-access")
    @Override
    public Double evaluate(final Double x) {
      return VOL;
    }
  };

  private static Function1D<Double, Double> NORM = new Function1D<Double, Double>() {

    @SuppressWarnings("synthetic-access")
    @Override
    public Double evaluate(final Double x) {
      final EuropeanVanillaOption option = new EuropeanVanillaOption(x, T, true);
      final NormalFunctionData dataNormal = new NormalFunctionData(F, 1, NORMAL_VOL);
      final double price = NORMAL_PRICE_FUNCTION.getPriceFunction(option).evaluate(dataNormal);
      final BlackFunctionData dataBlack = new BlackFunctionData(F, 1, VOL);
      return BLACK_IMPLIED_VOL.getImpliedVolatility(dataBlack, option, price);
    }
  };

  static {
    BLACK = new DistributionFromImpliedVolatility(F, T, FLAT);
    BACHELIER = new DistributionFromImpliedVolatility(F, T, NORM);
    NORMAL_VOL = F * VOL;
  }

  @Test
  public void logNormalTest() {
    for (int i = 0; i < 100; i++) {
      final double x = 0.1 + 0.1 * i;
      // System.out.println(x + "\t" + logNormalPDF(x) + "\t" + BLACK.getPDF(x));
      assertEquals(logNormalPDF(x), BLACK.getPDF(x), 1e-6);
      assertEquals(logNormalCDF(x), BLACK.getCDF(x), 1e-6);
    }
  }

  @Test
  public void normalTest() {
    for (int i = 0; i < 100; i++) {
      final double x = 0.1 + 0.1 * i;
      // System.out.println(x + "\t" + normalPDF(x) + "\t" + BACHELIER.getPDF(x));
      assertEquals(normalPDF(x), BACHELIER.getPDF(x), 1e-4);
      assertEquals(normalCDF(x), BACHELIER.getCDF(x), 1e-4);
    }
  }

  private double normalPDF(final double x) {
    return NORMAL.getPDF((x - F) / NORMAL_VOL / ROOT_T) / NORMAL_VOL / ROOT_T;
  }

  private double normalCDF(final double x) {
    return NORMAL.getCDF((x - F) / NORMAL_VOL / ROOT_T);
  }

  private double logNormalPDF(final double x) {
    if (x <= 0) {
      return 0.0;
    }
    final double d1 = (Math.log(x / F) + VOL * VOL * T / 2) / VOL / ROOT_T;
    return Math.exp(-d1 * d1 / 2) / ROOT_2_PI / VOL / ROOT_T / x;
  }

  private double logNormalCDF(final double x) {
    if (x <= 0) {
      return 0.0;
    }
    final double d2 = (Math.log(F / x) - VOL * VOL * T / 2) / VOL / ROOT_T;
    return NORMAL.getCDF(-d2);
  }

}
