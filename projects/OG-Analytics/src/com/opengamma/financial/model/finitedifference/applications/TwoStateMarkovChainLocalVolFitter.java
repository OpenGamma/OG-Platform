/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finitedifference.applications;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.opengamma.financial.model.finitedifference.ExponentialMeshing;
import com.opengamma.financial.model.finitedifference.HyperbolicMeshing;
import com.opengamma.financial.model.finitedifference.MeshingFunction;
import com.opengamma.financial.model.finitedifference.PDEFullResults1D;
import com.opengamma.financial.model.finitedifference.PDEGrid1D;
import com.opengamma.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.financial.model.volatility.surface.BlackVolatilitySurface;
import com.opengamma.financial.model.volatility.surface.DupireLocalVolatilityCalculator;
import com.opengamma.financial.model.volatility.surface.LocalVolatilitySurface;
import com.opengamma.math.function.Function;
import com.opengamma.math.interpolation.DoubleQuadraticInterpolator1D;
import com.opengamma.math.interpolation.GridInterpolator2D;
import com.opengamma.math.interpolation.data.Interpolator1DDoubleQuadraticDataBundle;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.math.surface.FunctionalDoublesSurface;
import com.opengamma.math.surface.Surface;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;

/**
 * AKA Poor man's LSV 
 */
public class TwoStateMarkovChainLocalVolFitter {
  private static final DoubleQuadraticInterpolator1D INTERPOLATOR_1D = new DoubleQuadraticInterpolator1D();
  private static final GridInterpolator2D<Interpolator1DDoubleQuadraticDataBundle, Interpolator1DDoubleQuadraticDataBundle> GRID_INTERPOLATOR2D = new GridInterpolator2D<Interpolator1DDoubleQuadraticDataBundle, Interpolator1DDoubleQuadraticDataBundle>(
      INTERPOLATOR_1D,
      INTERPOLATOR_1D);
  private static final TwoStateMarkovChainFitter CHAIN_FITTER = new TwoStateMarkovChainFitter();
  private static final DupireLocalVolatilityCalculator LOCAL_VOL_CALC = new DupireLocalVolatilityCalculator();
  private static final TwoStateMarkovChainLocalVolCalculator MC_LOCAL_VOL_CALC = new TwoStateMarkovChainLocalVolCalculator();

