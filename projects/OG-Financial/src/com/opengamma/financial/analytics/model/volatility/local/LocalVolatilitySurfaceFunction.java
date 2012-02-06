/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.local;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CALCULATION_METHOD;
import static com.opengamma.financial.analytics.model.volatility.local.InterpolatedForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_INTERPOLATOR;
import static com.opengamma.financial.analytics.model.volatility.local.InterpolatedForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR;
import static com.opengamma.financial.analytics.model.volatility.local.InterpolatedForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR;
import static com.opengamma.financial.analytics.model.volatility.local.LocalVolatilityPDEValuePropertyNames.PROPERTY_H;
import static com.opengamma.financial.analytics.model.volatility.local.LocalVolatilityPDEValuePropertyNames.PROPERTY_LAMBDA;
import static com.opengamma.financial.analytics.model.volatility.local.LocalVolatilityPDEValuePropertyNames.PROPERTY_SURFACE_TYPE;
import static com.opengamma.financial.analytics.model.volatility.local.LocalVolatilityPDEValuePropertyNames.PROPERTY_X_AXIS;
import static com.opengamma.financial.analytics.model.volatility.local.LocalVolatilityPDEValuePropertyNames.PROPERTY_Y_AXIS;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
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
import com.opengamma.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.financial.model.volatility.local.DupireLocalVolatilityCalculator;
import com.opengamma.financial.model.volatility.local.LocalVolatilitySurface;
import com.opengamma.financial.model.volatility.surface.BlackVolatilitySurface;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 * 
 */
public abstract class LocalVolatilitySurfaceFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String surfaceName = desiredValue.getConstraint(ValuePropertyNames.SURFACE);
    final String surfaceType = desiredValue.getConstraint(PROPERTY_SURFACE_TYPE);
    final String xAxis = desiredValue.getConstraint(PROPERTY_X_AXIS);
    final String yAxis = desiredValue.getConstraint(PROPERTY_Y_AXIS);
    final String lambdaName = desiredValue.getConstraint(PROPERTY_LAMBDA);
    final String forwardCurveCalculationMethod = desiredValue.getConstraint(CURVE_CALCULATION_METHOD);
    final String forwardCurveInterpolator = desiredValue.getConstraint(PROPERTY_FORWARD_CURVE_INTERPOLATOR);
    final String forwardCurveLeftExtrapolator = desiredValue.getConstraint(PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR);
    final String forwardCurveRightExtrapolator = desiredValue.getConstraint(PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR);
    final String hName = desiredValue.getConstraint(PROPERTY_H);
    final double h = Double.parseDouble(hName);
    final Object impliedVolatilitySurfaceObject = inputs.getValue(getVolatilitySurfaceRequirement(target, surfaceName, surfaceType, xAxis, yAxis, lambdaName, forwardCurveCalculationMethod,
        forwardCurveInterpolator, forwardCurveLeftExtrapolator, forwardCurveRightExtrapolator));
    if (impliedVolatilitySurfaceObject == null) {
      throw new OpenGammaRuntimeException("Volatility surface was null");
    }
    final Object forwardCurveObject = inputs.getValue(getForwardCurveRequirement(target, forwardCurveCalculationMethod, forwardCurveInterpolator, forwardCurveLeftExtrapolator,
        forwardCurveRightExtrapolator));
    if (forwardCurveObject == null) {
      throw new OpenGammaRuntimeException("Forward curve was null");
    }
    final ForwardCurve forwardCurve = (ForwardCurve) forwardCurveObject;
    final BlackVolatilitySurface<?> impliedVolatilitySurface = (BlackVolatilitySurface<?>) impliedVolatilitySurfaceObject;
    final ValueProperties properties = getResultProperties(surfaceName, surfaceType, xAxis, yAxis, lambdaName, forwardCurveCalculationMethod, hName,
        forwardCurveInterpolator, forwardCurveLeftExtrapolator, forwardCurveRightExtrapolator);
    final DupireLocalVolatilityCalculator calculator = new DupireLocalVolatilityCalculator(h);
    final LocalVolatilitySurface<?> localVolatilitySurface = calculator.getLocalVolatilitySurface(impliedVolatilitySurface, forwardCurve);
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.LOCAL_VOLATILITY_SURFACE, target.toSpecification(), properties);
    return Sets.newHashSet(new ComputedValue(spec, localVolatilitySurface));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PRIMITIVE;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getType() == ComputationTargetType.PRIMITIVE && UnorderedCurrencyPair.OBJECT_SCHEME.equals(target.getUniqueId().getScheme());
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
    final Set<String> lambdaNames = constraints.getValues(PROPERTY_LAMBDA);
    if (lambdaNames == null || lambdaNames.size() != 1) {
      return null;
    }
    final Set<String> hNames = constraints.getValues(PROPERTY_H);
    if (hNames == null || hNames.size() != 1) {
      return null;
    }
    final Set<String> forwardCurveCalculationMethodNames = constraints.getValues(CURVE_CALCULATION_METHOD);
    if (forwardCurveCalculationMethodNames == null || forwardCurveCalculationMethodNames.size() != 1) {
      return null;
    }
    final Set<String> forwardCurveInterpolatorNames = constraints.getValues(PROPERTY_FORWARD_CURVE_INTERPOLATOR);
    if (forwardCurveInterpolatorNames == null || forwardCurveInterpolatorNames.size() != 1) {
      return null;
    }
    final Set<String> forwardCurveLeftExtrapolatorNames = constraints.getValues(PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR);
    if (forwardCurveLeftExtrapolatorNames == null || forwardCurveLeftExtrapolatorNames.size() != 1) {
      return null;
    }
    final Set<String> forwardCurveRightExtrapolatorNames = constraints.getValues(PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR);
    if (forwardCurveRightExtrapolatorNames == null || forwardCurveRightExtrapolatorNames.size() != 1) {
      return null;
    }
    final String surfaceName = surfaceNames.iterator().next();
    final String surfaceType = surfaceTypeNames.iterator().next();
    final String xAxis = xAxisNames.iterator().next();
    final String yAxis = yAxisNames.iterator().next();
    final String lambda = lambdaNames.iterator().next();
    final String forwardCurveCalculationMethod = forwardCurveCalculationMethodNames.iterator().next();
    final String forwardCurveInterpolator = forwardCurveInterpolatorNames.iterator().next();
    final String forwardCurveLeftExtrapolator = forwardCurveLeftExtrapolatorNames.iterator().next();
    final String forwardCurveRightExtrapolator = forwardCurveRightExtrapolatorNames.iterator().next();
    return Sets.newHashSet(getVolatilitySurfaceRequirement(target, surfaceName, surfaceType, xAxis, yAxis, lambda, forwardCurveCalculationMethod,
        forwardCurveInterpolator, forwardCurveLeftExtrapolator, forwardCurveRightExtrapolator),
        getForwardCurveRequirement(target, forwardCurveCalculationMethod, forwardCurveInterpolator, forwardCurveLeftExtrapolator, forwardCurveRightExtrapolator));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    String surfaceName = null;
    String surfaceType = null;
    String xAxis = null;
    String yAxis = null;
    String lambda = null;
    String forwardCurveCalculationMethod = null;
    String forwardCurveInterpolator = null;
    String forwardCurveLeftExtrapolator = null;
    String forwardCurveRightExtrapolator = null;
    for (final Map.Entry<ValueSpecification, ValueRequirement> input : inputs.entrySet()) {
      final ValueProperties constraints = input.getValue().getConstraints();
      if (input.getValue().getValueName().equals(ValueRequirementNames.FORWARD_CURVE)) {
        if (constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_METHOD) != null) {
          final Set<String> forwardCurveCalculationMethodNames = constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_METHOD);
          if (forwardCurveCalculationMethodNames == null || forwardCurveCalculationMethodNames.size() != 1) {
            throw new OpenGammaRuntimeException("Missing or non-unique forward curve calculation method name");
          }
          forwardCurveCalculationMethod = forwardCurveCalculationMethodNames.iterator().next();
        }
        if (constraints.getValues(PROPERTY_FORWARD_CURVE_INTERPOLATOR) != null) {
          final Set<String> forwardCurveInterpolatorNames = constraints.getValues(PROPERTY_FORWARD_CURVE_INTERPOLATOR);
          if (forwardCurveInterpolatorNames == null || forwardCurveInterpolatorNames.size() != 1) {
            throw new OpenGammaRuntimeException("Missing or non-unique forward curve interpolator name");
          }
          forwardCurveInterpolator = forwardCurveInterpolatorNames.iterator().next();
        }
        if (constraints.getValues(PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR) != null) {
          final Set<String> forwardCurveLeftExtrapolatorNames = constraints.getValues(PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR);
          if (forwardCurveLeftExtrapolatorNames == null || forwardCurveLeftExtrapolatorNames.size() != 1) {
            throw new OpenGammaRuntimeException("Missing or non-unique forward curve left extrapolator name");
          }
          forwardCurveLeftExtrapolator = forwardCurveLeftExtrapolatorNames.iterator().next();
        }
        if (constraints.getValues(PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR) != null) {
          final Set<String> forwardCurveRightExtrapolatorNames = constraints.getValues(PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR);
          if (forwardCurveRightExtrapolatorNames == null || forwardCurveRightExtrapolatorNames.size() != 1) {
            throw new OpenGammaRuntimeException("Missing or non-unique forward curve right extrapolator name");
          }
          forwardCurveRightExtrapolator = forwardCurveRightExtrapolatorNames.iterator().next();
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
        if (constraints.getValues(PROPERTY_LAMBDA) != null) {
          final Set<String> lambdaNames = constraints.getValues(PROPERTY_LAMBDA);
          if (lambdaNames == null || lambdaNames.size() != 1) {
            throw new OpenGammaRuntimeException("Missing or non-unique lambda property name");
          }
          lambda = lambdaNames.iterator().next();
        }
      }
    }
    assert surfaceName != null;
    assert surfaceType != null;
    assert xAxis != null;
    assert yAxis != null;
    assert lambda != null;
    assert forwardCurveCalculationMethod != null;
    assert forwardCurveInterpolator != null;
    assert forwardCurveLeftExtrapolator != null;
    assert forwardCurveRightExtrapolator != null;
    final ValueProperties properties = getResultProperties(surfaceName, surfaceType, xAxis, yAxis, lambda, forwardCurveCalculationMethod, forwardCurveInterpolator,
        forwardCurveLeftExtrapolator, forwardCurveRightExtrapolator);
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.LOCAL_VOLATILITY_SURFACE, target.toSpecification(), properties));
  }

  protected abstract ValueProperties getResultProperties();

  protected abstract ValueProperties getResultProperties(final String surfaceName, final String surfaceType, final String xAxis, final String yAxis, final String lambda,
      final String forwardCurveCalculationMethod, final String forwardCurveInterpolator, final String forwardCurveLeftExtrapolator, final String forwardCurveRightExtrapolator);

  protected abstract ValueProperties getResultProperties(final String surfaceName, final String surfaceType, final String xAxis, final String yAxis, final String lambda,
      final String forwardCurveCalculationMethod, final String h, final String forwardCurveInterpolator, final String forwardCurveLeftExtrapolator, final String forwardCurveRightExtrapolator);

  protected abstract ValueProperties getSurfaceProperties(final String surfaceName, final String surfaceType, final String xAxis, final String yAxis, final String lambda,
      final String forwardCurveCalculationMethod, final String forwardCurveInterpolator, final String forwardCurveLeftExtrapolator, final String forwardCurveRightExtrapolator);

  private ValueRequirement getForwardCurveRequirement(final ComputationTarget target, final String calculationMethod, final String forwardCurveInterpolator, final String forwardCurveLeftExtrapolator,
      final String forwardCurveRightExtrapolator) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, calculationMethod)
        .with(InterpolatedForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_INTERPOLATOR, forwardCurveInterpolator)
        .with(InterpolatedForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR, forwardCurveLeftExtrapolator)
        .with(InterpolatedForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR, forwardCurveRightExtrapolator).get();
    return new ValueRequirement(ValueRequirementNames.FORWARD_CURVE, target.toSpecification(), properties);
  }

  private ValueRequirement getVolatilitySurfaceRequirement(final ComputationTarget target, final String definitionName, final String surfaceType, final String xAxis, final String yAxis,
      final String lambda, final String forwardCurveCalculationMethod, final String forwardCurveInterpolator, final String forwardCurveLeftExtrapolator,
      final String forwardCurveRightExtrapolator) {
    final ValueProperties properties = getSurfaceProperties(definitionName, surfaceType, xAxis, yAxis, lambda, forwardCurveCalculationMethod, forwardCurveInterpolator,
        forwardCurveLeftExtrapolator, forwardCurveRightExtrapolator);
    return new ValueRequirement(ValueRequirementNames.PIECEWISE_SABR_VOL_SURFACE, target.toSpecification(), properties);
  }
}
