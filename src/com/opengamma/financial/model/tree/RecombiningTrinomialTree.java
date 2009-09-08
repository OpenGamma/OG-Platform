package com.opengamma.financial.model.tree;

/**
 * 
 * @author emcleod
 * 
 */

public class RecombiningTrinomialTree<T> extends RecombiningTree<T> {

  public RecombiningTrinomialTree(T[][] tree) {
    super(tree);
  }

  @Override
  protected int getMaxNodesForStep(int step) {
    return 2 * step + 1;
  }

}
