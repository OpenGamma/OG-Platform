/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import java.util.Collections;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
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
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.util.ArgumentChecker;

/**
 * Dummy function to work with {@link CurrencyConversionFunction} to reduce a wild-card currency constraint to the
 * default currency. References to this function from a dependency graph are only temporary to force value specification
 * resolution and dropped during construction.
 */
public class DefaultCurrencyInjectionFunction extends AbstractFunction.NonCompiledInvoker {

  /**
   * Default currency suffix.
   */
  protected static final String DEFAULT_CURRENCY_SUFFIX = "Default";

  private String _defaultCurrencyValueName = createValueName(CurrencyConversionFunction.DEFAULT_LOOKUP_VALUE_NAME);

  public void setRateLookupValueName(final String rateLookupValueName) {
    ArgumentChecker.notNull(rateLookupValueName, "rateLookupValueName");
    setDefaultCurrencyValueName(createValueName(rateLookupValueName));
  }

  protected String getDefaultCurrencyValueName() {
    return _defaultCurrencyValueName;
  }

  protected void setDefaultCurrencyValueName(final String valueName) {
    _defaultCurrencyValueName = valueName;
  }

  protected static String createValueName(final String rateLookup) {
    return rateLookup + DEFAULT_CURRENCY_SUFFIX;
  }

  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) {
    throw new IllegalStateException("This function should never be executed");
  }

  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    return ComputationTargetType.PRIMITIVE.equals(target.getType());
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    return Collections.emptySet();
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    final String defaultCurrencyISO = getViewDefaultCurrencyISO(context);
    return Collections.singleton(new ValueSpecification(getDefaultCurrencyValueName(), target.toSpecification(), createValueProperties().with(ValuePropertyNames.CURRENCY, defaultCurrencyISO).get()));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PRIMITIVE;
  }

  protected static String getViewDefaultCurrencyISO(final FunctionCompilationContext context) {
    ViewCalculationConfiguration viewCalculationConfiguration = context.getViewCalculationConfiguration();
    if (viewCalculationConfiguration == null) {
      throw new OpenGammaRuntimeException("View calculation configuration not found in function compilation context");
    }
    ValueProperties defaultProperties = viewCalculationConfiguration.getDefaultProperties();
    if (defaultProperties == null) {
      throw new IllegalStateException("No default properties found for the view calculation configuration");
    }
    final Set<String> currencies = defaultProperties.getValues(ValuePropertyNames.CURRENCY);
    if (currencies == null) {
      throw new IllegalStateException("Default currency not set for the view calculation configuration");
    }
    if (currencies.size() != 1) {
      throw new IllegalStateException("Invalid default currency - " + currencies);
    }
    return currencies.iterator().next();
  }

}

