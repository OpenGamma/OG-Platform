/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.tree;

import com.opengamma.analytics.math.function.Function1D;

/**
 * 
 * @param <T>
 */
public class RecombiningBinomialTree<T> extends RecombiningTree<T> {
  /** Number of nodes at each level */
  public static final Function1D<Integer, Integer> NODES = new Function1D<Integer, Integer>() {

    @Override
    public Integer evaluate(final Integer i) {
      return i + 1;
    }
  };

  public RecombiningBinomialTree(final T[][] data) {
    super(data);
  }

  @Override
  protected int getMaxNodesForStep(final int step) {
    return NODES.evaluate(step);
  }

}
