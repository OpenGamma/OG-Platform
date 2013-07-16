package com.opengamma.integration.tool.enginedebugger.node;

import java.util.Map;

import com.opengamma.engine.depgraph.ResolutionFailureImpl;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

public class SuccessfulFunctionNode extends AbstractFailureWithRequirementsNode {
  
  private static final String NAME = "SuccessfulFunction";

  public SuccessfulFunctionNode(Object parent, ValueRequirement valueRequirement, String function, ValueSpecification desiredOutput, 
                                Map<ValueSpecification, ValueRequirement> satisfied) {
    super(parent, valueRequirement, function, desiredOutput, satisfied, "Satisfied");
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
    return result && o instanceof SuccessfulFunctionNode;
  }
  
}
