/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.functions.utilities;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

/**
 * Uniques a vector. i.e. parses a vector and removes repeat values.
 * Two implementations: strict uniqueness - bit patterns used
 *                    : unique to tolerance - unique at a specified precision
 * TODO: Code is pretty horrible and inefficient, merge sort with remove dupes would be better
 */
public final class Unique {

  /**
   * Uniques the data, duplicates are removed based on bitwise comparison of data.
   * @param data int[] the data to unique
   * @return the int[] uniqued data
   */
  public static int[] bitwise(int[] data) {
    Validate.notNull(data);
    int n = data.length;
    int[] sorteddata = Sort.stateless(data);
    int j = 0;
    for (int i = 1; i < n; i++) {
      if (sorteddata[i] != sorteddata[j]) {
        j++;
        sorteddata[j] = sorteddata[i];
      }
    }
    return Arrays.copyOf(sorteddata, j + 1);
  }

  /**
   * Uniques the data, duplicates are removed based on bitwise comparison of data.
   * @param data long[] the data to unique
   * @return the long[] uniqued data
   */
  public static long[] bitwise(long[] data) {
    Validate.notNull(data);
    int n = data.length;
    long[] sorteddata = Sort.stateless(data);
    int j = 0;
    for (int i = 1; i < n; i++) {
      if (sorteddata[i] != sorteddata[j]) {
        j++;
        sorteddata[j] = sorteddata[i];
      }
    }
    return Arrays.copyOf(sorteddata, j + 1);
  }


  /* floats can be uniqued by bit match or by being within some tolerance */
  /**
   * Uniques the data, duplicates are removed based on bitwise comparison of data.
   * @param data float[] the data to unique
   * @return the float[] uniqued data
   */
  public static float[] bitwise(float[] data) {
    Validate.notNull(data);
    int n = data.length;
    float[] sorteddata = Sort.stateless(data);
    int j = 0;
    for (int i = 1; i < n; i++) {
      if (Float.floatToIntBits(sorteddata[i]) != Float.floatToIntBits(sorteddata[j])) {
        j++;
        sorteddata[j] = sorteddata[i];
      }
    }
    return Arrays.copyOf(sorteddata, j + 1);
  }

  /**
   * Uniques the data, duplicates are removed based on their absolute difference in magnitude (set based on native type)
   * See also byTol(native[] data, float tol).
   * @param data float[] the data
   * @return the float[] uniqued data
   */
  public static float[] byTol(float[] data) {
    return byTol(data, 1e-7f);
  }

/**
 * Uniques the data, duplicates are removed based on their absolute difference in magnitude with respect to tol
 * @param data float[] the data
 * @param tol the tolerance for two numbers being considered identical
 * @return the float[] uniqued data
 */
  public static float[] byTol(float[] data, float tol) {
    Validate.notNull(data);
    int n = data.length;
    float[] sorteddata = Sort.stateless(data);
    int j = 0;
    for (int i = 1; i < n; i++) {
      if (!(Math.abs(sorteddata[i] - sorteddata[j]) < tol)) {
        j++;
        sorteddata[j] = sorteddata[i];
      }
    }
    return Arrays.copyOf(sorteddata, j + 1);
  }


  /* doubles can be uniqued by bit match or by being within some tolerance */
  /**
   * Uniques the data, duplicates are removed based on bitwise comparison of data.
   * @param data double[] the data to unique
   * @return the double[] uniqued data
   */
  public static double[] bitwise(double[] data) {
    Validate.notNull(data);
    int n = data.length;
    double[] sorteddata = Sort.stateless(data);
    int j = 0;
    for (int i = 1; i < n; i++) {
      if (Double.doubleToLongBits(sorteddata[i]) != Double.doubleToLongBits(sorteddata[j])) {
        j++;
        sorteddata[j] = sorteddata[i];
      }
    }
    return Arrays.copyOf(sorteddata, j + 1);
  }

  /**
   * Uniques the data, duplicates are removed based on their absolute difference in magnitude (set based on native type)
   * See also byTol(native[] data, double tol).
   * @param data double[] the data
   * @return the double[] uniqued data
   */
  public static double[] byTol(double[] data) {
    return byTol(data, 1e-15);
  }

/**
 * Uniques the data, duplicates are removed based on their absolute difference in magnitude with respect to tol
 * @param data double[] the data
 * @param tol the tolerance for two numbers being considered identical
 * @return the double[] uniqued data
 */
  public static double[] byTol(double[] data, double tol) {
    Validate.notNull(data);
    int n = data.length;
    double[] sorteddata = Sort.stateless(data);
    int j = 0;
    for (int i = 1; i < n; i++) {
      if (!(Math.abs(sorteddata[i] - sorteddata[j]) < tol)) {
        j++;
        sorteddata[j] = sorteddata[i];
      }
    }
    return Arrays.copyOf(sorteddata, j + 1);
  }


} // end class
