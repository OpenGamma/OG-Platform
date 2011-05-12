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
import com.opengamma.math.surface.Surface;

/**
 * 
 */
public class CoupledFiniteDifferenceTest {

  private static final BlackImpliedVolatilityFormula BLACK_IMPLIED_VOL = new BlackImpliedVolatilityFormula();
  private static BoundaryCondition LOWER;
  private static BoundaryCondition UPPER;

  private static final double SPOT = 100;
  private static final double FORWARD;
  private static final double STRIKE;
  private static final double T = 5.0;
  private static final double RATE = 0.05;
  private static final YieldAndDiscountCurve YIELD_CURVE = new YieldCurve(ConstantDoublesCurve.from(RATE));
  private static final double VOL1 = 0.20;
  private static final double VOL2 = 0.30;

  private static final EuropeanVanillaOption OPTION;
  private static final ConvectionDiffusionPDEDataBundle DATA1;
  private static final ConvectionDiffusionPDEDataBundle DATA2;

  private static Surface<Double, Double, Double> A1;
  private static Surface<Double, Double, Double> A2;
  private static Surface<Double, Double, Double> B;
  private static Surface<Double, Double, Double> C;

  static {

    FORWARD = SPOT / YIELD_CURVE.getDiscountFactor(T);
    STRIKE = FORWARD; // ATM option
    OPTION = new EuropeanVanillaOption(FORWARD, T, true); // true option

    LOWER = new DirichletBoundaryCondition(0.0, 0.0);// call is worth 0 when stock falls to zero
    // UPPER = new DirichletBoundaryCondition(0.0, 5.0 * SPOT);
    UPPER = new FixedSecondDerivativeBoundaryCondition(0.0, 10 * SPOT, false);

    final Function<Double, Double> a1 = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        double s = ts[1];
        return -s * s * VOL1 * VOL1 / 2;
      }
    };

    final Function<Double, Double> a2 = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        double s = ts[1];
        return -s * s * VOL2 * VOL2 / 2;
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
        return Math.max(0, x - STRIKE);
      }
    };

    A1 = FunctionalDoublesSurface.from(a1);
    A2 = FunctionalDoublesSurface.from(a2);
    B = FunctionalDoublesSurface.from(b);
    C = FunctionalDoublesSurface.from(c);

    DATA1 = new ConvectionDiffusionPDEDataBundle(A1, B, C, payoff);
    DATA2 = new ConvectionDiffusionPDEDataBundle(A2, B, C, payoff);
  }

  @Test
  public void testNoCoupling() {
    CoupledFiniteDifference solver = new CoupledFiniteDifference();
    double lambda12 = 0.0;
    double lambda21 = 0.0;
    int timeNodes = 10;
    int spaceNodes = 101;
    double lowerMoneyness = 0.4;
    double upperMoneyness = 3.0;

    MeshingFunction timeMesh = new ExponentalMeshing(0, T, timeNodes, 0);
    // MeshingFunction spaceMesh = new HyperbolicMeshing(LOWER.getLevel(), UPPER.getLevel(), OPTION.getStrike(), 0.01, spaceNodes);
    MeshingFunction spaceMesh = new ExponentalMeshing(LOWER.getLevel(), UPPER.getLevel(), spaceNodes, 0.0);

    double[] timeGrid = new double[timeNodes];
    for (int n = 0; n < timeNodes; n++) {
      timeGrid[n] = timeMesh.evaluate(n);
    }

    double[] spaceGrid = new double[spaceNodes];
    for (int i = 0; i < spaceNodes; i++) {
      spaceGrid[i] = spaceMesh.evaluate(i);
    }

    double[][] res = solver.solve(DATA1, DATA2, timeGrid, spaceGrid, LOWER, UPPER, lambda12, lambda21, null);
    double df = YIELD_CURVE.getDiscountFactor(T);
    int n = res[0].length;
    for (int i = 0; i < n; i++) {
      double spot = res[0][i];
      double moneyness = spot / OPTION.getStrike();
      if (moneyness >= lowerMoneyness && moneyness <= upperMoneyness) {
        BlackFunctionData data = new BlackFunctionData(spot / df, df, 0.0);
        double impVol1;
        try {
          impVol1 = BLACK_IMPLIED_VOL.getImpliedVolatility(data, OPTION, res[1][i]);
        } catch (Exception e) {
          impVol1 = 0.0;
        }
        double impVol2;
        try {
          impVol2 = BLACK_IMPLIED_VOL.getImpliedVolatility(data, OPTION, res[2][i]);
        } catch (Exception e) {
          impVol2 = 0.0;
        }
        // System.out.println(spot + "\t" + res[1][i] + "\t" + impVol1 + "\t" + impVol2);
        assertEquals(VOL1, impVol1, 1e-3);
        assertEquals(VOL2, impVol2, 1e-3);
      }
    }
  }

  @Test
  public void testDegenerate() {
    CoupledFiniteDifference solver = new CoupledFiniteDifference();
    double lambda12 = 0.2;
    double lambda21 = 0.3;
    int timeNodes = 20;
    int spaceNodes = 101;
    double lowerMoneyness = 0.4;
    double upperMoneyness = 3.0;

    MeshingFunction timeMesh = new ExponentalMeshing(0, T, timeNodes, 0);
    // MeshingFunction spaceMesh = new HyperbolicMeshing(LOWER.getLevel(), UPPER.getLevel(), OPTION.getStrike(), 0.01, spaceNodes);
    MeshingFunction spaceMesh = new ExponentalMeshing(LOWER.getLevel(), UPPER.getLevel(), spaceNodes, 0.0);

    double[] timeGrid = new double[timeNodes];
    for (int n = 0; n < timeNodes; n++) {
      timeGrid[n] = timeMesh.evaluate(n);
    }

    double[] spaceGrid = new double[spaceNodes];
    for (int i = 0; i < spaceNodes; i++) {
      spaceGrid[i] = spaceMesh.evaluate(i);
    }

    double[][] res = solver.solve(DATA1, DATA1, timeGrid, spaceGrid, LOWER, UPPER, lambda12, lambda21, null);
    double df = YIELD_CURVE.getDiscountFactor(T);
    int n = res[0].length;
    for (int i = 0; i < n; i++) {
      double spot = res[0][i];
      double moneyness = spot / OPTION.getStrike();
      if (moneyness >= lowerMoneyness && moneyness <= upperMoneyness) {
        BlackFunctionData data = new BlackFunctionData(spot / df, df, 0.0);
        double impVol1;
        try {
          impVol1 = BLACK_IMPLIED_VOL.getImpliedVolatility(data, OPTION, res[1][i]);
        } catch (Exception e) {
          impVol1 = 0.0;
        }
        double impVol2;
        try {
          impVol2 = BLACK_IMPLIED_VOL.getImpliedVolatility(data, OPTION, res[2][i]);
        } catch (Exception e) {
          impVol2 = 0.0;
        }
        // System.out.println(spot + "\t" + res[1][i] + "\t" + res[2][i] + "\t" + impVol1 + "\t" + impVol2);
        assertEquals(VOL1, impVol1, 1e-3);
        assertEquals(VOL1, impVol2, 1e-3);
      }
    }
  }

  @Test(enabled = false)
  public void testSmile() {
    CoupledFiniteDifference solver = new CoupledFiniteDifference();
    double lambda12 = 0.5;
    double lambda21 = 10.0;
    int timeNodes = 10;
    int spaceNodes = 201;
    double lowerMoneyness = 0.0;
    double upperMoneyness = 3.0;

    MeshingFunction timeMesh = new ExponentalMeshing(0, T, timeNodes, 0);
    // MeshingFunction spaceMesh = new HyperbolicMeshing(LOWER.getLevel(), UPPER.getLevel(), OPTION.getStrike(), 0.01, spaceNodes);
    MeshingFunction spaceMesh = new ExponentalMeshing(LOWER.getLevel(), UPPER.getLevel(), spaceNodes, 0.0);

    double[] timeGrid = new double[timeNodes];
    for (int n = 0; n < timeNodes; n++) {
      timeGrid[n] = timeMesh.evaluate(n);
    }

    double[] spaceGrid = new double[spaceNodes];
    for (int i = 0; i < spaceNodes; i++) {
      spaceGrid[i] = spaceMesh.evaluate(i);
    }

    double[][] res = solver.solve(DATA1, DATA2, timeGrid, spaceGrid, LOWER, UPPER, lambda12, lambda21, null);
    double df = YIELD_CURVE.getDiscountFactor(T);
    int n = res[0].length;
    for (int i = 0; i < n; i++) {
      double spot = res[0][i];
      double moneyness = spot / OPTION.getStrike();
      if (moneyness >= lowerMoneyness && moneyness <= upperMoneyness) {
        BlackFunctionData data = new BlackFunctionData(spot / df, df, 0.0);
        double impVol1;
        try {
          impVol1 = BLACK_IMPLIED_VOL.getImpliedVolatility(data, OPTION, res[1][i]);
        } catch (Exception e) {
          impVol1 = 0.0;
        }
        double impVol2;
        try {
          impVol2 = BLACK_IMPLIED_VOL.getImpliedVolatility(data, OPTION, res[2][i]);
        } catch (Exception e) {
          impVol2 = 0.0;
        }
        System.out.println(spot + "\t" + res[1][i] + "\t" + res[2][i] + "\t" + impVol1 + "\t" + impVol2);
        // assertEquals(VOL1, impVol1, 1e-3);
        // assertEquals(VOL1, impVol2, 1e-3);
      }
    }
  }
}
