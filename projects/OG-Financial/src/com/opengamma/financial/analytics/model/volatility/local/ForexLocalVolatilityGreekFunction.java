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
import static com.opengamma.financial.analytics.model.volatility.local.LocalVolatilityPDEValuePropertyNames.PROPERTY_MAX_MONEYNESS;
import static com.opengamma.financial.analytics.model.volatility.local.LocalVolatilityPDEValuePropertyNames.PROPERTY_PDE_DIRECTION;
import static com.opengamma.financial.analytics.model.volatility.local.LocalVolatilityPDEValuePropertyNames.PROPERTY_RESULT_STRIKE_INTERPOLATOR;
import static com.opengamma.financial.analytics.model.volatility.local.LocalVolatilityPDEValuePropertyNames.PROPERTY_SPACE_GRID_BUNCHING;
import static com.opengamma.financial.analytics.model.volatility.local.LocalVolatilityPDEValuePropertyNames.PROPERTY_SPACE_STEPS;
import static com.opengamma.financial.analytics.model.volatility.local.LocalVolatilityPDEValuePropertyNames.PROPERTY_SURFACE_TYPE;
import static com.opengamma.financial.analytics.model.volatility.local.LocalVolatilityPDEValuePropertyNames.PROPERTY_THETA;
import static com.opengamma.financial.analytics.model.volatility.local.LocalVolatilityPDEValuePropertyNames.PROPERTY_TIME_GRID_BUNCHING;
import static com.opengamma.financial.analytics.model.volatility.local.LocalVolatilityPDEValuePropertyNames.PROPERTY_TIME_STEPS;
import static com.opengamma.financial.analytics.model.volatility.local.LocalVolatilityPDEValuePropertyNames.PROPERTY_X_AXIS;
import static com.opengamma.financial.analytics.model.volatility.local.LocalVolatilityPDEValuePropertyNames.PROPERTY_Y_AXIS;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
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
import com.opengamma.financial.analytics.model.forex.ForexUtils;
import com.opengamma.financial.analytics.model.forex.ForexVolatilitySurfaceFunction;
import com.opengamma.financial.analytics.volatility.surface.RawVolatilitySurfaceDataFunction;
import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.greeks.PDEGreekResultCollection;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class ForexLocalVolatilityGreekFunction extends AbstractFunction.NonCompiledInvoker {
  private static final String[] GREEK_NAMES = new String[] {
    ValueRequirementNames.LOCAL_VOLATILITY_DELTA,
    ValueRequirementNames.LOCAL_VOLATILITY_DUAL_DELTA,
    ValueRequirementNames.LOCAL_VOLATILITY_DUAL_GAMMA,
    ValueRequirementNames.LOCAL_VOLATILITY_GAMMA,
    ValueRequirementNames.LOCAL_VOLATILITY_VANNA,
    ValueRequirementNames.LOCAL_VOLATILITY_VEGA,
    ValueRequirementNames.LOCAL_VOLATILITY_VOMMA};
  private static final Greek[] GREEKS = new Greek[] {
    PDEGreekResultCollection.GRID_DELTA,
    PDEGreekResultCollection.GRID_DUAL_DELTA,
    PDEGreekResultCollection.GRID_DUAL_GAMMA,
    PDEGreekResultCollection.GRID_GAMMA,
    PDEGreekResultCollection.GRID_VANNA,
    PDEGreekResultCollection.GRID_VEGA,
    PDEGreekResultCollection.GRID_VOMMA};

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final FXOptionSecurity fxOption = (FXOptionSecurity) target.getSecurity();
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String surfaceName = desiredValue.getConstraint(ValuePropertyNames.SURFACE);
    final String surfaceType = desiredValue.getConstraint(PROPERTY_SURFACE_TYPE);
    final String xAxis = desiredValue.getConstraint(PROPERTY_X_AXIS);
    final String yAxis = desiredValue.getConstraint(PROPERTY_Y_AXIS);
    final String lambda = desiredValue.getConstraint(PROPERTY_LAMBDA);
    final String forwardCurveCalculationMethod = desiredValue.getConstraint(CURVE_CALCULATION_METHOD);
    final String forwardCurveInterpolator = desiredValue.getConstraint(PROPERTY_FORWARD_CURVE_INTERPOLATOR);
    final String forwardCurveLeftExtrapolator = desiredValue.getConstraint(PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR);
    final String forwardCurveRightExtrapolator = desiredValue.getConstraint(PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR);
    final String h = desiredValue.getConstraint(PROPERTY_H);
    final String theta = desiredValue.getConstraint(PROPERTY_THETA);
    final String timeSteps = desiredValue.getConstraint(PROPERTY_TIME_STEPS);
    final String spaceSteps = desiredValue.getConstraint(PROPERTY_SPACE_STEPS);
    final String timeGridBunching = desiredValue.getConstraint(PROPERTY_TIME_GRID_BUNCHING);
    final String spaceGridBunching = desiredValue.getConstraint(PROPERTY_SPACE_GRID_BUNCHING);
    final String maxMoneyness = desiredValue.getConstraint(PROPERTY_MAX_MONEYNESS);
    final String pdeDirection = desiredValue.getConstraint(PROPERTY_PDE_DIRECTION);
    if (!(pdeDirection.equals(LocalVolatilityPDEValuePropertyNames.FORWARD_PDE))) {
      throw new OpenGammaRuntimeException("Can only use forward PDE; should never ask for this direction: " + pdeDirection);
    }
    String strikeInterpolatorName = null;
    for (final ValueRequirement value : desiredValues) {
      if (value.getConstraints().getValues(PROPERTY_RESULT_STRIKE_INTERPOLATOR) != null && value.getConstraints().getValues(PROPERTY_RESULT_STRIKE_INTERPOLATOR).size() == 1) {
        strikeInterpolatorName = value.getConstraint(PROPERTY_RESULT_STRIKE_INTERPOLATOR);
        break;
      }
    }
    if (strikeInterpolatorName == null) {
      throw new OpenGammaRuntimeException("Could not get strike intepolator name; should never happen");
    }
    final Interpolator1D strikeInterpolator = Interpolator1DFactory.getInterpolator(strikeInterpolatorName);
    final ValueRequirement gridGreekRequirement = getGridGreeksRequirement(target, surfaceName, surfaceType, xAxis, yAxis, lambda,
        forwardCurveCalculationMethod, h, forwardCurveInterpolator, forwardCurveLeftExtrapolator, forwardCurveRightExtrapolator,
        theta, timeSteps, spaceSteps, timeGridBunching, spaceGridBunching, maxMoneyness, pdeDirection);
    final Object gridGreeksObject = inputs.getValue(gridGreekRequirement);
    if (gridGreeksObject == null) {
      throw new OpenGammaRuntimeException("Grid greeks were null");
    }
    final double strike = getStrike(fxOption);
    final PDEGreekResultCollection gridGreeks = (PDEGreekResultCollection) gridGreeksObject;
    final ValueProperties properties = getResultProperties(surfaceName, surfaceType, xAxis, yAxis, lambda, forwardCurveCalculationMethod, h,
        forwardCurveInterpolator, forwardCurveLeftExtrapolator, forwardCurveRightExtrapolator, theta, timeSteps, spaceSteps, timeGridBunching,
        spaceGridBunching, maxMoneyness, pdeDirection, strikeInterpolatorName);
    final ComputationTargetSpecification spec = target.toSpecification();
    final Set<ComputedValue> result = new HashSet<ComputedValue>();
    for (int i = 0; i < GREEKS.length; i++) {
      final Double value = gridGreeks.getPointGreek(GREEKS[i], strike, strikeInterpolator);
      if (value == null) {
        throw new OpenGammaRuntimeException("Grid greeks for " + GREEKS[i] + " were null");
      }
      final ValueSpecification resultSpec = new ValueSpecification(GREEK_NAMES[i], spec, properties);
      result.add(new ComputedValue(resultSpec, value));
    }
    return result;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getType() == ComputationTargetType.SECURITY && target.getSecurity() instanceof FXOptionSecurity;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final Set<ValueSpecification> results = new HashSet<ValueSpecification>();
    final ValueProperties properties = createValueProperties()
        .withAny(RawVolatilitySurfaceDataFunction.PROPERTY_SURFACE_INSTRUMENT_TYPE)
        .with(ValuePropertyNames.CALCULATION_METHOD, LocalVolatilityPDEValuePropertyNames.LOCAL_VOLATILITY_METHOD)
        .withAny(ValuePropertyNames.SURFACE)
        .withAny(PROPERTY_SURFACE_TYPE)
        .withAny(PROPERTY_X_AXIS)
        .withAny(PROPERTY_Y_AXIS)
        .withAny(PROPERTY_LAMBDA)
        .withAny(CURVE_CALCULATION_METHOD)
        .withAny(PROPERTY_FORWARD_CURVE_INTERPOLATOR)
        .withAny(PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR)
        .withAny(PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR)
        .withAny(PROPERTY_THETA)
        .withAny(PROPERTY_TIME_STEPS)
        .withAny(PROPERTY_SPACE_STEPS)
        .withAny(PROPERTY_TIME_GRID_BUNCHING)
        .withAny(PROPERTY_SPACE_GRID_BUNCHING)
        .withAny(PROPERTY_MAX_MONEYNESS)
        .withAny(PROPERTY_H)
        .withAny(PROPERTY_PDE_DIRECTION)
        .withAny(PROPERTY_RESULT_STRIKE_INTERPOLATOR)
        .get();
    final ComputationTargetSpecification specification = target.toSpecification();
    for (final String requirement : GREEK_NAMES) {
      results.add(new ValueSpecification(requirement, specification, properties));
    }
    return results;
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
    final Set<String> forwardCurveCalculationMethodNames = constraints.getValues(CURVE_CALCULATION_METHOD);
    if (forwardCurveCalculationMethodNames == null || forwardCurveCalculationMethodNames.size() != 1) {
      return null;
    }
    final Set<String> hNames = constraints.getValues(PROPERTY_H);
    if (hNames == null || hNames.size() != 1) {
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
    final Set<String> thetaNames = constraints.getValues(PROPERTY_THETA);
    if (thetaNames == null || thetaNames.size() != 1) {
      return null;
    }
    final Set<String> timeStepsNames = constraints.getValues(PROPERTY_TIME_STEPS);
    if (timeStepsNames == null || timeStepsNames.size() != 1) {
      return null;
    }
    final Set<String> spaceStepsNames = constraints.getValues(PROPERTY_SPACE_STEPS);
    if (spaceStepsNames == null || spaceStepsNames.size() != 1) {
      return null;
    }
    final Set<String> timeGridBunchingNames = constraints.getValues(PROPERTY_TIME_GRID_BUNCHING);
    if (timeGridBunchingNames == null || timeGridBunchingNames.size() != 1) {
      return null;
    }
    final Set<String> spaceGridBunchingNames = constraints.getValues(PROPERTY_SPACE_GRID_BUNCHING);
    if (spaceGridBunchingNames == null || spaceGridBunchingNames.size() != 1) {
      return null;
    }
    final Set<String> maxMoneynessNames = constraints.getValues(PROPERTY_MAX_MONEYNESS);
    if (maxMoneynessNames == null || maxMoneynessNames.size() != 1) {
      return null;
    }
    final Set<String> pdeDirectionNames = constraints.getValues(PROPERTY_PDE_DIRECTION);
    if (pdeDirectionNames == null || pdeDirectionNames.size() != 1) {
      return null;
    }
    final Set<String> strikeInterpolatorNames = constraints.getValues(PROPERTY_RESULT_STRIKE_INTERPOLATOR);
    if (strikeInterpolatorNames == null || strikeInterpolatorNames.size() != 1) {
      return null;
    }
    final String surfaceType = surfaceTypeNames.iterator().next();
    final String xAxis = xAxisNames.iterator().next();
    final String yAxis = yAxisNames.iterator().next();
    final String lambda = lambdaNames.iterator().next();
    final String forwardCurveCalculationMethod = forwardCurveCalculationMethodNames.iterator().next();
    final String h = hNames.iterator().next();
    final String forwardCurveInterpolator = forwardCurveInterpolatorNames.iterator().next();
    final String forwardCurveLeftExtrapolator = forwardCurveLeftExtrapolatorNames.iterator().next();
    final String forwardCurveRightExtrapolator = forwardCurveRightExtrapolatorNames.iterator().next();
    final String theta = thetaNames.iterator().next();
    final String timeSteps = timeStepsNames.iterator().next();
    final String spaceSteps = spaceStepsNames.iterator().next();
    final String timeGridBunching = timeGridBunchingNames.iterator().next();
    final String spaceGridBunching = spaceGridBunchingNames.iterator().next();
    final String maxMoneyness = maxMoneynessNames.iterator().next();
    final String pdeDirection = pdeDirectionNames.iterator().next();
    final String surfaceName = surfaceNames.iterator().next();
    final ValueRequirement greeksSpec = getGridGreeksRequirement(target, surfaceName, surfaceType, xAxis, yAxis, lambda, forwardCurveCalculationMethod, h, forwardCurveInterpolator,
        forwardCurveLeftExtrapolator, forwardCurveRightExtrapolator, theta, timeSteps, spaceSteps, timeGridBunching, spaceGridBunching, maxMoneyness, pdeDirection);
    return Collections.singleton(greeksSpec);
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
    String h = null;
    String theta = null;
    String timeSteps = null;
    String spaceSteps = null;
    String timeGridBunching = null;
    String spaceGridBunching = null;
    String maxMoneyness = null;
    String pdeDirection = null;
    for (final Map.Entry<ValueSpecification, ValueRequirement> input : inputs.entrySet()) {
      final ValueProperties constraints = input.getValue().getConstraints();
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
      if (constraints.getValues(CURVE_CALCULATION_METHOD) != null) {
        final Set<String> forwardCurveCalculationMethodNames = constraints.getValues(CURVE_CALCULATION_METHOD);
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
      if (constraints.getValues(PROPERTY_H) != null) {
        final Set<String> hNames = constraints.getValues(PROPERTY_H);
        if (hNames == null || hNames.size() != 1) {
          throw new OpenGammaRuntimeException("Missing or non-unique h name");
        }
        h = hNames.iterator().next();
      }
      if (constraints.getValues(PROPERTY_THETA) != null) {
        final Set<String> thetaNames = constraints.getValues(PROPERTY_THETA);
        if (thetaNames == null || thetaNames.size() != 1) {
          throw new OpenGammaRuntimeException("Missing or non-unique theta name");
        }
        theta = thetaNames.iterator().next();
      }
      if (constraints.getValues(PROPERTY_TIME_STEPS) != null) {
        final Set<String> timeStepsNames = constraints.getValues(PROPERTY_TIME_STEPS);
        if (timeStepsNames == null || timeStepsNames.size() != 1) {
          throw new OpenGammaRuntimeException("Missing or non-unique time steps name");
        }
        timeSteps = timeStepsNames.iterator().next();
      }
      if (constraints.getValues(PROPERTY_SPACE_STEPS) != null) {
        final Set<String> spaceStepsNames = constraints.getValues(PROPERTY_SPACE_STEPS);
        if (spaceStepsNames == null || spaceStepsNames.size() != 1) {
          throw new OpenGammaRuntimeException("Missing or non-unique space steps name");
        }
        spaceSteps = spaceStepsNames.iterator().next();
      }
      if (constraints.getValues(PROPERTY_TIME_GRID_BUNCHING) != null) {
        final Set<String> timeGridBunchingNames = constraints.getValues(PROPERTY_TIME_GRID_BUNCHING);
        if (timeGridBunchingNames == null || timeGridBunchingNames.size() != 1) {
          throw new OpenGammaRuntimeException("Missing or non-unique time grid bunching name");
        }
        timeGridBunching = timeGridBunchingNames.iterator().next();
      }
      if (constraints.getValues(PROPERTY_SPACE_GRID_BUNCHING) != null) {
        final Set<String> spaceGridBunchingNames = constraints.getValues(PROPERTY_SPACE_GRID_BUNCHING);
        if (spaceGridBunchingNames == null || spaceGridBunchingNames.size() != 1) {
          throw new OpenGammaRuntimeException("Missing or non-unique space grid bunching name");
        }
        spaceGridBunching = spaceGridBunchingNames.iterator().next();
      }
      if (constraints.getValues(PROPERTY_MAX_MONEYNESS) != null) {
        final Set<String> maxMoneynessNames = constraints.getValues(PROPERTY_MAX_MONEYNESS);
        if (maxMoneynessNames == null || maxMoneynessNames.size() != 1) {
          throw new OpenGammaRuntimeException("Missing or non-unique max. moneyness name");
        }
        maxMoneyness = maxMoneynessNames.iterator().next();
      }
      if (constraints.getValues(PROPERTY_PDE_DIRECTION) != null) {
        final Set<String> pdeDirectionNames = constraints.getValues(PROPERTY_PDE_DIRECTION);
        if (pdeDirectionNames == null || pdeDirectionNames.size() != 1) {
          throw new OpenGammaRuntimeException("Missing or non-unique PDE direction name");
        }
        pdeDirection = pdeDirectionNames.iterator().next();
      }
    }
    assert surfaceName != null;
    assert surfaceType != null;
    assert xAxis != null;
    assert yAxis != null;
    assert lambda != null;
    assert forwardCurveCalculationMethod != null;
    assert h != null;
    assert forwardCurveInterpolator != null;
    assert forwardCurveLeftExtrapolator != null;
    assert forwardCurveRightExtrapolator != null;
    assert theta != null;
    assert timeSteps != null;
    assert spaceSteps != null;
    assert timeGridBunching != null;
    assert spaceGridBunching != null;
    assert maxMoneyness != null;
    assert pdeDirection != null;
    final ComputationTargetSpecification specification = target.toSpecification();
    final ValueProperties properties = getResultProperties(surfaceName, surfaceType, xAxis, yAxis, lambda, forwardCurveCalculationMethod, h,
        forwardCurveInterpolator, forwardCurveLeftExtrapolator, forwardCurveRightExtrapolator, theta, timeSteps, spaceSteps,
        timeGridBunching, spaceGridBunching, maxMoneyness, pdeDirection);
    final Set<ValueSpecification> results = new HashSet<ValueSpecification>();
    for (final String requirement : GREEK_NAMES) {
      results.add(new ValueSpecification(requirement, specification, properties));
    }
    return results;

  }

  private ValueRequirement getGridGreeksRequirement(final ComputationTarget target, final String surfaceName, final String surfaceType, final String xAxis, final String yAxis,
      final String lambda, final String forwardCurveCalculationMethod, final String h, final String forwardCurveInterpolator, final String forwardCurveLeftExtrapolator,
      final String forwardCurveRightExtrapolator, final String theta, final String timeSteps, final String spaceSteps, final String timeGridBunching,
      final String spaceGridBunching, final String maxMoneyness, final String pdeDirection) {
    final ValueProperties properties = getGridGreekProperties(surfaceName, surfaceType, xAxis, yAxis, lambda, forwardCurveCalculationMethod, h, forwardCurveInterpolator,
        forwardCurveLeftExtrapolator, forwardCurveRightExtrapolator, theta, timeSteps, spaceSteps, timeGridBunching, spaceGridBunching, maxMoneyness, pdeDirection);
    return new ValueRequirement(ValueRequirementNames.LOCAL_VOLATILITY_PDE_GREEKS, target.toSpecification(), properties);
  }

  private ValueProperties getGridGreekProperties(final String surfaceName, final String surfaceType, final String xAxis, final String yAxis, final String lambda,
      final String forwardCurveCalculationMethod, final String h, final String forwardCurveInterpolator, final String forwardCurveLeftExtrapolator,
      final String forwardCurveRightExtrapolator, final String theta, final String timeSteps, final String spaceSteps, final String timeGridBunching,
      final String spaceGridBunching, final String maxMoneyness, final String pdeDirection) {
    return ValueProperties.builder()
        .with(RawVolatilitySurfaceDataFunction.PROPERTY_SURFACE_INSTRUMENT_TYPE, ForexVolatilitySurfaceFunction.INSTRUMENT_TYPE)
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(PROPERTY_SURFACE_TYPE, surfaceType)
        .with(PROPERTY_X_AXIS, xAxis)
        .with(PROPERTY_Y_AXIS, yAxis)
        .with(PROPERTY_LAMBDA, lambda)
        .with(CURVE_CALCULATION_METHOD, forwardCurveCalculationMethod)
        .with(PROPERTY_FORWARD_CURVE_INTERPOLATOR, forwardCurveInterpolator)
        .with(PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR, forwardCurveLeftExtrapolator)
        .with(PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR, forwardCurveRightExtrapolator)
        .with(PROPERTY_THETA, theta)
        .with(PROPERTY_TIME_STEPS, timeSteps)
        .with(PROPERTY_SPACE_STEPS, spaceSteps)
        .with(PROPERTY_TIME_GRID_BUNCHING, timeGridBunching)
        .with(PROPERTY_SPACE_GRID_BUNCHING, spaceGridBunching)
        .with(PROPERTY_MAX_MONEYNESS, maxMoneyness)
        .with(PROPERTY_H, h)
        .with(PROPERTY_PDE_DIRECTION, pdeDirection)
        .get();
  }

  private ValueProperties getResultProperties(final String surfaceName, final String surfaceType, final String xAxis, final String yAxis, final String lambda,
      final String forwardCurveCalculationMethod, final String h, final String forwardCurveInterpolator, final String forwardCurveLeftExtrapolator,
      final String forwardCurveRightExtrapolator, final String theta, final String timeSteps, final String spaceSteps, final String timeGridBunching,
      final String spaceGridBunching, final String maxMoneyness, final String pdeDirection) {
    return createValueProperties()
        .with(RawVolatilitySurfaceDataFunction.PROPERTY_SURFACE_INSTRUMENT_TYPE, ForexVolatilitySurfaceFunction.INSTRUMENT_TYPE)
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(ValuePropertyNames.CALCULATION_METHOD, LocalVolatilityPDEValuePropertyNames.LOCAL_VOLATILITY_METHOD)
        .with(PROPERTY_SURFACE_TYPE, surfaceType)
        .with(PROPERTY_X_AXIS, xAxis)
        .with(PROPERTY_Y_AXIS, yAxis)
        .with(PROPERTY_LAMBDA, lambda)
        .with(CURVE_CALCULATION_METHOD, forwardCurveCalculationMethod)
        .with(PROPERTY_FORWARD_CURVE_INTERPOLATOR, forwardCurveInterpolator)
        .with(PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR, forwardCurveLeftExtrapolator)
        .with(PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR, forwardCurveRightExtrapolator)
        .with(PROPERTY_THETA, theta)
        .with(PROPERTY_TIME_STEPS, timeSteps)
        .with(PROPERTY_SPACE_STEPS, spaceSteps)
        .with(PROPERTY_TIME_GRID_BUNCHING, timeGridBunching)
        .with(PROPERTY_SPACE_GRID_BUNCHING, spaceGridBunching)
        .with(PROPERTY_MAX_MONEYNESS, maxMoneyness)
        .with(PROPERTY_H, h)
        .with(PROPERTY_PDE_DIRECTION, pdeDirection)
        .withAny(PROPERTY_RESULT_STRIKE_INTERPOLATOR)
        .get();
  }

  private ValueProperties getResultProperties(final String surfaceName, final String surfaceType, final String xAxis, final String yAxis, final String lambda,
      final String forwardCurveCalculationMethod, final String h, final String forwardCurveInterpolator, final String forwardCurveLeftExtrapolator,
      final String forwardCurveRightExtrapolator, final String theta, final String timeSteps, final String spaceSteps, final String timeGridBunching,
      final String spaceGridBunching, final String maxMoneyness, final String pdeDirection, final String strikeInterpolatorName) {
    return createValueProperties()
        .with(RawVolatilitySurfaceDataFunction.PROPERTY_SURFACE_INSTRUMENT_TYPE, ForexVolatilitySurfaceFunction.INSTRUMENT_TYPE)
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(ValuePropertyNames.CALCULATION_METHOD, LocalVolatilityPDEValuePropertyNames.LOCAL_VOLATILITY_METHOD)
        .with(PROPERTY_SURFACE_TYPE, surfaceType)
        .with(PROPERTY_X_AXIS, xAxis)
        .with(PROPERTY_Y_AXIS, yAxis)
        .with(PROPERTY_LAMBDA, lambda)
        .with(CURVE_CALCULATION_METHOD, forwardCurveCalculationMethod)
        .with(PROPERTY_FORWARD_CURVE_INTERPOLATOR, forwardCurveInterpolator)
        .with(PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR, forwardCurveLeftExtrapolator)
        .with(PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR, forwardCurveRightExtrapolator)
        .with(PROPERTY_THETA, theta)
        .with(PROPERTY_TIME_STEPS, timeSteps)
        .with(PROPERTY_SPACE_STEPS, spaceSteps)
        .with(PROPERTY_TIME_GRID_BUNCHING, timeGridBunching)
        .with(PROPERTY_SPACE_GRID_BUNCHING, spaceGridBunching)
        .with(PROPERTY_MAX_MONEYNESS, maxMoneyness)
        .with(PROPERTY_H, h)
        .with(PROPERTY_PDE_DIRECTION, pdeDirection)
        .with(PROPERTY_RESULT_STRIKE_INTERPOLATOR, strikeInterpolatorName)
        .get();
  }

  private double getStrike(final FXOptionSecurity fxOption) {
    final Currency putCurrency = fxOption.getPutCurrency();
    final Currency callCurrency = fxOption.getCallCurrency();
    if (ForexUtils.isBaseCurrency(putCurrency, callCurrency)) {
      return fxOption.getPutAmount() / fxOption.getCallAmount(); //TODO check this
    }
    return fxOption.getCallAmount() / fxOption.getPutAmount();
  }
}
