/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.tree;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * 
 */
public class RecombiningBinomialTreeTest {
  private static final Double[][] DATA = new Double[][] {new Double[] {1.}, new Double[] {2., 3.}, new Double[] {4., 5., 6.}, new Double[] {7., 8., 9., 10.}};
  private static final RecombiningBinomialTree<Double> TREE = new RecombiningBinomialTree<Double>(DATA);

  @Test
  public void test() {
    final RecombiningBinomialTree<Double> other = new RecombiningBinomialTree<Double>(DATA);
    assertEquals(TREE, other);
    for (int i = 0; i < DATA.length; i++) {
      assertEquals(DATA[i].length, TREE.getMaxNodesForStep(i));
    }
  }
}
