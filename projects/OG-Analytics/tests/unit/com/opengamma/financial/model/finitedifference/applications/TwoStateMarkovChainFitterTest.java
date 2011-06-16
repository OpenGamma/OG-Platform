/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finitedifference.applications;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.opengamma.financial.model.finitedifference.ExponentialMeshing;
import com.opengamma.financial.model.finitedifference.HyperbolicMeshing;
import com.opengamma.financial.model.finitedifference.MeshingFunction;
import com.opengamma.financial.model.finitedifference.PDEFullResults1D;
import com.opengamma.financial.model.finitedifference.PDEGrid1D;
import com.opengamma.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.volatility.BlackImpliedVolatilityFormula;
import com.opengamma.math.curve.ConstantDoublesCurve;
import com.opengamma.math.interpolation.DoubleQuadraticInterpolator1D;
import com.opengamma.math.interpolation.GridInterpolator2D;
import com.opengamma.math.interpolation.data.Interpolator1DDoubleQuadraticDataBundle;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.util.monitor.OperationTimer;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class TwoStateMarkovChainFitterTest {
  private static final BlackImpliedVolatilityFormula BLACK_IMPLIED_VOL = new BlackImpliedVolatilityFormula();
  //  private static final ShepardInterpolatorND INTERPOLATOR = new ShepardInterpolatorND(2.0);
  private static final DoubleQuadraticInterpolator1D INTERPOLATOR_1D = new DoubleQuadraticInterpolator1D();
  private static final GridInterpolator2D<Interpolator1DDoubleQuadraticDataBundle, Interpolator1DDoubleQuadraticDataBundle> GRID_INTERPOLATOR2D = new GridInterpolator2D<Interpolator1DDoubleQuadraticDataBundle, Interpolator1DDoubleQuadraticDataBundle>(
      INTERPOLATOR_1D,
      INTERPOLATOR_1D);

  private static final double SPOT = 1.0;
  private static final double RATE = 0.0;
  private static final List<double[]> EXPIRY_AND_STRIKES = new ArrayList<double[]>();
  private static final double VOL1 = 0.15;
  private static final double DELTA_VOL = 0.55;
  private static final double LAMBDA12 = 0.2;
  private static final double LAMBDA21 = 2.5;
  private static final double P0 = 1.0;//1.0;
  private static final double BETA1 = 0.5;
  private static final double BETA2 = 0.5;
  private static final ForwardCurve FORWARD_CURVE;
  private static final YieldCurve YIELD_CURVE;
  // private static final InterpolatorNDDataBundle DATABUNDLE;
  private static final PDEFullResults1D PDE_RESULTS;
  private static final Map<DoublesPair, Double> DATA;
  private static Map<Double, Interpolator1DDoubleQuadraticDataBundle> DATABUNDLE;

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

    FORWARD_CURVE = new ForwardCurve(SPOT);
    YIELD_CURVE = new YieldCurve(ConstantDoublesCurve.from(RATE));

    TwoStateMarkovChainPricer mc = new TwoStateMarkovChainPricer(FORWARD_CURVE, VOL1, VOL1 + DELTA_VOL, LAMBDA12, LAMBDA21, P0, BETA1, BETA2);

    int tNodes = 20;
    int xNodes = 100;
    MeshingFunction timeMesh = new ExponentialMeshing(0, 5, tNodes, 7.5);
    MeshingFunction spaceMesh = new HyperbolicMeshing(0, 6 * SPOT, SPOT, xNodes, 0.01);
    PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);
    PDE_RESULTS = mc.solve(grid);

    //DATABUNDLE = INTERPOLATOR.getDataBundle(transformData(res));
    DATA = transformData2(PDE_RESULTS);
    DATABUNDLE = GRID_INTERPOLATOR2D.getDataBundle(DATA);

    MARKET_VOLS = new ArrayList<Pair<double[], Double>>(EXPIRY_AND_STRIKES.size());
    for (int i = 0; i < EXPIRY_AND_STRIKES.size(); i++) {
      double[] tk = EXPIRY_AND_STRIKES.get(i);
      Pair<double[], Double> pair = new ObjectsPair<double[], Double>(tk, GRID_INTERPOLATOR2D.interpolate(DATABUNDLE, new DoublesPair(tk[0],
          tk[1])));
      MARKET_VOLS.add(pair);
    }
  }

  @Test
  public void timeTest() {
    int warmups = 3;
    int benchmarkCycles = 10;
    final Logger logger = LoggerFactory.getLogger(TwoStateMarkovChainFitterTest.class);

    TwoStateMarkovChainPricer mc = new TwoStateMarkovChainPricer(FORWARD_CURVE, VOL1, VOL1 + DELTA_VOL, LAMBDA12, LAMBDA21, P0, BETA1, BETA2);

    int tNodes = 20;
    int xNodes = 100;
    MeshingFunction timeMesh = new ExponentialMeshing(0, 5, tNodes, 5.0);
    MeshingFunction spaceMesh = new HyperbolicMeshing(0, 6 * SPOT, SPOT, xNodes, 0.01);
    PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);

    for (int i = 0; i < warmups; i++) {
      PDEFullResults1D res = mc.solve(grid);
    }
    if (benchmarkCycles > 0) {
      final OperationTimer timer = new OperationTimer(logger, "processing {} cycles on timeTest", benchmarkCycles);
      for (int i = 0; i < benchmarkCycles; i++) {
        PDEFullResults1D res = mc.solve(grid);
      }
      timer.finished();
    }

  }

  @Test
  public void dumpPDESurfaceTest() {
    int xNodes = PDE_RESULTS.getNumberSpaceNodes();
    int tNodes = PDE_RESULTS.getNumberTimeNodes();

    for (int i = 0; i < xNodes; i++) {
      System.out.print("\t" + PDE_RESULTS.getSpaceValue(i));
    }
    System.out.print("\n");

    double t, k;

    for (int j = 0; j < tNodes; j++) {
      t = PDE_RESULTS.getTimeValue(j);
      double df = YIELD_CURVE.getDiscountFactor(t);
      BlackFunctionData data = new BlackFunctionData(SPOT / df, df, 0.0);

      System.out.print(t);

      for (int i = 0; i < xNodes; i++) {
        k = PDE_RESULTS.getSpaceValue(i);
        double price = PDE_RESULTS.getFunctionValue(i, j);
        double impVol;
        EuropeanVanillaOption option = new EuropeanVanillaOption(k, t, true);
        try {
          impVol = BLACK_IMPLIED_VOL.getImpliedVolatility(data, option, price);
        } catch (Exception e) {
          impVol = 0.0;
        }
        System.out.print("\t" + impVol);
      }
      System.out.print("\n");
    }
    System.out.print("\n");
  }

  @Test
  public void dumpSurfaceTest() {

    for (int i = 0; i < 101; i++) {
      double k = SPOT / 4.0 + 4.0 * SPOT * i / 100.;
      System.out.print("\t" + k);
    }
    System.out.print("\n");

    for (int j = 0; j < 101; j++) {
      double t = 0.2 + 4.8 * j / 100.;
      System.out.print(t);
      for (int i = 0; i < 101; i++) {
        double k = SPOT / 4.0 + 4.0 * SPOT * i / 100.;
        // System.out.print("\t" + INTERPOLATOR.interpolate(DATABUNDLE, new double[] {t, k }));
        System.out.print("\t" + GRID_INTERPOLATOR2D.interpolate(DATABUNDLE, new DoublesPair(t, k)));
      }
      System.out.print("\n");
    }
  }

  @Test
  public void test() {
    DoubleMatrix1D initalGuess = new DoubleMatrix1D(new double[] {0.2, 0.8, 0.3, 2.0, 0.9, 0.9 });
    TwoStateMarkovChainFitter fitter = new TwoStateMarkovChainFitter();
    LeastSquareResults res = fitter.fit(FORWARD_CURVE, MARKET_VOLS, initalGuess);
    System.out.println("chi^2:" + res.getChiSq() + "\n params: " + res.getParameters().toString());
  }

  private static List<Pair<double[], Double>> transformData(final PDEFullResults1D prices) {
    int xNodes = prices.getNumberSpaceNodes();
    int tNodes = prices.getNumberTimeNodes();
    int n = xNodes * tNodes;
    List<Pair<double[], Double>> out = new ArrayList<Pair<double[], Double>>(n);
    BlackFunctionData data = new BlackFunctionData(SPOT, 1.0, 0);
    for (int i = 0; i < tNodes; i++) {
      for (int j = 0; j < xNodes; j++) {
        double price = prices.getFunctionValue(j, i);
        EuropeanVanillaOption option = new EuropeanVanillaOption(prices.getSpaceValue(j), prices.getTimeValue(i), true);
        try {
          double impVol = BLACK_IMPLIED_VOL.getImpliedVolatility(data, option, price);
          if (Math.abs(impVol) > 1e-15 && Math.abs(impVol) < 1.0) {
            Pair<double[], Double> pair = new ObjectsPair<double[], Double>(
                new double[] {prices.getTimeValue(i), prices.getSpaceValue(j) }, impVol);
            out.add(pair);
          }
        } catch (Exception e) {
          System.out.println("Test: can't find vol for strike: " + prices.getSpaceValue(j) + " and expiry " + prices.getTimeValue(i) + " . Not added to data set");
        }
      }
    }
    return out;
  }

  private static Map<DoublesPair, Double> transformData2(final PDEFullResults1D prices) {
    int xNodes = prices.getNumberSpaceNodes();
    int tNodes = prices.getNumberTimeNodes();
    int n = xNodes * tNodes;
    Map<DoublesPair, Double> out = new HashMap<DoublesPair, Double>(n);
    BlackFunctionData data = new BlackFunctionData(SPOT, 1.0, 0);
    int count = 0;
    for (int i = 0; i < tNodes; i++) {
      for (int j = 0; j < xNodes; j++) {
        double price = prices.getFunctionValue(j, i);
        EuropeanVanillaOption option = new EuropeanVanillaOption(prices.getSpaceValue(j), prices.getTimeValue(i), true);
        double impVol = 0.0;
        try {
          impVol = BLACK_IMPLIED_VOL.getImpliedVolatility(data, option, price);
          if (Math.abs(impVol) > 1e-15 && Math.abs(impVol) < 1.0) {
            DoublesPair pair = new DoublesPair(prices.getTimeValue(i), prices.getSpaceValue(j));
            out.put(pair, impVol);
          }
        } catch (Exception e) {
          count++;
          //System.out.println("Test: can't find vol for strike: " + prices.getSpaceValue(j) + " and expiry " + prices.getTimeValue(i) + " . Not added to data set");
        }
      }
    }
    if (count > 0) {
      System.err.println("TEST " + count + " out of " + xNodes * tNodes + " data points removed");
    }
    return out;
  }

}
