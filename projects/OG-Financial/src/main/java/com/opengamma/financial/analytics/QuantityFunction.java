/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Set;

import com.opengamma.core.position.PositionOrTrade;
import com.opengamma.core.security.Security;
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
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Function that returns the quantity of a position or trade. In the case of bonds,
 * this is the quantity / par amount. For all other security types, the quantity is
 * read from the trade or position.
 */
public class QuantityFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final PositionOrTrade positionOrTrade = target.getPositionOrTrade();
    final Security security = positionOrTrade.getSecurity();
    final BigDecimal quantity;
    if (security instanceof BondSecurity) {
      final BondSecurity bondSecurity = (BondSecurity) security;
      quantity = new BigDecimal(positionOrTrade.getQuantity().doubleValue() / bondSecurity.getParAmount());
    } else {
      quantity = target.getPositionOrTrade().getQuantity();
    }
    final ValueSpecification valueSpec = new ValueSpecification(ValueRequirementNames.QUANTITY, target.toSpecification(), desiredValue.getConstraints());
    return Collections.singleton(new ComputedValue(valueSpec, quantity));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION_OR_TRADE;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.QUANTITY, target.toSpecification(), createValueProperties().get()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    return Collections.emptySet();
  }
}
