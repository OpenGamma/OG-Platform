/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueProperties.Builder;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * Converts a value from one currency to another, preserving all other properties.
 */
public class CurrencyConversionFunction extends AbstractFunction.NonCompiledInvoker {

  /**
   * Default value for {@code _rateLookupValueName}.
   */
  public static final String DEFAULT_LOOKUP_VALUE_NAME = "CurrencyConversion";

  /**
   * Default value for {@code _rateLookupIdentifierScheme}.
   */
  public static final String DEFAULT_LOOKUP_IDENTIFIER_SCHEME = "CurrencyISO";

  private static final Logger s_logger = LoggerFactory.getLogger(CurrencyConversionFunction.class);

  private final ComputationTargetType _targetType;
  private final Set<String> _valueNames;
  private boolean _allowViewDefaultCurrency; // = false;
  private String _rateLookupValueName = DEFAULT_LOOKUP_VALUE_NAME;
  private String _defaultCurrencyValueName = DefaultCurrencyInjectionFunction.createValueName(DEFAULT_LOOKUP_VALUE_NAME);
  private String _rateLookupIdentifierScheme = DEFAULT_LOOKUP_IDENTIFIER_SCHEME;

  public CurrencyConversionFunction(final ComputationTargetType targetType, final String valueName) {
    ArgumentChecker.notNull(targetType, "targetType");
    ArgumentChecker.notNull(valueName, "valueName");
    _targetType = targetType;
    _valueNames = Collections.singleton(valueName);
  }

  public CurrencyConversionFunction(final ComputationTargetType targetType, final String... valueNames) {
    ArgumentChecker.notNull(targetType, "targetType");
    ArgumentChecker.notEmpty(valueNames, "valueNames");
    _targetType = targetType;
    _valueNames = new HashSet<String>(Arrays.asList(valueNames));
  }

  public void setRateLookupValueName(final String rateLookupValueName) {
    ArgumentChecker.notNull(rateLookupValueName, "rateLookupValueName");
    _rateLookupValueName = rateLookupValueName;
    setDefaultCurrencyValueName(DefaultCurrencyInjectionFunction.createValueName(rateLookupValueName));
  }

  public String getRateLookupValueName() {
    return _rateLookupValueName;
  }

  private void setDefaultCurrencyValueName(final String valueName) {
    _defaultCurrencyValueName = valueName;
  }

  public String getDefaultCurrencyValueName() {
    return _defaultCurrencyValueName;
  }

  public void setRateLookupIdentifierScheme(final String rateLookupIdentifierScheme) {
    ArgumentChecker.notNull(rateLookupIdentifierScheme, "rateLookupIdentifierScheme");
    _rateLookupIdentifierScheme = rateLookupIdentifierScheme;
  }

