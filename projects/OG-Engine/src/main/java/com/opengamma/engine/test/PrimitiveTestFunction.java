/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.test;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInvoker;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * A function for primitive testing.
 */
public class PrimitiveTestFunction extends AbstractFunction.NonCompiled {

  /**
   * The requirement name.
   */
  private final String _requirementName;
  /**
   * The function invoker.
   */
  private FunctionInvoker _functionInvoker;

  /**
   * Creates an instance.
   * 
   * @param requirementName  the name, not null
   */
  public PrimitiveTestFunction(String requirementName) {
    ArgumentChecker.notNull(requirementName, "Requirement name");
    _requirementName = requirementName;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    assert ComputationTargetType.PRIMITIVE.isCompatible(target.getType());
    return true;
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, final ValueRequirement desiredValue) {
    ValueRequirement requirement = new ValueRequirement(_requirementName,
        ComputationTargetType.PRIMITIVE,
        UniqueId.of("foo", "bar"));
    return Collections.singleton(requirement);
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    ValueSpecification specification = new ValueSpecification(_requirementName, target.toSpecification(), createValueProperties().get());
    return Collections.singleton(specification);
  }

  @Override
  public String getShortName() {
    return "PrimitiveTestFunction for " + _requirementName;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PRIMITIVE;
  }

  public void setFunctionInvoker(final FunctionInvoker functionInvoker) {
    _functionInvoker = functionInvoker;
  }

  @Override
  public FunctionInvoker getFunctionInvoker() {
    return _functionInvoker;
  }

}
