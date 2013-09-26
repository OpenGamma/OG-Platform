/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option;

import java.util.Collections;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
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
import com.opengamma.financial.analytics.model.forex.ConventionBasedFXRateFunction;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 *
 */
public class FXOptionSpotRateFunction extends AbstractFunction.NonCompiledInvoker {
  /** Property indicating the data type required */
  public static final String PROPERTY_DATA_TYPE = "DataType";
  /** Live FX spot rates for a security */
  public static final String LIVE = "Live";
  /** Last close FX spot rates for a security */
  public static final String LAST_CLOSE = "LastClose";

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
    final String dataType = desiredValue.getConstraint(PROPERTY_DATA_TYPE);
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Currency putCurrency = security.accept(ForexVisitors.getPutCurrencyVisitor());
    final Currency callCurrency = security.accept(ForexVisitors.getCallCurrencyVisitor());
    final UnorderedCurrencyPair currencyPair = UnorderedCurrencyPair.of(putCurrency, callCurrency);
    if (dataType.equals(LIVE)) {
      final Object spotObject = inputs.getValue(ValueRequirementNames.SPOT_RATE);
      if (spotObject == null) {
        throw new OpenGammaRuntimeException("Could not get live market data for " + currencyPair);
      }
      final double spot = (Double) spotObject;
      return Collections.singleton(new ComputedValue(new ValueSpecification(ValueRequirementNames.SPOT_RATE_FOR_SECURITY, target.toSpecification(),
          createValueProperties().with(PROPERTY_DATA_TYPE, LIVE).get()), spot));
    } else if (dataType.equals(LAST_CLOSE)) {
      final Object spotObject = inputs.getValue(ValueRequirementNames.HISTORICAL_TIME_SERIES_LATEST);
      if (spotObject == null) {
        throw new OpenGammaRuntimeException("Could not get last close market data for " + currencyPair);
      }
      final double spot = (Double) spotObject;
      return Collections.singleton(new ComputedValue(new ValueSpecification(ValueRequirementNames.SPOT_RATE_FOR_SECURITY, target.toSpecification(),
          createValueProperties().with(PROPERTY_DATA_TYPE, LAST_CLOSE).get()), spot));
    }
    throw new OpenGammaRuntimeException("Did not recognise property type " + dataType);
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.FX_OPTION_SECURITY.or(FinancialSecurityTypes.FX_DIGITAL_OPTION_SECURITY).or(FinancialSecurityTypes.FX_BARRIER_OPTION_SECURITY);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.SPOT_RATE_FOR_SECURITY, target.toSpecification(),
        createValueProperties().with(PROPERTY_DATA_TYPE, LIVE, LAST_CLOSE).get()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Currency putCurrency = security.accept(ForexVisitors.getPutCurrencyVisitor());
    final Currency callCurrency = security.accept(ForexVisitors.getCallCurrencyVisitor());
    final UnorderedCurrencyPair currencyPair = UnorderedCurrencyPair.of(putCurrency, callCurrency);
    final Set<String> dataTypes = desiredValue.getConstraints().getValues(PROPERTY_DATA_TYPE);
    if ((dataTypes == null) || dataTypes.isEmpty() || dataTypes.contains(LIVE)) {
      // Live
      return Collections.singleton(ConventionBasedFXRateFunction.getSpotRateRequirement(currencyPair));
    }
    // Last close
    return Collections.singleton(ConventionBasedFXRateFunction.getLatestHistoricalRequirement(currencyPair));
  }

}
