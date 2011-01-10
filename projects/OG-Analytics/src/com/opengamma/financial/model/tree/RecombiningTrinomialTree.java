/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.tree;

import com.opengamma.math.function.Function1D;

/**
 * 
 * @param <T>
 */
public class RecombiningTrinomialTree<T> extends RecombiningTree<T> {
  /**
   * 
   */
  public static final Function1D<Integer, Integer> NODES = new Function1D<Integer, Integer>() {

    @Override
    public Integer evaluate(final Integer i) {
      return 2 * i + 1;
    }

  };

  public RecombiningTrinomialTree(final T[][] data) {
    super(data);
  }

  @Override
  protected int getMaxNodesForStep(final int step) {
    return NODES.evaluate(step);
  }

}
