/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.enginedebugger.node;

import java.util.ArrayList;
import java.util.List;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;

/**
 * Node containing an ExternalIdBundle
 */
public class ExternalIdBundleNode implements TreeTableNode {
  private static final String NAME = "ExternalIdBundle";
  private ExternalIdBundle _externalIdBundle;
  private List<ExternalId> _externalIds;
  @SuppressWarnings("unused")
  private Object _parent;

  public ExternalIdBundleNode(Object parent, ExternalIdBundle externalIdBundle) {
    _parent = parent;
    _externalIdBundle = externalIdBundle;
    _externalIds = new ArrayList<>(externalIdBundle.getExternalIds());
  }

  @Override
  public Object getChildAt(int index) {
    return _externalIds.get(index);
  }

  @Override
  public int getChildCount() {
    return _externalIds.size();
  }

  @Override
  public int getIndexOfChild(Object child) {
    return 0;
  }

  @Override
  public Object getColumn(int column) {
    if (column == 0) {
      return NAME;
    }
    return null;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_externalIdBundle == null) ? 0 : _externalIdBundle.hashCode());
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
    if (!(obj instanceof ExternalIdBundleNode)) {
      return false;
    }
    ExternalIdBundleNode other = (ExternalIdBundleNode) obj;
    if (_externalIdBundle == null) {
      if (other._externalIdBundle != null) {
        return false;
      }
    } else if (!_externalIdBundle.equals(other._externalIdBundle)) {
      return false;
    }
    return true;
  }
}
