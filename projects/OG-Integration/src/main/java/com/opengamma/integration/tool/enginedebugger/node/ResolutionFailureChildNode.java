package com.opengamma.integration.tool.enginedebugger.node;

import com.opengamma.engine.depgraph.ResolutionFailureImpl;

public class ResolutionFailureChildNode {
  private Object[] _children;
  private ResolutionFailureImpl _parent;

  public ResolutionFailureChildNode(ResolutionFailureImpl parent, Object[] children) {
    _parent = parent;
    _children = children;
  }
}
