/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finitedifference;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.testng.annotations.Test;

import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.volatility.BlackImpliedVolatilityFormula;
import com.opengamma.math.curve.ConstantDoublesCurve;
import com.opengamma.math.function.Function;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.ShepardInterpolatorND;
import com.opengamma.math.interpolation.data.InterpolatorNDDataBundle;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.surface.FunctionalDoublesSurface;
import com.opengamma.math.surface.Surface;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class CoupledFokkedPlankPDEtest {

  private static final BlackImpliedVolatilityFormula BLACK_IMPLIED_VOL = new BlackImpliedVolatilityFormula();
  private static final BoundaryCondition LOWER;
  private static final BoundaryCondition UPPER;

  private static final double SPOT = 100;
  private static final double FORWARD;
  private static final double STRIKE;
  private static final double T = 5.0;
  private static final double RATE = 0.05;
  private static final YieldAndDiscountCurve YIELD_CURVE = new YieldCurve(ConstantDoublesCurve.from(RATE));
  private static final double VOL1 = 0.20;
  private static final double VOL2 = 0.70;
  private static final double LAMBDA12 = 0.2;
  private static final double LAMBDA21 = 2.0;
  private static final double INITIAL_PROB_STATE1 = 1.0;

  private static final EuropeanVanillaOption OPTION;
  private static final ConvectionDiffusionPDEDataBundle DATA1;
  private static final ConvectionDiffusionPDEDataBundle DATA2;

  private static final Surface<Double, Double, Double> A1;
  private static final Surface<Double, Double, Double> A2;
  private static final Surface<Double, Double, Double> B1;
  private static final Surface<Double, Double, Double> C1;
  private static final Surface<Double, Double, Double> B2;
  private static final Surface<Double, Double, Double> C2;

  private static final ShepardInterpolatorND INTERPOLATOR = new ShepardInterpolatorND(3.0);

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

    LOWER = new DirichletBoundaryCondition(0.0, 0.0);
    UPPER = new DirichletBoundaryCondition(0.0, 15.0 * SPOT);

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

    final Function<Double, Double> b1 = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        double s = ts[1];
        //return RATE;
        return (RATE - 2 * VOL1 * VOL1) * s;
      }
    };

    final Function<Double, Double> b2 = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        double s = ts[1];
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
      double tOffset = 0.05;

      @Override
      public Double evaluate(Double s) {

        if (s == 0) {
          return 0.0;
        }
        double x = Math.log(s / SPOT);
        NormalDistribution dist = new NormalDistribution((RATE - VOL1 * VOL1 / 2) * tOffset, VOL1 * Math.sqrt(tOffset));
        return INITIAL_PROB_STATE1 * dist.getPDF(x) / s;
      }
    };

    final Function1D<Double, Double> initialCondition2 = new Function1D<Double, Double>() {
      double tOffset = 0.05;

      @Override
      public Double evaluate(Double s) {

        if (s == 0) {
          return 0.0;
        }
        double x = Math.log(s / SPOT);
        NormalDistribution dist = new NormalDistribution((RATE - VOL2 * VOL2 / 2) * tOffset, VOL2 * Math.sqrt(tOffset));
        return (1 - INITIAL_PROB_STATE1) * dist.getPDF(x) / s;
      }
    };

    A1 = FunctionalDoublesSurface.from(a1);
    A2 = FunctionalDoublesSurface.from(a2);
    B1 = FunctionalDoublesSurface.from(b1);
    B2 = FunctionalDoublesSurface.from(b1);
    C1 = FunctionalDoublesSurface.from(c1);
    C2 = FunctionalDoublesSurface.from(c2);

    DATA1 = new ConvectionDiffusionPDEDataBundle(A1, B1, C1, initialCondition1);
    DATA2 = new ConvectionDiffusionPDEDataBundle(A2, B2, C2, initialCondition2);

  }

  @Test
  public void testDensity() {
    CoupledFiniteDifference solver = new CoupledFiniteDifference(0.5, true);
    int tNodes = 50;
    int xNodes = 150;

    MeshingFunction timeMesh = new ExponentalMeshing(0, T, tNodes, 0);
    // MeshingFunction spaceMesh = new HyperbolicMeshing(LOWER.getLevel(), UPPER.getLevel(), OPTION.getStrike(), 0.01, spaceNodes);
    MeshingFunction spaceMesh = new ExponentalMeshing(LOWER.getLevel(), UPPER.getLevel(), xNodes, 0.0);

    double[] timeGrid = new double[tNodes];
    for (int n = 0; n < tNodes; n++) {
      timeGrid[n] = timeMesh.evaluate(n);
    }

    double[] spaceGrid = new double[xNodes];
    for (int i = 0; i < xNodes; i++) {
      spaceGrid[i] = spaceMesh.evaluate(i);
    }

    PDEGrid1D grid = new PDEGrid1D(timeGrid, spaceGrid);
    PDEResults1D[] res = solver.solve(DATA1, DATA2, grid, LOWER, UPPER, -LAMBDA21, -LAMBDA12, null);
    PDEFullResults1D res1 = (PDEFullResults1D) res[0];
    PDEFullResults1D res2 = (PDEFullResults1D) res[1];

    //    for (int i = 0; i < xNodes; i++) {
    //      System.out.print("\t" + res1.getSpaceValue(i));
    //    }
    //    System.out.print("\n");
    //
    //    for (int j = 0; j < tNodes; j++) {
    //      System.out.print(res1.getTimeValue(j));
    //      for (int i = 0; i < xNodes; i++) {
    //        System.out.print("\t" + res1.getFunctionValue(i, j));
    //      }
    //      System.out.print("\n");
    //    }
    //    System.out.print("\n");
    //    for (int i = 0; i < xNodes; i++) {
    //      System.out.print("\t" + res2.getSpaceValue(i));
    //    }
    //    System.out.print("\n");
    //
    //    for (int j = 0; j < tNodes; j++) {
    //      System.out.print(res2.getTimeValue(j));
    //      for (int i = 0; i < xNodes; i++) {
    //        System.out.print("\t" + res2.getFunctionValue(i, j));
    //      }
    //      System.out.print("\n");
    //    }

    //calculated the local vol surface
    List<Pair<double[], Double>> localVolData = new ArrayList<Pair<double[], Double>>(xNodes * tNodes);
    double norm;
    double[] x;
    double value;
    for (int j = 0; j < tNodes; j++) {
      for (int i = 0; i < xNodes; i++) {
        x = new double[2];
        x[0] = res1.getTimeValue(j);
        x[1] = res1.getSpaceValue(i);
        norm = res1.getFunctionValue(i, j) + res2.getFunctionValue(i, j);
        if (norm == 0.0) {
          value = 0.0;
        } else {
          value = VOL1 * VOL1 * res1.getFunctionValue(i, j) + VOL2 * VOL2 * res2.getFunctionValue(i, j);
          value /= norm;
          value = Math.sqrt(value);
        }
        localVolData.add(new ObjectsPair<double[], Double>(x, value));
      }
    }

    final InterpolatorNDDataBundle dataBundle = INTERPOLATOR.getDataBundle(localVolData);

    final Function<Double, Double> localVolFunction = new Function<Double, Double>() {

      @Override
      public Double evaluate(Double... x) {
        double[] tx = new double[2];
        tx[0] = x[0];
        tx[1] = x[1];
        return INTERPOLATOR.interpolate(dataBundle, tx);
      }
    };

    final FunctionalDoublesSurface localVolSurface = FunctionalDoublesSurface.from(localVolFunction);

    final Function<Double, Double> a = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        double s = ts[1];
        double vol = localVolSurface.getZValue(ts[0], ts[1]);
        return -s * s * vol * vol / 2;
      }
    };

    final Function<Double, Double> b = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        double s = ts[1];
        //return RATE;
        return RATE * s;
      }
    };

    final Function<Double, Double> c = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        double s = ts[1];
        return RATE;
      }
    };

  }

}
