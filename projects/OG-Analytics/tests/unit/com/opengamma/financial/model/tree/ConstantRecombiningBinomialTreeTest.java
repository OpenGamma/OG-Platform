/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.tree;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * 
 */
public class ConstantRecombiningBinomialTreeTest {

  @Test
  public void test() {
    final double x = 3;
    final double y = 5;
    final RecombiningBinomialTree<Double> tree = new ConstantRecombiningBinomialTree<Double>(x);
    RecombiningBinomialTree<Double> other = new ConstantRecombiningBinomialTree<Double>(x);
    assertEquals(tree, other);
    assertEquals(tree.hashCode(), other.hashCode());
    other = new ConstantRecombiningBinomialTree<Double>(y);
    assertFalse(tree.equals(other));
    assertArrayEquals(tree.getTree(), new Double[][] {new Double[] {x}});
    for (int i = 0; i < 100; i++) {
      for (int j = 0; j < i + 1; j++) {
        assertEquals(tree.getNode(i, j), x, 0);
      }
      try {
        tree.getNode(i, i + 100);
        fail();
      } catch (final IllegalArgumentException e) {
      }
    }
  }
}
