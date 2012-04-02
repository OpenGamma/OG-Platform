/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr;

import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public abstract class SmileSurfaceDataBundle {

  public abstract double[] getExpiries();

  public abstract double[][] getStrikes();

  public abstract double[][] getVolatilities();

  public abstract double[] getForwards();

  public abstract ForwardCurve getForwardCurve();

  public abstract boolean isCallData();

  public abstract SmileSurfaceDataBundle withBumpedPoint(int expiryIndex, int strikeIndex, double amount);

  protected void checkVolatilities(final double[] expiries, final double[][] vols) {
    final int nExpiries = expiries.length;
    final int n = vols[0].length;
    for (int i = 0; i < n; i++) {
      final double[] intVar = new double[nExpiries];
      for (int j = 0; j < nExpiries; j++) {
        final double vol = vols[j][i];
        intVar[j] = vol * vol * expiries[j];
        if (j > 0) {
          ArgumentChecker.isTrue(intVar[j] >= intVar[j - 1], "integrated variance not increasing, have {}, {}", intVar[j - 1], intVar[j]);
        }
      }
    }

  }
}
