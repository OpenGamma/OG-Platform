/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.local;

import static com.opengamma.financial.analytics.model.volatility.local.InterpolatedForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_INTERPOLATOR;
import static com.opengamma.financial.analytics.model.volatility.local.InterpolatedForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR;
import static com.opengamma.financial.analytics.model.volatility.local.InterpolatedForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR;
import static com.opengamma.financial.analytics.model.volatility.local.LocalVolatilityPDEValuePropertyNames.PROPERTY_LAMBDA;
import static com.opengamma.financial.analytics.model.volatility.local.LocalVolatilityPDEValuePropertyNames.PROPERTY_SURFACE_TYPE;
import static com.opengamma.financial.analytics.model.volatility.local.LocalVolatilityPDEValuePropertyNames.PROPERTY_X_AXIS;
import static com.opengamma.financial.analytics.model.volatility.local.LocalVolatilityPDEValuePropertyNames.PROPERTY_Y_AXIS;
import static com.opengamma.financial.analytics.volatility.surface.RawVolatilitySurfaceDataFunction.PROPERTY_SURFACE_INSTRUMENT_TYPE;

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
import com.opengamma.financial.model.volatility.smile.fitting.sabr.MoneynessPiecewiseSABRSurfaceFitter;
import com.opengamma.financial.model.volatility.smile.fitting.sabr.PiecewiseSABRSurfaceFitter1;
import com.opengamma.financial.model.volatility.smile.fitting.sabr.SmileSurfaceDataBundle;
import com.opengamma.financial.model.volatility.smile.fitting.sabr.StrikePiecewiseSABRSurfaceFitter;
import com.opengamma.financial.model.volatility.surface.BlackVolatilitySurface;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 * 
 */
public abstract class PiecewiseSABRSurfaceFunction extends AbstractFunction.NonCompiledInvoker {
  private final String _instrumentType;

