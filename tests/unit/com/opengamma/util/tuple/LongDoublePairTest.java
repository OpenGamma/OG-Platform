/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.tuple;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


/**
 * Test LongDoublePair.
 */
public class LongDoublePairTest {

  @Test
  public void testConstructionGets() {
    LongDoublePair test = new LongDoublePair(1L, 2.0);
    assertEquals(test.getLongKey(), 1L);
    assertEquals(test.getDoubleValue(), 2.0, 1E-10);
    assertEquals(test.getKey(), (Long) 1L);
    assertEquals(test.getValue(), (Double) 2.0);
  }

  //-------------------------------------------------------------------------
  @Test
  public void testSetValue() {
    LongDoublePair test = new LongDoublePair(1L, 2.0);
    double old = test.setValue(3.0);
    assertEquals(test.getLongKey(), 1L);
    assertEquals(test.getDoubleValue(), 3.0, 1E-10);
    assertEquals(test.getKey(), (Long) 1L);
    assertEquals(test.getValue(), (Double) 3.0);
    assertEquals(old, 2.0, 1E-10);
  }

  //-------------------------------------------------------------------------
  @Test
  public void testEquals() {
    LongDoublePair a = new LongDoublePair(1L, 2.0);
    LongDoublePair b = new LongDoublePair(1L, 3.0);
    LongDoublePair c = new LongDoublePair(2L, 2.0);
    LongDoublePair d = new LongDoublePair(2L, 3.0);
    assertEquals(a.equals(a), true);
    assertEquals(a.equals(b), false);
    assertEquals(a.equals(c), false);
    assertEquals(a.equals(d), false);
    
    assertEquals(b.equals(a), false);
    assertEquals(b.equals(b), true);
    assertEquals(b.equals(c), false);
    assertEquals(b.equals(d), false);
    
    assertEquals(c.equals(a), false);
    assertEquals(c.equals(b), false);
    assertEquals(c.equals(c), true);
    assertEquals(c.equals(d), false);
    
    assertEquals(d.equals(a), false);
    assertEquals(d.equals(b), false);
    assertEquals(d.equals(c), false);
    assertEquals(d.equals(d), true);
  }

  @Test
  public void testHashCode() {
    LongDoublePair a = new LongDoublePair(1L, 2.0);
    assertEquals(a.hashCode(), a.hashCode());
    assertEquals(a.hashCode(), Long.valueOf(1L).hashCode() ^ Double.valueOf(2.0).hashCode());
    // can't test for different hash codes as they might not be different
  }
  
}
