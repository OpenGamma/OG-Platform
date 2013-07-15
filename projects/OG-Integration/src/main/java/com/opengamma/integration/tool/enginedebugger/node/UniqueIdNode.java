/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.enginedebugger.node;

import com.opengamma.id.UniqueId;

/**
 * Node representing a UniqueId
 */
public class UniqueIdNode extends AbstractTreeTableLeafNode {

  private static final String NAME = "UniqueId";
  private UniqueId _uniqueId;
  private Object _parent;

  public UniqueIdNode(Object parent, UniqueId uniqueId) {
    _parent = parent;
    _uniqueId = uniqueId;
  }

  @Override
  public Object getColumn(int column) {
    switch (column) {
      case 0:
        return NAME;
      case 1:
        return _uniqueId.toString();
    }
    return null;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_uniqueId == null) ? 0 : _uniqueId.hashCode());
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
    if (!(obj instanceof UniqueIdNode)) {
      return false;
    }
    UniqueIdNode other = (UniqueIdNode) obj;
    if (_uniqueId == null) {
      if (other._uniqueId != null) {
        return false;
      }
    } else if (!_uniqueId.equals(other._uniqueId)) {
      return false;
    }
    return true;
  }
}
