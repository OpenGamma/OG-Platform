/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.future;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.model.irfutureoption.InterestRateFutureOptionBlackPositionDeltaGammaScaleDefaults;
import com.opengamma.financial.analytics.model.multicurve.MultiCurvePricingFunction;
import com.opengamma.financial.property.StaticDefaultPropertyFunction;

/**
 * Function for injecting default scale factors for delta positions into the dependency graph.
 * <p>
 * See {@link InterestRateFutureOptionBlackPositionDeltaGammaScaleDefaults}
 * 
 * @deprecated These properties are no longer needed when using {@link MultiCurvePricingFunction} and related classes.
 */
@Deprecated
public class InterestRateFutureDeltaScaleDefaults extends StaticDefaultPropertyFunction {

  private final Set<String> _defaultScale;

  public InterestRateFutureDeltaScaleDefaults(final String scaleFactor) {
    super(ComputationTargetType.SECURITY, ValuePropertyNames.SCALE, true, ValueRequirementNames.DELTA, ValueRequirementNames.VALUE_DELTA);
    _defaultScale = Collections.singleton(scaleFactor);
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    return _defaultScale;
  }
}
