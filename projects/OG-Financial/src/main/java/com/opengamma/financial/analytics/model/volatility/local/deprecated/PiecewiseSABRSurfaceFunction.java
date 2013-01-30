/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.local.deprecated;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.SURFACE;
import static com.opengamma.financial.analytics.model.volatility.local.deprecated.LocalVolatilityPDEValuePropertyNames.PROPERTY_SURFACE_TYPE;
import static com.opengamma.financial.analytics.model.volatility.local.deprecated.LocalVolatilityPDEValuePropertyNames.PROPERTY_X_AXIS;
import static com.opengamma.financial.analytics.model.volatility.local.deprecated.LocalVolatilityPDEValuePropertyNames.PROPERTY_Y_AXIS;
import static com.opengamma.financial.analytics.model.volatility.local.deprecated.LocalVolatilityPDEValuePropertyNames.PROPERTY_Y_AXIS_TYPE;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.GeneralSmileInterpolator;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SmileInterpolatorSpline;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.SmileSurfaceDataBundle;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurface;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurfaceInterpolator;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames;

/**
 *
 * @deprecated Deprecated
 */
@Deprecated
public abstract class PiecewiseSABRSurfaceFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String surfaceType = desiredValue.getConstraint(PROPERTY_SURFACE_TYPE);
    final boolean moneynessSurface = LocalVolatilityPDEUtils.isMoneynessSurface(surfaceType);
    if (!moneynessSurface) {
      throw new OpenGammaRuntimeException("Cannot handle surface type other than moneyness; asked for strike");
    }
    final String xAxis = desiredValue.getConstraint(PROPERTY_X_AXIS);
    final boolean useLogTime = LocalVolatilityPDEUtils.useLogTime(xAxis);
    final String yAxis = desiredValue.getConstraint(PROPERTY_Y_AXIS);
    final boolean useIntegratedVariance = LocalVolatilityPDEUtils.useIntegratedVariance(yAxis);
    final String yAxisType = desiredValue.getConstraint(PROPERTY_Y_AXIS_TYPE);
    final boolean useLogValue = LocalVolatilityPDEUtils.useLogValue(yAxisType);
    final String surfaceName = desiredValue.getConstraint(SURFACE);
    final String curveCalculationMethodName = desiredValue.getConstraint(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD);
    final String forwardCurveName = desiredValue.getConstraint(CURVE);
    //TODO R White testing using spline rather than SABR - this should be an option
    final GeneralSmileInterpolator smileInterpolator = new SmileInterpolatorSpline();
    final VolatilitySurfaceInterpolator surfaceFitter = new VolatilitySurfaceInterpolator(smileInterpolator, useLogTime, useIntegratedVariance, useLogValue);
    //  final PiecewiseSABRSurfaceFitter1<?> surfaceFitter = new MoneynessPiecewiseSABRSurfaceFitter(useLogTime, useIntegratedVariance, useLogValue);
    final SmileSurfaceDataBundle data = getData(inputs, getVolatilityDataRequirement(target, surfaceName), getForwardCurveRequirement(target, curveCalculationMethodName, forwardCurveName));
    final BlackVolatilitySurface<?> impliedVolatilitySurface = surfaceFitter.getVolatilitySurface(data);
    final ValueProperties properties = getResultProperties(surfaceName, surfaceType, xAxis, yAxis, yAxisType, curveCalculationMethodName,
        forwardCurveName);
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.PIECEWISE_SABR_VOL_SURFACE, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, impliedVolatilitySurface));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = getResultProperties();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.PIECEWISE_SABR_VOL_SURFACE, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> surfaceNames = constraints.getValues(SURFACE);
    if (surfaceNames == null || surfaceNames.size() != 1) {
      return null;
    }
    final String surfaceName = surfaceNames.iterator().next();
    final Set<String> forwardCurveCalculationMethodNames = constraints.getValues(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD);
    if (forwardCurveCalculationMethodNames == null || forwardCurveCalculationMethodNames.size() != 1) {
      return null;
    }
    final String forwardCurveCalculationMethod = forwardCurveCalculationMethodNames.iterator().next();
    final Set<String> forwardCurveNames = constraints.getValues(CURVE);
    if (forwardCurveNames == null || forwardCurveNames.size() != 1) {
      return null;
    }
    final String forwardCurveName = forwardCurveNames.iterator().next();
    final Set<String> surfaceTypes = constraints.getValues(PROPERTY_SURFACE_TYPE);
    if (surfaceTypes == null || surfaceTypes.size() != 1) {
      return null;
    }
    final Set<String> xAxes = constraints.getValues(PROPERTY_X_AXIS);
    if (xAxes == null || xAxes.size() != 1) {
      return null;
    }
    final Set<String> yAxes = constraints.getValues(PROPERTY_Y_AXIS);
    if (yAxes == null || yAxes.size() != 1) {
      return null;
    }
    final Set<String> yAxisTypes = constraints.getValues(PROPERTY_Y_AXIS_TYPE);
    if (yAxisTypes == null || yAxisTypes.size() != 1) {
      return null;
    }
    return Sets.newHashSet(getVolatilityDataRequirement(target, surfaceName), getForwardCurveRequirement(target, forwardCurveCalculationMethod, forwardCurveName));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    String surfaceName = null;
    String forwardCurveCalculationMethod = null;
    String forwardCurveName = null;
    for (final Map.Entry<ValueSpecification, ValueRequirement> input : inputs.entrySet()) {
      final ValueProperties constraints = input.getValue().getConstraints();
      if (input.getValue().getValueName().equals(ValueRequirementNames.FORWARD_CURVE)) {
        if (constraints.getValues(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD) != null) {
          final Set<String> forwardCurveCalculationMethodNames = constraints.getValues(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD);
          if (forwardCurveCalculationMethodNames == null || forwardCurveCalculationMethodNames.size() != 1) {
            throw new OpenGammaRuntimeException("Missing or non-unique forward curve calculation method name");
          }
          forwardCurveCalculationMethod = forwardCurveCalculationMethodNames.iterator().next();
        }
        if (constraints.getValues(CURVE) != null) {
          final Set<String> forwardCurveNames = constraints.getValues(CURVE);
          if (forwardCurveNames == null || forwardCurveNames.size() != 1) {
            throw new OpenGammaRuntimeException("Missing or non-unique forward curve name");
          }
          forwardCurveName = forwardCurveNames.iterator().next();
        }
      } else if (input.getValue().getValueName().equals(ValueRequirementNames.VOLATILITY_SURFACE_DATA)) {
        if (constraints.getValues(ValuePropertyNames.SURFACE) != null) {
          final Set<String> surfaceNames = constraints.getValues(ValuePropertyNames.SURFACE);
          if (surfaceNames == null || surfaceNames.size() != 1) {
            throw new OpenGammaRuntimeException("Missing or non-unique surface name");
          }
          surfaceName = surfaceNames.iterator().next();
        }
      }
    }
    assert surfaceName != null;
    assert forwardCurveCalculationMethod != null;
    assert forwardCurveName != null;
    final ValueProperties properties = getResultProperties(surfaceName, forwardCurveCalculationMethod, forwardCurveName);
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.PIECEWISE_SABR_VOL_SURFACE, target.toSpecification(), properties));
  }

  protected abstract SmileSurfaceDataBundle getData(FunctionInputs inputs, ValueRequirement volDataRequirement, ValueRequirement forwardCurveRequirement);

  protected abstract ValueProperties getResultProperties();

  protected abstract ValueProperties getResultProperties(final String definitionName, final String forwardCurveCalculationMethod, final String forwardCurveName);

  protected abstract ValueProperties getResultProperties(final String definitionName, final String surfaceType, final String xAxis, final String yAxis,
      final String yAxisType, final String forwardCurveCalculationMethod, final String forwardCurveName);

  protected abstract ValueRequirement getVolatilityDataRequirement(final ComputationTarget target, final String surfaceName);

  private ValueRequirement getForwardCurveRequirement(final ComputationTarget target, final String calculationMethod, final String forwardCurveName) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD, calculationMethod)
        .with(CURVE, forwardCurveName).get();
    return new ValueRequirement(ValueRequirementNames.FORWARD_CURVE, target.toSpecification(), properties);
  }

}
