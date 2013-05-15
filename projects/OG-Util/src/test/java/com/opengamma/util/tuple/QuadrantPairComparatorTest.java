/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.tuple;

import static org.testng.AssertJUnit.assertTrue;

import java.util.Comparator;

import com.opengamma.util.tuple.Pair;
import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test QuadrantPairComparator.
 */
@Test(groups = TestGroup.UNIT)
public class QuadrantPairComparatorTest {

  public void testCompare_differentQuadrants() {
    final Pair<Double, Double> first = Pair.of(0.0, 0.0);
    final Pair<Double, Double> second = Pair.of(-0.1, 0.0);
    final Pair<Double, Double> third = Pair.of(-0.1, -0.1);
    final Pair<Double, Double> fourth = Pair.of(0.0, -0.1);
    
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
    final Pair<Double, Double> first = Pair.of(0.0, 0.0);
    final Pair<Double, Double> second = Pair.of(1.0, 0.0);
    
    final Comparator<Pair<Double, Double>> test = QuadrantPairComparator.INSTANCE;
    
    assertTrue(test.compare(first, first) == 0);
    assertTrue(test.compare(first, second) < 0);
    
    assertTrue(test.compare(second, first) > 0);
    assertTrue(test.compare(second, second) == 0);
  }

}
