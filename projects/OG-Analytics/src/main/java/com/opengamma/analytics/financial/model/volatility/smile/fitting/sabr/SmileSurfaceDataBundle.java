/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public abstract class SmileSurfaceDataBundle {
  private static final Logger s_logger = LoggerFactory.getLogger(SmileSurfaceDataBundle.class);

  public abstract int getNumExpiries();

  public abstract double[] getExpiries();

  public abstract double[][] getStrikes();

  public abstract double[][] getVolatilities();

  public abstract double[] getForwards();

  public abstract ForwardCurve getForwardCurve();

  //  public abstract boolean isCallData();

  public abstract SmileSurfaceDataBundle withBumpedPoint(int expiryIndex, int strikeIndex, double amount);

  /**
   * Sanity check
   * Test that integrated variance is increasing as one moves out in expiries. This is just a proxy.
   * It compares various as one moves down a column, be it strike or Moneyness.
   * @param expiries Option expiries
   * @param vols Implied Volatilities
   */
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

  /**
   * Sanity check
   * Test that integrated variance is increasing as one moves out in expiries. This is just a proxy.
   * This version is to be used when expiry rows have different length, but share common strikes.
   * @param expiries Option expiries
   * @param strikes Available strikes for each expiry
   * @param vols Implied Volatilities, sharing dimension of strikes
   */
  protected void checkVolatilities(final double[] expiries, final double[][] strikes, final double[][] vols) {
    final int nExpiries = expiries.length;
    // Build a map keyed by the strike
    final HashMap<Double, ArrayList<Double>> strikeVarMap = new HashMap<>();
    for (int k = 0; k < strikes[0].length; k++) {
      final ArrayList<Double> intVar = new ArrayList<>();
      intVar.add(vols[0][k] * vols[0][k] * expiries[0]);
      strikeVarMap.put(strikes[0][k], intVar);
    }
    // Loop over expiries
    for (int i = 1; i < nExpiries; i++) {
      final int nK = strikes[i].length;
      for (int k = 0; k < nK; k++) {
        if (strikeVarMap.containsKey(strikes[i][k])) {
          // Add Vol to existing key
          strikeVarMap.get(strikes[i][k]).add(vols[i][k] * vols[i][k] * expiries[i]);
        } else { // Add new key
          final ArrayList<Double> intVar = new ArrayList<>();
          intVar.add(vols[i][k] * vols[i][k] * expiries[i]);
          strikeVarMap.put(strikes[i][k], intVar);
        }
      }
    }
    // Perform check by looping over strikes confirming that variance increases with expiry
    for (final Map.Entry<Double, ArrayList<Double>> entry : strikeVarMap.entrySet()) {
      final ArrayList<Double> intVar = entry.getValue();
      final int nVars = intVar.size();
      if (nVars > 1) {
        for (int t = 1; t < nVars; t++) {
          if (intVar.get(t) < intVar.get(t - 1)) {
            s_logger.error("Integrated variance not increasing, have (" + (t - 1) + "," + intVar.get(t - 1) + "),(" + t + "," + intVar.get(t) + ")");
          }
        }
      }
    }
  }
}