  public String getRateLookupIdentifierScheme() {
    return _rateLookupIdentifierScheme;
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

  private ValueRequirement getInputValueRequirement(final ValueRequirement desiredValue) {
    return new ValueRequirement(desiredValue.getValueName(), desiredValue.getTargetSpecification(), desiredValue.getConstraints().copy().withAny(ValuePropertyNames.CURRENCY).get());
  }

  private ValueRequirement getInputValueRequirement(final ValueRequirement desiredValue, final String forceCurrency) {
    return new ValueRequirement(desiredValue.getValueName(), desiredValue.getTargetSpecification(), desiredValue.getConstraints().copy().withoutAny(ValuePropertyNames.CURRENCY).with(
        ValuePropertyNames.CURRENCY, forceCurrency).get());
  }

  /**
   * Divides the value by the conversion rate. Override this in a subclass for anything more elaborate - e.g. if 
   * the value is in "somethings per currency unit foo" so needs multiplying by the rate instead.
   * 
   * @param value input value to convert
   * @param conversionRate conversion rate to use
   * @return the converted value
   */
  protected double convertDouble(final double value, final double conversionRate) {
    return value / conversionRate;
  }

  protected double[] convertDoubleArray(final double[] values, final double conversionRate) {
    final double[] newValues = new double[values.length];
    for (int i = 0; i < values.length; i++) {
      newValues[i] = convertDouble(values[i], conversionRate);
    }
    return newValues;
  }

  protected DoubleLabelledMatrix1D convertDoubleLabelledMatrix1D(final DoubleLabelledMatrix1D value, final double conversionRate) {
    return new DoubleLabelledMatrix1D(value.getKeys(), value.getLabels(), convertDoubleArray(value.getValues(), conversionRate));
  }

  /**
   * Delegates off to the other convert methods depending on the type of value.
   * 
   * @param inputValue input value to convert
   * @param desiredValue requested value requirement
   * @param conversionRate conversion rate to use
   * @return the converted value
   */
  protected Object convertValue(final ComputedValue inputValue, final ValueRequirement desiredValue, final double conversionRate) {
    final Object value = inputValue.getValue();
    if (value instanceof Double) {
      return convertDouble((Double) value, conversionRate);
    } else if (value instanceof DoubleLabelledMatrix1D) {
      return convertDoubleLabelledMatrix1D((DoubleLabelledMatrix1D) value, conversionRate);
    } else {
      s_logger.warn("Can't convert {} to {}", inputValue, desiredValue);
      return null;
    }
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Set<ComputedValue> results = Sets.newHashSetWithExpectedSize(desiredValues.size());
    final Collection<ComputedValue> inputValues = inputs.getAllValues();
    desiredValueLoop:
    for (ValueRequirement desiredValue : desiredValues) {
      final ValueRequirement inputRequirement = getInputValueRequirement(desiredValue);
      final String outputCurrency = desiredValue.getConstraint(ValuePropertyNames.CURRENCY);
      for (ComputedValue inputValue : inputValues) {
        if (inputRequirement.isSatisfiedBy(inputValue.getSpecification())) {
          final String inputCurrency = inputValue.getSpecification().getProperty(ValuePropertyNames.CURRENCY);
          if (outputCurrency.equals(inputCurrency)) {
            results.add(inputValue);
          } else {
            s_logger.debug("Converting from {} to {}", inputCurrency, outputCurrency);
            final ValueRequirement rateRequirement = getCurrencyConversion(inputCurrency, outputCurrency);
            final Object rate = inputs.getValue(rateRequirement);
            if (!(rate instanceof Double)) {
              s_logger.warn("Invalid rate {} for {}", rate, rateRequirement);
              continue desiredValueLoop;
            }
            final Object converted = convertValue(inputValue, desiredValue, (Double) rate);
            if (converted != null) {
              results.add(new ComputedValue(new ValueSpecification(desiredValue, desiredValue.getConstraints()), converted));
            }
          }
          continue desiredValueLoop;
        }
      }
    }
    return results;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getType() == getTargetType();
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    if (!canApplyTo(context, target)) {
      return null;
    }
    final Set<String> possibleCurrencies = desiredValue.getConstraints().getValues(ValuePropertyNames.CURRENCY);
    if (possibleCurrencies == null) {
      s_logger.debug("Must specify a currency constraint; use DefaultCurrencyFunction instead");
      return null;
    } else if (possibleCurrencies.isEmpty()) {
      if (isAllowViewDefaultCurrency()) {
        // The original function may not have delivered a result because it had heterogeneous input currencies, so try forcing the view default
        final String defaultCurrencyISO;
        try {
          defaultCurrencyISO = DefaultCurrencyInjectionFunction.getViewDefaultCurrencyISO(context);
        } catch (IllegalStateException e) {
          s_logger.debug("Caught exception", e);
          return null;
        }
        s_logger.debug("Injecting view default currency {}", defaultCurrencyISO);
        final Set<ValueRequirement> req = new HashSet<ValueRequirement>();
        req.add(getInputValueRequirement(desiredValue, defaultCurrencyISO));
        // Eject this requirement as a hack to force the final result to be correct
        req.add(new ValueRequirement(getDefaultCurrencyValueName(), new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, target.getUniqueId()), ValueProperties.with(
            ValuePropertyNames.CURRENCY, defaultCurrencyISO).get()));
        return req;
      } else {
        s_logger.debug("Cannot satisfy a wildcard currency constraint");
        return null;
      }
    } else {
      // Actual input requirement is desired requirement with the currency wild-carded
      return Collections.singleton(getInputValueRequirement(desiredValue));
    }
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    // Maximal set of outputs is the valueNames with the infinite property set
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    if (getValueNames().size() == 1) {
      return Collections.singleton(new ValueSpecification(getValueNames().iterator().next(), targetSpec, ValueProperties.all()));
    } else {
      final Set<ValueSpecification> result = new HashSet<ValueSpecification>();
      for (String valueName : getValueNames()) {
        result.add(new ValueSpecification(valueName, targetSpec, ValueProperties.all()));
      }
      return result;
    }
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    // Resolved outputs are the inputs with the currency wild-carded - even the function ID will be preserved
    final Set<ValueSpecification> result = Sets.newHashSetWithExpectedSize(inputs.size());
    String currency = null;
    for (ValueSpecification input : inputs.keySet()) {
      if (getDefaultCurrencyValueName().equals(input.getValueName())) {
        currency = input.getProperty(ValuePropertyNames.CURRENCY);
      }
    }
    for (ValueSpecification input : inputs.keySet()) {
      if (!getDefaultCurrencyValueName().equals(input.getValueName())) {
        final Builder vpb = input.getProperties().copy();
        if (currency != null) {
          s_logger.debug("Force default {} currency for {}", currency, input);
          vpb.with(ValuePropertyNames.CURRENCY, currency);
        } else {
          s_logger.debug("Currency wild-card for {}", input);
          vpb.withAny(ValuePropertyNames.CURRENCY);
        }
        result.add(new ValueSpecification(input.getValueName(), input.getTargetSpecification(), vpb.get()));
      }
    }
    return result;
  }

