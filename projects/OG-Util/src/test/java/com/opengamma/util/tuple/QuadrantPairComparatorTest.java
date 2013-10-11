/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.tuple;

import static org.testng.AssertJUnit.assertTrue;

import java.util.Comparator;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test QuadrantPairComparator.
 */
@Test(groups = TestGroup.UNIT)
public class QuadrantPairComparatorTest {

  public void testCompare_differentQuadrants() {
    final DoublesPair first = DoublesPair.of(0.0, 0.0);
    final DoublesPair second = DoublesPair.of(-0.1, 0.0);
    final DoublesPair third = DoublesPair.of(-0.1, -0.1);
    final DoublesPair fourth = DoublesPair.of(0.0, -0.1);
    
    final Comparator<Pair<Double, Double>> test = QuadrantPairComparator.INSTANCE;
    
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

  public void testCompare_sameQuadrant() {
    final Pair<Double, Double> first = DoublesPair.of(0.0, 0.0);
    final Pair<Double, Double> second = DoublesPair.of(1.0, 0.0);
    
    final Comparator<Pair<Double, Double>> test = QuadrantPairComparator.INSTANCE;
    
    assertTrue(test.compare(first, first) == 0);
    assertTrue(test.compare(first, second) < 0);
    
    assertTrue(test.compare(second, first) > 0);
    assertTrue(test.compare(second, second) == 0);
  }

}
