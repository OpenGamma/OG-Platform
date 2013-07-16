/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.enginedebugger.node;

import java.util.Map;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Node representing a failure of getAdditionalRequirements() during graph-building
 */
public class GetAdditionalRequirementsFailedNode extends AbstractFailureWithRequirementsNode {

  private static final String NAME = "GetAdditionalRequirementsFailed";

  public GetAdditionalRequirementsFailedNode(Object parent, ValueRequirement valueRequirement, String function, 
                                             ValueSpecification desiredOutput, Map<ValueSpecification, ValueRequirement> requirements) {
    super(parent, valueRequirement, function, desiredOutput, requirements, "Requirements");
  }

  @Override
  public Object getColumn(int column) {
    if (column == 0) {
      return NAME;
    }
    return null;
  }
  
  @Override
  public boolean equals(Object o) {
    boolean result = super.equals(o);
    return result && o instanceof GetAdditionalRequirementsFailedNode;
  }

  // hashCode from superclass used.
}
