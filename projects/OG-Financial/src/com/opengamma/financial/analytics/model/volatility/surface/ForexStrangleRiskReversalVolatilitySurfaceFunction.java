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

import javax.time.calendar.Period;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaParameter;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureParameter;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceData;
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
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.InterpolatedDataProperties;
import com.opengamma.financial.analytics.volatility.surface.BloombergFXOptionVolatilitySurfaceInstrumentProvider.FXVolQuoteType;
import com.opengamma.financial.analytics.volatility.surface.DefaultVolatilitySurfaceShiftFunction;
import com.opengamma.financial.analytics.volatility.surface.SurfaceAndCubePropertyNames;
import com.opengamma.financial.analytics.volatility.surface.SurfaceAndCubeQuoteType;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceShiftFunction;
import com.opengamma.util.money.UnorderedCurrencyPair;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class ForexStrangleRiskReversalVolatilitySurfaceFunction extends AbstractFunction.NonCompiledInvoker {
  /**
   * 
   */
  private static final SmileDeltaParameter[] EMPTY_ARRAY = new SmileDeltaParameter[0];
  private static final Logger s_logger = LoggerFactory.getLogger(ForexStrangleRiskReversalVolatilitySurfaceFunction.class);

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
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
    final VolatilitySurfaceData<Tenor, Pair<Number, FXVolQuoteType>> fxVolatilitySurface = (VolatilitySurfaceData<Tenor, Pair<Number, FXVolQuoteType>>) volatilitySurfaceObject;
    final Tenor[] tenors = fxVolatilitySurface.getXs();
    Arrays.sort(tenors);
    final Pair<Number, FXVolQuoteType>[] quotes = fxVolatilitySurface.getYs();
    final Number[] deltaValues = getDeltaValues(quotes);
    final ObjectArrayList<SmileDeltaParameter> smile = new ObjectArrayList<SmileDeltaParameter>();
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
        smile.add(new SmileDeltaParameter(t, atm, deltas.toDoubleArray(), riskReversals.toDoubleArray(), butterflies.toDoubleArray()));
      } else {
        s_logger.info("Could not get atm data for tenor {}", tenor);
      }
    }
    if (smile.size() == 0) {
      throw new OpenGammaRuntimeException("Could not get any data for surface " + surfaceName);
    }
    final Interpolator1D interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(interpolatorName, leftExtrapolatorName, rightExtrapolatorName);
    final SmileDeltaTermStructureParameter smiles = new SmileDeltaTermStructureParameter(smile.toArray(EMPTY_ARRAY), interpolator);
    final ValueProperties.Builder resultProperties = createValueProperties()
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.FOREX)
        .with(InterpolatedDataProperties.X_INTERPOLATOR_NAME, interpolatorName)
        .with(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME, leftExtrapolatorName)
        .with(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME, rightExtrapolatorName);
    if (shifts != null) {
      resultProperties.with(VolatilitySurfaceShiftFunction.SHIFT, shifts);
    }
    return Collections.<ComputedValue>singleton(new ComputedValue(new ValueSpecification(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA, target.toSpecification(),
        resultProperties.get()), smiles));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PRIMITIVE;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.PRIMITIVE) {
      return false;
    }
    return UnorderedCurrencyPair.OBJECT_SCHEME.equals(target.getUniqueId().getScheme());
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> surfaceNames = constraints.getValues(ValuePropertyNames.SURFACE);
    if (surfaceNames == null || surfaceNames.size() != 1) {
      throw new OpenGammaRuntimeException("Need one surface name; have " + surfaceNames);
    }
    final Set<String> interpolatorNames = constraints.getValues(InterpolatedDataProperties.X_INTERPOLATOR_NAME);
    if (interpolatorNames == null || interpolatorNames.size() != 1) {
      return null;
    }
    final Set<String> leftExtrapolatorNames = constraints.getValues(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME);
    if (leftExtrapolatorNames == null || leftExtrapolatorNames.size() != 1) {
      return null;
    }
    final Set<String> rightExtrapolatorNames = constraints.getValues(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME);
    if (rightExtrapolatorNames == null || rightExtrapolatorNames.size() != 1) {
      return null;
    }
    final String surfaceName = surfaceNames.iterator().next();
    return Collections.<ValueRequirement>singleton(getDataRequirement(surfaceName, target));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties.Builder resultProperties = createValueProperties()
        .withAny(ValuePropertyNames.SURFACE)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.FOREX)
        .withAny(InterpolatedDataProperties.X_INTERPOLATOR_NAME)
        .withAny(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME)
        .withAny(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME);
    if (context.getViewCalculationConfiguration() != null) {
      final Set<String> shifts = context.getViewCalculationConfiguration().getDefaultProperties().getValues(DefaultVolatilitySurfaceShiftFunction.VOLATILITY_SURFACE_SHIFT);
      if ((shifts != null) && (shifts.size() == 1)) {
        resultProperties.with(VolatilitySurfaceShiftFunction.SHIFT, shifts.iterator().next());
      }
    }
    return Collections.<ValueSpecification>singleton(new ValueSpecification(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA, target.toSpecification(), resultProperties.get()));
  }

  private double getTime(final Tenor tenor) {
    final Period period = tenor.getPeriod();
    if (period.getYears() != 0) {
      return period.getYears();
    }
    if (period.getMonths() != 0) {
      return ((double) period.getMonths()) / 12;
    }
    if (period.getDays() != 0) {
      return ((double) period.getDays()) / 365;
    }
    throw new OpenGammaRuntimeException("Should never happen");
  }

  private Number[] getDeltaValues(final Pair<Number, FXVolQuoteType>[] quotes) {
    final TreeSet<Number> values = new TreeSet<Number>();
    for (final Pair<Number, FXVolQuoteType> pair : quotes) {
      values.add(pair.getFirst());
    }
    return values.toArray((Number[]) Array.newInstance(Number.class, values.size()));
  }

  private ValueRequirement getDataRequirement(final String surfaceName, final ComputationTarget target) {
    return new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE_DATA, target.toSpecification(),
        ValueProperties.builder()
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.FOREX)
        .with(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_QUOTE_TYPE, SurfaceAndCubeQuoteType.MARKET_STRANGLE_RISK_REVERSAL)
        .with(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_UNITS, SurfaceAndCubePropertyNames.VOLATILITY_QUOTE).get());
  }
}
