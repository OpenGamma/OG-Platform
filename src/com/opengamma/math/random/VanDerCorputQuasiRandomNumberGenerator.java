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
  private int _base;

  public VanDerCorputQuasiRandomNumberGenerator(final int base) {
    if (base < 2)
      throw new IllegalArgumentException("Base must be greater than or equal to two");
    _base = base;
  }

  public void setBase(final int base) {
    if (base < 2)
      throw new IllegalArgumentException("Base must be greater than or equal to two");
    _base = base;
  }

  @Override
  public List<Double[]> getVectors(final int dimension, final int n) {
    if (dimension < 0)
      throw new IllegalArgumentException("Dimension must be greater than zero");
    if (n < 0)
      throw new IllegalArgumentException("Number of values must be greater than zero");
    if (dimension != 1) {
      s_Log.info("Van der Corput sequences are one-dimensional only: ignoring other " + (dimension - 1) + " dimension(s)");
    }
    final List<Double[]> result = new ArrayList<Double[]>();
    Double x;
    for (int i = 1; i < n + 1; i++) {
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
      x = 0.;
      for (int j = m; j >= 0; j--) {
        x += a[j] / power[j + 1];
      }
      result.add(new Double[] { x });
    }
    return result;
  }
}
