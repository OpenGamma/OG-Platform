/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.tuple;

import static org.junit.Assert.assertTrue;

import java.util.Comparator;

import org.junit.Test;

/**
 * Test QuadrantDoublesPairComparator.
 */
public class QuadrantDoublesPairComparatorTest {

  @Test
  public void testCompare_differentQuadrants() {
    final DoublesPair first = Pair.of(0.0, 0.0);
    final DoublesPair second = Pair.of(-0.1, 0.0);
    final DoublesPair third = Pair.of(-0.1, -0.1);
    final DoublesPair fourth = Pair.of(0.0, -0.1);
    
    final Comparator<DoublesPair> test = QuadrantDoublesPairComparator.INSTANCE;
    
    assertTrue(test.compare(first, first) == 0);
    assertTrue(test.compare(first, second) < 0);
    assertTrue(test.compare(first, third) < 0);
    assertTrue(test.compare(first, fourth) < 0);
    
    assertTrue(test.compare(second, first) > 0);
    assertTrue(test.compare(second, second) == 0);
    assertTrue(test.compare(second, third) < 0);
    assertTrue(test.compare(second, fourth) < 0);
    
    assertTrue(test.compare(third, first) > 0);
    assertTrue(test.compare(third, second) > 0);
    assertTrue(test.compare(third, third) == 0);
    assertTrue(test.compare(third, fourth) < 0);
    
    assertTrue(test.compare(fourth, first) > 0);
    assertTrue(test.compare(fourth, second) > 0);
    assertTrue(test.compare(fourth, third) > 0);
    assertTrue(test.compare(fourth, fourth) == 0);
  }

  @Test
  public void testCompare_sameQuadrant() {
    final DoublesPair first = Pair.of(0.0, 0.0);
    final DoublesPair second = Pair.of(1.0, 0.0);
    
    final Comparator<DoublesPair> test = QuadrantDoublesPairComparator.INSTANCE;
    
    assertTrue(test.compare(first, first) == 0);
    assertTrue(test.compare(first, second) < 0);
    
    assertTrue(test.compare(second, first) > 0);
    assertTrue(test.compare(second, second) == 0);
  }

}
