/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.tree;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ConstantRecombiningBinomialTreeTest {

  @Test
  public void test() {
    final double x = 3;
    final double y = 5;
    final RecombiningBinomialTree<Double> tree = new ConstantRecombiningBinomialTree<>(x);
    RecombiningBinomialTree<Double> other = new ConstantRecombiningBinomialTree<>(x);
    assertEquals(tree, other);
    assertEquals(tree.hashCode(), other.hashCode());
    other = new ConstantRecombiningBinomialTree<>(y);
    assertFalse(tree.equals(other));
    assertArrayEquals(tree.getNodes(), new Double[][] {new Double[] {x}});
    for (int i = 0; i < 100; i++) {
      for (int j = 0; j < i + 1; j++) {
        assertEquals(tree.getNode(i, j), x, 0);
      }
      try {
        tree.getNode(i, i + 100);
        Assert.fail();
      } catch (final IllegalArgumentException e) {
      }
    }
  }
}