  private Set<String> getCurrencies(final Set<ValueSpecification> specs) {
    final Set<String> currencies = new HashSet<String>();
    for (ValueSpecification spec : specs) {
      final Set<String> specCurrencies = spec.getProperties().getValues(ValuePropertyNames.CURRENCY);
      if ((specCurrencies == null) || (specCurrencies.size() != 1)) {
        return null;
      }
      currencies.add(specCurrencies.iterator().next());
    }
    return currencies;
  }

  private ValueRequirement getCurrencyConversion(final String fromCurrency, final String toCurrency) {
    return new ValueRequirement(getRateLookupValueName(), new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, UniqueId.of(getRateLookupIdentifierScheme(), fromCurrency + "_"
        + toCurrency)));
  }

  @Override
  public Set<ValueRequirement> getAdditionalRequirements(final FunctionCompilationContext context, final ComputationTarget target, final Set<ValueSpecification> inputs,
      final Set<ValueSpecification> outputs) {
    s_logger.debug("FX requirements for {} -> {}", inputs, outputs);
    final Set<String> inputCurrencies = getCurrencies(inputs);
    if (inputCurrencies == null) {
      return null;
    }
    final Set<String> outputCurrencies = getCurrencies(outputs);
    if (outputCurrencies == null) {
      return null;
    }
    if ((inputCurrencies.size() == 1) && (outputCurrencies.size() == 1)) {
      final String input = inputCurrencies.iterator().next();
      final String output = outputCurrencies.iterator().next();
      if (input.equals(output)) {
        return Collections.emptySet();
      } else {
        return Collections.singleton(getCurrencyConversion(input, output));
      }
    } else {
      // NOTE 2010-10-27 Andrew -- The cross product is not optimal for all input/output possibilities (e.g. IN={A1,B2}, OUT={A3,B4} gives the unused 1->4 and 2->3) but the current graph typically
      // won't produce such complex nodes
      final Set<ValueRequirement> rateLookups = Sets.newHashSetWithExpectedSize(inputCurrencies.size() * outputCurrencies.size());
      for (String inputCurrency : inputCurrencies) {
        for (String outputCurrency : outputCurrencies) {
          rateLookups.add(getCurrencyConversion(inputCurrency, outputCurrency));
        }
      }
      return rateLookups;
    }
  }

  @Override
  public ComputationTargetType getTargetType() {
    return _targetType;
  }

}
