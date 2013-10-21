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
 * Test MagnitudeDoublesPairComparator.
 */
@Test(groups = TestGroup.UNIT)
public class MagnitudeDoublesPairComparatorTest {

  public void testCompare_differentQuadrants() {
    final DoublesPair first = DoublesPair.of(2.0, 3.0);
    final DoublesPair second = DoublesPair.of(3.0, 2.0);
    final DoublesPair third = DoublesPair.of(4.0, 4.0);
    
    final Comparator<DoublesPair> test = MagnitudeDoublesPairComparator.INSTANCE;
    
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
