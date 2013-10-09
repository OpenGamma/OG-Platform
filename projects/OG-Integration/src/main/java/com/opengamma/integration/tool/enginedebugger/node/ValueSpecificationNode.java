/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.enginedebugger.node;

import com.opengamma.engine.value.ValueSpecification;

public class ValueSpecificationNode implements TreeTableNode {

  private static final String NAME = "ValueSpecification";
  private Object _parent;
  private ValueSpecification _valueSpec;
  
  public ValueSpecificationNode(Object parent, ValueSpecification valueSpec) {
    _parent = parent;
    _valueSpec = valueSpec;
  }
  @Override
  public Object getChildAt(int index) {
    if (_valueSpec != null) {
      switch (index) {
        case 0:
          return new ComputationTargetReferenceNode(this, _valueSpec.getTargetSpecification());
        case 1:
          return new ValuePropertiesNode(this, _valueSpec.getProperties());
      }
    }
    return null;
  }

  @Override
  public int getChildCount() {
    if (_valueSpec != null) {
      return 2;
    } else {
      return 0;
    }
  }

  @Override
  public int getIndexOfChild(Object child) {
    if (_valueSpec != null) {
      if (_valueSpec.getTargetSpecification() != null) {
        if (child instanceof ComputationTargetReferenceNode) {
          if (child.equals(new ComputationTargetReferenceNode(this, _valueSpec.getTargetSpecification()))) {
            return 0;
          }
        } else if (child instanceof ValuePropertiesNode) {
          if (child.equals(new ValuePropertiesNode(this, _valueSpec.getProperties()))) {
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
      return _valueSpec.getValueName();
    }
    return null;
  }
}