  /**
   * THIS ONLY WORKS WITH CONSTANT RATE!!!!!!!!!!!!!!
   * @param spot
   * @param drift
   * @param discountCurve
   * @param marketVols
   */
  public void fit(final ForwardCurve forward, final BlackVolatilitySurface marketVolSurface, final List<Pair<double[], Double>> marketVols, DoubleMatrix1D initalGuess) {

    Map<DoublesPair, Double> marketVolsMap = convertFormatt(marketVols);
    Set<DoublesPair> dataPoints = marketVolsMap.keySet();

    // first fit calibrate the basic Markov chain model (i.e. fixed vol levels) to marktet data
    LeastSquareResults chiSqRes = CHAIN_FITTER.fit(forward, marketVols, initalGuess);

    double vol1 = chiSqRes.getParameters().getEntry(0);
    double vol2 = chiSqRes.getParameters().getEntry(1) + vol1;
    double lambda12 = chiSqRes.getParameters().getEntry(2);
    double lambda21 = chiSqRes.getParameters().getEntry(3);
    double p0 = chiSqRes.getParameters().getEntry(4);
    double beta = chiSqRes.getParameters().getEntry(5);
    //    double vol1 = initalGuess.getEntry(0);
    //    double vol2 = initalGuess.getEntry(1) + vol1;
    //    double lambda12 = initalGuess.getEntry(2);
    //    double lambda21 = initalGuess.getEntry(3);
    //    double p0 = initalGuess.getEntry(4);
    //    double beta = initalGuess.getEntry(5);

    TwoStateMarkovChainDataBundle chainData = new TwoStateMarkovChainDataBundle(vol1, vol2, lambda12, lambda21, p0, beta, beta);

    //interpolate the market vol surface 
    final Map<Double, Interpolator1DDoubleQuadraticDataBundle> dataBundle = GRID_INTERPOLATOR2D.getDataBundle(marketVolsMap);
    Function<Double, Double> mrkVolFunc = new Function<Double, Double>() {
      @Override
      public Double evaluate(Double... ts) {
        double t = ts[0];
        double s = ts[1];
        return GRID_INTERPOLATOR2D.interpolate(dataBundle, new DoublesPair(t, s));
      }
    };

    int tNodes = 50;
    int xNodes = 100;
    int nMarketValues = marketVols.size();
    double tminT = Double.POSITIVE_INFINITY;
    double tminK = Double.POSITIVE_INFINITY;
    double tmaxT = 0;
    double tmaxK = 0;

    for (int i = 0; i < nMarketValues; i++) {
      double[] tk = marketVols.get(i).getFirst();

      if (tk[0] > tmaxT) {
        tmaxT = tk[0];
      }
      if (tk[0] < tminT) {
        tminT = tk[0];
      }
      if (tk[1] > tmaxK) {
        tmaxK = tk[1];
      }
      if (tk[1] < tminK) {
        tminK = tk[1];
      }
    }

    final double minT = 0.6 * tminT;
    final double minK = 0.9 * tminK;
    final double maxT = 1.0 * tmaxT;
    final double maxK = 1.1 * tmaxK;

    //TODO have InterpolatedDoublesSurface use data bundles 
    // BlackVolatilitySurface marketVolSurface = new BlackVolatilitySurface(FunctionalDoublesSurface.from(mrkVolFunc));

    //get the market local vol surface
    //TODO local vol with non-constant (but deterministic) rates
    LocalVolatilitySurface marketLocalVol = LOCAL_VOL_CALC.getLocalVolatility(marketVolSurface, forward.getSpot(),
        forward.getDrift(0));

    System.out.println("Market Vols");
    printSurface(marketVolSurface.getSurface(), minT, maxT, minK, maxK);
    System.out.println("Market local Vols");
    printSurface(marketLocalVol.getSurface(), minT, maxT, minK, maxK);

    //get the local vol of basic Markov chain model

    MeshingFunction timeMesh = new ExponentialMeshing(0, tmaxT, tNodes, 5.0);
    MeshingFunction spaceMesh = new HyperbolicMeshing(0, 6.0 * forward.getForward(tmaxT), forward.getForward(0), xNodes, 0.01);
    final PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);
    TwoStateMarkovChainDensity densityCal = new TwoStateMarkovChainDensity(forward, chainData);
    PDEFullResults1D[] denRes = densityCal.solve(grid);
    System.out.println("Densities ");
    printSurface(denRes[0]);
    printSurface(denRes[1]);

    //the Local vol of the basis Markov chain model 
    LocalVolatilitySurface mcLocalVol = MC_LOCAL_VOL_CALC.calc(denRes, chainData, null);
    System.out.println("Markov local Vols");
    printSurface(mcLocalVol.getSurface(), minT, maxT, minK, maxK);

    //    PDEFullResults1D mcLocalVol = getLocalVol(denRes, chainData);
    //    printSurface(mcLocalVol);

    //    Map<DoublesPair, Double> lvoData = getLocalVolOverlay(marketLocalVol, denRes, chainData, null);
    //    Map<Double, Interpolator1DDoubleQuadraticDataBundle> lvoDataBundle = GRID_INTERPOLATOR2D.getDataBundle(lvoData);
    //    final Map<Double, Interpolator1DDoubleQuadraticDataBundle> tempDataBundle = lvoDataBundle;
    //
    //    printSurface(lvoDataBundle, minT, maxT, minK, maxK);
    //
    //    //while local vol of Markov chain model (with local vol overlay) not equal to market local vol
    //    Function<Double, Double> lvOverlayFunc = new Function<Double, Double>() {
    //      @Override
    //      public Double evaluate(Double... ts) {
    //        double t = ts[0];
    //        double s = ts[1];
    //        return GRID_INTERPOLATOR2D.interpolate(tempDataBundle, new DoublesPair(t, s));
    //      }
    //    };
    //    LocalVolatilitySurface lvOverlay = new LocalVolatilitySurface(FunctionalDoublesSurface.from(lvOverlayFunc));

