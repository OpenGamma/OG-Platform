/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.fitting.sabr;

import java.util.Arrays;
import java.util.BitSet;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.model.volatility.smile.fitting.SABRModelFitter;
import com.opengamma.financial.model.volatility.smile.fitting.SmileModelFitter;
import com.opengamma.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.statistics.leastsquare.LeastSquareResultsWithTransform;

/**
 * 
 */
public class PiecewiseSABRFitter1 {
  private static final Logger s_logger = LoggerFactory.getLogger(PiecewiseSABRFitter1.class);
  private static final SABRHaganVolatilityFunction MODEL = new SABRHaganVolatilityFunction();
  private final double _defaultBeta;
  private final WeightingFunction _weightingFunction;

  public PiecewiseSABRFitter1() {
    this(0.5, new LinearWeightingFunction());
  }

  public PiecewiseSABRFitter1(final double defaultBeta, final WeightingFunction weightingFunction) {
    _defaultBeta = defaultBeta;
    _weightingFunction = weightingFunction;
  }

  public Function1D<Double, Double> getVolatilityFunction(final double forward, final double[] strikes, final double expiry, final double[] impliedVols) {
    Validate.notNull(strikes, "null strikes");
    Validate.notNull(impliedVols, "null impliedVols");
    final int n = strikes.length;
    Validate.isTrue(n > 2, "cannot fit less than three points");
    Validate.isTrue(impliedVols.length == n, "#strikes != # vols");
    validateStrikes(strikes);

    double avVol = 0;
    for (int i = 0; i < n; i++) {
      avVol += impliedVols[i];
    }
    avVol /= n;
    final double appoxAlpha = avVol * Math.pow(forward, 1 - _defaultBeta);
    DoubleMatrix1D start = new DoubleMatrix1D(appoxAlpha, _defaultBeta, 0.0, 0.3);
    final SABRFormulaData[] modelParams = new SABRFormulaData[n - 2];

    double[] errors = new double[n];
    Arrays.fill(errors, 0.0001); //1bps
    final SmileModelFitter<SABRFormulaData> globalFitter = new SABRModelFitter(forward, strikes, expiry, impliedVols, errors, MODEL);
    final BitSet fixed = new BitSet();
    if (n == 3) {
      fixed.set(1); //fixed beta
    }

    //do a global fit first
    final LeastSquareResultsWithTransform gRes = globalFitter.solve(start, fixed);

    if (n == 3) {
      if (gRes.getChiSq() / n > 1.0) {
        s_logger.warn("chi^2 on SABR fit is " + gRes.getChiSq());
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
          s_logger.warn("chi^2 on SABR fit " + i + " is " + lRes.getChiSq());
        }
        modelParams[i] = new SABRFormulaData(lRes.getModelParameters().getData());
      }

    }
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
      Validate.isTrue(strikes[i] > strikes[i - 1], "strikes not in ascending order or equal strikes ");
    }
  }
}
