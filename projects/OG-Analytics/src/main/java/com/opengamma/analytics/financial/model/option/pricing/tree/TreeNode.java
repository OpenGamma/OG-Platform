/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

/**
 * /**
 * A Node that holds values of type T
 * @param <T> value type
 */
public interface TreeNode<T> {

  T getValue();

}
