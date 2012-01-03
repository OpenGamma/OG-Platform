/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finitedifference.applications;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.finitedifference.ExponentialMeshing;
import com.opengamma.financial.model.finitedifference.HyperbolicMeshing;
import com.opengamma.financial.model.finitedifference.MarkovChainApprox;
import com.opengamma.financial.model.finitedifference.MeshingFunction;
import com.opengamma.financial.model.finitedifference.PDEFullResults1D;
import com.opengamma.financial.model.finitedifference.PDEGrid1D;
import com.opengamma.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.volatility.BlackImpliedVolatilityFormula;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.DoubleQuadraticInterpolator1D;
import com.opengamma.math.interpolation.GridInterpolator2D;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.minimization.DoubleRangeLimitTransform;
import com.opengamma.math.minimization.ParameterLimitsTransform;
import com.opengamma.math.minimization.ParameterLimitsTransform.LimitType;
import com.opengamma.math.minimization.SingleRangeLimitTransform;
import com.opengamma.math.minimization.UncoupledParameterTransforms;
import com.opengamma.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.math.statistics.leastsquare.LeastSquareResultsWithTransform;
import com.opengamma.math.statistics.leastsquare.NonLinearLeastSquare;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class TwoStateMarkovChainFitter {
  private static final BlackImpliedVolatilityFormula BLACK_IMPLIED_VOL = new BlackImpliedVolatilityFormula();
  private static final DoubleQuadraticInterpolator1D INTERPOLATOR_1D = new DoubleQuadraticInterpolator1D();
  private static final GridInterpolator2D GRID_INTERPOLATOR2D = new GridInterpolator2D(INTERPOLATOR_1D, INTERPOLATOR_1D);
  private static final UncoupledParameterTransforms TRANSFORMS;

  static {
    final ParameterLimitsTransform[] trans = new ParameterLimitsTransform[6];
    trans[0] = new SingleRangeLimitTransform(0, LimitType.GREATER_THAN);
    trans[1] = new SingleRangeLimitTransform(0, LimitType.GREATER_THAN);
    //trans[2] = new SingleRangeLimitTransform(0, LimitType.GREATER_THAN);
    //trans[3] = new SingleRangeLimitTransform(0, LimitType.GREATER_THAN);
    //    trans[0] = new DoubleRangeLimitTransform(0.1, 0.5);
    //trans[1] = new DoubleRangeLimitTransform(0.0, 0.7);
    trans[2] = new DoubleRangeLimitTransform(0.1, 5.0); //try to keep transition rates physical
    trans[3] = new DoubleRangeLimitTransform(0.1, 5.0);
    trans[4] = new DoubleRangeLimitTransform(0.0, 1.0);
    trans[5] = new DoubleRangeLimitTransform(0.0, 2.0);
    // trans[6] = new DoubleRangeLimitTransform(0.0, 2.0);
    TRANSFORMS = new UncoupledParameterTransforms(new DoubleMatrix1D(new double[6]), trans, new BitSet());
  }

  private final double _theta;

  public TwoStateMarkovChainFitter() {
    _theta = 0.5;
  }

  public TwoStateMarkovChainFitter(final double theta) {
    _theta = theta;
  }

  public LeastSquareResultsWithTransform fit(final ForwardCurve forward, final List<Pair<double[], Double>> marketVols, final DoubleMatrix1D initialGuess) {

    Validate.isTrue(initialGuess.getNumberOfElements() == TRANSFORMS.getNumberOfModelParameters());
    TRANSFORMS.transform(initialGuess);

    final int nMarketValues = marketVols.size();
    double tminT = Double.POSITIVE_INFINITY;
    double tminK = Double.POSITIVE_INFINITY;
    double tmaxT = 0;
    double tmaxK = 0;

    for (int i = 0; i < nMarketValues; i++) {
      final double[] tk = marketVols.get(i).getFirst();

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

    final int tNodes = 20;
    final int xNodes = 100;

    //TODO remove hard coded grid
    final MeshingFunction timeMesh = new ExponentialMeshing(0, tmaxT, tNodes, 5.0);
    final MeshingFunction spaceMesh = new HyperbolicMeshing(0, 10.0 * forward.getForward(maxT), forward.getSpot(), xNodes, 0.01);
    final PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);

    final Function1D<DoubleMatrix1D, DoubleMatrix1D> funcAppox = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
        final DoubleMatrix1D y = TRANSFORMS.inverseTransform(x);
        final double vol1 = y.getEntry(0);
        final double deltaVol = y.getEntry(1);
        final double lambda12 = y.getEntry(2);
        final double lambda21 = y.getEntry(3);
        final double p0 = y.getEntry(4);
        final double beta = y.getEntry(5);

        final double[] modVols = new double[nMarketValues];
        for (int i = 0; i < nMarketValues; i++) {
          final double[] temp = marketVols.get(i).getFirst();
          final double t = temp[0];
          final double k = temp[1];
          final EuropeanVanillaOption option = new EuropeanVanillaOption(k, t, true);
          final BlackFunctionData data = new BlackFunctionData(forward.getForward(t), 1.0, 0.0);
          final MarkovChainApprox mca = new MarkovChainApprox(vol1, vol1 + deltaVol, lambda12, lambda21, p0, t);
          final double price = mca.priceCEV(data.getForward(), data.getDiscountFactor(), k, beta);
          try {
            modVols[i] = BLACK_IMPLIED_VOL.getImpliedVolatility(data, option, price);
          } catch (final Exception e) {
            modVols[i] = 0.0;
            //System.out.println("arrrgggg");
          }
        }
        //debug(DataBundle);
        return new DoubleMatrix1D(modVols);
      }
    };

    final Function1D<DoubleMatrix1D, DoubleMatrix1D> func = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
        //        long timer = System.nanoTime();
        final DoubleMatrix1D y = TRANSFORMS.inverseTransform(x);
        final double vol1 = y.getEntry(0);
        final double deltaVol = y.getEntry(1);
        final double lambda12 = y.getEntry(2);
        final double lambda21 = y.getEntry(3);
        final double p0 = y.getEntry(4);
        final double beta = y.getEntry(5);
        final TwoStateMarkovChainDataBundle chainData = new TwoStateMarkovChainDataBundle(vol1, vol1 + deltaVol, lambda12, lambda21, p0, beta, beta);
        final TwoStateMarkovChainPricer mc = new TwoStateMarkovChainPricer(forward, chainData);
        //        long timer1 = System.nanoTime();
        final PDEFullResults1D res = mc.solve(grid, _theta);
        //        System.out.println("time1 " + ((System.nanoTime() - timer1)/1e6)+"ms");
        //        long timer2 = System.nanoTime();
        final Map<DoublesPair, Double> data = PDEUtilityTools.priceToImpliedVol(forward, res, minT, maxT, minK, maxK, true);
        //        System.out.println("time2 " + ((System.nanoTime() - timer2)/1e6)+"ms");
        //        long timer3 = System.nanoTime();
        final Map<Double, Interpolator1DDataBundle> dataBundle = GRID_INTERPOLATOR2D.getDataBundle(data);
        final double[] modVols = new double[nMarketValues];
        for (int i = 0; i < nMarketValues; i++) {
          final double[] temp = marketVols.get(i).getFirst();
          final DoublesPair tk = new DoublesPair(temp[0], temp[1]);
          try {
            modVols[i] = GRID_INTERPOLATOR2D.interpolate(dataBundle, tk);
          } catch (final Exception e) {
            System.out.println("arrrgggg");
          }
        }
        //        System.out.println("time3 " + ((System.nanoTime() - timer3)/1e6)+"ms");
        //        System.out.println("time " + ((System.nanoTime() - timer)/1e6)+"ms");
        //debug(DataBundle);
        return new DoubleMatrix1D(modVols);
      }

    };

    final double[] mrkVols = new double[nMarketValues];
    final double[] sigma = new double[nMarketValues];
    for (int i = 0; i < nMarketValues; i++) {
      mrkVols[i] = marketVols.get(i).getSecond();
      sigma[i] = 0.01; //1% error
    }

    final NonLinearLeastSquare ls = new NonLinearLeastSquare();
    //solve approx first
    LeastSquareResults solverRes = ls.solve(new DoubleMatrix1D(mrkVols), new DoubleMatrix1D(sigma), funcAppox, TRANSFORMS.transform(initialGuess));
    // now solve pde model
    solverRes = ls.solve(new DoubleMatrix1D(mrkVols), new DoubleMatrix1D(sigma), func, solverRes.getFitParameters());
    return new LeastSquareResultsWithTransform(solverRes, TRANSFORMS);
    // return new LeastSquareResults(solverRes.getChiSq(), TRANSFORMS.inverseTransform(solverRes.getFitParameters()), solverRes.getCovariance());
  }

  public void debug(final Map<Double, Interpolator1DDataBundle> dataBundle) {
    for (int i = 0; i < 101; i++) {
      final double k = 0. + 4.0 * i / 100.;
      System.out.print("\t" + k);
    }
    System.out.print("\n");

    for (int j = 0; j < 101; j++) {
      final double t = 0.2 + 4.8 * j / 100.;
      System.out.print(t);
      for (int i = 0; i < 101; i++) {
        final double k = 0. + 4.0 * i / 100.;
        // System.out.print("\t" + INTERPOLATOR.interpolate(DATABUNDLE, new double[] {t, k }));
        System.out.print("\t" + GRID_INTERPOLATOR2D.interpolate(dataBundle, new DoublesPair(t, k)));
      }
      System.out.print("\n");
    }
  }

  /**
   * Transforms the price data (in PDEFullResults1D form) to implied volatility in a form used by 2D interpolator
   * @param forward
   * @param yield
   * @param prices
   * @return
   */
  @SuppressWarnings("unused")
  private List<Pair<double[], Double>> transformData(final ForwardCurve forward, final YieldCurve yield, final PDEFullResults1D prices, final double minT, final double maxT, final double minK,
      final double maxK) {
    final int xNodes = prices.getNumberSpaceNodes();
    final int tNodes = prices.getNumberTimeNodes();
    final int n = xNodes * tNodes;
    final List<Pair<double[], Double>> out = new ArrayList<Pair<double[], Double>>(n);

    for (int i = 0; i < tNodes; i++) {
      final double t = prices.getTimeValue(i);
      if (t >= minT && t <= maxT) {
        final BlackFunctionData data = new BlackFunctionData(forward.getForward(t), yield.getDiscountFactor(t), 0);
        for (int j = 0; j < xNodes; j++) {
          final double k = prices.getSpaceValue(j);
          if (k >= minK && k <= maxK) {
            final double price = prices.getFunctionValue(j, i);
            final EuropeanVanillaOption option = new EuropeanVanillaOption(k, t, true);
            try {
              final double impVol = BLACK_IMPLIED_VOL.getImpliedVolatility(data, option, price);
              final Pair<double[], Double> pair = new ObjectsPair<double[], Double>(new double[] {prices.getTimeValue(i), prices.getSpaceValue(j) }, impVol);
              out.add(pair);
            } catch (final Exception e) {
              System.out.println("can't find vol for strike: " + prices.getSpaceValue(j) + " and expiry " + prices.getTimeValue(i) + " . Not added to data set");
            }
          }
        }
      }
    }
    return out;
  }

  //  protected Map<DoublesPair, Double> priceToImpliedVol(final ForwardCurve forward, final PDEFullResults1D prices,
  //      final double minT, final double maxT, final double minK, final double maxK) {
  //    int xNodes = prices.getNumberSpaceNodes();
  //    int tNodes = prices.getNumberTimeNodes();
  //    int n = xNodes * tNodes;
  //    Map<DoublesPair, Double> out = new HashMap<DoublesPair, Double>(n);
  //    int count = tNodes * xNodes;
  //
  //    for (int i = 0; i < tNodes; i++) {
  //      double t = prices.getTimeValue(i);
  //      if (t >= minT && t <= maxT) {
  //        BlackFunctionData data = new BlackFunctionData(forward.getForward(t), forward.getSpot() / forward.getForward(t), 0);
  //        for (int j = 0; j < xNodes; j++) {
  //          double k = prices.getSpaceValue(j);
  //          if (k >= minK && k <= maxK) {
  //            double price = prices.getFunctionValue(j, i);
  //            EuropeanVanillaOption option = new EuropeanVanillaOption(k, t, true);
  //            try {
  //              double impVol = BLACK_IMPLIED_VOL.getImpliedVolatility(data, option, price);
  //              if (Math.abs(impVol) > 1e-15) {
  //                DoublesPair pair = new DoublesPair(prices.getTimeValue(i), prices.getSpaceValue(j));
  //                out.put(pair, impVol);
  //                count--;
  //              }
  //            } catch (Exception e) {
  //              // System.out.println("can't find vol for strike: " + prices.getSpaceValue(j) + " and expiry " + prices.getTimeValue(i) + " . Not added to data set");
  //            }
  //          }
  //        }
  //      }
  //    }
  //    //    if (count > 0) {
  //    //      System.err.println(count + " out of " + xNodes * tNodes + " data points removed");
  //    //    }
  //    return out;
  //  }

}
