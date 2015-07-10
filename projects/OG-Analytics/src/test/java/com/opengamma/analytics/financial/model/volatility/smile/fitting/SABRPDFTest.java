/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.option.DistributionFromImpliedVolatility;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRBerestyckiVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganAlternativeVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRJohnsonVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRPaulotVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class SABRPDFTest {
  private static final double F = 5;
  private static final double BETA = 0.99;
  private static final double NU = 0.4;
  private static final double RHO = -0.5;
  private static final double T = 25;
  private static final double ATM_VOL = 0.3;
  private static final double ALPHA = ATM_VOL * Math.pow(F, 1 - BETA);
  private static ProbabilityDistribution<Double> SABR_DIST;
  private static ProbabilityDistribution<Double> HAGAN_DIST;
  private static ProbabilityDistribution<Double> BERESTYCKI_DIST;
  private static ProbabilityDistribution<Double> PAULOT_DIST;
  private static ProbabilityDistribution<Double> JOHNSON_DIST;
  private static final SABRFormulaData DATA = new SABRFormulaData( ALPHA, BETA, RHO, NU);
  private final static Function1D<Double, Double> SABR = new MyFunction(new SABRHaganVolatilityFunction());
  private final static Function1D<Double, Double> SABR_HAGAN = new MyFunction(new SABRHaganAlternativeVolatilityFunction());
  private final static Function1D<Double, Double> SABR_BERESTYCKI = new MyFunction(new SABRBerestyckiVolatilityFunction());
  private final static Function1D<Double, Double> SABR_PAULOT = new MyFunction(new SABRPaulotVolatilityFunction());
  private final static Function1D<Double, Double> SABR_JOHNSON = new MyFunction(new SABRJohnsonVolatilityFunction());

  static {
    SABR_DIST = new DistributionFromImpliedVolatility(F, T, SABR);
    HAGAN_DIST = new DistributionFromImpliedVolatility(F, T, SABR_HAGAN);
    BERESTYCKI_DIST = new DistributionFromImpliedVolatility(F, T, SABR_BERESTYCKI);
    PAULOT_DIST = new DistributionFromImpliedVolatility(F, T, SABR_PAULOT);
    JOHNSON_DIST = new DistributionFromImpliedVolatility(F, T, SABR_JOHNSON);
  }

  @Test
  public void test() {
    final int n = 800;
    final double[] strike = new double[n];
    final double[] impliedVol = new double[n];
    final double[] impliedVol2 = new double[n];
    final double[] impliedVol3 = new double[n];
    final double[] impliedVol4 = new double[n];
    final double[] impliedVol5 = new double[n];
    final double[] pdf1 = new double[n];
    final double[] pdf2 = new double[n];
    final double[] pdf3 = new double[n];
    final double[] pdf4 = new double[n];
    final double[] pdf5 = new double[n];
    //final double sigmaRootT = ATM_VOL * Math.sqrt(T);
    //final double sigmaRootT = ALPHA * Math.sqrt(T);
    final double step = 20.0 / (n);
    //System.out.println("Strike \t SABR Vol \t Hagan Vol \t Berestycki vol \t Paulot vol \t Johnson vol \t SABR PDF \t Hagan PDF \t Berestycki PDF \t Paulot PDF \t Johnson");
    for (int i = 0; i < n; i++) {
      //double z = (i - 3 * n) * step;
      // double k = F * Math.exp(sigmaRootT * z) * 1.2;
      final double k = 0.0 + (i + 1) * step;
      strike[i] = k;
      impliedVol[i] = SABR.evaluate(k);
      impliedVol2[i] = SABR_HAGAN.evaluate(k);
      impliedVol3[i] = SABR_BERESTYCKI.evaluate(k);
      impliedVol4[i] = SABR_PAULOT.evaluate(k);
      impliedVol5[i] = SABR_JOHNSON.evaluate(k);
      //price[i] = BLACK.callPrice(F, k, 1.0, impliedVol[i], T);
      pdf1[i] = SABR_DIST.getPDF(k);
      pdf2[i] = HAGAN_DIST.getPDF(k);
      pdf3[i] = BERESTYCKI_DIST.getPDF(k);
      pdf4[i] = PAULOT_DIST.getPDF(k);
      pdf5[i] = JOHNSON_DIST.getPDF(k);
      //System.out.println(strike[i] + "\t" + impliedVol[i] + "\t" + impliedVol2[i] + "\t" + impliedVol3[i] + "\t" + impliedVol4[i] + "\t" + impliedVol5[i] + "\t" + pdf1[i] + "\t" + pdf2[i] + "\t"
      //    + pdf3[i] + "\t" + pdf4[i] + "\t" + pdf5[i]);
    }
  }

  private static class MyFunction extends Function1D<Double, Double> {
    private final VolatilityFunctionProvider<SABRFormulaData> _sabr;

    public MyFunction(final VolatilityFunctionProvider<SABRFormulaData> sabr) {
      _sabr = sabr;
    }

    @SuppressWarnings("synthetic-access")
    @Override
    public Double evaluate(final Double k) {
      final EuropeanVanillaOption option = new EuropeanVanillaOption(k, T, true);
      return _sabr.getVolatilityFunction(option,F).evaluate(DATA);
    }
  }
}
