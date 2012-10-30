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
 * Tests the DCSEVL class
 */
public class DCSEVLTest {

  @Test(expectedExceptions=MathsExceptionIllegalArgument.class)
  public void nullTest(){
    DCSEVL.getDCSEVL(1, null, 5);    
  }

  @Test(expectedExceptions=MathsExceptionIllegalArgument.class)
  public void negTermsTest(){
    double [] cs={1,2,3};
    DCSEVL.getDCSEVL(1, cs, -1);    
  }

  @Test(expectedExceptions=MathsExceptionIllegalArgument.class)
  public void tooManyTermsTest(){
    double [] cs={1,2,3};
    DCSEVL.getDCSEVL(1, cs, 1001);    
  }  

  @Test(expectedExceptions=MathsExceptionIllegalArgument.class)
  public void outOfBoundsTest(){
    double [] cs={1,2,3};
    DCSEVL.getDCSEVL(1.01, cs, 10);    
  }  
  
  @Test(expectedExceptions=MathsExceptionIllegalArgument.class)
  public void outMoreTermsTheCoeffs(){
    double [] cs={1,2,3};
    DCSEVL.getDCSEVL(0.9, cs, 10);    
  }    
  
  @Test
  public void checkCall(){
    double [] cs={1,2,3};
    assertTrue(DCSEVL.getDCSEVL(0.9, cs, 3)==4.16);
  }    
  
}
