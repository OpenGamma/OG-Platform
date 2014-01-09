/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference.applications;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.finitedifference.ExponentialMeshing;
import com.opengamma.analytics.financial.model.finitedifference.HyperbolicMeshing;
import com.opengamma.analytics.financial.model.finitedifference.MeshingFunction;
import com.opengamma.analytics.financial.model.finitedifference.PDEFullResults1D;
import com.opengamma.analytics.financial.model.finitedifference.PDEGrid1D;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.statistics.leastsquare.LeastSquareResultsWithTransform;
import com.opengamma.util.monitor.OperationTimer;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class TwoStateMarkovChainFitterTest {
  //TODO just put this in to stop failures
  private static final Interpolator1D INTERPOLATOR_1D =
      CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.FLAT_EXTRAPOLATOR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final GridInterpolator2D GRID_INTERPOLATOR2D = new GridInterpolator2D(INTERPOLATOR_1D, INTERPOLATOR_1D);

  private static final double THETA = 0.55;

  private static final double SPOT = 1.0;
  private static final double RATE = 0.0;
  private static final List<double[]> EXPIRY_AND_STRIKES = new ArrayList<>();
  private static final double VOL1 = 0.15;
  private static final double DELTA_VOL = 0.55;
  private static final double LAMBDA12 = 0.2;
  private static final double LAMBDA21 = 2.5;
  private static final double P0 = 1.0;
  private static final double BETA = 0.5;

  private static final ForwardCurve FORWARD_CURVE;
  private static final TwoStateMarkovChainDataBundle MARKOV_CHAIN_DATA;
  private static final PDEFullResults1D PDE_RESULTS;
  private static final Map<DoublesPair, Double> DATA;
  private static Map<Double, Interpolator1DDataBundle> DATABUNDLE;

  private static final List<Pair<double[], Double>> MARKET_VOLS;

  static {
    EXPIRY_AND_STRIKES.add(new double[] {1. / 12, 0.925 });
    EXPIRY_AND_STRIKES.add(new double[] {1. / 12, 1.0 });
    EXPIRY_AND_STRIKES.add(new double[] {1. / 12, 1.075 });
    EXPIRY_AND_STRIKES.add(new double[] {0.5, 0.9 });
    EXPIRY_AND_STRIKES.add(new double[] {0.5, 0.95 });
    EXPIRY_AND_STRIKES.add(new double[] {0.5, 1.0 });
    EXPIRY_AND_STRIKES.add(new double[] {0.5, 1.05 });
    EXPIRY_AND_STRIKES.add(new double[] {0.5, 1.1 });
    EXPIRY_AND_STRIKES.add(new double[] {1, 0.8 });
    EXPIRY_AND_STRIKES.add(new double[] {1, 0.9 });
    EXPIRY_AND_STRIKES.add(new double[] {1, 1.0 });
    EXPIRY_AND_STRIKES.add(new double[] {1, 1.1 });
    EXPIRY_AND_STRIKES.add(new double[] {2, 0.7 });
    EXPIRY_AND_STRIKES.add(new double[] {2, 0.9 });
    EXPIRY_AND_STRIKES.add(new double[] {2, 1.0 });
    EXPIRY_AND_STRIKES.add(new double[] {2, 1.2 });
    EXPIRY_AND_STRIKES.add(new double[] {2, 1.4 });
    EXPIRY_AND_STRIKES.add(new double[] {3, 1.0 });
    EXPIRY_AND_STRIKES.add(new double[] {4, 1.0 });
    EXPIRY_AND_STRIKES.add(new double[] {5, 0.5 });
    EXPIRY_AND_STRIKES.add(new double[] {5, 0.75 });
    EXPIRY_AND_STRIKES.add(new double[] {5, 0.9 });
    EXPIRY_AND_STRIKES.add(new double[] {5, 1.0 });
    EXPIRY_AND_STRIKES.add(new double[] {5, 1.1 });
    EXPIRY_AND_STRIKES.add(new double[] {5, 1.3 });
    EXPIRY_AND_STRIKES.add(new double[] {5, 1.7 });
    EXPIRY_AND_STRIKES.add(new double[] {5, 2.0 });

    FORWARD_CURVE = new ForwardCurve(SPOT, RATE);
    MARKOV_CHAIN_DATA = new TwoStateMarkovChainDataBundle(VOL1, VOL1 + DELTA_VOL, LAMBDA12, LAMBDA21, P0, BETA, BETA);

    final TwoStateMarkovChainPricer mc = new TwoStateMarkovChainPricer(FORWARD_CURVE, MARKOV_CHAIN_DATA);

    final int tNodes = 20;
    final int xNodes = 100;
    final MeshingFunction timeMesh = new ExponentialMeshing(0, 5, tNodes, 5.0);
    final MeshingFunction spaceMesh = new HyperbolicMeshing(0, 10 * SPOT, SPOT, xNodes, 0.01);
    final PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);
    PDE_RESULTS = mc.solve(grid, THETA);

    DATA = PDEUtilityTools.priceToImpliedVol(FORWARD_CURVE, PDE_RESULTS, 0.01, 5.0, SPOT / 10, 6 * SPOT, true);
    DATABUNDLE = GRID_INTERPOLATOR2D.getDataBundle(DATA);

    MARKET_VOLS = new ArrayList<>(EXPIRY_AND_STRIKES.size());
    for (int i = 0; i < EXPIRY_AND_STRIKES.size(); i++) {
      final double[] tk = EXPIRY_AND_STRIKES.get(i);
      final Pair<double[], Double> pair = Pairs.of(tk, GRID_INTERPOLATOR2D.interpolate(DATABUNDLE, DoublesPair.of(tk[0], tk[1])));
      MARKET_VOLS.add(pair);
    }
  }

  @Test(enabled = false)
  public void timeTest() {
    final int warmups = 3;
    final int benchmarkCycles = 10;
    final Logger logger = LoggerFactory.getLogger(TwoStateMarkovChainFitterTest.class);

    final TwoStateMarkovChainPricer mc = new TwoStateMarkovChainPricer(FORWARD_CURVE, MARKOV_CHAIN_DATA);

    final int tNodes = 20;
    final int xNodes = 100;
    final MeshingFunction timeMesh = new ExponentialMeshing(0, 5, tNodes, 5.0);
    final MeshingFunction spaceMesh = new HyperbolicMeshing(0, 6 * SPOT, SPOT, xNodes, 0.01);
    final PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);

    for (int i = 0; i < warmups; i++) {
      mc.solve(grid, 0.5);
    }
    if (benchmarkCycles > 0) {
      final OperationTimer timer = new OperationTimer(logger, "processing {} cycles on timeTest", benchmarkCycles);
      for (int i = 0; i < benchmarkCycles; i++) {
        mc.solve(grid, 0.5);
      }
      timer.finished();
    }

  }

  @Test(enabled = false)
  public void dumpPDESurfaceTest() {
    PDEUtilityTools.printSurface("dumpPDESurfaceTest", PDE_RESULTS);
  }

  @Test(enabled = false)
  public void dumpSurfaceTest() {
    PDEUtilityTools.printSurface("dumpSurfaceTest", DATABUNDLE, 0, 5, SPOT / 10, SPOT * 6, 100, 100);
  }

  @Test(enabled = false)
  public void test() {
    final DoubleMatrix1D initalGuess = new DoubleMatrix1D(new double[] {0.2, 0.8, 0.3, 2.0, 0.9, 0.9 });
    final TwoStateMarkovChainFitter fitter = new TwoStateMarkovChainFitter(THETA);
    final LeastSquareResultsWithTransform res = fitter.fit(FORWARD_CURVE, MARKET_VOLS, initalGuess);
    //System.out.println("chi^2:" + res.getChiSq() + "\n params: " + res.getParameters().toString());
    final double[] modelParms = res.getModelParameters().getData();

    assertEquals(0.0, res.getChiSq(), 1e-3);
    assertEquals(VOL1, modelParms[0], 1e-3);
    assertEquals(DELTA_VOL, modelParms[1], 2e-3);
    assertEquals(LAMBDA12, modelParms[2], 2e-3);
    assertEquals(LAMBDA21, modelParms[3], 2e-3);
    assertEquals(P0, modelParms[4], 1e-3);
    assertEquals(BETA, modelParms[5], 1e-3);
  }
}
