/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.enginedebugger.node;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * @author jim
 * Represents a generic failure node to be subclassed by specific failure types
 */
public abstract class AbstractFailureNode implements TreeTableNode {

  private static final String NULL_DESIRED_OUTPUT_VALUE_SPEC = "null desired output value spec";
  /**
   * the parent node, unused.
   */
  protected Object _parent;
  /**
   * the child value requirement node
   */
  protected ValueRequirementNode _valueRequirementNode;
  /**
   * the child function entry node
   */
  protected FunctionEntryNode _functionEntry;
  /**
   * the child desired output value spec
   */
  protected ValueSpecificationNode _desiredOutputNode;

  public AbstractFailureNode(Object parent, ValueRequirement valueRequirement, String function, ValueSpecification desiredOutput) {
    _parent = parent;
    _valueRequirementNode = new ValueRequirementNode(this, valueRequirement);
    _functionEntry = new FunctionEntryNode(this, function);
    _desiredOutputNode = new ValueSpecificationNode(this, desiredOutput);
  }

  @Override
  public Object getChildAt(int index) {
    switch (index) {
      case 0:
        return _valueRequirementNode;
      case 1:
        return _functionEntry;
      case 2:
        return _desiredOutputNode == null ? NULL_DESIRED_OUTPUT_VALUE_SPEC : _desiredOutputNode;
    }
    return null;
  }

  @Override
  public int getIndexOfChild(Object child) {
    if (child.equals(_valueRequirementNode)) {
      return 0;
    } else if (child.equals(_functionEntry)) {
      return 1;
    } else if (child.equals(_desiredOutputNode) || child.equals(NULL_DESIRED_OUTPUT_VALUE_SPEC)) {
      return 2;
    }
    return -1;
  }

  @Override
  public int getChildCount() {
    return 3;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_desiredOutputNode == null) ? 0 : _desiredOutputNode.hashCode());
    result = prime * result + ((_functionEntry == null) ? 0 : _functionEntry.hashCode());
    result = prime * result + ((_valueRequirementNode == null) ? 0 : _valueRequirementNode.hashCode());
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
    if (!(obj instanceof AbstractFailureNode)) {
      return false;
    }
    AbstractFailureNode other = (AbstractFailureNode) obj;
    if (_desiredOutputNode == null) {
      if (other._desiredOutputNode != null) {
        return false;
      }
    } else if (!_desiredOutputNode.equals(other._desiredOutputNode)) {
      return false;
    }
    if (_functionEntry == null) {
      if (other._functionEntry != null) {
        return false;
      }
    } else if (!_functionEntry.equals(other._functionEntry)) {
      return false;
    }
    if (_valueRequirementNode == null) {
      if (other._valueRequirementNode != null) {
        return false;
      }
    } else if (!_valueRequirementNode.equals(other._valueRequirementNode)) {
      return false;
    }
    return true;
  }

}
