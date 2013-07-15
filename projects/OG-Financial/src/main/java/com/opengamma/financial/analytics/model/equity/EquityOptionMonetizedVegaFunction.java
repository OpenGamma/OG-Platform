/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
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
import com.opengamma.util.async.AsynchronousExecution;

/**
 *
 */
public class EquityOptionMonetizedVegaFunction extends AbstractFunction.NonCompiledInvoker {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(EquityOptionMonetizedVegaFunction.class);

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.EQUITY_OPTION_SECURITY.or(FinancialSecurityTypes.EQUITY_INDEX_OPTION_SECURITY);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = ValueProperties.all();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.MONETIZED_VEGA, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> calculationMethod = constraints.getValues(ValuePropertyNames.CALCULATION_METHOD);
    if (calculationMethod == null || calculationMethod.size() != 1) {
      s_logger.error("Need to have a single calculation method");
      return null;
    }
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.CALCULATION_METHOD, Iterables.getOnlyElement(calculationMethod))
        .get();
    return Collections.singleton(new ValueRequirement(ValueRequirementNames.VEGA, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final ValueSpecification spec = Iterables.getOnlyElement(inputs.entrySet()).getKey();
    final ValueProperties vegaProperties = spec.getProperties();
    final ValueProperties properties = vegaProperties.copy()
        .withoutAny(ValuePropertyNames.FUNCTION)
        .with(ValuePropertyNames.FUNCTION, getUniqueId())
        .with(ValuePropertyNames.CURRENCY, FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode())
        .get();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.MONETIZED_VEGA, target.toSpecification(), properties));
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final Object vegaObject = inputs.getValue(ValueRequirementNames.VEGA);
    if (vegaObject == null) {
      throw new OpenGammaRuntimeException("Could not get vega");
    }
    final double vega = (Double) vegaObject;
    final Double pointValue = EquitySecurityUtils.getPointValue(target.getSecurity());
    if (pointValue == null) {
      throw new OpenGammaRuntimeException("Could not get point value for " + target.getSecurity());
    }
    final double monetizedVega = vega * pointValue;
    final ValueProperties properties = Iterables.getOnlyElement(desiredValues).getConstraints().copy()
        .with(ValuePropertyNames.FUNCTION, getUniqueId())
        .get();
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.MONETIZED_VEGA, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, monetizedVega));
  }

}
