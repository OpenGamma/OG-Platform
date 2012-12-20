/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.functions.utilities;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

/**
 * Reverses Vectors
 */
public class Reverse {

  /**
   * Reverses a vector in place
   * @param v1 the vector to be reversed
   */
  public static void inPlace(int[] v1) {
    Validate.notNull(v1);
    int tmp;
    final int half = v1.length / 2;
    final int len = v1.length - 1;
    for (int i = 0; i < half; i++) {
      tmp = v1[len - i];
      v1[len - i] = v1[i];
      v1[i] = tmp;
    }
  }

  /**
   * Reverses a vector in place
   * @param v1 the vector to be reversed
   */
  public static void inPlace(long[] v1) {
    Validate.notNull(v1);
    long tmp;
    final int half = v1.length / 2;
    final int len = v1.length - 1;
    for (int i = 0; i < half; i++) {
      tmp = v1[len - i];
      v1[len - i] = v1[i];
      v1[i] = tmp;
    }
  }

  /**
   * Reverses a vector in place
   * @param v1 the vector to be reversed
   */
  public static void inPlace(float[] v1) {
    Validate.notNull(v1);
    float tmp;
    final int half = v1.length / 2;
    final int len = v1.length - 1;
    for (int i = 0; i < half; i++) {
      tmp = v1[len - i];
      v1[len - i] = v1[i];
      v1[i] = tmp;
    }
  }

  /**
   * Reverses a vector in place
   * @param v1 the vector to be reversed
   */
  public static void inPlace(double[] v1) {
    Validate.notNull(v1);
    double tmp;
    final int half = v1.length / 2;
    final int len = v1.length - 1;
    for (int i = 0; i < half; i++) {
      tmp = v1[len - i];
      v1[len - i] = v1[i];
      v1[i] = tmp;
    }
  }

  /**
   * Reverses a vector in place
   * @param v1 the vector to be reversed
   * @return r a reversed copy of v1
   */
  public static int[] stateless(int[] v1) {
    Validate.notNull(v1);
    int[] r = Arrays.copyOf(v1, v1.length);
    inPlace(r);
    return r;
  }

  /**
   * Reverses a vector in place
   * @param v1 the vector to be reversed
   * @return r a reversed copy of v1
   */
  public static long[] stateless(long[] v1) {
    Validate.notNull(v1);
    long[] r = Arrays.copyOf(v1, v1.length);
    inPlace(r);
    return r;
  }

  /**
   * Reverses a vector in place
   * @param v1 the vector to be reversed
   * @return r a reversed copy of v1
   */
  public static float[] stateless(float[] v1) {
    Validate.notNull(v1);
    float[] r = Arrays.copyOf(v1, v1.length);
    inPlace(r);
    return r;
  }

  /**
   * Reverses a vector in place
   * @param v1 the vector to be reversed
   * @return r a reversed copy of v1
   */
  public static double[] stateless(double[] v1) {
    Validate.notNull(v1);
    double[] r = Arrays.copyOf(v1, v1.length);
    inPlace(r);
    return r;
  }

}
