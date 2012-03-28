/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.functions.iss;

import org.apache.commons.lang.Validate;

/**
 * Tries to detect Infinities, where they are, and provides some potentially useful tools to do things like mask them off.
 */
public class IsInf {

  private static final double DOUBLEPINF = Double.POSITIVE_INFINITY;
  private static final double DOUBLENINF = Double.NEGATIVE_INFINITY;
  private static final double FLOATPINF = Float.POSITIVE_INFINITY;
  private static final double FLOATNINF = Float.NEGATIVE_INFINITY;

  
  /**
   * Walks through a vector looking for infinity regardless of sign, if one is found the routine returns TRUE. 
   * @param v the vector (with possible (+/-)Inf entries)
   * @return a boolean, TRUE if a (+/-)Inf is found in v, FALSE otherwise
   */
  public static boolean any(double[] v) {
    Validate.notNull(v);
    boolean logical = false;
    final int len = v.length;
    for (int i = 0; i < len; i++) {
      if (v[i] == DOUBLEPINF || v[i] == DOUBLENINF) {
        logical = true;
        return logical;
      }
    }
    return logical;
  }
  
  /**
   * Walks through a vector looking for positive infinity, if one is found the routine returns TRUE. 
   * @param v the vector (with possible +Inf entries)
   * @return a boolean, TRUE if a +Inf is found in v, FALSE otherwise
   */
  public static boolean anyPositive(double[] v) {
    Validate.notNull(v);
    boolean logical = false;
    final int len = v.length;
    for (int i = 0; i < len; i++) {
      if (v[i] == DOUBLEPINF) {
        logical = true;
        return logical;
      }
    }
    return logical;
  }
  
  /**
   * Walks through a vector looking for negative infinity, if one is found the routine returns TRUE. 
   * @param v the vector (with possible -Inf entries)
   * @return a boolean, TRUE if a -Inf is found in v, FALSE otherwise
   */
  public static boolean anyNegative(double[] v) {
    Validate.notNull(v);
    boolean logical = false;
    final int len = v.length;
    for (int i = 0; i < len; i++) {
      if (v[i] == DOUBLENINF) {
        logical = true;
        return logical;
      }
    }
    return logical;
  }

  /**
   * Walks through a vector looking for infinity regardless of sign, and sets the index of a corresponding boolean vector to TRUE if an infinity is found.
   * @param v the vector (with possible (+/-)Inf entries)
   * @return a boolean vector, true or false depending on whether a (+/-)Inf is found in the input vector (true = NaN found at position). 
   */
  public static boolean[] getBooleans(double[] v) {
    Validate.notNull(v);
    final int len = v.length;
    boolean[] logical = new boolean[len];
    for (int i = 0; i < len; i++) {
      if (v[i] == DOUBLEPINF || v[i] == DOUBLENINF) {
        logical[i] = true;
      }
    }
    return logical;
  }  

  /**
   * Walks through a vector looking for positive infinity and sets the index of a corresponding boolean vector to TRUE if an infinity is found.
   * @param v the vector (with possible +Inf entries)
   * @return a boolean vector, true or false depending on whether a +Inf is found in the input vector (true = NaN found at position). 
   */
  public static boolean[] getBooleansPositive(double[] v) {
    Validate.notNull(v);
    final int len = v.length;
    boolean[] logical = new boolean[len];
    for (int i = 0; i < len; i++) {
      if (v[i] == DOUBLEPINF) {
        logical[i] = true;
      }
    }
    return logical;
  }    

  /**
   * Walks through a vector looking for negative infinity and sets the index of a corresponding boolean vector to TRUE if an infinity is found.
   * @param v the vector (with possible -Inf entries)
   * @return a boolean vector, true or false depending on whether a -Inf is found in the input vector (true = NaN found at position). 
   */
  public static boolean[] getBooleansNegative(double[] v) {
    Validate.notNull(v);
    final int len = v.length;
    boolean[] logical = new boolean[len];
    for (int i = 0; i < len; i++) {
      if (v[i] == DOUBLENINF) {
        logical[i] = true;
      }
    }
    return logical;
  }    
  
  
  /**
   * Walks through a vector looking for infinity regardless of sign, if one is found the routine returns TRUE. 
   * @param v the vector (with possible (+/-)Inf entries)
   * @return a boolean, TRUE if a (+/-)Inf is found in v, FALSE otherwise
   */
  public static boolean any(float[] v) {
    Validate.notNull(v);
    boolean logical = false;
    final int len = v.length;
    for (int i = 0; i < len; i++) {
      if (v[i] == FLOATPINF || v[i] == FLOATNINF) {
        logical = true;
        return logical;
      }
    }
    return logical;
  }
  
  /**
   * Walks through a vector looking for positive infinity, if one is found the routine returns TRUE. 
   * @param v the vector (with possible +Inf entries)
   * @return a boolean, TRUE if a +Inf is found in v, FALSE otherwise
   */
  public static boolean anyPositive(float[] v) {
    Validate.notNull(v);
    boolean logical = false;
    final int len = v.length;
    for (int i = 0; i < len; i++) {
      if (v[i] == FLOATPINF) {
        logical = true;
        return logical;
      }
    }
    return logical;
  }
  
  /**
   * Walks through a vector looking for negative infinity, if one is found the routine returns TRUE. 
   * @param v the vector (with possible -Inf entries)
   * @return a boolean, TRUE if a -Inf is found in v, FALSE otherwise
   */
  public static boolean anyNegative(float[] v) {
    Validate.notNull(v);
    boolean logical = false;
    final int len = v.length;
    for (int i = 0; i < len; i++) {
      if (v[i] == FLOATNINF) {
        logical = true;
        return logical;
      }
    }
    return logical;
  }

  /**
   * Walks through a vector looking for infinity regardless of sign, and sets the index of a corresponding boolean vector to TRUE if an infinity is found.
   * @param v the vector (with possible (+/-)Inf entries)
   * @return a boolean vector, true or false depending on whether a (+/-)Inf is found in the input vector (true = NaN found at position). 
   */
  public static boolean[] getBooleans(float[] v) {
    Validate.notNull(v);
    final int len = v.length;
    boolean[] logical = new boolean[len];
    for (int i = 0; i < len; i++) {
      if (v[i] == FLOATPINF || v[i] == FLOATNINF) {
        logical[i] = true;
      }
    }
    return logical;
  }  

  /**
   * Walks through a vector looking for positive infinity and sets the index of a corresponding boolean vector to TRUE if an infinity is found.
   * @param v the vector (with possible +Inf entries)
   * @return a boolean vector, true or false depending on whether a +Inf is found in the input vector (true = NaN found at position). 
   */
  public static boolean[] getBooleansPositive(float[] v) {
    Validate.notNull(v);
    final int len = v.length;
    boolean[] logical = new boolean[len];
    for (int i = 0; i < len; i++) {
      if (v[i] == FLOATPINF) {
        logical[i] = true;
      }
    }
    return logical;
  }    

  /**
   * Walks through a vector looking for negative infinity and sets the index of a corresponding boolean vector to TRUE if an infinity is found.
   * @param v the vector (with possible -Inf entries)
   * @return a boolean vector, true or false depending on whether a -Inf is found in the input vector (true = NaN found at position). 
   */
  public static boolean[] getBooleansNegative(float[] v) {
    Validate.notNull(v);
    final int len = v.length;
    boolean[] logical = new boolean[len];
    for (int i = 0; i < len; i++) {
      if (v[i] == FLOATNINF) {
        logical[i] = true;
      }
    }
    return logical;
  }    
  
  
}
