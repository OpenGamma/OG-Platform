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
 * Test IntDoublePair.
 */
@Test(groups = TestGroup.UNIT)
public class IntDoublePairTest {

  public void test_IntDoublePair_of() {
    IntDoublePair test = IntDoublePair.of(2, 2.5d);
    assertEquals(Integer.valueOf(2), test.getFirst());
    assertEquals(Double.valueOf(2.5d), test.getSecond());
    assertEquals(2, test.getFirstInt());
    assertEquals(2.5d, test.getSecondDouble(), 0.00001d);
    assertEquals(Integer.valueOf(2), test.getKey());
    assertEquals(Double.valueOf(2.5d), test.getValue());
    assertEquals(2, test.getIntKey());
    assertEquals(2.5d, test.getDoubleValue(), 0.00001d);
    assertEquals("[2, 2.5]", test.toString());
  }

  public void test_IntDoublePair_parse1() {
    IntDoublePair test = IntDoublePair.parse("[2, 2.5]");
    assertEquals(Integer.valueOf(2), test.getFirst());
    assertEquals(Double.valueOf(2.5d), test.getSecond());
  }

  public void test_IntDoublePair_parse2() {
    IntDoublePair test = IntDoublePair.parse("[2,2.5]");
    assertEquals(Integer.valueOf(2), test.getFirst());
    assertEquals(Double.valueOf(2.5d), test.getSecond());
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testSetValue() {
    IntDoublePair pair = IntDoublePair.of(2, -0.3d);
    pair.setValue(Double.valueOf(1.2d));
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testSetValue_null() {
    IntDoublePair pair = IntDoublePair.of(2, -0.3d);
    pair.setValue(null);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testSetValue_primitives() {
    IntDoublePair pair = IntDoublePair.of(2, -0.3d);
    pair.setValue(1.2d);
  }

  //-------------------------------------------------------------------------
  public void compareTo_IntDoublePair() {
    IntDoublePair p12 = IntDoublePair.of(1, 2d);
    IntDoublePair p13 = IntDoublePair.of(1, 3d);
    IntDoublePair p21 = IntDoublePair.of(2, 1d);
    
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

  public void compareTo_IntDoublePairAsPair() {
    Pair<Integer, Double> p12 = IntDoublePair.of(1, 2d);
    Pair<Integer, Double> p13 = IntDoublePair.of(1, 3d);
    Pair<Integer, Double> p21 = IntDoublePair.of(2, 1d);
    
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

  public void compareTo_comparatorFirstThenSecond() {
    IntDoublePair ab = IntDoublePair.of(1, 1.7d);
    IntDoublePair ac = IntDoublePair.of(1, 1.9d);
    IntDoublePair ba = IntDoublePair.of(2, 1.5d);

    FirstThenSecondPairComparator<Integer, Double> comparator = new FirstThenSecondPairComparator<Integer, Double>();
    assertTrue(comparator.compare(ab, ab) == 0);
    assertTrue(comparator.compare(ac, ab) > 0);
    assertTrue(comparator.compare(ba, ab) > 0);

    assertTrue(comparator.compare(ab, ac) < 0);
    assertTrue(comparator.compare(ac, ac) == 0);
    assertTrue(comparator.compare(ba, ac) > 0);

    assertTrue(comparator.compare(ab, ba) < 0);
    assertTrue(comparator.compare(ac, ba) < 0);
    assertTrue(comparator.compare(ba, ba) == 0);
  }

  //-------------------------------------------------------------------------
  public void testEquals() {
    IntDoublePair a = IntDoublePair.of(1, 2.0);
    IntDoublePair b = IntDoublePair.of(1, 3.0);
    IntDoublePair c = IntDoublePair.of(2, 2.0);
    IntDoublePair d = IntDoublePair.of(2, 3.0);
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
    IntDoublePair a = IntDoublePair.of(1, 1.7d);
    Pair<Integer, Double> b = ObjectsPair.of(Integer.valueOf(1), Double.valueOf(1.7d));
    assertEquals(true, a.equals(b));
    assertEquals(true, b.equals(a));
  }

  public void testEquals_toObjectVersion_null() {
    IntDoublePair b = IntDoublePair.of(1, 1.7d);
    Pair<Integer, Double> a = ObjectsPair.of(null, Double.valueOf(1.9d));
    assertEquals(true, a.equals(a));
    assertEquals(false, a.equals(b));
    assertEquals(false, b.equals(a));
    assertEquals(true, b.equals(b));
  }

  public void testHashCode() {
    IntDoublePair a = IntDoublePair.of(1, 1.7d);
    Pair<Integer, Double> b = ObjectsPair.of(Integer.valueOf(1), Double.valueOf(1.7d));
    assertEquals(a.hashCode(), b.hashCode());
  }

  public void testHashCode_value() {
    IntDoublePair a = IntDoublePair.of(1, 2.0);
    assertEquals(a.hashCode(), a.hashCode());
    assertEquals(Integer.valueOf(1).hashCode() ^ Double.valueOf(2.0).hashCode(), a.hashCode());
    // can't test for different hash codes as they might not be different
  }

}
