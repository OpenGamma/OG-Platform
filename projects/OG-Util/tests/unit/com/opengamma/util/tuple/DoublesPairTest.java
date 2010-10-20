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
 * Test DoublesPair.
 */
public class DoublesPairTest {

  @Test
  public void testOf_double_double() {
    DoublesPair test = Pair.of(1.5d, -0.3d);
    assertEquals(test.getFirst(), Double.valueOf(1.5d));
    assertEquals(test.getSecond(), Double.valueOf(-0.3d));
    assertEquals(test.getFirstDouble(), 1.5d, 1E-10);
    assertEquals(test.getSecondDouble(), -0.3d, 1E-10);
    assertEquals(test.getKey(), Double.valueOf(1.5d));
    assertEquals(test.getValue(), Double.valueOf(-0.3d));
    assertEquals(test.getDoubleKey(), 1.5d, 1E-10);
    assertEquals(test.getDoubleValue(), -0.3d, 1E-10);
  }

  //-------------------------------------------------------------------------
  @Test
  public void testPair_Object_Object() {
    DoublesPair test = new DoublesPair(1.5d, -0.3d);
    assertEquals(test.getFirst(), Double.valueOf(1.5d));
    assertEquals(test.getSecond(), Double.valueOf(-0.3d));
    assertEquals(test.getFirstDouble(), 1.5d, 1E-10);
    assertEquals(test.getSecondDouble(), -0.3d, 1E-10);
    assertEquals(test.getKey(), Double.valueOf(1.5d));
    assertEquals(test.getValue(), Double.valueOf(-0.3d));
    assertEquals(test.getDoubleKey(), 1.5d, 1E-10);
    assertEquals(test.getDoubleValue(), -0.3d, 1E-10);
  }

  //-------------------------------------------------------------------------
  @Test(expected=UnsupportedOperationException.class)
  public void testSetValue() {
    DoublesPair pair = new DoublesPair(1.5d, -0.3d);
    pair.setValue(Double.valueOf(1.2));
  }

  @Test(expected=UnsupportedOperationException.class)
  public void testSetValue_null() {
    DoublesPair pair = new DoublesPair(1.5d, -0.3d);
    pair.setValue(null);
  }

  @Test(expected=UnsupportedOperationException.class)
  public void testSetValue_primitives() {
    DoublesPair pair = new DoublesPair(1.5d, -0.3d);
    pair.setValue(1.2d);
  }

  //-------------------------------------------------------------------------
  @Test
  public void compareTo() {
    DoublesPair ab = Pair.of(1.5d, 1.7d);
    DoublesPair ac = Pair.of(1.5d, 1.9d);
    DoublesPair ba = Pair.of(1.7d, 1.5d);
    
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
    DoublesPair a = Pair.of(1.5d, 1.7d);
    DoublesPair b = Pair.of(1.5d, 1.9d);
    DoublesPair c = Pair.of(1.7d, 1.7d);
    DoublesPair d = Pair.of(1.7d, 1.9d);
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
    DoublesPair a = Pair.of(1.5d, 1.7d);
    Pair<Double, Double> b = Pair.of(Double.valueOf(1.5d), Double.valueOf(1.7d));
    assertEquals(a.equals(b), true);
    assertEquals(b.equals(a), true);
  }

  @Test
  public void testEquals_toObjectVersion_null() {
    Pair<Double, Double> a = Pair.of(null, Double.valueOf(1.9d));
    DoublesPair b = Pair.of(1.5d, 1.7d);
    assertEquals(a.equals(a), true);
    assertEquals(a.equals(b), false);
    assertEquals(b.equals(a), false);
    assertEquals(b.equals(b), true);
  }

  @Test
  public void testHashCode() {
    DoublesPair a = Pair.of(1.5d, 1.7d);
    Pair<Double, Double> b = Pair.of(Double.valueOf(1.5d), Double.valueOf(1.7d));
    assertEquals(a.hashCode(), b.hashCode());
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testDoublesPairConversionNullFirst() {
    Pair<Double, Double> pair = new ObjectsPair<Double, Double>(null, 0.);
    DoublesPair.of(pair);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testDoublesPairConversionNullSecond() {
    Pair<Double, Double> pair = new ObjectsPair<Double, Double>(0., null);
    DoublesPair.of(pair);
  }
  
  @Test
  public void testDoublesPairConversion() {
    final double first = 1.2;
    final double second = 3.4;
    DoublesPair pair1 = new DoublesPair(first, second);
    Pair<Double, Double> pair2 = new ObjectsPair<Double, Double>(first, second);
    assertEquals(pair1, DoublesPair.of(pair2));
  }

}
