/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class CurrencyPairsDefaults extends DefaultPropertyFunction {
  private final String _name;

  public CurrencyPairsDefaults(final String name) {
    super(ComputationTargetType.NULL, true);
    ArgumentChecker.notNull(name, "name");
    _name = name;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    defaults.addValuePropertyName(ValueRequirementNames.CURRENCY_PAIRS, CurrencyPairsFunction.CURRENCY_PAIRS_NAME);
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if (CurrencyPairsFunction.CURRENCY_PAIRS_NAME.equals(propertyName)) {
      return Collections.singleton(_name);
    }
    return null;
  }

}
