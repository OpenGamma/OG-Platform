/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.finitedifference.applications.CoupledPDEDataBundleProvider;
import com.opengamma.analytics.financial.model.finitedifference.applications.TwoStateMarkovChainDataBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.BlackImpliedVolatilityFormula;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.test.TestGroup;

/**
 * Tests on a pair of backwards Black-Scholes PDEs. The model is a Black-Scholes SDE where the volatility can take one of two values
 * depending on the state of a hidden Markov chain. Degenerate (both vols the same) and uncoupled cases are tested along with a comparison
 * to Monte Carlo.  
 */
@SuppressWarnings("unused")
@Test(groups = TestGroup.UNIT)
public class CoupledFiniteDifferenceTest {

  private static final CoupledPDEDataBundleProvider PDE_DATA_PROVIDER = new CoupledPDEDataBundleProvider();
  private static final BlackImpliedVolatilityFormula BLACK_IMPLIED_VOL = new BlackImpliedVolatilityFormula();
  private static BoundaryCondition LOWER;
  private static BoundaryCondition UPPER;
  private static Function1D<Double, Double> INITIAL_COND;

  private static final double SPOT = 100;
  private static final ForwardCurve FORWARD;
  private static final double STRIKE;
  private static final double T = 5.0;
  private static final double RATE = 0.0; //TODO this is not working properly with non-zero rate. Why?
  private static final YieldAndDiscountCurve YIELD_CURVE = YieldCurve.from(ConstantDoublesCurve.from(RATE));
  private static final double VOL1 = 0.15;//0.2;
  private static final double VOL2 = 0.70;

  private static final EuropeanVanillaOption OPTION;

