/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.tuple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Test LongDoublePair.
 */
public class LongDoublePairTest {

  @Test
  public void testConstructionGets() {
    LongDoublePair test = new LongDoublePair(1L, 2.0d);
    assertEquals(test.getFirst(), Long.valueOf(1L));
    assertEquals(test.getSecond(), Double.valueOf(2.0d));
    assertEquals(test.getFirstLong(), 1L);
    assertEquals(test.getSecondDouble(), 2.0d, 1E-10);
    assertEquals(test.getKey(), Long.valueOf(1L));
    assertEquals(test.getValue(), Double.valueOf(2.0d));
    assertEquals(test.getLongKey(), 1L);
    assertEquals(test.getDoubleValue(), 2.0, 1E-10);
  }

  //-------------------------------------------------------------------------
  @Test(expected=UnsupportedOperationException.class)
  public void testSetValue() {
    LongDoublePair pair = new LongDoublePair(2L, -0.3d);
    pair.setValue(Double.valueOf(1.2d));
  }

  @Test(expected=UnsupportedOperationException.class)
  public void testSetValue_null() {
    LongDoublePair pair = new LongDoublePair(2L, -0.3d);
    pair.setValue(null);
  }

  @Test(expected=UnsupportedOperationException.class)
  public void testSetValue_primitives() {
    LongDoublePair pair = new LongDoublePair(2L, -0.3d);
    pair.setValue(1.2d);
  }

  //-------------------------------------------------------------------------
  @Test
  public void compareTo() {
    LongDoublePair ab = Pair.of(1L, 1.7d);
    LongDoublePair ac = Pair.of(1L, 1.9d);
    LongDoublePair ba = Pair.of(2L, 1.5d);
    
    assertTrue(ab.compareTo(ab) == 0);
    assertTrue(ab.compareTo(ac) < 0);
    assertTrue(ab.compareTo(ba) < 0);
    
    assertTrue(ac.compareTo(ab) > 0);
    assertTrue(ac.compareTo(ac) == 0);
    assertTrue(ac.compareTo(ba) < 0);
    
    assertTrue(ba.compareTo(ab) > 0);
    assertTrue(ba.compareTo(ac) > 0);
    assertTrue(ba.compareTo(ba) == 0);
  }

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
  public void testEquals_toObjectVersion() {
    LongDoublePair a = Pair.of(1L, 1.7d);
    Pair<Long, Double> b = Pair.of(Long.valueOf(1L), Double.valueOf(1.7d));
    assertEquals(a.equals(b), true);
    assertEquals(b.equals(a), true);
  }

  @Test
  public void testEquals_toObjectVersion_null() {
    Pair<Long, Double> a = Pair.of(null, Double.valueOf(1.9d));
    LongDoublePair b = Pair.of(1L, 1.7d);
    assertEquals(a.equals(a), true);
    assertEquals(a.equals(b), false);
    assertEquals(b.equals(a), false);
    assertEquals(b.equals(b), true);
  }

  @Test
  public void testHashCode() {
    LongDoublePair a = Pair.of(1L, 1.7d);
    Pair<Long, Double> b = Pair.of(Long.valueOf(1L), Double.valueOf(1.7d));
    assertEquals(a.hashCode(), b.hashCode());
  }

  @Test
  public void testHashCode_value() {
    LongDoublePair a = new LongDoublePair(1L, 2.0);
    assertEquals(a.hashCode(), a.hashCode());
    assertEquals(a.hashCode(), Long.valueOf(1L).hashCode() ^ Double.valueOf(2.0).hashCode());
    // can't test for different hash codes as they might not be different
  }

}
