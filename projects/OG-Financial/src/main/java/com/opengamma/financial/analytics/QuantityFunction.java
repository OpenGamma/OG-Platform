/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Function that returns the quantity of a position or trade.
 */
public class QuantityFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION_OR_TRADE;
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.QUANTITY,
                                                        target.toSpecification(),
                                                        createValueProperties().get()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context,
                                               ComputationTarget target,
                                               ValueRequirement desiredValue) {
    return Collections.emptySet();
  }

  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext,
                                    FunctionInputs inputs,
                                    ComputationTarget target,
                                    Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    ValueRequirement desiredValue = desiredValues.iterator().next();
    BigDecimal quantity = target.getPositionOrTrade().getQuantity();
    ValueSpecification valueSpec = new ValueSpecification(ValueRequirementNames.QUANTITY,
                                                          target.toSpecification(),
                                                          desiredValue.getConstraints());
    return Collections.singleton(new ComputedValue(valueSpec, quantity));
  }
}
