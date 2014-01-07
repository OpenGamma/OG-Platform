/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.tree;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class RecombiningTreeTest {
  private static final Double[][] DATA1 = new Double[][] {new Double[] {1.}, new Double[] {2., 3.}, new Double[] {4., 5., 6.}, new Double[] {7., 8., 9., 10., 11.}};
  private static final Double[][] DATA2 = new Double[][] {new Double[] {1.5}, new Double[] {2.5, 3.5}, new Double[] {4.5, 5.5, 6.5}};
  private static final RecombiningTree<Double> TREE = new DummyTree(DATA1);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    new DummyTree(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyData() {
    new DummyTree(new Double[0][0]);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeStep() {
    TREE.getNode(-2, 0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeNode() {
    TREE.getNode(0, -34);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongStep() {
    TREE.getNode(6, 0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongNode() {
    TREE.getNode(2, 10);
  }

  @Test
  public void testGetters() {
    assertArrayEquals(TREE.getNodes(), DATA1);
    assertEquals(TREE.getDepth(), 4);
    assertEquals(TREE.getNumberOfTerminatingNodes(), 5);
    for (int i = 0; i < DATA1.length; i++) {
      for (int j = 0; j < DATA1[i].length; j++) {
        assertEquals(DATA1[i][j], TREE.getNode(i, j), 0);
      }
    }
  }

  @Test
  public void testHashCodeAndEquals() {
    RecombiningTree<Double> other = new DummyTree(DATA1);
    assertEquals(TREE, other);
    assertEquals(TREE.hashCode(), other.hashCode());
    final Double[][] copy = new Double[4][];
    for (int i = 0; i < 4; i++) {
      copy[i] = Arrays.copyOf(DATA1[i], DATA1[i].length);
    }
    other = new DummyTree(copy);
    assertEquals(TREE, other);
    assertEquals(TREE.hashCode(), other.hashCode());
    other = new DummyTree(DATA2);
    assertFalse(TREE.equals(other));
  }

  private static class DummyTree extends RecombiningTree<Double> {

    public DummyTree(final Double[][] data) {
      super(data);
    }

    @SuppressWarnings("synthetic-access")
    @Override
    protected int getMaxNodesForStep(final int step) {
      return DATA1[step].length;
    }

    

  }
}
