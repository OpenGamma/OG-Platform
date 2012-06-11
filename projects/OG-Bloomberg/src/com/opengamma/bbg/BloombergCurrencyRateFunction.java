/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg;

import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.currency.CurrencyConversionFunction;
import com.opengamma.id.UniqueId;
import com.opengamma.util.tuple.Pair;

/**
 * Maps the codes used by the generic currency conversion functions to Bloomberg ticker lookups
 * which will then be provided by the live data integration (or a user override).
 */
public class BloombergCurrencyRateFunction extends AbstractFunction.NonCompiledInvoker {

  /**
   * Pattern for identifying specific codes.
   */
  private static final Pattern s_validate = Pattern.compile("[A-Z]{3}_[A-Z]{3}");
  /**
   * The USD reference currency.
   */
  private static final String REFERENCE_CURRENCY_ISO = "USD";

  /**
   * The rate lookup scheme.
   */
  private String _rateLookupIdentifierScheme = CurrencyConversionFunction.DEFAULT_LOOKUP_IDENTIFIER_SCHEME;
  /**
   * The rate lookup value name.
   */
  private String _rateLookupValueName = CurrencyConversionFunction.DEFAULT_LOOKUP_VALUE_NAME;

  //-------------------------------------------------------------------------
  /**
   * Gets the rate lookup scheme.
   * 
   * @return the scheme
   */
  public String getRateLookupIdentifierScheme() {
    return _rateLookupIdentifierScheme;
  }

  /**
   * Sets the rate lookup scheme.
   * 
   * @param rateLookupIdentifierScheme  the scheme
   */
  public void setRateLookupIdentifierScheme(final String rateLookupIdentifierScheme) {
    _rateLookupIdentifierScheme = rateLookupIdentifierScheme;
  }

  /**
   * Sets the rate lookup value name.
   * 
   * @return  the value name
   */
  public String getRateLookupValueName() {
    return _rateLookupValueName;
  }

  /**
   * Sets the rate lookup value name.
   * 
   * @param rateLookupValueName  the value name
   */
  public void setRateLookupValueName(final String rateLookupValueName) {
    _rateLookupValueName = rateLookupValueName;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    if (target.getType() != ComputationTargetType.PRIMITIVE) {
      return false;
    }
    if (getRateLookupIdentifierScheme().equals(target.getUniqueId().getScheme()) == false) {
      return false;
    }
    if (s_validate.matcher(target.getUniqueId().getValue()).matches() == false) {
      return false;
    }
    final Pair<String, String> currencies = parse(target);
    return REFERENCE_CURRENCY_ISO.equals(currencies.getFirst());
  }

  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs,
      ComputationTarget target, Set<ValueRequirement> desiredValues) {
    final Object value = inputs.getValue(MarketDataRequirementNames.MARKET_VALUE);
    if (value == null) {
      throw new IllegalArgumentException(MarketDataRequirementNames.MARKET_VALUE + " for " + target + " not available");
    }
    if (value instanceof Double == false) {
      throw new IllegalArgumentException(MarketDataRequirementNames.MARKET_VALUE + " for " + target
          + " is not a double - " + value);
    }
    return Collections.singleton(new ComputedValue(createResultValueSpecification(target), value));
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target,
      ValueRequirement desiredValue) {
    return Collections.singleton(createLiveDataRequirement(target));
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    return Collections.singleton(createResultValueSpecification(target));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PRIMITIVE;
  }

  //-------------------------------------------------------------------------
  private ValueRequirement createLiveDataRequirement(final ComputationTarget target) {
    final Pair<String, String> currencies = parse(target);
    return new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.PRIMITIVE,
        UniqueId.of(ExternalSchemes.BLOOMBERG_TICKER.getName(), currencies.getSecond() + " Curncy"));
  }

  private ValueSpecification createResultValueSpecification(final ComputationTarget target) {
    return new ValueSpecification(getRateLookupValueName(), target.toSpecification(), createValueProperties().get());
  }

  private static Pair<String, String> parse(final ComputationTarget target) {
    final int underscore = target.getUniqueId().getValue().indexOf('_');
    final String numerator = target.getUniqueId().getValue().substring(0, underscore);
    final String denominator = target.getUniqueId().getValue().substring(underscore + 1);
    return Pair.of(numerator, denominator);
  }

}
