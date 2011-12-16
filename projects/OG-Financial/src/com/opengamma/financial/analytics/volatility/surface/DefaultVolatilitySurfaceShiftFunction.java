/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.financial.property.StaticDefaultPropertyFunction;

/**
 * Function to shift a volatility surface, implemented using properties and constraints.
 */
public class DefaultVolatilitySurfaceShiftFunction extends StaticDefaultPropertyFunction {

  /**
   * Property to shift all volatility surfaces.
   */
  public static final String VOLATILITY_SURFACE_SHIFT = "VOLATILITY_SURFACE_" + VolatilitySurfaceShiftFunction.SHIFT;

  public DefaultVolatilitySurfaceShiftFunction() {
    super(ComputationTargetType.SECURITY, VolatilitySurfaceShiftFunction.SHIFT, false, ValueRequirementNames.VOLATILITY_SURFACE);
  }

  @Override
  protected Set<String> getDefaultValue(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    final ViewCalculationConfiguration config = context.getViewCalculationConfiguration();
    // TODO: should probably check the target type (or other properties) so that shifts can be applied more selectively than to all surfaces
    if (config != null) {
      return config.getDefaultProperties().getValues(VOLATILITY_SURFACE_SHIFT);
    } 
    return null;
  }

}
