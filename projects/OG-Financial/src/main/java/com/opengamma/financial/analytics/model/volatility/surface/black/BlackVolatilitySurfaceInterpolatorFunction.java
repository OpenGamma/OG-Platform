/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface.black;

import static com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_TIME_AXIS;
import static com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_TIME_INTERPOLATOR;
import static com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_TIME_LEFT_EXTRAPOLATOR;
import static com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_TIME_RIGHT_EXTRAPOLATOR;
import static com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_VOLATILITY_TRANSFORM;
import static com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_Y_AXIS;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.GeneralSmileInterpolator;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurfaceInterpolator;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;

/**
 * 
 */
public abstract class BlackVolatilitySurfaceInterpolatorFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String timeAxis = desiredValue.getConstraint(PROPERTY_TIME_AXIS);
    final boolean useLogTime = BlackVolatilitySurfacePropertyUtils.useLogTime(timeAxis);
    final String yAxis = desiredValue.getConstraint(PROPERTY_Y_AXIS);
    final boolean useLogValue = BlackVolatilitySurfacePropertyUtils.useLogYAxis(yAxis);
    final String volatilityTransform = desiredValue.getConstraint(PROPERTY_VOLATILITY_TRANSFORM);
    final boolean useIntegratedVariance = BlackVolatilitySurfacePropertyUtils.useIntegratedVariance(volatilityTransform);
    final GeneralSmileInterpolator smileInterpolator = getSmileInterpolator(desiredValue);
    final String interpolator = desiredValue.getConstraint(PROPERTY_TIME_INTERPOLATOR);
    final String leftExtrapolator = desiredValue.getConstraint(PROPERTY_TIME_LEFT_EXTRAPOLATOR);
    final String rightExtrapolator = desiredValue.getConstraint(PROPERTY_TIME_RIGHT_EXTRAPOLATOR);
    final Interpolator1D timeInterpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(interpolator, leftExtrapolator, rightExtrapolator);
    final VolatilitySurfaceInterpolator surfaceInterpolator = new VolatilitySurfaceInterpolator(smileInterpolator, timeInterpolator, useLogTime, useIntegratedVariance, useLogValue);
    final ValueProperties properties = getResultProperties(desiredValue);
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.BLACK_VOLATILITY_SURFACE_INTERPOLATOR, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, surfaceInterpolator));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.NULL;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = getResultProperties();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.BLACK_VOLATILITY_SURFACE_INTERPOLATOR, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<ValueRequirement> requirements = BlackVolatilitySurfacePropertyUtils.ensureCommonVolatilityInterpolatorProperties(constraints);
    if (requirements == null) {
      return null;
    }
    return getSpecificRequirements(constraints);
  }

  protected abstract Set<ValueRequirement> getSpecificRequirements(final ValueProperties constraints);

  protected abstract GeneralSmileInterpolator getSmileInterpolator(final ValueRequirement desiredValue);

  protected abstract ValueProperties getResultProperties();

  protected abstract ValueProperties getResultProperties(final ValueRequirement desiredValue);

}
