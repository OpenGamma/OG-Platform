/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * 
 *
 * @author kirk
 */
public class PairTest {
  
  @Test
  public void equals() {
    assertEquals(new Pair<String,String>("Kirk", "Wylie"), new Pair<String,String>("Kirk", "Wylie"));
    assertFalse(new Pair<String,String>("Kirk", "Wylie").equals(new Pair<String,String>("Jim", "Moores")));
  }
  
  @Test
  public void compareTo() {
    assertTrue(new Pair<String,String>("Kirk", "Wylie").compareTo(new Pair<String,String>("Jim", "Wylie")) > 0);
    assertTrue(new Pair<String,String>("Kirk", "Wylie").compareTo(new Pair<String,String>("Kirk", "Moores")) > 0);
    assertTrue(new Pair<String,String>("Jim", "Wylie").compareTo(new Pair<String,String>("Kirk", "Wylie")) < 0);
    assertTrue(new Pair<String,String>("Kirk", "Moores").compareTo(new Pair<String,String>("Kirk", "Wylie")) < 0);
    assertEquals(0, new Pair<String,String>("Kirk", "Wylie").compareTo(new Pair<String,String>("Kirk", "Wylie")));
  }

}
