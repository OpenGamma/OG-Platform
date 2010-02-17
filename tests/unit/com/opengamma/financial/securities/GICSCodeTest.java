/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.securities;

import org.junit.Test;

import com.opengamma.financial.GICSCode;

/**
 * 
 *
 * @author Andrew
 */
public class GICSCodeTest {
  
  @Test(expected=IllegalArgumentException.class)
  public void testInvalid1 () {
    GICSCode.getInstance (0);
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testInvalid2 () {
    GICSCode.getInstance (100);
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testInvalid3 () {
    GICSCode.getInstance (10100);
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testInvalid4 () {
    GICSCode.getInstance (1010100);
  }
  
  @Test
  public void testValid1 () {
    for (int i = 1; i <= 99; i++) {
      GICSCode.getInstance (i);
    }
  }
  
  @Test
  public void testValid2 () {
    for (int i = 1; i <= 99; i++) {
      GICSCode.getInstance (100 + i);
    }
  }
  
  @Test
  public void testValid3 () {
    for (int i = 1; i <= 99; i++) {
      GICSCode.getInstance (10100 + i);
    }
  }
  
  @Test
  public void testValid4 () {
    for (int i = 1; i <= 99; i++) {
      GICSCode.getInstance (1010100 + i);
    }
  }
  
}
