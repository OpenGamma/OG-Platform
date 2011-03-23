/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finiteDifference;

import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;
import org.apache.commons.lang.Validate;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.CEVFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.CEVPriceFunction;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.volatility.BlackImpliedVolatilityFormula;
import com.opengamma.math.curve.ConstantDoublesCurve;
import com.opengamma.math.function.Function;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.surface.FunctionalDoublesSurface;
import com.opengamma.math.surface.Surface;

/**
 * 
 */
public class CrankNicolsonFiniteDifferenceTest {

  private static final BlackImpliedVolatilityFormula BLACK_IMPLIED_VOL = new BlackImpliedVolatilityFormula();
  private static final CEVPriceFunction CEV = new CEVPriceFunction();

  private static BoundaryCondition LOWER;
  private static BoundaryCondition UPPER;

  private static BoundaryCondition LN_LOWER;
  private static BoundaryCondition LN_UPPER;

  private static final double SPOT = 100;
  private static final double FORWARD;
  private static final double BETA = 0.4;
  private static final double T = 5.0;
  private static final double RATE = 0.05;
  private static final YieldAndDiscountCurve YIELD_CURVE = new YieldCurve(ConstantDoublesCurve.from(RATE));
  private static final double ATM_VOL = 0.20;
  private static final double VOL_BETA;
  private static final EuropeanVanillaOption OPTION;
  private static final ConvectionDiffusionPDEDataBundle DATA;
  private static final ConvectionDiffusionPDEDataBundle LN_DATA;
  private static final ConvectionDiffusionPDEDataBundle BETA_DATA;
  private static Surface<Double, Double, Double> A;
  private static Surface<Double, Double, Double> B;
  private static Surface<Double, Double, Double> LN_A;
  private static Surface<Double, Double, Double> LN_B;
  private static Surface<Double, Double, Double> BETA_A;
  private static Surface<Double, Double, Double> C;
  private static Surface<Double, Double, Double> ZERO_SURFACE;

