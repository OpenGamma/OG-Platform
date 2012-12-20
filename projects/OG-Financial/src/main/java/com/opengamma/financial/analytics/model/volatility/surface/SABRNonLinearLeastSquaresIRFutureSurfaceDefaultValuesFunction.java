/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class SABRNonLinearLeastSquaresIRFutureSurfaceDefaultValuesFunction extends DefaultPropertyFunction {
  private static final String[] s_valueNames = new String[] {
    ValueRequirementNames.SABR_SURFACES,
    ValueRequirementNames.VOLATILITY_SURFACE_FITTED_POINTS};
  private final String _surfaceDefinitionName;

  public SABRNonLinearLeastSquaresIRFutureSurfaceDefaultValuesFunction(final String surfaceDefinitionName) {
    super(ComputationTargetType.PRIMITIVE, true);
    ArgumentChecker.notNull(surfaceDefinitionName, "surface definition name");
    _surfaceDefinitionName = surfaceDefinitionName;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueName : s_valueNames) {
      defaults.addValuePropertyName(valueName, ValuePropertyNames.SURFACE);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if (ValuePropertyNames.SURFACE.equals(propertyName)) {
      return Collections.singleton(_surfaceDefinitionName);
    }
    return null;
  }
}
