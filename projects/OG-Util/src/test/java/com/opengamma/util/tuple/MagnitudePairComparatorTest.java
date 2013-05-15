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
 * Test MagnitudePairComparator.
 */
@Test(groups = TestGroup.UNIT)
public class MagnitudePairComparatorTest {

  public void testCompare_differentQuadrants() {
    final Pair<Double, Double> first = Pair.of(2.0, 3.0);
    final Pair<Double, Double> second = Pair.of(3.0, 2.0);
    final Pair<Double, Double> third = Pair.of(4.0, 4.0);
    
    final Comparator<Pair<Double, Double>> test = MagnitudePairComparator.INSTANCE;
    
    assertTrue(test.compare(first, first) == 0);
    assertTrue(test.compare(first, second) < 0);
    assertTrue(test.compare(first, third) < 0);
    
    assertTrue(test.compare(second, first) > 0);
    assertTrue(test.compare(second, second) == 0);
    assertTrue(test.compare(second, third) < 0);
    
    assertTrue(test.compare(third, first) > 0);
    assertTrue(test.compare(third, second) > 0);
    assertTrue(test.compare(third, third) == 0);
  }

}
