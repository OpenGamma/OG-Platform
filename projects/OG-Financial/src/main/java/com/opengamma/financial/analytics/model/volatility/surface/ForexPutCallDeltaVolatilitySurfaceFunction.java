/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaParameters;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParametersStrikeInterpolation;
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
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceShiftFunction;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
public abstract class ForexPutCallDeltaVolatilitySurfaceFunction extends ForexVolatilitySurfaceFunction {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(ForexPutCallDeltaVolatilitySurfaceFunction.class);

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String surfaceName = desiredValue.getConstraint(ValuePropertyNames.SURFACE);
    final String interpolatorName = desiredValue.getConstraint(InterpolatedDataProperties.X_INTERPOLATOR_NAME);
    final String leftExtrapolatorName = desiredValue.getConstraint(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME);
    final String rightExtrapolatorName = desiredValue.getConstraint(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME);
    final ValueRequirement surfaceRequirement = getDataRequirement(surfaceName, target);
    final Object volatilitySurfaceObject = inputs.getValue(surfaceRequirement);
    if (volatilitySurfaceObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + surfaceRequirement);
    }
    // In some circumstances, we will get Object arrays for xs and ys, so need to cope with that.
    @SuppressWarnings("unchecked")
    final VolatilitySurfaceData<Object, Object> fxVolatilitySurface = (VolatilitySurfaceData<Object, Object>) volatilitySurfaceObject;
    final Object[] tenorsObjs = fxVolatilitySurface.getXs();
    final Tenor[] tenors = new Tenor[tenorsObjs.length];
    System.arraycopy(tenorsObjs, 0, tenors, 0, tenors.length);
    final Object[] deltaValueObjs = fxVolatilitySurface.getYs();
    final Double[] deltaValues = new Double[deltaValueObjs.length];
    System.arraycopy(deltaValueObjs, 0, deltaValues, 0, deltaValueObjs.length);
    Arrays.sort(tenors);
    Arrays.sort(deltaValues);
    final int nPoints = tenors.length;
    final SmileDeltaParameters[] smile = new SmileDeltaParameters[nPoints];
    final int nSmileValues = deltaValues.length;
    final Set<String> shifts = desiredValues.iterator().next().getConstraints().getValues(VolatilitySurfaceShiftFunction.SHIFT);
    final double shiftMultiplier;
    if ((shifts != null) && (shifts.size() == 1)) {
      final String shift = shifts.iterator().next();
      shiftMultiplier = 1 + Double.parseDouble(shift);
    } else {
      shiftMultiplier = 1;
    }
    for (int i = 0; i < tenors.length; i++) {
      final Tenor tenor = tenors[i];
      final double t = getTime(tenor);
      final DoubleArrayList deltas = new DoubleArrayList();
      final DoubleArrayList volatilities = new DoubleArrayList();
      for (int j = 0; j < nSmileValues; j++) {
        final Double delta = deltaValues[j];
        if (delta != null) {
          Double volatility = fxVolatilitySurface.getVolatility((Object) tenor, (Object) delta);
          if (volatility != null) {
            volatility *= shiftMultiplier;
            if (delta < 50) {
              deltas.add(getTransformedDelta(delta));
            }
            volatilities.add(volatility);
          }
        } else {
          s_logger.info("Had a null value for tenor number " + j);
        }
      }
      smile[i] = new SmileDeltaParameters(t, deltas.toDoubleArray(), volatilities.toDoubleArray());
    }
    final Interpolator1D interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(interpolatorName, leftExtrapolatorName, rightExtrapolatorName);
    final SmileDeltaTermStructureParametersStrikeInterpolation smiles = new SmileDeltaTermStructureParametersStrikeInterpolation(smile, interpolator);
    final ValueProperties.Builder resultProperties = createValueProperties()
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.FOREX)
        .with(InterpolatedDataProperties.X_INTERPOLATOR_NAME, interpolatorName)
        .with(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME, leftExtrapolatorName)
        .with(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME, rightExtrapolatorName);
    if (shifts != null) {
      resultProperties.with(VolatilitySurfaceShiftFunction.SHIFT, shifts);
    }
    return Collections.singleton(new ComputedValue(new ValueSpecification(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA, target.toSpecification(),
        resultProperties.get()), smiles));
  }

  /**
   * Transforms the delta for this surface type into that expected by the analytics library.
   * @param delta The delta
   * @return The transformed delta.
   */
  protected abstract double getTransformedDelta(double delta);
}
