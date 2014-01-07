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
 * Test LongDoublePair.
 */
@Test(groups = TestGroup.UNIT)
public class LongDoublePairTest {

  public void test_LongDoublePair_of() {
    LongDoublePair test = LongDoublePair.of(2L, 2.5d);
    assertEquals(Long.valueOf(2), test.getFirst());
    assertEquals(Double.valueOf(2.5d), test.getSecond());
    assertEquals(2, test.getFirstLong());
    assertEquals(2.5d, test.getSecondDouble(), 0.00001d);
    assertEquals(Long.valueOf(2), test.getKey());
    assertEquals(Double.valueOf(2.5d), test.getValue());
    assertEquals(2, test.getLongKey());
    assertEquals(2.5d, test.getDoubleValue(), 0.00001d);
    assertEquals("[2, 2.5]", test.toString());
  }

  public void test_LongDoublePair_parse1() {
    LongDoublePair test = LongDoublePair.parse("[2, 2.5]");
    assertEquals(Long.valueOf(2), test.getFirst());
    assertEquals(Double.valueOf(2.5d), test.getSecond());
  }

  public void test_LongDoublePair_parse2() {
    LongDoublePair test = LongDoublePair.parse("[2,2.5]");
    assertEquals(Long.valueOf(2), test.getFirst());
    assertEquals(Double.valueOf(2.5d), test.getSecond());
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testSetValue() {
    LongDoublePair pair = LongDoublePair.of(2L, -0.3d);
    pair.setValue(Double.valueOf(1.2d));
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testSetValue_null() {
    LongDoublePair pair = LongDoublePair.of(2L, -0.3d);
    pair.setValue(null);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testSetValue_primitives() {
    LongDoublePair pair = LongDoublePair.of(2L, -0.3d);
    pair.setValue(1.2d);
  }

  //-------------------------------------------------------------------------
  public void compareTo() {
    LongDoublePair ab = LongDoublePair.of(1L, 1.7d);
    LongDoublePair ac = LongDoublePair.of(1L, 1.9d);
    LongDoublePair ba = LongDoublePair.of(2L, 1.5d);

    FirstThenSecondPairComparator<Long, Double> comparator = new FirstThenSecondPairComparator<Long, Double>();
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

  public void testEquals() {
    LongDoublePair a = LongDoublePair.of(1L, 2.0);
    LongDoublePair b = LongDoublePair.of(1L, 3.0);
    LongDoublePair c = LongDoublePair.of(2L, 2.0);
    LongDoublePair d = LongDoublePair.of(2L, 3.0);
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
    LongDoublePair a = LongDoublePair.of(1L, 1.7d);
    Pair<Long, Double> b = ObjectsPair.of(Long.valueOf(1), Double.valueOf(1.7d));
    assertEquals(true, a.equals(b));
    assertEquals(true, b.equals(a));
  }

  public void testEquals_toObjectVersion_null() {
    LongDoublePair b = LongDoublePair.of(1L, 1.7d);
    Pair<Long, Double> a = ObjectsPair.of(null, Double.valueOf(1.9d));
    assertEquals(true, a.equals(a));
    assertEquals(false, a.equals(b));
    assertEquals(false, b.equals(a));
    assertEquals(true, b.equals(b));
  }

  public void testHashCode() {
    LongDoublePair a = LongDoublePair.of(1L, 1.7d);
    Pair<Long, Double> b = ObjectsPair.of(Long.valueOf(1), Double.valueOf(1.7d));
    assertEquals(a.hashCode(), b.hashCode());
  }

  public void testHashCode_value() {
    LongDoublePair a = LongDoublePair.of(1L, 2.0);
    assertEquals(a.hashCode(), a.hashCode());
    assertEquals(Long.valueOf(1).hashCode() ^ Double.valueOf(2.0).hashCode(), a.hashCode());
    // can't test for different hash codes as they might not be different
  }

}
