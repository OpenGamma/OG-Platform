/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.finitedifference.applications.CoupledPDEDataBundleProvider;
import com.opengamma.analytics.financial.model.finitedifference.applications.PDEUtilityTools;
import com.opengamma.analytics.financial.model.finitedifference.applications.TwoStateMarkovChainDataBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.DoubleQuadraticInterpolator1D;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.DoublesPair;

/**
 * @deprecated This class tests deprecated functionality
 */
@Deprecated
@SuppressWarnings("unused")
@Test(groups = TestGroup.UNIT)
public class CoupledFokkerPlankPDEtest {

  private static final CoupledPDEDataBundleProvider PDE_DATA_PROVIDER = new CoupledPDEDataBundleProvider();

  //private static final BlackImpliedVolatilityFormula BLACK_IMPLIED_VOL = new BlackImpliedVolatilityFormula();
  private static final BoundaryCondition LOWER;
  private static final BoundaryCondition UPPER;

  private static final double SPOT = 1.0;
  private static final ForwardCurve FORWARD;
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
  private static final ExtendedCoupledPDEDataBundle DATA1;
  private static final ExtendedCoupledPDEDataBundle DATA2;

  private static final DoubleQuadraticInterpolator1D INTERPOLATOR_1D = new DoubleQuadraticInterpolator1D();
  private static final GridInterpolator2D GRID_INTERPOLATOR2D = new GridInterpolator2D(INTERPOLATOR_1D, INTERPOLATOR_1D);

  static {

    FORWARD = new ForwardCurve(SPOT, RATE);
    final TwoStateMarkovChainDataBundle chainData = new TwoStateMarkovChainDataBundle(VOL1, VOL2, LAMBDA12, LAMBDA21, INITIAL_PROB_STATE1);
    final ExtendedCoupledPDEDataBundle[] pdeData = PDE_DATA_PROVIDER.getCoupledFokkerPlankPair(FORWARD, chainData);
    DATA1 = pdeData[0];
    DATA2 = pdeData[1];

    final Function1D<Double, Double> upper1stDev = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double t) {
        return Math.exp(-RATE * t);
      }
    };

    LOWER = new DirichletBoundaryCondition(0.0, 0.0);
    UPPER = new DirichletBoundaryCondition(0.0, 15.0 * SPOT);

  }

  //TODO quantitative test here (rather than printing surfaces)
  @Test(enabled = false)
  public void testDensity() {
    final ExtendedCoupledFiniteDifference solver = new ExtendedCoupledFiniteDifference(0.5);
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
        localVolData.put(DoublesPair.of(t, k), value);
      }
    }

    final Map<Double, Interpolator1DDataBundle> dataBundle = GRID_INTERPOLATOR2D.getDataBundle(localVolData);

    final Function<Double, Double> localVolFunction = new Function<Double, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double... x) {
        return GRID_INTERPOLATOR2D.interpolate(dataBundle, DoublesPair.of(x[0].doubleValue(), x[1].doubleValue()));
      }
    };

    final FunctionalDoublesSurface localVolSurface = FunctionalDoublesSurface.from(localVolFunction);

    PDEUtilityTools.printSurface("LV surface", localVolSurface, 0, 5.0, SPOT / 4.0, 4.0 * SPOT);
  }

}