  public PiecewiseSABRSurfaceFunction(final String instrumentType) {
    ArgumentChecker.notNull(instrumentType, "instrument type");
    _instrumentType = instrumentType;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String surfaceType = desiredValue.getConstraint(PROPERTY_SURFACE_TYPE);
    final boolean moneynessSurface = LocalVolatilityPDEUtils.isMoneynessSurface(surfaceType);
    final String xAxis = desiredValue.getConstraint(PROPERTY_X_AXIS);
    final boolean useLogTime = LocalVolatilityPDEUtils.useLogTime(xAxis);
    final String yAxis = desiredValue.getConstraint(PROPERTY_Y_AXIS);
    final boolean useIntegratedVar = LocalVolatilityPDEUtils.useIntegratedVariance(yAxis);
    final String lambdaName = desiredValue.getConstraint(PROPERTY_LAMBDA);
    final double lambda = Double.parseDouble(lambdaName);
    final String surfaceName = desiredValue.getConstraint(ValuePropertyNames.SURFACE);
    final String curveCalculationMethodName = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_METHOD);
    final String forwardCurveInterpolator = desiredValue.getConstraint(PROPERTY_FORWARD_CURVE_INTERPOLATOR);
    final String forwardCurveLeftExtrapolator = desiredValue.getConstraint(PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR);
    final String forwardCurveRightExtrapolator = desiredValue.getConstraint(PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR);
    final PiecewiseSABRSurfaceFitter1<?> surfaceFitter = moneynessSurface ? new MoneynessPiecewiseSABRSurfaceFitter(useLogTime, useIntegratedVar, lambda) :
      new StrikePiecewiseSABRSurfaceFitter(useLogTime, useIntegratedVar, lambda);
    final ValueRequirement volDataRequirement = getVolatilityDataRequirement(target, surfaceName);
    final SmileSurfaceDataBundle data = getData(inputs, volDataRequirement, getForwardCurveRequirement(target, curveCalculationMethodName, forwardCurveInterpolator,
        forwardCurveLeftExtrapolator, forwardCurveRightExtrapolator));
    final BlackVolatilitySurface<?> impliedVolatilitySurface = surfaceFitter.getVolatilitySurface(data);
    final ValueProperties properties = getResultProperties(surfaceName, surfaceType, xAxis, yAxis, lambdaName, curveCalculationMethodName,
        forwardCurveInterpolator, forwardCurveLeftExtrapolator, forwardCurveRightExtrapolator);
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.PIECEWISE_SABR_VOL_SURFACE, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, impliedVolatilitySurface));
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
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.PIECEWISE_SABR_VOL_SURFACE, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> surfaceNames = constraints.getValues(ValuePropertyNames.SURFACE);
    if (surfaceNames == null || surfaceNames.size() != 1) {
      return null;
    }
    final String surfaceName = surfaceNames.iterator().next();
    final Set<String> forwardCurveCalculationMethodNames = constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_METHOD);
    if (forwardCurveCalculationMethodNames == null || forwardCurveCalculationMethodNames.size() != 1) {
      return null;
    }
    final String forwardCurveCalculationMethod = forwardCurveCalculationMethodNames.iterator().next();
    final Set<String> forwardCurveInterpolatorNames = constraints.getValues(PROPERTY_FORWARD_CURVE_INTERPOLATOR);
    if (forwardCurveInterpolatorNames == null || forwardCurveInterpolatorNames.size() != 1) {
      return null;
    }
    final String forwardCurveInterpolator = forwardCurveInterpolatorNames.iterator().next();
    final Set<String> forwardCurveLeftExtrapolatorNames = constraints.getValues(PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR);
    if (forwardCurveLeftExtrapolatorNames == null || forwardCurveLeftExtrapolatorNames.size() != 1) {
      return null;
    }
    final String forwardCurveLeftExtrapolator = forwardCurveLeftExtrapolatorNames.iterator().next();
    final Set<String> forwardCurveRightExtrapolatorNames = constraints.getValues(PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR);
    if (forwardCurveRightExtrapolatorNames == null || forwardCurveRightExtrapolatorNames.size() != 1) {
      return null;
    }
    final ValueRequirement volDataRequirement = getVolatilityDataRequirement(target, surfaceName);
    final String forwardCurveRightExtrapolator = forwardCurveRightExtrapolatorNames.iterator().next();
    return Sets.newHashSet(volDataRequirement, getForwardCurveRequirement(target, forwardCurveCalculationMethod, forwardCurveInterpolator, forwardCurveLeftExtrapolator,
        forwardCurveRightExtrapolator));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    String surfaceName = null;
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
    assert forwardCurveInterpolator != null;
    assert forwardCurveLeftExtrapolator != null;
    assert forwardCurveRightExtrapolator != null;
    final ValueProperties properties = getResultProperties(surfaceName, forwardCurveCalculationMethod, forwardCurveInterpolator,
        forwardCurveLeftExtrapolator, forwardCurveRightExtrapolator);
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.PIECEWISE_SABR_VOL_SURFACE, target.toSpecification(), properties));
  }

  protected abstract SmileSurfaceDataBundle getData(FunctionInputs inputs, ValueRequirement volDataRequirement, ValueRequirement forwardCurveRequirement);

  protected abstract ValueProperties getResultProperties();

  protected abstract ValueProperties getResultProperties(final String definitionName, final String forwardCurveCalculationMethod, final String forwardCurveInterpolator,
      final String forwardCurveLeftExtrapolator, final String forwardCurveRightExtrapolator);

  protected abstract ValueProperties getResultProperties(final String definitionName, final String surfaceType, final String xAxis, final String yAxis, final String lambda,
      final String forwardCurveCalculationMethod, final String forwardCurveInterpolator, final String forwardCurveLeftExtrapolator,
      final String forwardCurveRightExtrapolator);

  private ValueRequirement getForwardCurveRequirement(final ComputationTarget target, final String calculationMethod, final String forwardCurveInterpolator,
      final String forwardCurveLeftExtrapolator, final String forwardCurveRightExtrapolator) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, calculationMethod)
        .with(PROPERTY_FORWARD_CURVE_INTERPOLATOR, forwardCurveInterpolator)
        .with(PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR, forwardCurveLeftExtrapolator)
        .with(PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR, forwardCurveRightExtrapolator).get();
    return new ValueRequirement(ValueRequirementNames.FORWARD_CURVE, target.toSpecification(), properties);
  }

  private ValueRequirement getVolatilityDataRequirement(final ComputationTarget target, final String surfaceName) {
    final ValueRequirement volDataRequirement = new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE_DATA, target.toSpecification(),
        ValueProperties
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(PROPERTY_SURFACE_INSTRUMENT_TYPE, _instrumentType).get());
    return volDataRequirement;
  }
}
