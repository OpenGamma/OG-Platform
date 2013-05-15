/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.model.volatility.curve.BlackForexTermStructureParameters;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceData;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.InterpolatedDataProperties;
import com.opengamma.financial.analytics.volatility.surface.BloombergFXOptionVolatilitySurfaceInstrumentProvider.FXVolQuoteType;
import com.opengamma.financial.analytics.volatility.surface.SurfaceAndCubeQuoteType;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceShiftFunction;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public class ForexFlatWithTermStructureVolatilitySurfaceFunction extends ForexVolatilitySurfaceFunction {

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
    final String surfaceName = desiredValue.getConstraint(ValuePropertyNames.SURFACE);
    final String interpolatorName = desiredValue.getConstraint(InterpolatedDataProperties.X_INTERPOLATOR_NAME);
    final String leftExtrapolatorName = desiredValue.getConstraint(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME);
    final String rightExtrapolatorName = desiredValue.getConstraint(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME);
    final Set<String> shifts = desiredValue.getConstraints().getValues(VolatilitySurfaceShiftFunction.SHIFT);
    final ValueRequirement surfaceRequirement = getDataRequirement(surfaceName, target);
    final Object volatilitySurfaceObject = inputs.getValue(surfaceRequirement);
    if (volatilitySurfaceObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + surfaceRequirement);
    }
    @SuppressWarnings("unchecked")
    final VolatilitySurfaceData<Tenor, Pair<Number, FXVolQuoteType>> fxVolatilitySurface = (VolatilitySurfaceData<Tenor, Pair<Number, FXVolQuoteType>>) volatilitySurfaceObject;
    if (fxVolatilitySurface.getYs().length != 1) {
      throw new OpenGammaRuntimeException("Have smile data present in a surface that should only have a term structure");
    }
    final Tenor[] tenors = fxVolatilitySurface.getXs();
    Arrays.sort(tenors);
    final double shiftMultiplier;
    if ((shifts != null) && (shifts.size() == 1)) {
      final String shift = shifts.iterator().next();
      shiftMultiplier = 1 + Double.parseDouble(shift);
    } else {
      shiftMultiplier = 1;
    }
    final DoubleArrayList timesList = new DoubleArrayList();
    final DoubleArrayList volsList = new DoubleArrayList();
    for (final Tenor tenor : tenors) {
      final double t = getTime(tenor);
      for (final Pair<Number, FXVolQuoteType> y : fxVolatilitySurface.getYs()) {
        Double volatility = fxVolatilitySurface.getVolatility(tenor, y);
        if (volatility != null) {
          volatility *= shiftMultiplier;
          if (y.getSecond().equals(FXVolQuoteType.ATM)) {
            volsList.add(volatility);
            timesList.add(t);
          }
        }
      }
    }
    if (volsList.size() == 0) {
      throw new OpenGammaRuntimeException("No volatility surface data for FX surface " + target.getUniqueId());
    }
    final ValueProperties.Builder resultProperties = createValueProperties()
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.FOREX)
        .with(InterpolatedDataProperties.X_INTERPOLATOR_NAME, interpolatorName)
        .with(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME, leftExtrapolatorName)
        .with(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME, rightExtrapolatorName);
    if (shifts != null) {
      resultProperties.with(VolatilitySurfaceShiftFunction.SHIFT, shifts);
    }
    final Interpolator1D interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(interpolatorName, leftExtrapolatorName, rightExtrapolatorName);
    final DoublesCurve volatility = InterpolatedDoublesCurve.fromSorted(timesList.toDoubleArray(), volsList.toDoubleArray(), interpolator);
    final BlackForexTermStructureParameters termStructure = new BlackForexTermStructureParameters(volatility);
    return Collections.singleton(new ComputedValue(new ValueSpecification(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA, target.toSpecification(),
        resultProperties.get()), termStructure));
  }

  @Override
  protected String getVolatilitySurfaceQuoteType() {
    return SurfaceAndCubeQuoteType.FLAT_WITH_TERM_STRUCTURE;
  }

}
