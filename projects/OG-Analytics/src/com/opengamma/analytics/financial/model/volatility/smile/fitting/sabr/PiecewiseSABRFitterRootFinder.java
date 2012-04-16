/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr;

import java.util.Arrays;
import java.util.BitSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.SABRModelFitter;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.SmileModelFitter;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SurfaceArrayUtils;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.WeightingFunction;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.WeightingFunctionFactory;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.minimization.DoubleRangeLimitTransform;
import com.opengamma.analytics.math.minimization.NonLinearParameterTransforms;
import com.opengamma.analytics.math.minimization.NonLinearTransformFunction;
import com.opengamma.analytics.math.minimization.ParameterLimitsTransform;
import com.opengamma.analytics.math.minimization.ParameterLimitsTransform.LimitType;
import com.opengamma.analytics.math.minimization.SingleRangeLimitTransform;
import com.opengamma.analytics.math.minimization.UncoupledParameterTransforms;
import com.opengamma.analytics.math.rootfinding.newton.BroydenVectorRootFinder;
import com.opengamma.analytics.math.statistics.leastsquare.LeastSquareResultsWithTransform;
import com.opengamma.util.ArgumentChecker;

/**
 * TODO use root finding rather than chi^2 for this
 */
public class PiecewiseSABRFitterRootFinder {

  private static final ParameterLimitsTransform ALPHA_TRANSFORM = new SingleRangeLimitTransform(0, LimitType.GREATER_THAN);
  private static final ParameterLimitsTransform RHO_TRANSFORM = new DoubleRangeLimitTransform(-1, 1);
  private static final ParameterLimitsTransform NU_TRANSFORM = new SingleRangeLimitTransform(0, LimitType.GREATER_THAN);
  private static final NonLinearParameterTransforms TRANSFORM = new UncoupledParameterTransforms(new DoubleMatrix1D(3, 0.0),
      new ParameterLimitsTransform[] {ALPHA_TRANSFORM, RHO_TRANSFORM, NU_TRANSFORM }, new BitSet());

  private static final double DEFAULT_BETA = 0.9;
  private static final WeightingFunction DEFAULT_WEIGHTING_FUNCTION = WeightingFunctionFactory.SINE_WEIGHTING_FUNCTION;

  private static final Logger s_logger = LoggerFactory.getLogger(PiecewiseSABRFitterRootFinder.class);
  private static final VolatilityFunctionProvider<SABRFormulaData> MODEL = new SABRHaganVolatilityFunction();
  private final WeightingFunction _weightingFunction;
  private final double _defaultBeta;
  private final boolean _globalBetaSearch;

  public PiecewiseSABRFitterRootFinder() {
    _defaultBeta = DEFAULT_BETA;
    _weightingFunction = DEFAULT_WEIGHTING_FUNCTION;
    _globalBetaSearch = true;
  }

  public PiecewiseSABRFitterRootFinder(final double beta, final WeightingFunction weightingFunction) {
    ArgumentChecker.notNull(weightingFunction, "weighting function");
    _defaultBeta = beta;
    _weightingFunction = weightingFunction;
    _globalBetaSearch = false;
  }

  public final SABRFormulaData[] getFittedfModelParameters(final double forward, final double[] strikes, final double expiry, final double[] impliedVols) {
    ArgumentChecker.notNull(strikes, "strikes");
    ArgumentChecker.notNull(impliedVols, "implied volatilities");
    final int n = strikes.length;
    ArgumentChecker.isTrue(n > 2, "cannot fit less than three points; have {}", n);
    ArgumentChecker.isTrue(impliedVols.length == n, "#strikes != # vols; have {} and {}", impliedVols.length, n);
    validateStrikes(strikes);

    double averageVol = 0;
    double averageVol2 = 0;
    for (int i = 0; i < n; i++) {
      final double vol = impliedVols[i];
      averageVol += vol;
      averageVol2 += vol * vol;
    }
    final double temp = averageVol2 - averageVol * averageVol / n;
    averageVol2 = temp <= 0.0 ? 0.0 : Math.sqrt(temp) / (n - 1); //while temp should never be negative, rounding errors can make it so
    averageVol /= n;

    DoubleMatrix1D start;

    //almost flat surface
    if (averageVol2 / averageVol < 0.01) {
      start = new DoubleMatrix1D(averageVol, 1.0, 0.0, 0.0);
      if (!_globalBetaSearch && _defaultBeta != 1.0) {
        s_logger.warn("Smile almost flat. Cannot use beta = ", +_defaultBeta + " so ignored");
      }
    } else {
      final double approxAlpha = averageVol * Math.pow(forward, 1 - _defaultBeta);
      start = new DoubleMatrix1D(approxAlpha, _defaultBeta, 0.0, 0.3);
    }

    final SABRFormulaData[] modelParams = new SABRFormulaData[n - 2];

    final double[] errors = new double[n];
    Arrays.fill(errors, 0.0001); //1bps
    final SmileModelFitter<SABRFormulaData> globalFitter = new SABRModelFitter(forward, strikes, expiry, impliedVols, errors, MODEL);
    final BitSet fixed = new BitSet();
    if (n == 3 || !_globalBetaSearch) {
      fixed.set(1); //fixed beta
    }

    //do a global fit first
    final LeastSquareResultsWithTransform gRes = globalFitter.solve(start, fixed);

    if (n == 3) {
      if (gRes.getChiSq() / n > 1.0) {
        s_logger.warn("chi^2 on SABR fit to ", +n + " points is " + gRes.getChiSq());
      }
      modelParams[0] = new SABRFormulaData(gRes.getModelParameters().getData());
    } else {
      //impose a global beta on the remaining 3 point fits
      final double[] gFitParms = gRes.getModelParameters().getData();
      final double beta = gFitParms[1];
      start = new DoubleMatrix1D(gFitParms[0], gFitParms[2], gFitParms[3]);
      final BroydenVectorRootFinder rootFinder = new BroydenVectorRootFinder();

      double[] tStrikes = new double[3];
      double[] tVols = new double[3];

      for (int i = 0; i < n - 2; i++) {
        tStrikes = Arrays.copyOfRange(strikes, i, i + 3);
        tVols = Arrays.copyOfRange(impliedVols, i, i + 3);
        final Function1D<DoubleMatrix1D, DoubleMatrix1D> func = getVolDiffFunc(forward, tStrikes, expiry, tVols);
        final Function1D<DoubleMatrix1D, DoubleMatrix2D> jac = getVolJacFunc(forward, tStrikes, expiry, beta);
        final NonLinearTransformFunction tf = new NonLinearTransformFunction(func, jac, TRANSFORM);
        final DoubleMatrix1D res = rootFinder.getRoot(tf.getFittingFunction(), tf.getFittingJacobian(), start);
        final double[] root = TRANSFORM.inverseTransform(res).getData();
        modelParams[i] = new SABRFormulaData(new double[] {root[0], beta, root[1], root[2] });
      }
    }

    return modelParams;
  }

