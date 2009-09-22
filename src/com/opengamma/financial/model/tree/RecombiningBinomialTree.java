package com.opengamma.financial.model.tree;

import com.opengamma.math.function.Function1D;

/**
 * 
 * @author emcleod
 * 
 */

public class RecombiningBinomialTree<T> extends RecombiningTree<T> {
  public static final Function1D<Integer, Integer> NODES = new Function1D<Integer, Integer>() {

    @Override
    public Integer evaluate(Integer i) {
      return i + 1;
    }

  };

  public RecombiningBinomialTree(T[][] data) {
    super(data);
  }

  @Override
  protected int getMaxNodesForStep(int step) {
    return NODES.evaluate(step);
  }
}
