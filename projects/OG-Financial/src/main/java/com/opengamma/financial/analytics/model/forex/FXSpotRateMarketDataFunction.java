/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import java.util.Collections;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.currency.CurrencyMatrixSpotSourcingFunction;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 * Function to source an FX Spot Rate based on a quoting convention. It is up to the consumer of the value to recognize the convention and interpret the value accordingly.
 * <p>
 * This function is implemented by querying a value from the CurrencyMatrix
 */
public class FXSpotRateMarketDataFunction extends AbstractFunction.NonCompiledInvoker {

  /**
   * The property name that indicates the convention document that was used.
   */
  private static final String CONVENTION_NAME_PROPERTY = "ConventionConfig";

  /**
   * The property name that indicates the quoting convention, for example "GBP/USD".
   */
  private static final String QUOTING_CONVENTION_PROPERTY = "ConventionQuoting";

  private final String _convention;
  private CurrencyPairs _currencyPairs;

  public FXSpotRateMarketDataFunction() {
    this(CurrencyPairs.DEFAULT_CURRENCY_PAIRS);
  }

  public FXSpotRateMarketDataFunction(final String currencyPairsConvention) {
    _convention = currencyPairsConvention;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    _currencyPairs = OpenGammaCompilationContext.getCurrencyPairsSource(context).getCurrencyPairs(_convention);
    if (_currencyPairs == null) {
      throw new UnsupportedOperationException("No convention called " + _convention + " found");
    }
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.UNORDERED_CURRENCY_PAIR;
  }

  protected ValueProperties.Builder createValueProperties(final ComputationTarget target) {
    final ValueProperties.Builder properties = super.createValueProperties();
    properties.with(CONVENTION_NAME_PROPERTY, _convention);
    final UnorderedCurrencyPair unordered = UnorderedCurrencyPair.of(target.getUniqueId());
    final CurrencyPair ordered = _currencyPairs.getCurrencyPair(unordered.getFirstCurrency(), unordered.getSecondCurrency());
    if (ordered == null) {
      return null;
    }
    properties.with(QUOTING_CONVENTION_PROPERTY, ordered.toString());
    return properties;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties.Builder properties = createValueProperties(target);
    if (properties == null) {
      return null;
    }
    return ImmutableSet.of(new ValueSpecification(ValueRequirementNames.SPOT_RATE, target.toSpecification(), properties.get()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final UnorderedCurrencyPair unordered = UnorderedCurrencyPair.of(target.getUniqueId());
    final CurrencyPair ordered = _currencyPairs.getCurrencyPair(unordered.getFirstCurrency(), unordered.getSecondCurrency());
    return Collections.singleton(CurrencyMatrixSpotSourcingFunction.getConversionRequirement(ordered.getCounter(), ordered.getBase()));
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final ComputedValue inputValue = inputs.getAllValues().iterator().next();
    final ValueSpecification outputSpec = new ValueSpecification(ValueRequirementNames.SPOT_RATE, target.toSpecification(), desiredValue.getConstraints());
    return ImmutableSet.of(new ComputedValue(outputSpec, inputValue.getValue()));
  }

}
