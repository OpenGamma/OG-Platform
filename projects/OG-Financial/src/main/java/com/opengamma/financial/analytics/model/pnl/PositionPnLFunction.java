/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.timeseries.DoubleTimeSeries;

/**
 *
 */
public class PositionPnLFunction extends AbstractFunction.NonCompiledInvoker {

  private static final Logger s_logger = LoggerFactory.getLogger(PositionPnLFunction.class);

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String currency = desiredValue.getConstraint(ValuePropertyNames.CURRENCY);
    final String samplingPeriod = desiredValue.getConstraint(ValuePropertyNames.SAMPLING_PERIOD);
    final String scheduleCalculator = desiredValue.getConstraint(ValuePropertyNames.SCHEDULE_CALCULATOR);
    final String samplingFunction = desiredValue.getConstraint(ValuePropertyNames.SAMPLING_FUNCTION);
    final ValueProperties properties = createValueProperties()
        .with(ValuePropertyNames.CURRENCY, currency)
        .with(ValuePropertyNames.SAMPLING_PERIOD, samplingPeriod)
        .with(ValuePropertyNames.SCHEDULE_CALCULATOR, scheduleCalculator)
        .with(ValuePropertyNames.SAMPLING_FUNCTION, samplingFunction)
        .with(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS, "Delta").get();
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.PNL_SERIES, target.toSpecification(), properties);
    DoubleTimeSeries<?> ts = null;
    for (final ComputedValue input : inputs.getAllValues()) {
      final Object value = input.getValue();
      if (value == null) {
        throw new OpenGammaRuntimeException("Could not get time series for " + input.getSpecification());
      } else if (!(DoubleTimeSeries.class.isAssignableFrom(value.getClass()))) {
        throw new OpenGammaRuntimeException("Value for " + input.getSpecification() + " was not a time series: " + value.getClass());
      }
      if (ts == null) {
        ts = (DoubleTimeSeries<?>) value;
      } else {
        ts = ts.add((DoubleTimeSeries<?>) value);
      }
    }
    return Collections.singleton(new ComputedValue(spec, ts));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getPosition().getSecurity() instanceof FinancialSecurity;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties()
        .withAny(ValuePropertyNames.CURRENCY)
        .withAny(ValuePropertyNames.SAMPLING_PERIOD)
        .withAny(ValuePropertyNames.SCHEDULE_CALCULATOR)
        .withAny(ValuePropertyNames.SAMPLING_FUNCTION)
        .with(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS, "Delta").get();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.PNL_SERIES, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> currencies = constraints.getValues(ValuePropertyNames.CURRENCY);
    if (currencies == null || currencies.size() != 1) {
      return null;
    }
    final Set<String> samplingPeriods = constraints.getValues(ValuePropertyNames.SAMPLING_PERIOD);
    if (samplingPeriods == null || samplingPeriods.size() != 1) {
      return null;
    }
    final Set<String> scheduleCalculators = constraints.getValues(ValuePropertyNames.SCHEDULE_CALCULATOR);
    if (scheduleCalculators == null || scheduleCalculators.size() != 1) {
      return null;
    }
    final Set<String> samplingFunctions = constraints.getValues(ValuePropertyNames.SAMPLING_FUNCTION);
    if (samplingFunctions == null || samplingFunctions.size() != 1) {
      return null;
    }
    final String samplingPeriod = samplingPeriods.iterator().next();
    final String scheduleCalculator = scheduleCalculators.iterator().next();
    final String samplingFunction = samplingFunctions.iterator().next();
    final FinancialSecurity security = (FinancialSecurity) target.getPosition().getSecurity();
    final String currency = currencies.iterator().next();
    try {
      final Set<ValueRequirement> set = OpenGammaCompilationContext.getPnLRequirementsGatherer(context).getFirstOrderRequirements(security, samplingPeriod, scheduleCalculator, samplingFunction,
          target.toSpecification(), currency);
      return set;
    } catch (final OpenGammaRuntimeException e) {
      s_logger.error("Could not get delta requirements for {} {}; reason was {}", new Object[] {getCcyString(security), security.getClass(), e.getMessage() });
      return null;
    }
  }

  private String getCcyString(final FinancialSecurity financialSecurity) {
    try {
      return FinancialSecurityUtils.getCurrency(financialSecurity).getCode();
    } catch (final UnsupportedOperationException e) {
      return financialSecurity.accept(new FinancialSecurityVisitorAdapter<String>() {

        @Override
        public String visitFXForwardSecurity(final FXForwardSecurity security) {
          return security.getPayCurrency().getCode() + "/" + security.getReceiveCurrency().getCode();
        }

        @Override
        public String visitFXOptionSecurity(final FXOptionSecurity security) {
          return security.getPutCurrency().getCode() + "/" + security.getCallCurrency().getCode();
        }
      });
    }
  }
}
