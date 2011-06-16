/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finitedifference;

import static org.testng.AssertJUnit.assertEquals;

import org.apache.commons.lang.Validate;
import org.testng.annotations.Test;

import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.volatility.BlackImpliedVolatilityFormula;
import com.opengamma.math.curve.ConstantDoublesCurve;
import com.opengamma.math.function.Function;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.surface.FunctionalDoublesSurface;

/**
 * Tests on a pair of backwards Black-Scholes PDEs. The model is a Black-Scholes SDE where the volatility can take one of two values
 * depending on the state of a hidden Markov chain. Degenerate (both vols the same) and uncoupled cases are tested along with a comparison
 * to Monte Carlo.  
 */
@SuppressWarnings("unused")
public class CoupledFiniteDifferenceTest {

  private static final BlackImpliedVolatilityFormula BLACK_IMPLIED_VOL = new BlackImpliedVolatilityFormula();
  private static BoundaryCondition LOWER;
  private static BoundaryCondition UPPER;

  private static final double SPOT = 100;
  private static final double FORWARD;
  private static final double STRIKE;
  private static final double T = 5.0;
  private static final double RATE = 0.0;//0.05;
  private static final YieldAndDiscountCurve YIELD_CURVE = new YieldCurve(ConstantDoublesCurve.from(RATE));
  private static final double VOL1 = 0.15;//0.2;
  private static final double VOL2 = 0.70;

  private static final EuropeanVanillaOption OPTION;

