/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model;

import java.util.List;

import com.opengamma.analytics.math.matrix.DoubleMatrix2D;

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
      final List<double[]> jacobianList = (List<double[]>) jacobianObject;
      final int rows = jacobianList.size();
      array = new double[rows][];
      int i = 0;
      for (final double[] d : jacobianList) {
        array[i++] = d;
      }
    } else if (jacobianObject instanceof DoubleMatrix2D) {
      array = ((DoubleMatrix2D) jacobianObject).getData();
    } else {
      throw new ClassCastException("Jacobian object " + jacobianObject + " not List<double[]> or double[][]; have " + jacobianObject.getClass());
    }
    return array;
  }

  public static double[] decodeCouponSensitivities(final Object couponSensitivitiesObject) {
    final double[] array;
    // Fudge encodings of double[] and List<Double> are identical, so receiving either is valid.
    if (couponSensitivitiesObject instanceof double[]) {
      array = (double[]) couponSensitivitiesObject;
    } else if (couponSensitivitiesObject instanceof List<?>) {
      @SuppressWarnings("unchecked")
      final List<Double> couponSensitivitiesList = (List<Double>) couponSensitivitiesObject;
      final int rows = couponSensitivitiesList.size();
      array = new double[rows];
      int i = 0;
      for (final Double d : couponSensitivitiesList) {
        array[i++] = d;
      }
    } else {
      throw new ClassCastException("Coupon sensitivities object " + couponSensitivitiesObject + " not List<Double> or double[]");
    }
    return array;
  }
}
