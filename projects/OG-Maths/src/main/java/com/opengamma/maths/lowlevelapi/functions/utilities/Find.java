/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.functions.utilities;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.MathException;

/**
 * Finds the indexes of values that satisfy a given condition.
 */
public class Find {

  /**
   * Enum for the condition to satisfy, rather Fortran.
   */
  public enum condition {
    /** Equal to */
    eq,
    /** Not equal to */
    ne,
    /** Less than */
    lt,
    /** Less than or equal to */
    le,
    /** Greater than */
    gt,
    /** Greater than or equal to */
    ge
  }


  /**
   * Enum for the condition to satisfy in the case of booleans.
   */
  public enum booleanCondition {
    /** Equal to */
    eq,
    /** Not equal to */
    ne
  }
  
  
  /**
   * Find values that match a given condition.
   * For example: Find(v,eq,3) will return the indices in vector v which are numerically equal to 3.
   * @param v the values to try against the condition
   * @param op the condition
   * @param value the value to which the vector of values is compared
   * @return the indices of the values which satisfy the condition
   */
  public static int[] indexes(int[] v, condition op, int value) {
    Validate.notNull(v);

    final int n = v.length;
    int[] tmp = new int[n];

    int count = 0;
    switch (op) {
      case eq:
        for (int i = 0; i < n; i++) {
          if (v[i] == value) {
            tmp[count] = i;
            count++;
          }
        }
        break;
      case ne:
        for (int i = 0; i < n; i++) {
          if (v[i] != value) {
            tmp[count] = i;
            count++;
          }
        }
        break;
      case lt:
        for (int i = 0; i < n; i++) {
          if (v[i] < value) {
            tmp[count] = i;
            count++;
          }
        }
        break;
      case le:
        for (int i = 0; i < n; i++) {
          if (v[i] <= value) {
            tmp[count] = i;
            count++;
          }
        }
        break;
      case gt:
        for (int i = 0; i < n; i++) {
          if (v[i] > value) {
            tmp[count] = i;
            count++;
          }
        }
        break;
      case ge:
        for (int i = 0; i < n; i++) {
          if (v[i] >= value) {
            tmp[count] = i;
            count++;
          }
        }
        break;
      default:
        throw new MathException("Impossible condition in Find.");
    }
    int[] ret = new int[count];
    System.arraycopy(tmp, 0, ret, 0, count);
    return ret;
  }

  /**
   * Find values that match a given condition.
   * For example: Find(v,eq,3) will return the indices in vector v which are numerically equal to 3.
   * @param v the values to try against the condition
   * @param op the condition
   * @param value the value to which the vector of values is compared
   * @return the indices of the values which satisfy the condition
   */
  public static int[] indexes(long[] v, condition op, long value) {
    Validate.notNull(v);

    final int n = v.length;
    int[] tmp = new int[n];

    int count = 0;
    switch (op) {
      case eq:
        for (int i = 0; i < n; i++) {
          if (v[i] == value) {
            tmp[count] = i;
            count++;
          }
        }
        break;
      case ne:
        for (int i = 0; i < n; i++) {
          if (v[i] != value) {
            tmp[count] = i;
            count++;
          }
        }
        break;
      case lt:
        for (int i = 0; i < n; i++) {
          if (v[i] < value) {
            tmp[count] = i;
            count++;
          }
        }
        break;
      case le:
        for (int i = 0; i < n; i++) {
          if (v[i] <= value) {
            tmp[count] = i;
            count++;
          }
        }
        break;
      case gt:
        for (int i = 0; i < n; i++) {
          if (v[i] > value) {
            tmp[count] = i;
            count++;
          }
        }
        break;
      case ge:
        for (int i = 0; i < n; i++) {
          if (v[i] >= value) {
            tmp[count] = i;
            count++;
          }
        }
        break;
      default:
        throw new MathException("Impossible condition in Find.");
    }
    int[] ret = new int[count];
    System.arraycopy(tmp, 0, ret, 0, count);
    return ret;
  }

  /*
   * Find values that match a given condition.
   * For example: Find(v,eq,3) will return the indices in vector v which are numerically equal to 3.
   * @param v the values to try against the condition
   * @param op the condition
   * @param value the value to which the vector of values is compared
   * @param tol a tolerance within which comparison is valid, the tolerance hedges towards inclusion
   * @return the indices of the values which satisfy the condition
   * TODO: When we come up with a sane way of deciding tolerances, we'll use this
  public static int[] indexes(float[] v, condition op, float value, float tol) {
    Validate.notNull(v);

    final int n = v.length;
    int[] tmp = new int[n];

    int count = 0;
    switch (op) {
      case eq:
        for (int i = 0; i < n; i++) {
          if (Math.abs(v[i] - value) < tol) {
            tmp[count] = i;
            count++;
          }
        }
        break;
      case ne:
        for (int i = 0; i < n; i++) {
          if (Math.abs(v[i] - value) > tol) {
            tmp[count] = i;
            count++;
          }
        }
        break;
      case lt:
        for (int i = 0; i < n; i++) {
          if (v[i] < value + tol) {
            tmp[count] = i;
            count++;
          }
        }
        break;
      case le:
        for (int i = 0; i < n; i++) {
          if (v[i] <= value + tol) {
            tmp[count] = i;
            count++;
          }
        }
        break;
      case gt:
        for (int i = 0; i < n; i++) {
          if (v[i] > value - tol) {
            tmp[count] = i;
            count++;
          }
        }
        break;
      case ge:
        for (int i = 0; i < n; i++) {
          if (v[i] >= value - tol) {
            tmp[count] = i;
            count++;
          }
        }
        break;
      default:
        throw new MathException("Impossible condition in Find.");
    }
    int[] ret = new int[count];
    System.arraycopy(tmp, 0, ret, 0, count);
    return ret;
  }

  */

