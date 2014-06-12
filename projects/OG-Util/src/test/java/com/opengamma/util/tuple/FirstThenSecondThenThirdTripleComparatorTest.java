/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.tuple;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests a triple comparator.
 */
@Test(groups = TestGroup.UNIT)
public class FirstThenSecondThenThirdTripleComparatorTest {

  /**
   * Tests the case where the input data are already sorted.
   */
  @Test
  public void testAlreadySorted() {
    final Double[] xs = new Double[] {1., 2., 3., };
    final Double[] ys = new Double[] {11., 13., 15. };
    final Double[] zs = new Double[] {21., 22., 23., 26. };
    final List<Triple<Double, Double, Double>> expected = new ArrayList<>();
    SortedSet<Triple<Double, Double, Double>> actual = new TreeSet<>(FirstThenSecondThenThirdTripleComparator.INSTANCE);
    for (final Double x : xs) {
      for (final Double y : ys) {
        for (final Double z : zs) {
          final Triple<Double, Double, Double> triple = Triple.of(x, y, z);
          expected.add(triple);
          actual.add(triple);
        }
      }
    }
    final int total = xs.length * ys.length * zs.length;
    assertArrayEquals(expected.toArray(new Triple[total]), actual.toArray(new Triple[total]));
    actual = new TreeSet<>(FirstThenSecondThenThirdTripleComparator.INSTANCE_DOUBLES);
    for (final Double x : xs) {
      for (final Double y : ys) {
        for (final Double z : zs) {
          final Triple<Double, Double, Double> triple = Triple.of(x, y, z);
          actual.add(triple);
        }
      }
    }
    assertArrayEquals(expected.toArray(new Triple[total]), actual.toArray(new Triple[total]));
  }

  /**
   * Tests the comparator.
   */
  @Test
  public void test() {
    final Double[] xs = new Double[] {3., 2., 1., };
    final Double[] ys = new Double[] {13., 11., 15. };
    final Double[] zs = new Double[] {23., 22., 21., 26. };
    final Double[] sortedXs = new Double[] {1., 2., 3., };
    final Double[] sortedYs = new Double[] {11., 13., 15. };
    final Double[] sortedZs = new Double[] {21., 22., 23., 26. };
    final List<Triple<Double, Double, Double>> expected = new ArrayList<>();
    SortedSet<Triple<Double, Double, Double>> actual = new TreeSet<>(FirstThenSecondThenThirdTripleComparator.INSTANCE);
    for (int i = 0; i < xs.length; i++) {
      for (int j = 0; j < ys.length; j++) {
        for (int k = 0; k < zs.length; k++) {
          actual.add(Triple.of(xs[i], ys[j], zs[k]));
          expected.add(Triple.of(sortedXs[i], sortedYs[j], sortedZs[k]));
        }
      }
    }
    final int total = xs.length * ys.length * zs.length;
    assertArrayEquals(expected.toArray(new Triple[total]), actual.toArray(new Triple[total]));
    actual = new TreeSet<>(FirstThenSecondThenThirdTripleComparator.INSTANCE_DOUBLES);
    for (final Double x : xs) {
      for (final Double y : ys) {
        for (final Double z : zs) {
          actual.add(Triple.of(x, y, z));
        }
      }
    }
    assertArrayEquals(expected.toArray(new Triple[total]), actual.toArray(new Triple[total]));
  }

  /**
   * Checks that every element of two arrays of triples is equal.
   * @param expected The expected array
   * @param actual The actual array
   */
  @SuppressWarnings("rawtypes")
  private static void assertArrayEquals(final Triple[] expected, final Triple[] actual) {
    assertEquals(expected.length, actual.length);
    for (int i = 0; i < expected.length; i++) {
      assertEquals("Element " + i + ": ", expected[i], actual[i]);
    }
  }
}
