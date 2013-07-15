/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.enginedebugger.node;

import java.util.Map;
import java.util.Set;

import com.opengamma.engine.depgraph.ResolutionFailure;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Node representing a failed function (more than one unsatisfied value requirements)
 */
public class FailedFunctionNode extends AbstractFailureWithRequirementsNode {

  private static final String NAME = "FailedFunction: 1+ unsatisfied ValueReqs";
  private UnsatisfiedResolutionFailuresNode _unsatisfiedFailures;

  public FailedFunctionNode(Object parent, ValueRequirement valueRequirement, String function, ValueSpecification desiredOutput,
      Map<ValueSpecification, ValueRequirement> satisfied, Set<ResolutionFailure> unsatisfied) {
    super(parent, valueRequirement, function, desiredOutput, satisfied, "Satisfied");
    _unsatisfiedFailures = new UnsatisfiedResolutionFailuresNode(_parent, unsatisfied);
  }

  @Override
  public Object getChildAt(int index) {
    switch (index) {
      case 4:
        return _unsatisfiedFailures;
      default:
        return super.getChildAt(index);
    }
  }

  @Override
  public int getIndexOfChild(Object child) {
    if (child.equals(_unsatisfiedFailures)) {
      return 4;
    } else {
      return super.getIndexOfChild(child);
    }
  }

  @Override
  public int getChildCount() {
    return 5;
  }

  @Override
  public Object getColumn(int column) {
    if (column == 0) {
      return NAME;
    } else if (column == 1) {
      return _functionEntry.getFunctionName();
    }
    return null;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((_unsatisfiedFailures == null) ? 0 : _unsatisfiedFailures.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (!(obj instanceof FailedFunctionNode)) {
      return false;
    }
    FailedFunctionNode other = (FailedFunctionNode) obj;
    if (_unsatisfiedFailures == null) {
      if (other._unsatisfiedFailures != null) {
        return false;
      }
    } else if (!_unsatisfiedFailures.equals(other._unsatisfiedFailures)) {
      return false;
    }
    return true;
  }

}
