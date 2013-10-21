/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
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
import com.opengamma.financial.analytics.timeseries.DateConstraint;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 * Function to source FX rates (live, historical, latest historical) based on a quoting convention. It is up to the consumer of the value to recognize the convention and interpret the value
 * accordingly.
 * <p>
 * This function is implemented by querying a value on an ordered currency pair, which will typically be handled by the CurrencyMatrix based functions.
 */
public class ConventionBasedFXRateFunction extends AbstractFunction {

  /**
   * The property name that indicates the convention document that was used.
   */
  private static final String CONVENTION_NAME_PROPERTY = "ConventionConfig";

  /**
   * The property name that indicates the quoting convention, for example "GBP/USD".
   */
  private static final String QUOTING_CONVENTION_PROPERTY = "ConventionQuoting";

  private final String _convention;

  public ConventionBasedFXRateFunction() {
    this(CurrencyPairs.DEFAULT_CURRENCY_PAIRS);
  }

  public ConventionBasedFXRateFunction(final String currencyPairsConvention) {
    _convention = currencyPairsConvention;
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    final CurrencyPairs currencyPairs = OpenGammaCompilationContext.getCurrencyPairsSource(context).getCurrencyPairs(_convention);
    if (currencyPairs == null) {
      throw new UnsupportedOperationException("No convention called " + _convention + " found");
    }
    return new Compiled(currencyPairs);
  }

  /**
   * The compiled form.
   */
  protected class Compiled extends AbstractInvokingCompiledFunction {

    private final CurrencyPairs _currencyPairs;

    public Compiled(final CurrencyPairs currencyPairs) {
      _currencyPairs = currencyPairs;
    }

    @SuppressWarnings("synthetic-access")
    protected ValueProperties.Builder createValueProperties(final ComputationTarget target) {
      final ValueProperties.Builder properties = ConventionBasedFXRateFunction.this.createValueProperties();
      properties.with(CONVENTION_NAME_PROPERTY, _convention);
      final UnorderedCurrencyPair unordered = UnorderedCurrencyPair.of(target.getUniqueId());
      if (unordered.getFirstCurrency().equals(unordered.getSecondCurrency())) {
        return null;
      }
      final CurrencyPair ordered = _currencyPairs.getCurrencyPair(unordered.getFirstCurrency(), unordered.getSecondCurrency());
      if (ordered == null) {
        return null;
      }
      properties.with(QUOTING_CONVENTION_PROPERTY, ordered.toString());
      return properties;
    }

    private ValueSpecification createSpotRateResult(final ComputationTargetSpecification targetSpec, final ValueProperties properties) {
      return new ValueSpecification(ValueRequirementNames.SPOT_RATE, targetSpec, properties);
    }

    private ValueSpecification createHistoricalTimeSeriesResult(final ComputationTargetSpecification targetSpec, ValueProperties properties) {
      properties = properties.copy()
          .withAny(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY)
          .with(HistoricalTimeSeriesFunctionUtils.INCLUDE_START_PROPERTY, HistoricalTimeSeriesFunctionUtils.NO_VALUE, HistoricalTimeSeriesFunctionUtils.YES_VALUE)
          .withAny(HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY)
          .with(HistoricalTimeSeriesFunctionUtils.INCLUDE_END_PROPERTY, HistoricalTimeSeriesFunctionUtils.NO_VALUE, HistoricalTimeSeriesFunctionUtils.YES_VALUE).get();
      return new ValueSpecification(ValueRequirementNames.HISTORICAL_FX_TIME_SERIES, targetSpec, properties);
    }

    private ValueSpecification createTimeSeriesLatestResult(final ComputationTargetSpecification targetSpec, final ValueProperties properties) {
      return new ValueSpecification(ValueRequirementNames.HISTORICAL_TIME_SERIES_LATEST, targetSpec, properties);
    }

    // CompiledFunctionDefinition

