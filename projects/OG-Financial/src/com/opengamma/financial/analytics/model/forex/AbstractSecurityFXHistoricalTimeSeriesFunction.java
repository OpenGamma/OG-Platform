/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 *
 */
public abstract class AbstractSecurityFXHistoricalTimeSeriesFunction extends AbstractFunction.NonCompiledInvoker {
  private SecuritySource _securitySource;

  @Override
  public void init(final FunctionCompilationContext context) {
    _securitySource = context.getSecuritySource();
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
    final String desiredCurrency = desiredValue.getConstraint(ValuePropertyNames.CURRENCY);
    final Collection<Currency> currencies = FinancialSecurityUtils.getCurrencies(target.getSecurity(), _securitySource);
    if (currencies.size() != inputs.getAllValues().size()) {
      if (!currencies.contains(desiredCurrency)) {
        throw new OpenGammaRuntimeException("Do not have one FX series for each requested");
      }
    }
    final Map<UnorderedCurrencyPair, HistoricalTimeSeries> fxSeries = new HashMap<UnorderedCurrencyPair, HistoricalTimeSeries>();
    final Iterator<Currency> currencyIterator = currencies.iterator();
    for (final ComputedValue input : inputs.getAllValues()) {
      final Currency currency = currencyIterator.next();
      if (currency.getCode().equals(desiredCurrency)) {
        currencyIterator.next();
      } else {
        fxSeries.put(UnorderedCurrencyPair.of(currency, Currency.of(desiredCurrency)), (HistoricalTimeSeries) input.getValue());
      }
    }
    final ValueProperties properties = createValueProperties().with(ValuePropertyNames.CURRENCY, desiredCurrency).get();
    final ValueSpecification outputSpec = new ValueSpecification(ValueRequirementNames.HISTORICAL_FX_TIME_SERIES, target.toSpecification(), properties);
    return ImmutableSet.of(new ComputedValue(outputSpec, fxSeries));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getUniqueId() == null) {
      return false;
    }
    if (!(target.getSecurity() instanceof FinancialSecurity)) {
      return false;
    }
    return true;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties().withAny(ValuePropertyNames.CURRENCY).get();
    return ImmutableSet.of(new ValueSpecification(ValueRequirementNames.HISTORICAL_FX_TIME_SERIES, target.toSpecification(), properties));
  }

  protected SecuritySource getSecuritySource() {
    return _securitySource;
  }

}
