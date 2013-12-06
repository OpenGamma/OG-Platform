/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import static org.testng.AssertJUnit.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.analytics.financial.greeks.GreekResultCollection;
import com.opengamma.analytics.financial.model.finitedifference.applications.InitialConditionsProvider;
import com.opengamma.analytics.financial.model.finitedifference.applications.PDE1DCoefficientsProvider;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.option.definition.AmericanVanillaOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.financial.model.option.pricing.analytic.AnalyticOptionModel;
import com.opengamma.analytics.financial.model.option.pricing.analytic.BjerksundStenslandModel;
import com.opengamma.analytics.financial.model.option.pricing.analytic.BlackScholesMertonModel;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.CEVFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.CEVPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.BlackImpliedVolatilityFormula;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ConvectionDiffusionPDESolverTestCase {

  //private static final PDEDataBundleProvider PDE_DATA_PROVIDER = new PDEDataBundleProvider();
  private static final PDE1DCoefficientsProvider PDE_PROVIDER = new PDE1DCoefficientsProvider();
  private static final InitialConditionsProvider INITIAL_CONDITION_PROVIDER = new InitialConditionsProvider();

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
  private static final double RATE = 0.05;
  private static final YieldAndDiscountCurve YIELD_CURVE = YieldCurve.from(ConstantDoublesCurve.from(RATE));
  private static final double ATM_VOL = 0.20;
  private static final double VOL_BETA;
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2010, 7, 1);
  private static final EuropeanVanillaOption OPTION;
  private static final ConvectionDiffusionPDE1DStandardCoefficients DATA;
  private static final ConvectionDiffusionPDE1DStandardCoefficients LN_DATA;
  private static final ConvectionDiffusionPDE1DStandardCoefficients CEV_DATA;

  private static VolatilitySurface VOL_SURFACE;
  private static final EuropeanVanillaOptionDefinition OPTION_DEFINITION;
  private static Set<Greek> GREEKS;
  private static Function1D<StandardOptionDataBundle, Double> BS_PRICE;

  private static boolean ISCALL = false;
  private static Function1D<Double, Double> PAYOFF;
  private static Function1D<Double, Double> PAYOFF_LOG_COOR;
  private static Surface<Double, Double, Double> EARLY_EXCISE;

  static {
    GREEKS = new HashSet<>();
    GREEKS.add(Greek.DELTA);
    GREEKS.add(Greek.GAMMA);

    VOL_SURFACE = new VolatilitySurface(ConstantDoublesSurface.from(ATM_VOL));

    FORWARD = SPOT / YIELD_CURVE.getDiscountFactor(T);
    STRIKE = FORWARD;

    final int secondsInAYear = (int) (365.25 * 24 * 60 * 60);
    OPTION_DEFINITION = new EuropeanVanillaOptionDefinition(STRIKE, new Expiry(DATE.plusSeconds((int) (secondsInAYear * T))), ISCALL);
    OPTION = new EuropeanVanillaOption(STRIKE, T, ISCALL);
    BS_PRICE = BS_MODEL.getPricingFunction(OPTION_DEFINITION);

    VOL_BETA = ATM_VOL * Math.pow(FORWARD, 1 - BETA);

    final Function1D<Double, Double> spotZeroPrice = new Function1D<Double, Double>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double tau) {
        if (ISCALL) {
          return 0.0;
        }
        return STRIKE * Math.exp(-RATE * tau);
      }
    };

    @SuppressWarnings("unused")
    final Function1D<Double, Double> upper1stDev = new Function1D<Double, Double>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double t) {
        if (ISCALL) {
          return Math.exp(-RATE * t);
        }
        return 0.0;
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

    final Function1D<Double, Double> logSpotZeroPrice = new Function1D<Double, Double>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double t) {
        if (ISCALL) {
          return 0.0;
        }
        return STRIKE * Math.exp(-RATE * t) - Math.exp(logGridLow);
      }
    };

    LN_LOWER = new DirichletBoundaryCondition(logSpotZeroPrice, logGridLow);
    LN_UPPER = new DirichletBoundaryCondition(0.0, logGridHi); // put only

    //    final Function<Double, Double> payoff = new Function<Double, Double>() {
    //
    //      @SuppressWarnings("synthetic-access")
    //      @Override
    //      public Double evaluate(final Double... ts) {
    //        final double s = ts[1];
    //        if (ISCALL) {
    //          return Math.max(0, s - STRIKE);
    //        }
    //        return Math.max(0, STRIKE - s);
    //      }
    //
    //    };

    PAYOFF = INITIAL_CONDITION_PROVIDER.getEuropeanPayoff(STRIKE, ISCALL);
    PAYOFF_LOG_COOR = INITIAL_CONDITION_PROVIDER.getLogEuropeanPayoff(STRIKE, ISCALL);
    EARLY_EXCISE = INITIAL_CONDITION_PROVIDER.getAmericanEarlyExcise(STRIKE, ISCALL);

    DATA = PDE_PROVIDER.getBlackScholes(RATE, 0.0, ATM_VOL);
    LN_DATA = PDE_PROVIDER.getLogBlackScholes(RATE, 0.0, ATM_VOL);
    CEV_DATA = PDE_PROVIDER.getCEV(RATE, BETA, VOL_BETA);
    //    DATA = PDE_DATA_PROVIDER.getBackwardsBlackScholes(ATM_VOL, RATE, STRIKE, ISCALL);
    //    LN_DATA = PDE_DATA_PROVIDER.getBackwardsLogBlackScholes(ATM_VOL, RATE, STRIKE, ISCALL);
    //    BETA_DATA = PDE_DATA_PROVIDER.getBackwardsCEV(VOL_BETA, RATE, STRIKE, BETA, ISCALL);
  }

  /**
   * Tests that the solver can solve Black-Scholes equation on a uniform grid
   */
  public void testBlackScholesEquationUniformGrid(final ConvectionDiffusionPDESolver solver, final int timeNodes, final int spotNodes, final double lowerMoneyness, final double upperMoneyness,
      final double volTol, final double priceTol, final double deltaTol, final double gammaTol, final boolean print) {
    final PDEGrid1D grid = new PDEGrid1D(timeNodes, spotNodes, T, LOWER.getLevel(), UPPER.getLevel());
    final PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients> data = new PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients>(DATA, PAYOFF, LOWER, UPPER, grid);
    final PDEResults1D res = solver.solve(data);
    testBlackScholesEquation(res, lowerMoneyness, upperMoneyness, volTol, priceTol, deltaTol, gammaTol, print);
  }

  public void testBlackScholesEquationNonuniformGrid(final ConvectionDiffusionPDESolver solver, final int timeSteps, final int spotSteps, final double lowerMoneyness, final double upperMoneyness,
      final double volTol, final double priceTol, final double deltaTol, final double gammaTol, final boolean print) {

    final MeshingFunction timeMesh = new ExponentialMeshing(0, T, timeSteps + 1, 0);
    final MeshingFunction spaceMesh = new HyperbolicMeshing(LOWER.getLevel(), UPPER.getLevel(), OPTION.getStrike(), spotSteps + 1, 0.1);
    // MeshingFunction spaceMesh = new ExponentalMeshing(LOWER.getLevel(), UPPER.getLevel(), spotSteps + 1, 0.0);

    final PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);
    final PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients> data = new PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients>(DATA, PAYOFF, LOWER, UPPER, grid);
    final PDEResults1D res = solver.solve(data);
    testBlackScholesEquation(res, lowerMoneyness, upperMoneyness, volTol, priceTol, deltaTol, gammaTol, print);
  }

  private void testBlackScholesEquation(final PDEResults1D res, final double lowerMoneyness, final double upperMoneyness, final double volTol, final double priceTol, final double deltaTol,
      final double gammaTol, final boolean print) {

    final double df = YIELD_CURVE.getDiscountFactor(T);
    final int n = res.getNumberSpaceNodes();
    for (int i = 0; i < n; i++) {

      final double spot = res.getSpaceValue(i);
      final double price = res.getFunctionValue(i);
      final double delta = res.getFirstSpatialDerivative(i);
      final double gamma = res.getSecondSpatialDerivative(i);
      final double moneyness = spot / OPTION.getStrike();

      final StandardOptionDataBundle standOptData = new StandardOptionDataBundle(YIELD_CURVE, RATE, VOL_SURFACE, spot, DATE);
      final GreekResultCollection greekResults = BS_MODEL.getGreeks(OPTION_DEFINITION, standOptData, GREEKS);

      final BlackFunctionData data = new BlackFunctionData(spot / df, df, ATM_VOL);

      double impVol;
      try {
        impVol = BLACK_IMPLIED_VOL.getImpliedVolatility(data, OPTION, price);
      } catch (final Exception e) {
        impVol = 0.0;
      }

      final double bs_price = BS_PRICE.evaluate(standOptData);
      final double bs_delta = greekResults.get(Greek.DELTA);
      final double bs_gamma = greekResults.get(Greek.GAMMA);

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
    final PDEGrid1D grid1 = new PDEGrid1D(timeSteps + 1, spotSteps + 1, T, LOWER.getLevel(), UPPER.getLevel());
    final PDEGrid1D grid2 = new PDEGrid1D(2 * timeSteps + 1, spotSteps + 1, T, LOWER.getLevel(), UPPER.getLevel());

    final PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients> data1 = new PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients>(DATA, PAYOFF, LOWER, UPPER, grid1);
    final PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients> data2 = data1.withGrid(grid2);
    final PDEResults1D res1 = solver.solve(data1);
    final PDEResults1D res2 = solver.solve(data2);

    final double df = YIELD_CURVE.getDiscountFactor(T);
    final int n = res1.getNumberSpaceNodes();

    for (int i = 0; i < n; i++) {
      final double spot = res1.getSpaceValue(i);
      final double price = 2.0 * res2.getFunctionValue(i) - res1.getFunctionValue(i);
      final double delta = 2.0 * res2.getFirstSpatialDerivative(i) - res1.getFirstSpatialDerivative(i);
      final double gamma = 2.0 * res2.getSecondSpatialDerivative(i) - res1.getSecondSpatialDerivative(i);
      final double moneyness = spot / OPTION.getStrike();

      final StandardOptionDataBundle standOptData = new StandardOptionDataBundle(YIELD_CURVE, RATE, VOL_SURFACE, spot, DATE);
      final GreekResultCollection greekResults = BS_MODEL.getGreeks(OPTION_DEFINITION, standOptData, GREEKS);

      final BlackFunctionData data = new BlackFunctionData(spot / df, df, ATM_VOL);

      double impVol;
      try {
        impVol = BLACK_IMPLIED_VOL.getImpliedVolatility(data, OPTION, price);
      } catch (final Exception e) {
        impVol = 0.0;
      }

      final double bs_price = BS_PRICE.evaluate(standOptData);
      final double bs_delta = greekResults.get(Greek.DELTA);
      final double bs_gamma = greekResults.get(Greek.GAMMA);

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
  public void testLogTransformedBlackScholesEquation(final ConvectionDiffusionPDESolver solver, final int timeNodes, final int spotNodes, final double lowerMoneyness, final double upperMoneyness,
      final double volTol, final double priceTol, final double deltaTol, final double gammaTol, final boolean print) {
    final PDEGrid1D grid = new PDEGrid1D(timeNodes + 1, spotNodes + 1, T, LN_LOWER.getLevel(), LN_UPPER.getLevel());
    final PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients> db =
        new PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients>(LN_DATA, PAYOFF_LOG_COOR, LN_LOWER, LN_UPPER, grid);
    final PDEResults1D res = solver.solve(db);
    final double df = YIELD_CURVE.getDiscountFactor(T);
    final int n = res.getNumberSpaceNodes();

    for (int i = 0; i < n; i++) {

      final double x = res.getSpaceValue(i);
      final double spot = Math.exp(x);
      final double price = res.getFunctionValue(i);
      final double delta = res.getFirstSpatialDerivative(i) / spot;
      final double gamma = (res.getSecondSpatialDerivative(i) / spot - delta) / spot;
      final double moneyness = spot / OPTION.getStrike();

      final StandardOptionDataBundle standOptData = new StandardOptionDataBundle(YIELD_CURVE, RATE, VOL_SURFACE, spot, DATE);
      final GreekResultCollection greekResults = BS_MODEL.getGreeks(OPTION_DEFINITION, standOptData, GREEKS);

      final BlackFunctionData data = new BlackFunctionData(spot / df, df, ATM_VOL);

      double impVol;
      try {
        impVol = BLACK_IMPLIED_VOL.getImpliedVolatility(data, OPTION, price);
      } catch (final Exception e) {
        impVol = 0.0;
      }

      final double bs_price = BS_PRICE.evaluate(standOptData);
      final double bs_delta = greekResults.get(Greek.DELTA);
      final double bs_gamma = greekResults.get(Greek.GAMMA);

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
  public void testCEV(final ConvectionDiffusionPDESolver solver, final int timeNodes, final int priceNodes, final double lowerMoneyness, final double upperMoneyness, final double volTol,
      final boolean print) {
    if (print) {
      System.out.println(this.getClass().toString() + " ConvectionDiffusionPDESolverTestCase.testCEV");
    }

    final PDEGrid1D grid = new PDEGrid1D(timeNodes + 1, priceNodes + 1, T, LOWER.getLevel(), UPPER.getLevel());
    final PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients> db = new PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients>(CEV_DATA, PAYOFF, LOWER, UPPER, grid);
    final PDEResults1D res = solver.solve(db);
    final double df = YIELD_CURVE.getDiscountFactor(T);
    final int n = res.getNumberSpaceNodes();
    for (int i = 0; i < n; i++) {
      final double fwd = res.getSpaceValue(i);
      final double price = res.getFunctionValue(i);
      final double moneyness = fwd / OPTION.getStrike();
      if (moneyness >= lowerMoneyness && moneyness <= upperMoneyness) {
        final BlackFunctionData data = new BlackFunctionData(fwd, df, 0.0);
        double impVol;
        try {
          impVol = BLACK_IMPLIED_VOL.getImpliedVolatility(data, OPTION, price);
        } catch (final Exception e) {
          impVol = 0.0;
        }

        final CEVFunctionData cevData = new CEVFunctionData(fwd, df, VOL_BETA, BETA);
        final double cevPrice = CEV.getPriceFunction(OPTION).evaluate(cevData);
        final double cevVol = BLACK_IMPLIED_VOL.getImpliedVolatility(data, OPTION, cevPrice);

        if (print) {
          System.out.println(fwd + "\t" + cevPrice + "\t" + price + "\t" + cevVol + "\t" + impVol);
        } else {
          assertEquals(cevVol, impVol, volTol * cevVol);
        }
      }
    }
  }

  public void testAmericanPrice(final ConvectionDiffusionPDESolver solver, final int timeSteps, final int priceSteps, final double lowerMoneyness, final double upperMoneyness, final boolean print) {

    final AmericanVanillaOptionDefinition option = new AmericanVanillaOptionDefinition(FORWARD, new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, T)), false);
    final BjerksundStenslandModel model = new BjerksundStenslandModel();
    final Function1D<StandardOptionDataBundle, Double> pFunc = model.getPricingFunction(option);

    final PDEGrid1D grid = new PDEGrid1D(timeSteps + 1, priceSteps + 1, T, LOWER.getLevel(), UPPER.getLevel());
    final PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients> db = new PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients>(DATA, PAYOFF, LOWER, UPPER, EARLY_EXCISE, grid);

    final PDEResults1D res = solver.solve(db);
    final int n = res.getNumberSpaceNodes();
    for (int i = 0; i < n; i++) {
      final double spot = res.getSpaceValue(i);
      final double price = res.getFunctionValue(i);
      final double delta = res.getFirstSpatialDerivative(i);
      final double gamma = res.getSecondSpatialDerivative(i);
      final double moneyness = spot / OPTION.getStrike();
      final StandardOptionDataBundle dataBundle = new StandardOptionDataBundle(YIELD_CURVE, RATE, VOL_SURFACE, spot, DATE);
      final Double anal_price = pFunc.evaluate(dataBundle);
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
