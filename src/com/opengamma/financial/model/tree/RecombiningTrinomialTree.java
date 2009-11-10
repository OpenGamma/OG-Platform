/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.tree;

/**
 * 
 * @author emcleod
 * 
 */

public class RecombiningTrinomialTree<T> extends RecombiningTree<T> {

  public RecombiningTrinomialTree(final T[][] tree) {
    super(tree);
  }

  @Override
  protected int getMaxNodesForStep(final int step) {
    return 2 * step + 1;
  }

}
