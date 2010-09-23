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
 * Test IntDoublePair.
 */
public class IntDoublePairTest {

  @Test
  public void testConstructionGets() {
    IntDoublePair test = new IntDoublePair(1, 2.0d);
    assertEquals(test.getFirst(), Integer.valueOf(1));
    assertEquals(test.getSecond(), Double.valueOf(2.0d));
    assertEquals(test.getFirstInt(), 1);
    assertEquals(test.getSecondDouble(), 2.0d, 1E-10);
    assertEquals(test.getKey(), Integer.valueOf(1));
    assertEquals(test.getValue(), Double.valueOf(2.0d));
    assertEquals(test.getIntKey(), 1);
    assertEquals(test.getDoubleValue(), 2.0d, 1E-10);
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
    IntDoublePair a = Pair.of(1, 1.7d);
    Pair<Integer, Double> b = Pair.of(Integer.valueOf(1), Double.valueOf(1.7d));
    assertEquals(a.equals(b), true);
    assertEquals(b.equals(a), true);
  }

  @Test
  public void testEquals_toObjectVersion_null() {
    Pair<Integer, Double> a = Pair.of(null, Double.valueOf(1.9d));
    IntDoublePair b = Pair.of(1, 1.7d);
    assertEquals(a.equals(a), true);
    assertEquals(a.equals(b), false);
    assertEquals(b.equals(a), false);
    assertEquals(b.equals(b), true);
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
    assertEquals(a.hashCode(), Integer.valueOf(1).hashCode() ^ Double.valueOf(2.0).hashCode());
    // can't test for different hash codes as they might not be different
  }

}
