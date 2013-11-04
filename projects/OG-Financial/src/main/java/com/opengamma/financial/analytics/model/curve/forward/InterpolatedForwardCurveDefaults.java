/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve.forward;

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
public class InterpolatedForwardCurveDefaults extends DefaultPropertyFunction {

  private final Set<String> _forwardCurveInterpolator;
  private final Set<String> _forwardCurveLeftExtrapolator;
  private final Set<String> _forwardCurveRightExtrapolator;

  public InterpolatedForwardCurveDefaults(final String forwardCurveInterpolator, final String forwardCurveLeftExtrapolator, final String forwardCurveRightExtrapolator) {
    super(ComputationTargetType.ANYTHING, true); // // [PLAT-2286]: change to correct type
    ArgumentChecker.notNull(forwardCurveInterpolator, "forward curve interpolator");
    ArgumentChecker.notNull(forwardCurveLeftExtrapolator, "forward curve left extrapolator");
    ArgumentChecker.notNull(forwardCurveRightExtrapolator, "forward curve right extrapolator");
    _forwardCurveInterpolator = Collections.singleton(forwardCurveInterpolator);
    _forwardCurveLeftExtrapolator = Collections.singleton(forwardCurveLeftExtrapolator);
    _forwardCurveRightExtrapolator = Collections.singleton(forwardCurveRightExtrapolator);
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    defaults.addValuePropertyName(ValueRequirementNames.FORWARD_CURVE, ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_INTERPOLATOR);
    defaults.addValuePropertyName(ValueRequirementNames.FORWARD_CURVE, ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR);
    defaults.addValuePropertyName(ValueRequirementNames.FORWARD_CURVE, ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR);
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    switch (propertyName) {
      case ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_INTERPOLATOR:
        return _forwardCurveInterpolator;
      case ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR:
        return _forwardCurveLeftExtrapolator;
      case ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR:
        return _forwardCurveRightExtrapolator;
      default:
        return null;
    }
  }

}
