/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finitedifference;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.testng.annotations.Test;

import com.opengamma.financial.model.finitedifference.applications.PDEUtilityTools;
import com.opengamma.math.function.Function;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.DoubleQuadraticInterpolator1D;
import com.opengamma.math.interpolation.GridInterpolator2D;
import com.opengamma.math.interpolation.data.Interpolator1DDoubleQuadraticDataBundle;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.surface.FunctionalDoublesSurface;
import com.opengamma.math.surface.Surface;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
@SuppressWarnings("unused")
public class CoupledFokkerPlankPDEtest {

  //private static final BlackImpliedVolatilityFormula BLACK_IMPLIED_VOL = new BlackImpliedVolatilityFormula();
  private static final BoundaryCondition LOWER;
  private static final BoundaryCondition UPPER;

  private static final double SPOT = 1.0;
  //private static final double FORWARD;
  //private static final double STRIKE;
  private static final double T = 5.0;
  private static final double RATE = 0.0;
  //private static final YieldAndDiscountCurve YIELD_CURVE = new YieldCurve(ConstantDoublesCurve.from(RATE));
  private static final double VOL1 = 0.20;
  private static final double VOL2 = 0.70;
  private static final double LAMBDA12 = 0.2;
  private static final double LAMBDA21 = 2.0;
  private static final double INITIAL_PROB_STATE1 = 1.0;

  //private static final EuropeanVanillaOption OPTION;
  private static final CoupledPDEDataBundle DATA1;
  private static final CoupledPDEDataBundle DATA2;

  private static final Surface<Double, Double, Double> A1;
  private static final Surface<Double, Double, Double> A2;
  private static final Surface<Double, Double, Double> B1;
  private static final Surface<Double, Double, Double> C1;
  private static final Surface<Double, Double, Double> B2;
  private static final Surface<Double, Double, Double> C2;

  private static final DoubleQuadraticInterpolator1D INTERPOLATOR_1D = new DoubleQuadraticInterpolator1D();
  private static final GridInterpolator2D<Interpolator1DDoubleQuadraticDataBundle, Interpolator1DDoubleQuadraticDataBundle> GRID_INTERPOLATOR2D = 
    new GridInterpolator2D<Interpolator1DDoubleQuadraticDataBundle, Interpolator1DDoubleQuadraticDataBundle>(INTERPOLATOR_1D, INTERPOLATOR_1D);

