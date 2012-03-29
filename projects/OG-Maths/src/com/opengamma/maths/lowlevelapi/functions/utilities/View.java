/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.functions.utilities;

import org.apache.commons.lang.Validate;

/**
 * Sets a view on a set of data. Basically the supplied indexes are looked up within the data
 * and the corresponding data is returned.
 */
public class View {

  /**
   * Looks up the values in v at positions given by index and returns them!
   * @param v the vector
   * @param index the indices to look up in the vector
   * @return tmp the values looked up by index. i.e. values[index];
   */
  public static double[] byIndex(double[] v, int[] index) {
    Validate.notNull(v);
    Validate.notNull(index);
    Validate.isTrue(Max.value(index) < v.length);
    Validate.isTrue(Min.value(index) > -1);
    final int idxn = index.length;
    double[] tmp = new double[idxn];
    for (int i = 0; i < idxn; i++) {
      tmp[i] = v[index[i]];
    }
    return tmp;
  }

  /**
   * Looks up the values in v at positions given by index and returns them!
   * @param v the vector
   * @param low the lowest element of the range
   * @param high the highest element of the range
   * @return tmp the values looked up by range. i.e. values[low:high];
   */
  public static double[] byRange(double[] v, int low, int high) {
    Validate.notNull(v);
    final int n = v.length;
    Validate.isTrue(high < n, "Input \"high\" index out of range. " + high + " whereas vector to dereference is length " + n);
    Validate.isTrue(low > -1, "Negative index dereference requested on vector");
    Validate.isTrue(high >= low, "high value is lower than low value, negative range not supported.");
    double[] tmp = new double[high - low + 1]; // fence post
    int ptr = 0;
    for (int i = low; i <= high; i++) {
      tmp[ptr] = v[i];
      ptr++;
    }
    return tmp;
  }

  /**
   * Looks up the values in v at positions given by index and returns them!
   * @param v the vector
   * @param index the indices to look up in the vector
   * @return tmp the values looked up by index. i.e. values[index];
   */
  public static float[] byIndex(float[] v, int[] index) {
    Validate.notNull(v);
    Validate.notNull(index);
    Validate.isTrue(Max.value(index) < v.length);
    Validate.isTrue(Min.value(index) > -1);
    final int idxn = index.length;
    float[] tmp = new float[idxn];
    for (int i = 0; i < idxn; i++) {
      tmp[i] = v[index[i]];
    }
    return tmp;
  }

  /**
   * Looks up the values in v at positions given by index and returns them!
   * @param v the vector
   * @param low the lowest element of the range
   * @param high the highest element of the range
   * @return tmp the values looked up by range. i.e. values[low:high];
   */
  public static float[] byRange(float[] v, int low, int high) {
    Validate.notNull(v);
    final int n = v.length;
    Validate.isTrue(high < n, "Input \"high\" index out of range. " + high + " whereas vector to dereference is length " + n);
    Validate.isTrue(low > -1, "Negative index dereference requested on vector");
    Validate.isTrue(high >= low, "high value is lower than low value, negative range not supported.");
    float[] tmp = new float[high - low + 1]; // fence post
    int ptr = 0;
    for (int i = low; i <= high; i++) {
      tmp[ptr] = v[i];
      ptr++;
    }
    return tmp;
  }

  /**
   * Looks up the values in v at positions given by index and returns them!
   * @param v the vector
   * @param index the indices to look up in the vector
   * @return tmp the values looked up by index. i.e. values[index];
   */
  public static long[] byIndex(long[] v, int[] index) {
    Validate.notNull(v);
    Validate.notNull(index);
    Validate.isTrue(Max.value(index) < v.length);
    Validate.isTrue(Min.value(index) > -1);
    final int idxn = index.length;
    long[] tmp = new long[idxn];
    for (int i = 0; i < idxn; i++) {
      tmp[i] = v[index[i]];
    }
    return tmp;
  }

  /**
   * Looks up the values in v at positions given by index and returns them!
   * @param v the vector
   * @param low the lowest element of the range
   * @param high the highest element of the range
   * @return tmp the values looked up by range. i.e. values[low:high];
   */
  public static long[] byRange(long[] v, int low, int high) {
    Validate.notNull(v);
    final int n = v.length;
    Validate.isTrue(high < n, "Input \"high\" index out of range. " + high + " whereas vector to dereference is length " + n);
    Validate.isTrue(low > -1, "Negative index dereference requested on vector");
    Validate.isTrue(high >= low, "high value is lower than low value, negative range not supported.");
    long[] tmp = new long[high - low + 1]; // fence post
    int ptr = 0;
    for (int i = low; i <= high; i++) {
      tmp[ptr] = v[i];
      ptr++;
    }
    return tmp;
  }

  /**
   * Looks up the values in v at positions given by index and returns them!
   * @param v the vector
   * @param index the indices to look up in the vector
   * @return tmp the values looked up by index. i.e. values[index];
   */
  public static int[] byIndex(int[] v, int[] index) {
    Validate.notNull(v);
    Validate.notNull(index);
    Validate.isTrue(Max.value(index) < v.length);
    Validate.isTrue(Min.value(index) > -1);
    final int idxn = index.length;
    int[] tmp = new int[idxn];
    for (int i = 0; i < idxn; i++) {
      tmp[i] = v[index[i]];
    }
    return tmp;
  }

  /**
   * Looks up the values in v at positions given by index and returns them!
   * @param v the vector
   * @param low the lowest element of the range
   * @param high the highest element of the range
   * @return tmp the values looked up by range. i.e. values[low:high];
   */
  public static int[] byRange(int[] v, int low, int high) {
    Validate.notNull(v);
    final int n = v.length;
    Validate.isTrue(high < n, "Input \"high\" index out of range. " + high + " whereas vector to dereference is length " + n);
    Validate.isTrue(low > -1, "Negative index dereference requested on vector");
    Validate.isTrue(high >= low, "high value is lower than low value, negative range not supported.");
    int[] tmp = new int[high - low + 1]; // fence post
    int ptr = 0;
    for (int i = low; i <= high; i++) {
      tmp[ptr] = v[i];
      ptr++;
    }
    return tmp;
  }

}
