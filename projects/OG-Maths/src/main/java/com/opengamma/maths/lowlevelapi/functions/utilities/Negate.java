/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.functions.utilities;

import org.apache.commons.lang.Validate;

/**
 * Negates a vector
 * TODO: Have a discussion over whether we should also support 0-1 vectors as booleans.
 */

public class Negate {
  /**
   * Negates the boolean values in a vector in a stateless manner.
   * @param v a vector of booleans
   * @return tmp the negated representation of v
   */
  public static boolean[] stateless(boolean[] v) {
    Validate.notNull(v);
    final int len = v.length;
    boolean[] tmp = new boolean[len];
    for (int i = 0; i < len; i++) {
      if (v[i] == true) {
        tmp[i] = false;
      } else {
        tmp[i] = true;
      }
    }
    return tmp;
  }

  /**
   * Negates the boolean values in a vector, this is done in place, memory will be changed!
   * @param v a vector of booleans
   */
  public static void inPlace(boolean[] v) {
    Validate.notNull(v);
    final int len = v.length;
    for (int i = 0; i < len; i++) {
      if (v[i] == true) {
        v[i] = false;
      } else {
        v[i] = true;
      }
    }
  }
}
