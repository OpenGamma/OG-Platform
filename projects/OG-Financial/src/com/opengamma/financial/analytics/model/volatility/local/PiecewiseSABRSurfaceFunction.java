/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.local;

import static com.opengamma.financial.analytics.fxforwardcurve.InterpolatedForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_INTERPOLATOR;
import static com.opengamma.financial.analytics.fxforwardcurve.InterpolatedForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR;
import static com.opengamma.financial.analytics.fxforwardcurve.InterpolatedForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR;
import static com.opengamma.financial.analytics.model.volatility.local.LocalVolatilityPDEValuePropertyNames.PROPERTY_LAMBDA;
import static com.opengamma.financial.analytics.model.volatility.local.LocalVolatilityPDEValuePropertyNames.PROPERTY_SURFACE_TYPE;
import static com.opengamma.financial.analytics.model.volatility.local.LocalVolatilityPDEValuePropertyNames.PROPERTY_X_AXIS;
import static com.opengamma.financial.analytics.model.volatility.local.LocalVolatilityPDEValuePropertyNames.PROPERTY_Y_AXIS;
import static com.opengamma.financial.analytics.volatility.surface.RawVolatilitySurfaceDataFunction.PROPERTY_SURFACE_INSTRUMENT_TYPE;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
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
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.volatility.surface.ConfigDBVolatilitySurfaceDefinitionSource;
import com.opengamma.financial.analytics.volatility.surface.ConfigDBVolatilitySurfaceSpecificationSource;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceDefinition;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceSpecification;
import com.opengamma.financial.model.volatility.smile.fitting.sabr.MoneynessPiecewiseSABRSurfaceFitter;
import com.opengamma.financial.model.volatility.smile.fitting.sabr.PiecewiseSABRSurfaceFitter1;
import com.opengamma.financial.model.volatility.smile.fitting.sabr.SmileSurfaceDataBundle;
import com.opengamma.financial.model.volatility.smile.fitting.sabr.StrikePiecewiseSABRSurfaceFitter;
import com.opengamma.financial.model.volatility.surface.BlackVolatilitySurface;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public abstract class PiecewiseSABRSurfaceFunction extends AbstractFunction.NonCompiledInvoker {
  private final String _definitionName;
  private final String _specificationName;
  private final String _instrumentType;
  private VolatilitySurfaceSpecification _specification;
  private VolatilitySurfaceDefinition<?, ?> _definition;
  private ValueRequirement _volDataRequirement;

  public PiecewiseSABRSurfaceFunction(final String definitionName, final String specificationName, final String instrumentType) {
    ArgumentChecker.notNull(definitionName, "definition name");
    ArgumentChecker.notNull(specificationName, "specification name");
    ArgumentChecker.notNull(instrumentType, "instrument type");
    _definitionName = definitionName;
    _specificationName = specificationName;
    _instrumentType = instrumentType;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    final ConfigDBVolatilitySurfaceDefinitionSource volSurfaceDefinitionSource = new ConfigDBVolatilitySurfaceDefinitionSource(configSource);
    _definition = volSurfaceDefinitionSource.getDefinition(_definitionName, _instrumentType);
    if (_definition == null) {
      throw new OpenGammaRuntimeException("Couldn't find Volatility Surface Definition for " + _instrumentType + " called " + _definitionName);
    }
    final ConfigDBVolatilitySurfaceSpecificationSource volatilitySurfaceSpecificationSource = new ConfigDBVolatilitySurfaceSpecificationSource(configSource);
    _specification = volatilitySurfaceSpecificationSource.getSpecification(_specificationName, _instrumentType);
    if (_specification == null) {
      throw new OpenGammaRuntimeException("Couldn't find Volatility Surface Specification for " + _instrumentType + " called " + _specificationName);
    }
    _volDataRequirement = new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE_DATA, _definition.getTarget(),
        ValueProperties
        .with(ValuePropertyNames.SURFACE, _definitionName)
        .with(PROPERTY_SURFACE_INSTRUMENT_TYPE, _instrumentType).get());
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
    final String curveCalculationMethodName = desiredValue.getConstraint(ValuePropertyNames.CALCULATION_METHOD);
    final String forwardCurveInterpolator = desiredValue.getConstraint(PROPERTY_FORWARD_CURVE_INTERPOLATOR);
    final String forwardCurveLeftExtrapolator = desiredValue.getConstraint(PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR);
    final String forwardCurveRightExtrapolator = desiredValue.getConstraint(PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR);
    final PiecewiseSABRSurfaceFitter1<?> surfaceFitter = moneynessSurface ? new MoneynessPiecewiseSABRSurfaceFitter(useLogTime, useIntegratedVar, lambda) :
      new StrikePiecewiseSABRSurfaceFitter(useLogTime, useIntegratedVar, lambda);
    final SmileSurfaceDataBundle data = getData(inputs, _volDataRequirement, getForwardCurveRequirement(curveCalculationMethodName, forwardCurveInterpolator,
        forwardCurveLeftExtrapolator, forwardCurveRightExtrapolator));
    final BlackVolatilitySurface<?> impliedVolatilitySurface = surfaceFitter.getVolatilitySurface(data);
    final ValueProperties properties = getResultProperties(_definitionName, surfaceType, xAxis, yAxis, lambdaName, curveCalculationMethodName,
        forwardCurveInterpolator, forwardCurveLeftExtrapolator, forwardCurveRightExtrapolator);
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.PIECEWISE_SABR_VOL_SURFACE, new ComputationTargetSpecification(_definition.getTarget()), properties);
    return Collections.singleton(new ComputedValue(spec, impliedVolatilitySurface));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PRIMITIVE;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getType() == ComputationTargetType.PRIMITIVE && ObjectUtils.equals(target.getUniqueId(), _definitionName);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = getResultProperties();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.PIECEWISE_SABR_VOL_SURFACE, new ComputationTargetSpecification(_definition.getTarget()), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
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
    final String forwardCurveRightExtrapolator = forwardCurveRightExtrapolatorNames.iterator().next();
    return Sets.newHashSet(_volDataRequirement, getForwardCurveRequirement(forwardCurveCalculationMethod, forwardCurveInterpolator, forwardCurveLeftExtrapolator,
        forwardCurveRightExtrapolator));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
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
    }
    assert surfaceType != null;
    assert xAxis != null;
    assert yAxis != null;
    assert lambda != null;
    assert forwardCurveCalculationMethod != null;
    assert forwardCurveInterpolator != null;
    assert forwardCurveLeftExtrapolator != null;
    assert forwardCurveRightExtrapolator != null;
    final ValueProperties properties = getResultProperties(_definitionName, surfaceType, xAxis, yAxis, lambda, forwardCurveCalculationMethod, forwardCurveInterpolator,
        forwardCurveLeftExtrapolator, forwardCurveRightExtrapolator);
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.PIECEWISE_SABR_VOL_SURFACE, new ComputationTargetSpecification(_definition.getTarget()), properties));
  }

  protected abstract SmileSurfaceDataBundle getData(FunctionInputs inputs, ValueRequirement volDataRequirement, ValueRequirement forwardCurveRequirement);

  protected abstract ValueProperties getResultProperties();

  protected abstract ValueProperties getResultProperties(final String definitionName, final String surfaceType, final String xAxis, final String yAxis, final String lambda,
      final String forwardCurveCalculationMethod, final String forwardCurveInterpolator, final String forwardCurveLeftExtrapolator, final String forwardCurveRightExtrapolator);

  private ValueRequirement getForwardCurveRequirement(final String calculationMethod, final String forwardCurveInterpolator, final String forwardCurveLeftExtrapolator,
      final String forwardCurveRightExtrapolator) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, calculationMethod)
        .with(PROPERTY_FORWARD_CURVE_INTERPOLATOR, forwardCurveInterpolator)
        .with(PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR, forwardCurveLeftExtrapolator)
        .with(PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR, forwardCurveRightExtrapolator).get();
    return new ValueRequirement(ValueRequirementNames.FORWARD_CURVE, _definition.getTarget(), properties);
  }
}
