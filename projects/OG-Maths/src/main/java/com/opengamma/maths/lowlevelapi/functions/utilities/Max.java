/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.functions.utilities;

import org.apache.commons.lang.Validate;

/**
 * Finds information about the maximum value within a vector of primitives.
 */
public class Max {

  /**
   * Returns the maximum value in data
   * @param data the data to search
   * @return max, the maximum
   */
  public static int value(int... data) {
    Validate.notNull(data);
    int max = 0x80000000;
    final int n = data.length;
    for (int i = 0; i < n; i++) {
      if (data[i] > max) {
        max = data[i];
      }
    }
    return max;
  }

  /**
   * Returns the index of the maximum value in data
   * @param data the data to search
   * @return idx, the index of the maximum value in the data
   */
  public static int index(int... data) {
    Validate.notNull(data);
    int max = 0x80000000;
    int idx = -1;
    final int n = data.length;
    for (int i = 0; i < n; i++) {
      if (data[i] > max) {
        max = data[i];
        idx = i;
      }
    }
    return idx;
  }

  /**
   * Returns the maximum value in data
   * @param data the data to search
   * @return max, the maximum
   */

  public static long value(long... data) {
    Validate.notNull(data);
    long max = 0x80000000;
    final long n = data.length;
    for (int i = 0; i < n; i++) {
      if (data[i] > max) {
        max = data[i];
      }
    }
    return max;
  }

  /**
   * Returns the index of the maximum value in data
   * @param data the data to search
   * @return idx, the index of the maximum value in the data
   */
  public static int index(long... data) {
    Validate.notNull(data);
    long max = 0x80000000;
    int idx = -1;
    final int n = data.length;
    for (int i = 0; i < n; i++) {
      if (data[i] > max) {
        max = data[i];
        idx = i;
      }
    }
    return idx;
  }

  /**
   * Returns the maximum value in data
   * @param data the data to search
   * @return max, the maximum
   */
  public static float value(float... data) {
    Validate.notNull(data);
    float max = Float.MIN_VALUE;
    final int n = data.length;
    for (int i = 0; i < n; i++) {
      if (data[i] > max) {
        max = data[i];
      }
    }
    return max;
  }

  /**
   * Returns the index of the maximum value in data
   * @param data the data to search
   * @return idx, the index of the maximum value in the data
   */
  public static int index(float... data) {
    Validate.notNull(data);
    float max = Float.MIN_VALUE;
    int idx = -1;
    final int n = data.length;
    for (int i = 0; i < n; i++) {
      if (data[i] > max) {
        max = data[i];
        idx = i;
      }
    }
    return idx;
  }

  /**
   * Returns the maximum value in data
   * @param data the data to search
   * @return max, the maximum
   */
  public static double value(double... data) {
    Validate.notNull(data);
    double max = Double.MIN_VALUE;
    final int n = data.length;
    for (int i = 0; i < n; i++) {
      if (data[i] > max) {
        max = data[i];
      }
    }
    return max;
  }

  /**
   * Returns the index of the maximum value in data
   * @param data the data to search
   * @return idx, the index of the maximum value in the data
   */
  public static int index(double... data) {
    Validate.notNull(data);
    double max = Double.MIN_VALUE;
    int idx = -1;
    final int n = data.length;
    for (int i = 0; i < n; i++) {
      if (data[i] > max) {
        max = data[i];
        idx = i;
      }
    }
    return idx;
  }

}
