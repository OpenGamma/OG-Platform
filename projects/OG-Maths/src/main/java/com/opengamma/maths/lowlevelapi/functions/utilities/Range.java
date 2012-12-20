/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.functions.utilities;


/**
 * Does something a bit like the Range function in Python, the colon operator in Octave or
 * the START,END,STEP notation of Fortran.
 */
public class Range {

  /**
   * Returns a vector containing the number from "start" to "end" in steps of 1.
   * @param start the first value in the range.
   * @param end the last value in the range
   * @return tmp the vector containing the values from start to end in steps of 1.
   */
  public static int[] fromToInts(final int start, final int end) {
    if (end < start) {
      throw new IllegalArgumentException("Impossible range: Range is negative [" + (end - start) + "].");
    }
    return fromToInStepInts(start, end, 1);
  }

  /**
   * Returns a vector containing the number from "start" to "end" in steps of "step".
   * @param start the first value in the range.
   * @param end the last value in the range
   * @param step the step to be used through the range.
   * @return tmp the vector containing the values from start to end in steps of 1.
   */
  public static int[] fromToInStepInts(final int start, final int end, final int step) {
    if (step == 0) {
      throw new IllegalArgumentException("Impossible step: Step is 0, increment is therefore invalid.");
    }
    if (end < start && step > 0) {
      throw new IllegalArgumentException("Impossible range: Range is negative, step is positive");
    }
    if (end > start && step < 0) {
      throw new IllegalArgumentException("Impossible range: Range is positive, step is negative");
    }
    int r = ((end - start) / step) + 1;
    int[] tmp = new int[r];
    int v = start;
    for (int i = 0; i < r; i++) {
      tmp[i] = v;
      v += step;
    }
    return tmp;
  }

  /**
   * Returns a vector containing the number from "start" to "end" in steps of 1.
   * @param start the first value in the range.
   * @param end the last value in the range
   * @return tmp the vector containing the values from start to end in steps of 1.
   */
  public static double[] fromToDoubles(final int start, final int end) {
    if (end < start) {
      throw new IllegalArgumentException("Impossible range: Range is negative [" + (end - start) + "].");
    }
    return fromToInStepDoubles(start, end, 1);
  }

  /**
   * Returns a vector containing the number from "start" to "end" in steps of "step".
   * @param start the first value in the range.
   * @param end the last value in the range
   * @param step the step to be used through the range.
   * @return tmp the vector containing the values from start to end in steps of 1.
   */
  public static double[] fromToInStepDoubles(final int start, final int end, final int step) {
    if (step == 0) {
      throw new IllegalArgumentException("Impossible step: Step is 0, increment is therefore invalid.");
    }
    if (end < start && step > 0) {
      throw new IllegalArgumentException("Impossible range: Range is negative, step is positive");
    }
    if (end > start && step < 0) {
      throw new IllegalArgumentException("Impossible range: Range is positive, step is negative");
    }
    int r = ((end - start) / step) + 1;
    double[] tmp = new double[r];
    int v = start;
    for (int i = 0; i < r; i++) {
      tmp[i] = v;
      v += step;
    }
    return tmp;
  }

}
