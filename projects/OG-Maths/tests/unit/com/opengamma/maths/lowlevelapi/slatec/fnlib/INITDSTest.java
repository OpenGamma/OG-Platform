/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.slatec.fnlib;

import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.maths.commonapi.exceptions.MathsExceptionIllegalArgument;

/**
 * Tests the INITDS function 
 */
public class INITDSTest {

  @Test(expectedExceptions = MathsExceptionIllegalArgument.class)
  public void nullTest() {
    INITDS.getInitds(null, 1, 1);
  }

  @Test(expectedExceptions = MathsExceptionIllegalArgument.class)
  public void badNOS() {
    double[] os = {1, 2, 3 };
    INITDS.getInitds(os, 42, 1);
  }

  @Test(expectedExceptions = MathsExceptionIllegalArgument.class)
  public void negNOS() {
    double[] os = {1, 2, 3 };
    INITDS.getInitds(os, 0, 1);
  }

  @Test(expectedExceptions = MathsExceptionIllegalArgument.class)
  public void shortSeries() {
    double[] os = {1e-16, 2e-16, 3e-16 };
    INITDS.getInitds(os, os.length, 1e-14);
  }

  @Test
  public void checkSeriesBreak() {
    double [] os = {4e-16,3e-16,2e-16,1e-16};
    assertTrue(INITDS.getInitds(os, os.length, 2e-16)==2);
  }

}
