/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.fd;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.fd.BoundaryCondition;
import com.opengamma.financial.model.fd.ConvectionDiffusionPDEDataBundle;
import com.opengamma.financial.model.fd.ConvectionDiffusionPDESolver;
import com.opengamma.financial.model.fd.DirichletBoundaryCondition;
import com.opengamma.financial.model.fd.FixedSecondDerivativeBoundaryCondition;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.model.option.definition.AmericanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.option.pricing.analytic.AnalyticOptionModel;
import com.opengamma.financial.model.option.pricing.analytic.BjerksundStenslandModel;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.CEVFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.CEVPriceFunction;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.volatility.BlackImpliedVolatilityFormula;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
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
public class ConvectionDiffusionPDESolverTestCase {

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
  private static VolatilitySurface VOL_SURFACE;

  private static Surface<Double, Double, Double> AMERICAN_PAYOFF;

  static {
    VOL_SURFACE = new VolatilitySurface(ConstantDoublesSurface.from(ATM_VOL));

    FORWARD = SPOT / YIELD_CURVE.getDiscountFactor(T);
    OPTION = new EuropeanVanillaOption(FORWARD, T, false);
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
        return Math.max(0, FORWARD - x);
      }
    };

    final Function1D<Double, Double> lnPayoff = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(Double x) {
        double s = Math.exp(x);
        return payoff.evaluate(s);
      }
    };

    final Function<Double, Double> americanPayoff = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        double s = ts[1];
        return payoff.evaluate(s);
      }
    };

    A = FunctionalDoublesSurface.from(a);
    B = FunctionalDoublesSurface.from(b);
    LN_A = FunctionalDoublesSurface.from(ln_a);
    BETA_A = FunctionalDoublesSurface.from(beta_a);
    LN_B = FunctionalDoublesSurface.from(ln_b);

    C = FunctionalDoublesSurface.from(c);

    AMERICAN_PAYOFF = FunctionalDoublesSurface.from(americanPayoff);

    ZERO_SURFACE = FunctionalDoublesSurface.from(zero);

    DATA = new ConvectionDiffusionPDEDataBundle(A, B, C, payoff);
    LN_DATA = new ConvectionDiffusionPDEDataBundle(LN_A, LN_B, C, lnPayoff);
    BETA_DATA = new ConvectionDiffusionPDEDataBundle(BETA_A, ZERO_SURFACE, ZERO_SURFACE, payoff);
  }

  public void testBlackScholesEquation(final ConvectionDiffusionPDESolver solver, final int timeSteps, final int spotSteps, final double lowerMoneyness, final double upperMoneyness) {
    double[][] res = solver.solve(DATA, timeSteps, spotSteps, T, LOWER, UPPER);
    double df = YIELD_CURVE.getDiscountFactor(T);
    int n = res[0].length;
    for (int i = 0; i < n; i++) {
      double spot = res[0][i];
      double moneyness = spot / OPTION.getStrike();
      if (moneyness >= lowerMoneyness && moneyness <= upperMoneyness) {
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
  }

  public void testLogTransformedBlackScholesEquation(ConvectionDiffusionPDESolver solver, int timeSteps, int spotSteps, final double lowerMoneyness, final double upperMoneyness) {
    double[][] res = solver.solve(LN_DATA, timeSteps, spotSteps, T, LN_LOWER, LN_UPPER);
    double df = YIELD_CURVE.getDiscountFactor(T);
    int n = res[0].length;
    for (int i = 0; i < n; i++) {
      double spot = Math.exp(res[0][i]);
      double moneyness = spot / OPTION.getStrike();
      if (moneyness >= lowerMoneyness && moneyness <= upperMoneyness) {
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
  }

  /**
   * In this test we are pricing the non-discounted option on a grid of forward values, hence the returned option values must be multiplied by the discount factor
   * to give the turn option value
   */
  public void testCEV(ConvectionDiffusionPDESolver solver, int timeSteps, int priceSteps, final double lowerMoneyness, final double upperMoneyness) {

    double[][] res = solver.solve(BETA_DATA, timeSteps, priceSteps, T, LOWER, UPPER);
    double df = YIELD_CURVE.getDiscountFactor(T);
    int n = res[0].length;
    for (int i = 1; i < n; i++) {
      double f = res[0][i];
      double moneyness = f / OPTION.getStrike();
      if (moneyness >= lowerMoneyness && moneyness <= upperMoneyness) {
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

        // System.out.println(i + "\t" + f + "\t" + OPTION.getStrike() + "\t" + cevData.getBlackVolatility() + "\t" + cevPrice + "\t" + res[1][i] + "\t" + cevVol + "\t" + impVol);
        assertEquals(cevVol, impVol, 1e-3);
      }
    }
  }

  public void testAmericanPrice(ConvectionDiffusionPDESolver solver, int timeSteps, int priceSteps, final double lowerMoneyness, final double upperMoneyness) {

    AmericanVanillaOptionDefinition option = new AmericanVanillaOptionDefinition(FORWARD, new Expiry(DateUtil.getDateOffsetWithYearFraction(DATE, T)), false);
    AnalyticOptionModel<AmericanVanillaOptionDefinition, StandardOptionDataBundle> model = new BjerksundStenslandModel();
    Function1D<StandardOptionDataBundle, Double> pFunc = model.getPricingFunction(option);

    double[][] res = solver.solve(DATA, timeSteps, priceSteps, T, LOWER, UPPER, AMERICAN_PAYOFF);
    int n = res[0].length;
    for (int i = 0; i < n; i++) {
      double spot = res[0][i];
      double moneyness = spot / OPTION.getStrike();
      if (moneyness >= lowerMoneyness && moneyness <= upperMoneyness) {
        StandardOptionDataBundle dataBundle = new StandardOptionDataBundle(YIELD_CURVE, RATE, VOL_SURFACE, spot, DATE);
        Double price = pFunc.evaluate(dataBundle);

        // System.out.println(spot + "\t" + res[1][i] + "\t" + price);
        assertEquals(price, res[1][i], price * 1e-1);
      }
    }
  }

}