    // get the local vol of  Markov chain model (with local vol overlay)
    LocalVolatilitySurface lvOverlay = null;
    int count = 0;
    while (!converged(marketLocalVol, mcLocalVol, dataPoints) && count < 5) {
      count++;
      //get the local vol overlay to the Markov chain model 
      lvOverlay = getLocalVolOverlay(marketLocalVol, mcLocalVol);

      //debug
      //lvOverlay = new LocalVolatilitySurface(ConstantDoublesSurface.from(1.0));

      System.out.println("OverLay " + count);
      printSurface(lvOverlay.getSurface(), minT, maxT, minK, maxK);

      TwoStateMarkovChainWithLocalVolDensity lvDensityCal = new TwoStateMarkovChainWithLocalVolDensity(forward, chainData, lvOverlay);
      denRes = lvDensityCal.solve(grid);
      System.out.println("Densities " + count);
      printSurface(denRes[0]);
      printSurface(denRes[1]);

      //calculate mc local vol without overlay
      mcLocalVol = MC_LOCAL_VOL_CALC.calc(denRes, chainData, null);

      System.out.println("Markov LV local Vols " + count);
      printSurface(mcLocalVol.getSurface(), minT, maxT, minK, maxK);
    }
    System.out.println("Markov LV local Vols");
    printSurface(mcLocalVol.getSurface(), minT, maxT, minK, maxK);
    System.out.println("OverLay");
    printSurface(lvOverlay.getSurface(), minT, maxT, minK, maxK);

