/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.tree;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.tree.RecombiningBinomialTree;

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
