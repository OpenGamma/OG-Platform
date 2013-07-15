/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.enginedebugger.node;

import com.opengamma.engine.value.ValueRequirement;

/**
 * Node representing an unsatisfied value requirement
 */
public class UnsatisfiedNode extends ValueRequirementNode {

  public UnsatisfiedNode(Object parent, ValueRequirement valueRequirement) {
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
    if (!(obj instanceof UnsatisfiedNode)) {
      return false;
    }
    UnsatisfiedNode other = (UnsatisfiedNode) obj;
    if (_valueRequirement == null) {
      if (other._valueRequirement != null) {
        return false;
      }
    } else if (!_valueRequirement.equals(other._valueRequirement)) {
      return false;
    }
    return true;
  }
  
  // use superclass hashCode

}
