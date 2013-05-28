/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

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
import com.opengamma.financial.analytics.volatility.surface.BloombergFXOptionVolatilitySurfaceInstrumentProvider.FXVolQuoteType;
import com.opengamma.financial.analytics.volatility.surface.SurfaceAndCubeQuoteType;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceShiftFunction;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public class ForexStrangleRiskReversalVolatilitySurfaceFunction extends ForexVolatilitySurfaceFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(ForexStrangleRiskReversalVolatilitySurfaceFunction.class);

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String surfaceName = desiredValue.getConstraint(ValuePropertyNames.SURFACE);
    final String interpolatorName = desiredValue.getConstraint(InterpolatedDataProperties.X_INTERPOLATOR_NAME);
    final String leftExtrapolatorName = desiredValue.getConstraint(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME);
    final String rightExtrapolatorName = desiredValue.getConstraint(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME);
    final ValueRequirement surfaceRequirement = getDataRequirement(surfaceName, target);
    final Object volatilitySurfaceObject = inputs.getValue(getDataRequirement(surfaceName, target));
    if (volatilitySurfaceObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + surfaceRequirement);
    }
    @SuppressWarnings("unchecked")
    final VolatilitySurfaceData<Object, Object> fxVolatilitySurface = (VolatilitySurfaceData<Object, Object>) volatilitySurfaceObject;
    final Tenor[] tenors = getTenors(fxVolatilitySurface.getXs());
    Arrays.sort(tenors);
    final Pair<Number, FXVolQuoteType>[] quotes = getYs(fxVolatilitySurface.getYs());
    final Number[] deltaValues = getDeltaValues(quotes);
    final ObjectArrayList<SmileDeltaParameters> smile = new ObjectArrayList<>();
    final int nSmileValues = deltaValues.length - 1;
    final Set<String> shifts = desiredValue.getConstraints().getValues(VolatilitySurfaceShiftFunction.SHIFT);
    final double shiftMultiplier;
    if ((shifts != null) && (shifts.size() == 1)) {
      final String shift = shifts.iterator().next();
      shiftMultiplier = 1 + Double.parseDouble(shift);
    } else {
      // No shift requested
      shiftMultiplier = 1;
    }
    for (final Tenor tenor : tenors) {
      final double t = getTime(tenor);
      Double atm = fxVolatilitySurface.getVolatility(tenor, ObjectsPair.of(deltaValues[0], FXVolQuoteType.ATM));
      if (atm != null) {
        if (shiftMultiplier != 1) {
          atm = atm * shiftMultiplier;
        }
        final DoubleArrayList deltas = new DoubleArrayList();
        final DoubleArrayList riskReversals = new DoubleArrayList();
        final DoubleArrayList butterflies = new DoubleArrayList();
        for (int j = 0; j < nSmileValues; j++) {
          final Number delta = deltaValues[j + 1];
          if (delta != null) {
            Double rr = fxVolatilitySurface.getVolatility(tenor, ObjectsPair.of(delta, FXVolQuoteType.RISK_REVERSAL));
            Double butterfly = fxVolatilitySurface.getVolatility(tenor, ObjectsPair.of(delta, FXVolQuoteType.BUTTERFLY));
            if (rr != null && butterfly != null) {
              rr = rr * shiftMultiplier;
              butterfly = butterfly * shiftMultiplier;
              deltas.add(delta.doubleValue() / 100.);
              riskReversals.add(rr);
              butterflies.add(butterfly);
            }
          } else {
            s_logger.info("Had a null delta value for tenor {}", j);
          }
        }
        smile.add(new SmileDeltaParameters(t, atm, deltas.toDoubleArray(), riskReversals.toDoubleArray(), butterflies.toDoubleArray()));
      } else {
        s_logger.info("Could not get atm data for tenor {}", tenor);
      }
    }
    if (smile.size() == 0) {
      throw new OpenGammaRuntimeException("Could not get any data for surface " + surfaceName + " with target " + target);
    }
    final Interpolator1D interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(interpolatorName, leftExtrapolatorName, rightExtrapolatorName);
    final SmileDeltaTermStructureParametersStrikeInterpolation smiles = new SmileDeltaTermStructureParametersStrikeInterpolation(smile.toArray(new SmileDeltaParameters[smile.size()]), interpolator);
    final ValueProperties.Builder resultProperties = createValueProperties()
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.FOREX)
        .with(InterpolatedDataProperties.X_INTERPOLATOR_NAME, interpolatorName)
        .with(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME, leftExtrapolatorName)
        .with(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME, rightExtrapolatorName);
    if (shifts != null) {
      resultProperties.with(VolatilitySurfaceShiftFunction.SHIFT, shifts);
    }
    return Collections.<ComputedValue>singleton(new ComputedValue(new ValueSpecification(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA, target
        .toSpecification(), resultProperties.get()), smiles));
  }

  @Override
  protected String getVolatilitySurfaceQuoteType() {
    return SurfaceAndCubeQuoteType.MARKET_STRANGLE_RISK_REVERSAL;
  }

  private Number[] getDeltaValues(final Pair<Number, FXVolQuoteType>[] quotes) {
    final TreeSet<Number> values = new TreeSet<>();
    for (final Pair<Number, FXVolQuoteType> pair : quotes) {
      values.add(pair.getFirst());
    }
    return values.toArray((Number[]) Array.newInstance(Number.class, values.size()));
  }

  //TODO why are these next two methods suddenly needed?
  private Tenor[] getTenors(final Object[] tenors) {
    final Tenor[] converted = new Tenor[tenors.length];
    for (int i = 0; i < tenors.length; i++) {
      converted[i] = (Tenor) tenors[i];
    }
    return converted;
  }

  @SuppressWarnings("unchecked")
  private Pair<Number, FXVolQuoteType>[] getYs(final Object[] ys) {
    final Pair<Number, FXVolQuoteType>[] converted = new Pair[ys.length];
    for (int i = 0; i < ys.length; i++) {
      converted[i] = (Pair<Number, FXVolQuoteType>) ys[i];
    }
    return converted;
  }
}
