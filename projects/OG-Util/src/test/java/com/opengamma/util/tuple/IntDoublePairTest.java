/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.tuple;

import static com.opengamma.util.tuple.TuplesUtil.pairToEntry;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.util.tuple.IntDoublePair;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.test.TestGroup;

/**
 * Test IntDoublePair.
 */
@Test(groups = TestGroup.UNIT)
public class IntDoublePairTest {

  public void test_IntDoublePair_of() {
    IntDoublePair test = IntDoublePair.of(1, 2.5d);
    assertEquals(new IntDoublePair(1, 2.5d), test);
  }

  public void testConstructionGets() {
    IntDoublePair test = new IntDoublePair(1, 2.0d);
    assertEquals(Integer.valueOf(1), test.getFirst());
    assertEquals(Double.valueOf(2.0d), test.getSecond());
    assertEquals(1, test.getFirstInt());
    assertEquals(2.0d, test.getSecondDouble(), 1E-10);
    assertEquals(Integer.valueOf(1), test.getFirst());
    assertEquals(Double.valueOf(2.0d), test.getSecond());
    assertEquals(1, test.getFirstInt());
    assertEquals(2.0d, test.getSecondDouble(), 1E-10);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testSetValue() {
    IntDoublePair pair = new IntDoublePair(2, -0.3d);
    pairToEntry(pair).setValue(Double.valueOf(1.2d));
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testSetValue_null() {
    IntDoublePair pair = new IntDoublePair(2, -0.3d);
    pairToEntry(pair).setValue(null);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testSetValue_primitives() {
    IntDoublePair pair = new IntDoublePair(2, -0.3d);
    pairToEntry(pair).setValue(1.2d);
  }

  //-------------------------------------------------------------------------
  public void compareTo() {
    IntDoublePair ab = Pair.of(1, 1.7d);
    IntDoublePair ac = Pair.of(1, 1.9d);
    IntDoublePair ba = Pair.of(2, 1.5d);

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

  public void testEquals_toObjectVersion() {
    IntDoublePair a = Pair.of(1, 1.7d);
    Pair<Integer, Double> b = Pair.of(Integer.valueOf(1), Double.valueOf(1.7d));
    assertEquals(true, a.equals(b));
    assertEquals(true, b.equals(a));
  }

  public void testEquals_toObjectVersion_null() {
    Pair<Integer, Double> a = Pair.of(null, Double.valueOf(1.9d));
    IntDoublePair b = Pair.of(1, 1.7d);
    assertEquals(true, a.equals(a));
    assertEquals(false, a.equals(b));
    assertEquals(false, b.equals(a));
    assertEquals(true, b.equals(b));
  }

  public void testHashCode() {
    IntDoublePair a = Pair.of(1, 1.7d);
    Pair<Integer, Double> b = Pair.of(Integer.valueOf(1), Double.valueOf(1.7d));
    assertEquals(a.hashCode(), b.hashCode());
  }

  public void testHashCode_value() {
    IntDoublePair a = new IntDoublePair(1, 2.0);
    assertEquals(a.hashCode(), a.hashCode());
    assertEquals(Integer.valueOf(1).hashCode() ^ Double.valueOf(2.0).hashCode(), a.hashCode());
    // can't test for different hash codes as they might not be different
  }

}
