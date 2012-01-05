/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.functions.iss;

import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.maths.lowlevelapi.functions.iss.IsInf;

/**
 * Tests the IsInf class
 */
public class IsInfTest {

  double[] _nullDouble = null;
  double[] _stdDouble = {1, 2, 3, 4, 5 };
  float[] _nullFloat = null;
  float[] _stdFloat = {1, 2, 3, 4, 5 };
  boolean[] _infPositions = {true, false, true, false, false };
  int[] _turnToInf = {0, 2 };
  boolean[] _allFalse = {false, false, false, false, false };

  /* helpers, create NaNs at given indices */
  private double[] createPositiveInfAtDouble(double[] v, int... i) {
    double[] tmp = Arrays.copyOf(v, v.length);
    double zero = 0d;
    for (int index = 0; index < i.length; index++)
    {
      tmp[i[index]] = 1 / zero;
    }
    return tmp;
  }

  private double[] createNegativeInfAtDouble(double[] v, int... i) {
    double[] tmp = Arrays.copyOf(v, v.length);
    double zero = 0d;
    for (int index = 0; index < i.length; index++)
    {
      tmp[i[index]] = -1 / zero;
    }
    return tmp;
  }

  private float[] createPositiveInfAtFloat(float[] v, int... i) {
    float[] tmp = Arrays.copyOf(v, v.length);
    float zero = 0f;
    for (int index = 0; index < i.length; index++)
    {
      tmp[i[index]] = 1 / zero;
    }
    return tmp;
  }

  private float[] createNegativeInfAtFloat(float[] v, int... i) {
    float[] tmp = Arrays.copyOf(v, v.length);
    float zero = 0f;
    for (int index = 0; index < i.length; index++)
    {
      tmp[i[index]] = -1 / zero;
    }
    return tmp;
  }

