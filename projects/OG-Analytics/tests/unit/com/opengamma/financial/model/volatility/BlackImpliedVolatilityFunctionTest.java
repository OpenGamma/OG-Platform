/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;

/**
 * 
 */
public class BlackImpliedVolatilityFunctionTest {
  private static final double FORWARD = 134.5;
  private static final double DF = 0.87;
  private static final double T = 4.5;
  private static final double SIGMA = 0.2;
  private static final BlackFunctionData[] DATA;
  private static final EuropeanVanillaOption[] OPTIONS;
  private static final double[] PRICES;
  private static final double[] STRIKES;
  private static final BlackPriceFunction FORMULA = new BlackPriceFunction();
  private static final int N = 10;

  //  private static final double[] ERF_COF = new double[] {-1.3026537197817094, 6.4196979235649026e-1,
  //      1.9476473204185836e-2, -9.561514786808631e-3, -9.46595344482036e-4,
  //      3.66839497852761e-4, 4.2523324806907e-5, -2.0278578112534e-5,
  //      -1.624290004647e-6, 1.303655835580e-6, 1.5626441722e-8, -8.5238095915e-8,
  //      6.529054439e-9, 5.059343495e-9, -9.91364156e-10, -2.27365122e-10,
  //      9.6467911e-11, 2.394038e-12, -6.886027e-12, 8.94487e-13, 3.13092e-13,
  //      -1.12708e-13, 3.81e-16, 7.106e-15, -1.523e-15, -9.4e-17, 1.21e-16, -2.8e-17 };

  static {
    PRICES = new double[N];
    STRIKES = new double[N];
    DATA = new BlackFunctionData[N];
    OPTIONS = new EuropeanVanillaOption[N];
    for (int i = 0; i < 10; i++) {
      STRIKES[i] = 50 + 2 * i;
      DATA[i] = new BlackFunctionData(FORWARD, DF, SIGMA);
      OPTIONS[i] = new EuropeanVanillaOption(STRIKES[i], T, true);
      PRICES[i] = FORMULA.getPriceFunction(OPTIONS[i]).evaluate(DATA[i]);
    }
  }

  @Test
  public void test() {
    final BlackImpliedVolatilityFormula formula = new BlackImpliedVolatilityFormula();
    for (int i = 0; i < N; i++) {
      final double vol = formula.getImpliedVolatility(DATA[i], OPTIONS[i], PRICES[i]);
      assertEquals(SIGMA, vol, 1e-6);
    }
  }
  //
  //  @Test
  //  public void test2() {
  //    final double f = 0.03;
  //    final double k = 0.0636;
  //    final double t = 0.25;
  //    final double sigma = 0.2;
  //
  //    final BlackImpliedVolatilityFormula formula = new BlackImpliedVolatilityFormula();
  //    final EuropeanVanillaOption option = new EuropeanVanillaOption(k, t, true);
  //    final BlackFunctionData data = new BlackFunctionData(f, 1.0, sigma);
  //    final double price = FORMULA.getPriceFunction(option).evaluate(data);
  //    final double impVol = formula.getImpliedVolatility(data, option, price);
  //    assertEquals(sigma, impVol, 1e-4);
  //  }

  //TODO clean this up
  //TODO this doesn't test anything
  //  @Test(enabled = false)
  //  public void test3() {
  //    final NormalDistribution normal = new NormalDistribution(0, 1);
  //    final double f = 0.03;
  //    final double k = 0.0636;
  //    final double t = 0.25;
  //    //double sigma = 0.2;
  //
  //    final BlackImpliedVolatilityFormula formula = new BlackImpliedVolatilityFormula();
  //    final EuropeanVanillaOption option = new EuropeanVanillaOption(k, t, true);
  //
  //    for (int i = 0; i < 201; i++) {
  //      final double sigma = 0.19 + 0.025 * i / 200.0;
  //      final BlackFunctionData data = new BlackFunctionData(f, 1.0, sigma);
  //      final double d1 = (Math.log(f / k) + sigma * sigma * t / 2) / sigma / Math.sqrt(t);
  //      final double d2 = d1 - sigma * Math.sqrt(t);
  //      final double price = FORMULA.getPriceFunction(option).evaluate(data);
  //
  //      System.out.println(sigma + "\t" + price + "\t" + Math.log(price));
  //    }
  //  }
  //
  //  @Test(enabled = false)
  //  public void testNormalCDF() {
  //    final NormalDistribution normal = new NormalDistribution(0, 1);
  //    final double eps = 1e-3;
  //    for (int i = 0; i < 201; i++) {
  //      final double z = -8.0 + 1.0 * i / 200.0;
  //      final double cdf = normal.getCDF(z);
  //      final double div = (normal.getCDF(z + eps) - normal.getCDF(z - eps)) / 2 / eps;
  //      System.out.println(/*z + "\t" + */cdf);// + "\t" + Math.log(cdf) + "\t" + div);
  //
  //    }
  //  }

  //  private double normalCDF(final double x) {
  //    //  return 1 - 0.5 * erfcc(x / Math.sqrt(2));
  //    return 0.5 * (1 + erf(x / Math.sqrt(2)));
  //  }
  //
  //  double erf(double x) {
  //    if (x >= 0.) {
  //      return 1.0 - erfccheb(x);
  //    } else {
  //      return erfccheb(-x) - 1.0;
  //    }
  //  }
  //
  //  private double erfccheb(double z) {
  //    int j;
  //    int ncof = ERF_COF.length;
  //    double t, ty, tmp, d = 0., dd = 0.;
  //    Validate.isTrue(z >= 0.0);
  //    t = 2. / (2. + z);
  //    ty = 4. * t - 2.;
  //    for (j = ncof - 1; j > 0; j--) {
  //      tmp = d;
  //      d = ty * d - dd + ERF_COF[j];
  //      dd = tmp;
  //    }
  //    return t * Math.exp(-z * z + 0.5 * (ERF_COF[0] + ty * d) - dd);
  //  }
  //
  //  private double erfcc(final double x) {
  //    double t, z, ans;
  //
  //    z = Math.abs(x);
  //    t = 1.0 / (1.0 + 0.5 * z);
  //    ans = t * Math.exp(-z * z - 1.26551223 + t * (1.00002368 + t * (0.37409196 + t * (0.09678418 +
  //        t * (-0.18628806 + t * (0.27886807 + t * (-1.13520398 + t * (1.48851587 +
  //            t * (-0.82215223 + t * 0.17087277)))))))));
  //    return x >= 0.0 ? ans : 2.0 - ans;
  //  }

}
