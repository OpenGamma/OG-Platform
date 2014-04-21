/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.tree;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class RecombiningTrinomialTreeTest {
  private static final Double[][] DATA = new Double[][] {new Double[] {1.}, new Double[] {2., 3., 4.}, new Double[] {5., 6., 7., 8., 9.}, new Double[] {10., 11., 12., 13., 14., 15., 16.}};
  private static final RecombiningTrinomialTree<Double> TREE = new RecombiningTrinomialTree<>(DATA);

  @Test
  public void test() {
    final RecombiningTrinomialTree<Double> other = new RecombiningTrinomialTree<>(DATA);
    assertEquals(TREE, other);
    for (int i = 0; i < DATA.length; i++) {
      assertEquals(DATA[i].length, TREE.getMaxNodesForStep(i));
    }
  }
}
