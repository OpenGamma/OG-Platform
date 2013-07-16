/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.enginedebugger.node;

import com.opengamma.engine.target.ComputationTargetType;

/**
 * Node for representing ComputationTargetType
 */
public class ComputationTargetTypeNode extends AbstractTreeTableLeafNode {

  private static final String NAME = "CompuatationTargetType";
  private ComputationTargetType _targetType;
  @SuppressWarnings("unused")
  private Object _parent;

  public ComputationTargetTypeNode(Object parent, ComputationTargetType targetType) {
    _parent = parent;
    _targetType = targetType;
  }

  @Override
  public Object getColumn(int column) {
    switch (column) {
      case 0:
        return NAME;
      case 1:
        return _targetType.getName();
    }
    return null;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_targetType == null) ? 0 : _targetType.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof ComputationTargetTypeNode)) {
      return false;
    }
    ComputationTargetTypeNode other = (ComputationTargetTypeNode) obj;
    if (_targetType == null) {
      if (other._targetType != null) {
        return false;
      }
    } else if (!_targetType.equals(other._targetType)) {
      return false;
    }
    return true;
  }

}
