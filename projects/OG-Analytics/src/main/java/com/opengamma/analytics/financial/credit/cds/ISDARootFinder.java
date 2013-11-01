/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.cds;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.rootfinding.BrentSingleRootFinder;
import com.opengamma.util.ArgumentChecker;

/**
 * Numerical solver replicating the ISDA 'Brent' root finder algorithm. This
 * algorithm is only intended for use with the ISDA CDS pricing method.
 * 
 * Bounds for an initial interval are taken from the guess parameter. Then
 * the secant method is called to find an interval where the function straddles
 * zero. If the secant method fails to find an interval, the intervals between the
 * initial guess and the upper and lower bounds are tried. Once an interval straddling
 * zero is found, the brent solver is called for that interval.
 * 
 * @see BrentSingleRootFinder
 * @see ISDAApproxCDSPricingMethod
 * @deprecated Use classes from isdastandardmodel
 */
@Deprecated
public class ISDARootFinder {

  private static final int MAX_ITER = 100;
  private static final double ONE_PERCENT = 0.01;
  private static final double DEFAULT_TOLERANCE = 1E-15;

  private final double _tolerance;
  private final BrentSingleRootFinder _brentRootFinder;

  public ISDARootFinder() {
    _tolerance = DEFAULT_TOLERANCE;
    _brentRootFinder = new BrentSingleRootFinder(DEFAULT_TOLERANCE);
  }

  public ISDARootFinder(final double tolerance) {
    _tolerance = tolerance;
    _brentRootFinder = new BrentSingleRootFinder(tolerance);
  }

  public double findRoot(final Function1D<Double, Double> function, final double guess, final double lowerBound, final double upperBound, final double initialStep, final double initialDerivative) {

    ArgumentChecker.isTrue(upperBound > lowerBound, "Upper bound must be greater than lower bound");
    ArgumentChecker.isTrue(guess >= lowerBound, "Guess is out of range");
    ArgumentChecker.isTrue(guess <= upperBound, "Guess is out of range");

    double x1, x2, y1, y2, temp;

    x1 = guess;
    y1 = function.evaluate(x1);

    if (Math.abs(y1) <= _tolerance && (Math.abs(x1 - lowerBound) <= _tolerance || Math.abs(x1 - upperBound) <= _tolerance)) {
      return y1;
    }

    x2 = initialDerivative == 0.0 ? guess + initialStep : guess - (y1 / initialDerivative);

    if (x2 < lowerBound || x2 > upperBound) {
      final double nextGuess = guess - initialStep;
      final double boundSpread = upperBound - lowerBound;

      x2 = nextGuess < lowerBound ? lowerBound : nextGuess > upperBound ? upperBound : nextGuess;

      if (x2 == x1) {
        x2 = x2 == lowerBound ? lowerBound + ONE_PERCENT * boundSpread : upperBound - ONE_PERCENT * boundSpread;
      }
    }

    y2 = function.evaluate(x2);

    if (Math.abs(y2) <= _tolerance && (Math.abs(x2 - lowerBound) <= _tolerance || Math.abs(x2 - upperBound) <= _tolerance)) {
      return y2;
    }

    final SecantResultData secant = secantMethod(function, lowerBound, upperBound, x1, x2, y1, y2);

    if (secant.getResult() == SecantResult.FOUND) {
      return secant.getRoot();
    }

    if (secant.getResult() == SecantResult.BRACKETED) {
      x1 = secant.getLower();
      x2 = secant.getUpper();
    } else {

      final double yLo = function.evaluate(lowerBound);
      final double yHi = function.evaluate(upperBound);

      if (Math.abs(yLo) <= _tolerance && Math.abs(lowerBound - x1) <= _tolerance) {
        return lowerBound;
      }

      if (Math.abs(yHi) <= _tolerance && Math.abs(upperBound - x1) <= _tolerance) {
        return upperBound;
      }

      if (y1 * yLo < 0.0) {
        x2 = x1;
        x1 = lowerBound;
        y2 = y1;
        y1 = yLo;
      } else if (y1 * yHi < 0.0) {
        x2 = upperBound;
        y2 = yHi;
      } else {
        throw new OpenGammaRuntimeException("Failed to find root");
      }
    }

    if (x1 > x2) {
      temp = x1;
      x1 = x2;
      x2 = temp;
    }

    return _brentRootFinder.getRoot(function, x1, x2);
  }

