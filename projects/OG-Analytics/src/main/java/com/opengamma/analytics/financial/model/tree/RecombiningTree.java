/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.tree;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.util.ArgumentChecker;

/**
 * 
 * @param <T>
 */
public abstract class RecombiningTree<T> implements Lattice<T> {
  private final T[][] _tree;

  public RecombiningTree(final T[][] data) {
    Validate.notNull(data, "data");
    ArgumentChecker.notEmpty(data, "data");
    _tree = data;
  }

  protected abstract int getMaxNodesForStep(int step);

  @Override
  public T getNode(final int step, final int node) {
    if (step < 0) {
      throw new IllegalArgumentException("Step number cannot be negative");
    }
    if (node < 0) {
      throw new IllegalArgumentException("Node number cannot be negative");
    }
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
  public T[][] getNodes() {
    return _tree;
  }

  public int getDepth() {
    return _tree.length;
  }

  public int getNumberOfTerminatingNodes() {
    return _tree[_tree.length - 1].length;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    final int steps = _tree.length;
    result = prime * result + steps;
    int count = 0;
    for (int i = 0; i < steps; i++) {
      final int nodes = _tree[i].length;
      result = prime * result + nodes;
      for (int j = 0; j < nodes; j++) {
        result = prime * result + _tree[i][j].hashCode();
        if (count == 10) {
          break;
        }
        count++;
      }
      if (count == 10) {
        break;
      }
    }
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final RecombiningTree<?> other = (RecombiningTree<?>) obj;
    if (ObjectUtils.equals(_tree, other._tree)) {
      return true;
    }
    final int length = _tree.length;
    if (length != other._tree.length) {
      return false;
    }
    for (int i = 0; i < length; i++) {
      final int width = _tree[i].length;
      if (width != other._tree[i].length) {
        return false;
      }
      for (int j = 0; j < width; j++) {
        if (!ObjectUtils.equals(_tree[i][j], other._tree[i][j])) {
          return false;
        }
      }
    }
    return true;
  }

}
