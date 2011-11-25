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
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.property.StaticDefaultPropertyFunction;

/**
 * If no currency is explicitly requested, inject the view's default currency. This function should never
 * be added to a dependency graph as the input will always match the output.
 */
public class DefaultCurrencyFunction extends StaticDefaultPropertyFunction {

  public DefaultCurrencyFunction(final ComputationTargetType targetType, final boolean permitWithout, final String valueName) {
    super(targetType, ValuePropertyNames.CURRENCY, permitWithout, valueName);
  }

  public DefaultCurrencyFunction(final ComputationTargetType targetType, final boolean permitWithout, final String... valueNames) {
    super(targetType, ValuePropertyNames.CURRENCY, permitWithout, valueNames);
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    try {
      return Collections.singleton(DefaultCurrencyInjectionFunction.getViewDefaultCurrencyISO(context));
    } catch (IllegalStateException e) {
      return null;
    }
  }

}
