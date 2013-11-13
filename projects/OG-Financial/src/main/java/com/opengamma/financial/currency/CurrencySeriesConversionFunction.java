/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.google.common.annotations.VisibleForTesting;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueProperties.Builder;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.LabelledObjectMatrix1D;
import com.opengamma.financial.analytics.TenorLabelledLocalDateDoubleTimeSeriesMatrix1D;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;

/**
 * Converts a series of values from one currency to another, preserving all other properties.
 */
public class CurrencySeriesConversionFunction extends AbstractFunction.NonCompiledInvoker {

  private static final String CURRENCY_INJECTION_PROPERTY = ValuePropertyNames.OUTPUT_RESERVED_PREFIX + "Currency";

  private static final String CONVERSION_METHOD_VALUE = "Series";

  private static final Logger s_logger = LoggerFactory.getLogger(CurrencySeriesConversionFunction.class);

  private static final ComputationTargetType TYPE = ComputationTargetType.PORTFOLIO_NODE.or(ComputationTargetType.POSITION).or(ComputationTargetType.SECURITY).or(ComputationTargetType.TRADE);

  private final Set<String> _valueNames;
  private boolean _allowViewDefaultCurrency; // = false;

  public CurrencySeriesConversionFunction(final String valueName) {
    ArgumentChecker.notNull(valueName, "valueName");
    _valueNames = Collections.singleton(valueName);
  }

  public CurrencySeriesConversionFunction(final String... valueNames) {
    ArgumentChecker.notEmpty(valueNames, "valueNames");
    _valueNames = new HashSet<String>(Arrays.asList(valueNames));
  }

  protected Set<String> getValueNames() {
    return _valueNames;
  }

  public void setAllowViewDefaultCurrency(final boolean allowViewDefaultCurrency) {
    _allowViewDefaultCurrency = allowViewDefaultCurrency;
  }

  public boolean isAllowViewDefaultCurrency() {
    return _allowViewDefaultCurrency;
  }

  private ValueRequirement getInputValueRequirement(final ComputationTargetSpecification targetSpec, final ValueRequirement desiredValue) {
    Builder properties = desiredValue.getConstraints().copy()
        .withoutAny(CURRENCY_INJECTION_PROPERTY)
        .withoutAny(ValuePropertyNames.CONVERSION_METHOD)
        .withAny(ValuePropertyNames.CURRENCY);
    return new ValueRequirement(desiredValue.getValueName(), targetSpec, properties.get());
  }

  private ValueRequirement getInputValueRequirement(final ComputationTargetSpecification targetSpec, final ValueRequirement desiredValue, final String forceCurrency) {
    return new ValueRequirement(desiredValue.getValueName(), targetSpec, desiredValue.getConstraints().copy().withoutAny(ValuePropertyNames.CURRENCY).with(
        ValuePropertyNames.CURRENCY, forceCurrency).withOptional(CURRENCY_INJECTION_PROPERTY).get());
  }

  protected DoubleTimeSeries<LocalDate> convertDouble(final double value, final DoubleTimeSeries<LocalDate> conversionRates) {
    return conversionRates.divide(value).reciprocal();
  }

  protected Double convertDouble(final double value, final double conversionRate) {
    return value / conversionRate;
  }

  protected DoubleTimeSeries<LocalDate> convertTimeSeries(final DoubleTimeSeries<LocalDate> values, final DoubleTimeSeries<LocalDate> conversionRates) {
    return values.divide(conversionRates);
  }

  @VisibleForTesting
  /* package */ TenorLabelledLocalDateDoubleTimeSeriesMatrix1D convertLabelledMatrix(final LabelledObjectMatrix1D<Tenor, LocalDateDoubleTimeSeries, Period> values, final DoubleTimeSeries<LocalDate> conversionRates) {
    LocalDateDoubleTimeSeries[] convertedValues = new LocalDateDoubleTimeSeries[values.size()];
    for (int i = 0; i < values.size(); i++) {
      convertedValues[i] = (LocalDateDoubleTimeSeries) convertTimeSeries(values.getValues()[i], conversionRates);
    }
    return new TenorLabelledLocalDateDoubleTimeSeriesMatrix1D(values.getKeys(), values.getLabels(), values.getLabelsTitle(), convertedValues, values.getValuesTitle());
  }

  protected DoubleTimeSeries<LocalDate> convertTimeSeries(final DoubleTimeSeries<LocalDate> values, final double conversionRate) {
    return values.divide(conversionRate);
  }

