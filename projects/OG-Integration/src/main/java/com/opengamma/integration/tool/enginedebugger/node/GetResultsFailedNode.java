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
 * Node representing when call to getResults() fails during graph building
 */
public class GetResultsFailedNode extends AbstractFailureWithRequirementsNode {

  private static final String NAME = "GetResultsFailed";

  public GetResultsFailedNode(Object parent, ValueRequirement valueRequirement, String function, ValueSpecification desiredOutput, Map<ValueSpecification, ValueRequirement> satisfied) {
    super(parent, valueRequirement, function, desiredOutput, satisfied, "Requirements");
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
    return result && o instanceof GetResultsFailedNode;
  }
  
  // hashCode from super class

}
