/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.functions.utilities;

import org.apache.commons.lang.Validate;

/**
 * Computes the sum of a vector.
 * e.g. if a vector contains elements {1,2,3,4,5} then Sum(vector) will return 1+2+3+4+5.
 */
public class Sum {

  /**
   * Sums over all indices of a given vector (classic vector reduction)
   * @param v the vector over which the sum will be undertaken.
   * @return the sum of vector v
   */
  public static double overAllIndices(double[] v) {
    Validate.notNull(v);
    double tmp = 0;
    final int n = v.length;
    for (int i = 0; i < n; i++) {
      tmp += v[i];
    }
    return tmp;
  }

  /**
   * Sums over selected indices of a given vector
   * @param v the vector over which the sum will be undertaken
   * @param idx the indices to be included in the sum
   * @return the sum of dereferenced indices of vector v
   */
  public static double overIndices(double[] v, int[] idx) {
    Validate.notNull(v);
    Validate.notNull(idx);
    final int n = v.length;
    Validate.isTrue(Max.value(idx) < n, "Index out of range. Max of indexes is " + Max.value(idx) + " whereas vector to dereference is length " + n);
    Validate.isTrue(Min.value(idx) > -1, "Negative index dereference requested on vector");
    double tmp = 0;
    final int idxn = idx.length;
    for (int i = 0; i < idxn; i++) {
      tmp += v[idx[i]];
    }
    return tmp;
  }

  /**
   * Sums over a selected range indices of a given vector
   * Will do SUM(v(low:high))
   * @param v the vector over which the sum will be undertaken
   * @param low the lowest element of the range
   * @param high the highest element of the range
   * @return the sum of dereferenced specified range of vector v
   */
  public static double overRange(double[] v, int low, int high) {
    Validate.notNull(v);
    final int n = v.length;
    Validate.isTrue(high < n, "Input \"high\" index out of range. " + high + " whereas vector to dereference is length " + n);
    Validate.isTrue(low > -1, "Negative index dereference requested on vector");
    Validate.isTrue(high >= low, "high value is lower than low value, negative range not supported.");
    double tmp = 0;
    for (int i = low; i <= high; i++) {
      tmp += v[i];
    }
    return tmp;
  }

  /**
   * Sums over all indices of a given vector (classic vector reduction)
   * @param v the vector over which the sum will be undertaken.
   * @return the sum of vector v
   */
  public static float overAllIndices(float[] v) {
    Validate.notNull(v);
    float tmp = 0;
    final int n = v.length;
    for (int i = 0; i < n; i++) {
      tmp += v[i];
    }
    return tmp;
  }

  /**
   * Sums over selected indices of a given vector
   * @param v the vector over which the sum will be undertaken
   * @param idx the indices to be included in the sum
   * @return the sum of dereferenced indices of vector v
   */
  public static float overIndices(float[] v, int[] idx) {
    Validate.notNull(v);
    Validate.notNull(idx);
    final int n = v.length;
    Validate.isTrue(Max.value(idx) < n, "Index out of range. Max of indexes is " + Max.value(idx) + " whereas vector to dereference is length " + n);
    Validate.isTrue(Min.value(idx) > -1, "Negative index dereference requested on vector");
    float tmp = 0;
    final int idxn = idx.length;
    for (int i = 0; i < idxn; i++) {
      tmp += v[idx[i]];
    }
    return tmp;
  }

  /**
   * Sums over a selected range indices of a given vector
   * Will do SUM(v(low:high))
   * @param v the vector over which the sum will be undertaken
   * @param low the lowest element of the range
   * @param high the highest element of the range
   * @return the sum of dereferenced specified range of vector v
   */
  public static float overRange(float[] v, int low, int high) {
    Validate.notNull(v);
    final int n = v.length;
    Validate.isTrue(high < n, "Input \"high\" index out of range. " + high + " whereas vector to dereference is length " + n);
    Validate.isTrue(low > -1, "Negative index dereference requested on vector");
    Validate.isTrue(high >= low, "high value is lower than low value, negative range not supported.");
    float tmp = 0;
    for (int i = low; i <= high; i++) {
      tmp += v[i];
    }
    return tmp;
  }

