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
import java.util.Map.Entry;
import java.util.Set;

import com.opengamma.financial.model.finitedifference.ExponentialMeshing;
import com.opengamma.financial.model.finitedifference.HyperbolicMeshing;
import com.opengamma.financial.model.finitedifference.MeshingFunction;
import com.opengamma.financial.model.finitedifference.PDEFullResults1D;
import com.opengamma.financial.model.finitedifference.PDEGrid1D;
import com.opengamma.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.financial.model.volatility.surface.AbsoluteLocalVolatilitySurface;
import com.opengamma.financial.model.volatility.surface.BlackVolatilitySurface;
import com.opengamma.financial.model.volatility.surface.DupireLocalVolatilityCalculator;
import com.opengamma.financial.model.volatility.surface.LocalVolatilitySurface;
import com.opengamma.math.function.Function;
import com.opengamma.math.interpolation.DoubleQuadraticInterpolator1D;
import com.opengamma.math.interpolation.GridInterpolator2D;
import com.opengamma.math.interpolation.data.Interpolator1DDoubleQuadraticDataBundle;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.math.surface.ConstantDoublesSurface;
import com.opengamma.math.surface.FunctionalDoublesSurface;
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
  private final boolean _print;

  public TwoStateMarkovChainLocalVolFitter(boolean printAll) {
    _print = printAll;
  }

  /**
   * 
   * @param forward The forward curve <b>NOTE</b> Anything other than a constant rate will give spurious results because the local vol calculator does not handle time 
   * varying rates
   * @param marketVolSurface <b> THIS IS A CHEAT</b> Don't have access to some continuous (twice differentiable in strike and once in time) volatility surface, but have
   * to approximate it from finite data points (i.e. option prices at gives strike/maturity). We assume for now that this is provided by some magical algorithm 
   * @param marketVols Collection of expiry-strike coordinates and the implied volatility at those points  
   * @param initialGuess Initial guess at the parameters of the Markov chain. these are: CEV vol of normal state; CEV vol of excited state (which must
   *  be greater than the vol in normal state); transition rate from normal to excited; transition rate from excited to normal; probability of
   *  starting in normal state; and the CEV parameter, beta  
   */
  public void fit(final ForwardCurve forward, final BlackVolatilitySurface marketVolSurface, final List<Pair<double[], Double>> marketVols, DoubleMatrix1D initialGuess) {

    Map<DoublesPair, Double> marketVolsMap = convertFormatt(marketVols);
    Set<DoublesPair> dataPoints = marketVolsMap.keySet();

    // first fit calibrate the basic Markov chain model (i.e. fixed vol levels) to market data
    LeastSquareResults chiSqRes = CHAIN_FITTER.fit(forward, marketVols, initialGuess);

    double vol1 = chiSqRes.getParameters().getEntry(0);
    double vol2 = chiSqRes.getParameters().getEntry(1) + vol1;
    double lambda12 = chiSqRes.getParameters().getEntry(2);
    double lambda21 = chiSqRes.getParameters().getEntry(3);
    double p0 = chiSqRes.getParameters().getEntry(4);
    double beta = chiSqRes.getParameters().getEntry(5);

    TwoStateMarkovChainDataBundle chainData = new TwoStateMarkovChainDataBundle(vol1, vol2, lambda12, lambda21, p0, beta, beta);

    //**** DO NOT REMOVE ****************************************************************************************************
    //TODO This is not used because we are passing in the market Vol Surface - this should be replaced by a "smart" interpolator to get vol surface
    //interpolate the market vol surface. 
    //    final Map<Double, Interpolator1DDoubleQuadraticDataBundle> dataBundle = GRID_INTERPOLATOR2D.getDataBundle(marketVolsMap);
    //    @SuppressWarnings("unused")
    //    Function<Double, Double> mrkVolFunc = new Function<Double, Double>() {
    //      @Override
    //      public Double evaluate(Double... ts) {
    //        double t = ts[0];
    //        double s = ts[1];
    //        return GRID_INTERPOLATOR2D.interpolate(dataBundle, new DoublesPair(t, s));
    //      }
    //    }; 
    // BlackVolatilitySurface marketVolSurface = new BlackVolatilitySurface(FunctionalDoublesSurface.from(mrkVolFunc));
    //******************************************************************************************************************

    int tNodes = 50;
    int xNodes = 100;

    //Get min/max strikes and expiries 
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

    //get the market local vol surface
    //TODO local vol with non-constant (but deterministic) rates
    AbsoluteLocalVolatilitySurface marketLocalVol = LOCAL_VOL_CALC.getAbsoluteLocalVolatilitySurface(marketVolSurface, forward.getSpot(),
        forward.getDrift(0));

    if (_print) {
      PDEUtilityTools.printSurface("Market implied Vols", marketVolSurface.getSurface(), minT, maxT, minK, maxK);
      PDEUtilityTools.printSurface("Market local Vols", marketLocalVol.getSurface(), minT, maxT, minK, maxK);
    }

    //get the local vol of basic Markov chain model

    MeshingFunction timeMesh = new ExponentialMeshing(0, tmaxT, tNodes, 2.0);
    MeshingFunction spaceMesh = new HyperbolicMeshing(0, 6.0 * forward.getForward(tmaxT), forward.getForward(0), xNodes, 0.01);
    final PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);

    TwoStateMarkovChainWithLocalVolDensity densityCal = new TwoStateMarkovChainWithLocalVolDensity(forward, chainData, new AbsoluteLocalVolatilitySurface(ConstantDoublesSurface.from(1.0)));
    PDEFullResults1D[] denRes = densityCal.solve(grid);

    if (_print) {
      PDEUtilityTools.printSurface("State 1 densities", denRes[0]);
      PDEUtilityTools.printSurface("State 2 densities", denRes[1]);
    }

    //the Local vol of the basis Markov chain model 
    AbsoluteLocalVolatilitySurface mcBaseLocalVol = MC_LOCAL_VOL_CALC.calc(denRes, chainData, null);
    AbsoluteLocalVolatilitySurface mcLocalVol;
    if (_print) {
      PDEUtilityTools.printSurface("Markov local Vols", mcBaseLocalVol.getSurface(), minT, maxT, minK, maxK);
    }

    AbsoluteLocalVolatilitySurface lvOverlay = null;
    mcLocalVol = mcBaseLocalVol;

    int count = 0;
    while (!converged(marketLocalVol, mcLocalVol, dataPoints) && count < 10) {
      count++;

      //get the local vol overlay to the Markov chain model 
      lvOverlay = getLocalVolOverlay(marketLocalVol, mcBaseLocalVol);
      if (_print) {
        PDEUtilityTools.printSurface("Local vol overlay at step " + count, lvOverlay.getSurface(), minT, maxT, minK, maxK);
      }

      TwoStateMarkovChainWithLocalVolDensity lvDensityCal = new TwoStateMarkovChainWithLocalVolDensity(forward, chainData, lvOverlay);
      denRes = lvDensityCal.solve(grid);

      if (_print) {
        PDEUtilityTools.printSurface("State 1 densities at step " + count, denRes[0]);
        PDEUtilityTools.printSurface("State 2 densities at step" + count, denRes[1]);
      }

      //calculate mc local vol without overlay
      mcBaseLocalVol = MC_LOCAL_VOL_CALC.calc(denRes, chainData, null);
      //... and with overlay  
      mcLocalVol = MC_LOCAL_VOL_CALC.calc(denRes, chainData, lvOverlay);
    }

    if (_print) {
      PDEUtilityTools.printSurface("Markov LV local Vols", mcLocalVol.getSurface(), minT, maxT, minK, maxK);
      PDEUtilityTools.printSurface("Local vol overlay ", lvOverlay.getSurface(), minT, maxT, minK, maxK);
    }

    //Solve the forward PDE with the local vol overlay to check match with data
    final TwoStateMarkovChainPricer pricer = new TwoStateMarkovChainPricer(forward, chainData, lvOverlay);
    PDEFullResults1D res = pricer.solve(grid, 1.0);
    Map<DoublesPair, Double> modelVols = PDEUtilityTools.priceToImpliedVol(forward, res, minT, maxT, minK, maxK);
    Map<Double, Interpolator1DDoubleQuadraticDataBundle> volData = GRID_INTERPOLATOR2D.getDataBundle(modelVols);

    Iterator<Entry<DoublesPair, Double>> iter = marketVolsMap.entrySet().iterator();
    while (iter.hasNext()) {
      Entry<DoublesPair, Double> entry = iter.next();
      double vol = GRID_INTERPOLATOR2D.interpolate(volData, entry.getKey());
      System.out.println(entry.getKey().first + "\t" + entry.getKey().second + "\t" + entry.getValue() + "\t" + vol);
    }

  }

  private boolean converged(AbsoluteLocalVolatilitySurface mrkLV, AbsoluteLocalVolatilitySurface modLV, Set<DoublesPair> dataPoints) {
    double error = 0.0;
    Iterator<DoublesPair> interator = dataPoints.iterator();
    while (interator.hasNext()) {
      DoublesPair point = interator.next();
      double temp = mrkLV.getVolatility(point) - modLV.getVolatility(point);
      error += temp * temp;
    }
    System.out.println("error " + error);
    return error < 1e-5; //TODO arbitrary error 
  }

  //  private void printSurface(PDEFullResults1D res) {
  //    int tNodes = res.getNumberTimeNodes();
  //    int xNodes = res.getNumberSpaceNodes();
  //
  //    for (int i = 0; i < xNodes; i++) {
  //      double k = res.getSpaceValue(i);
  //      System.out.print("\t" + k);
  //    }
  //    System.out.print("\n");
  //
  //    for (int j = 0; j < tNodes; j++) {
  //      double t = res.getTimeValue(j);
  //      System.out.print(t);
  //      for (int i = 0; i < xNodes; i++) {
  //        System.out.print("\t" + res.getFunctionValue(i, j));
  //      }
  //      System.out.print("\n");
  //    }
  //  }

  //  private void printSurface(Map<Double, Interpolator1DDoubleQuadraticDataBundle> dataBundle, double tMin, double tMax, double kMin, double kMax) {
  //
  //    for (int i = 0; i < 101; i++) {
  //      double k = kMin + (kMax - kMin) * i / 100.;
  //      System.out.print("\t" + k);
  //    }
  //    System.out.print("\n");
  //
  //    for (int j = 0; j < 101; j++) {
  //      double t = tMin + (tMax - tMin) * j / 100.;
  //      System.out.print(t);
  //      for (int i = 0; i < 101; i++) {
  //        double k = kMin + (kMax - kMin) * i / 100.;
  //        DoublesPair tk = new DoublesPair(t, k);
  //
  //        System.out.print("\t" + GRID_INTERPOLATOR2D.interpolate(dataBundle, tk));
  //      }
  //      System.out.print("\n");
  //    }
  //  }

  //  private void printSurface(Surface<Double, Double, Double> surface, double tMin, double tMax, double kMin, double kMax) {
  //
  //    System.out.print("\n");
  //    for (int i = 0; i < 101; i++) {
  //      double k = kMin + (kMax - kMin) * i / 100.;
  //      System.out.print("\t" + k);
  //    }
  //    System.out.print("\n");
  //
  //    for (int j = 0; j < 101; j++) {
  //      double t = tMin + (tMax - tMin) * j / 100.;
  //      System.out.print(t);
  //      for (int i = 0; i < 101; i++) {
  //        double k = kMin + (kMax - kMin) * i / 100.;
  //        System.out.print("\t" + surface.getZValue(t, k));
  //      }
  //      System.out.print("\n");
  //    }
  //    System.out.print("\n");
  //  }

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

  private AbsoluteLocalVolatilitySurface getLocalVolOverlay(final AbsoluteLocalVolatilitySurface marketLocalVol, final AbsoluteLocalVolatilitySurface modelLocalVol) {

    Function<Double, Double> func = new Function<Double, Double>() {

      @Override
      public Double evaluate(Double... ts) {
        double t = ts[0];
        double s = ts[1];
        double mrk = marketLocalVol.getVolatility(t, s);
        double mod = modelLocalVol.getVolatility(t, s);
        if (mrk == 0.0 && mod == 0.0) {
          return 0.0;
        }
        double temp = marketLocalVol.getVolatility(t, s) / modelLocalVol.getVolatility(t, s);
        return temp;
      }
    };

    return new AbsoluteLocalVolatilitySurface(FunctionalDoublesSurface.from(func));
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
