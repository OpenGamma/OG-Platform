/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.tuple;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

/**
 * Test LongDoublePair.
 */
@Test
public class LongDoublePairTest {

  public void test_LongDoublePair_of() {
    LongDoublePair test = LongDoublePair.of(1L, 2.5d);
    assertEquals(Long.valueOf(1L), test.getFirst());
    assertEquals(Double.valueOf(2.5d), test.getSecond());
    assertEquals(1L, test.getFirstLong());
    assertEquals(2.5d, test.getSecondDouble(), 1E-10);
    assertEquals(Long.valueOf(1L), test.getKey());
    assertEquals(Double.valueOf(2.5d), test.getValue());
    assertEquals(1L, test.getLongKey());
    assertEquals(2.5d, test.getDoubleValue(), 1E-10);
  }

  public void testConstructionGets() {
    LongDoublePair test = new LongDoublePair(1L, 2.0d);
    assertEquals(Long.valueOf(1L), test.getFirst());
    assertEquals(Double.valueOf(2.0d), test.getSecond());
    assertEquals(1L, test.getFirstLong());
    assertEquals(2.0d, test.getSecondDouble(), 1E-10);
    assertEquals(Long.valueOf(1L), test.getKey());
    assertEquals(Double.valueOf(2.0d), test.getValue());
    assertEquals(1L, test.getLongKey());
    assertEquals(2.0, test.getDoubleValue(), 1E-10);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testSetValue() {
    LongDoublePair pair = new LongDoublePair(2L, -0.3d);
    pair.setValue(Double.valueOf(1.2d));
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testSetValue_null() {
    LongDoublePair pair = new LongDoublePair(2L, -0.3d);
    pair.setValue(null);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testSetValue_primitives() {
    LongDoublePair pair = new LongDoublePair(2L, -0.3d);
    pair.setValue(1.2d);
  }

  //-------------------------------------------------------------------------
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

  public void testEquals() {
    LongDoublePair a = new LongDoublePair(1L, 2.0);
    LongDoublePair b = new LongDoublePair(1L, 3.0);
    LongDoublePair c = new LongDoublePair(2L, 2.0);
    LongDoublePair d = new LongDoublePair(2L, 3.0);
    assertEquals(true, a.equals(a));
    assertEquals(false, a.equals(b));
    assertEquals(false, a.equals(c));
    assertEquals(false, a.equals(d));
    
    assertEquals(false, b.equals(a));
    assertEquals(true, b.equals(b));
    assertEquals(false, b.equals(c));
    assertEquals(false, b.equals(d));
    
    assertEquals(false, c.equals(a));
    assertEquals(false, c.equals(b));
    assertEquals(true, c.equals(c));
    assertEquals(false, c.equals(d));
    
    assertEquals(false, d.equals(a));
    assertEquals(false, d.equals(b));
    assertEquals(false, d.equals(c));
    assertEquals(true, d.equals(d));
  }

  public void testEquals_toObjectVersion() {
    LongDoublePair a = Pair.of(1L, 1.7d);
    Pair<Long, Double> b = Pair.of(Long.valueOf(1L), Double.valueOf(1.7d));
    assertEquals(true, a.equals(b));
    assertEquals(true, b.equals(a));
  }

  public void testEquals_toObjectVersion_null() {
    Pair<Long, Double> a = Pair.of(null, Double.valueOf(1.9d));
    LongDoublePair b = Pair.of(1L, 1.7d);
    assertEquals(true, a.equals(a));
    assertEquals(false, a.equals(b));
    assertEquals(false, b.equals(a));
    assertEquals(true, b.equals(b));
  }

  public void testHashCode() {
    LongDoublePair a = Pair.of(1L, 1.7d);
    Pair<Long, Double> b = Pair.of(Long.valueOf(1L), Double.valueOf(1.7d));
    assertEquals(b.hashCode(), a.hashCode());
  }

  public void testHashCode_value() {
    LongDoublePair a = new LongDoublePair(1L, 2.0);
    assertEquals(a.hashCode(), a.hashCode());
    assertEquals(Long.valueOf(1L).hashCode() ^ Double.valueOf(2.0).hashCode(), a.hashCode());
    // can't test for different hash codes as they might not be different
  }

}