  /**
   * Sums over all indices of a given vector (classic vector reduction)
   * @param v the vector over which the sum will be undertaken.
   * @return the sum of vector v
   */
  public static long overAllIndices(long[] v) {
    Validate.notNull(v);
    long tmp = 0;
    final int n = v.length;
    for (int i = 0; i < n; i++) {
      tmp += v[i];
    }
    return tmp;
  }

  /**
   * Sums over selected indices of a given vector
   * @param v the vector over which the sum will be undertaken
   * @param idx the indices to be included in the sum
   * @return the sum of dereferenced indices of vector v
   */
  public static long overIndices(long[] v, int[] idx) {
    Validate.notNull(v);
    Validate.notNull(idx);
    final int n = v.length;
    Validate.isTrue(Max.value(idx) < n, "Index out of range. Max of indexes is " + Max.value(idx) + " whereas vector to dereference is length " + n);
    Validate.isTrue(Min.value(idx) > -1, "Negative index dereference requested on vector");
    long tmp = 0;
    final int idxn = idx.length;
    for (int i = 0; i < idxn; i++) {
      tmp += v[idx[i]];
    }
    return tmp;
  }

  /**
   * Sums over a selected range indices of a given vector
   * Will do SUM(v(low:high))
   * @param v the vector over which the sum will be undertaken
   * @param low the lowest element of the range
   * @param high the highest element of the range
   * @return the sum of dereferenced specified range of vector v
   */
  public static long overRange(long[] v, int low, int high) {
    Validate.notNull(v);
    final int n = v.length;
    Validate.isTrue(high < n, "Input \"high\" index out of range. " + high + " whereas vector to dereference is length " + n);
    Validate.isTrue(low > -1, "Negative index dereference requested on vector");
    Validate.isTrue(high >= low, "high value is lower than low value, negative range not supported.");
    long tmp = 0;
    for (int i = low; i <= high; i++) {
      tmp += v[i];
    }
    return tmp;
  }

  /**
   * Sums over all indices of a given vector (classic vector reduction)
   * @param v the vector over which the sum will be undertaken.
   * @return the sum of vector v
   */
  public static int overAllIndices(int[] v) {
    Validate.notNull(v);
    int tmp = 0;
    final int n = v.length;
    for (int i = 0; i < n; i++) {
      tmp += v[i];
    }
    return tmp;
  }

  /**
   * Sums over selected indices of a given vector
   * @param v the vector over which the sum will be undertaken
   * @param idx the indices to be included in the sum
   * @return the sum of dereferenced indices of vector v
   */
  public static int overIndices(int[] v, int[] idx) {
    Validate.notNull(v);
    Validate.notNull(idx);
    final int n = v.length;
    Validate.isTrue(Max.value(idx) < n, "Index out of range. Max of indexes is " + Max.value(idx) + " whereas vector to dereference is length " + n);
    Validate.isTrue(Min.value(idx) > -1, "Negative index dereference requested on vector");
    int tmp = 0;
    final int idxn = idx.length;
    for (int i = 0; i < idxn; i++) {
      tmp += v[idx[i]];
    }
    return tmp;
  }

  /**
   * Sums over a selected range indices of a given vector
   * Will do SUM(v(low:high))
   * @param v the vector over which the sum will be undertaken
   * @param low the lowest element of the range
   * @param high the highest element of the range
   * @return the sum of dereferenced specified range of vector v
   */
  public static int overRange(int[] v, int low, int high) {
    Validate.notNull(v);
    final int n = v.length;
    Validate.isTrue(high < n, "Input \"high\" index out of range. " + high + " whereas vector to dereference is length " + n);
    Validate.isTrue(low > -1, "Negative index dereference requested on vector");
    Validate.isTrue(high >= low, "high value is lower than low value, negative range not supported.");
    int tmp = 0;
    for (int i = low; i <= high; i++) {
      tmp += v[i];
    }
    return tmp;
  }

}