    @Override
    public ComputationTargetType getTargetType() {
      return ComputationTargetType.UNORDERED_CURRENCY_PAIR;
    }

    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
      final ValueProperties.Builder propertiesBuilder = createValueProperties(target);
      if (propertiesBuilder == null) {
        return null;
      }
      final ValueProperties properties = propertiesBuilder.get();
      final ComputationTargetSpecification targetSpec = target.toSpecification();
      return ImmutableSet.of(createSpotRateResult(targetSpec, properties), createHistoricalTimeSeriesResult(targetSpec, properties), createTimeSeriesLatestResult(targetSpec, properties));
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
      final ValueProperties constraints;
      if (desiredValue.getConstraints().getProperties() != null) {
        final ValueProperties.Builder constraintsBuilder = ValueProperties.builder();
        for (final String constraintName : desiredValue.getConstraints().getProperties()) {
          if (ValuePropertyNames.FUNCTION.equals(constraintName) || constraintName.startsWith(ValuePropertyNames.OUTPUT_RESERVED_PREFIX) || QUOTING_CONVENTION_PROPERTY.equals(constraintName)) {
            continue;
          }
          final Set<String> values = desiredValue.getConstraints().getValues(constraintName);
          if (values.isEmpty()) {
            constraintsBuilder.withAny(constraintName);
          } else {
            constraintsBuilder.with(constraintName, values);
          }
          if (desiredValue.getConstraints().isOptional(constraintName)) {
            constraintsBuilder.withOptional(constraintName);
          }
        }
        constraints = constraintsBuilder.get();
      } else {
        constraints = ValueProperties.none();
      }
      final UnorderedCurrencyPair unordered = UnorderedCurrencyPair.of(target.getUniqueId());
      final CurrencyPair ordered = _currencyPairs.getCurrencyPair(unordered.getFirstCurrency(), unordered.getSecondCurrency());
      return Collections.singleton(new ValueRequirement(desiredValue.getValueName(), CurrencyPair.TYPE.specification(ordered), constraints));
    }

    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
      final Set<ValueSpecification> results = Sets.newHashSetWithExpectedSize(inputs.size());
      final ComputationTargetSpecification targetSpec = target.toSpecification();
      for (final ValueSpecification input : inputs.keySet()) {
        final ValueProperties.Builder properties = createValueProperties(target);
        final ValueProperties inputProperties = input.getProperties();
        for (final String propertyName : inputProperties.getProperties()) {
          if (!ValuePropertyNames.FUNCTION.equals(propertyName)) {
            final Set<String> values = inputProperties.getValues(propertyName);
            if (values.isEmpty()) {
              properties.withAny(propertyName);
            } else {
              properties.with(propertyName, values);
            }
          }
        }
        results.add(new ValueSpecification(input.getValueName(), targetSpec, properties.get()));
      }
      return results;
    }

    // FunctionInvoker

    @Override
    public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
      final Set<ComputedValue> results = Sets.newHashSetWithExpectedSize(desiredValues.size());
      for (final ValueRequirement desiredValue : desiredValues) {
        final Object input = inputs.getValue(desiredValue.getValueName());
        results.add(new ComputedValue(new ValueSpecification(desiredValue.getValueName(), target.toSpecification(), desiredValue.getConstraints()), input));
      }
      return results;
    }

  }

  public static ValueRequirement getSpotRateRequirement(final UnorderedCurrencyPair currencies) {
    return new ValueRequirement(ValueRequirementNames.SPOT_RATE, ComputationTargetType.UNORDERED_CURRENCY_PAIR.specification(currencies));
  }

  public static ValueRequirement getSpotRateRequirement(final Currency currency1, final Currency currency2) {
    return getSpotRateRequirement(UnorderedCurrencyPair.of(currency1, currency2));
  }

  public static ValueRequirement getHistoricalTimeSeriesRequirement(final UnorderedCurrencyPair currencies) {
    return getHistoricalTimeSeriesRequirement(currencies, DateConstraint.NULL, true, DateConstraint.VALUATION_TIME, true);
  }

  public static ValueRequirement getHistoricalTimeSeriesRequirement(final Currency currency1, final Currency currency2) {
    return getHistoricalTimeSeriesRequirement(UnorderedCurrencyPair.of(currency1, currency2));
  }

  public static ValueRequirement getHistoricalTimeSeriesRequirement(final UnorderedCurrencyPair currencies, final DateConstraint startDate, final boolean includeStart, final DateConstraint endDate,
      final boolean includeEnd) {
    return new ValueRequirement(ValueRequirementNames.HISTORICAL_FX_TIME_SERIES, ComputationTargetType.UNORDERED_CURRENCY_PAIR.specification(currencies), HistoricalTimeSeriesFunctionUtils
        .htsConstraints(ValueProperties.builder(), startDate, includeStart, endDate, includeEnd).get());
  }

  public static ValueRequirement getHistoricalTimeSeriesRequirement(final Currency currency1, final Currency currency2, final DateConstraint startDate, final boolean includeStart,
      final DateConstraint endDate, final boolean includeEnd) {
    return getHistoricalTimeSeriesRequirement(UnorderedCurrencyPair.of(currency1, currency2), startDate, includeStart, endDate, includeEnd);
  }

  public static ValueRequirement getLatestHistoricalRequirement(final UnorderedCurrencyPair currencies) {
    return new ValueRequirement(ValueRequirementNames.HISTORICAL_TIME_SERIES_LATEST, ComputationTargetType.UNORDERED_CURRENCY_PAIR.specification(currencies));
  }

  public static ValueRequirement getLatestHistoricalRequirement(final Currency currency1, final Currency currency2) {
    return getLatestHistoricalRequirement(UnorderedCurrencyPair.of(currency1, currency2));
  }

}
