/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.utilities;

import static org.testng.AssertJUnit.assertTrue;

import org.apache.commons.lang.Validate;

/**
 * Applies a permutation to a given vector
 */
public class Permute {

  /**
   * Applies permutation p inplace to vector v.
   * @param v the vector to permute
   * @param p the permutation
   */
  public static void inplace(int[] v, int[] p) {
    inputValidator(v, p);
    final int[] tmp = permuter(v, p);
    System.arraycopy(tmp, 0, v, 0, v.length);
    return;
  }
  
  /**
   * Applies permutation p statelessly to vector v
   * @param v the vector to permute
   * @param p the permutation
   * @return tmp, a permuted vector v[p].
   */
  public static int[] stateless(int[] v, int[] p) {
    inputValidator(v, p);
    return permuter(v, p);
  }

  /* checks the input */
  private static void inputValidator(int[] v, int[] p) {
    Validate.notNull(v);
    Validate.notNull(p);
    assertTrue("Permutation is of length: " + p.length + " whereas vector is of length " + v.length + ". Permutation is therefore impossible.",
        v.length == p.length); // shortcut costly parse tests if vectors are not the same length
    assertTrue("Permutation contains indices with impossible range (too large)", Max.value(p) < v.length); // make sure the permutation won't go out of range.
    assertTrue("Permutation doesn't contain index 0", Min.value(p) == 0); // make sure the permutation won't go out of range, also catches -ve indices
    assertTrue("Permutation is nonunique (some indices are repeated)", Unique.bitwise(p).length == p.length); // make sure the permutation is a valid permutation.
  }

  /* actually does the permutation, can proceed unchecked as validity is performed in the callers */
  private static int[] permuter(int[] v, int[] p) {
    final int n = v.length;
    int[] tmp = new int[n];
    for (int i = 0; i < n; i++) {
      tmp[i] = v[p[i]];
    }
    return tmp;
  }

}
