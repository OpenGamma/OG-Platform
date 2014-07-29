/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference.applications;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.finitedifference.ExponentialMeshing;
import com.opengamma.analytics.financial.model.finitedifference.HyperbolicMeshing;
import com.opengamma.analytics.financial.model.finitedifference.MeshingFunction;
import com.opengamma.analytics.financial.model.finitedifference.PDEFullResults1D;
import com.opengamma.analytics.financial.model.finitedifference.PDEGrid1D;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.volatility.local.AbsoluteLocalVolatilitySurface;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class TwoStateMarkovChainWithLocalVolDensityTest {

  private static final double VOL1 = 0.2;
  private static final double VOL2 = 0.5;
  private static final double LAMBDA12 = 0.2;
  private static final double LAMBDA21 = 2.0;
  private static final double P0 = 0.95;

  private static final double BETA = 0.5;

  //private static final double T = 5.0;
  private static final double SPOT = 1.0;
  private static final ForwardCurve FORWARD_CURVE;
  //private static final YieldCurve YIELD_CURVE;
  private static final double RATE = 0.0;
  private static final TwoStateMarkovChainDataBundle DATA = new TwoStateMarkovChainDataBundle(VOL1, VOL2, LAMBDA12, LAMBDA21, P0, BETA, BETA);

  static {

    final Function1D<Double, Double> fwd = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double t) {
        return SPOT * Math.exp(t * RATE);
      }
    };

    FORWARD_CURVE = new ForwardCurve(fwd);
    //YIELD_CURVE = new YieldCurve(ConstantDoublesCurve.from(RATE));

  }

  @Test(enabled = false)
  public void test() {
    final double t = 5.0;
    final int tNodes = 50;
    final int xNodes = 100;

    final MeshingFunction timeMesh = new ExponentialMeshing(0, t, tNodes, 2.0);
    final MeshingFunction spaceMesh = new HyperbolicMeshing(0, 6.0 * FORWARD_CURVE.getForward(t), FORWARD_CURVE.getSpot(), xNodes, 0.01);
    final PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);
    //TwoStateMarkovChainDensity densityCal = new TwoStateMarkovChainDensity(forward, chainData);
    final TwoStateMarkovChainWithLocalVolDensity densityCal = new TwoStateMarkovChainWithLocalVolDensity(FORWARD_CURVE, DATA, new AbsoluteLocalVolatilitySurface(ConstantDoublesSurface.from(1.0)));
    final PDEFullResults1D[] denRes = densityCal.solve(grid);
    System.out.println("Densities ");
    PDEUtilityTools.printSurface("state 1 density", denRes[0]);
    PDEUtilityTools.printSurface("state 2 density", denRes[1]);
  }

}
