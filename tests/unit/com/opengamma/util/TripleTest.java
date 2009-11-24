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
 * @author emcleod
 */
public class TripleTest {

  @Test
  public void testEquals() {
    assertEquals(new Triple<Integer, Integer, Integer>(1, 2, 3), new Triple<Integer, Integer, Integer>(1, 2, 3));
    assertFalse(new Triple<Integer, Integer, Integer>(1, 2, 3).equals(new Triple<Integer, Integer, Integer>(1, 2, 4)));    
    assertFalse(new Triple<Integer, Integer, Integer>(1, 2, 3).equals(new Triple<Integer, Integer, Integer>(1, 3, 3)));    
    assertFalse(new Triple<Integer, Integer, Integer>(1, 2, 3).equals(new Triple<Integer, Integer, Integer>(0, 2, 3)));    
  }
  
  @Test
  public void testCompare() {
    assertTrue(new Triple<Integer, Integer, Integer>(1, 2, 3).compareTo(new Triple<Integer, Integer, Integer>(1, 2, 0)) > 0);
    assertTrue(new Triple<Integer, Integer, Integer>(1, 2, 3).compareTo(new Triple<Integer, Integer, Integer>(1, 2, 5)) < 0);
    assertTrue(new Triple<Integer, Integer, Integer>(1, 2, 3).compareTo(new Triple<Integer, Integer, Integer>(1, 1, 3)) > 0);
    assertTrue(new Triple<Integer, Integer, Integer>(1, 2, 3).compareTo(new Triple<Integer, Integer, Integer>(1, 3, 3)) < 0);
    assertTrue(new Triple<Integer, Integer, Integer>(1, 2, 3).compareTo(new Triple<Integer, Integer, Integer>(0, 2, 3)) > 0);
    assertTrue(new Triple<Integer, Integer, Integer>(1, 2, 3).compareTo(new Triple<Integer, Integer, Integer>(2, 2, 3)) < 0);
    assertEquals(new Triple<Integer, Integer, Integer>(1, 2, 3).compareTo(new Triple<Integer, Integer, Integer>(1, 2, 3)), 0);
  }
}
