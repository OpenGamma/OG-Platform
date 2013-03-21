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
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.local.DupireLocalVolatilityCalculator;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilityForwardPDEGreekCalculator1;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilitySurface;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.GeneralSmileInterpolator;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SmileInterpolatorSpline;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.SmileSurfaceDataBundle;
import com.opengamma.analytics.financial.model.volatility.surface.Moneyness;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurfaceInterpolator;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;

/**
 * @deprecated Deprecated
 */
@Deprecated
public abstract class LocalVolatilityPDEGridFunction extends AbstractFunction.NonCompiledInvoker {
  private final String _instrumentType;

  public LocalVolatilityPDEGridFunction(final String instrumentType) {
    _instrumentType = instrumentType;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String surfaceName = desiredValue.getConstraint(SURFACE);
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
    final String forwardCurveCalculationMethod = desiredValue.getConstraint(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD);
    final String forwardCurveName = desiredValue.getConstraint(CURVE);
    final String hName = desiredValue.getConstraint(PROPERTY_H);
    final double h = Double.parseDouble(hName);
    final String thetaName = desiredValue.getConstraint(PROPERTY_THETA);
    final double theta = Double.parseDouble(thetaName);
    final String timeStepsName = desiredValue.getConstraint(PROPERTY_TIME_STEPS);
    final int timeSteps = Integer.parseInt(timeStepsName);
    final String spaceStepsName = desiredValue.getConstraint(PROPERTY_SPACE_STEPS);
    final int spaceSteps = Integer.parseInt(spaceStepsName);
    final String timeGridBunchingName = desiredValue.getConstraint(PROPERTY_TIME_GRID_BUNCHING);
    final double timeGridBunching = Double.parseDouble(timeGridBunchingName);
    final String spaceGridBunchingName = desiredValue.getConstraint(PROPERTY_SPACE_GRID_BUNCHING);
    final double spaceGridBunching = Double.parseDouble(spaceGridBunchingName);
    final String pdeDirection = desiredValue.getConstraint(PROPERTY_PDE_DIRECTION);
    if (!(pdeDirection.equals(LocalVolatilityPDEValuePropertyNames.FORWARD_PDE))) {
      throw new OpenGammaRuntimeException("Can only use forward PDE; should never ask for this direction: " + pdeDirection);
    }
    final DupireLocalVolatilityCalculator localVolatilityCalculator = new DupireLocalVolatilityCalculator(h);
    //TODO R White testing using spline rather than SABR - this should be an option
    final GeneralSmileInterpolator smileInterpolator = new SmileInterpolatorSpline();
    final VolatilitySurfaceInterpolator surfaceFitter = new VolatilitySurfaceInterpolator(smileInterpolator, useLogTime, useIntegratedVariance, useLogValue);
    //final PiecewiseSABRSurfaceFitter1<?> surfaceFitter = new MoneynessPiecewiseSABRSurfaceFitter(useLogTime, useIntegratedVariance, useLogValue);
    //TODO get rid of hardcoded maxProxydelta = 1.5
    final LocalVolatilityForwardPDEGreekCalculator1<?> calculator = new LocalVolatilityForwardPDEGreekCalculator1<Moneyness>(theta, timeSteps, spaceSteps, timeGridBunching, spaceGridBunching,
        /*(MoneynessPiecewiseSABRSurfaceFitter)*/surfaceFitter, localVolatilityCalculator, 1.5);
    final ValueSpecification spec = new ValueSpecification(desiredValue.getValueName(), target.toSpecification(), desiredValue.getConstraints());
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final ComputationTargetReference id = getTargetForUnderlyings(target);
    final ValueRequirement surfaceRequirement = getVolatilitySurfaceRequirement(surfaceName, surfaceType, xAxis, yAxis, yAxisType, hName, forwardCurveCalculationMethod, forwardCurveName, id);
    final Object localVolatilitySurfaceObject = inputs.getValue(surfaceRequirement);
    if (localVolatilitySurfaceObject == null) {
      throw new OpenGammaRuntimeException("Local volatility surface was null");
    }
    final LocalVolatilitySurface<?> localVolatilitySurface = (LocalVolatilitySurface<?>) localVolatilitySurfaceObject;
    final ValueRequirement forwardCurveRequirement = getForwardCurveRequirement(forwardCurveCalculationMethod, forwardCurveName, id);
    final Object forwardCurveObject = inputs.getValue(forwardCurveRequirement);
    if (forwardCurveObject == null) {
      throw new OpenGammaRuntimeException("Forward curve was null");
    }
    final ForwardCurve forwardCurve = (ForwardCurve) forwardCurveObject;
    final ValueRequirement volDataRequirement = getUnderlyingVolatilityDataRequirement(surfaceName, id);
    final SmileSurfaceDataBundle data = getData(inputs, volDataRequirement, forwardCurveRequirement);
    final FXOptionSecurity fxOption = (FXOptionSecurity) security;
    final CurrencyPairs currencyPairs = OpenGammaExecutionContext.getCurrencyPairsSource(executionContext).getCurrencyPairs(CurrencyPairs.DEFAULT_CURRENCY_PAIRS);
    final CurrencyPair currencyPair = currencyPairs.getCurrencyPair(fxOption.getPutCurrency(), fxOption.getCallCurrency());
    final EuropeanVanillaOption option = getOption(security, now, currencyPair);
    return Collections.singleton(new ComputedValue(spec, getResult(calculator, localVolatilitySurface, forwardCurve, data, option)));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties()
        .with(ValuePropertyNames.CALCULATION_METHOD, LocalVolatilityPDEValuePropertyNames.LOCAL_VOLATILITY_METHOD)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, _instrumentType)
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
        .withAny(PROPERTY_PDE_DIRECTION).get();
    return Collections.singleton(new ValueSpecification(getResultName(), target.toSpecification(), properties));
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
    final String surfaceType = surfaceTypeNames.iterator().next();
    final String xAxis = xAxisNames.iterator().next();
    final String yAxis = yAxisNames.iterator().next();
    final String yAxisType = yAxisTypeNames.iterator().next();
    final String forwardCurveCalculationMethod = forwardCurveCalculationMethodNames.iterator().next();
    final String h = hNames.iterator().next();
    final String forwardCurveName = forwardCurveNames.iterator().next();
    final String surfaceName = surfaceNames.iterator().next();
    final ComputationTargetReference id = getTargetForUnderlyings(target);
    final ValueRequirement volDataRequirement = getUnderlyingVolatilityDataRequirement(surfaceName, id);
    return Sets.newHashSet(getVolatilitySurfaceRequirement(surfaceName, surfaceType, xAxis, yAxis, yAxisType, h,
        forwardCurveCalculationMethod, forwardCurveName, id),
        getForwardCurveRequirement(forwardCurveCalculationMethod, forwardCurveName, id),
        volDataRequirement);
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
      } else if (input.getValue().getValueName().equals(ValueRequirementNames.LOCAL_VOLATILITY_SURFACE)) {
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
        if (constraints.getValues(PROPERTY_H) != null) {
          final Set<String> hNames = constraints.getValues(PROPERTY_H);
          if (hNames == null || hNames.size() != 1) {
            throw new OpenGammaRuntimeException("Missing or non-unique h name");
          }
          h = hNames.iterator().next();
        }
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
    return Collections.singleton(getResultSpec(target, surfaceName, surfaceType, xAxis, yAxis, yAxisType, forwardCurveCalculationMethod, h, forwardCurveName));
  }

  protected abstract ComputationTargetReference getTargetForUnderlyings(final ComputationTarget target);

  protected abstract EuropeanVanillaOption getOption(final FinancialSecurity security, final ZonedDateTime date, final CurrencyPair currencyPair);

  //TODO shouldn't need to do this - write a fudge builder for the data bundle and have it as an input
  protected abstract SmileSurfaceDataBundle getData(final FunctionInputs inputs, final ValueRequirement volDataRequirement, final ValueRequirement forwardCurveRequirement);

  protected abstract Object getResult(final LocalVolatilityForwardPDEGreekCalculator1<?> calculator, final LocalVolatilitySurface<?> localVolatilitySurface,
      final ForwardCurve forwardCurve, final SmileSurfaceDataBundle data, final EuropeanVanillaOption option);

  protected abstract String getResultName();

  protected abstract ValueRequirement getUnderlyingVolatilityDataRequirement(final String surfaceName, final ComputationTargetReference id);

  private ValueRequirement getVolatilitySurfaceRequirement(final String surfaceName, final String surfaceType, final String xAxis, final String yAxis, final String yAxisType,
      final String h, final String forwardCurveCalculationMethod, final String forwardCurveName, final ComputationTargetReference target) {
    final ValueProperties properties = ValueProperties.builder()
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, _instrumentType)
        .with(SURFACE, surfaceName)
        .with(PROPERTY_SURFACE_TYPE, surfaceType)
        .with(PROPERTY_X_AXIS, xAxis)
        .with(PROPERTY_Y_AXIS, yAxis)
        .with(PROPERTY_Y_AXIS_TYPE, yAxisType)
        .with(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD, forwardCurveCalculationMethod)
        .with(CURVE, forwardCurveName)
        .with(PROPERTY_H, h).get();
    return new ValueRequirement(ValueRequirementNames.LOCAL_VOLATILITY_SURFACE, target, properties);
  }

  private ValueRequirement getForwardCurveRequirement(final String calculationMethod, final String forwardCurveName, final ComputationTargetReference target) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD, calculationMethod)
        .with(CURVE, forwardCurveName).get();
    return new ValueRequirement(ValueRequirementNames.FORWARD_CURVE, target, properties);
  }

  private ValueProperties getResultProperties(final String surfaceName, final String surfaceType, final String xAxis, final String yAxis, final String yAxisType,
      final String forwardCurveCalculationMethod, final String h, final String forwardCurveName) {
    return createValueProperties()
        .with(ValuePropertyNames.CALCULATION_METHOD, LocalVolatilityPDEValuePropertyNames.LOCAL_VOLATILITY_METHOD)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, _instrumentType)
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(PROPERTY_SURFACE_TYPE, surfaceType)
        .with(PROPERTY_X_AXIS, xAxis)
        .with(PROPERTY_Y_AXIS, yAxis)
        .with(PROPERTY_Y_AXIS_TYPE, yAxisType)
        .with(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD, forwardCurveCalculationMethod)
        .with(CURVE, forwardCurveName)
        .withAny(PROPERTY_THETA)
        .withAny(PROPERTY_TIME_STEPS)
        .withAny(PROPERTY_SPACE_STEPS)
        .withAny(PROPERTY_TIME_GRID_BUNCHING)
        .withAny(PROPERTY_SPACE_GRID_BUNCHING)
        .withAny(PROPERTY_MAX_MONEYNESS)
        .with(PROPERTY_H, h)
        .withAny(PROPERTY_PDE_DIRECTION).get();
  }

  private ValueSpecification getResultSpec(final ComputationTarget target, final String definitionName, final String surfaceType, final String xAxis, final String yAxis,
      final String yAxisType, final String forwardCurveCalculationMethod, final String h, final String forwardCurveName) {
    final ValueProperties properties = getResultProperties(definitionName, surfaceType, xAxis, yAxis, yAxisType, forwardCurveCalculationMethod, h, forwardCurveName);
    return new ValueSpecification(getResultName(), target.toSpecification(), properties);
  }

}
