/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.SecuritySource;
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
import com.opengamma.financial.security.CurrenciesVisitor;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 *
 */
public class SecurityFXHistoricalTimeSeriesFunction extends AbstractFunction.NonCompiledInvoker {

  private SecuritySource _securitySource;

  @Override
  public void init(final FunctionCompilationContext context) {
    _securitySource = context.getSecuritySource();
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.FINANCIAL_SECURITY;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties()
        .withAny(ValuePropertyNames.CURRENCY).get();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.HISTORICAL_FX_TIME_SERIES, target.toSpecification(), properties));
  }

  private ValueRequirement createRequirement(final FunctionCompilationContext context, final Currency desiredCurrency, final Currency securityCurrency) {
    if (desiredCurrency.equals(securityCurrency)) {
      return null;
    }
    return ConventionBasedFXRateFunction.getHistoricalTimeSeriesRequirement(desiredCurrency, securityCurrency);
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Collection<Currency> securityCurrencies = CurrenciesVisitor.getCurrencies(security, getSecuritySource());
    final Set<String> resultCurrencies = constraints.getValues(ValuePropertyNames.CURRENCY);
    if ((resultCurrencies == null) || (resultCurrencies.size() != 1)) {
      return null;
    }
    final Currency desiredCurrency = Currency.of(Iterables.getOnlyElement(resultCurrencies));
    if (securityCurrencies.size() == 1) {
      final Currency securityCurrency = Iterables.getOnlyElement(securityCurrencies);
      final ValueRequirement htsRequirement = createRequirement(context, desiredCurrency, securityCurrency);
      return htsRequirement != null ? ImmutableSet.of(htsRequirement) : null;
    }
    final Set<ValueRequirement> requirements = new HashSet<>();
    for (final Currency securityCurrency : securityCurrencies) {
      final ValueRequirement htsRequirement = createRequirement(context, desiredCurrency, securityCurrency);
      if (htsRequirement != null) {
        requirements.add(htsRequirement);
      }
    }
    return !requirements.isEmpty() ? requirements : null;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
    final String desiredCurrency = desiredValue.getConstraint(ValuePropertyNames.CURRENCY);
    final Collection<Currency> currencies = CurrenciesVisitor.getCurrencies(target.getSecurity(), _securitySource);
    if (currencies.size() != inputs.getAllValues().size()) {
      if (!currencies.contains(Currency.of(desiredCurrency))) {
        throw new OpenGammaRuntimeException("Do not have one FX series for each requested");
      }
    }
    final Map<UnorderedCurrencyPair, DoubleTimeSeries<?>> fxSeries = new HashMap<>();
    final Iterator<Currency> currencyIterator = currencies.iterator();
    for (final ComputedValue input : inputs.getAllValues()) {
      final Currency currency = currencyIterator.next();
      if (currency.getCode().equals(desiredCurrency)) {
        currencyIterator.next();
      } else {
        fxSeries.put(UnorderedCurrencyPair.of(currency, Currency.of(desiredCurrency)), (DoubleTimeSeries<?>) input.getValue());
      }
    }
    final ValueProperties properties = createValueProperties().with(ValuePropertyNames.CURRENCY, desiredCurrency).get();
    final ValueSpecification outputSpec = new ValueSpecification(ValueRequirementNames.HISTORICAL_FX_TIME_SERIES, target.toSpecification(), properties);
    return ImmutableSet.of(new ComputedValue(outputSpec, fxSeries));
  }

  protected SecuritySource getSecuritySource() {
    return _securitySource;
  }

}
