/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finiteDifference;

import static org.junit.Assert.*;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;
import org.junit.Test;

import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.GeneralLogNormalOptionDataBundle;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.PDEOptionDataBundle;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackImpliedVolFormula;
import com.opengamma.financial.model.option.pricing.analytic.formula.CEVFormula;
import com.opengamma.financial.model.option.pricing.tree.BinomialTreeBuilder;
import com.opengamma.financial.model.option.pricing.tree.LogNormalBinomialTreeBuilder;
import com.opengamma.financial.model.volatility.surface.DriftSurface;
import com.opengamma.math.curve.ConstantDoublesCurve;
import com.opengamma.math.function.Function;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.surface.FunctionalDoublesSurface;
import com.opengamma.math.surface.Surface;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

/**
 * 
 */
public class ExplicitFiniteDifferenceTest {

  private static final double SPOT = 100;
  private static final double FORWARD;
  private static final double BETA = 0.4;
  private static final double T = 5.0;
  private static final double RATE = 0.0;
  private static final YieldAndDiscountCurve YIELD_CURVE = new YieldCurve(ConstantDoublesCurve.from(RATE));
  private static final double ATM_VOL = 0.20;
  private static final double VOL_BETA;
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2010, 7, 1);
  private static final OptionDefinition OPTION;
  private static final ConvectionDiffusionPDEDataBundle DATA;
  private static final ConvectionDiffusionPDEDataBundle LN_DATA;
  private static final ConvectionDiffusionPDEDataBundle BETA_DATA;
  private static Surface<Double, Double, Double> A;
  private static Surface<Double, Double, Double> B;
  private static Surface<Double, Double, Double> LN_A;
  private static Surface<Double, Double, Double> LN_B;
  private static Surface<Double, Double, Double> BETA_A;
  private static Surface<Double, Double, Double> C;

  static {

    FORWARD = SPOT / YIELD_CURVE.getDiscountFactor(T);
    VOL_BETA = ATM_VOL * Math.pow(FORWARD, 1 - BETA);
    OPTION = new EuropeanVanillaOptionDefinition(FORWARD, new Expiry(DateUtil.getDateOffsetWithYearFraction(DATE, T)), true);

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
    BETA_DATA = new ConvectionDiffusionPDEDataBundle(BETA_A, B, C, payoff);
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
    double[] res = solver.solve(DATA, timeSteps, priceSteps, T, lowerBound, upperBound);
    int n = res.length;
    for (int i = 20; i < n - 100; i++) {
      double spot = lowerBound + i * (upperBound - lowerBound) / priceSteps;
      double impVol;
      try {
        impVol = BlackImpliedVolFormula.impliedVol(res[i], spot / df, FORWARD, df, T, true);
      } catch (Exception e) {
        impVol = 0.0;
      }
      // System.out.println(spot+"\t"+res[i]+"\t"+impVol);
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
    double[] res = solver.solve(LN_DATA, timeSteps, priceSteps, T, lowerBound, upperBound);
    int n = res.length;
    for (int i = 150; i < n - 150; i++) {
      double spot = Math.exp(lowerBound + i * (upperBound - lowerBound) / priceSteps);
      double impVol;
      try {
        impVol = BlackImpliedVolFormula.impliedVol(res[i], spot / df, FORWARD, df, T, true);
      } catch (Exception e) {
        impVol = 0.0;
      }
      // System.out.println(i+"\t"+spot+"\t"+res[i]+"\t"+impVol);
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
    ExplicitFiniteDifference solver = new ExplicitFiniteDifference();
    double[] res = solver.solve(BETA_DATA, timeSteps, priceSteps, T, lowerBound, upperBound);
    int n = res.length;
    for (int i = 50; i < n - 50; i++) {
      double spot = lowerBound + i * (upperBound - lowerBound) / priceSteps;
      double impVol;
      try {
        impVol = BlackImpliedVolFormula.impliedVol(res[i], spot / df, FORWARD, df, T, true);
      } catch (Exception e) {
        impVol = 0.0;
      }
      double cevPrice = CEVFormula.optionPrice(spot / df, FORWARD, BETA, df, VOL_BETA, T, true);
      double cevVol = BlackImpliedVolFormula.impliedVol(cevPrice, spot / df, FORWARD, df, T, true);

      // System.out.println(i+"\t"+spot+"\t"+cevPrice+"\t"+cevVol+"\t"+impVol);
      assertEquals(cevVol, impVol, 1e-3);
    }
  }

}
