/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.utilities;

import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;

import org.testng.annotations.Test;

/**
 * Tests permutations code
 */
public class PermuteTest {

  /* int data */
  int[] _nulldataI = null;
  int[] _singledataI = {1 };
  int[] _smalldataI = {1, 2, 3, 4, 5 };
  int[] _meddataI = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

  /* int answers */
  int[] _singlePermedI = {1 };
  int[] _smallPermedI = {1, 3, 5, 2, 4 };
  int[] _medPermedI = {2, 4, 6, 8, 10, 1, 3, 5, 7, 9 };

  /* permutations */
  int[] _nullPerm = null;
  int[] _singlePerm = {0 };
  int[] _smallPerm = {0, 2, 4, 1, 3 };
  int[] _medPerm = {1, 3, 5, 7, 9, 0, 2, 4, 6, 8 };
  int[] _badPermNeg = {-1, 0, 2, 3, 1 };
  int[] _badPermPos = {0, 1, 2, 3, 19 };
  int[] _badPermUniq = {0, 1, 2, 2, 3 };

  /** try and break the function */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDataI() {
    Permute.inplace(_nulldataI, _singlePerm);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPermutationI() {
    Permute.inplace(_singledataI, _nullPerm);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testShortPermutationI() {
    Permute.inplace(_smalldataI, _singlePerm);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testShortVectorI() {
    Permute.inplace(_singledataI, _smallPerm);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testbadPermNegI() {
    Permute.inplace(_smalldataI, _badPermNeg);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testbadPermPosI() {
    Permute.inplace(_smalldataI, _badPermPos);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testbadPermUniqI() {
    Permute.inplace(_smalldataI, _badPermUniq);
  }

  /* test permutations on ints */
  @Test
  public void testSinglePermInplaceI() {
    int[] tmp = Arrays.copyOf(_singledataI, _singledataI.length);
    Permute.inplace(tmp, _singlePerm);
    assertTrue(Arrays.equals(_singlePermedI, tmp));
  }

  @Test
  public void testSmallPermInplaceI() {
    int[] tmp = Arrays.copyOf(_smalldataI, _smalldataI.length);
    Permute.inplace(tmp, _smallPerm);
    assertTrue(Arrays.equals(_smallPermedI, tmp));
  }

  @Test
  public void testMedPermInplaceI() {
    int[] tmp = Arrays.copyOf(_meddataI, _meddataI.length);
    Permute.inplace(tmp, _medPerm);
    assertTrue(Arrays.equals(_medPermedI, tmp));
  }
}
