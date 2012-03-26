/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.fitting.sabr;

import java.util.Arrays;
import java.util.BitSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.model.volatility.smile.fitting.SABRModelFitter;
import com.opengamma.financial.model.volatility.smile.fitting.SmileModelFitter;
import com.opengamma.financial.model.volatility.smile.fitting.interpolation.SineWeightingFunction;
import com.opengamma.financial.model.volatility.smile.fitting.interpolation.SurfaceArrayUtils;
import com.opengamma.financial.model.volatility.smile.fitting.interpolation.WeightingFunction;
import com.opengamma.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.statistics.leastsquare.LeastSquareResultsWithTransform;
import com.opengamma.util.ArgumentChecker;

/**
 * TODO use root finding rather than chi^2 for this
 */
public class PiecewiseSABRFitter {
  private static final double DEFAULT_BETA = 0.9;
  private static final WeightingFunction DEFAULT_WEIGHTING_FUNCTION = SineWeightingFunction.getInstance();

  private static final Logger s_logger = LoggerFactory.getLogger(PiecewiseSABRFitter.class);
  private static final SABRHaganVolatilityFunction MODEL = new SABRHaganVolatilityFunction();
  private final double _defaultBeta;
  private final WeightingFunction _weightingFunction;
  private final boolean _globalBetaSearch;

  public PiecewiseSABRFitter() {
    _defaultBeta = DEFAULT_BETA;
    _weightingFunction = DEFAULT_WEIGHTING_FUNCTION;
    _globalBetaSearch = true;
  }

  public PiecewiseSABRFitter(final double defaultBeta) {
    ArgumentChecker.isTrue(ArgumentChecker.isInRangeInclusive(0, 1, defaultBeta), "Beta must be >= 0 and <= 1; have {}", defaultBeta);
    _defaultBeta = defaultBeta;
    _weightingFunction = DEFAULT_WEIGHTING_FUNCTION;
    _globalBetaSearch = false;
  }

  public PiecewiseSABRFitter(final double defaultBeta, final WeightingFunction weightingFunction) {
    ArgumentChecker.isTrue(ArgumentChecker.isInRangeInclusive(0, 1, defaultBeta), "Beta must be >= 0 and <= 1; have {}", defaultBeta);
    ArgumentChecker.notNull(weightingFunction, "weighting function");
    _defaultBeta = defaultBeta;
    _weightingFunction = weightingFunction;
    _globalBetaSearch = false;
  }

  public final SABRFormulaData[] getFittedModelParameters(final double forward, final double[] strikes, final double expiry, final double[] impliedVols) {
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
    double temp = averageVol2 - averageVol * averageVol / n;
    averageVol2 = temp <= 0.0 ? 0.0 : Math.sqrt(temp) / (n - 1); //while temp should never be negative, rounding errors can make it so
    averageVol /= n;

    DoubleMatrix1D start;

    //almost flat surface
    if (averageVol2 / averageVol < 0.01) { //TODO remove the false before put back
      final double approxAlpha = averageVol * Math.pow(forward, 0.01);
      start = new DoubleMatrix1D(approxAlpha, 0.99, 0.0, 0.001);
      if (!_globalBetaSearch && _defaultBeta != 1.0) {
        s_logger.warn("Smile almost flat. Cannot use beta = ", +_defaultBeta + " so ignored");
      }
    } else {
      final double approxAlpha = averageVol * Math.pow(forward, 1 - _defaultBeta);
      start = new DoubleMatrix1D(approxAlpha, _defaultBeta, 0.0, 0.3);
    }

    final SABRFormulaData[] modelParams = new SABRFormulaData[n - 2];

    double[] errors = new double[n];
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
      fixed.set(1);
      start = gRes.getModelParameters();
      double[] tStrikes = new double[3];
      double[] tVols = new double[3];
      errors = new double[3];
      Arrays.fill(errors, 0.0001); //1bps
      for (int i = 0; i < n - 2; i++) {
        tStrikes = Arrays.copyOfRange(strikes, i, i + 3);
        tVols = Arrays.copyOfRange(impliedVols, i, i + 3);
        final SmileModelFitter<SABRFormulaData> fitter = new SABRModelFitter(forward, tStrikes, expiry, tVols, errors, MODEL);
        final LeastSquareResultsWithTransform lRes = fitter.solve(start, fixed);
        if (lRes.getChiSq() > 3.0) {
          s_logger.warn("chi^2 on 3-point SABR fit #" + i + " is " + lRes.getChiSq());
        }
        modelParams[i] = new SABRFormulaData(lRes.getModelParameters().getData());
      }

    }
    return modelParams;
  }

  public Function1D<Double, Double> getVolatilityFunction(final double forward, final double[] strikes, final double expiry, final double[] impliedVols) {

    final SABRFormulaData[] modelParams = getFittedModelParameters(forward, strikes, expiry, impliedVols);
    final int n = strikes.length;
    return new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double strike) {
        final int index = SurfaceArrayUtils.getLowerBoundIndex(strikes, strike);
        if (index == 0) {
          final SABRFormulaData p = modelParams[0];
          return MODEL.getVolatility(forward, strike, expiry, p.getAlpha(), p.getBeta(), p.getRho(), p.getNu());
        }
        if (index >= n - 2) {
          final SABRFormulaData p = modelParams[n - 3];
          return MODEL.getVolatility(forward, strike, expiry, p.getAlpha(), p.getBeta(), p.getRho(), p.getNu());
        }
        final double w = _weightingFunction.getWeight(strikes, index, strike);
        if (w == 1) {
          final SABRFormulaData p1 = modelParams[index - 1];
          return MODEL.getVolatility(forward, strike, expiry, p1.getAlpha(), p1.getBeta(), p1.getRho(), p1.getNu());
        } else if (w == 0) {
          final SABRFormulaData p2 = modelParams[index];
          return MODEL.getVolatility(forward, strike, expiry, p2.getAlpha(), p2.getBeta(), p2.getRho(), p2.getNu());
        } else {
          final SABRFormulaData p1 = modelParams[index - 1];
          final SABRFormulaData p2 = modelParams[index];
          return w * MODEL.getVolatility(forward, strike, expiry, p1.getAlpha(), p1.getBeta(), p1.getRho(), p1.getNu()) +
              (1 - w) * MODEL.getVolatility(forward, strike, expiry, p2.getAlpha(), p2.getBeta(), p2.getRho(), p2.getNu());
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
