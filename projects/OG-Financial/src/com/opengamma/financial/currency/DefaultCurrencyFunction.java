/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.financial.property.StaticDefaultPropertyFunction;

/**
 * If no currency is explicitly requested, inject the view's default currency. This function should never be added to a dependency graph as the input will always match the output.
 */
public class DefaultCurrencyFunction extends StaticDefaultPropertyFunction {

  public DefaultCurrencyFunction(final boolean permitWithout, final String valueName) {
    super(ComputationTargetType.PORTFOLIO_NODE.or(ComputationTargetType.POSITION).or(ComputationTargetType.SECURITY), ValuePropertyNames.CURRENCY, permitWithout, valueName);
  }

  public DefaultCurrencyFunction(final boolean permitWithout, final String... valueNames) {
    super(ComputationTargetType.PORTFOLIO_NODE.or(ComputationTargetType.POSITION).or(ComputationTargetType.SECURITY), ValuePropertyNames.CURRENCY, permitWithout, valueNames);
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final String currency = getViewDefaultCurrencyISO(context);
    if (currency == null) {
      return null;
    } else {
      return Collections.singleton(currency);
    }
  }

  @Override
  public PriorityClass getPriority() {
    // Currency injection should be after conventional property injection.
    return PriorityClass.LOWEST;
  }

  /**
   * Returns the default currency as defined in a view.
   * 
   * @param context the function compilation context - this must have a view calculation configuration bound to it
   * @return the default currency or null if there is none
   */
  protected static String getViewDefaultCurrencyISO(final FunctionCompilationContext context) {
    ViewCalculationConfiguration viewCalculationConfiguration = context.getViewCalculationConfiguration();
    if (viewCalculationConfiguration == null) {
      return null;
    }
    ValueProperties defaultProperties = viewCalculationConfiguration.getDefaultProperties();
    if (defaultProperties == null) {
      return null;
    }
    final Set<String> currencies = defaultProperties.getValues(ValuePropertyNames.CURRENCY);
    if (currencies == null) {
      return null;
    }
    if (currencies.size() != 1) {
      return null;
    }
    return currencies.iterator().next();
  }

  /**
   * Flagged to inject the default currency at a higher priority to avoid other functions handling the currency omitted case.
   */
  public static class Strict extends DefaultCurrencyFunction {

    public Strict(final String valueName) {
      super(false, valueName);
    }

    public Strict(final String... valueNames) {
      super(false, valueNames);
    }

  }

  /**
   * Flagged to inject the default currency at a lower priority to allow other functions to handle the currency omitted case.
   */
  public static class Permissive extends DefaultCurrencyFunction {

    public Permissive(final String valueName) {
      super(true, valueName);
    }

    public Permissive(final String... valueNames) {
      super(true, valueNames);
    }

  }

}
