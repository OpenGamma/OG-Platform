/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.enginedebugger.node;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * A wrapper for a map of value specification to value requirement that makes it easier to write 
 * a tree table model 
 */
public class ValueSpecificationToRequirementMapNode implements TreeTableNode {
  private static final Logger s_logger = LoggerFactory.getLogger(ValueSpecificationToRequirementMapNode.class);
  private static final Object NAME = "Map of ValueSpec->ValueReq";
  private Map<ValueSpecification, ValueRequirement> _map;
  private String _description;
  private List<ValueSpecification> _keySet;
  private Object _parent;

  public ValueSpecificationToRequirementMapNode(Object parent, Map<ValueSpecification, ValueRequirement> map, String description) {
    _parent = parent;
    _map = map;
    _description = description;
    _keySet = new ArrayList<ValueSpecification>(map.keySet());
  }
  
  public String getDescription() {
    return _description;
  }
  
  private int getSize() {
    return _keySet.size();
  }
  
  private SpecToRequirementEntryNode getEntry(int index) {
    ValueSpecification valueSpecification = _keySet.get(index);
    ValueRequirement valueRequirement = _map.get(valueSpecification);
    return new SpecToRequirementEntryNode(valueSpecification, valueRequirement);
  }
  
  private int indexOf(SpecToRequirementEntryNode entry) {
    return _keySet.indexOf(entry.getValueSpecification());
  }
  
  @Override
  public Object getChildAt(int index) {
    return getEntry(index);
  }

  @Override
  public int getChildCount() {
    return getSize();
  }

  @Override
  public int getIndexOfChild(Object child) {
    if (child instanceof SpecToRequirementEntryNode) {
      return indexOf((SpecToRequirementEntryNode) child);
    }
    return -1;
  }

  @Override
  public Object getColumn(int column) {
    switch (column) {
      case 0:
        return NAME + ((getChildCount() == 0) ? " (Empty)" : "");
      case 1:
        return _description;
    }
    return null;
  }
}
