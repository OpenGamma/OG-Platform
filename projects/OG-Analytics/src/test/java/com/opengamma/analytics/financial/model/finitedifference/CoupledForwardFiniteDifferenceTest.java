/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;

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
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class CoupledForwardFiniteDifferenceTest {

  private static final CoupledPDEDataBundleProvider PDE_DATA_PROVIDER = new CoupledPDEDataBundleProvider();

  private static final BlackImpliedVolatilityFormula BLACK_IMPLIED_VOL = new BlackImpliedVolatilityFormula();
  private static BoundaryCondition LOWER1;
  private static BoundaryCondition LOWER2;
  private static BoundaryCondition UPPER;

  private static final double SPOT = 0.03;

  private static final double T = 5.0;
  private static final double RATE = 0.04;
  private static final ForwardCurve FORWARD = new ForwardCurve(SPOT, RATE);
  private static final YieldAndDiscountCurve YIELD_CURVE = YieldCurve.from(ConstantDoublesCurve.from(RATE));
  private static final double VOL1 = 0.15;
  private static final double VOL2 = 0.70;
  private static final double LAMBDA12 = 0.3;
  private static final double LAMBDA21 = 4.0;
  private static final double PROB_STATE1 = 1.0;
  private static final Function1D<Double, Double> INITIAL_COND1;
  private static final Function1D<Double, Double> INITIAL_COND2;
  private static final boolean ISCALL = true;

  static {

    final Function1D<Double, Double> strikeZeroPrice1 = new Function1D<Double, Double>() {
      @SuppressWarnings({"synthetic-access", "unused" })
      @Override
      public Double evaluate(final Double t) {
        if (ISCALL) {
          return probState1(LAMBDA12, LAMBDA21, PROB_STATE1, t) * SPOT;
        }
        return 0.0;
      }
    };

    final Function1D<Double, Double> strikeZeroPrice2 = new Function1D<Double, Double>() {
      @SuppressWarnings({"synthetic-access", "unused" })
      @Override
      public Double evaluate(final Double t) {
        if (ISCALL) {
          return (1 - probState1(LAMBDA12, LAMBDA21, PROB_STATE1, t)) * SPOT;
        }
        return 0.0;
      }
    };

    LOWER1 = new DirichletBoundaryCondition(strikeZeroPrice1, 0.0);
    LOWER2 = new DirichletBoundaryCondition(strikeZeroPrice2, 0.0);
    //LOWER = new FixedSecondDerivativeBoundaryCondition(0, 0, true);

    if (ISCALL) {
      //UPPER = new DirichletBoundaryCondition(0, 6.0 * SPOT * Math.exp(T * RATE));
      UPPER = new NeumannBoundaryCondition(0, 10.0 * SPOT * Math.exp(T * RATE), false);
      //UPPER = new FixedSecondDerivativeBoundaryCondition(0, 6.0 * SPOT * Math.exp(T * RATE), false);
      // UPPER = new NeumannBoundaryCondition(0.0, 10.0 * SPOT * Math.exp(T * RATE), false);
    } else {
      UPPER = new NeumannBoundaryCondition(1.0, 5.0 * SPOT * Math.exp(T * RATE), false);
    }

    INITIAL_COND1 = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double x) {
        return PROB_STATE1 * Math.max(0, SPOT - x);
      }
    };

    INITIAL_COND2 = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double x) {
        return (1.0 - PROB_STATE1) * Math.max(0, SPOT - x);
      }
    };

  }

  @Test(enabled = false)
  public void testMCSmile() {
    final int timeNodes = 51;
    final int spaceNodes = 151;

    final MeshingFunction timeMesh = new ExponentialMeshing(0.00, T, timeNodes, 7.5);
    final MeshingFunction spaceMesh = new HyperbolicMeshing(LOWER1.getLevel(), UPPER.getLevel(), SPOT, spaceNodes, 0.01);
    // MeshingFunction spaceMesh = new ExponentialMeshing(SPOT / 10., SPOT * 5, spaceNodes, 0.0);

    final double[] expiries = Arrays.copyOfRange(timeMesh.getPoints(), 0, timeNodes);
    final double[] strikes = spaceMesh.getPoints();
    final double[] forwards = new double[timeNodes];
    final double[] df = new double[timeNodes];
    for (int i = 0; i < timeNodes; i++) {
      df[i] = YIELD_CURVE.getDiscountFactor(expiries[i]);
      forwards[i] = SPOT / df[i];
    }

    final MarkovChain mc = new MarkovChain(VOL1, VOL2, LAMBDA12, LAMBDA21, PROB_STATE1);
    double impVol;

    final double weight = Math.exp(-LAMBDA12 * 0.004);
    double[][] mcSims = mc.simulate(expiries, 10000, 0.0, weight);
    final double[][] pricesA = mc.price(forwards, df, strikes, expiries, mcSims);

    mcSims = mc.simulate(expiries, 1000, weight, 1.0);
    final double[][] pricesB = mc.price(forwards, df, strikes, expiries, mcSims);

    for (int i = 0; i < spaceNodes; i++) {
      System.out.print("\t" + strikes[i]);
    }
    System.out.print("\n");
    for (int j = 0; j < timeNodes; j++) {
      final BlackFunctionData data = new BlackFunctionData(forwards[j], df[j], 0.0);
      System.out.print(expiries[j]);
      for (int i = 0; i < spaceNodes; i++) {
        try {
          final EuropeanVanillaOption option = new EuropeanVanillaOption(strikes[i], expiries[j], true);
          impVol = BLACK_IMPLIED_VOL.getImpliedVolatility(data, option, weight * pricesA[j][i] + (1 - weight) * pricesB[j][i]);
        } catch (final Exception e) {
          impVol = 0.0;
        }
        System.out.print("\t" + impVol);
      }
      System.out.print("\n");
    }

  }

  @Test(enabled = false)
  public void testApproxSmile() {
    final int timeNodes = 11;
    final int spaceNodes = 151;

    final MeshingFunction timeMesh = new ExponentialMeshing(0, 0.04, timeNodes, 0.0);
    // MeshingFunction spaceMesh = new HyperbolicMeshing(LOWER1.getLevel(), UPPER.getLevel(), SPOT, spaceNodes, 0.1);
    final MeshingFunction spaceMesh = new ExponentialMeshing(SPOT / 10., SPOT * 5, spaceNodes, 0.0);

    final double[] expiries = Arrays.copyOfRange(timeMesh.getPoints(), 0, timeNodes);
    final double[] strikes = spaceMesh.getPoints();
    final double[] forwards = new double[timeNodes];
    final double[] df = new double[timeNodes];
    for (int i = 0; i < timeNodes; i++) {
      df[i] = YIELD_CURVE.getDiscountFactor(expiries[i]);
      forwards[i] = SPOT / df[i];
    }

    final MarkovChainSmallTimeApprox mc = new MarkovChainSmallTimeApprox(VOL1, VOL2, LAMBDA12, LAMBDA21, PROB_STATE1);

    double impVol;

    for (int i = 0; i < spaceNodes; i++) {
      System.out.print("\t" + strikes[i]);
    }
    System.out.print("\n");
    for (int j = 0; j < timeNodes; j++) {
      final BlackFunctionData data = new BlackFunctionData(forwards[j], df[j], 0.0);
      System.out.print(expiries[j]);
      for (int i = 0; i < spaceNodes; i++) {
        final double price = mc.price(forwards[j], df[j], strikes[i], expiries[j]);
        try {

          final EuropeanVanillaOption option = new EuropeanVanillaOption(strikes[i], expiries[j], true);
          impVol = BLACK_IMPLIED_VOL.getImpliedVolatility(data, option, price);
        } catch (final Exception e) {
          impVol = 0.0;
        }
        System.out.print("\t" + impVol);
      }
      System.out.print("\n");
    }

  }

  @Test
  public void testDegenerate() {
    //NOTE vols equal 
    final TwoStateMarkovChainDataBundle mcData = new TwoStateMarkovChainDataBundle(VOL1, VOL1, LAMBDA12, LAMBDA21, PROB_STATE1);
    final CoupledFiniteDifference solver = new CoupledFiniteDifference(0.55, true);
    final ConvectionDiffusionPDE1DCoupledCoefficients[] pdeData = PDE_DATA_PROVIDER.getCoupledForwardPair(new ForwardCurve(SPOT, RATE), mcData);

    final int tNodes = 50;
    final int xNodes = 150;

    final MeshingFunction timeMesh = new ExponentialMeshing(0, T, tNodes, 7.5);
    final MeshingFunction spaceMesh = new HyperbolicMeshing(LOWER1.getLevel(), UPPER.getLevel(), SPOT, xNodes, 0.01);
    //MeshingFunction spaceMesh = new ExponentalMeshing(LOWER1.getLevel(), UPPER.getLevel(), xNodes, 0.0);

    final PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);

    final CoupledPDEDataBundle d1 = new CoupledPDEDataBundle(pdeData[0], INITIAL_COND1, LOWER1, UPPER, grid);
    final CoupledPDEDataBundle d2 = new CoupledPDEDataBundle(pdeData[1], INITIAL_COND2, LOWER2, UPPER, grid);

    final PDEResults1D[] res = solver.solve(d1, d2);
    final PDEFullResults1D res1 = (PDEFullResults1D) res[0];
    final PDEFullResults1D res2 = (PDEFullResults1D) res[1];
    double t, k;
    double price;
    double impVol;
    final double tMin = 0.03;

    for (int j = 0; j < tNodes; j++) {
      t = res1.getTimeValue(j);
      if (t > tMin) {
        final double rootT = Math.sqrt(t);
        final double df = YIELD_CURVE.getDiscountFactor(t);
        final BlackFunctionData data = new BlackFunctionData(SPOT / df, df, 0.0);

        for (int i = 0; i < xNodes; i++) {
          k = res1.getSpaceValue(i);
          final double z = (Math.log(k / SPOT) - (RATE - VOL1 * VOL1 / 2) * t) / VOL1 / rootT;
          if (z > -2.6 && z < 3.2) {
            price = res1.getFunctionValue(i, j) + res2.getFunctionValue(i, j);
            final EuropeanVanillaOption option = new EuropeanVanillaOption(k, t, ISCALL);
            try {
              impVol = BLACK_IMPLIED_VOL.getImpliedVolatility(data, option, price);
            } catch (final Exception e) {
              impVol = 0.0;
            }
            //System.out.println(t + "\t" + k);
            assertEquals(VOL1, impVol, 5e-3);
          }
        }
      }
    }
  }

  @Test
  public void testSmile() {

    final CoupledFiniteDifference solver = new CoupledFiniteDifference(0.55, true);

    final TwoStateMarkovChainDataBundle mcData = new TwoStateMarkovChainDataBundle(VOL1, VOL2, LAMBDA12, LAMBDA21, PROB_STATE1);
    final ConvectionDiffusionPDE1DCoupledCoefficients[] pdeData = PDE_DATA_PROVIDER.getCoupledForwardPair(FORWARD, mcData);

    final int tNodes = 51;
    final int xNodes = 151;

    final MeshingFunction timeMesh = new ExponentialMeshing(0, T, tNodes, 7.5);
    final MeshingFunction spaceMesh = new HyperbolicMeshing(LOWER1.getLevel(), UPPER.getLevel(), SPOT, xNodes, 0.01);
    //MeshingFunction spaceMesh = new ExponentalMeshing(LOWER1.getLevel(), UPPER.getLevel(), xNodes, 0.0);

    final double[] expiries = Arrays.copyOfRange(timeMesh.getPoints(), 1, tNodes);
    final double[] strikes = spaceMesh.getPoints();
    final double[] forwards = new double[tNodes - 1];
    final double[] df = new double[tNodes - 1];
    for (int i = 0; i < tNodes - 1; i++) {
      df[i] = YIELD_CURVE.getDiscountFactor(expiries[i]);
      forwards[i] = SPOT / df[i];
    }

    final MarkovChain mc = new MarkovChain(VOL1, VOL2, LAMBDA12, LAMBDA21, PROB_STATE1);

    final double[][] mcSims = mc.simulate(expiries, 1000);
    final double[][] mcPrices = mc.price(forwards, df, strikes, expiries, mcSims);

    final PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);
    final CoupledPDEDataBundle d1 = new CoupledPDEDataBundle(pdeData[0], INITIAL_COND1, LOWER1, UPPER, grid);
    final CoupledPDEDataBundle d2 = new CoupledPDEDataBundle(pdeData[1], INITIAL_COND2, LOWER2, UPPER, grid);

    final PDEResults1D[] res = solver.solve(d1, d2);
    final PDEFullResults1D res1 = (PDEFullResults1D) res[0];
    final PDEFullResults1D res2 = (PDEFullResults1D) res[1];
    double t, k;
    double price;
    double impVol, mcImpVol = 0;
    final double tMin = 0.5;
    final double zMax = 2.0;
    double mcPrice;

    for (int j = 1; j < tNodes; j++) {
      t = res1.getTimeValue(j);
      if (t > tMin) {
        final double rootT = Math.sqrt(t);
        final BlackFunctionData data = new BlackFunctionData(forwards[j - 1], df[j - 1], 0.0);

        for (int i = 0; i < xNodes; i++) {
          k = res1.getSpaceValue(i);
          final double z = (Math.log(k / SPOT) - (RATE - VOL1 * VOL1 / 2) * t) / VOL1 / rootT;
          if (Math.abs(z) < zMax) {
            price = res1.getFunctionValue(i, j) + res2.getFunctionValue(i, j);
            mcPrice = mcPrices[j - 1][i];
            //  System.out.println(t + "\t" + k);
            assertEquals(mcPrice, price, 1e-1 * mcPrice);
            final EuropeanVanillaOption option = new EuropeanVanillaOption(k, t, ISCALL);
            try {
              impVol = BLACK_IMPLIED_VOL.getImpliedVolatility(data, option, price);
              mcImpVol = BLACK_IMPLIED_VOL.getImpliedVolatility(data, option, mcPrice);
            } catch (final Exception e) {
              impVol = 0.0;
            }
            assertEquals(impVol, mcImpVol, 4e-3);
          }
        }
      }
    }
  }

  @Test(enabled = false)
  public void testSmilePrint() {
    //final boolean print = true;
    final CoupledFiniteDifference solver = new CoupledFiniteDifference(0.55, true);

    final TwoStateMarkovChainDataBundle mcData = new TwoStateMarkovChainDataBundle(VOL1, VOL2, LAMBDA12, LAMBDA21, PROB_STATE1);
    final ConvectionDiffusionPDE1DCoupledCoefficients[] pdeData = PDE_DATA_PROVIDER.getCoupledForwardPair(FORWARD, mcData);

    final int tNodes = 51;
    final int xNodes = 151;
    //final double lowerMoneyness = 0.0;
    //final double upperMoneyness = 3.0;

    //  MarkovChain mc = new MarkovChain(VOL1, VOL2, lambda12, lambda21, 1.0);
    // double[] mcSims = mc.simulate(T, 10000); // simulate the vol path

    final MeshingFunction timeMesh = new ExponentialMeshing(0, T, tNodes, 7.5);
    final MeshingFunction spaceMesh = new HyperbolicMeshing(LOWER1.getLevel(), UPPER.getLevel(), SPOT, xNodes, 0.01);
    //MeshingFunction spaceMesh = new ExponentialMeshing(LOWER1.getLevel(), UPPER.getLevel(), xNodes, 0.0);

    final PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);
    final CoupledPDEDataBundle d1 = new CoupledPDEDataBundle(pdeData[0], INITIAL_COND1, LOWER1, UPPER, grid);
    final CoupledPDEDataBundle d2 = new CoupledPDEDataBundle(pdeData[1], INITIAL_COND2, LOWER2, UPPER, grid);

    final PDEResults1D[] res = solver.solve(d1, d2);
    final PDEFullResults1D res1 = (PDEFullResults1D) res[0];
    final PDEFullResults1D res2 = (PDEFullResults1D) res[1];
    double t, k;
    double price;
    double impVol;
    //final double lowerK, upperK;
    //final double tMin = 0.02;

    //Combined price

    for (int i = 0; i < xNodes; i++) {
      System.out.print("\t" + res1.getSpaceValue(i));
    }
    System.out.print("\n");

    for (int j = 0; j < tNodes; j++) {
      t = res1.getTimeValue(j);
      //final double p1 = probState1(LAMBDA12, LAMBDA21, 1.0, t);
      final double df = YIELD_CURVE.getDiscountFactor(t);
      final BlackFunctionData data = new BlackFunctionData(SPOT / df, df, 0.0);

      System.out.print(t);

      for (int i = 0; i < xNodes; i++) {
        k = res1.getSpaceValue(i);
        price = res1.getFunctionValue(i, j) + res2.getFunctionValue(i, j);
        final EuropeanVanillaOption option = new EuropeanVanillaOption(k, t, ISCALL);
        try {
          impVol = BLACK_IMPLIED_VOL.getImpliedVolatility(data, option, price);
        } catch (final Exception e) {
          impVol = 0.0;
        }
        System.out.print("\t" + impVol);
      }
      System.out.print("\n");
    }
    System.out.print("\n");

  }

  private static double probState1(final double lambda12, final double lambda21, final double pState1T0, final double t) {
    final double sum = lambda12 + lambda21;
    if (sum == 0) {
      return pState1T0;
    }
    final double pi1 = lambda21 / sum;
    return pi1 + (pState1T0 - pi1) * Math.exp(-sum * t);
  }

}
