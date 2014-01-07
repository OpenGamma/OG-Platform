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
import com.opengamma.analytics.financial.model.finitedifference.MarkovChain;
import com.opengamma.analytics.financial.model.finitedifference.MeshingFunction;
import com.opengamma.analytics.financial.model.finitedifference.PDEFullResults1D;
import com.opengamma.analytics.financial.model.finitedifference.PDEGrid1D;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class TwoStateMarkovChainPricerTest {

  private static final double SPOT = 0.03;
  private static final double RATE = 0.0;
  private static final double T = 5.0;

  private static final double VOL1 = 0.15;
  private static final double VOL2 = 0.70;
  private static final double LAMBDA12 = 0.3;
  private static final double LAMBDA21 = 4.0;
  private static final double P0 = 1.0;
  private static final double BETA = 0.6;

  private static final ForwardCurve FORWARD_CURVE;

  private static final TwoStateMarkovChainPricer PRICER;
  private static final MarkovChain CHAIN;

  static {
    FORWARD_CURVE = new ForwardCurve(SPOT, RATE);
    final TwoStateMarkovChainDataBundle chainData = new TwoStateMarkovChainDataBundle(VOL1, VOL2, LAMBDA12, LAMBDA21, P0, BETA, BETA);
    PRICER = new TwoStateMarkovChainPricer(FORWARD_CURVE, chainData);
    CHAIN = new MarkovChain(VOL1, VOL2, LAMBDA12, LAMBDA21, P0);
  }

  @Test
  public void test() {
    final double theta = 0.55;
    final int tNodes = 51;
    final int xNodes = 151;
    final MeshingFunction timeMesh = new ExponentialMeshing(0, T, tNodes, 7.5);
    final MeshingFunction spaceMesh = new HyperbolicMeshing(0, 10 * SPOT, SPOT, xNodes, 0.01);
    final PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);
    final PDEFullResults1D res = PRICER.solve(grid, theta);

    final double[] expiries = timeMesh.getPoints();
    final double[] strikes = spaceMesh.getPoints();
    final double[] forwards = new double[tNodes];
    // double[] df = new double[tNodes];
    for (int i = 0; i < tNodes; i++) {

      forwards[i] = FORWARD_CURVE.getForward(expiries[i]);
    }

    final double[] sims = CHAIN.simulate(T, 1000);
    for (int i = 0; i < xNodes; i++) {

      if (strikes[i] < 0.08) {
        final double mcPrice = CHAIN.priceCEV(FORWARD_CURVE.getForward(T), FORWARD_CURVE.getSpot() / FORWARD_CURVE.getForward(T), strikes[i], T, BETA, sims);
        final double price = res.getFunctionValue(i, tNodes - 1);
        // System.out.println(strikes[i] + "\t" + mcPrice + "\t" + price);
        assertEquals(mcPrice, price, 1e-2 * mcPrice);
      }

    }

  }

}
