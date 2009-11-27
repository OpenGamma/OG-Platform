/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.tree;

/**
 * 
 * @author emcleod
 */
public class ConstantRecombiningBinomialTree<T> extends RecombiningBinomialTree<T> {
  private final T _value;

  public ConstantRecombiningBinomialTree(final T value) {
    super(null);
    _value = value;
  }

  @Override
  public T getNode(final int step, final int node) {
    return _value;
  }

}