  static {

    FORWARD = SPOT / YIELD_CURVE.getDiscountFactor(T);
    OPTION = new EuropeanVanillaOption(FORWARD, T, true);
    VOL_BETA = ATM_VOL * Math.pow(FORWARD, 1 - BETA);

    LOWER = new DirichletBoundaryCondition(0.0, 0.0);
    UPPER = new FixedSecondDerivativeBoundaryCondition(0.0, 5.0 * SPOT);

    LN_LOWER = new DirichletBoundaryCondition(0.0, Math.log(SPOT / 100.0));
    LN_UPPER = new FixedSecondDerivativeBoundaryCondition(100 * SPOT, Math.log(100 * SPOT));

    final Function<Double, Double> a = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        double s = ts[1];
        return -s * s * ATM_VOL * ATM_VOL / 2;
      }
    };

    final Function<Double, Double> b = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        double s = ts[1];
        return -s * RATE;
      }
    };

    final Function<Double, Double> ln_a = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        return -ATM_VOL * ATM_VOL / 2;
      }
    };

    final Function<Double, Double> ln_b = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        return ATM_VOL * ATM_VOL / 2 - RATE;
      }
    };

    final Function<Double, Double> beta_a = new Function<Double, Double>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        double s = ts[1];
        return -VOL_BETA * VOL_BETA * Math.pow(s, 2 * BETA) / 2;
      }
    };

    final Function<Double, Double> c = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        return RATE;
      }
    };

    final Function<Double, Double> zero = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        return 0.0;
      }
    };

    final Function1D<Double, Double> payoff = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(Double x) {
        return Math.max(0, x - FORWARD);
      }
    };

    final Function1D<Double, Double> lnPayoff = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(Double x) {
        double s = Math.exp(x);
        return Math.max(0, s - FORWARD);
      }
    };

    A = FunctionalDoublesSurface.from(a);
    B = FunctionalDoublesSurface.from(b);
    LN_A = FunctionalDoublesSurface.from(ln_a);
    BETA_A = FunctionalDoublesSurface.from(beta_a);
    LN_B = FunctionalDoublesSurface.from(ln_b);

    C = FunctionalDoublesSurface.from(c);

    ZERO_SURFACE = FunctionalDoublesSurface.from(zero);

    DATA = new ConvectionDiffusionPDEDataBundle(A, B, C, payoff);
    LN_DATA = new ConvectionDiffusionPDEDataBundle(LN_A, LN_B, C, lnPayoff);
    BETA_DATA = new ConvectionDiffusionPDEDataBundle(BETA_A, ZERO_SURFACE, ZERO_SURFACE, payoff);
  }

  @Test
  public void testBlackScholesEquation() {
    double df = YIELD_CURVE.getDiscountFactor(T);
    int timeSteps = 10;
    int priceSteps = 100;

    CrankNicolsonFiniteDifference solver = new CrankNicolsonFiniteDifference();
    double[][] res = solver.solve(DATA, timeSteps, priceSteps, T, LOWER, UPPER);
    int n = res[0].length;
    for (int i = 10; i < n - 20; i++) {
      double spot = res[0][i];
      BlackFunctionData data = new BlackFunctionData(spot / df, df, 0.0);
      double impVol;
      try {
        impVol = BLACK_IMPLIED_VOL.getImpliedVolatility(data, OPTION, res[1][i]);
      } catch (Exception e) {
        impVol = 0.0;
      }
      // System.out.println(spot + "\t" + res[1][i] + "\t" + impVol);
      assertEquals(ATM_VOL, impVol, 1e-3);
    }
  }

  /**
   * This needs more price steps for the same accuracy
   */
  @Test
  public void testLogTransformedBlackScholesEquation() {
    double df = YIELD_CURVE.getDiscountFactor(T);
    int timeSteps = 10;
    int priceSteps = 200;

    CrankNicolsonFiniteDifference solver = new CrankNicolsonFiniteDifference();

    double[][] res = solver.solve(LN_DATA, timeSteps, priceSteps, T, LN_LOWER, LN_UPPER);
    int n = res[0].length;
    for (int i = 81; i < n - 81; i++) {
      double spot = Math.exp(res[0][i]);
      BlackFunctionData data = new BlackFunctionData(spot / df, df, 0.0);
      double impVol;
      try {
        impVol = BLACK_IMPLIED_VOL.getImpliedVolatility(data, OPTION, res[1][i]);
      } catch (Exception e) {
        impVol = 0.0;
      }
      // System.out.println(i + "\t" + spot + "\t" + res[1][i] + "\t" + impVol);
      assertEquals(ATM_VOL, impVol, 1e-3);
    }
  }

  /**
   * In this test we are pricing the non-discounted option on a grid of forward values, hence the returned option values must be multiplied by the discount factor
   * to give the turn option value
   */
  @Test
  public void testCEV() {
    double df = YIELD_CURVE.getDiscountFactor(T);
    int timeSteps = 25;
    int priceSteps = 100;

    // double modSigma = VOL_BETA * Math.pow(df, BETA - 1);

    CrankNicolsonFiniteDifference solver = new CrankNicolsonFiniteDifference();
    double[][] res = solver.solve(BETA_DATA, timeSteps, priceSteps, T, LOWER, UPPER);
    int n = res[0].length;
    for (int i = 10; i < n - 30; i++) {
      double f = res[0][i];
      BlackFunctionData data = new BlackFunctionData(f, df, 0.0);
      double impVol;
      try {
        impVol = BLACK_IMPLIED_VOL.getImpliedVolatility(data, OPTION, res[1][i] * df);
      } catch (Exception e) {
        impVol = 0.0;
      }

      final CEVFunctionData cevData = new CEVFunctionData(f, df, VOL_BETA, BETA);
      final double cevPrice = CEV.getPriceFunction(OPTION).evaluate(cevData);
      final double cevVol = BLACK_IMPLIED_VOL.getImpliedVolatility(data, OPTION, cevPrice);

      // System.out.println(i + "\t" + f + "\t" + OPTION.getStrike() + "\t" + cevData.getSimga() + "\t" + cevPrice + "\t" + res[1][i] + "\t" + cevVol + "\t" + impVol);
      assertEquals(cevVol, impVol, 1e-3);
    }
  }

}
