/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.enginedebugger;

import java.util.ArrayList;
import java.util.List;

import com.opengamma.engine.depgraph.ResolutionFailureImpl;
import com.opengamma.integration.tool.enginedebugger.node.TreeTableNode;

public class ResolutionFailureTreeTableNode  implements TreeTableNode {

  private static final String NAME = "Failure";
  private ResolutionFailureImpl _node;
  private List<Object> _children;

  public ResolutionFailureTreeTableNode(ResolutionFailureImpl node) {
    _node = node;
    _children = new ArrayList<Object>(node.accept(new ResolutionFailureChildNodeCreatingVisitor(node)));
  }

  @Override
  public Object getChildAt(int index) {
    return _children.get(index);
  }

  @Override
  public int getChildCount() {
    return _children.size();
  }

  @Override
  public int getIndexOfChild(Object child) {
    return _children.indexOf(child);
  }

  @Override
  public Object getColumn(int column) {
    if (column == 0) {
      return NAME;
    }
    return null;
  }
}