  static {

    FORWARD = new ForwardCurve(SPOT, RATE);
    STRIKE = FORWARD.getForward(T); // ATM option
    OPTION = new EuropeanVanillaOption(FORWARD.getForward(T), T, true); // true option

    LOWER = new DirichletBoundaryCondition(0.0, 0.0);// call is worth 0 when stock falls to zero
    //UPPER = new DirichletBoundaryCondition(10 * SPOT - STRIKE, 10.0 * SPOT);
    UPPER = new NeumannBoundaryCondition(1.0, 10 * STRIKE, false);
    // UPPER = new FixedSecondDerivativeBoundaryCondition(0.0, 10 * FORWARD, false);
    INITIAL_COND = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(Double x) {
        return Math.max(0, x - STRIKE);
      }
    };
  }

  @Test
  public void testNoCoupling() {
    //making theta 0.55 (rather than 0.5 Crank-Nicolson) cuts down ATM oscillations 
    final CoupledFiniteDifference solver = new CoupledFiniteDifference(0.5, false);
    final double lambda12 = 0.0;
    final double lambda21 = 0.0;
    final double p0 = 0.5;

    TwoStateMarkovChainDataBundle chainData = new TwoStateMarkovChainDataBundle(VOL1, VOL2, lambda12, lambda21, p0);
    ConvectionDiffusionPDE1DCoupledCoefficients[] pdeData = PDE_DATA_PROVIDER.getCoupledBackwardsPair(FORWARD, T, chainData);
    final int timeNodes = 20;
    final int spaceNodes = 150;
    final double lowerMoneyness = 0.4;
    final double upperMoneyness = 2.5;

    final MeshingFunction timeMesh = new ExponentialMeshing(0, T, timeNodes, 5.0);
    final MeshingFunction spaceMesh = new HyperbolicMeshing(LOWER.getLevel(), UPPER.getLevel(), OPTION.getStrike(), spaceNodes, 0.1);
    // MeshingFunction spaceMesh = new ExponentialMeshing(LOWER.getLevel(), UPPER.getLevel(), spaceNodes, 0.0);

    final double[] timeGrid = new double[timeNodes];
    for (int n = 0; n < timeNodes; n++) {
      timeGrid[n] = timeMesh.evaluate(n);
    }

    final double[] spaceGrid = new double[spaceNodes];
    for (int i = 0; i < spaceNodes; i++) {
      spaceGrid[i] = spaceMesh.evaluate(i);
    }

    final PDEGrid1D grid = new PDEGrid1D(timeGrid, spaceGrid);
    CoupledPDEDataBundle d1 = new CoupledPDEDataBundle(pdeData[0], INITIAL_COND, LOWER, UPPER, grid);
    CoupledPDEDataBundle d2 = new CoupledPDEDataBundle(pdeData[1], INITIAL_COND, LOWER, UPPER, grid);

    final PDEResults1D[] res = solver.solve(d1, d2);
    final double df = YIELD_CURVE.getDiscountFactor(T);
    final int n = res[0].getNumberSpaceNodes();
    for (int i = 0; i < n; i++) {
      final double spot = res[0].getSpaceValue(i);
      final double price1 = res[0].getFunctionValue(i);
      final double price2 = res[1].getFunctionValue(i);
      final double moneyness = spot / OPTION.getStrike();

      final BlackFunctionData data = new BlackFunctionData(spot / df, df, 0.0);
      double impVol1;
      try {
        impVol1 = BLACK_IMPLIED_VOL.getImpliedVolatility(data, OPTION, price1);
      } catch (final Exception e) {
        impVol1 = 0.0;
      }
      double impVol2;
      try {
        impVol2 = BLACK_IMPLIED_VOL.getImpliedVolatility(data, OPTION, price2);
      } catch (final Exception e) {
        impVol2 = 0.0;
      }
      //  System.out.println(spot + "\t" + price1 + "\t" + price2 + "\t" + impVol1 + "\t" + impVol2);
      if (moneyness >= lowerMoneyness && moneyness <= upperMoneyness) {
        assertEquals(VOL1, impVol1, 1e-3);
        assertEquals(VOL2, impVol2, 5e-3);
      }
    }
  }

  @Test
  public void testDegenerate() {
    final CoupledFiniteDifference solver = new CoupledFiniteDifference();
    final double lambda12 = 0.2;
    final double lambda21 = 0.5;

    TwoStateMarkovChainDataBundle chainData = new TwoStateMarkovChainDataBundle(VOL1, VOL1, lambda12, lambda21, 0.5);
    ConvectionDiffusionPDE1DCoupledCoefficients[] pdeData = PDE_DATA_PROVIDER.getCoupledBackwardsPair(FORWARD, T, chainData);
    final int timeNodes = 20;
    final int spaceNodes = 150;
    final double lowerMoneyness = 0.4;
    final double upperMoneyness = 2.5;

    final MeshingFunction timeMesh = new ExponentialMeshing(0, T, timeNodes, 5.0);
    final MeshingFunction spaceMesh = new HyperbolicMeshing(LOWER.getLevel(), UPPER.getLevel(), OPTION.getStrike(), spaceNodes, 0.1);
    // MeshingFunction spaceMesh = new ExponentialMeshing(LOWER.getLevel(), UPPER.getLevel(), spaceNodes, 0.0);
    //
    final double[] timeGrid = new double[timeNodes];
    for (int n = 0; n < timeNodes; n++) {
      timeGrid[n] = timeMesh.evaluate(n);
    }

    final double[] spaceGrid = new double[spaceNodes];
    for (int i = 0; i < spaceNodes; i++) {
      spaceGrid[i] = spaceMesh.evaluate(i);
    }

    final PDEGrid1D grid = new PDEGrid1D(timeGrid, spaceGrid);

    final CoupledPDEDataBundle d1 = new CoupledPDEDataBundle(pdeData[0], INITIAL_COND, LOWER, UPPER, grid);
    final CoupledPDEDataBundle d2 = new CoupledPDEDataBundle(pdeData[1], INITIAL_COND, LOWER, UPPER, grid);

    final PDEResults1D[] res = solver.solve(d1, d2);
    final double df = YIELD_CURVE.getDiscountFactor(T);
    final int n = res[0].getNumberSpaceNodes();
    for (int i = 0; i < n; i++) {
      final double spot = res[0].getSpaceValue(i);
      final double price1 = res[0].getFunctionValue(i);
      final double price2 = res[1].getFunctionValue(i);
      final double moneyness = spot / OPTION.getStrike();

      final BlackFunctionData data = new BlackFunctionData(spot / df, df, 0.0);
      double impVol1;
      try {
        impVol1 = BLACK_IMPLIED_VOL.getImpliedVolatility(data, OPTION, price1);
      } catch (final Exception e) {
        impVol1 = 0.0;
      }
      double impVol2;
      try {
        impVol2 = BLACK_IMPLIED_VOL.getImpliedVolatility(data, OPTION, price2);
      } catch (final Exception e) {
        impVol2 = 0.0;
      }
      //  System.out.println(spot + "\t" + price1 + "\t" + price2 + "\t" + impVol1 + "\t" + impVol2);
      if (moneyness >= lowerMoneyness && moneyness <= upperMoneyness) {
        assertEquals(impVol1, impVol2, 1e-8);
        assertEquals(VOL1, impVol1, 2e-3);
      }
    }
  }

  /**
   * Test to look at the smile produced from the Monte Carlo simulation of the two state Markov chain model
   */
  @Test(enabled = false)
  public void testMCSmile() {
    final double lambda12 = 0.3;//0.2;
    final double lambda21 = 4.0;//2.0;
    final double df = YIELD_CURVE.getDiscountFactor(T);
    final MarkovChain mc = new MarkovChain(VOL1, VOL2, lambda12, lambda21, 1.0);

    final double[] mcSims = mc.simulate(T, 1000);
    for (int i = 0; i < 101; i++) {
      final double strike = 0.003 + 0.18 * i / 100.0;
      final BlackFunctionData data = new BlackFunctionData(0.03, 1.0, 0.0);
      final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, T, true);
      final double price = mc.price(0.03, 1.0, strike, T, mcSims);
      double impVol1;
      try {
        impVol1 = BLACK_IMPLIED_VOL.getImpliedVolatility(data, option, price);
      } catch (final Exception e) {
        impVol1 = 0.0;
      }
      System.out.println(strike + "\t" + price + "\t" + impVol1);
    }
  }

  @Test
  public void testSmile() {
    final CoupledFiniteDifference solver = new CoupledFiniteDifference(0.5, false);
    final double lambda12 = 0.2;
    final double lambda21 = 2.0;

    TwoStateMarkovChainDataBundle chainData = new TwoStateMarkovChainDataBundle(VOL1, VOL2, lambda12, lambda21, 1.0);
    ConvectionDiffusionPDE1DCoupledCoefficients[] pdeData = PDE_DATA_PROVIDER.getCoupledBackwardsPair(FORWARD, T, chainData);
    final int timeNodes = 20;
    final int spaceNodes = 150;
    final double lowerMoneyness = 0.3;
    final double upperMoneyness = 3.0;

    //state in state 1
    final MarkovChain mc = new MarkovChain(VOL1, VOL2, lambda12, lambda21, 1.0);
    final double[] mcSims = mc.simulate(T, 10000); // simulate the vol path

    final MeshingFunction timeMesh = new ExponentialMeshing(0, T, timeNodes, 0);
    // MeshingFunction spaceMesh = new HyperbolicMeshing(LOWER.getLevel(), UPPER.getLevel(), OPTION.getStrike(), 0.01, spaceNodes);
    final MeshingFunction spaceMesh = new ExponentialMeshing(LOWER.getLevel(), UPPER.getLevel(), spaceNodes, 0.0);

    final double[] timeGrid = new double[timeNodes];
    for (int n = 0; n < timeNodes; n++) {
      timeGrid[n] = timeMesh.evaluate(n);
    }

    final double[] spaceGrid = new double[spaceNodes];
    for (int i = 0; i < spaceNodes; i++) {
      spaceGrid[i] = spaceMesh.evaluate(i);
    }

    final PDEGrid1D grid = new PDEGrid1D(timeGrid, spaceGrid);
    final CoupledPDEDataBundle d1 = new CoupledPDEDataBundle(pdeData[0], INITIAL_COND, LOWER, UPPER, grid);
    final CoupledPDEDataBundle d2 = new CoupledPDEDataBundle(pdeData[1], INITIAL_COND, LOWER, UPPER, grid);

    final PDEResults1D[] res = solver.solve(d1, d2);
    final double df = YIELD_CURVE.getDiscountFactor(T);
    final int n = res[0].getNumberSpaceNodes();
    for (int i = 0; i < n; i++) {
      final double spot = res[0].getSpaceValue(i);
      final double price1 = res[0].getFunctionValue(i);
      final double price2 = res[1].getFunctionValue(i);
      final double delta = res[0].getFirstSpatialDerivative(i);
      final double gamma = res[0].getSecondSpatialDerivative(i);
      final double moneyness = spot / OPTION.getStrike();

      final BlackFunctionData data = new BlackFunctionData(spot / df, df, 0.0);

      final double mc_price = mc.price(spot / df, df, STRIKE, T, mcSims);

      double impVol1;
      try {
        impVol1 = BLACK_IMPLIED_VOL.getImpliedVolatility(data, OPTION, price1);
      } catch (final Exception e) {
        impVol1 = 0.0;
      }
      double impVol2;
      try {
        impVol2 = BLACK_IMPLIED_VOL.getImpliedVolatility(data, OPTION, price2);
      } catch (final Exception e) {
        impVol2 = 0.0;
      }
      double impVol_mc;
      try {
        impVol_mc = BLACK_IMPLIED_VOL.getImpliedVolatility(data, OPTION, mc_price);
      } catch (final Exception e) {
        impVol_mc = 0.0;
      }
      //   System.out.println(spot + "\t" + price1 + "\t" + price2 + "\t" + impVol1 + "\t" + impVol2 + "\t" + delta + "\t" + gamma);
      if (moneyness >= lowerMoneyness && moneyness <= upperMoneyness) {
        assertEquals(impVol_mc, impVol1, 1e-2);
      }
    }
  }
}