  static {

    //FORWARD = SPOT / YIELD_CURVE.getDiscountFactor(T);
    //STRIKE = FORWARD; // ATM option
    //OPTION = new EuropeanVanillaOption(FORWARD, T, true); // true option

    final Function1D<Double, Double> upper1stDev = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double t) {
        return Math.exp(-RATE * t);
      }
    };

    LOWER = new DirichletBoundaryCondition(0.0, 0.0);
    UPPER = new DirichletBoundaryCondition(0.0, 15.0 * SPOT);

    final Function<Double, Double> a1 = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        final double s = ts[1];
        return -s * s * VOL1 * VOL1 / 2;
      }
    };

    final Function<Double, Double> a2 = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        final double s = ts[1];
        return -s * s * VOL2 * VOL2 / 2;
      }
    };

    final Function<Double, Double> b1 = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        final double s = ts[1];
        //return RATE;
        return (RATE - 2 * VOL1 * VOL1) * s;
      }
    };

    final Function<Double, Double> b2 = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        final double s = ts[1];
        //return RATE;
        return (RATE - 2 * VOL2 * VOL2) * s;
      }
    };

    final Function<Double, Double> c1 = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        //return 0.0;
        return RATE + LAMBDA12 - VOL1 * VOL1;
      }
    };

    final Function<Double, Double> c2 = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        //return 0.0;
        return RATE + LAMBDA21 - VOL2 * VOL2;
      }
    };

    final Function<Double, Double> zero = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        return 0.0;
      }
    };

    //using a normal distribution with a very small Standard deviation as a proxy for a Dirac delta
    final Function1D<Double, Double> initialCondition1 = new Function1D<Double, Double>() {
      private final double _tOffset = 0.05;

      @Override
      public Double evaluate(final Double s) {

        if (s == 0) {
          return 0.0;
        }
        final double x = Math.log(s / SPOT);
        final NormalDistribution dist = new NormalDistribution((RATE - VOL1 * VOL1 / 2) * _tOffset, VOL1 * Math.sqrt(_tOffset));
        return INITIAL_PROB_STATE1 * dist.getPDF(x) / s;
      }
    };

    final Function1D<Double, Double> initialCondition2 = new Function1D<Double, Double>() {
      private final double _tOffset = 0.05;

      @Override
      public Double evaluate(final Double s) {

        if (s == 0) {
          return 0.0;
        }
        final double x = Math.log(s / SPOT);
        final NormalDistribution dist = new NormalDistribution((RATE - VOL2 * VOL2 / 2) * _tOffset, VOL2 * Math.sqrt(_tOffset));
        return (1 - INITIAL_PROB_STATE1) * dist.getPDF(x) / s;
      }
    };

    A1 = FunctionalDoublesSurface.from(a1);
    A2 = FunctionalDoublesSurface.from(a2);
    B1 = FunctionalDoublesSurface.from(b1);
    B2 = FunctionalDoublesSurface.from(b1);
    C1 = FunctionalDoublesSurface.from(c1);
    C2 = FunctionalDoublesSurface.from(c2);

    DATA1 = new CoupledPDEDataBundle(A1, B1, C1, -LAMBDA21, initialCondition1);
    DATA2 = new CoupledPDEDataBundle(A2, B2, C2, -LAMBDA12, initialCondition2);

  }

  //TODO quantitative test here (rather than printing surfaces)
  //TODO Use the PDEDATABundleProvider
  @Test(enabled = false)
  public void testDensity() {
    final CoupledFiniteDifference solver = new CoupledFiniteDifference(0.5, true);
    final int tNodes = 50;
    final int xNodes = 150;

    final MeshingFunction timeMesh = new ExponentialMeshing(0, T, tNodes, 5.0);
    final MeshingFunction spaceMesh = new HyperbolicMeshing(LOWER.getLevel(), UPPER.getLevel(), SPOT, xNodes, 0.01);
    //MeshingFunction spaceMesh = new ExponentialMeshing(LOWER.getLevel(), UPPER.getLevel(), xNodes, 0.0);

    final double[] timeGrid = new double[tNodes];
    for (int n = 0; n < tNodes; n++) {
      timeGrid[n] = timeMesh.evaluate(n);
    }

    final double[] spaceGrid = new double[xNodes];
    for (int i = 0; i < xNodes; i++) {
      spaceGrid[i] = spaceMesh.evaluate(i);
    }

    final PDEGrid1D grid = new PDEGrid1D(timeGrid, spaceGrid);
    final PDEResults1D[] res = solver.solve(DATA1, DATA2, grid, LOWER, UPPER, LOWER, UPPER, null);
    final PDEFullResults1D res1 = (PDEFullResults1D) res[0];
    final PDEFullResults1D res2 = (PDEFullResults1D) res[1];

    PDEUtilityTools.printSurface("State 1 density", res1);
    PDEUtilityTools.printSurface("State 2 density", res2);

    //calculated the local vol surface
    final Map<DoublesPair, Double> localVolData = new HashMap<DoublesPair, Double>(xNodes * tNodes);
    double norm;
    double t, k;
    double value;
    for (int j = 0; j < tNodes; j++) {
      for (int i = 0; i < xNodes; i++) {
        t = res1.getTimeValue(j);
        k = res1.getSpaceValue(i);
        norm = res1.getFunctionValue(i, j) + res2.getFunctionValue(i, j);
        if (norm == 0.0) {
          value = 0.0;
        } else {
          value = VOL1 * VOL1 * res1.getFunctionValue(i, j) + VOL2 * VOL2 * res2.getFunctionValue(i, j);
          value /= norm;
          value = Math.sqrt(value);
        }
        localVolData.put(new DoublesPair(t, k), value);
      }
    }

    final Map<Double, Interpolator1DDoubleQuadraticDataBundle> dataBundle = GRID_INTERPOLATOR2D.getDataBundle(localVolData);

    final Function<Double, Double> localVolFunction = new Function<Double, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double... x) {
        return GRID_INTERPOLATOR2D.interpolate(dataBundle, new DoublesPair(x[0], x[1]));
      }
    };

    final FunctionalDoublesSurface localVolSurface = FunctionalDoublesSurface.from(localVolFunction);

    PDEUtilityTools.printSurface("LV surface", localVolSurface, 0, 5.0, SPOT / 4.0, 4.0 * SPOT);
  }

}