    //end while

  }

  private boolean converged(LocalVolatilitySurface mrkLV, LocalVolatilitySurface modLV, Set<DoublesPair> dataPoints) {
    double error = 0.0;
    Iterator<DoublesPair> interator = dataPoints.iterator();
    while (interator.hasNext()) {
      DoublesPair point = interator.next();
      double temp = mrkLV.getVolatility(point) - modLV.getVolatility(point);
      error += temp * temp;
    }
    return error < 1e-2; //TODO arbitrary error 
  }

  private void printSurfaceInterpolate(PDEFullResults1D res) {

    int tNodes = res.getNumberTimeNodes();
    int xNodes = res.getNumberSpaceNodes();

    int n = xNodes * tNodes;
    Map<DoublesPair, Double> out = new HashMap<DoublesPair, Double>(n);

    for (int i = 0; i < tNodes; i++) {
      double t = res.getTimeValue(i);

      for (int j = 0; j < xNodes; j++) {
        double k = res.getSpaceValue(j);
        DoublesPair tk = new DoublesPair(t, k);
        out.put(tk, res.getFunctionValue(j, i));
      }
    }

    Map<Double, Interpolator1DDoubleQuadraticDataBundle> dataBundle = GRID_INTERPOLATOR2D.getDataBundle(out);
    double tMin = res.getTimeValue(0);
    double tMax = res.getTimeValue(res.getNumberTimeNodes() - 1);
    double kMin = res.getSpaceValue(0);
    double kMax = res.getSpaceValue(res.getNumberSpaceNodes() - 1);
    printSurface(dataBundle, tMin, tMax, kMin, kMax);
  }

  private void printSurface(PDEFullResults1D res) {
    int tNodes = res.getNumberTimeNodes();
    int xNodes = res.getNumberSpaceNodes();

    for (int i = 0; i < xNodes; i++) {
      double k = res.getSpaceValue(i);
      System.out.print("\t" + k);
    }
    System.out.print("\n");

    for (int j = 0; j < tNodes; j++) {
      double t = res.getTimeValue(j);
      System.out.print(t);
      for (int i = 0; i < xNodes; i++) {
        System.out.print("\t" + res.getFunctionValue(i, j));
      }
      System.out.print("\n");
    }
  }

  private void printSurface(Map<Double, Interpolator1DDoubleQuadraticDataBundle> dataBundle, double tMin, double tMax, double kMin, double kMax) {

    for (int i = 0; i < 101; i++) {
      double k = kMin + (kMax - kMin) * i / 100.;
      System.out.print("\t" + k);
    }
    System.out.print("\n");

    for (int j = 0; j < 101; j++) {
      double t = tMin + (tMax - tMin) * j / 100.;
      System.out.print(t);
      for (int i = 0; i < 101; i++) {
        double k = kMin + (kMax - kMin) * i / 100.;
        DoublesPair tk = new DoublesPair(t, k);

        System.out.print("\t" + GRID_INTERPOLATOR2D.interpolate(dataBundle, tk));
      }
      System.out.print("\n");
    }

  }

  private void printSurface(Surface<Double, Double, Double> surface, double tMin, double tMax, double kMin, double kMax) {

    System.out.print("\n");
    for (int i = 0; i < 101; i++) {
      double k = kMin + (kMax - kMin) * i / 100.;
      System.out.print("\t" + k);
    }
    System.out.print("\n");

    for (int j = 0; j < 101; j++) {
      double t = tMin + (tMax - tMin) * j / 100.;
      System.out.print(t);
      for (int i = 0; i < 101; i++) {
        double k = kMin + (kMax - kMin) * i / 100.;
        System.out.print("\t" + surface.getZValue(t, k));
      }
      System.out.print("\n");
    }
    System.out.print("\n");
  }

  private PDEFullResults1D getLocalVol(PDEFullResults1D[] denRes, TwoStateMarkovChainDataBundle chainData) {

    int tNodes = denRes[0].getNumberTimeNodes();
    int xNodes = denRes[0].getNumberSpaceNodes();
    double[][] lv = new double[tNodes][xNodes];
    double s;
    for (int j = 0; j < xNodes; j++) {
      s = denRes[0].getSpaceValue(j);
      double nu1 = chainData.getVol1() * chainData.getVol1() * Math.pow(s, 2 * chainData.getBeta1() - 2.0);
      double nu2 = chainData.getVol2() * chainData.getVol2() * Math.pow(s, 2 * chainData.getBeta2() - 2.0);

      for (int i = 0; i < tNodes; i++) {

        //form the equivalent local vol
        double p1 = denRes[0].getFunctionValue(j, i);
        double p2 = denRes[1].getFunctionValue(j, i);
        double p = p1 + p2;
        if (p > 0) { //if p = 0 can't find equivalent local vol for this t-s, so don't use point 
          lv[i][j] = Math.sqrt((nu1 * p1 + nu2 * p2) / p);
        }
      }
    }
    return new PDEFullResults1D(denRes[0].getGrid(), lv);
  }

  private LocalVolatilitySurface getLocalVolOverlay(final LocalVolatilitySurface marketLocalVol, final LocalVolatilitySurface modelLocalVol) {

    Function<Double, Double> func = new Function<Double, Double>() {

      @Override
      public Double evaluate(Double... ts) {
        double t = ts[0];
        double s = ts[1];
        double temp = marketLocalVol.getVolatility(t, s) / modelLocalVol.getVolatility(t, s);
        return temp;
      }
    };

    return new LocalVolatilitySurface(FunctionalDoublesSurface.from(func));
  }

  private Map<DoublesPair, Double> getLocalVolOverlay(LocalVolatilitySurface marketLocalVol, PDEFullResults1D[] denRes,
      TwoStateMarkovChainDataBundle chainData, LocalVolatilitySurface lvOverlay) {

    int tNodes = denRes[0].getNumberTimeNodes();
    int xNodes = denRes[0].getNumberSpaceNodes();
    Map<DoublesPair, Double> res = new HashMap<DoublesPair, Double>(tNodes * xNodes);
    double t, s;
    for (int j = 0; j < xNodes; j++) {
      s = denRes[0].getSpaceValue(j);
      double nu1 = chainData.getVol1() * chainData.getVol1() * Math.pow(s, 2 * chainData.getBeta1() - 2.0);
      double nu2 = chainData.getVol2() * chainData.getVol2() * Math.pow(s, 2 * chainData.getBeta2() - 2.0);

      for (int i = 0; i < tNodes; i++) {
        t = denRes[0].getTimeValue(i);

        //form the equivalent local vol
        double p1 = denRes[0].getFunctionValue(j, i);
        double p2 = denRes[1].getFunctionValue(j, i);
        double p = p1 + p2;
        if (p > 0) { //if p = 0 can't find equivalent local vol for this t-s, so don't use point 
          double eNu = (nu1 * p1 + nu2 * p2) / p;
          double mrkLV = marketLocalVol.getVolatility(t, s);
          double overlay = 1.0;
          if (lvOverlay != null) {
            overlay = lvOverlay.getVolatility(t, s);
          }
          if (eNu > 0.0 && overlay > 0.0) {
            double lVOverlay = mrkLV * mrkLV / overlay / overlay / eNu;
            res.put(new DoublesPair(t, s), Math.sqrt(lVOverlay));
          }
        }
      }
    }
    return res;
  }

  private Map<DoublesPair, Double> convertFormatt(final List<Pair<double[], Double>> from) {
    Map<DoublesPair, Double> res = new HashMap<DoublesPair, Double>(from.size());
    Iterator<Pair<double[], Double>> iter = from.iterator();
    while (iter.hasNext()) {
      Pair<double[], Double> temp = iter.next();
      res.put(new DoublesPair(temp.getFirst()[0], temp.getFirst()[1]), temp.getSecond());
    }

    return res;

  }

}
