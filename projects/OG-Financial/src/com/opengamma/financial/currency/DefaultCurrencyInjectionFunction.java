/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
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
 * <p>
 * If a function makes a requirement for the default currency value with a specific currency constraint, this function
 * will match. This allows the function to pass a currency from the {@link CompiledFunctionDefinition#getRequirements}
 * function to {@link CompiledFunctionDefinition#getResults} without maintaining any internal state. The rewritten result
 * then matches the other input requirement and both the injection and original conversion function are removed from
 * the dependency graph.
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

  /**
   * Returns the set of results. This is the "default currency value" and a constraint of "any" currency. This will be composed
   * against the specific currency required by the peer function. Note that this does not specify the view's default currency -
   * it is the responsibility of the peer function to request the default in the value requirement satisfied by this function.
   * This allows the function to be used to inject currencies other than the view's default to functions that require it.
   * 
   * @param context the function compilation context, not null
   * @param target the target, not null
   * @return a singleton describing the default currency value and a constraint of "any" currency, not null
   */
  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(getDefaultCurrencyValueName(), target.toSpecification(), createValueProperties().withAny(ValuePropertyNames.CURRENCY).get()));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PRIMITIVE;
  }

  protected static String getViewDefaultCurrencyISO(final FunctionCompilationContext context) {
    ViewCalculationConfiguration viewCalculationConfiguration = context.getViewCalculationConfiguration();
    if (viewCalculationConfiguration == null) {
      throw new IllegalStateException("No view calculation configuration");
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

