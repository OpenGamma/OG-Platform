/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.function;

import java.util.Collections;
import java.util.Set;

import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.security.lookup.DefaultSecurityAttributeMappings;
import com.opengamma.financial.security.lookup.SecurityAttribute;
import com.opengamma.financial.security.lookup.SecurityAttributeMapper;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Function which uses mappings configured in the {@link SecurityAttributeMapper} to 
 * extract its values from securities.
 */
public class DefaultSecurityAttributeFunction extends AbstractFunction.NonCompiledInvoker {

  private SecurityAttribute _attribute;
  private String _valueRequirementName;

  public DefaultSecurityAttributeFunction(String attribute, String valueRequirementName) {
    _attribute = SecurityAttribute.valueOf(attribute);
    _valueRequirementName = valueRequirementName;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION_OR_TRADE;
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    return Collections.singleton(getSpec(target));
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    return Collections.singleton(new ValueRequirement(
        ValueRequirementNames.CURRENCY_PAIRS,
        ComputationTargetSpecification.NULL));
  }

  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    CurrencyPairs ccyPairs = (CurrencyPairs) inputs.getComputedValue(ValueRequirementNames.CURRENCY_PAIRS).getValue();

    SecurityAttributeMapper mapper = DefaultSecurityAttributeMappings.create(ccyPairs);
    
    Security security = target.getPositionOrTrade().getSecurity();
    Object result = mapper.valueFor(_attribute, security);
    
    return Collections.singleton(new ComputedValue(getSpec(target), result));
  }

  private ValueSpecification getSpec(ComputationTarget target) {
    return new ValueSpecification(_valueRequirementName,
        target.toSpecification(),
        createValueProperties().get());
  }

}
