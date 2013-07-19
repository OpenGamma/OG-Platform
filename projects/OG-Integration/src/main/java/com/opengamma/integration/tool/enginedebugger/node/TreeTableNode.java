/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.enginedebugger.node;

public interface TreeTableNode {

  Object getChildAt(int index);

  int getChildCount();

  int getIndexOfChild(Object child);

  Object getColumn(int column);

}
