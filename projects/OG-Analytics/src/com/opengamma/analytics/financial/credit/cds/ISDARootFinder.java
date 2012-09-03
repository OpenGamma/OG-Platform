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
 * Numerical solver replicating the ISDA Brent root finder algorithm
 */
public class ISDARootFinder {
  
  private static final int MAX_ITER = 100;
  
  private static final BrentSingleRootFinder brent = new BrentSingleRootFinder(1e-18);

  public static double findRoot(final Function1D<Double, Double> function, final double xLower, final double xUpper, final double xGuess,
      final double initialStep, final double initialDeriv, final double xTolerance, double yTolerance) {

    ArgumentChecker.isTrue(xUpper > xLower, "Upper bound must be greater than lower bound");
    ArgumentChecker.isTrue(xGuess >= xLower, "Guess is out of range");
    ArgumentChecker.isTrue(xGuess <= xUpper, "Guess is out of range");

    double x1, x2, x3, y1, y2, y3, temp;
    
    x1 = xGuess;
    y1 = function.evaluate(x1);

    if (Math.abs(y1) <= yTolerance && (Math.abs(x1 - xLower) <= xTolerance || Math.abs(x1 - xUpper) <= xTolerance)) {
      return y1;
    }
    
    x3 = initialDeriv == 0.0 ? xGuess + initialStep : xGuess - (y1 / initialDeriv);

    if (x3 < xLower || x3 > xUpper) {
      final double x_ = xGuess - initialStep;
      final double boundSpread = xUpper - xLower;

      x3 =
        x_ < xLower ? xLower :
        x_ > xUpper ? xUpper :
        x_;

      if (x3 == x1) {
        x3 = x3 == xLower ? xLower + 0.01 * boundSpread : xUpper - 0.01 * boundSpread;
      }
    }
    
    y3 = function.evaluate(x3);
    
    if (Math.abs(y3) <= yTolerance && (Math.abs(x3 - xLower) <= xTolerance || Math.abs(x3 - xUpper) <= xTolerance)) {
      return y3;
    }
    
    SecantResultData secant = secantMethod(function, xLower, xUpper, xTolerance, yTolerance, x1, x3, y1, y3);
    
    if (secant.result == SecantResult.FOUND) {
      return secant.root;
    }
    
    if (secant.result == SecantResult.BRACKETED) {
      x1 = secant.lower;
      x3 = secant.upper;
    } else {
      
      final double yLo = function.evaluate(xLower);
      final double yHi = function.evaluate(xUpper);
      
      if (Math.abs(yLo) <= yTolerance && Math.abs(xLower - x1) <= xTolerance) {
        return xLower;
      }
      
      if (Math.abs(yHi) <= yTolerance && Math.abs(xUpper - x1) <= xTolerance) {
        return xUpper;
      }
      
      if (y1 * yLo < 0.0) {
        x3 = x1;
        x1 = xLower;
        y3 = y1;
        y1 = yLo;
      } else if (y1 * yHi < 0.0) {
        x3 = xUpper;
        y3 = yHi;
      } else {
        throw new OpenGammaRuntimeException("Failed to find root");
      } 
    }
    
    if (x1 > x3) {
      temp = x1; x1 = x3; x3 = temp;
    }

    return brent.getRoot(function, x1, x3);
  }
  
  private enum SecantResult { FOUND, BRACKETED, NOT_FOUND };
  
  private static class SecantResultData {
    
    public final SecantResult result;
    public final double root;
    public final double lower, upper;
    
    public SecantResultData(final double root) {
      this.result = SecantResult.FOUND;
      this.root = root;
      this.lower = Double.NaN;
      this.upper = Double.NaN;
    }
    
    public SecantResultData(final double lower, final double upper) {
      this.result = SecantResult.BRACKETED;
      this.root = Double.NaN;
      this.lower = lower;
      this.upper = upper;
    }
    
    public SecantResultData() {
      this.result = SecantResult.NOT_FOUND;
      this.root = Double.NaN;
      this.lower = Double.NaN;
      this.upper = Double.NaN;
    }
  }
  
  public static SecantResultData secantMethod(final Function1D<Double, Double> function, final double xLower, final double xUpper, final double xTolerance, double yTolerance,
    final double xLow, final double xHigh, final double yLow, final double yHigh) {
    
    double x1, x2, x3, y1, y2, y3, dx, temp;
    
    x1 = xLow;
    x3 = xHigh;
    y1 = yLow;
    y3 = yHigh;
    
    for (int i = 0; i < MAX_ITER; ++i) {
      
      if (Math.abs(y1) > Math.abs(y3)) {
        temp = x1; x1 = x3; x3 = temp;
        temp = y1; y1 = y3; y3 = temp;
      }
      
      dx = Math.abs(y1 - y3) <= yTolerance
        ? y1 - y3 > 0
          ? -y1 * (x1 - x3) / yTolerance
          :  y1 * (x1 - x3) / yTolerance
        : (x3 - x1) * y1 / (y1 - y3);
      
      x2 = x1 + dx;
      
      if (x2 < xLower || x2 > xUpper) {
        return new SecantResultData();
      }
      
      y2 = function.evaluate(x2);
      
      if (Math.abs(y2) <= yTolerance && (Math.abs(x2 - xLower) <= xTolerance || Math.abs(x2 - xUpper) <= xTolerance)) {
        return new SecantResultData(x2);
      }
      
      if ((y1 < 0.0 && y2 < 0.0 && y3 < 0.0) || (y1 > 0.0 && y2 > 0.0 && y3 > 0.0)) {
        
        if (y2 > y1) {
          temp = x3; x3 = x1; x1 = temp;
          temp = y3; y3 = y1; y1 = temp;
          temp = x2; x2 = x1; x1 = temp;
          temp = y2; y2 = y1; y1 = temp;
        } else {
          temp = x3; x3 = x2; x2 = temp;
          temp = y3; y3 = y2; y2 = temp;
        }
     
        continue;
        
      } else {
        
        if (y1 * y3 > 0.0) {
          
          if (x2 > x1) {
            temp = x1; x1 = x2; x2 = temp;
            temp = y1; y1 = y2; y2 = temp;
          } else {
            temp = x2; x2 = x3; x3 = temp;
            temp = y2; y2 = y3; y3 = temp;
          }
        }
        
        return new SecantResultData(x1, x3);
      }
    }
    
    // Root not found or bracketed
    return new SecantResultData();
  }
}




































