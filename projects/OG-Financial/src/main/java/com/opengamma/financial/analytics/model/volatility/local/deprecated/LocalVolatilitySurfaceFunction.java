/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.local.deprecated;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.financial.analytics.model.volatility.local.deprecated.LocalVolatilityPDEValuePropertyNames.PROPERTY_H;
import static com.opengamma.financial.analytics.model.volatility.local.deprecated.LocalVolatilityPDEValuePropertyNames.PROPERTY_SURFACE_TYPE;
import static com.opengamma.financial.analytics.model.volatility.local.deprecated.LocalVolatilityPDEValuePropertyNames.PROPERTY_X_AXIS;
import static com.opengamma.financial.analytics.model.volatility.local.deprecated.LocalVolatilityPDEValuePropertyNames.PROPERTY_Y_AXIS;
import static com.opengamma.financial.analytics.model.volatility.local.deprecated.LocalVolatilityPDEValuePropertyNames.PROPERTY_Y_AXIS_TYPE;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.volatility.local.DupireLocalVolatilityCalculator;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilitySurface;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurface;
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
public abstract class LocalVolatilitySurfaceFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String surfaceName = desiredValue.getConstraint(ValuePropertyNames.SURFACE);
    final String surfaceType = desiredValue.getConstraint(PROPERTY_SURFACE_TYPE);
    final String xAxis = desiredValue.getConstraint(PROPERTY_X_AXIS);
    final String yAxis = desiredValue.getConstraint(PROPERTY_Y_AXIS);
    final String yAxisType = desiredValue.getConstraint(PROPERTY_Y_AXIS_TYPE);
    final String forwardCurveCalculationMethod = desiredValue.getConstraint(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD);
    final String forwardCurveName = desiredValue.getConstraint(CURVE);
    final String hName = desiredValue.getConstraint(PROPERTY_H);
    final double h = Double.parseDouble(hName);
    final Object impliedVolatilitySurfaceObject = inputs.getValue(getVolatilitySurfaceRequirement(target, surfaceName, surfaceType, xAxis, yAxis, yAxisType, forwardCurveCalculationMethod,
        forwardCurveName));
    if (impliedVolatilitySurfaceObject == null) {
      throw new OpenGammaRuntimeException("Volatility surface was null");
    }
    final Object forwardCurveObject = inputs.getValue(getForwardCurveRequirement(target, forwardCurveCalculationMethod, forwardCurveName));
    if (forwardCurveObject == null) {
      throw new OpenGammaRuntimeException("Forward curve was null");
    }
    final ForwardCurve forwardCurve = (ForwardCurve) forwardCurveObject;
    final BlackVolatilitySurface<?> impliedVolatilitySurface = (BlackVolatilitySurface<?>) impliedVolatilitySurfaceObject;
    final ValueProperties properties = getResultProperties(surfaceName, surfaceType, xAxis, yAxis, yAxisType, forwardCurveCalculationMethod, forwardCurveName, hName);
    final DupireLocalVolatilityCalculator calculator = new DupireLocalVolatilityCalculator(h);
    final LocalVolatilitySurface<?> localVolatilitySurface = calculator.getLocalVolatilitySurface(impliedVolatilitySurface, forwardCurve);
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.LOCAL_VOLATILITY_SURFACE, target.toSpecification(), properties);
    return Sets.newHashSet(new ComputedValue(spec, localVolatilitySurface));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = getResultProperties();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.LOCAL_VOLATILITY_SURFACE, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> surfaceNames = constraints.getValues(ValuePropertyNames.SURFACE);
    if (surfaceNames == null || surfaceNames.size() != 1) {
      return null;
    }
    final Set<String> surfaceTypeNames = constraints.getValues(PROPERTY_SURFACE_TYPE);
    if (surfaceTypeNames == null || surfaceTypeNames.size() != 1) {
      return null;
    }
    final Set<String> xAxisNames = constraints.getValues(PROPERTY_X_AXIS);
    if (xAxisNames == null || xAxisNames.size() != 1) {
      return null;
    }
    final Set<String> yAxisNames = constraints.getValues(PROPERTY_Y_AXIS);
    if (yAxisNames == null || yAxisNames.size() != 1) {
      return null;
    }
    final Set<String> yAxisTypeNames = constraints.getValues(PROPERTY_Y_AXIS_TYPE);
    if (yAxisTypeNames == null || yAxisTypeNames.size() != 1) {
      return null;
    }
    final Set<String> hNames = constraints.getValues(PROPERTY_H);
    if (hNames == null || hNames.size() != 1) {
      return null;
    }
    final Set<String> forwardCurveCalculationMethodNames = constraints.getValues(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD);
    if (forwardCurveCalculationMethodNames == null || forwardCurveCalculationMethodNames.size() != 1) {
      return null;
    }
    final Set<String> forwardCurveNames = constraints.getValues(CURVE);
    if (forwardCurveNames == null || forwardCurveNames.size() != 1) {
      return null;
    }
    final String surfaceName = surfaceNames.iterator().next();
    final String surfaceType = surfaceTypeNames.iterator().next();
    final String xAxis = xAxisNames.iterator().next();
    final String yAxis = yAxisNames.iterator().next();
    final String yAxisType = yAxisTypeNames.iterator().next();
    final String forwardCurveCalculationMethod = forwardCurveCalculationMethodNames.iterator().next();
    final String forwardCurveName = forwardCurveNames.iterator().next();
    return Sets.newHashSet(getVolatilitySurfaceRequirement(target, surfaceName, surfaceType, xAxis, yAxis, yAxisType, forwardCurveCalculationMethod,
        forwardCurveName),
        getForwardCurveRequirement(target, forwardCurveCalculationMethod, forwardCurveName));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    String surfaceName = null;
    String surfaceType = null;
    String xAxis = null;
    String yAxis = null;
    String yAxisType = null;
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
      } else if (input.getValue().getValueName().equals(ValueRequirementNames.PIECEWISE_SABR_VOL_SURFACE)) {
        if (constraints.getValues(ValuePropertyNames.SURFACE) != null) {
          final Set<String> surfaceNames = constraints.getValues(ValuePropertyNames.SURFACE);
          if (surfaceNames == null || surfaceNames.size() != 1) {
            throw new OpenGammaRuntimeException("Missing or non-unique surface name");
          }
          surfaceName = surfaceNames.iterator().next();
        }
        if (constraints.getValues(PROPERTY_SURFACE_TYPE) != null) {
          final Set<String> surfaceTypeNames = constraints.getValues(PROPERTY_SURFACE_TYPE);
          if (surfaceTypeNames == null || surfaceTypeNames.size() != 1) {
            throw new OpenGammaRuntimeException("Missing or non-unique surface type name");
          }
          surfaceType = surfaceTypeNames.iterator().next();
        }
        if (constraints.getValues(PROPERTY_X_AXIS) != null) {
          final Set<String> xAxisNames = constraints.getValues(PROPERTY_X_AXIS);
          if (xAxisNames == null || xAxisNames.size() != 1) {
            throw new OpenGammaRuntimeException("Missing or non-unique x-axis property name");
          }
          xAxis = xAxisNames.iterator().next();
        }
        if (constraints.getValues(PROPERTY_Y_AXIS) != null) {
          final Set<String> yAxisNames = constraints.getValues(PROPERTY_Y_AXIS);
          if (yAxisNames == null || yAxisNames.size() != 1) {
            throw new OpenGammaRuntimeException("Missing or non-unique y-axis property name");
          }
          yAxis = yAxisNames.iterator().next();
        }
        if (constraints.getValues(PROPERTY_Y_AXIS_TYPE) != null) {
          final Set<String> yAxisTypeNames = constraints.getValues(PROPERTY_Y_AXIS_TYPE);
          if (yAxisTypeNames == null || yAxisTypeNames.size() != 1) {
            throw new OpenGammaRuntimeException("Missing or non-unique y-axis property name");
          }
          yAxisType = yAxisTypeNames.iterator().next();
        }
      }
    }
    assert surfaceName != null;
    assert surfaceType != null;
    assert xAxis != null;
    assert yAxis != null;
    assert yAxisType != null;
    assert forwardCurveCalculationMethod != null;
    assert forwardCurveName != null;
    final ValueProperties properties = getResultProperties(surfaceName, surfaceType, xAxis, yAxis, yAxisType, forwardCurveCalculationMethod, forwardCurveName);
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.LOCAL_VOLATILITY_SURFACE, target.toSpecification(), properties));
  }

  protected abstract ValueProperties getResultProperties();

  protected abstract ValueProperties getResultProperties(final String surfaceName, final String surfaceType, final String xAxis, final String yAxis,
      final String yAxisType, final String forwardCurveCalculationMethod, final String forwardCurveName);

  protected abstract ValueProperties getResultProperties(final String surfaceName, final String surfaceType, final String xAxis, final String yAxis,
      final String yAxisType, final String forwardCurveCalculationMethod, final String forwardCurveName, final String h);

  protected abstract ValueProperties getSurfaceProperties(final String surfaceName, final String surfaceType, final String xAxis, final String yAxis,
      final String yAxisType, final String forwardCurveCalculationMethod, final String forwardCurveName);

  private ValueRequirement getForwardCurveRequirement(final ComputationTarget target, final String calculationMethod, final String curveName) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD, calculationMethod)
        .with(ValuePropertyNames.CURVE, curveName).get();
    return new ValueRequirement(ValueRequirementNames.FORWARD_CURVE, target.toSpecification(), properties);
  }

  private ValueRequirement getVolatilitySurfaceRequirement(final ComputationTarget target, final String definitionName, final String surfaceType, final String xAxis, final String yAxis,
      final String yAxisType, final String forwardCurveCalculationMethod, final String forwardCurveName) {
    final ValueProperties properties = getSurfaceProperties(definitionName, surfaceType, xAxis, yAxis, yAxisType, forwardCurveCalculationMethod, forwardCurveName);
    return new ValueRequirement(ValueRequirementNames.PIECEWISE_SABR_VOL_SURFACE, target.toSpecification(), properties);
  }
}