  /**
   * Find values that match a given condition.
   * For example: Find(v,eq,3) will return the indices in vector v which are numerically equal to 3.
   * Warning: this method call has zero tolerance for numerical error in fp representations!
   * @param v the values to try against the condition
   * @param op the condition
   * @param value the value to which the vector of values is compared
   * @return the indices of the values which satisfy the condition
   */
  public static int[] indexes(float[] v, condition op, float value) {
    Validate.notNull(v);

    final int n = v.length;
    int[] tmp = new int[n];
    int count = 0;
    switch (op) {
      case eq:
        for (int i = 0; i < n; i++) {
          if (v[i] == value) {
            tmp[count] = i;
            count++;
          }
        }
        break;
      case ne:
        for (int i = 0; i < n; i++) {
          if (v[i] != value) {
            tmp[count] = i;
            count++;
          }
        }
        break;
      case lt:
        for (int i = 0; i < n; i++) {
          if (v[i] < value) {
            tmp[count] = i;
            count++;
          }
        }
        break;
      case le:
        for (int i = 0; i < n; i++) {
          if (v[i] <= value) {
            tmp[count] = i;
            count++;
          }
        }
        break;
      case gt:
        for (int i = 0; i < n; i++) {
          if (v[i] > value) {
            tmp[count] = i;
            count++;
          }
        }
        break;
      case ge:
        for (int i = 0; i < n; i++) {
          if (v[i] >= value) {
            tmp[count] = i;
            count++;
          }
        }
        break;
      default:
        throw new MathException("Impossible condition in Find.");
    }
    int[] ret = new int[count];
    System.arraycopy(tmp, 0, ret, 0, count);
    return ret;
  }

  /**
   * Find values that match a given condition.
   * For example: Find(v,eq,3) will return the indices in vector v which are numerically equal to 3.
   * Warning: this method call has zero tolerance for numerical error in fp representations!
   * @param v the values to try against the condition
   * @param op the condition
   * @param value the value to which the vector of values is compared
   * @return the indices of the values which satisfy the condition
   */
  public static int[] indexes(double[] v, condition op, double value) {
    Validate.notNull(v);

    final int n = v.length;
    int[] tmp = new int[n];
    int count = 0;
    switch (op) {
      case eq:
        for (int i = 0; i < n; i++) {
          if (v[i] == value) {
            tmp[count] = i;
            count++;
          }
        }
        break;
      case ne:
        for (int i = 0; i < n; i++) {
          if (v[i] != value) {
            tmp[count] = i;
            count++;
          }
        }
        break;
      case lt:
        for (int i = 0; i < n; i++) {
          if (v[i] < value) {
            tmp[count] = i;
            count++;
          }
        }
        break;
      case le:
        for (int i = 0; i < n; i++) {
          if (v[i] <= value) {
            tmp[count] = i;
            count++;
          }
        }
        break;
      case gt:
        for (int i = 0; i < n; i++) {
          if (v[i] > value) {
            tmp[count] = i;
            count++;
          }
        }
        break;
      case ge:
        for (int i = 0; i < n; i++) {
          if (v[i] >= value) {
            tmp[count] = i;
            count++;
          }
        }
        break;
      default:
        throw new MathException("Impossible condition in Find.");
    }
    int[] ret = new int[count];
    System.arraycopy(tmp, 0, ret, 0, count);
    return ret;
  }

  /**
   * Find values that match a given condition.
   * For example: Find(v,eq,3) will return the indices in vector v which are numerically equal to 3.
   * @param v the values to try against the condition
   * @param op the booleanCondition
   * @param value the value to which the vector of values is compared
   * @return the indices of the values which satisfy the condition
   */
  public static int[] indexes(boolean[] v, booleanCondition op, boolean value) {
    Validate.notNull(v);

    final int n = v.length;
    int[] tmp = new int[n];

    int count = 0;
    switch (op) {
      case eq:
        for (int i = 0; i < n; i++) {
          if (v[i] == value) {
            tmp[count] = i;
            count++;
          }
        }
        break;
      case ne:
        for (int i = 0; i < n; i++) {
          if (v[i] != value) {
            tmp[count] = i;
            count++;
          }
        }
        break;
      default:
        throw new MathException("Impossible condition in Find.");
    }
    int[] ret = new int[count];
    System.arraycopy(tmp, 0, ret, 0, count);
    return ret;
  }
} // class end
