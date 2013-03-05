/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity;

import java.util.Collections;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.util.async.AsynchronousExecution;

/**
 *
 */
public class EquityOptionMonetizedVegaFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.EQUITY_OPTION_SECURITY.or(FinancialSecurityTypes.EQUITY_INDEX_OPTION_SECURITY);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties()
        .with(ValuePropertyNames.CURRENCY, FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode())
        .get();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.MONETIZED_VEGA, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.CALCULATION_METHOD, "BlackMethod")
        .get();
    return Collections.singleton(new ValueRequirement(ValueRequirementNames.VEGA, target.toSpecification(), properties));
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final Object vegaObject = inputs.getValue(ValueRequirementNames.VEGA);
    if (vegaObject == null) {
      throw new OpenGammaRuntimeException("Could not get vega");
    }
    final double vega = (Double) vegaObject;
    final double monetizedVega = vega * getContractSize(target.getSecurity());
    final ValueProperties properties = createValueProperties()
        .with(ValuePropertyNames.CURRENCY, FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode())
        .get();
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.MONETIZED_VEGA, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, monetizedVega));
  }

  private double getContractSize(final Security security) {
    if (security instanceof EquityOptionSecurity) {
      return ((EquityOptionSecurity) security).getPointValue();
    }
    if (security instanceof EquityIndexOptionSecurity) {
      return ((EquityIndexOptionSecurity) security).getPointValue();
    }
    throw new OpenGammaRuntimeException("Unhandled type of security " + security.getClass());
  }
}
