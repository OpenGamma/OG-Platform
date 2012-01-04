/*
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.property.DefaultPropertyFunction;

/**
 * Dummy function for injecting default curve names into the dependency graph.
 */
public class InterestRateFutureOptionDefaultValuesFunction extends DefaultPropertyFunction {
  private final String[] _valueNames;
  private final String _forwardCurve;
  private final String _fundingCurve;
  private final String _surfaceName;

  public InterestRateFutureOptionDefaultValuesFunction(final String forwardCurve, final String fundingCurve, final String surfaceName, final String... valueNames) {
    super(ComputationTargetType.TRADE, true);
    _forwardCurve = forwardCurve;
    _fundingCurve = fundingCurve;
    _surfaceName = surfaceName;
    _valueNames = valueNames;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueName : _valueNames) {
      defaults.addValuePropertyName(valueName, YieldCurveFunction.PROPERTY_FORWARD_CURVE);
      defaults.addValuePropertyName(valueName, YieldCurveFunction.PROPERTY_FUNDING_CURVE);
      defaults.addValuePropertyName(valueName, ValuePropertyNames.SURFACE);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if (YieldCurveFunction.PROPERTY_FORWARD_CURVE.equals(propertyName)) {
      return Collections.singleton(_forwardCurve);
    } else if (YieldCurveFunction.PROPERTY_FUNDING_CURVE.equals(propertyName)) {
      return Collections.singleton(_fundingCurve);
    } else if (ValuePropertyNames.SURFACE.equals(propertyName)) {
      return Collections.singleton(_surfaceName);
    }
    return null;
  }

}
