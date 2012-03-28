/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.functions.iss;

import static org.testng.AssertJUnit.assertTrue;
import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.maths.lowlevelapi.functions.iss.IsNaN;

/**
 * Tests the IsNaN class
 */
public class IsNaNTest {

  double[] _nullDouble = null;
  double[] _stdDouble = {1, 2, 3, 4, 5 };
  float[] _nullFloat = null;
  float[] _stdFloat = {1, 2, 3, 4, 5 };  
  boolean[] _NaNPositions = {true, false, true, false, false };
  int[] _turnToNaN = {0, 2 };
  boolean[] _allFalse = {false, false, false, false, false };

  /* helpers, create NaNs at given indices */
  private double[] createNaNAtDouble(double[] v, int... i) {
    double[] tmp = Arrays.copyOf(v, v.length);
    double zero = 0d;
    for (int index = 0; index < i.length; index++)
    {
      tmp[i[index]] = 0/zero;
    }
    return tmp;
  }
  
  private float[] createNaNAtFloat(float[] v, int... i) {
    float[] tmp = Arrays.copyOf(v, v.length);
    float zero = 0f;
    for (int index = 0; index < i.length; index++)
    {
      tmp[i[index]] = 0/zero;
    }
    return tmp;
  }  

  /* doubles */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testIsNaNAnyNullDouble() {
    IsNaN.any(_nullDouble);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testIsNaNgetBooleansNullDouble() {
    IsNaN.getBooleans(_nullDouble);
  }

  @Test
  public void testIsNaNAnyNoNaNsDouble() {
    boolean tmp = IsNaN.any(_stdDouble);
    assertTrue(tmp == false);
  }

  @Test
  public void testIsNaNAnyNaNPresentDouble() {
    double[] v = createNaNAtDouble(_stdDouble, 2);
    boolean tmp = IsNaN.any(v);
    assertTrue(tmp == true);
  }

  @Test
  public void testIsNaNgetBooleansNoNaNsDouble() {
    boolean[] tmp = IsNaN.getBooleans(_stdDouble);
    assertTrue(Arrays.equals(tmp, _allFalse));
  }

  @Test
  public void testIsNaNgetBooleansNaNPresentDouble() {
    double[] v = createNaNAtDouble(_stdDouble, _turnToNaN);
    boolean[] tmp = IsNaN.getBooleans(v);
    assertTrue(Arrays.equals(tmp, _NaNPositions));
  }
  
 /* floats */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testIsNaNAnyNullFloat() {
    IsNaN.any(_nullFloat);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testIsNaNgetBooleansNullFloat() {
    IsNaN.getBooleans(_nullFloat);
  }

  @Test
  public void testIsNaNAnyNoNaNsFloat() {
    boolean tmp = IsNaN.any(_stdFloat);
    assertTrue(tmp == false);
  }

  @Test
  public void testIsNaNAnyNaNPresentFloat() {
    float[] v = createNaNAtFloat(_stdFloat, 2);
    boolean tmp = IsNaN.any(v);
    assertTrue(tmp == true);
  }

  @Test
  public void testIsNaNgetBooleansNoNaNsFloat() {
    boolean[] tmp = IsNaN.getBooleans(_stdFloat);
    assertTrue(Arrays.equals(tmp, _allFalse));
  }

  @Test
  public void testIsNaNgetBooleansNaNPresentFloat() {
    float[] v = createNaNAtFloat(_stdFloat, _turnToNaN);
    boolean[] tmp = IsNaN.getBooleans(v);
    assertTrue(Arrays.equals(tmp, _NaNPositions));
  }  

}
