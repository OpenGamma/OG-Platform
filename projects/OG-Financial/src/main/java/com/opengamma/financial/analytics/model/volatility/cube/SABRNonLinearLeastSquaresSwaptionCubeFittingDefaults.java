/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.cube;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class SABRNonLinearLeastSquaresSwaptionCubeFittingDefaults extends DefaultPropertyFunction {
  private final String _currency;
  private final String _cube;

  public SABRNonLinearLeastSquaresSwaptionCubeFittingDefaults(final String currency, final String cube) {
    super(ComputationTargetType.CURRENCY, true);
    ArgumentChecker.notNull(currency, "currency");
    ArgumentChecker.notNull(cube, "cube");
    _currency = currency;
    _cube = cube;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    defaults.addValuePropertyName(ValueRequirementNames.SABR_SURFACES, ValuePropertyNames.CURRENCY);
    defaults.addValuePropertyName(ValueRequirementNames.SABR_SURFACES, ValuePropertyNames.CUBE);
    defaults.addValuePropertyName(ValueRequirementNames.VOLATILITY_CUBE_FITTED_POINTS, ValuePropertyNames.CURRENCY);
    defaults.addValuePropertyName(ValueRequirementNames.VOLATILITY_CUBE_FITTED_POINTS, ValuePropertyNames.CUBE);
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if (ValuePropertyNames.CURRENCY.equals(propertyName)) {
      return Collections.singleton(_currency);
    }
    if (ValuePropertyNames.CUBE.equals(propertyName)) {
      return Collections.singleton(_cube);
    }
    return null;
  }

}
