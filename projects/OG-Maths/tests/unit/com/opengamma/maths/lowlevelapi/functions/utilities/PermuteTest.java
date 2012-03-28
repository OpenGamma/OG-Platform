/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.functions.utilities;

import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.maths.lowlevelapi.functions.utilities.Permute;

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

  /* long data */
  long[] _nulldataL = null;
  long[] _singledataL = {1 };
  long[] _smalldataL = {1, 2, 3, 4, 5 };
  long[] _meddataL = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
  /* long answers */
  long[] _singlePermedL = {1 };
  long[] _smallPermedL = {1, 3, 5, 2, 4 };
  long[] _medPermedL = {2, 4, 6, 8, 10, 1, 3, 5, 7, 9 };

  /* float data */
  float[] _nulldataF = null;
  float[] _singledataF = {1 };
  float[] _smalldataF = {1, 2, 3, 4, 5 };
  float[] _meddataF = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
  /* float answers */
  float[] _singlePermedF = {1 };
  float[] _smallPermedF = {1, 3, 5, 2, 4 };
  float[] _medPermedF = {2, 4, 6, 8, 10, 1, 3, 5, 7, 9 };

  /* double data */
  double[] _nulldataD = null;
  double[] _singledataD = {1 };
  double[] _smalldataD = {1, 2, 3, 4, 5 };
  double[] _meddataD = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
  /* double answers */
  double[] _singlePermedD = {1 };
  double[] _smallPermedD = {1, 3, 5, 2, 4 };
  double[] _medPermedD = {2, 4, 6, 8, 10, 1, 3, 5, 7, 9 };

  /* permutations */
  int[] _nullPerm = null;
  int[] _singlePerm = {0 };
  int[] _smallPerm = {0, 2, 4, 1, 3 };
  int[] _medPerm = {1, 3, 5, 7, 9, 0, 2, 4, 6, 8 };
  int[] _badPermNeg = {-1, 0, 2, 3, 1 };
  int[] _badPermPos = {0, 1, 2, 3, 19 };
  int[] _badPermUniq = {0, 1, 2, 2, 3 };

  /* try and break the on Int function */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDataI() {
    Permute.inPlace(_nulldataI, _singlePerm);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPermutationI() {
    Permute.inPlace(_singledataI, _nullPerm);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testShortPermutationI() {
    Permute.inPlace(_smalldataI, _singlePerm);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testShortVectorI() {
    Permute.inPlace(_singledataI, _smallPerm);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testbadPermNegI() {
    Permute.inPlace(_smalldataI, _badPermNeg);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testbadPermPosI() {
    Permute.inPlace(_smalldataI, _badPermPos);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testbadPermUniqI() {
    Permute.inPlace(_smalldataI, _badPermUniq);
  }

  /* test permutations on ints */
  @Test
  public void testSinglePermInplaceI() {
    int[] tmp = Arrays.copyOf(_singledataI, _singledataI.length);
    Permute.inPlace(tmp, _singlePerm);
    assertTrue(Arrays.equals(_singlePermedI, tmp));
  }

  @Test
  public void testSmallPermInplaceI() {
    int[] tmp = Arrays.copyOf(_smalldataI, _smalldataI.length);
    Permute.inPlace(tmp, _smallPerm);
    assertTrue(Arrays.equals(_smallPermedI, tmp));
  }

  @Test
  public void testMedPermInplaceI() {
    int[] tmp = Arrays.copyOf(_meddataI, _meddataI.length);
    Permute.inPlace(tmp, _medPerm);
    assertTrue(Arrays.equals(_medPermedI, tmp));
  }


  /* try and break the on Long function */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDataL() {
    Permute.inPlace(_nulldataL, _singlePerm);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPermutationL() {
    Permute.inPlace(_singledataL, _nullPerm);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testShortPermutationL() {
    Permute.inPlace(_smalldataL, _singlePerm);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testShortVectorL() {
    Permute.inPlace(_singledataL, _smallPerm);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testbadPermNegL() {
    Permute.inPlace(_smalldataL, _badPermNeg);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testbadPermPosL() {
    Permute.inPlace(_smalldataL, _badPermPos);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testbadPermUniqL() {
    Permute.inPlace(_smalldataL, _badPermUniq);
  }
  /* test permutations on longs */
  @Test
  public void testSinglePermInplaceL() {
    long[] tmp = Arrays.copyOf(_singledataL, _singledataL.length);
    Permute.inPlace(tmp, _singlePerm);
    assertTrue(Arrays.equals(_singlePermedL, tmp));
  }

  @Test
  public void testSmallPermInplaceL() {
    long[] tmp = Arrays.copyOf(_smalldataL, _smalldataL.length);
    Permute.inPlace(tmp, _smallPerm);
    assertTrue(Arrays.equals(_smallPermedL, tmp));
  }

  @Test
  public void testMedPermInplaceL() {
    long[] tmp = Arrays.copyOf(_meddataL, _meddataL.length);
    Permute.inPlace(tmp, _medPerm);
    assertTrue(Arrays.equals(_medPermedL, tmp));
  }


  /* try and break the on float function */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDataF() {
    Permute.inPlace(_nulldataF, _singlePerm);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPermutationF() {
    Permute.inPlace(_singledataF, _nullPerm);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testShortPermutationF() {
    Permute.inPlace(_smalldataF, _singlePerm);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testShortVectorF() {
    Permute.inPlace(_singledataF, _smallPerm);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testbadPermNegF() {
    Permute.inPlace(_smalldataF, _badPermNeg);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testbadPermPosF() {
    Permute.inPlace(_smalldataF, _badPermPos);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testbadPermUniqF() {
    Permute.inPlace(_smalldataF, _badPermUniq);
  }
  /* test permutations on floats */
  @Test
  public void testSinglePermInplaceF() {
    float[] tmp = Arrays.copyOf(_singledataF, _singledataF.length);
    Permute.inPlace(tmp, _singlePerm);
    assertTrue(Arrays.equals(_singlePermedF, tmp));
  }

  @Test
  public void testSmallPermInplaceF() {
    float[] tmp = Arrays.copyOf(_smalldataF, _smalldataF.length);
    Permute.inPlace(tmp, _smallPerm);
    assertTrue(Arrays.equals(_smallPermedF, tmp));
  }

  @Test
  public void testMedPermInplaceF() {
    float[] tmp = Arrays.copyOf(_meddataF, _meddataF.length);
    Permute.inPlace(tmp, _medPerm);
    assertTrue(Arrays.equals(_medPermedF, tmp));
  }

  /* try and break the on doubles function */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDataD() {
    Permute.inPlace(_nulldataD, _singlePerm);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPermutationD() {
    Permute.inPlace(_singledataD, _nullPerm);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testShortPermutationD() {
    Permute.inPlace(_smalldataD, _singlePerm);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testShortVectorD() {
    Permute.inPlace(_singledataD, _smallPerm);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testbadPermNegD() {
    Permute.inPlace(_smalldataD, _badPermNeg);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testbadPermPosD() {
    Permute.inPlace(_smalldataD, _badPermPos);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testbadPermUniqD() {
    Permute.inPlace(_smalldataD, _badPermUniq);
  }
  /* test permutations on doubles */
  @Test
  public void testSinglePermInplaceD() {
    double[] tmp = Arrays.copyOf(_singledataD, _singledataD.length);
    Permute.inPlace(tmp, _singlePerm);
    assertTrue(Arrays.equals(_singlePermedD, tmp));
  }

  @Test
  public void testSmallPermInplaceD() {
    double[] tmp = Arrays.copyOf(_smalldataD, _smalldataD.length);
    Permute.inPlace(tmp, _smallPerm);
    assertTrue(Arrays.equals(_smallPermedD, tmp));
  }

  @Test
  public void testMedPermInplaceD() {
    double[] tmp = Arrays.copyOf(_meddataD, _meddataD.length);
    Permute.inPlace(tmp, _medPerm);
    assertTrue(Arrays.equals(_medPermedD, tmp));
  }

}
