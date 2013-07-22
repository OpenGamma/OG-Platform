/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.enginedebugger.node;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * Listing an entry in the map - has convenience functions to make it easier to represent in a table.
 */
public class SpecToRequirementEntryNode implements TreeTableNode {
  private static final String NAME = "ValueSpec->ValueReq";
  private final ValueSpecification _specification;
  private final ValueRequirement _requirement;
  private List<String> _orderedUnionProperties;
  
  public SpecToRequirementEntryNode(ValueSpecification specification, ValueRequirement requirement) {
    ArgumentChecker.notNull(specification, "specification");
    ArgumentChecker.notNull(requirement, "requirement");
    _specification = specification;
    _requirement = requirement;
    _orderedUnionProperties = new ArrayList<>(getUnionConstraints());
  }
  
  private Set<String> getUnionConstraints() {
    Set<String> constraintNames = new LinkedHashSet<>(_specification.getProperties().getProperties());
    constraintNames.addAll(_specification.getProperties().getProperties());
    return constraintNames;
  }
  
  public String getValueName() {
    return _specification.getValueName();
  }
  
  public String getFunctionUniqueId() {
    return _specification.getFunctionUniqueId();
  }
  
  public ComputationTargetRequirement getRequirementTarget() {
    return (ComputationTargetRequirement) _requirement.getTargetReference();
  }
  
  public ComputationTargetSpecification getSpecificationTarget() {
    return _specification.getTargetSpecification();
  }
  
  public SpanningValuePropertyEntryNode getSpanningValuePropertyEntry(int i) {
    String propertyName = _orderedUnionProperties.get(i);
    return new SpanningValuePropertyEntryNode(propertyName, getRequirementConstraint(propertyName), isOptionalRequirementConstraint(propertyName),
                                          getSpecificationConstraint(propertyName), isOptionalSpecificationConstraint(propertyName));
  }
  
  public String getRequirementConstraint(String constraint) {
    return _requirement.getConstraints().getValues(constraint).toString();
  }
  
  public boolean isOptionalRequirementConstraint(String constraint) {
    return _requirement.getConstraints().isOptional(constraint);
  }
  
  public String getSpecificationConstraint(String constraint) {
    return _specification.getProperties().getValues(constraint).toString();
  }
  
  public boolean isOptionalSpecificationConstraint(String constraint) {
    return _specification.getProperties().isOptional(constraint);
  }
  
  public ValueSpecification getValueSpecification() {
    return _specification;
  }
  
  public ValueRequirement getValueRequirement() {
    return _requirement;
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null) {
      return false;
    }
    if (!(o instanceof SpecToRequirementEntryNode)) {
      return false;
    }
    SpecToRequirementEntryNode other = (SpecToRequirementEntryNode) o;
    return _specification.equals(other._specification) && _requirement.equals(other._requirement);
  }
  
  @Override
  public int hashCode() {
    return _specification.hashCode() ^ _requirement.hashCode();
  }

  @Override
  public Object getChildAt(int index) {
    switch (index) {
      case 0:
        return new ValueRequirementNode(this, _requirement);
      case 1:
        return new ValueSpecificationNode(this, _specification);
      default:
        return getSpanningValuePropertyEntry(index - 2);
    }
    
  }

  @Override
  public int getChildCount() {
    return _orderedUnionProperties.size() + 2; // add the requirement and spec
  }

  @Override
  public int getIndexOfChild(Object child) {
    if (child instanceof SpanningValuePropertyEntryNode) {
      SpanningValuePropertyEntryNode node = (SpanningValuePropertyEntryNode) child;
      return _orderedUnionProperties.indexOf(node.getCommonConstraintName()) + 2;
    } else if (child instanceof ValueRequirementNode) {
      if (child.equals(new ValueRequirementNode(this, _requirement))) {
        return 0;
      }
    } else if (child instanceof ValueSpecificationNode) {
      if (child.equals(new ValueSpecificationNode(this, _specification))) {
        return 1;
      }
    }
    return -1;
  }

  @Override
  public Object getColumn(int column) {
    switch (column) {
      case 0:
        return NAME;
      case 1:
        return getValueName();
      case 2:
        return getFunctionUniqueId();
    }
    return null;
  }
  
}
