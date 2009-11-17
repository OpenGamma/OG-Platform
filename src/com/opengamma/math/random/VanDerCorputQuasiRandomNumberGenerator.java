/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.random;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author emcleod
 */
public class VanDerCorputQuasiRandomNumberGenerator implements QuasiRandomNumberGenerator {
  private static final Logger s_Log = LoggerFactory.getLogger(VanDerCorputQuasiRandomNumberGenerator.class);
  private final int _base;

  public VanDerCorputQuasiRandomNumberGenerator(final int base) {
    _base = base;
  }

  @Override
  public List<Double[]> getQuasiRandomVectors(final int dimension, final int n) {
    if (dimension != 1) {
      s_Log.info("Van der Corput sequences are one-dimensional only: not using dimension " + dimension);
    }
    final Double[] x = new Double[n];
    for (int i = 1; i < n; i++) {
      final int m = (int) Math.floor(Math.log(i) / Math.log(_base));
      final int[] a = new int[m + 1];
      final double[] power = new double[m + 2];
      double number = i;
      power[m + 1] = Math.pow(_base, m + 1);
      power[m] = power[m + 1] / _base;
      for (int j = m; j >= 0; j--) {
        a[j] = (int) (number / power[j]);
        number = number % power[j];
        if (j > 0) {
          power[j - 1] = power[j] / _base;
        }
      }
      x[i - 1] = 0.;
      for (int j = m; j >= 0; j--) {
        x[i - 1] += a[j] / power[j + 1];
      }
      System.out.println(i + ": " + x[i - 1]);
    }
    final List<Double[]> result = new ArrayList<Double[]>();
    result.add(x);
    return result;
  }
}
