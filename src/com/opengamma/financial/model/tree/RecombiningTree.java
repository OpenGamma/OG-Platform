/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.tree;

/**
 * 
 * @param <T>
 */
public abstract class RecombiningTree<T> implements Lattice<T> {
  private final T[][] _tree;

  public RecombiningTree(final T[][] data) {
    _tree = data;
  }

  protected abstract int getMaxNodesForStep(int step);

  @Override
  public T getNode(final int step, final int node) {
    if (step > _tree.length) {
      throw new IllegalArgumentException("Step number " + step + " is greater than maximum in this tree (max =  " + _tree.length + ")");
    }
    final int max = getMaxNodesForStep(step);
    if (node > max) {
      throw new IllegalArgumentException("Node number " + node + " is greater than the number of nodes at this step number (max = " + max + ")");
    }
    return _tree[step][node];
  }

  @Override
  public T[][] getTree() {
    return _tree;
  }
}
