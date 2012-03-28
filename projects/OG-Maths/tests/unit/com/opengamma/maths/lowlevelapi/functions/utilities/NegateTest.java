/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.functions.utilities;

import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.Test;

import com.opengamma.maths.lowlevelapi.functions.utilities.Negate;

import java.util.Arrays;

/**
 * Tests the negate class 
 */
public class NegateTest {
  boolean[] _booleanNullVector = null;
  boolean[] _booleanVector = {true, false, true, false, false };
  boolean[] _negatedBooleanVector = {false, true, false, true, true };

  // stateless
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullStateless() {
    Negate.stateless(_booleanNullVector);
  }

  @Test
  public void testBooleanStateless() {
    assertTrue(Arrays.equals(Negate.stateless(_booleanVector), _negatedBooleanVector));
  }

  // in place
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInPlace() {
    Negate.inPlace(_booleanNullVector);
  }
  
  @Test
  public void testBooleanInPlace() {
    boolean [] tmp = Arrays.copyOf(_booleanVector,_booleanVector.length);
    Negate.inPlace(tmp);
    assertTrue(Arrays.equals(tmp, _negatedBooleanVector));
  }
  
}// class end