  public Function1D<DoubleMatrix1D, DoubleMatrix1D> getVolDiffFunc(final double forward, final double[] strikes, final double expiry, final double[] impliedVols) {

    final Function1D<SABRFormulaData, double[]> func = MODEL.getVolatilityFunction(forward, strikes, expiry);
    final int n = strikes.length;

    return new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {
      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
        final double sigma = x.getEntry(0);
        final double theta = x.getEntry(1);
        final double phi = x.getEntry(2);
        final double[] params = new double[] {sigma, 0.0, theta, phi };
        final SABRFormulaData data = new SABRFormulaData(params);
        final double[] vols = func.evaluate(data);
        final double[] res = new double[n];
        for (int i = 0; i < n; i++) {
          res[i] = vols[i] - impliedVols[i];
        }
        return new DoubleMatrix1D(res);
      }
    };
  }

  public Function1D<DoubleMatrix1D, DoubleMatrix2D> getVolJacFunc(final double forward, final double[] strikes, final double expiry, final double beta) {

    final Function1D<SABRFormulaData, double[][]> adjointFunc = MODEL.getModelAdjointFunction(forward, strikes, expiry);

    return new Function1D<DoubleMatrix1D, DoubleMatrix2D>() {

      @Override
      public DoubleMatrix2D evaluate(final DoubleMatrix1D x) {
        final double alpha = x.getEntry(0);
        final double rho = x.getEntry(1);
        final double nu = x.getEntry(2);
        final double[] params = new double[] {alpha, beta, rho, nu };
        final SABRFormulaData data = new SABRFormulaData(params);

        final double[][] temp = adjointFunc.evaluate(data);
        //remove the delta sigma sense
        final double[][] res = new double[3][3];
        for (int i = 0; i < 3; i++) {
          res[i][0] = temp[i][0];
          res[i][1] = temp[i][2];
          res[i][2] = temp[i][3];
        }

        return new DoubleMatrix2D(res);
      }
    };
  }

  public Function1D<Double, Double> getVolatilityFunction(final double forward, final double[] strikes, final double expiry, final double[] impliedVols) {
    final int n = strikes.length;
    final SABRFormulaData[] modelParams = getFittedfModelParameters(forward, strikes, expiry, impliedVols);

    return new Function1D<Double, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double strike) {
        final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, expiry, true);
        final Function1D<SABRFormulaData, Double> vFunc = MODEL.getVolatilityFunction(option, forward);
        final int index = SurfaceArrayUtils.getLowerBoundIndex(strikes, strike);
        if (index == 0) {
          final SABRFormulaData p = modelParams[0];

          return vFunc.evaluate(p);
        }
        if (index >= n - 2) {
          final SABRFormulaData p = modelParams[n - 3];
          return vFunc.evaluate(p);
        }
        final double w = _weightingFunction.getWeight(strikes, index, strike);
        if (w == 1) {
          final SABRFormulaData p1 = modelParams[index - 1];
          return vFunc.evaluate(p1);
        } else if (w == 0) {
          final SABRFormulaData p2 = modelParams[index];
          return vFunc.evaluate(p2);
        } else {
          final SABRFormulaData p1 = modelParams[index - 1];
          final SABRFormulaData p2 = modelParams[index];
          return w * vFunc.evaluate(p1) + (1 - w) * vFunc.evaluate(p2);
        }
      }
    };
  }

  private void validateStrikes(final double[] strikes) {
    final int n = strikes.length;
    for (int i = 1; i < n; i++) {
      ArgumentChecker.isTrue(strikes[i] > strikes[i - 1],
          "strikes must be in ascending order; have {} (element {}) and {} (element {})", strikes[i - 1], i - 1, strikes[i], i);
    }
  }
}
