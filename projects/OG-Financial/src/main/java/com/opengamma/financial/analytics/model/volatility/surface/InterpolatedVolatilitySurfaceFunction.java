/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceData;
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
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.InterpolatedDataProperties;
import com.opengamma.util.CompareUtils;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class InterpolatedVolatilitySurfaceFunction extends AbstractFunction.NonCompiledInvoker {
  private static final Logger s_logger = LoggerFactory.getLogger(InterpolatedVolatilitySurfaceFunction.class);

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String surfaceName = desiredValue.getConstraint(ValuePropertyNames.SURFACE);
    final String instrumentType = desiredValue.getConstraint(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE);
    final String leftXExtrapolatorName = desiredValue.getConstraint(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME);
    final String rightXExtrapolatorName = desiredValue.getConstraint(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME);
    final String xInterpolatorName = desiredValue.getConstraint(InterpolatedDataProperties.X_INTERPOLATOR_NAME);
    final String leftYExtrapolatorName = desiredValue.getConstraint(InterpolatedDataProperties.LEFT_Y_EXTRAPOLATOR_NAME);
    final String rightYExtrapolatorName = desiredValue.getConstraint(InterpolatedDataProperties.RIGHT_Y_EXTRAPOLATOR_NAME);
    final String yInterpolatorName = desiredValue.getConstraint(InterpolatedDataProperties.Y_INTERPOLATOR_NAME);
    final ValueRequirement volatilityDataRequirement = getVolatilityDataRequirement(target, surfaceName, instrumentType);
    final Object volatilityDataObject = inputs.getValue(volatilityDataRequirement);
    if (volatilityDataObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + volatilityDataRequirement);
    }
    @SuppressWarnings("unchecked")
    final VolatilitySurfaceData<Double, Double> volatilityData = (VolatilitySurfaceData<Double, Double>) volatilityDataObject;
    if (volatilityData.size() == 0) {
      throw new OpenGammaRuntimeException("Volatility surface data for requirement " + volatilityDataRequirement + " was empty");
    }
    final DoubleArrayList x = new DoubleArrayList();
    final DoubleArrayList y = new DoubleArrayList();
    final DoubleArrayList sigma = new DoubleArrayList();
    final Double[] xData = volatilityData.getXs();
    final Double[] yData = volatilityData.getYs();
    final int n = xData.length;
    for (int i = 0; i < n; i++) {
      final Double vol = volatilityData.getVolatility(xData[i], yData[i]);
      if (vol != null && !CompareUtils.closeEquals(vol, 0)) {
        x.add(xData[i]);
        y.add(yData[i]);
        sigma.add(vol);
      }
    }
    if (x.isEmpty()) {
      throw new OpenGammaRuntimeException("Could not get any data for " + volatilityDataRequirement);
    }
    final Interpolator1D xInterpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(xInterpolatorName, leftXExtrapolatorName, rightXExtrapolatorName);
    final Interpolator1D yInterpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(yInterpolatorName, leftYExtrapolatorName, rightYExtrapolatorName);
    final GridInterpolator2D interpolator = new GridInterpolator2D(xInterpolator, yInterpolator);
    final Surface<Double, Double, Double> surface = InterpolatedDoublesSurface.from(x.toDoubleArray(), y.toDoubleArray(), sigma.toDoubleArray(), interpolator);
    final VolatilitySurface volatilitySurface = new VolatilitySurface(surface);
    final ValueProperties properties = getResultProperties(surfaceName, instrumentType, leftXExtrapolatorName, rightXExtrapolatorName, xInterpolatorName, leftYExtrapolatorName,
        rightYExtrapolatorName, yInterpolatorName);
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.INTERPOLATED_VOLATILITY_SURFACE, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, volatilitySurface));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PRIMITIVE.or(ComputationTargetType.CURRENCY);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getValue() instanceof Currency) {
      return true;
    } else {
      final String scheme = target.getUniqueId().getScheme();
      return scheme.equalsIgnoreCase(ExternalSchemes.BLOOMBERG_TICKER.getName())
          || scheme.equalsIgnoreCase(ExternalSchemes.BLOOMBERG_TICKER_WEAK.getName())
          || scheme.equalsIgnoreCase(ExternalSchemes.ACTIVFEED_TICKER.getName());
    }
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = getResultProperties();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.INTERPOLATED_VOLATILITY_SURFACE, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> surfaceNames = constraints.getValues(ValuePropertyNames.SURFACE);
    if (surfaceNames == null || surfaceNames.size() != 1) {
      s_logger.error("Could not get surface name from constraints {}", constraints);
      return null;
    }
    final Set<String> instrumentTypeNames = constraints.getValues(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE);
    if (instrumentTypeNames == null || instrumentTypeNames.size() != 1) {
      s_logger.error("Could not get instrument type from constraints {}", constraints);
      return null;
    }
    final Set<String> leftXExtrapolatorNames = constraints.getValues(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME);
    if (leftXExtrapolatorNames == null || leftXExtrapolatorNames.size() != 1) {
      return null;
    }
    final Set<String> rightXExtrapolatorNames = constraints.getValues(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME);
    if (rightXExtrapolatorNames == null || rightXExtrapolatorNames.size() != 1) {
      return null;
    }
    final Set<String> xInterpolatorNames = constraints.getValues(InterpolatedDataProperties.X_INTERPOLATOR_NAME);
    if (xInterpolatorNames == null || xInterpolatorNames.size() != 1) {
      return null;
    }
    final Set<String> leftYExtrapolatorNames = constraints.getValues(InterpolatedDataProperties.LEFT_Y_EXTRAPOLATOR_NAME);
    if (leftYExtrapolatorNames == null || leftYExtrapolatorNames.size() != 1) {
      return null;
    }
    final Set<String> rightYExtrapolatorNames = constraints.getValues(InterpolatedDataProperties.RIGHT_Y_EXTRAPOLATOR_NAME);
    if (rightYExtrapolatorNames == null || rightYExtrapolatorNames.size() != 1) {
      return null;
    }
    final Set<String> yInterpolatorNames = constraints.getValues(InterpolatedDataProperties.Y_INTERPOLATOR_NAME);
    if (yInterpolatorNames == null || yInterpolatorNames.size() != 1) {
      return null;
    }
    final String surfaceName = surfaceNames.iterator().next();
    final String instrumentType = instrumentTypeNames.iterator().next();
    return Collections.singleton(getVolatilityDataRequirement(target, surfaceName, instrumentType));
  }

  private ValueRequirement getVolatilityDataRequirement(final ComputationTarget target, final String surfaceName, final String instrumentType) {
    final ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.SURFACE, surfaceName).with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, instrumentType).get();
    return new ValueRequirement(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA, target.toSpecification(), properties);
  }

  private ValueProperties getResultProperties() {
    final ValueProperties properties = createValueProperties().withAny(ValuePropertyNames.SURFACE).withAny(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE)
        .withAny(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME).withAny(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME)
        .withAny(InterpolatedDataProperties.X_INTERPOLATOR_NAME).withAny(InterpolatedDataProperties.LEFT_Y_EXTRAPOLATOR_NAME)
        .withAny(InterpolatedDataProperties.RIGHT_Y_EXTRAPOLATOR_NAME).withAny(InterpolatedDataProperties.Y_INTERPOLATOR_NAME)
        .with(ValuePropertyNames.CALCULATION_METHOD, InterpolatedDataProperties.CALCULATION_METHOD_NAME).get();
    return properties;
  }

  private ValueProperties getResultProperties(final String surfaceName, final String instrumentType, final String leftXExtrapolatorName, final String rightXExtrapolatorName,
      final String xInterpolatorName, final String leftYExtrapolatorName, final String rightYExtrapolatorName, final String yInterpolatorName) {
    final ValueProperties properties = createValueProperties().with(ValuePropertyNames.SURFACE, surfaceName).with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, instrumentType)
        .with(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME, leftXExtrapolatorName).with(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME, rightXExtrapolatorName)
        .with(InterpolatedDataProperties.X_INTERPOLATOR_NAME, xInterpolatorName).with(InterpolatedDataProperties.LEFT_Y_EXTRAPOLATOR_NAME, leftYExtrapolatorName)
        .with(InterpolatedDataProperties.RIGHT_Y_EXTRAPOLATOR_NAME, rightYExtrapolatorName).with(InterpolatedDataProperties.Y_INTERPOLATOR_NAME, yInterpolatorName)
        .with(ValuePropertyNames.CALCULATION_METHOD, InterpolatedDataProperties.CALCULATION_METHOD_NAME).get();
    return properties;
  }
}
