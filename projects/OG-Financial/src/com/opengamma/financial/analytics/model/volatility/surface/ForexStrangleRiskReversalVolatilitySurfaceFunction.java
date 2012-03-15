/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import javax.time.calendar.Period;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
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
import com.opengamma.financial.analytics.volatility.surface.BloombergFXOptionVolatilitySurfaceInstrumentProvider.FXVolQuoteType;
import com.opengamma.financial.analytics.volatility.surface.DefaultVolatilitySurfaceShiftFunction;
import com.opengamma.financial.analytics.volatility.surface.SurfacePropertyNames;
import com.opengamma.financial.analytics.volatility.surface.SurfaceQuoteType;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceShiftFunction;
import com.opengamma.financial.model.option.definition.SmileDeltaParameter;
import com.opengamma.financial.model.option.definition.SmileDeltaTermStructureParameter;
import com.opengamma.util.money.UnorderedCurrencyPair;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class ForexStrangleRiskReversalVolatilitySurfaceFunction extends AbstractFunction.NonCompiledInvoker {
  private static final Logger s_logger = LoggerFactory.getLogger(ForexStrangleRiskReversalVolatilitySurfaceFunction.class);

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final String surfaceName = desiredValues.iterator().next().getConstraint(ValuePropertyNames.SURFACE);
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
    final int nPoints = tenors.length;
    final SmileDeltaParameter[] smile = new SmileDeltaParameter[nPoints];
    final int nSmileValues = deltaValues.length - 1;
    final Set<String> shifts = desiredValues.iterator().next().getConstraints().getValues(VolatilitySurfaceShiftFunction.SHIFT);
    final double shiftMultiplier;
    if ((shifts != null) && (shifts.size() == 1)) {
      final String shift = shifts.iterator().next();
      shiftMultiplier = 1 + Double.parseDouble(shift);
    } else {
      // No shift requested
      shiftMultiplier = 1;
    }
    for (int i = 0; i < tenors.length; i++) {
      final Tenor tenor = tenors[i];
      final double t = getTime(tenor);
      Double atm = fxVolatilitySurface.getVolatility(tenor, ObjectsPair.of(deltaValues[0], FXVolQuoteType.ATM));
      if (atm == null) {
        throw new OpenGammaRuntimeException("Could not get ATM volatility data for surface");
      }
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
          s_logger.info("Had a null value for tenor number " + j);
        }
      }
      smile[i] = new SmileDeltaParameter(t, atm, deltas.toDoubleArray(), riskReversals.toDoubleArray(), butterflies.toDoubleArray());
    }
    final SmileDeltaTermStructureParameter smiles = new SmileDeltaTermStructureParameter(smile);
    final ValueProperties.Builder resultProperties = createValueProperties()
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.FOREX);
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
    if (UnorderedCurrencyPair.OBJECT_SCHEME.equals(target.getUniqueId().getScheme())) {
      return true;
    }
    return false;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<String> surfaceNames = desiredValue.getConstraints().getValues(ValuePropertyNames.SURFACE);
    if (surfaceNames == null || surfaceNames.size() != 1) {
      throw new OpenGammaRuntimeException("Need one surface name; have " + surfaceNames);
    }
    final String surfaceName = surfaceNames.iterator().next();
    return Collections.<ValueRequirement>singleton(getDataRequirement(surfaceName, target));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties.Builder resultProperties = createValueProperties()
        .withAny(ValuePropertyNames.SURFACE)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.FOREX);
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
        .with(SurfacePropertyNames.PROPERTY_SURFACE_QUOTE_TYPE, SurfaceQuoteType.MARKET_STRANGLE_RISK_REVERSAL)
        .with(SurfacePropertyNames.PROPERTY_SURFACE_UNITS, SurfacePropertyNames.VOLATILITY_QUOTE).get());
  }
}
