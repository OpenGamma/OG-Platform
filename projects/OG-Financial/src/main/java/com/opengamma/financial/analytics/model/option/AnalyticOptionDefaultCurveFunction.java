/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.option;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.analytics.greeks.AvailableGreeks;
import com.opengamma.financial.property.DefaultPropertyFunction;

/**
 * Dummy function for injecting default curve names into the dependency graph.
 */
public class AnalyticOptionDefaultCurveFunction extends DefaultPropertyFunction {

  private final String _curveName;

  public AnalyticOptionDefaultCurveFunction(final String curveName) {
    super(ComputationTargetType.SECURITY, true);
    _curveName = curveName;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (String valueName : AvailableGreeks.getAllGreekNames()) {
      defaults.addValuePropertyName(valueName, ValuePropertyNames.CURVE);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if (ValuePropertyNames.CURVE.equals(propertyName)) {
      return Collections.singleton(_curveName);
    }
    return null;
  }

  @Override
  public PriorityClass getPriority() {
    if ("SECONDARY".equals(_curveName)) {
      return PriorityClass.BELOW_NORMAL;
    } else {
      return super.getPriority();
    }
  }

}
