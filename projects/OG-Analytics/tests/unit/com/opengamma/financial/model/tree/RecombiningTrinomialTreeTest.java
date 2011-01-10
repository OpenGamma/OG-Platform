/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.tree;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * 
 */
public class RecombiningTrinomialTreeTest {
  private static final Double[][] DATA = new Double[][] {new Double[] {1.}, new Double[] {2., 3., 4.}, new Double[] {5., 6., 7., 8., 9.}, new Double[] {10., 11., 12., 13., 14., 15., 16.}};
  private static final RecombiningTrinomialTree<Double> TREE = new RecombiningTrinomialTree<Double>(DATA);

  @Test
  public void test() {
    final RecombiningTrinomialTree<Double> other = new RecombiningTrinomialTree<Double>(DATA);
    assertEquals(TREE, other);
    for (int i = 0; i < DATA.length; i++) {
      assertEquals(DATA[i].length, TREE.getMaxNodesForStep(i));
    }
  }
}
