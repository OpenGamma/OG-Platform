/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.enginedebugger.node;

import com.opengamma.util.ArgumentChecker;

/**
 * Represents a property name spanning a value property in both a requirement and spec in a map so they can be seen side-by-side
 */
public class SpanningValuePropertyEntryNode extends AbstractTreeTableLeafNode {
  private static final Object NAME = null;
  private final String _commonConstraintName;
  private final String _requirementConstraint;
  private final boolean _requirementOptional;
  private final String _specificationConstraing;
  private final boolean _specificationOptional;
  
  public SpanningValuePropertyEntryNode(String commonConstraintName,
                                    String requirementConstraint, boolean requirementOptional,
                                    String specificationConstraint, boolean specificationOptional) {
    ArgumentChecker.notNull(commonConstraintName, "commonConstraintName");
    _commonConstraintName = commonConstraintName;
    _requirementConstraint = requirementConstraint;
    _requirementOptional = requirementOptional;
    _specificationConstraing = specificationConstraint;
    _specificationOptional = specificationOptional;
  }
  
  public String getCommonConstraintName() {
    return _commonConstraintName;
  }
  
  public String getRequirementConstraint() {
    return _requirementConstraint;
  }

  public boolean isRequirementOptional() {
    return _requirementOptional;
  }

  public String getSpecificationConstraint() {
    return _specificationConstraing;
  }

  public boolean isSpecificationOptional() {
    return _specificationOptional;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _commonConstraintName.hashCode();
    result = prime * result + ((_requirementConstraint == null) ? 0 : _requirementConstraint.hashCode());
    result = prime * result + (_requirementOptional ? 1231 : 1237);
    result = prime * result + ((_specificationConstraing == null) ? 0 : _specificationConstraing.hashCode());
    result = prime * result + (_specificationOptional ? 1231 : 1237);
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
    if (!(obj instanceof SpanningValuePropertyEntryNode)) {
      return false;
    }
    SpanningValuePropertyEntryNode other = (SpanningValuePropertyEntryNode) obj;
    if (_commonConstraintName.equals(other._commonConstraintName)) {
      return true;
    }
    if (_requirementConstraint == null) {
      if (other._requirementConstraint != null) {
        return false;
      }
    } else if (!_requirementConstraint.equals(other._requirementConstraint)) {
      return false;
    }
    if (_requirementOptional != other._requirementOptional) {
      return false;
    }
    if (_specificationConstraing == null) {
      if (other._specificationConstraing != null) {
        return false;
      }
    } else if (!_specificationConstraing.equals(other._specificationConstraing)) {
      return false;
    }
    if (_specificationOptional != other._specificationOptional) {
      return false;
    }
    return true;
  }

  @Override
  public Object getColumn(int column) {
    switch (column) {
      case 0:
        return getCommonConstraintName();
      case 1:
        return getRequirementConstraint() + (isRequirementOptional() ? "?" : "");
      case 2:
        return getSpecificationConstraint() + (isSpecificationOptional() ? "?" : "");
    }
    return null;
  }
  
}
