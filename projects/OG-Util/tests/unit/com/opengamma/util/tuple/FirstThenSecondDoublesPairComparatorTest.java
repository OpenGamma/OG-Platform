/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.tuple;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Test FirstThenSecondDoublesPairComparator.
 */
public class FirstThenSecondDoublesPairComparatorTest {

  @Test
  public void testSingleton() {
    DoublesPair a = Pair.of(1.0, 1.0);
    DoublesPair b = Pair.of(1.0, 2.0);
    assertTrue(FirstThenSecondDoublesPairComparator.INSTANCE.compare(a, a) == 0);
    assertTrue(FirstThenSecondDoublesPairComparator.INSTANCE.compare(a, b) < 0);
    
    assertTrue(FirstThenSecondDoublesPairComparator.INSTANCE.compare(b, a) > 0);
    assertTrue(FirstThenSecondDoublesPairComparator.INSTANCE.compare(b, b) == 0);
  }

  @Test
  public void testCompare() {
    final DoublesPair first = Pair.of(1.0, 3.0);
    final DoublesPair second = Pair.of(1.0, 5.0);
    final DoublesPair third = Pair.of(1.0, 6.0);
    final DoublesPair fourth = Pair.of(2.0, 1.0);
    
    final FirstThenSecondDoublesPairComparator test = new FirstThenSecondDoublesPairComparator();
    
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
