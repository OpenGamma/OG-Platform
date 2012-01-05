/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.functions.iss;

import org.apache.commons.lang.Validate;

/**
 * Tries to detect NaNs, where they are, and provides some potentially useful tools to do things like mask off NaNs.
 */
public class IsNaN {

  /**
   * Walks through a vector looking for NaN's if one is found the routine returns TRUE. 
   * @param v the vector (with possible NaN entries)
   * @return a boolean, TRUE if a NaN is found in v, FALSE otherwise
   */
  public static boolean any(double[] v) {
    Validate.notNull(v);
    boolean logical = false;
    final int len = v.length;
    for (int i = 0; i < len; i++) {
      if (v[i] != v[i]) {
        logical = true;
        return logical;
      }
    }
    return logical;
  }

  /**
   * Walks through a vector looking for NaN's and sets the index of a corresponding boolean vector to TRUE if a NaN is found.
   * @param v the vector (with possible NaN entries)
   * @return a boolean vector, true or false depending on whether a NaN is found in the input vector (true = NaN found at position). 
   */
  public static boolean[] getBooleans(double[] v) {
    Validate.notNull(v);
    final int len = v.length;
    boolean[] logical = new boolean[len];
    for (int i = 0; i < len; i++) {
      if (v[i] != v[i]) {
        logical[i] = true;
      }
    }
    return logical;
  }
  
  /**
   * Walks through a vector looking for NaN's if one is found the routine returns TRUE. 
   * @param v the vector (with possible NaN entries)
   * @return a boolean, TRUE if a NaN is found in v, FALSE otherwise
   */
  public static boolean any(float[] v) {
    Validate.notNull(v);
    boolean logical = false;
    final int len = v.length;
    for (int i = 0; i < len; i++) {
      if (v[i] != v[i]) {
        logical = true;
        return logical;
      }
    }
    return logical;
  }

  /**
   * Walks through a vector looking for NaN's and sets the index of a corresponding boolean vector to TRUE if a NaN is found.
   * @param v the vector (with possible NaN entries)
   * @return a boolean vector, true or false depending on whether a NaN is found in the input vector (true = NaN found at position). 
   */
  public static boolean[] getBooleans(float[] v) {
    Validate.notNull(v);
    final int len = v.length;
    boolean[] logical = new boolean[len];
    for (int i = 0; i < len; i++) {
      if (v[i] != v[i]) {
        logical[i] = true;
      }
    }
    return logical;
  }  

}
