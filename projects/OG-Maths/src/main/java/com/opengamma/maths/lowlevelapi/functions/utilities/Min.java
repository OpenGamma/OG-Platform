/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.functions.utilities;

import org.apache.commons.lang.Validate;

/**
 * Finds information about the minimum value within a vector of primitives.
 */
public class Min {

  /**
   * Returns the minimum value in data
   * @param data the data to search
   * @return min, the minimum
   */
  public static int value(int... data) {
    Validate.notNull(data);
    int min = 0x7fffffff;
    final int n = data.length;
    for (int i = 0; i < n; i++) {
      if (data[i] < min) {
        min = data[i];
      }
    }
    return min;
  }

  /**
   * Returns the index of the minimum value in data
   * @param data the data to search
   * @return idx, the index of the minimum value in the data
   */
  public static int index(int... data) {
    Validate.notNull(data);
    int min = 0x7fffffff;
    int idx = -1;
    final int n = data.length;
    for (int i = 0; i < n; i++) {
      if (data[i] < min) {
        min = data[i];
        idx = i;
      }
    }
    return idx;
  }

  /**
   * Returns the minimum value in data
   * @param data the data to search
   * @return min, the minimum
   */

  public static long value(long... data) {
    Validate.notNull(data);
    long min = 0x7fffffff;
    final long n = data.length;
    for (int i = 0; i < n; i++) {
      if (data[i] < min) {
        min = data[i];
      }
    }
    return min;
  }

  /**
   * Returns the index of the minimum value in data
   * @param data the data to search
   * @return idx, the index of the minimum value in the data
   */
  public static int index(long... data) {
    Validate.notNull(data);
    long min = 0x7fffffff;
    int idx = -1;
    final int n = data.length;
    for (int i = 0; i < n; i++) {
      if (data[i] < min) {
        min = data[i];
        idx = i;
      }
    }
    return idx;
  }

  /**
   * Returns the minimum value in data
   * @param data the data to search
   * @return min, the minimum
   */
  public static float value(float... data) {
    Validate.notNull(data);
    float min = Float.MAX_VALUE;
    final int n = data.length;
    for (int i = 0; i < n; i++) {
      if (data[i] < min) {
        min = data[i];
      }
    }
    return min;
  }

  /**
   * Returns the index of the minimum value in data
   * @param data the data to search
   * @return idx, the index of the minimum value in the data
   */
  public static int index(float... data) {
    Validate.notNull(data);
    float min = Float.MAX_VALUE;
    int idx = -1;
    final int n = data.length;
    for (int i = 0; i < n; i++) {
      if (data[i] < min) {
        min = data[i];
        idx = i;
      }
    }
    return idx;
  }

  /**
   * Returns the minimum value in data
   * @param data the data to search
   * @return min, the minimum
   */
  public static double value(double... data) {
    Validate.notNull(data);
    double min = Double.MAX_VALUE;
    final int n = data.length;
    for (int i = 0; i < n; i++) {
      if (data[i] < min) {
        min = data[i];
      }
    }
    return min;
  }

  /**
   * Returns the index of the minimum value in data
   * @param data the data to search
   * @return idx, the index of the minimum value in the data
   */
  public static int index(double... data) {
    Validate.notNull(data);
    double min = Double.MAX_VALUE;
    int idx = -1;
    final int n = data.length;
    for (int i = 0; i < n; i++) {
      if (data[i] < min) {
        min = data[i];
        idx = i;
      }
    }
    return idx;
  }

}
