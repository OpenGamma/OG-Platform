/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.tree;

/**
 * /**
 * A Node that holds values of type T
 * @param <T> value type
 */
public interface TreeNode<T> {

  T getValue();

}
