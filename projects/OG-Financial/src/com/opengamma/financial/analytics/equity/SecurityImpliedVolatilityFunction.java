/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.equity;

import java.util.Collections;
import java.util.Set;

import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;


/**
 * Provides the implied volatility for the security of a position as a value on the position
 */
public class SecurityImpliedVolatilityFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs,
      ComputationTarget target, Set<ValueRequirement> desiredValues) {
    double marketValue = (Double) inputs.getValue(getRequirement(target));
    return Collections.singleton(new ComputedValue(getSpecification(target), marketValue));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    if (target.getType() != getTargetType()) {
      return false;
    }
    final Security security = target.getPosition().getSecurity();
    //TODO what else?
    return security instanceof EquityOptionSecurity || security instanceof EquityIndexOptionSecurity;
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target,
      ValueRequirement desiredValue) {
    ValueRequirement valueRequirement = getRequirement(target);
    return Collections.singleton(valueRequirement);
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    ValueSpecification spec = getSpecification(target);
    return Collections.singleton(spec);
  }

  private ValueSpecification getSpecification(ComputationTarget target) {
    return new ValueSpecification(new ValueRequirement(ValueRequirementNames.SECURITY_IMPLIED_VOLATLITY, target.getPosition()), getUniqueId());
  }
  
  private ValueRequirement getRequirement(ComputationTarget target) {
    return new ValueRequirement(MarketDataRequirementNames.IMPLIED_VOLATILITY, ComputationTargetType.SECURITY, target.getPosition().getSecurity().getUniqueId());
  }

}
