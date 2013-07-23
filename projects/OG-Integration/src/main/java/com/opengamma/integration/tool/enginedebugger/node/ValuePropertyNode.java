/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.enginedebugger.node;

/**
 * container for a value property, purely for use by the tree table model.
 */
public class ValuePropertyNode extends AbstractTreeTableLeafNode {
  private static final String NAME = "ValueProperty";
  private String _name;
  private String _value;

  public ValuePropertyNode(String name, String value) {
    _name = name;
    _value = value;
  }
  
  public String getName() {
    return _name;
  }
  
  public String getValue() {
    return _value;
  }
  
  @Override
  public boolean equals(Object other) {
    if (!(other instanceof ValuePropertyNode)) {
      return false;
    }
    ValuePropertyNode o = (ValuePropertyNode) other;
    return _name.equals(o.getName()) && _value.equals(o.getValue());
  }
  
  @Override
  public int hashCode() {
    return _name.hashCode() * _value.hashCode();
  }

  @Override
  public Object getColumn(int column) {
    switch (column) {
      case 0:
        return NAME;
      case 1:
        return getName();
      case 2:
        return getValue();
    }
    return null;
  }


}
