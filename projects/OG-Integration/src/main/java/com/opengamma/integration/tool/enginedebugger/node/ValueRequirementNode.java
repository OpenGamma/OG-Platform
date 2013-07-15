/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.enginedebugger.node;

import com.opengamma.engine.value.ValueRequirement;

/** 
 * Node representing a ValueRequirement
 */
public class ValueRequirementNode implements TreeTableNode {

  private static final String NAME = "ValueRequirement";
  /**
   * Value requirement itself
   */
  protected ValueRequirement _valueRequirement;
  @SuppressWarnings("unused")
  private Object _parent;

  public ValueRequirementNode(Object parent, ValueRequirement valueRequirement) {
    _parent = parent;
    _valueRequirement = valueRequirement;
  }

  @Override
  public Object getChildAt(int index) {
    if (_valueRequirement != null) {
      switch (index) {
        case 0:
          return new ComputationTargetReferenceNode(this, _valueRequirement.getTargetReference());
        case 1:
          return new ValuePropertiesNode(this, _valueRequirement.getConstraints());
      }
    }
    return null;
  }

  @Override
  public int getChildCount() {
    if (_valueRequirement != null) {
      return 2;
    } else {
      return 0;
    }
  }

  @Override
  public int getIndexOfChild(Object child) {
    if (_valueRequirement != null) {
      if (_valueRequirement.getTargetReference() != null) {
        if (child instanceof ComputationTargetReferenceNode) {
          if (child.equals(new ComputationTargetReferenceNode(this, _valueRequirement.getTargetReference()))) {
            return 0;
          }
        } else if (child instanceof ValuePropertiesNode) {
          if (child.equals(new ValuePropertiesNode(this, _valueRequirement.getConstraints()))) {
            return 1;
          }
        }

      }
    }
    return -1;
  }

  @Override
  public Object getColumn(int column) {
    if (column == 0) {
      return NAME;
    } else if (column == 1) {
      return _valueRequirement.getValueName();
    }
    return null;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_valueRequirement == null) ? 0 : _valueRequirement.hashCode());
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
    if (!(obj instanceof ValueRequirementNode)) {
      return false;
    }
    ValueRequirementNode other = (ValueRequirementNode) obj;
    if (_valueRequirement == null) {
      if (other._valueRequirement != null) {
        return false;
      }
    } else if (!_valueRequirement.equals(other._valueRequirement)) {
      return false;
    }
    return true;
  }

}