  /* doubles */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testIsInfAnyNullDouble() {
    IsInf.any(_nullDouble);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testIsInfAnyPositiveNullDouble() {
    IsInf.anyPositive(_nullDouble);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testIsInfAnyNegativeNullDouble() {
    IsInf.anyNegative(_nullDouble);
  }

  @Test
  public void testIsInfAnyNoInfsDouble() {
    boolean tmp = IsInf.any(_stdDouble);
    assertTrue(tmp == false);
  }

  @Test
  public void testIsInfAnyPositiveNoInfsDouble() {
    boolean tmp = IsInf.anyPositive(_stdDouble);
    assertTrue(tmp == false);
  }

  @Test
  public void testIsInfAnyNegativeNoInfsDouble() {
    boolean tmp = IsInf.anyNegative(_stdDouble);
    assertTrue(tmp == false);
  }

  @Test
  public void testIsInfAnyInfPositivePresentDouble() {
    double[] vP = createPositiveInfAtDouble(_stdDouble, 2);
    boolean tmpP = IsInf.any(vP);
    assertTrue(tmpP == true);
  }

  @Test
  public void testIsInfAnyInfNegativePresentDouble() {
    double[] vN = createNegativeInfAtDouble(_stdDouble, 2);
    boolean tmpN = IsInf.any(vN);
    assertTrue(tmpN == true);
  }

  @Test
  public void testIsInfAnyPositiveInfPositivePresentDouble() {
    double[] vP = createPositiveInfAtDouble(_stdDouble, 2);
    boolean tmpP = IsInf.anyPositive(vP);
    assertTrue(tmpP == true);
  }

  @Test
  public void testIsInfAnyNegativeInfNegativePresentDouble() {
    double[] vN = createNegativeInfAtDouble(_stdDouble, 2);
    boolean tmpN = IsInf.anyNegative(vN);
    assertTrue(tmpN == true);
  }

  @Test
  public void testIsInfgetBooleansNoInfsDouble() {
    boolean[] tmp = IsInf.getBooleans(_stdDouble);
    assertTrue(Arrays.equals(tmp, _allFalse));
  }

  @Test
  public void testIsInfgetBooleansNoPositiveInfsDouble() {
    double[] vN = createNegativeInfAtDouble(_stdDouble, 2);
    boolean[] tmp = IsInf.getBooleansPositive(vN);
    assertTrue(Arrays.equals(tmp, _allFalse));
  }

  @Test
  public void testIsInfgetBooleansNoNegativeInfsDouble() {
    double[] vP = createPositiveInfAtDouble(_stdDouble, 2);
    boolean[] tmp = IsInf.getBooleansNegative(vP);
    assertTrue(Arrays.equals(tmp, _allFalse));
  }

  @Test
  public void testIsInfgetBooleansInfPresentDouble() {
    double[] v = createPositiveInfAtDouble(_stdDouble, 2);
    v[0] = -1 / 0d;
    boolean[] tmp = IsInf.getBooleans(v);
    assertTrue(Arrays.equals(tmp, _infPositions));
  }

  @Test
  public void testIsInfgetBooleansPositiveInfPresentDouble() {
    double[] v = createPositiveInfAtDouble(_stdDouble, _turnToInf);
    boolean[] tmp = IsInf.getBooleansPositive(v);
    assertTrue(Arrays.equals(tmp, _infPositions));
  }

  @Test
  public void testIsInfgetBooleansNegativeInfPresentDouble() {
    double[] v = createNegativeInfAtDouble(_stdDouble, _turnToInf);
    boolean[] tmp = IsInf.getBooleansNegative(v);
    assertTrue(Arrays.equals(tmp, _infPositions));
  }

  /* floats */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testIsInfAnyNullFloat() {
    IsInf.any(_nullFloat);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testIsInfAnyPositiveNullFloat() {
    IsInf.anyPositive(_nullFloat);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testIsInfAnyNegativeNullFloat() {
    IsInf.anyNegative(_nullFloat);
  }

  @Test
  public void testIsInfAnyNoInfsFloat() {
    boolean tmp = IsInf.any(_stdFloat);
    assertTrue(tmp == false);
  }

  @Test
  public void testIsInfAnyPositiveNoInfsFloat() {
    boolean tmp = IsInf.anyPositive(_stdFloat);
    assertTrue(tmp == false);
  }

  @Test
  public void testIsInfAnyNegativeNoInfsFloat() {
    boolean tmp = IsInf.anyNegative(_stdFloat);
    assertTrue(tmp == false);
  }

  @Test
  public void testIsInfAnyInfPositivePresentFloat() {
    float[] vP = createPositiveInfAtFloat(_stdFloat, 2);
    boolean tmpP = IsInf.any(vP);
    assertTrue(tmpP == true);
  }

  @Test
  public void testIsInfAnyInfNegativePresentFloat() {
    float[] vN = createNegativeInfAtFloat(_stdFloat, 2);
    boolean tmpN = IsInf.any(vN);
    assertTrue(tmpN == true);
  }

  @Test
  public void testIsInfAnyPositiveInfPositivePresentFloat() {
    float[] vP = createPositiveInfAtFloat(_stdFloat, 2);
    boolean tmpP = IsInf.anyPositive(vP);
    assertTrue(tmpP == true);
  }

  @Test
  public void testIsInfAnyNegativeInfNegativePresentFloat() {
    float[] vN = createNegativeInfAtFloat(_stdFloat, 2);
    boolean tmpN = IsInf.anyNegative(vN);
    assertTrue(tmpN == true);
  }

  @Test
  public void testIsInfgetBooleansNoInfsFloat() {
    boolean[] tmp = IsInf.getBooleans(_stdFloat);
    assertTrue(Arrays.equals(tmp, _allFalse));
  }

  @Test
  public void testIsInfgetBooleansNoPositiveInfsFloat() {
    float[] vN = createNegativeInfAtFloat(_stdFloat, 2);
    boolean[] tmp = IsInf.getBooleansPositive(vN);
    assertTrue(Arrays.equals(tmp, _allFalse));
  }

  @Test
  public void testIsInfgetBooleansNoNegativeInfsFloat() {
    float[] vP = createPositiveInfAtFloat(_stdFloat, 2);
    boolean[] tmp = IsInf.getBooleansNegative(vP);
    assertTrue(Arrays.equals(tmp, _allFalse));
  }

  @Test
  public void testIsInfgetBooleansInfPresentFloat() {
    float[] v = createPositiveInfAtFloat(_stdFloat, 2);
    v[0] = -1 / 0f;
    boolean[] tmp = IsInf.getBooleans(v);
    assertTrue(Arrays.equals(tmp, _infPositions));
  }

  @Test
  public void testIsInfgetBooleansPositiveInfPresentFloat() {
    float[] v = createPositiveInfAtFloat(_stdFloat, _turnToInf);
    boolean[] tmp = IsInf.getBooleansPositive(v);
    assertTrue(Arrays.equals(tmp, _infPositions));
  }

  @Test
  public void testIsInfgetBooleansNegativeInfPresentFloat() {
    float[] v = createNegativeInfAtFloat(_stdFloat, _turnToInf);
    boolean[] tmp = IsInf.getBooleansNegative(v);
    assertTrue(Arrays.equals(tmp, _infPositions));
  }

}
