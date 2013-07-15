/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.enginedebugger.node;

import com.opengamma.id.ExternalId;

/**
 * Node representing an ExternalId
 */
public class ExternalIdNode extends AbstractTreeTableLeafNode {

  private static final String NAME = "ExternalId";
  private ExternalId _externalId;
  private Object _parent;

  public ExternalIdNode(Object parent, ExternalId externalId) {
    _parent = parent;
    _externalId = externalId;
  }

  @Override
  public Object getColumn(int column) {
    switch (column) {
      case 0:
        return NAME;
      case 1:
        return _externalId.toString();
    }
    return null;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_externalId == null) ? 0 : _externalId.hashCode());
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
    if (!(obj instanceof ExternalIdNode)) {
      return false;
    }
    ExternalIdNode other = (ExternalIdNode) obj;
    if (_externalId == null) {
      if (other._externalId != null) {
        return false;
      }
    } else if (!_externalId.equals(other._externalId)) {
      return false;
    }
    return true;
  }

}
