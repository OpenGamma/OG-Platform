/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.tuple;

import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test FirstThenSecondPairComparator.
 */
@Test(groups = TestGroup.UNIT)
public class FirstThenSecondPairComparatorTest {

  public void testSingleton() {
    Pair<Integer, String> a = ObjectsPair.of(1, "A");
    Pair<Integer, String> b = ObjectsPair.of(1, "B");
    assertTrue(FirstThenSecondPairComparator.INSTANCE.compare(a, a) == 0);
    assertTrue(FirstThenSecondPairComparator.INSTANCE.compare(a, b) < 0);
    
    assertTrue(FirstThenSecondPairComparator.INSTANCE.compare(b, a) > 0);
    assertTrue(FirstThenSecondPairComparator.INSTANCE.compare(b, b) == 0);
  }

  public void testSingleton_doubles() {
    Pair<Double, Double> a = DoublesPair.of(1.0, 1.0);
    Pair<Double, Double> b = DoublesPair.of(1.0, 2.0);
    assertTrue(FirstThenSecondPairComparator.INSTANCE_DOUBLES.compare(a, a) == 0);
    assertTrue(FirstThenSecondPairComparator.INSTANCE_DOUBLES.compare(a, b) < 0);
    
    assertTrue(FirstThenSecondPairComparator.INSTANCE_DOUBLES.compare(b, a) > 0);
    assertTrue(FirstThenSecondPairComparator.INSTANCE_DOUBLES.compare(b, b) == 0);
  }

  public void testCompare() {
    final Pair<Double, Double> first = DoublesPair.of(1.0, 3.0);
    final Pair<Double, Double> second = DoublesPair.of(1.0, 5.0);
    final Pair<Double, Double> third = DoublesPair.of(1.0, 6.0);
    final Pair<Double, Double> fourth = DoublesPair.of(2.0, 1.0);
    
    final FirstThenSecondPairComparator<Double, Double> test = new FirstThenSecondPairComparator<Double, Double>();
    
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

}
