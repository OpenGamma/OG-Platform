package com.opengamma.integration.tool.enginedebugger;

import java.util.Map;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.integration.tool.enginedebugger.node.AbstractFailureWithRequirementsNode;
import com.opengamma.integration.tool.enginedebugger.node.GetAdditionalRequirementsFailedNode;

public class BlacklistSuppressedNode extends AbstractFailureWithRequirementsNode {

  private static final String NAME = "BlacklistSuppressed";

  public BlacklistSuppressedNode(Object parent, ValueRequirement valueRequirement, String function, ValueSpecification desiredOutput, Map<ValueSpecification, ValueRequirement> requirements) {
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
    return result && o instanceof BlacklistSuppressedNode;
  }

}
