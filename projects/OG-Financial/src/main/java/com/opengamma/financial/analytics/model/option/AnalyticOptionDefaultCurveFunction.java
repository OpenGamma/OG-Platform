/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.option;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.analytics.greeks.AvailableGreeks;
import com.opengamma.financial.property.StaticDefaultPropertyFunction;

/**
 * Dummy function for injecting default curve names into the dependency graph.
 * 
 * @deprecated The functions for which these defaults apply are deprecated.
 */
@Deprecated
public class AnalyticOptionDefaultCurveFunction extends StaticDefaultPropertyFunction {

  private final Set<String> _curveName;

  public AnalyticOptionDefaultCurveFunction(final String curveName) {
    super(ComputationTargetType.SECURITY, ValuePropertyNames.CURVE, true, AvailableGreeks.getAllGreekNames().toArray(new String[0]));
    _curveName = Collections.singleton(curveName);
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueName : AvailableGreeks.getAllGreekNames()) {
      defaults.addValuePropertyName(valueName, ValuePropertyNames.CURVE);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    return _curveName;
  }

  @Override
  public PriorityClass getPriority() {
    return PriorityClass.BELOW_NORMAL;
  }

}
