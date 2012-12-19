/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function1D;

/**
 * 
 */
public abstract class MeshingFunction extends Function1D<Integer, Double> {

  private final int _nPoints;

  protected MeshingFunction(final int nPoints) {
    Validate.isTrue(nPoints > 1, "Need more than 1 point for a mesh");
    _nPoints = nPoints;
  }

  public int getNumberOfPoints() {
    return _nPoints;
  }

  public double[] getPoints() {
    double[] res = new double[_nPoints];
    for (int i = 0; i < _nPoints; i++) {
      res[i] = evaluate(i);
    }
    return res;
  }

  /**
   * Same behaviour as mathlab unique 
   * @param in input array
   * @return a sorted array with no duplicates values 
   */
  protected double[] unique(final double[] in) {
    Arrays.sort(in);
    final int n = in.length;
    double[] temp = new double[n];
    temp[0] = in[0];
    int count = 1;
    for (int i = 1; i < n; i++) {
      if (in[i] != in[i - 1]) {
        temp[count++] = in[i];
      }
    }
    if (count == n) {
      return temp;
    }
    return Arrays.copyOf(temp, count);
  }

  /**
   * Same behaviour as mathlab unique 
   * @param in input array
   * @return a sorted array with no duplicates values 
   */
  protected int[] unique(final int[] in) {
    Arrays.sort(in);
    final int n = in.length;
    int[] temp = new int[n];
    temp[0] = in[0];
    int count = 1;
    for (int i = 1; i < n; i++) {
      if (in[i] != in[i - 1]) {
        temp[count++] = in[i];
      }
    }
    if (count == n) {
      return temp;
    }
    return Arrays.copyOf(in, count);
  }

}
