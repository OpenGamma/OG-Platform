/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.linearalgebra.blas.auxiliary;

import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.maths.lowlevelapi.linearalgebra.blas.auxiliary.D1MACH;

/**
 * Tests double precision machine constants found in D1MACH
 */
public class D1MACHTest {

  @Test
  public void d1machTestone() {
    assertTrue(D1MACH.one()==2.2250738585072014E-308);
  }

  @Test
  public void d1machTesttwo() {
    assertTrue(D1MACH.two()==1.7976931348623157E308);
  }
  
  @Test
  public void d1machTestthree() {
    assertTrue(D1MACH.three()==1.1102230246251565E-16);
  }
  
  @Test
  public void d1machTestfour() {
    assertTrue(D1MACH.four()==2.220446049250313E-16);
  }
  
  @Test
  public void d1machTestfive() {
    assertTrue(D1MACH.five()==0.3010299956639812);
  }

}
