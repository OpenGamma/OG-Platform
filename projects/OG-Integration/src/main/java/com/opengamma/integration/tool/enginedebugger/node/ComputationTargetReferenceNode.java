/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.enginedebugger.node;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetRequirement;

/**
 * Node representing a ComputationTargetReference
 */
public class ComputationTargetReferenceNode implements TreeTableNode {

  private static final Object NULL_NAME = "Null ComputationTargetReference";
  private static final Object REQUIREMENT_NAME = "CompuatationTargetRequirement";
  private static final Object SPECIFICATION_NAME = "CompuatationTargetSpecification";

  private ComputationTargetReference _targetReference;
  @SuppressWarnings("unused")
  private Object _parent;

  public ComputationTargetReferenceNode(Object parent, ComputationTargetReference targetReference) {
    _parent = parent;
    _targetReference = targetReference;
  }

  @Override
  public Object getChildAt(int index) {
    if (_targetReference instanceof ComputationTargetRequirement) {
      ComputationTargetRequirement requirement = _targetReference.getRequirement();
      switch (index) {
        case 0:
          return new ComputationTargetTypeNode(this, requirement.getType());
        case 1:
          return new ExternalIdBundleNode(this, requirement.getIdentifiers());
        default:
          return null;
      }
    } else {
      ComputationTargetSpecification specification = _targetReference.getSpecification();
      switch (index) {
        case 0:
          return new ComputationTargetTypeNode(this, specification.getType());
        case 1:
          return new UniqueIdNode(this, specification.getUniqueId());
        default:
          return null;
      }
    }

  }

  @Override
  public int getChildCount() {
    return 2;
  }

  @Override
  public int getIndexOfChild(Object child) {
    if (child instanceof ComputationTargetTypeNode) {
      if (new ComputationTargetTypeNode(this, _targetReference.getType()).equals(child)) {
        return 0;
      }
    } else if (child instanceof ExternalIdBundleNode && _targetReference instanceof ComputationTargetRequirement) {
      ComputationTargetRequirement targetRequirement = (ComputationTargetRequirement) _targetReference;
      if (new ExternalIdBundleNode(this, targetRequirement.getIdentifiers()).equals(child)) {
        return 1;
      }
    } else if (child instanceof UniqueIdNode && _targetReference instanceof ComputationTargetSpecification) {
      ComputationTargetSpecification targetSpec = (ComputationTargetSpecification) _targetReference;
      if (new UniqueIdNode(this, targetSpec.getUniqueId()).equals(child)) {
        return 1;
      }
    }
    return -1;
  }

  @Override
  public Object getColumn(int column) {
    switch (column) {
      case 0:
        if (_targetReference == null) {
          return NULL_NAME;
        } else if (_targetReference instanceof ComputationTargetRequirement) {
          return REQUIREMENT_NAME;
        } else if (_targetReference instanceof ComputationTargetSpecification) {
          return SPECIFICATION_NAME;
        }
      case 1:
        return _targetReference.toString();
    }
    return null;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_targetReference == null) ? 0 : _targetReference.hashCode());
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
    if (!(obj instanceof ComputationTargetReferenceNode)) {
      return false;
    }
    ComputationTargetReferenceNode other = (ComputationTargetReferenceNode) obj;
    if (_targetReference == null) {
      if (other._targetReference != null) {
        return false;
      }
    } else if (!_targetReference.equals(other._targetReference)) {
      return false;
    }
    return true;
  }
}
