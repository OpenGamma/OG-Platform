/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference.applications;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.financial.model.finitedifference.ExponentialMeshing;
import com.opengamma.analytics.financial.model.finitedifference.HyperbolicMeshing;
import com.opengamma.analytics.financial.model.finitedifference.MarkovChainApprox;
import com.opengamma.analytics.financial.model.finitedifference.MeshingFunction;
import com.opengamma.analytics.financial.model.finitedifference.PDEFullResults1D;
import com.opengamma.analytics.financial.model.finitedifference.PDEGrid1D;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.BlackImpliedVolatilityFormula;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.DoubleQuadraticInterpolator1D;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.minimization.DoubleRangeLimitTransform;
import com.opengamma.analytics.math.minimization.ParameterLimitsTransform;
import com.opengamma.analytics.math.minimization.ParameterLimitsTransform.LimitType;
import com.opengamma.analytics.math.minimization.SingleRangeLimitTransform;
import com.opengamma.analytics.math.minimization.UncoupledParameterTransforms;
import com.opengamma.analytics.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.analytics.math.statistics.leastsquare.LeastSquareResultsWithTransform;
import com.opengamma.analytics.math.statistics.leastsquare.NonLinearLeastSquare;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * 
 */
public class TwoStateMarkovChainFitter {
  /** A logger */
  private static final Logger s_logger = LoggerFactory.getLogger(TwoStateMarkovChainFitter.class);
  /** The Black implied volatility calculator */
  private static final BlackImpliedVolatilityFormula BLACK_IMPLIED_VOL = new BlackImpliedVolatilityFormula();
  /** The interpolator */
  private static final DoubleQuadraticInterpolator1D INTERPOLATOR_1D = Interpolator1DFactory.DOUBLE_QUADRATIC_INSTANCE;
  /** The grid interpolator */
  private static final GridInterpolator2D GRID_INTERPOLATOR2D = new GridInterpolator2D(INTERPOLATOR_1D, INTERPOLATOR_1D);
  /** The parameter transforms */
  private static final UncoupledParameterTransforms TRANSFORMS;

  static {
    final ParameterLimitsTransform[] trans = new ParameterLimitsTransform[6];
    trans[0] = new SingleRangeLimitTransform(0, LimitType.GREATER_THAN);
    trans[1] = new SingleRangeLimitTransform(0, LimitType.GREATER_THAN);
    trans[2] = new DoubleRangeLimitTransform(0.1, 5.0); //try to keep transition rates physical
    trans[3] = new DoubleRangeLimitTransform(0.1, 5.0);
    trans[4] = new DoubleRangeLimitTransform(0.0, 1.0);
    trans[5] = new DoubleRangeLimitTransform(0.0, 2.0);
    TRANSFORMS = new UncoupledParameterTransforms(new DoubleMatrix1D(new double[6]), trans, new BitSet(6));
  }

  /** Theta */
  private final double _theta;

  /**
   * Default constructor that sets theta to 0.5
   */
  public TwoStateMarkovChainFitter() {
    _theta = 0.5;
  }

  /**
   * @param theta Theta
   */
  public TwoStateMarkovChainFitter(final double theta) {
    _theta = theta;
  }

  /**
   * @param forward The forward
   * @param marketVols The market volatilities
   * @param initialGuess The initial guess. Must have the same number of elements as the parameter transforms
   * @return The results of the least squared fit
   */
  public LeastSquareResultsWithTransform fit(final ForwardCurve forward, final List<Pair<double[], Double>> marketVols, final DoubleMatrix1D initialGuess) {

    ArgumentChecker.isTrue(initialGuess.getNumberOfElements() == TRANSFORMS.getNumberOfModelParameters(),
        "Number of elements in initial guess {} did not match the number of parameter transforms {}", initialGuess.getNumberOfElements(),
        TRANSFORMS.getNumberOfFittingParameters());
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
          }
        }
        return new DoubleMatrix1D(modVols);
      }
    };

    final Function1D<DoubleMatrix1D, DoubleMatrix1D> func = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {

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
        final TwoStateMarkovChainDataBundle chainData = new TwoStateMarkovChainDataBundle(vol1, vol1 + deltaVol, lambda12, lambda21, p0, beta, beta);
        final TwoStateMarkovChainPricer mc = new TwoStateMarkovChainPricer(forward, chainData);
        final PDEFullResults1D res = mc.solve(grid, _theta);
        final Map<DoublesPair, Double> data = PDEUtilityTools.priceToImpliedVol(forward, res, minT, maxT, minK, maxK, true);
        final Map<Double, Interpolator1DDataBundle> dataBundle = GRID_INTERPOLATOR2D.getDataBundle(data);
        final double[] modVols = new double[nMarketValues];
        for (int i = 0; i < nMarketValues; i++) {
          final double[] temp = marketVols.get(i).getFirst();
          final DoublesPair tk = DoublesPair.of(temp[0], temp[1]);
          try {
            modVols[i] = GRID_INTERPOLATOR2D.interpolate(dataBundle, tk);
          } catch (final Exception e) {
            s_logger.error(e.getMessage());
          }
        }
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
  }

  /**
   * Transforms the price data (in PDEFullResults1D form) to implied volatility in a form used by 2D interpolator
   * @param forward
   * @param yield
   * @param prices
   * @return The transformed data
   */
  @SuppressWarnings("unused")
  private List<Pair<double[], Double>> transformData(final ForwardCurve forward, final YieldCurve yield, final PDEFullResults1D prices, final double minT,
      final double maxT, final double minK, final double maxK) {
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
              final Pair<double[], Double> pair = Pairs.of(new double[] {prices.getTimeValue(i), prices.getSpaceValue(j)}, impVol);
              out.add(pair);
            } catch (final Exception e) {
              s_logger.error("can't find vol for strike: " + prices.getSpaceValue(j) + " and expiry " + prices.getTimeValue(i) + " . Not added to data set");
            }
          }
        }
      }
    }
    return out;
  }

}
