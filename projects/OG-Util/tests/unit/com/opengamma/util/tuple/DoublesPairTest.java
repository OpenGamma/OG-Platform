/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.tuple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Test DoublesPair.
 */
public class DoublesPairTest {

  @Test
  public void test_DoublesPair_of_DoublesPair() {
    DoublesPair base = DoublesPair.of(1.2d, 2.5d);
    DoublesPair test = DoublesPair.of(base);
    assertSame(base, test);
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_DoublesPair_of_DoublesPair_null() {
    DoublesPair.of((DoublesPair) null);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_DoublesPair_of_PairDoubleDouble() {
    Pair<Double, Double> base = ObjectsPair.of(Double.valueOf(1.2d), Double.valueOf(2.5d));
    DoublesPair test = DoublesPair.of(base);
    assertEquals(new DoublesPair(1.2d, 2.5d), test);
  }

  @Test
  public void test_DoublesPair_of_PairDoubleDouble_DoublesPair() {
    Pair<Double, Double> base = DoublesPair.of(1.2d, 2.5d);
    DoublesPair test = DoublesPair.of(base);
    assertSame(base, test);
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_DoublesPair_of_PairDoubleDouble_null() {
    DoublesPair.of((Pair<Double, Double>) null);
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_DoublesPair_of_PairDoubleDouble_nullFirst() {
    Pair<Double, Double> base = ObjectsPair.of(null, Double.valueOf(2.5d));
    DoublesPair.of(base);
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_DoublesPair_of_PairDoubleDouble_nullSecond() {
    Pair<Double, Double> base = ObjectsPair.of(Double.valueOf(1.2d), null);
    DoublesPair.of(base);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_DoublesPair_ofNumbers_PairNumberNumber() {
    Pair<Integer, Long> base = ObjectsPair.of(Integer.valueOf(2), Long.valueOf(3L));
    DoublesPair test = DoublesPair.ofNumbers(base);
    assertEquals(new DoublesPair(2, 3), test);
  }

  @Test
  public void test_DoublesPair_of_PairNumberNumber_DoublesPair() {
    DoublesPair base = DoublesPair.of(1.2d, 2.5d);
    DoublesPair test = DoublesPair.ofNumbers(base);
    assertSame(base, test);
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_DoublesPair_ofNumbers_PairDoubleDouble_null() {
    DoublesPair.ofNumbers((Pair<Double, Double>) null);
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_DoublesPair_ofNumbers_PairDoubleDouble_nullFirst() {
    Pair<Double, Integer> base = ObjectsPair.of(null, Integer.valueOf(2));
    DoublesPair.ofNumbers(base);
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_DoublesPair_ofNumbers_PairDoubleDouble_nullSecond() {
    Pair<Integer, Double> base = ObjectsPair.of(Integer.valueOf(1), null);
    DoublesPair.ofNumbers(base);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_DoublesPair_of_double_double() {
    DoublesPair test = DoublesPair.of(1.2d, 2.5d);
    assertEquals(Double.valueOf(1.2d), test.getFirst());
    assertEquals(Double.valueOf(2.5d), test.getSecond());
    assertEquals(1.2d, test.getFirstDouble(), 1E-10);
    assertEquals(2.5d, test.getSecondDouble(), 1E-10);
    assertEquals(Double.valueOf(1.2d), test.getKey());
    assertEquals(Double.valueOf(2.5d), test.getValue());
    assertEquals(1.2d, test.getDoubleKey(), 1E-10);
    assertEquals(2.5d, test.getDoubleValue(), 1E-10);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_PairOf_double_double() {
    DoublesPair test = Pair.of(1.5d, -0.3d);
    assertEquals(Double.valueOf(1.5d), test.getFirst());
    assertEquals(Double.valueOf(-0.3d), test.getSecond());
    assertEquals(1.5d, test.getFirstDouble(), 1E-10);
    assertEquals(-0.3d, test.getSecondDouble(), 1E-10);
    assertEquals(Double.valueOf(1.5d), test.getKey());
    assertEquals(Double.valueOf(-0.3d), test.getValue());
    assertEquals(1.5d, test.getDoubleKey(), 1E-10);
    assertEquals(-0.3d, test.getDoubleValue(), 1E-10);
  }

  //-------------------------------------------------------------------------
  @Test
  public void testConstructionGets() {
    DoublesPair test = new DoublesPair(1.5d, -0.3d);
    assertEquals(Double.valueOf(1.5d), test.getFirst());
    assertEquals(Double.valueOf(-0.3d), test.getSecond());
    assertEquals(1.5d, test.getFirstDouble(), 1E-10);
    assertEquals(-0.3d, test.getSecondDouble(), 1E-10);
    assertEquals(Double.valueOf(1.5d), test.getKey());
    assertEquals(Double.valueOf(-0.3d), test.getValue());
    assertEquals(1.5d, test.getDoubleKey(), 1E-10);
    assertEquals(-0.3d, test.getDoubleValue(), 1E-10);
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
    DoublesPair a = Pair.of(1.5d, 1.7d);
    Pair<Double, Double> b = Pair.of(Double.valueOf(1.5d), Double.valueOf(1.7d));
    assertEquals(true, a.equals(b));
    assertEquals(true, b.equals(a));
  }

  @Test
  public void testEquals_toObjectVersion_null() {
    Pair<Double, Double> a = Pair.of(null, Double.valueOf(1.9d));
    DoublesPair b = Pair.of(1.5d, 1.7d);
    assertEquals(true, a.equals(a));
    assertEquals(false, a.equals(b));
    assertEquals(false, b.equals(a));
    assertEquals(true, b.equals(b));
  }

  @Test
  public void testHashCode() {
    DoublesPair a = Pair.of(1.5d, 1.7d);
    Pair<Double, Double> b = Pair.of(Double.valueOf(1.5d), Double.valueOf(1.7d));
    assertEquals(b.hashCode(), a.hashCode());
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
