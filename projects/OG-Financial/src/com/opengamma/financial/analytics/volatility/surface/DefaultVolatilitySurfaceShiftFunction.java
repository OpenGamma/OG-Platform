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
import com.opengamma.financial.property.DefaultPropertyFunction;

/**
 * Function to shift a volatility surface, implemented using properties and constraints.
 */
public class DefaultVolatilitySurfaceShiftFunction extends DefaultPropertyFunction {

  /**
   * Property to shift all volatility surfaces.
   */
  protected static final String VOLATILITY_SURFACE_SHIFT = "VOLATILITY_SURFACE_" + VolatilitySurfaceShiftFunction.SHIFT;

  public DefaultVolatilitySurfaceShiftFunction() {
    super(ComputationTargetType.SECURITY, VolatilitySurfaceShiftFunction.SHIFT, ValueRequirementNames.VOLATILITY_SURFACE);
  }

  @Override
  protected Set<String> getDefaultValue(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    return context.getViewCalculationConfiguration().getDefaultProperties().getValues(VOLATILITY_SURFACE_SHIFT);
  }

}
