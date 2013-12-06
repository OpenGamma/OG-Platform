/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference.applications;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.finitedifference.ExponentialMeshing;
import com.opengamma.analytics.financial.model.finitedifference.HyperbolicMeshing;
import com.opengamma.analytics.financial.model.finitedifference.MeshingFunction;
import com.opengamma.analytics.financial.model.finitedifference.PDEFullResults1D;
import com.opengamma.analytics.financial.model.finitedifference.PDEGrid1D;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class TwoStateMarkovChainDensityTest {

  private static final ForwardCurve FORWARD;
  private static final TwoStateMarkovChainDataBundle DATA;
  private static final TwoStateMarkovChainDensity DENSITY_CAL;
  private static final double T = 2.0;

  static {
    double rate = 0.05;
    double spot = 1.0;
    FORWARD = new ForwardCurve(spot, rate);

    double vol1 = 0.4;
    double vol2 = 0.8;
    double lambda12 = 0.2;
    double lambda21 = 2.0;
    double p0 = 0.9;
    double beta1 = 1.0;
    double beta2 = 0.0;

    DATA = new TwoStateMarkovChainDataBundle(vol1, vol2, lambda12, lambda21, p0, beta1, beta2);
    DENSITY_CAL = new TwoStateMarkovChainDensity(FORWARD, DATA);
  }

  @Test(enabled = false)
  public void test() {
    int tNodes = 100;
    int xNodes = 200;

    MeshingFunction timeMesh = new ExponentialMeshing(0, T, tNodes, 5.0);
    //MeshingFunction spaceMesh = new ExponentialMeshing(0.0, 6.0 * FORWARD.getForward(T), xNodes, 3.0);
    MeshingFunction spaceMesh = new HyperbolicMeshing(-0.0 * FORWARD.getForward(T), 6.0 * FORWARD.getForward(T), FORWARD.getSpot(), xNodes, 0.01);

    PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);

    PDEFullResults1D[] res = DENSITY_CAL.solve(grid);

    PDEUtilityTools.printSurface("state 1 density", res[0]);
    PDEUtilityTools.printSurface("state 2 density", res[1]);
  }

  @Test
  public void degenerateTest() {
    int tNodes = 20;
    int xNodes = 100;
    MeshingFunction timeMesh = new ExponentialMeshing(0, T, tNodes, 5.0);
    MeshingFunction spaceMesh = new HyperbolicMeshing(-0.0 * FORWARD.getForward(T), 6.0 * FORWARD.getForward(T), FORWARD.getSpot(), xNodes, 0.01);
    PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);

    double l12 = 0.3;
    double l21 = 2.0;
    double pi1 = l21 / (l12 + l21);

    TwoStateMarkovChainDataBundle data = new TwoStateMarkovChainDataBundle(0.2, 0.2, l12, l21, pi1, 0.5, 0.5);
    TwoStateMarkovChainDensity cal = new TwoStateMarkovChainDensity(FORWARD, data);
    PDEFullResults1D[] res = cal.solve(grid);
    for (int i = 0; i < xNodes; i++) {
      assertEquals(res[0].getFunctionValue(i, tNodes - 1) / pi1, res[1].getFunctionValue(i, tNodes - 1) / (1 - pi1), 1e-6);
    }

  }
}
