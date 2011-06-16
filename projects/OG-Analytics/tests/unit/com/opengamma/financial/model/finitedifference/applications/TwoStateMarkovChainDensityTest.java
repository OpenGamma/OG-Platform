/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finitedifference.applications;

import org.testng.annotations.Test;

import com.opengamma.financial.model.finitedifference.ExponentialMeshing;
import com.opengamma.financial.model.finitedifference.HyperbolicMeshing;
import com.opengamma.financial.model.finitedifference.MeshingFunction;
import com.opengamma.financial.model.finitedifference.PDEFullResults1D;
import com.opengamma.financial.model.finitedifference.PDEGrid1D;
import com.opengamma.financial.model.interestrate.curve.ForwardCurve;

/**
 * 
 */
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
    double lambda12 = 0.0;
    double lambda21 = 2.0;
    double p0 = 1.0;
    double beta1 = 1.0;
    double beta2 = 1.0;

    DATA = new TwoStateMarkovChainDataBundle(vol1, vol2, lambda12, lambda21, p0, beta1, beta2);

    DENSITY_CAL = new TwoStateMarkovChainDensity(FORWARD, DATA);
  }

  @Test
  public void test() {
    int tNodes = 100;
    int xNodes = 200;

    MeshingFunction timeMesh = new ExponentialMeshing(0, T, tNodes, 5.0);
    //MeshingFunction spaceMesh = new ExponentialMeshing(0.0, 6.0 * FORWARD.getForward(T), xNodes, 3.0);
    MeshingFunction spaceMesh = new HyperbolicMeshing(-0.0 * FORWARD.getForward(T), 6.0 * FORWARD.getForward(T), FORWARD.getSpot(), xNodes, 0.01);

    PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);

    PDEFullResults1D[] res = DENSITY_CAL.solve(grid);

    printResults(res[0]);
    // printResults(res[1]);
  }

  private PDEFullResults1D reflect(PDEFullResults1D in) {
    int xNodes = in.getNumberSpaceNodes();
    int tNodes = in.getNumberTimeNodes();

    return null;
  }

  private void printResults(PDEFullResults1D results) {
    int xNodes = results.getNumberSpaceNodes();
    int tNodes = results.getNumberTimeNodes();

    for (int i = 0; i < xNodes; i++) {
      System.out.print("\t" + results.getSpaceValue(i));
    }
    System.out.print("\n");

    for (int j = 0; j < tNodes; j++) {
      System.out.print(results.getTimeValue(j));
      for (int i = 0; i < xNodes; i++) {
        System.out.print("\t" + results.getFunctionValue(i, j));
      }
      System.out.print("\n");
    }
    System.out.print("\n");
  }

}