  private SecantResultData secantMethod(final Function1D<Double, Double> function, final double lowerBound, final double upperBound, final double xLow, final double xHigh, final double yLow,
      final double yHigh) {

    double x1, x2, x3, y1, y2, y3, dx, temp;

    x1 = xLow;
    x3 = xHigh;
    y1 = yLow;
    y3 = yHigh;

    for (int i = 0; i < MAX_ITER; ++i) {

      if (Math.abs(y1) > Math.abs(y3)) {
        temp = x1;
        x1 = x3;
        x3 = temp;
        temp = y1;
        y1 = y3;
        y3 = temp;
      }

      dx = Math.abs(y1 - y3) <= _tolerance ? y1 - y3 > 0 ? -y1 * (x1 - x3) / _tolerance : y1 * (x1 - x3) / _tolerance : (x3 - x1) * y1 / (y1 - y3);

      x2 = x1 + dx;

      if (x2 < lowerBound || x2 > upperBound) {
        // Root cannot be found or bracketed
        return new SecantResultData();
      }

      y2 = function.evaluate(x2);

      if (Math.abs(y2) <= _tolerance && (Math.abs(x2 - lowerBound) <= _tolerance || Math.abs(x2 - upperBound) <= _tolerance)) {
        // Root found
        return new SecantResultData(x2);
      }

      if ((y1 < 0.0 && y2 < 0.0 && y3 < 0.0) || (y1 > 0.0 && y2 > 0.0 && y3 > 0.0)) {

        if (y2 > y1) {
          temp = x3;
          x3 = x1;
          x1 = temp;
          temp = y3;
          y3 = y1;
          y1 = temp;
          temp = x2;
          x2 = x1;
          x1 = temp;
          temp = y2;
          y2 = y1;
          y1 = temp;
        } else {
          temp = x3;
          x3 = x2;
          x2 = temp;
          temp = y3;
          y3 = y2;
          y2 = temp;
        }

        continue;

      } else {

        if (y1 * y3 > 0.0) {

          if (x2 > x1) {
            temp = x1;
            x1 = x2;
            x2 = temp;
            temp = y1;
            y1 = y2;
            y2 = temp;
          } else {
            temp = x2;
            x2 = x3;
            x3 = temp;
            temp = y2;
            y2 = y3;
            y3 = temp;
          }
        }

        // Interval found
        return new SecantResultData(x1, x3);
      }
    }

    // Root not found or bracketed
    return new SecantResultData();
  }

  private static class SecantResultData {

    private final SecantResult _result;
    private final double _root;
    private final double _lower, _upper;

    public SecantResult getResult() {
      return _result;
    }

    public double getRoot() {
      return _root;
    }

    public double getLower() {
      return _lower;
    }

    public double getUpper() {
      return _upper;
    }

    public SecantResultData(final double root) {
      this._result = SecantResult.FOUND;
      this._root = root;
      this._lower = Double.NaN;
      this._upper = Double.NaN;
    }

    public SecantResultData(final double lower, final double upper) {
      this._result = SecantResult.BRACKETED;
      this._root = Double.NaN;
      this._lower = lower;
      this._upper = upper;
    }

    public SecantResultData() {
      this._result = SecantResult.NOT_FOUND;
      this._root = Double.NaN;
      this._lower = Double.NaN;
      this._upper = Double.NaN;
    }
  }

  private enum SecantResult {
    FOUND, BRACKETED, NOT_FOUND
  };
}
