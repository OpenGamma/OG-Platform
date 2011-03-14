/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finiteDifference;

import static org.junit.Assert.assertEquals;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;
import org.junit.Test;

import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.CEVFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.CEVPriceFunction;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.volatility.BlackImpliedVolatilityFormula;
import com.opengamma.math.curve.ConstantDoublesCurve;
import com.opengamma.math.function.Function;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.surface.ConstantDoublesSurface;
import com.opengamma.math.surface.FunctionalDoublesSurface;
import com.opengamma.math.surface.Surface;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

/**
 * 
 */
public class ExplicitFiniteDifferenceTest {

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
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2010, 7, 1);
  private static final OptionDefinition OPTION;
  private static final ConvectionDiffusionPDEDataBundle DATA;
  private static final ConvectionDiffusionPDEDataBundle LN_DATA;
  private static final ConvectionDiffusionPDEDataBundle CEV_DATA;
  private static Surface<Double, Double, Double> A;
  private static Surface<Double, Double, Double> B;
  private static Surface<Double, Double, Double> LN_A;
  private static Surface<Double, Double, Double> LN_B;
  private static Surface<Double, Double, Double> BETA_A;
  private static Surface<Double, Double, Double> C;
  private static Surface<Double, Double, Double> ZERO_SURFACE = ConstantDoublesSurface.from(0.0);

  static {

    FORWARD = SPOT / YIELD_CURVE.getDiscountFactor(T);
    VOL_BETA = ATM_VOL * Math.pow(FORWARD, 1 - BETA);
    OPTION = new EuropeanVanillaOptionDefinition(FORWARD, new Expiry(DateUtil.getDateOffsetWithYearFraction(DATE, T)), true);

    LOWER = new FixedValueBoundaryCondition(0.0, 0.0);
    UPPER = new FixedSecondDerivativeBoundaryCondition(0.0, 5.0 * FORWARD);

    LN_LOWER = new FixedValueBoundaryCondition(0.0, Math.log(FORWARD / 100.0));
    LN_UPPER = new FixedSecondDerivativeBoundaryCondition(0 * FORWARD, Math.log(50 * FORWARD));

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

    DATA = new ConvectionDiffusionPDEDataBundle(A, B, C, payoff);
    LN_DATA = new ConvectionDiffusionPDEDataBundle(LN_A, LN_B, C, lnPayoff);
    CEV_DATA = new ConvectionDiffusionPDEDataBundle(BETA_A, ZERO_SURFACE, C, payoff);
  }

  /**
   * Need a huge number of time steps to get good accuracy 
   */
  @Test
  public void testBlackScholesEquation() {
    double df = YIELD_CURVE.getDiscountFactor(T);
    int timeSteps = 50000;
    int priceSteps = 400;
    double lowerBound = 0.0;
    double upperBound = 5 * FORWARD;
    ExplicitFiniteDifference solver = new ExplicitFiniteDifference();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(FORWARD, T, true);

    double[] res = solver.solve(DATA, timeSteps, priceSteps, T, LOWER, UPPER, null);
    int n = res.length;
    for (int i = 20; i < n - 100; i++) {
      double spot = lowerBound + i * (upperBound - lowerBound) / priceSteps;
      BlackFunctionData data = new BlackFunctionData(spot / df, df, 0.0);
      double impVol;
      try {
        impVol = BLACK_IMPLIED_VOL.getImpliedVolatility(data, option, res[i]);
      } catch (Exception e) {
        impVol = 0.0;
      }
      // System.out.println(spot + "\t" + res[i] + "\t" + impVol);
      assertEquals(ATM_VOL, impVol, 1e-3);
    }
  }

  @Test
  public void testLogTransformedBlackScholesEquation() {
    double df = YIELD_CURVE.getDiscountFactor(T);
    int timeSteps = 50000;
    int priceSteps = 400;
    double lowerBound = Math.log(FORWARD / 100.0);
    double upperBound = Math.log(50 * FORWARD);
    ExplicitFiniteDifference solver = new ExplicitFiniteDifference();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(FORWARD, T, true);

    double[] res = solver.solve(LN_DATA, timeSteps, priceSteps, T, LN_LOWER, LN_UPPER, null);
    int n = res.length;
    for (int i = 150; i < n - 150; i++) {
      double spot = Math.exp(lowerBound + i * (upperBound - lowerBound) / priceSteps);
      BlackFunctionData data = new BlackFunctionData(spot / df, df, 0.0);
      double impVol;
      try {
        impVol = BLACK_IMPLIED_VOL.getImpliedVolatility(data, option, res[i]);
      } catch (Exception e) {
        impVol = 0.0;
      }
      // System.out.println(i + "\t" + spot + "\t" + res[i] + "\t" + impVol);
      assertEquals(ATM_VOL, impVol, 1e-3);
    }
  }

  @Test
  public void testCEV() {
    double df = YIELD_CURVE.getDiscountFactor(T);
    int timeSteps = 50000;
    int priceSteps = 400;
    double lowerBound = 0.0;
    double upperBound = 5 * FORWARD;
    final EuropeanVanillaOption option = new EuropeanVanillaOption(FORWARD, T, true);
    double modSigma = VOL_BETA * Math.pow(df, BETA - 1);

    ExplicitFiniteDifference solver = new ExplicitFiniteDifference();

    double[] res = solver.solve(CEV_DATA, timeSteps, priceSteps, T, LOWER, UPPER, null);
    int n = res.length;
    for (int i = 50; i < n - 150; i++) {
      double f = lowerBound + i * (upperBound - lowerBound) / priceSteps;
      BlackFunctionData data = new BlackFunctionData(f, df, 0.0);
      double impVol;
      try {
        impVol = BLACK_IMPLIED_VOL.getImpliedVolatility(data, option, res[i]);
      } catch (Exception e) {
        impVol = 0.0;
      }

      final CEVFunctionData cevData = new CEVFunctionData(f, df, VOL_BETA, BETA);
      final double cevPrice = CEV.getPriceFunction(option).evaluate(cevData);
      final double cevVol = BLACK_IMPLIED_VOL.getImpliedVolatility(cevData, option, cevPrice);

   //   System.out.println(i + "\t" + f + "\t" + res[i] + "\t" + cevVol + "\t" + impVol);
       assertEquals(cevVol, impVol, 1e-3);
    }
  }

}
