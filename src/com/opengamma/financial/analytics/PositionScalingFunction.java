/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.util.Collections;
import java.util.Set;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
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
 */
public class PositionScalingFunction extends AbstractFunction implements FunctionInvoker {
  private final String _requirementName;
  
  public PositionScalingFunction(String requirementName) {
    ArgumentChecker.notNull(requirementName, "Requirement name");
    _requirementName = requirementName;
  }

  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    return target.getType() == ComputationTargetType.POSITION;
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target) {
    Position position = target.getPosition();
    Security security = position.getSecurity();
    ValueRequirement requirement = new ValueRequirement(_requirementName, ComputationTargetType.SECURITY, security.getUniqueIdentifier());
    return Collections.singleton(requirement);
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, 
      ComputationTarget target) {
    ValueRequirement requirement = new ValueRequirement(_requirementName, target.toSpecification());
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
      ComputationTarget target,
      Set<ValueRequirement> desiredValues) {
    Object value = inputs.getValue(_requirementName);
    ValueRequirement requirement = new ValueRequirement(_requirementName, target.toSpecification());
    ValueSpecification specification = new ValueSpecification(requirement);
    ComputedValue scaledValue = null;
    if (value instanceof Double) {
      Double doubleValue = (Double) value;
      double quantity = target.getPosition().getQuantity().doubleValue();
      doubleValue *= quantity;
      scaledValue = new ComputedValue(specification, doubleValue);
    } else {
      scaledValue = new ComputedValue(specification, value);
    }
    return Collections.singleton(scaledValue);
  }

  private static final String REQUIREMENT_NAME_KEY = "requirementName";

  @Override
  public void toFudgeMsg(final FudgeSerializationContext context, final MutableFudgeFieldContainer message) {
    super.toFudgeMsg(context, message);
    message.add(REQUIREMENT_NAME_KEY, _requirementName);
  }

  public static PositionScalingFunction fromFudgeMsg(final FudgeFieldContainer message) {
    return fromFudgeMsg(new PositionScalingFunction(message.getString(REQUIREMENT_NAME_KEY)), message);
  }

}
