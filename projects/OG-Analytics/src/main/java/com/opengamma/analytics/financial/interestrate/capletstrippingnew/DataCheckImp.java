/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstrippingnew;

import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class DataCheckImp {

  /**
   * Check data 
   * @param expiries expiries in ascending order
   * @param strikes each entry in the outer array is an array of strikes (in ascending order) corresponding to an expiry 
   * @param forwards the forwards corresponding to the expiries 
   * @return total number of strikes
   */
  public int checkData(final double[] expiries, final double[][] strikes, final double[] forwards) {
    ArgumentChecker.notEmpty(expiries, "expiries");
    ArgumentChecker.notEmpty(forwards, "forwards");
    ArgumentChecker.noNulls(strikes, "strikes");
    final int nExp = expiries.length;
    ArgumentChecker.isTrue(nExp == forwards.length, "wrong number of forwards");
    ArgumentChecker.isTrue(nExp == strikes.length, "wrong number of strikes");
    int count = 0;
    ArgumentChecker.isTrue(expiries[0] > 0, "first expiry negative");
    for (int i = 0; i < nExp; i++) {
      if (i > 0) {
        ArgumentChecker.isTrue(expiries[i] > expiries[i - 1], "expiries not assending");
      }
      final double[] s = strikes[i];
      final int nStrikes = s.length;
      ArgumentChecker.isTrue(s[0] > 0, "first strike at {}, negative", expiries[i]);
      count += nStrikes;
      for (int j = 0; j < nStrikes; j++) {
        if (j > 0) {
          ArgumentChecker.isTrue(s[j] > s[j - 1], "strikes not assending");
        }
      }
    }
    return count;
  }

}
