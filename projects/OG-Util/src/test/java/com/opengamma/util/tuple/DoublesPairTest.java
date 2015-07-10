/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.tuple;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test DoublesPair.
 */
@Test(groups = TestGroup.UNIT)
public class DoublesPairTest {

  public void test_DoublesPair_of() {
    DoublesPair test = DoublesPair.of(1.2d, 2.5d);
    assertEquals(Double.valueOf(1.2d), test.getFirst());
    assertEquals(Double.valueOf(2.5d), test.getSecond());
    assertEquals(1.2d, test.getFirstDouble(), 0.00001d);
    assertEquals(2.5d, test.getSecondDouble(), 0.00001d);
    assertEquals(Double.valueOf(1.2d), test.getKey());
    assertEquals(Double.valueOf(2.5d), test.getValue());
    assertEquals(1.2d, test.getDoubleKey(), 0.00001d);
    assertEquals(2.5d, test.getDoubleValue(), 0.00001d);
    assertEquals("[1.2, 2.5]", test.toString());
  }

  public void test_DoublesPair_parse1() {
    DoublesPair test = DoublesPair.parse("[1.2, 2.5]");
    assertEquals(Double.valueOf(1.2d), test.getFirst());
    assertEquals(Double.valueOf(2.5d), test.getSecond());
  }

  public void test_DoublesPair_parse2() {
    DoublesPair test = DoublesPair.parse("[1.2,2.5]");
    assertEquals(Double.valueOf(1.2d), test.getFirst());
    assertEquals(Double.valueOf(2.5d), test.getSecond());
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testSetValue() {
    DoublesPair pair = DoublesPair.of(2.1d, -0.3d);
    pair.setValue(Double.valueOf(1.2d));
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testSetValue_null() {
    DoublesPair pair = DoublesPair.of(2.1d, -0.3d);
    pair.setValue(null);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testSetValue_primitives() {
    DoublesPair pair = DoublesPair.of(2.1d, -0.3d);
    pair.setValue(1.2d);
  }

  //-------------------------------------------------------------------------
  public void compareTo_DoublesPair() {
    DoublesPair p12 = DoublesPair.of(1d, 2d);
    DoublesPair p13 = DoublesPair.of(1d, 3d);
    DoublesPair p21 = DoublesPair.of(2d, 1d);
    
    assertTrue(p12.compareTo(p12) == 0);
    assertTrue(p12.compareTo(p13) < 0);
    assertTrue(p12.compareTo(p21) < 0);
    
    assertTrue(p13.compareTo(p12) > 0);
    assertTrue(p13.compareTo(p13) == 0);
    assertTrue(p13.compareTo(p21) < 0);
    
    assertTrue(p21.compareTo(p12) > 0);
    assertTrue(p21.compareTo(p13) > 0);
    assertTrue(p21.compareTo(p21) == 0);
  }

  public void compareTo_DoublesPairAsPair() {
    Pair<Double, Double> p12 = DoublesPair.of(1d, 2d);
    Pair<Double, Double> p13 = DoublesPair.of(1d, 3d);
    Pair<Double, Double> p21 = DoublesPair.of(2d, 1d);
    
    assertTrue(p12.compareTo(p12) == 0);
    assertTrue(p12.compareTo(p13) < 0);
    assertTrue(p12.compareTo(p21) < 0);
    
    assertTrue(p13.compareTo(p12) > 0);
    assertTrue(p13.compareTo(p13) == 0);
    assertTrue(p13.compareTo(p21) < 0);
    
    assertTrue(p21.compareTo(p12) > 0);
    assertTrue(p21.compareTo(p13) > 0);
    assertTrue(p21.compareTo(p21) == 0);
  }

  //-------------------------------------------------------------------------
  public void testEquals() {
    DoublesPair a = DoublesPair.of(1d, 2.0d);
    DoublesPair b = DoublesPair.of(1d, 3.0d);
    DoublesPair c = DoublesPair.of(2d, 2.0d);
    DoublesPair d = DoublesPair.of(2d, 3.0d);
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
    DoublesPair a = DoublesPair.of(1.1d, 1.7d);
    Pair<Double, Double> b = ObjectsPair.of(Double.valueOf(1.1d), Double.valueOf(1.7d));
    assertEquals(true, a.equals(b));
    assertEquals(true, b.equals(a));
  }

  public void testEquals_toObjectVersion_null() {
    DoublesPair b = DoublesPair.of(1.1d, 1.7d);
    Pair<Double, Double> a = ObjectsPair.of(null, Double.valueOf(1.9d));
    assertEquals(true, a.equals(a));
    assertEquals(false, a.equals(b));
    assertEquals(false, b.equals(a));
    assertEquals(true, b.equals(b));
  }

  public void testHashCode() {
    DoublesPair a = DoublesPair.of(1.1d, 1.7d);
    Pair<Double, Double> b = ObjectsPair.of(Double.valueOf(1.1d), Double.valueOf(1.7d));
    assertEquals(a.hashCode(), b.hashCode());
  }

  public void testHashCode_value() {
    DoublesPair a = DoublesPair.of(1.1d, 2.0);
    assertEquals(a.hashCode(), a.hashCode());
    assertEquals(Double.valueOf(1.1d).hashCode() ^ Double.valueOf(2.0).hashCode(), a.hashCode());
    // can't test for different hash codes as they might not be different
  }

}
