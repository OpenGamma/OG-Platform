/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.functions.utilities;

import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.maths.lowlevelapi.functions.utilities.Reverse;

/**
 * Tests the reverse class
 */
public class ReverseTest {

  int[] v1I = {1, 2, 3, 4, 5, 6, 7, 8 };
  int[] v2I = {1, 2, 3, 4, 5, 6, 7, 8, 9 };
  int[] v3I = {-1, 2, -3, 4, -5, 6, -7, 8 };

  int[] v1rI = {8, 7, 6, 5, 4, 3, 2, 1 };
  int[] v2rI = {9, 8, 7, 6, 5, 4, 3, 2, 1 };
  int[] v3rI = {8, -7, 6, -5, 4, -3, 2, -1 };

  long[] v1L = {1, 2, 3, 4, 5, 6, 7, 8 };
  long[] v2L = {1, 2, 3, 4, 5, 6, 7, 8, 9 };
  long[] v3L = {-1, 2, -3, 4, -5, 6, -7, 8 };

  long[] v1rL = {8, 7, 6, 5, 4, 3, 2, 1 };
  long[] v2rL = {9, 8, 7, 6, 5, 4, 3, 2, 1 };
  long[] v3rL = {8, -7, 6, -5, 4, -3, 2, -1 };

  float[] v1F = {1, 2, 3, 4, 5, 6, 7, 8 };
  float[] v2F = {1, 2, 3, 4, 5, 6, 7, 8, 9 };
  float[] v3F = {-1, 2, -3, 4, -5, 6, -7, 8 };

  float[] v1rF = {8, 7, 6, 5, 4, 3, 2, 1 };
  float[] v2rF = {9, 8, 7, 6, 5, 4, 3, 2, 1 };
  float[] v3rF = {8, -7, 6, -5, 4, -3, 2, -1 };

  double[] v1D = {1, 2, 3, 4, 5, 6, 7, 8 };
  double[] v2D = {1, 2, 3, 4, 5, 6, 7, 8, 9 };
  double[] v3D = {-1, 2, -3, 4, -5, 6, -7, 8 };

  double[] v1rD = {8, 7, 6, 5, 4, 3, 2, 1 };
  double[] v2rD = {9, 8, 7, 6, 5, 4, 3, 2, 1 };
  double[] v3rD = {8, -7, 6, -5, 4, -3, 2, -1 };

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadInputIPI() {
    double[] badI = null;
    Reverse.inPlace(badI);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadInputSI() {
    double[] badI = null;
    Reverse.stateless(badI);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadInputIPL() {
    double[] badL = null;
    Reverse.inPlace(badL);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadInputSL() {
    double[] badL = null;
    Reverse.stateless(badL);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadInputIPF() {
    double[] badF = null;
    Reverse.inPlace(badF);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadInputSF() {
    double[] badF = null;
    Reverse.stateless(badF);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadInputIPD() {
    double[] badD = null;
    Reverse.inPlace(badD);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadInputsD() {
    double[] badD = null;
    Reverse.stateless(badD);
  }

  @Test
  public void testReverseInts() {
    int[] tmp;
    tmp = Arrays.copyOf(v1I, v1I.length);
    Reverse.inPlace(tmp);
    assertTrue(Arrays.equals(v1rI, tmp));
    tmp = Arrays.copyOf(v2I, v2I.length);
    Reverse.inPlace(tmp);
    assertTrue(Arrays.equals(v2rI, tmp));
    tmp = Arrays.copyOf(v3I, v3I.length);
    Reverse.inPlace(tmp);
    assertTrue(Arrays.equals(v3rI, tmp));

    assertTrue(Arrays.equals(v1rI, Reverse.stateless(v1I)));
    assertTrue(Arrays.equals(v2rI, Reverse.stateless(v2I)));
    assertTrue(Arrays.equals(v3rI, Reverse.stateless(v3I)));
  }

  @Test
  public void testReverseLongs() {
    long[] tmp;
    tmp = Arrays.copyOf(v1L, v1L.length);
    Reverse.inPlace(tmp);
    assertTrue(Arrays.equals(v1rL, tmp));
    tmp = Arrays.copyOf(v2L, v2L.length);
    Reverse.inPlace(tmp);
    assertTrue(Arrays.equals(v2rL, tmp));
    tmp = Arrays.copyOf(v3L, v3L.length);
    Reverse.inPlace(tmp);
    assertTrue(Arrays.equals(v3rL, tmp));

    assertTrue(Arrays.equals(v1rL, Reverse.stateless(v1L)));
    assertTrue(Arrays.equals(v2rL, Reverse.stateless(v2L)));
    assertTrue(Arrays.equals(v3rL, Reverse.stateless(v3L)));
  }

  @Test
  public void testReverseFloats() {
    float[] tmp;
    tmp = Arrays.copyOf(v1F, v1F.length);
    Reverse.inPlace(tmp);
    assertTrue(Arrays.equals(v1rF, tmp));
    tmp = Arrays.copyOf(v2F, v2F.length);
    Reverse.inPlace(tmp);
    assertTrue(Arrays.equals(v2rF, tmp));
    tmp = Arrays.copyOf(v3F, v3F.length);
    Reverse.inPlace(tmp);
    assertTrue(Arrays.equals(v3rF, tmp));

    assertTrue(Arrays.equals(v1rF, Reverse.stateless(v1F)));
    assertTrue(Arrays.equals(v2rF, Reverse.stateless(v2F)));
    assertTrue(Arrays.equals(v3rF, Reverse.stateless(v3F)));
  }

  @Test
  public void testReverseDoubles() {
    double[] tmp;
    tmp = Arrays.copyOf(v1D, v1D.length);
    Reverse.inPlace(tmp);
    assertTrue(Arrays.equals(v1rD, tmp));
    tmp = Arrays.copyOf(v2D, v2D.length);
    Reverse.inPlace(tmp);
    assertTrue(Arrays.equals(v2rD, tmp));
    tmp = Arrays.copyOf(v3D, v3D.length);
    Reverse.inPlace(tmp);
    assertTrue(Arrays.equals(v3rD, tmp));

    assertTrue(Arrays.equals(v1rD, Reverse.stateless(v1D)));
    assertTrue(Arrays.equals(v2rD, Reverse.stateless(v2D)));
    assertTrue(Arrays.equals(v3rD, Reverse.stateless(v3D)));
  }

} // end class

