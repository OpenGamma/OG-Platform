/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.enginedebugger.node;

import com.opengamma.engine.value.ValueRequirement;

/**
 * Node representing no function found during graph building
 */
public class NoFunctionNode extends ValueRequirementNode {

  public NoFunctionNode(Object parent, ValueRequirement valueRequirement) {
    super(parent, valueRequirement);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof NoFunctionNode)) {
      return false;
    }
    NoFunctionNode other = (NoFunctionNode) obj;
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
