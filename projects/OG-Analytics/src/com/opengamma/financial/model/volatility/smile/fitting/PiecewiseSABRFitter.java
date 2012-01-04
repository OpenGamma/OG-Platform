/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.fitting;

import java.util.Arrays;
import java.util.BitSet;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.statistics.leastsquare.LeastSquareResultsWithTransform;

/**
 * 
 */
public class PiecewiseSABRFitter {
  private static final SABRHaganVolatilityFunction MODEL = new SABRHaganVolatilityFunction();
  private static final double DEFAULT_BETA = 0.5;
  private static final Logger LOGGER = LoggerFactory.getLogger(PiecewiseSABRFitter.class);

  private final SABRFormulaData[] _modelParams;
  private final double _forward;
  private final double _expiry;
  private final double[] _strikes;
  private final int _n;

  public PiecewiseSABRFitter(final double forward, final double[] strikes, final double timeToExpiry, final double[] impliedVols) {
    Validate.notNull(strikes, "null strikes");
    Validate.notNull(impliedVols, "null impliedVols");
    final int n = strikes.length;
    Validate.isTrue(n > 2, "cannot fit less than three points");
    Validate.isTrue(impliedVols.length == n, "#strikes != # vols");
    validateStrikes(strikes);

    _forward = forward;
    _expiry = timeToExpiry;
    _strikes = strikes;
    _n = n;

    double avVol = 0;
    for (int i = 0; i < n; i++) {
      avVol += impliedVols[i];
    }
    avVol /= n;
    double appoxAlpha = avVol * Math.pow(forward, 1 - DEFAULT_BETA);
    DoubleMatrix1D start = new DoubleMatrix1D(appoxAlpha, DEFAULT_BETA, 0.0, 0.3);
    _modelParams = new SABRFormulaData[n - 2];

    double[] errors = new double[n];
    Arrays.fill(errors, 0.0001); //1bps
    SmileModelFitter<SABRFormulaData> globalFitter = new SABRModelFitter(forward, strikes, timeToExpiry, impliedVols, errors, MODEL);
    BitSet fixed = new BitSet();
    if (n == 3) {
      fixed.set(1); //fixed beta
    }

    //do a global fit first
    LeastSquareResultsWithTransform gRes = globalFitter.solve(start, fixed);

    if (n == 3) {
      if (gRes.getChiSq() / n > 1.0) {
        LOGGER.warn("chi^2 on SABR fit is " + gRes.getChiSq());
      }
      _modelParams[0] = new SABRFormulaData(gRes.getModelParameters().getData());
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
        SmileModelFitter<SABRFormulaData> fitter =
          new SABRModelFitter(forward, tStrikes, timeToExpiry, tVols, errors, MODEL);
        LeastSquareResultsWithTransform lRes = fitter.solve(start, fixed);
        if (lRes.getChiSq() > 3.0) {
          LOGGER.warn("chi^2 on SABR fit " + i + " is " + lRes.getChiSq());
        }
        _modelParams[i] = new SABRFormulaData(lRes.getModelParameters().getData());
      }

    }

  }

  private void validateStrikes(final double[] strikes) {
    final int n = strikes.length;
    for (int i = 1; i < n; i++) {
      Validate.isTrue(strikes[i] > strikes[i - 1], "strikes not in ascending order or equal strikes ");
    }
  }

  public double getVol(final double strike) {
    int index = getLowerBoundIndex(strike);
    if (index == 0) {
      SABRFormulaData p = _modelParams[0];
      return MODEL.getVolatility(_forward, strike, _expiry, p.getAlpha(), p.getBeta(), p.getRho(), p.getNu());
    }
    if (index >= _n - 2) {
      SABRFormulaData p = _modelParams[_n - 3];
      return MODEL.getVolatility(_forward, strike, _expiry, p.getAlpha(), p.getBeta(), p.getRho(), p.getNu());
    }
    final SABRFormulaData p1 = _modelParams[index - 1];
    final SABRFormulaData p2 = _modelParams[index];
    final double w = getWeight(strike, index);
    if (w == 1) {
      return MODEL.getVolatility(_forward, strike, _expiry, p1.getAlpha(), p1.getBeta(), p1.getRho(), p1.getNu());
    } else if (w == 0) {
      return MODEL.getVolatility(_forward, strike, _expiry, p2.getAlpha(), p2.getBeta(), p2.getRho(), p2.getNu());
    } else {
      return w * MODEL.getVolatility(_forward, strike, _expiry, p1.getAlpha(), p1.getBeta(), p1.getRho(), p1.getNu()) +
      (1 - w) * MODEL.getVolatility(_forward, strike, _expiry, p2.getAlpha(), p2.getBeta(), p2.getRho(), p2.getNu());
    }
  }

  public Function1D<Double, Double> getVolFunction() {
    return new Function1D<Double, Double>() {

      @Override
      public Double evaluate(Double x) {
        return getVol(x);
      }
    };
  }

  private double getWeight(final double strike, final int index) {
    return (_strikes[index + 1] - strike) / (_strikes[index + 1] - _strikes[index]);
    //double cos = Math.cos(Math.PI / 2 * (strike - _strikes[index]) / (_strikes[index + 1] - _strikes[index]));
    //return cos * cos;
  }

  private int getLowerBoundIndex(final double strike) {
    if (strike < _strikes[0]) {
      return 0;
    }
    if (strike > _strikes[_n - 1]) {
      return _n - 1;
    }

    int index = Arrays.binarySearch(_strikes, strike);
    if (index >= 0) {
      // Fast break out if it's an exact match.
      return index;
    }
    if (index < 0) {
      index = -(index + 1);
      index--;
    }
    return index;
  }

}
