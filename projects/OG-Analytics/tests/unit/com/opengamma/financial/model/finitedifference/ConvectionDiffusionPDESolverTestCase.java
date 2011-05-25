/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finitedifference;

import static org.testng.AssertJUnit.assertEquals;

import java.util.HashSet;
import java.util.Set;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.greeks.GreekResultCollection;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.model.option.definition.AmericanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.option.pricing.analytic.AnalyticOptionModel;
import com.opengamma.financial.model.option.pricing.analytic.BjerksundStenslandModel;
import com.opengamma.financial.model.option.pricing.analytic.BlackScholesMertonModel;
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
  private static final AnalyticOptionModel<OptionDefinition, StandardOptionDataBundle> BS_MODEL = new BlackScholesMertonModel();
  private static final CEVPriceFunction CEV = new CEVPriceFunction();

  private static BoundaryCondition LOWER;
  private static BoundaryCondition UPPER;

  private static BoundaryCondition LN_LOWER;
  private static BoundaryCondition LN_UPPER;

  private static final double SPOT = 100;
  private static final double FORWARD;
  private static final double STRIKE;
  private static final double BETA = 0.4;
  private static final double T = 5.0;
  private static final double RATE = 0.05;// TODO change back to 5%
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
  @SuppressWarnings("unused")
  private static Surface<Double, Double, Double> ZERO_SURFACE;
  private static VolatilitySurface VOL_SURFACE;
  private static final EuropeanVanillaOptionDefinition OPTION_DEFINITION;
  private static Set<Greek> GREEKS;
  private static Function1D<StandardOptionDataBundle, Double> BS_PRICE;

  private static boolean ISCALL = false;
  private static Surface<Double, Double, Double> AMERICAN_PAYOFF;

  static {
    GREEKS = new HashSet<Greek>();
    GREEKS.add(Greek.DELTA);
    GREEKS.add(Greek.GAMMA);

    VOL_SURFACE = new VolatilitySurface(ConstantDoublesSurface.from(ATM_VOL));

    FORWARD = SPOT / YIELD_CURVE.getDiscountFactor(T);
    STRIKE = FORWARD;

    int secondsInAYear = (int) (365.25 * 24 * 60 * 60);
    OPTION_DEFINITION = new EuropeanVanillaOptionDefinition(STRIKE, new Expiry(DATE.plusSeconds((int) (secondsInAYear * T))), ISCALL);
    OPTION = new EuropeanVanillaOption(STRIKE, T, ISCALL);
    BS_PRICE = BS_MODEL.getPricingFunction(OPTION_DEFINITION);

    VOL_BETA = ATM_VOL * Math.pow(FORWARD, 1 - BETA);

    Function1D<Double, Double> spotZeroPrice = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double t) {
        if (ISCALL) {
          return 0.0;
        } else {
          return STRIKE * Math.exp(-RATE * t);
        }
      }
    };

    @SuppressWarnings("unused")
    Function1D<Double, Double> upper1stDev = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double t) {
        if (ISCALL) {
          return Math.exp(-RATE * t);
        } else {
          return 0.0;
        }
      }
    };

    LOWER = new DirichletBoundaryCondition(spotZeroPrice, 0.0);
    // UPPER = new NeumannBoundaryCondition(upper1stDev, 5 * FORWARD, ISCALL);
    if (ISCALL) {
      UPPER = new FixedSecondDerivativeBoundaryCondition(0.0, 5.0 * FORWARD, false);
    } else {
      UPPER = new NeumannBoundaryCondition(0.0, 5.0 * FORWARD, false);
    }

    final double logGridLow = Math.log(FORWARD / 10.0);
    final double logGridHi = Math.log(10 * FORWARD);

    Function1D<Double, Double> logSpotZeroPrice = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double t) {
        if (ISCALL) {
          return 0.0;
        } else {
          return STRIKE * Math.exp(-RATE * t) - Math.exp(logGridLow);
        }
      }
    };

    LN_LOWER = new DirichletBoundaryCondition(logSpotZeroPrice, logGridLow);
    LN_UPPER = new DirichletBoundaryCondition(0.0, logGridHi); // put only

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
        double t = ts[0];
        double s = ts[1];
        return -Math.exp(2. * RATE * (BETA - 1) * t) * VOL_BETA * VOL_BETA * Math.pow(s, 2 * BETA) / 2;
        // return -VOL_BETA * VOL_BETA * Math.pow(s, 2 * BETA) / 2;
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
        if (ISCALL) {
          return Math.max(0, x - STRIKE);
        } else {
          return Math.max(0, STRIKE - x);
        }
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
    BETA_DATA = new ConvectionDiffusionPDEDataBundle(BETA_A, B, C, payoff);
  }

  /**
   * Tests that the solver can solve Black-Scholes equation on a uniform grid
   */
  public void testBlackScholesEquationUniformGrid(final ConvectionDiffusionPDESolver solver, final int timeSteps, final int spotSteps, final double lowerMoneyness, final double upperMoneyness,
      final double volTol, final double priceTol, final double deltaTol, final double gammaTol, final boolean print) {
    PDEResults1D res = solver.solve(DATA, timeSteps, spotSteps, T, LOWER, UPPER);
    testBlackScholesEquation(res, lowerMoneyness, upperMoneyness, volTol, priceTol, deltaTol, gammaTol, print);
  }

  public void testBlackScholesEquationNonuniformGrid(final ConvectionDiffusionPDESolver solver, final int timeSteps, final int spotSteps, final double lowerMoneyness, final double upperMoneyness,
      final double volTol, final double priceTol, final double deltaTol, final double gammaTol, final boolean print) {

    MeshingFunction timeMesh = new ExponentalMeshing(0, T, timeSteps + 1, 0);
    MeshingFunction spaceMesh = new HyperbolicMeshing(LOWER.getLevel(), UPPER.getLevel(), OPTION.getStrike(), spotSteps + 1, 0.1);
    // MeshingFunction spaceMesh = new ExponentalMeshing(LOWER.getLevel(), UPPER.getLevel(), spotSteps + 1, 0.0);

    double[] timeGrid = new double[timeSteps + 1];
    for (int n = 0; n <= timeSteps; n++) {
      timeGrid[n] = timeMesh.evaluate(n);
    }

    double[] spaceGrid = new double[spotSteps + 1];
    for (int i = 0; i <= spotSteps; i++) {
      spaceGrid[i] = spaceMesh.evaluate(i);
    }

    PDEGrid1D grid = new PDEGrid1D(timeGrid, spaceGrid);

    PDEResults1D res = solver.solve(DATA, grid, LOWER, UPPER);
    testBlackScholesEquation(res, lowerMoneyness, upperMoneyness, volTol, priceTol, deltaTol, gammaTol, print);
  }

  private void testBlackScholesEquation(PDEResults1D res, final double lowerMoneyness, final double upperMoneyness, final double volTol, final double priceTol, final double deltaTol,
      final double gammaTol, final boolean print) {

    double df = YIELD_CURVE.getDiscountFactor(T);
    int n = res.getNumberSpaceNodes();
    for (int i = 0; i < n; i++) {

      double spot = res.getSpaceValue(i);
      double price = res.getFunctionValue(i);
      double delta = res.getFirstSpatialDerivative(i);
      double gamma = res.getSecondSpatialDerivative(i);
      double moneyness = spot / OPTION.getStrike();

      final StandardOptionDataBundle standOptData = new StandardOptionDataBundle(YIELD_CURVE, RATE, VOL_SURFACE, spot, DATE);
      GreekResultCollection greekResults = BS_MODEL.getGreeks(OPTION_DEFINITION, standOptData, GREEKS);

      BlackFunctionData data = new BlackFunctionData(spot / df, df, ATM_VOL);

      double impVol;
      try {
        impVol = BLACK_IMPLIED_VOL.getImpliedVolatility(data, OPTION, price);
      } catch (Exception e) {
        impVol = 0.0;
      }

      double bs_price = BS_PRICE.evaluate(standOptData);
      double bs_delta = greekResults.get(Greek.DELTA);
      double bs_gamma = greekResults.get(Greek.GAMMA);

      if (print) {
        System.out.println(spot + "\t" + impVol + "\t" + price + "\t" + bs_price + "\t" + delta + "\t" + bs_delta + '\t' + gamma + "\t" + bs_gamma);
      } else {
        if (moneyness >= lowerMoneyness && moneyness <= upperMoneyness) {
          assertEquals(ATM_VOL, impVol, volTol * ATM_VOL);
          assertEquals(bs_price, price, priceTol * (bs_price + 1e-8));
          assertEquals(bs_delta, delta, deltaTol * (Math.abs(bs_delta) + 1e-8));
          assertEquals(bs_gamma, gamma, gammaTol * (bs_gamma + 1e-8));
        }
      }
    }

  }

  // public void testSpaceExtrapolation(final ConvectionDiffusionPDESolver solver, final int timeSteps, final int spotSteps, final double lowerMoneyness, final double upperMoneyness) {
  // double[][] res1 = solver.solve(DATA, timeSteps, spotSteps, T, LOWER, UPPER);
  // double[][] res2 = solver.solve(DATA, timeSteps, 2 * spotSteps, T, LOWER, UPPER);
  //
  // double df = YIELD_CURVE.getDiscountFactor(T);
  // int n = res1[0].length;
  // double price;
  // for (int i = 0; i < n; i++) {
  // double spot = res1[0][i];
  // assertEquals(res1[0][i], res2[0][2 * i], 1e-9);
  // double moneyness = spot / OPTION.getStrike();
  // if (moneyness >= lowerMoneyness && moneyness <= upperMoneyness) {
  // BlackFunctionData data = new BlackFunctionData(spot / df, df, 0.0);
  // price = 2.0 * res2[1][2 * i] - res1[1][i];
  // double impVol;
  // try {
  // impVol = BLACK_IMPLIED_VOL.getImpliedVolatility(data, OPTION, price);
  // } catch (Exception e) {
  // impVol = 0.0;
  // }
  // // System.out.println(spot + "\t" + price + "\t" + impVol);
  // assertEquals(ATM_VOL, impVol, 1e-3);
  // }
  // }
  // }

  public void testTimeExtrapolation(final ConvectionDiffusionPDESolver solver, final int timeSteps, final int spotSteps, final double lowerMoneyness, final double upperMoneyness, final double volTol,
      final double priceTol, final double deltaTol, final double gammaTol, final boolean print) {
    PDEGrid1D grid1 = new PDEGrid1D(timeSteps + 1, spotSteps + 1, T, LOWER.getLevel(), UPPER.getLevel());
    PDEGrid1D grid2 = new PDEGrid1D(2 * timeSteps + 1, spotSteps + 1, T, LOWER.getLevel(), UPPER.getLevel());
    PDEResults1D res1 = solver.solve(DATA, grid1, LOWER, UPPER);
    PDEResults1D res2 = solver.solve(DATA, grid2, LOWER, UPPER);

    double df = YIELD_CURVE.getDiscountFactor(T);
    int n = res1.getNumberSpaceNodes();

    for (int i = 0; i < n; i++) {
      double spot = res1.getSpaceValue(i);
      double price = 2.0 * res2.getFunctionValue(i) - res1.getFunctionValue(i);
      double delta = 2.0 * res2.getFirstSpatialDerivative(i) - res1.getFirstSpatialDerivative(i);
      double gamma = 2.0 * res2.getSecondSpatialDerivative(i) - res1.getSecondSpatialDerivative(i);
      double moneyness = spot / OPTION.getStrike();

      final StandardOptionDataBundle standOptData = new StandardOptionDataBundle(YIELD_CURVE, RATE, VOL_SURFACE, spot, DATE);
      GreekResultCollection greekResults = BS_MODEL.getGreeks(OPTION_DEFINITION, standOptData, GREEKS);

      BlackFunctionData data = new BlackFunctionData(spot / df, df, ATM_VOL);

      double impVol;
      try {
        impVol = BLACK_IMPLIED_VOL.getImpliedVolatility(data, OPTION, price);
      } catch (Exception e) {
        impVol = 0.0;
      }

      double bs_price = BS_PRICE.evaluate(standOptData);
      double bs_delta = greekResults.get(Greek.DELTA);
      double bs_gamma = greekResults.get(Greek.GAMMA);

      if (print) {
        System.out.println(spot + "\t" + impVol + "\t" + price + "\t" + bs_price + "\t" + delta + "\t" + bs_delta + '\t' + gamma + "\t" + bs_gamma);
      } else {
        if (moneyness >= lowerMoneyness && moneyness <= upperMoneyness) {
          assertEquals(ATM_VOL, impVol, volTol * ATM_VOL);
          assertEquals(bs_price, price, priceTol * (bs_price + 1e-8));
          assertEquals(bs_delta, delta, deltaTol * (Math.abs(bs_delta) + 1e-8));
          assertEquals(bs_gamma, gamma, gammaTol * (bs_gamma + 1e-8));
        }
      }
    }
  }

  /**
   * Tests that the solver can solve the form of Black_scholes equation when the log of spot is the space variable 
   */
  public void testLogTransformedBlackScholesEquation(final ConvectionDiffusionPDESolver solver, final int timeSteps, final int spotSteps, final double lowerMoneyness, final double upperMoneyness,
      final double volTol, final double priceTol, final double deltaTol, final double gammaTol, final boolean print) {
    PDEResults1D res = solver.solve(LN_DATA, timeSteps, spotSteps, T, LN_LOWER, LN_UPPER);
    double df = YIELD_CURVE.getDiscountFactor(T);
    int n = res.getNumberSpaceNodes();

    for (int i = 0; i < n; i++) {

      double x = res.getSpaceValue(i);
      double spot = Math.exp(x);
      double price = res.getFunctionValue(i);
      double delta = res.getFirstSpatialDerivative(i) / spot;
      double gamma = (res.getSecondSpatialDerivative(i) / spot - delta) / spot;
      double moneyness = spot / OPTION.getStrike();

      final StandardOptionDataBundle standOptData = new StandardOptionDataBundle(YIELD_CURVE, RATE, VOL_SURFACE, spot, DATE);
      GreekResultCollection greekResults = BS_MODEL.getGreeks(OPTION_DEFINITION, standOptData, GREEKS);

      BlackFunctionData data = new BlackFunctionData(spot / df, df, ATM_VOL);

      double impVol;
      try {
        impVol = BLACK_IMPLIED_VOL.getImpliedVolatility(data, OPTION, price);
      } catch (Exception e) {
        impVol = 0.0;
      }

      double bs_price = BS_PRICE.evaluate(standOptData);
      double bs_delta = greekResults.get(Greek.DELTA);
      double bs_gamma = greekResults.get(Greek.GAMMA);

      if (print) {
        System.out.println(spot + "\t" + impVol + "\t" + price + "\t" + bs_price + "\t" + delta + "\t" + bs_delta + '\t' + gamma + "\t" + bs_gamma);
      } else {
        if (moneyness >= lowerMoneyness && moneyness <= upperMoneyness) {
          assertEquals(ATM_VOL, impVol, volTol * ATM_VOL);
          assertEquals(bs_price, price, priceTol * (bs_price + 1e-8));
          assertEquals(bs_delta, delta, deltaTol * (Math.abs(bs_delta) + 1e-8));
          assertEquals(bs_gamma, gamma, gammaTol * (bs_gamma + 1e-8));
        }
      }
    }

  }

  /**
   * In this test we are pricing the non-discounted option on a grid of forward values, hence the returned option values must be multiplied by the discount factor
   * to give the turn option value
   */
  public void testCEV(ConvectionDiffusionPDESolver solver, int timeSteps, int priceSteps, final double lowerMoneyness, final double upperMoneyness, final double volTol, final boolean print) {

    PDEResults1D res = solver.solve(BETA_DATA, timeSteps, priceSteps, T, LOWER, UPPER);
    double df = YIELD_CURVE.getDiscountFactor(T);
    int n = res.getNumberSpaceNodes();
    for (int i = 0; i < n; i++) {
      double spot = res.getSpaceValue(i);
      double price = res.getFunctionValue(i);// * df;
      double moneyness = spot / OPTION.getStrike();
      if (moneyness >= lowerMoneyness && moneyness <= upperMoneyness) {
        BlackFunctionData data = new BlackFunctionData(spot / df, df, 0.0);
        double impVol;
        try {
          impVol = BLACK_IMPLIED_VOL.getImpliedVolatility(data, OPTION, price);
        } catch (Exception e) {
          impVol = 0.0;
        }

        final CEVFunctionData cevData = new CEVFunctionData(spot / df, df, VOL_BETA, BETA);
        final double cevPrice = CEV.getPriceFunction(OPTION).evaluate(cevData);
        final double cevVol = BLACK_IMPLIED_VOL.getImpliedVolatility(data, OPTION, cevPrice);

        if (print) {
          System.out.println(spot + "\t" + cevPrice + "\t" + price + "\t" + cevVol + "\t" + impVol);
        } else {
          assertEquals(cevVol, impVol, volTol * cevVol);
        }
      }
    }
  }

  public void testAmericanPrice(ConvectionDiffusionPDESolver solver, int timeSteps, int priceSteps, final double lowerMoneyness, final double upperMoneyness, final double printTol, final boolean print) {

    AmericanVanillaOptionDefinition option = new AmericanVanillaOptionDefinition(FORWARD, new Expiry(DateUtil.getDateOffsetWithYearFraction(DATE, T)), false);
    AnalyticOptionModel<AmericanVanillaOptionDefinition, StandardOptionDataBundle> model = new BjerksundStenslandModel();
    Function1D<StandardOptionDataBundle, Double> pFunc = model.getPricingFunction(option);

    PDEResults1D res = solver.solve(DATA, timeSteps, priceSteps, T, LOWER, UPPER, AMERICAN_PAYOFF);
    int n = res.getNumberSpaceNodes();
    for (int i = 0; i < n; i++) {
      double spot = res.getSpaceValue(i);
      double price = res.getFunctionValue(i);
      double delta = res.getFirstSpatialDerivative(i);
      double gamma = res.getSecondSpatialDerivative(i);
      double moneyness = spot / OPTION.getStrike();
      StandardOptionDataBundle dataBundle = new StandardOptionDataBundle(YIELD_CURVE, RATE, VOL_SURFACE, spot, DATE);
      Double anal_price = pFunc.evaluate(dataBundle);
      if (print) {
        System.out.println(spot + "\t" + anal_price + "\t" + price + "\t" + delta + "\t" + gamma);
      } else {
        if (moneyness >= lowerMoneyness && moneyness <= upperMoneyness) {
          assertEquals(price, res.getFunctionValue(i), price * 1e-1);
        }
      }
    }
  }

}
