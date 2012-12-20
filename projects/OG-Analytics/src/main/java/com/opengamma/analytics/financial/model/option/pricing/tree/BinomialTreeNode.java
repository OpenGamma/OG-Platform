/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

/**
 * Represents a Binomial tree node - two nodes follow this
 * @param <T> The type of the value held
 */
public class BinomialTreeNode<T> implements TreeNode<T> {

  private T _value;
  private double _prob;

  public BinomialTreeNode(final T value, final double upProbability) {
    _value = value;
    _prob = upProbability;
  }

  public double getUpProbability() {
    return _prob;
  }

  @Override
  public T getValue() {
    return _value;
  }

}
