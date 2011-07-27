/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model;

import java.util.List;

/**
 * 
 */
public class FunctionUtils {

  public static double[][] decodeJacobian(final Object jacobianObject) {
    final double[][] array;
    // Fudge encodings of double[][] and List<double[]> are identical, so receiving either is valid.
    if (jacobianObject instanceof double[][]) {
      array = (double[][]) jacobianObject;
    } else if (jacobianObject instanceof List<?>) {
      @SuppressWarnings("unchecked")
      final List<double[]> parRateJacobianList = (List<double[]>) jacobianObject;
      final int rows = parRateJacobianList.size();
      array = new double[rows][];
      int i = 0;
      for (final double[] d : parRateJacobianList) {
        array[i++] = d;
      }
    } else {
      throw new ClassCastException("Jacobian object " + jacobianObject + " not List<double[]> or double[][]");
    }
    return array;
  }
}
