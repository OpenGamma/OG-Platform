/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.function.FunctionInvoker;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.security.Security;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * Able to scale values produced by the rest of the OG-Financial package.
 *
 * @author kirk
 */
public class PositionScalingFunction
extends AbstractFunction
implements FunctionInvoker {
  private final String _requirementName;
  
  public PositionScalingFunction(String requirementName) {
    ArgumentChecker.checkNotNull(requirementName, "Requirement name");
    _requirementName = requirementName;
  }

  @Override
  public boolean canApplyTo(ComputationTarget target) {
    return target.getType() == ComputationTargetType.POSITION;
  }

  @Override
  public Set<ValueRequirement> getRequirements(ComputationTarget target) {
    Position position = target.getPosition();
    Security security = position.getSecurity();
    ValueRequirement requirement = new ValueRequirement(_requirementName, ComputationTargetType.SECURITY, security.getIdentityKey());
    return Collections.singleton(requirement);
  }

  @Override
  public Set<ValueSpecification> getResults(ComputationTarget target,
      Set<ValueRequirement> requirements) {
    ValueRequirement requirement = new ValueRequirement(_requirementName, target.getSpecification());
    ValueSpecification specification = new ValueSpecification(requirement);
    return Collections.singleton(specification);
  }

  @Override
  public String getShortName() {
    return "PositionScaling for " + _requirementName;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

  @Override
  public Set<ComputedValue> execute(
      FunctionExecutionContext executionContext, FunctionInputs inputs,
      ComputationTarget target) {
    Object value = inputs.getValue(_requirementName);
    ValueRequirement requirement = new ValueRequirement(_requirementName, target.getSpecification());
    ValueSpecification specification = new ValueSpecification(requirement);
    ComputedValue scaledValue = null;
    if(value instanceof Double) {
      Double doubleValue = (Double) value;
      double quantity = target.getPosition().getQuantity().doubleValue();
      doubleValue *= quantity;
      scaledValue = new ComputedValue(specification, doubleValue);
    } else {
      scaledValue = new ComputedValue(specification, value);
    }
    return Collections.singleton(scaledValue);
  }

}
