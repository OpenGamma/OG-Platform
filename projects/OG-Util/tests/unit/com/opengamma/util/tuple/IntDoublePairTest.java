/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.tuple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Test IntDoublePair.
 */
public class IntDoublePairTest {

  @Test
  public void test_IntDoublePair_of() {
    IntDoublePair test = IntDoublePair.of(1, 2.5d);
    assertEquals(new IntDoublePair(1, 2.5d), test);
  }

  @Test
  public void testConstructionGets() {
    IntDoublePair test = new IntDoublePair(1, 2.0d);
    assertEquals(Integer.valueOf(1), test.getFirst());
    assertEquals(Double.valueOf(2.0d), test.getSecond());
    assertEquals(1, test.getFirstInt());
    assertEquals(2.0d, test.getSecondDouble(), 1E-10);
    assertEquals(Integer.valueOf(1), test.getKey());
    assertEquals(Double.valueOf(2.0d), test.getValue());
    assertEquals(1, test.getIntKey());
    assertEquals(2.0d, test.getDoubleValue(), 1E-10);
  }

  //-------------------------------------------------------------------------
  @Test(expected=UnsupportedOperationException.class)
  public void testSetValue() {
    IntDoublePair pair = new IntDoublePair(2, -0.3d);
    pair.setValue(Double.valueOf(1.2d));
  }

  @Test(expected=UnsupportedOperationException.class)
  public void testSetValue_null() {
    IntDoublePair pair = new IntDoublePair(2, -0.3d);
    pair.setValue(null);
  }

  @Test(expected=UnsupportedOperationException.class)
  public void testSetValue_primitives() {
    IntDoublePair pair = new IntDoublePair(2, -0.3d);
    pair.setValue(1.2d);
  }

  //-------------------------------------------------------------------------
  @Test
  public void compareTo() {
    IntDoublePair ab = Pair.of(1, 1.7d);
    IntDoublePair ac = Pair.of(1, 1.9d);
    IntDoublePair ba = Pair.of(2, 1.5d);
    
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
    IntDoublePair a = new IntDoublePair(1, 2.0);
    IntDoublePair b = new IntDoublePair(1, 3.0);
    IntDoublePair c = new IntDoublePair(2, 2.0);
    IntDoublePair d = new IntDoublePair(2, 3.0);
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

  @Test
  public void testEquals_toObjectVersion() {
    IntDoublePair a = Pair.of(1, 1.7d);
    Pair<Integer, Double> b = Pair.of(Integer.valueOf(1), Double.valueOf(1.7d));
    assertEquals(true, a.equals(b));
    assertEquals(true, b.equals(a));
  }

  @Test
  public void testEquals_toObjectVersion_null() {
    Pair<Integer, Double> a = Pair.of(null, Double.valueOf(1.9d));
    IntDoublePair b = Pair.of(1, 1.7d);
    assertEquals(true, a.equals(a));
    assertEquals(false, a.equals(b));
    assertEquals(false, b.equals(a));
    assertEquals(true, b.equals(b));
  }

  @Test
  public void testHashCode() {
    IntDoublePair a = Pair.of(1, 1.7d);
    Pair<Integer, Double> b = Pair.of(Integer.valueOf(1), Double.valueOf(1.7d));
    assertEquals(a.hashCode(), b.hashCode());
  }

  @Test
  public void testHashCode_value() {
    IntDoublePair a = new IntDoublePair(1, 2.0);
    assertEquals(a.hashCode(), a.hashCode());
    assertEquals(Integer.valueOf(1).hashCode() ^ Double.valueOf(2.0).hashCode(), a.hashCode());
    // can't test for different hash codes as they might not be different
  }

}
