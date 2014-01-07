/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.local.deprecated;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.SURFACE;
import static com.opengamma.financial.analytics.model.volatility.local.deprecated.LocalVolatilityPDEValuePropertyNames.PROPERTY_H;
import static com.opengamma.financial.analytics.model.volatility.local.deprecated.LocalVolatilityPDEValuePropertyNames.PROPERTY_MAX_MONEYNESS;
import static com.opengamma.financial.analytics.model.volatility.local.deprecated.LocalVolatilityPDEValuePropertyNames.PROPERTY_PDE_DIRECTION;
import static com.opengamma.financial.analytics.model.volatility.local.deprecated.LocalVolatilityPDEValuePropertyNames.PROPERTY_RESULT_STRIKE_INTERPOLATOR;
import static com.opengamma.financial.analytics.model.volatility.local.deprecated.LocalVolatilityPDEValuePropertyNames.PROPERTY_RESULT_TIME_INTERPOLATOR;
import static com.opengamma.financial.analytics.model.volatility.local.deprecated.LocalVolatilityPDEValuePropertyNames.PROPERTY_SPACE_GRID_BUNCHING;
import static com.opengamma.financial.analytics.model.volatility.local.deprecated.LocalVolatilityPDEValuePropertyNames.PROPERTY_SPACE_STEPS;
import static com.opengamma.financial.analytics.model.volatility.local.deprecated.LocalVolatilityPDEValuePropertyNames.PROPERTY_SURFACE_TYPE;
import static com.opengamma.financial.analytics.model.volatility.local.deprecated.LocalVolatilityPDEValuePropertyNames.PROPERTY_THETA;
import static com.opengamma.financial.analytics.model.volatility.local.deprecated.LocalVolatilityPDEValuePropertyNames.PROPERTY_TIME_GRID_BUNCHING;
import static com.opengamma.financial.analytics.model.volatility.local.deprecated.LocalVolatilityPDEValuePropertyNames.PROPERTY_TIME_STEPS;
import static com.opengamma.financial.analytics.model.volatility.local.deprecated.LocalVolatilityPDEValuePropertyNames.PROPERTY_X_AXIS;
import static com.opengamma.financial.analytics.model.volatility.local.deprecated.LocalVolatilityPDEValuePropertyNames.PROPERTY_Y_AXIS;
import static com.opengamma.financial.analytics.model.volatility.local.deprecated.LocalVolatilityPDEValuePropertyNames.PROPERTY_Y_AXIS_TYPE;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.Clock;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.model.finitedifference.PDEFullResults1D;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SurfaceArrayUtils;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 * @deprecated Deprecated
 */
