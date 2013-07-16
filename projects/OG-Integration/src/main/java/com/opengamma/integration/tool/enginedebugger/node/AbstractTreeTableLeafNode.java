/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.enginedebugger.node;

/**
 * Abstract implementation for all leaf nodes
 */
public abstract class AbstractTreeTableLeafNode implements TreeTableNode {

  @Override
  public Object getChildAt(int index) {
    return null;
  }

  @Override
  public int getChildCount() {
    return 0;
  }

  @Override
  public int getIndexOfChild(Object child) {
    return -1;
  }

}
