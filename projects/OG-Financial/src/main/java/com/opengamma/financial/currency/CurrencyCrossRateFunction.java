/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * Produces a cross rate using the configured intermediate currency.
 */
public class CurrencyCrossRateFunction extends AbstractFunction.NonCompiledInvoker {

  private static final Pattern s_validate = Pattern.compile("[A-Z]{3}_[A-Z]{3}");

  private final String _intermediate;
  private String _rateLookupIdentifierScheme = CurrencyConversionFunction.DEFAULT_LOOKUP_IDENTIFIER_SCHEME;
  private String _rateLookupValueName = CurrencyConversionFunction.DEFAULT_LOOKUP_VALUE_NAME;

  public CurrencyCrossRateFunction(final String intermediateCurrencyISO) {
    this(Currency.of(intermediateCurrencyISO));
  }

  public CurrencyCrossRateFunction(final Currency currency) {
    ArgumentChecker.notNull(currency, "currency");
    _intermediate = currency.getCode();
  }

  public void setRateLookupValueName(final String rateLookupValueName) {
    ArgumentChecker.notNull(rateLookupValueName, "rateLookupValueName");
    _rateLookupValueName = rateLookupValueName;
  }

  public String getRateLookupValueName() {
    return _rateLookupValueName;
  }

  public void setRateLookupIdentifierScheme(final String rateLookupIdentifierScheme) {
    ArgumentChecker.notNull(rateLookupIdentifierScheme, "rateLookupIdentifierScheme");
    _rateLookupIdentifierScheme = rateLookupIdentifierScheme;
  }

  public String getRateLookupIdentifierScheme() {
    return _rateLookupIdentifierScheme;
  }

  public String getIntermediateCurrencyISO() {
    return _intermediate;
  }

  public Currency getIntermediateCurrency() {
    return Currency.of(getIntermediateCurrencyISO());
  }

  private static Pair<String, String> parse(final ComputationTarget target) {
    final int underscore = target.getUniqueId().getValue().indexOf('_');
    final String numerator = target.getUniqueId().getValue().substring(0, underscore);
    final String denominator = target.getUniqueId().getValue().substring(underscore + 1);
    return Pair.of(numerator, denominator);
  }

  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) {
    final Pair<String, String> currencies = parse(target);
    ValueRequirement req = createRequirement(currencies.getFirst(), getIntermediateCurrencyISO());
    final Object rate1 = inputs.getValue(req);
    if (rate1 == null) {
      throw new IllegalArgumentException("rate 1 - " + req + " not available");
    }
    if (!(rate1 instanceof Double)) {
      throw new IllegalArgumentException("rate 1 is not double - " + rate1);
    }
    req = createRequirement(getIntermediateCurrencyISO(), currencies.getSecond());
    final Object rate2 = inputs.getValue(req);
    if (rate2 == null) {
      throw new IllegalArgumentException("rate 2 - " + req + " not available");
    }
    if (!(rate2 instanceof Double)) {
      throw new IllegalArgumentException("rate 2 is not double - " + rate2);
    }
    return Collections.singleton(new ComputedValue(createResultValueSpecification(target), (Double) rate1 * (Double) rate2));
  }

  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    if (target.getType() != ComputationTargetType.PRIMITIVE) {
      return false;
    }
    if (!getRateLookupIdentifierScheme().equals(target.getUniqueId().getScheme())) {
      return false;
    }
    if (!s_validate.matcher(target.getUniqueId().getValue()).matches()) {
      return false;
    }
    final Pair<String, String> currencies = parse(target);
    if (getIntermediateCurrencyISO().equals(currencies.getFirst())) {
      return false;
    }
    if (getIntermediateCurrencyISO().equals(currencies.getSecond())) {
      return false;
    }
    return true;
  }

  private ValueRequirement createRequirement(final String numerator, final String denominator) {
    return new ValueRequirement(getRateLookupValueName(), ComputationTargetType.PRIMITIVE, UniqueId.of(getRateLookupIdentifierScheme(), denominator + "_" + numerator));
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    final Pair<String, String> currencies = parse(target);
    return Sets.<ValueRequirement>newHashSet(createRequirement(currencies.getFirst(), getIntermediateCurrencyISO()), createRequirement(getIntermediateCurrencyISO(), currencies.getSecond()));
  }

  private ValueSpecification createResultValueSpecification(final ComputationTarget target) {
    return new ValueSpecification(getRateLookupValueName(), target.toSpecification(), createValueProperties().get());
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    return Collections.singleton(createResultValueSpecification(target));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PRIMITIVE;
  }

}