@Deprecated
public class ForexLocalVolatilityPDEPriceFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
    final FXOptionSecurity fxOption = (FXOptionSecurity) target.getSecurity();
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String surfaceName = desiredValue.getConstraint(SURFACE);
    final String surfaceType = desiredValue.getConstraint(PROPERTY_SURFACE_TYPE);
    final String xAxis = desiredValue.getConstraint(PROPERTY_X_AXIS);
    final String yAxis = desiredValue.getConstraint(PROPERTY_Y_AXIS);
    final String yAxisType = desiredValue.getConstraint(PROPERTY_Y_AXIS_TYPE);
    final String forwardCurveCalculationMethod = desiredValue.getConstraint(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD);
    final String forwardCurveName = desiredValue.getConstraint(CURVE);
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
    final String strikeInterpolatorName = desiredValue.getConstraint(PROPERTY_RESULT_STRIKE_INTERPOLATOR);
    final String timeInterpolatorName = desiredValue.getConstraint(PROPERTY_RESULT_TIME_INTERPOLATOR);
    final ValueRequirement gridRequirement = getPDEGridRequirement(target, surfaceName, surfaceType, xAxis, yAxis, yAxisType,
        forwardCurveCalculationMethod, h, forwardCurveName,
        theta, timeSteps, spaceSteps, timeGridBunching, spaceGridBunching, maxMoneyness, pdeDirection);
    final Object pdeGridObject = inputs.getValue(gridRequirement);
    if (pdeGridObject == null) {
      throw new OpenGammaRuntimeException("PDE grid was null");
    }
    final PDEFullResults1D pdeGrid = (PDEFullResults1D) pdeGridObject;
    final double[] gridTimes = pdeGrid.getGrid().getTimeNodes();
    final double[] gridMoneyness = pdeGrid.getGrid().getSpaceNodes();
    final CurrencyPairs currencyPairs = OpenGammaExecutionContext.getCurrencyPairsSource(executionContext).getCurrencyPairs(CurrencyPairs.DEFAULT_CURRENCY_PAIRS);
    final CurrencyPair currencyPair = currencyPairs.getCurrencyPair(fxOption.getPutCurrency(), fxOption.getCallCurrency());
    //TODO interpolate
    ///////////////////////////////
    final double tau = getExpiry(fxOption, now);
    final UnorderedCurrencyPair currencies = UnorderedCurrencyPair.of(fxOption.getCallCurrency(), fxOption.getPutCurrency());
    final ValueRequirement forwardCurveRequirement = getForwardCurveRequirement(forwardCurveCalculationMethod, forwardCurveName, currencies);
    final Object forwardCurveObject = inputs.getValue(forwardCurveRequirement);
    if (forwardCurveObject == null) {
      throw new OpenGammaRuntimeException("Forward curve was null");
    }
    final ForwardCurve forwardCurve = (ForwardCurve) forwardCurveObject;
    final double forward = forwardCurve.getForward(tau);
    final double moneyness = getStrike(fxOption, currencyPair) / forward;
    final int timeIndex = SurfaceArrayUtils.getLowerBoundIndex(gridTimes, tau);
    final int spaceIndex = SurfaceArrayUtils.getLowerBoundIndex(gridMoneyness, moneyness);

    final double value1 = forward * pdeGrid.getFunctionValue(spaceIndex, timeIndex);
    final double value2 = forward * pdeGrid.getFunctionValue(spaceIndex + 1, timeIndex);
    final double m1 = pdeGrid.getSpaceValue(spaceIndex);
    final double m2 = pdeGrid.getSpaceValue(spaceIndex + 1);
    final double value = ((m2 - moneyness) * value1 + (moneyness - m1) * value2) / (m2 - m1);

    ///////////////////////////////
    final ValueSpecification resultSpec = new ValueSpecification(desiredValue.getValueName(), target.toSpecification(), desiredValue.getConstraints());
    return Collections.singleton(new ComputedValue(resultSpec, value));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.FX_OPTION_SECURITY;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties()
        .withAny(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE) //TODO
        .with(ValuePropertyNames.CALCULATION_METHOD, LocalVolatilityPDEValuePropertyNames.LOCAL_VOLATILITY_METHOD)
        .withAny(SURFACE)
        .withAny(PROPERTY_SURFACE_TYPE)
        .withAny(PROPERTY_X_AXIS)
        .withAny(PROPERTY_Y_AXIS)
        .withAny(PROPERTY_Y_AXIS_TYPE)
        .withAny(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD)
        .withAny(CURVE)
        .withAny(PROPERTY_THETA)
        .withAny(PROPERTY_TIME_STEPS)
        .withAny(PROPERTY_SPACE_STEPS)
        .withAny(PROPERTY_TIME_GRID_BUNCHING)
        .withAny(PROPERTY_SPACE_GRID_BUNCHING)
        .withAny(PROPERTY_MAX_MONEYNESS)
        .withAny(PROPERTY_H)
        .withAny(PROPERTY_PDE_DIRECTION)
        .withAny(PROPERTY_RESULT_STRIKE_INTERPOLATOR)
        .withAny(PROPERTY_RESULT_TIME_INTERPOLATOR)
        .get();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.PRESENT_VALUE, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> surfaceNames = constraints.getValues(SURFACE);
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
    final Set<String> forwardCurveCalculationMethodNames = constraints.getValues(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD);
    if (forwardCurveCalculationMethodNames == null || forwardCurveCalculationMethodNames.size() != 1) {
      return null;
    }
    final Set<String> hNames = constraints.getValues(PROPERTY_H);
    if (hNames == null || hNames.size() != 1) {
      return null;
    }
    final Set<String> forwardCurveNames = constraints.getValues(CURVE);
    if (forwardCurveNames == null || forwardCurveNames.size() != 1) {
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
    final String surfaceName = surfaceNames.iterator().next();
    final String surfaceType = surfaceTypeNames.iterator().next();
    final String xAxis = xAxisNames.iterator().next();
    final String yAxis = yAxisNames.iterator().next();
    final String yAxisType = yAxisTypeNames.iterator().next();
    final String forwardCurveCalculationMethod = forwardCurveCalculationMethodNames.iterator().next();
    final String h = hNames.iterator().next();
    final String forwardCurveName = forwardCurveNames.iterator().next();
    final String theta = thetaNames.iterator().next();
    final String timeSteps = timeStepsNames.iterator().next();
    final String spaceSteps = spaceStepsNames.iterator().next();
    final String timeGridBunching = timeGridBunchingNames.iterator().next();
    final String spaceGridBunching = spaceGridBunchingNames.iterator().next();
    final String maxMoneyness = maxMoneynessNames.iterator().next();
    final String pdeDirection = pdeDirectionNames.iterator().next();
    final FXOptionSecurity fxOption = (FXOptionSecurity) target.getSecurity();
    final UnorderedCurrencyPair currencies = UnorderedCurrencyPair.of(fxOption.getCallCurrency(), fxOption.getPutCurrency()); //TODO down to subclass
    final ValueRequirement pdeGridRequirement = getPDEGridRequirement(target, surfaceName, surfaceType, xAxis, yAxis, yAxisType, forwardCurveCalculationMethod, h, forwardCurveName,
        theta, timeSteps, spaceSteps, timeGridBunching, spaceGridBunching, maxMoneyness, pdeDirection);
    final ValueRequirement forwardCurveRequirement = getForwardCurveRequirement(forwardCurveCalculationMethod, forwardCurveName, currencies);
    return Sets.newHashSet(pdeGridRequirement, forwardCurveRequirement);
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
      if (constraints.getValues(SURFACE) != null) {
        final Set<String> surfaceNames = constraints.getValues(SURFACE);
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
          throw new OpenGammaRuntimeException("Missing or non-unique y-axis type property name");
        }
        yAxisType = yAxisTypeNames.iterator().next();
      }
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
    assert yAxisType != null;
    assert forwardCurveCalculationMethod != null;
    assert h != null;
    assert forwardCurveName != null;
    assert theta != null;
    assert timeSteps != null;
    assert spaceSteps != null;
    assert timeGridBunching != null;
    assert spaceGridBunching != null;
    assert maxMoneyness != null;
    assert pdeDirection != null;
    return Collections.singleton(getResultSpec(target, surfaceName, surfaceType, xAxis, yAxis, yAxisType, forwardCurveCalculationMethod, h,
        forwardCurveName, theta, timeSteps, spaceSteps, timeGridBunching, spaceGridBunching, maxMoneyness, pdeDirection));
  }

  private ValueRequirement getPDEGridRequirement(final ComputationTarget target, final String surfaceName, final String surfaceType, final String xAxis, final String yAxis,
      final String yAxisType, final String forwardCurveCalculationMethod, final String h, final String forwardCurveName, final String theta, final String timeSteps,
      final String spaceSteps, final String timeGridBunching, final String spaceGridBunching, final String maxMoneyness, final String pdeDirection) {
    final ValueProperties properties = getPDEGridProperties(surfaceName, surfaceType, xAxis, yAxis, yAxisType, forwardCurveCalculationMethod, h, forwardCurveName, theta,
        timeSteps, spaceSteps, timeGridBunching, spaceGridBunching, maxMoneyness, pdeDirection);
    return new ValueRequirement(ValueRequirementNames.LOCAL_VOLATILITY_FULL_PDE_GRID, target.toSpecification(), properties);
  }

  private ValueProperties getPDEGridProperties(final String surfaceName, final String surfaceType, final String xAxis, final String yAxis, final String yAxisType,
      final String forwardCurveCalculationMethod, final String h, final String forwardCurveName, final String theta, final String timeSteps, final String spaceSteps, final String timeGridBunching,
      final String spaceGridBunching, final String maxMoneyness, final String pdeDirection) {
    return ValueProperties.builder()
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.FOREX)
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(PROPERTY_SURFACE_TYPE, surfaceType)
        .with(PROPERTY_X_AXIS, xAxis)
        .with(PROPERTY_Y_AXIS, yAxis)
        .with(PROPERTY_Y_AXIS_TYPE, yAxisType)
        .with(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD, forwardCurveCalculationMethod)
        .with(CURVE, forwardCurveName)
        .with(PROPERTY_THETA, theta)
        .with(PROPERTY_TIME_STEPS, timeSteps)
        .with(PROPERTY_SPACE_STEPS, spaceSteps)
        .with(PROPERTY_TIME_GRID_BUNCHING, timeGridBunching)
        .with(PROPERTY_SPACE_GRID_BUNCHING, spaceGridBunching)
        .with(PROPERTY_MAX_MONEYNESS, maxMoneyness)
        .with(PROPERTY_H, h)
        .with(PROPERTY_PDE_DIRECTION, pdeDirection).get();
  }

  private ValueSpecification getResultSpec(final ComputationTarget target, final String surfaceName, final String surfaceType, final String xAxis, final String yAxis,
      final String yAxisType, final String forwardCurveCalculationMethod, final String h, final String forwardCurveName, final String theta, final String timeSteps,
      final String spaceSteps, final String timeGridBunching, final String spaceGridBunching, final String maxMoneyness, final String pdeDirection) {
    final ValueProperties properties = getResultProperties(surfaceName, surfaceType, xAxis, yAxis, yAxisType, forwardCurveCalculationMethod, h, forwardCurveName,
        theta, timeSteps, spaceSteps, timeGridBunching, spaceGridBunching, maxMoneyness, pdeDirection);
    return new ValueSpecification(ValueRequirementNames.PRESENT_VALUE, target.toSpecification(), properties);
  }

  private ValueProperties getResultProperties(final String surfaceName, final String surfaceType, final String xAxis, final String yAxis, final String yAxisType,
      final String forwardCurveCalculationMethod, final String h, final String forwardCurveName, final String theta, final String timeSteps, final String spaceSteps, final String timeGridBunching,
      final String spaceGridBunching, final String maxMoneyness, final String pdeDirection) {
    return createValueProperties()
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.FOREX)
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(ValuePropertyNames.CALCULATION_METHOD, LocalVolatilityPDEValuePropertyNames.LOCAL_VOLATILITY_METHOD)
        .with(PROPERTY_SURFACE_TYPE, surfaceType)
        .with(PROPERTY_X_AXIS, xAxis)
        .with(PROPERTY_Y_AXIS, yAxis)
        .with(PROPERTY_Y_AXIS_TYPE, yAxisType)
        .with(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD, forwardCurveCalculationMethod)
        .with(CURVE, forwardCurveName)
        .with(PROPERTY_THETA, theta)
        .with(PROPERTY_TIME_STEPS, timeSteps)
        .with(PROPERTY_SPACE_STEPS, spaceSteps)
        .with(PROPERTY_TIME_GRID_BUNCHING, timeGridBunching)
        .with(PROPERTY_SPACE_GRID_BUNCHING, spaceGridBunching)
        .with(PROPERTY_MAX_MONEYNESS, maxMoneyness)
        .with(PROPERTY_H, h)
        .with(PROPERTY_PDE_DIRECTION, pdeDirection)
        .withAny(PROPERTY_RESULT_STRIKE_INTERPOLATOR)
        .withAny(PROPERTY_RESULT_TIME_INTERPOLATOR)
        .get();
  }

  private ValueRequirement getForwardCurveRequirement(final String calculationMethod, final String forwardCurveName, final UnorderedCurrencyPair target) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD, calculationMethod)
        .with(CURVE, forwardCurveName).get();
    return new ValueRequirement(ValueRequirementNames.FORWARD_CURVE, ComputationTargetType.UNORDERED_CURRENCY_PAIR.specification(target), properties);
  }

  private double getExpiry(final FXOptionSecurity fxOption, final ZonedDateTime date) {
    final DayCount actAct = DayCounts.ACT_ACT_ISDA;
    return actAct.getDayCountFraction(date, fxOption.getExpiry().getExpiry());
  }

  private double getStrike(final FXOptionSecurity fxOption, final CurrencyPair currencyPair) {
    final Currency putCurrency = fxOption.getPutCurrency();
    if (currencyPair.getBase().equals(putCurrency)) {
      return fxOption.getCallAmount() / fxOption.getPutAmount();
    }
    return fxOption.getPutAmount() / fxOption.getCallAmount();
  }

}
