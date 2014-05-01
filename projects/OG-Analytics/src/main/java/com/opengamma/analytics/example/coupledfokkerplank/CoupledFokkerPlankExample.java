/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.example.coupledfokkerplank;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.lang.text.StrBuilder;

import com.opengamma.analytics.financial.model.finitedifference.BoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.DirichletBoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.ExponentialMeshing;
import com.opengamma.analytics.financial.model.finitedifference.ExtendedCoupledFiniteDifference;
import com.opengamma.analytics.financial.model.finitedifference.ExtendedCoupledPDEDataBundle;
import com.opengamma.analytics.financial.model.finitedifference.HyperbolicMeshing;
import com.opengamma.analytics.financial.model.finitedifference.MeshingFunction;
import com.opengamma.analytics.financial.model.finitedifference.PDEFullResults1D;
import com.opengamma.analytics.financial.model.finitedifference.PDEGrid1D;
import com.opengamma.analytics.financial.model.finitedifference.PDEResults1D;
import com.opengamma.analytics.financial.model.finitedifference.applications.CoupledPDEDataBundleProvider;
import com.opengamma.analytics.financial.model.finitedifference.applications.PDEUtilityTools;
import com.opengamma.analytics.financial.model.finitedifference.applications.TwoStateMarkovChainDataBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.DoubleQuadraticInterpolator1D;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;

/**
 * Example for coupled fokker.
 */
@SuppressWarnings({"deprecation", "unused" })
public class CoupledFokkerPlankExample {
//CSOFF
  
  public static final CoupledPDEDataBundleProvider PDE_DATA_PROVIDER = new CoupledPDEDataBundleProvider();
  public static final BoundaryCondition LOWER;
  public static final BoundaryCondition UPPER;

  public static final double SPOT = 1.0;
  public static final ForwardCurve FORWARD;
  public static final double T = 5.0;
  public static final double RATE = 0.0;
  public static final double VOL1 = 0.20;
  public static final double VOL2 = 0.70;
  public static final double LAMBDA12 = 0.2;
  public static final double LAMBDA21 = 2.0;
  public static final double INITIAL_PROB_STATE1 = 1.0;
  public static final ExtendedCoupledPDEDataBundle DATA1;
  public static final ExtendedCoupledPDEDataBundle DATA2;

  public static final DoubleQuadraticInterpolator1D INTERPOLATOR_1D = new DoubleQuadraticInterpolator1D();
  public static final GridInterpolator2D GRID_INTERPOLATOR2D = new GridInterpolator2D(INTERPOLATOR_1D, INTERPOLATOR_1D);

  static {

    FORWARD = new ForwardCurve(SPOT, RATE);
    TwoStateMarkovChainDataBundle chainData = new TwoStateMarkovChainDataBundle(VOL1, VOL2, LAMBDA12, LAMBDA21, INITIAL_PROB_STATE1);
    ExtendedCoupledPDEDataBundle[] pdeData = PDE_DATA_PROVIDER.getCoupledFokkerPlankPair(FORWARD, chainData);
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

  public static void main(String[] args) throws Exception {
    runCoupledFokkerPlank(System.out);
  }

  public static void runCoupledFokkerPlank(PrintStream out) throws FileNotFoundException, IOException {
    final ExtendedCoupledFiniteDifference solver = new ExtendedCoupledFiniteDifference(0.5);
    final int tNodes = 50;
    final int xNodes = 150;

    final MeshingFunction timeMesh = new ExponentialMeshing(0, T, tNodes, 5.0);
    final MeshingFunction spaceMesh = new HyperbolicMeshing(LOWER.getLevel(), UPPER.getLevel(), SPOT, xNodes, 0.01);

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

    // output in JSON format without using a JSON library to save dependencies
    StrBuilder buf = new StrBuilder(2048).append('{');

    ByteArrayOutputStream state_1_stream = new ByteArrayOutputStream();
    PrintStream state_1_out = new PrintStream(state_1_stream, true);
    PDEUtilityTools.printSurface("State 1 density", res1, state_1_out);
    state_1_out.close();
    buf.append("\"state_1_data\":\"").append(state_1_stream.toString()).append("\",");

    ByteArrayOutputStream state_2_stream = new ByteArrayOutputStream();
    PrintStream state_2_out = new PrintStream(state_2_stream, true);
    PDEUtilityTools.printSurface("State 2 density", res2, state_2_out);
    state_2_out.close();
    buf.append("\"state_2_data\":\"").append(state_2_stream.toString()).append("\"}");

    buf.replaceAll("\t", "\\t").replaceAll("\r\n", "\\r\\n").replaceAll("\n", "\\n");
    
    out.print(buf.toString());
  }

}