  @SuppressWarnings("unchecked")
  protected Object convertValue(final ComputedValue inputValue, final ValueRequirement desiredValue, final DoubleTimeSeries<LocalDate> conversionRates) {
    final Object value = inputValue.getValue();
    if (value instanceof Double) {
      return convertDouble((Double) value, conversionRates);
    } else if (value instanceof DoubleTimeSeries) {
      // TODO: Note the unchecked cast. We'll either get a zero intersection and empty result if the rates aren't the same type or a class cast exception.
      return convertTimeSeries((DoubleTimeSeries) value, conversionRates);
    } else if (value instanceof TenorLabelledLocalDateDoubleTimeSeriesMatrix1D) {
      // Try to make this more generic
      return convertLabelledMatrix((TenorLabelledLocalDateDoubleTimeSeriesMatrix1D) value, conversionRates);
    } else {
      s_logger.error("Can't convert object with type {} to {}", inputValue.getValue().getClass(), desiredValue);
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  protected Object convertValue(final ComputedValue inputValue, final ValueRequirement desiredValue, final Double conversionRate) {
    final Object value = inputValue.getValue();
    if (value instanceof Double) {
      return convertDouble((Double) value, conversionRate);
    } else if (value instanceof DoubleTimeSeries) {
      return convertTimeSeries((DoubleTimeSeries) value, conversionRate);
    } else {
      s_logger.error("Can't convert object with type {} to {}", inputValue.getValue().getClass(), desiredValue);
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    ComputedValue inputValue = null;
    DoubleTimeSeries<LocalDate> exchangeRates = null;
    Double exchangeRate = null;
    for (final ComputedValue input : inputs.getAllValues()) {
      if (ValueRequirementNames.HISTORICAL_FX_TIME_SERIES.equals(input.getSpecification().getValueName())) {
        if (input.getValue() instanceof Double) {
          // Note: The rate might be a DOUBLE if the matrix being used has a hard coded constant in it. Improbable for a time-series conversion, but possible.
          exchangeRate = (Double) input.getValue();
        } else if (input.getValue() instanceof DoubleTimeSeries) {
          // TODO: Note the unchecked cast. We'll either get a zero intersection and empty result if the values aren't the same type or a class cast exception.
          exchangeRates = (DoubleTimeSeries) input.getValue();
        } else {
          return null;
        }
      } else {
        inputValue = input;
      }
    }
    if (inputValue == null) {
      return null;
    }
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String outputCurrency = desiredValue.getConstraint(ValuePropertyNames.CURRENCY);
    final String inputCurrency = inputValue.getSpecification().getProperty(ValuePropertyNames.CURRENCY);
    if (outputCurrency.equals(inputCurrency)) {
      // Don't think this should happen
      return Collections.singleton(inputValue);
    } else {
      s_logger.debug("Converting from {} to {}", inputCurrency, outputCurrency);
      final Object converted;
      if (exchangeRates != null) {
        converted = convertValue(inputValue, desiredValue, exchangeRates);
      } else if (exchangeRate != null) {
        converted = convertValue(inputValue, desiredValue, exchangeRate);
      } else {
        return null;
      }
      if (converted != null) {
        return Collections.singleton(new ComputedValue(new ValueSpecification(desiredValue.getValueName(), target.toSpecification(), desiredValue.getConstraints()), converted));
      } else {
        return null;
      }
    }
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<String> possibleCurrencies = desiredValue.getConstraints().getValues(ValuePropertyNames.CURRENCY);
    if (possibleCurrencies == null) {
      s_logger.debug("Must specify a currency constraint; use DefaultCurrencyFunction instead");
      return null;
    } else if (possibleCurrencies.isEmpty()) {
      if (isAllowViewDefaultCurrency()) {
        // The original function may not have delivered a result because it had heterogeneous input currencies, so try forcing the view default
        final String defaultCurrencyISO = DefaultCurrencyFunction.getViewDefaultCurrencyISO(context);
        if (defaultCurrencyISO == null) {
          s_logger.debug("No default currency from the view to inject");
          return null;
        }
        s_logger.debug("Injecting view default currency {}", defaultCurrencyISO);
        return Collections.singleton(getInputValueRequirement(target.toSpecification(), desiredValue, defaultCurrencyISO));
      } else {
        s_logger.debug("Cannot satisfy a wildcard currency constraint");
        return null;
      }
    } else {
      // Actual input requirement is desired requirement with the currency wild-carded
      return Collections.singleton(getInputValueRequirement(target.toSpecification(), desiredValue));
    }
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    // Maximal set of outputs is the valueNames with the infinite property set
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    if (getValueNames().size() == 1) {
      return Collections.singleton(new ValueSpecification(getValueNames().iterator().next(), targetSpec, ValueProperties.all()));
    }
    final Set<ValueSpecification> result = new HashSet<ValueSpecification>();
    for (final String valueName : getValueNames()) {
      result.add(new ValueSpecification(valueName, targetSpec, ValueProperties.all()));
    }
    return result;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final Map.Entry<ValueSpecification, ValueRequirement> input = inputs.entrySet().iterator().next();
    if (input.getValue().getConstraints().getValues(CURRENCY_INJECTION_PROPERTY) == null) {
      // Resolved output is the input with the currency wild-carded, and the function ID the same (this is so that after composition the node might
      // be removed from the graph)
      final ValueSpecification value = input.getKey();
      Builder properties = value.getProperties().copy()
          .withAny(ValuePropertyNames.CURRENCY)
          .with(ValuePropertyNames.CONVERSION_METHOD, CONVERSION_METHOD_VALUE);
      return Collections.singleton(new ValueSpecification(value.getValueName(), value.getTargetSpecification(), properties.get()));
    }
    // The input was requested with the converted currency, so return the same specification to remove this node from the graph
    return Collections.singleton(input.getKey());
  }

  private String getCurrency(final Collection<ValueSpecification> specifications) {
    final ValueSpecification specification = specifications.iterator().next();
    final Set<String> currencies = specification.getProperties().getValues(ValuePropertyNames.CURRENCY);
    if ((currencies == null) || (currencies.size() != 1)) {
      return null;
    }
    return currencies.iterator().next();
  }

  @Override
  public Set<ValueRequirement> getAdditionalRequirements(final FunctionCompilationContext context, final ComputationTarget target, final Set<ValueSpecification> inputs,
      final Set<ValueSpecification> outputs) {
    s_logger.debug("FX requirements for {} -> {}", inputs, outputs);
    final String inputCurrency = getCurrency(inputs);
    if (inputCurrency == null) {
      return null;
    }
    final String outputCurrency = getCurrency(outputs);
    if (outputCurrency == null) {
      return null;
    }
    if (inputCurrency.equals(outputCurrency)) {
      return Collections.emptySet();
    }
    return Collections.singleton(CurrencyMatrixSeriesSourcingFunction.getConversionRequirement(inputCurrency, outputCurrency));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return TYPE;
  }

}
