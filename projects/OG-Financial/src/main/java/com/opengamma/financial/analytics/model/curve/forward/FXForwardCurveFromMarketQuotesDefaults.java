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
public class FXForwardCurveFromMarketQuotesDefaults extends DefaultPropertyFunction {
  private final String _forwardCurveInterpolator;
  private final String _forwardCurveLeftExtrapolator;
  private final String _forwardCurveRightExtrapolator;

  public FXForwardCurveFromMarketQuotesDefaults(final String forwardCurveInterpolator, final String forwardCurveLeftExtrapolator,
      final String forwardCurveRightExtrapolator) {
    super(ComputationTargetType.UNORDERED_CURRENCY_PAIR, true);
    ArgumentChecker.notNull(forwardCurveInterpolator, "forward curve interpolator");
    ArgumentChecker.notNull(forwardCurveLeftExtrapolator, "forward curve left extrapolator");
    ArgumentChecker.notNull(forwardCurveRightExtrapolator, "forward curve right extrapolator");
    _forwardCurveInterpolator = forwardCurveInterpolator;
    _forwardCurveLeftExtrapolator = forwardCurveLeftExtrapolator;
    _forwardCurveRightExtrapolator = forwardCurveRightExtrapolator;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    defaults.addValuePropertyName(ValueRequirementNames.FORWARD_CURVE, ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_INTERPOLATOR);
    defaults.addValuePropertyName(ValueRequirementNames.FORWARD_CURVE, ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR);
    defaults.addValuePropertyName(ValueRequirementNames.FORWARD_CURVE, ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR);
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if (ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_INTERPOLATOR.equals(propertyName)) {
      return Collections.singleton(_forwardCurveInterpolator);
    }
    if (ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR.equals(propertyName)) {
      return Collections.singleton(_forwardCurveLeftExtrapolator);
    }
    if (ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR.equals(propertyName)) {
      return Collections.singleton(_forwardCurveRightExtrapolator);
    }
    return null;
  }
}
