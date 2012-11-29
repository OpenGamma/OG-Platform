/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties;

import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.property.DefaultPropertyFunction;

/**
 *
 */
public class SoybeanFutureOptionBlackVolatilitySurfaceDefaults extends DefaultPropertyFunction {

  /**
   * @param targetType
   * @param permitWithout
   */
  protected SoybeanFutureOptionBlackVolatilitySurfaceDefaults(final ComputationTargetType targetType, final boolean permitWithout) {
    super(targetType, permitWithout);
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    return null;
  }


}