  static {

    FORWARD = SPOT / YIELD_CURVE.getDiscountFactor(T);
    STRIKE = FORWARD; // ATM option
    OPTION = new EuropeanVanillaOption(FORWARD, T, true); // true option

    Function1D<Double, Double> upper1stDev = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double t) {
        return Math.exp(-RATE * t);
      }
    };

    LOWER = new DirichletBoundaryCondition(0.0, 0.0);// call is worth 0 when stock falls to zero
    // UPPER = new DirichletBoundaryCondition(0 * FORWARD - STRIKE, 10.0 * FORWARD);
    // UPPER = new NeumannBoundaryCondition(upper1stDev, 5 * FORWARD, false);
    UPPER = new FixedSecondDerivativeBoundaryCondition(0.0, 10 * FORWARD, false);

  }

  private CoupledPDEDataBundle getCoupledPDEDataBundle(final double vol, final double rate, final double strike, final double lambda) {

    final Function<Double, Double> a = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        double s = ts[1];
        return -s * s * vol * vol / 2;
      }
    };

    final Function<Double, Double> b = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        double s = ts[1];
        return -s * rate;
      }
    };

    final Function<Double, Double> c = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        return rate + lambda;
      }
    };

    final Function1D<Double, Double> payoff = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(Double x) {
        return Math.max(0, x - STRIKE);
      }
    };

    return new CoupledPDEDataBundle(FunctionalDoublesSurface.from(a), FunctionalDoublesSurface.from(b), FunctionalDoublesSurface.from(c), -lambda, payoff);
  }

  @Test
  public void testNoCoupling() {
    CoupledFiniteDifference solver = new CoupledFiniteDifference();
    double lambda12 = 0.0;
    double lambda21 = 0.0;
    CoupledPDEDataBundle data1 = getCoupledPDEDataBundle(VOL1, RATE, STRIKE, lambda12);
    CoupledPDEDataBundle data2 = getCoupledPDEDataBundle(VOL2, RATE, STRIKE, lambda21);
    int timeNodes = 20;
    int spaceNodes = 150;
    double lowerMoneyness = 0.4;
    double upperMoneyness = 2.5;

    MeshingFunction timeMesh = new ExponentialMeshing(0, T, timeNodes, 0);
    // MeshingFunction spaceMesh = new HyperbolicMeshing(LOWER.getLevel(), UPPER.getLevel(), OPTION.getStrike(), 0.01, spaceNodes);
    MeshingFunction spaceMesh = new ExponentialMeshing(LOWER.getLevel(), UPPER.getLevel(), spaceNodes, 0.0);

    double[] timeGrid = new double[timeNodes];
    for (int n = 0; n < timeNodes; n++) {
      timeGrid[n] = timeMesh.evaluate(n);
    }

    double[] spaceGrid = new double[spaceNodes];
    for (int i = 0; i < spaceNodes; i++) {
      spaceGrid[i] = spaceMesh.evaluate(i);
    }

    PDEGrid1D grid = new PDEGrid1D(timeGrid, spaceGrid);

    PDEResults1D[] res = solver.solve(data1, data2, grid, LOWER, UPPER, LOWER, UPPER, null);
    double df = YIELD_CURVE.getDiscountFactor(T);
    int n = res[0].getNumberSpaceNodes();
    for (int i = 0; i < n; i++) {
      double spot = res[0].getSpaceValue(i);
      double price1 = res[0].getFunctionValue(i);
      double price2 = res[1].getFunctionValue(i);
      double moneyness = spot / OPTION.getStrike();

      BlackFunctionData data = new BlackFunctionData(spot / df, df, 0.0);
      double impVol1;
      try {
        impVol1 = BLACK_IMPLIED_VOL.getImpliedVolatility(data, OPTION, price1);
      } catch (Exception e) {
        impVol1 = 0.0;
      }
      double impVol2;
      try {
        impVol2 = BLACK_IMPLIED_VOL.getImpliedVolatility(data, OPTION, price2);
      } catch (Exception e) {
        impVol2 = 0.0;
      }
      // System.out.println(spot + "\t" + price1 + "\t" + price2 + "\t" + impVol1 + "\t" + impVol2);
      if (moneyness >= lowerMoneyness && moneyness <= upperMoneyness) {
        assertEquals(VOL1, impVol1, 2e-3);
        assertEquals(VOL2, impVol2, 1e-2);
      }
    }
  }

  @Test
  public void testDegenerate() {
    CoupledFiniteDifference solver = new CoupledFiniteDifference();
    double lambda12 = 0.2;
    double lambda21 = 0.5;
    CoupledPDEDataBundle data1 = getCoupledPDEDataBundle(VOL1, RATE, STRIKE, lambda12);
    CoupledPDEDataBundle data2 = getCoupledPDEDataBundle(VOL1, RATE, STRIKE, lambda21);
    int timeNodes = 10;
    int spaceNodes = 150;
    double lowerMoneyness = 0.4;
    double upperMoneyness = 2.5;

    MeshingFunction timeMesh = new ExponentialMeshing(0, T, timeNodes, 0);
    // MeshingFunction spaceMesh = new HyperbolicMeshing(LOWER.getLevel(), UPPER.getLevel(), OPTION.getStrike(), 0.01, spaceNodes);
    MeshingFunction spaceMesh = new ExponentialMeshing(LOWER.getLevel(), UPPER.getLevel(), spaceNodes, 0.0);

    double[] timeGrid = new double[timeNodes];
    for (int n = 0; n < timeNodes; n++) {
      timeGrid[n] = timeMesh.evaluate(n);
    }

    double[] spaceGrid = new double[spaceNodes];
    for (int i = 0; i < spaceNodes; i++) {
      spaceGrid[i] = spaceMesh.evaluate(i);
    }

    PDEGrid1D grid = new PDEGrid1D(timeGrid, spaceGrid);

    PDEResults1D[] res = solver.solve(data1, data2, grid, LOWER, UPPER, LOWER, UPPER, null);
    double df = YIELD_CURVE.getDiscountFactor(T);
    int n = res[0].getNumberSpaceNodes();
    for (int i = 0; i < n; i++) {
      double spot = res[0].getSpaceValue(i);
      double price1 = res[0].getFunctionValue(i);
      double price2 = res[1].getFunctionValue(i);
      double moneyness = spot / OPTION.getStrike();

      BlackFunctionData data = new BlackFunctionData(spot / df, df, 0.0);
      double impVol1;
      try {
        impVol1 = BLACK_IMPLIED_VOL.getImpliedVolatility(data, OPTION, price1);
      } catch (Exception e) {
        impVol1 = 0.0;
      }
      double impVol2;
      try {
        impVol2 = BLACK_IMPLIED_VOL.getImpliedVolatility(data, OPTION, price2);
      } catch (Exception e) {
        impVol2 = 0.0;
      }
      //  System.out.println(spot + "\t" + price1 + "\t" + price2 + "\t" + impVol1 + "\t" + impVol2);
      if (moneyness >= lowerMoneyness && moneyness <= upperMoneyness) {
        assertEquals(impVol1, impVol2, 1e-8);
        assertEquals(VOL1, impVol1, 2e-3);
      }
    }
  }

  @Test
  public void testMCSmile() {
    double lambda12 = 0.3;//0.2;
    double lambda21 = 4.0;//2.0;
    double df = YIELD_CURVE.getDiscountFactor(T);
    MarkovChain mc = new MarkovChain(VOL1, VOL2, lambda12, lambda21, 1.0);

    double[] mcSims = mc.simulate(T, 1000);
    for (int i = 0; i < 101; i++) {
      double strike = 0.003 + 0.18 * i / 100.0;
      BlackFunctionData data = new BlackFunctionData(0.03, 1.0, 0.0);
      EuropeanVanillaOption option = new EuropeanVanillaOption(strike, T, true);
      double price = mc.price(0.03, 1.0, strike, T, mcSims);
      double impVol1;
      try {
        impVol1 = BLACK_IMPLIED_VOL.getImpliedVolatility(data, option, price);
      } catch (Exception e) {
        impVol1 = 0.0;
      }
      System.out.println(strike + "\t" + price + "\t" + impVol1);
    }
  }

  @Test
  public void testSmile() {
    CoupledFiniteDifference solver = new CoupledFiniteDifference(0.5, false);
    double lambda12 = 0.2;
    double lambda21 = 2.0;
    CoupledPDEDataBundle data1 = getCoupledPDEDataBundle(VOL1, RATE, STRIKE, lambda12);
    CoupledPDEDataBundle data2 = getCoupledPDEDataBundle(VOL2, RATE, STRIKE, lambda21);
    int timeNodes = 20;
    int spaceNodes = 150;
    double lowerMoneyness = 0.0;
    double upperMoneyness = 3.0;

    MarkovChain mc = new MarkovChain(VOL1, VOL2, lambda12, lambda21, 1.0);
    double[] mcSims = mc.simulate(T, 10000); // simulate the vol path

    MeshingFunction timeMesh = new ExponentialMeshing(0, T, timeNodes, 0);
    // MeshingFunction spaceMesh = new HyperbolicMeshing(LOWER.getLevel(), UPPER.getLevel(), OPTION.getStrike(), 0.01, spaceNodes);
    MeshingFunction spaceMesh = new ExponentialMeshing(LOWER.getLevel(), UPPER.getLevel(), spaceNodes, 0.0);

    double[] timeGrid = new double[timeNodes];
    for (int n = 0; n < timeNodes; n++) {
      timeGrid[n] = timeMesh.evaluate(n);
    }

    double[] spaceGrid = new double[spaceNodes];
    for (int i = 0; i < spaceNodes; i++) {
      spaceGrid[i] = spaceMesh.evaluate(i);
    }

    PDEGrid1D grid = new PDEGrid1D(timeGrid, spaceGrid);

    PDEResults1D[] res = solver.solve(data1, data2, grid, LOWER, UPPER, LOWER, UPPER, null);
    double df = YIELD_CURVE.getDiscountFactor(T);
    int n = res[0].getNumberSpaceNodes();
    for (int i = 0; i < n; i++) {
      double spot = res[0].getSpaceValue(i);
      double price1 = res[0].getFunctionValue(i);
      double price2 = res[1].getFunctionValue(i);
      double delta = res[0].getFirstSpatialDerivative(i);
      double gamma = res[0].getSecondSpatialDerivative(i);
      double moneyness = spot / OPTION.getStrike();

      BlackFunctionData data = new BlackFunctionData(spot / df, df, 0.0);

      double mc_price = mc.price(spot / df, df, STRIKE, T, mcSims);

      double impVol1;
      try {
        impVol1 = BLACK_IMPLIED_VOL.getImpliedVolatility(data, OPTION, price1);
      } catch (Exception e) {
        impVol1 = 0.0;
      }
      double impVol2;
      try {
        impVol2 = BLACK_IMPLIED_VOL.getImpliedVolatility(data, OPTION, price2);
      } catch (Exception e) {
        impVol2 = 0.0;
      }
      double impVol_mc;
      try {
        impVol_mc = BLACK_IMPLIED_VOL.getImpliedVolatility(data, OPTION, mc_price);
      } catch (Exception e) {
        impVol_mc = 0.0;
      }
      // System.out.println(spot + "\t" + price1 + "\t" + price2 + "\t" + impVol1 + "\t" + impVol2 + "\t" + delta + "\t" + gamma);
      if (moneyness >= lowerMoneyness && moneyness <= upperMoneyness) {
        assertEquals(impVol_mc, impVol1, 1e-2);
        // assertEquals(VOL1, impVol2, 1e-3);
      }
    }
  }
}
