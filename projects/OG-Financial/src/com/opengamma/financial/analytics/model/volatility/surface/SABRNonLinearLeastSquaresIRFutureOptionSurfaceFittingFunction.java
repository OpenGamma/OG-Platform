/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
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
import com.opengamma.financial.analytics.model.irfutureoption.IRFutureOptionUtils;
import com.opengamma.financial.analytics.volatility.fittedresults.SABRFittedSurfaces;
import com.opengamma.financial.analytics.volatility.surface.fitting.SurfaceFittedSmileDataPoints;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.model.volatility.smile.fitting.SABRModelFitter;
import com.opengamma.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.id.UniqueId;
import com.opengamma.math.curve.NodalDoublesCurve;
import com.opengamma.math.interpolation.FlatExtrapolator1D;
import com.opengamma.math.interpolation.GridInterpolator2D;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.statistics.leastsquare.LeastSquareResultsWithTransform;
import com.opengamma.math.surface.InterpolatedDoublesSurface;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.ObjectsPair;

/**
 * 
 */
public class SABRNonLinearLeastSquaresIRFutureOptionSurfaceFittingFunction extends AbstractFunction.NonCompiledInvoker {
  private static final Logger s_logger = LoggerFactory.getLogger(SABRNonLinearLeastSquaresIRFutureOptionSurfaceFittingFunction.class);
  private static final double ERROR = 0.001;
  private static final SABRHaganVolatilityFunction SABR_FUNCTION = new SABRHaganVolatilityFunction();
  private static final DoubleMatrix1D SABR_INITIAL_VALUES = new DoubleMatrix1D(new double[] {0.05, 1., 0.7, 0.0});
  private static final BitSet FIXED = new BitSet();
  private static final LinearInterpolator1D LINEAR = (LinearInterpolator1D) Interpolator1DFactory.getInterpolator(Interpolator1DFactory.LINEAR);
  private static final FlatExtrapolator1D FLAT = new FlatExtrapolator1D();
  private static final GridInterpolator2D INTERPOLATOR = new GridInterpolator2D(LINEAR, LINEAR, FLAT, FLAT);
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");

  static {
    FIXED.set(1);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String surfaceName = desiredValue.getConstraint(ValuePropertyNames.SURFACE);
    final ValueProperties surfaceProperties = ValueProperties.builder()
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.IR_FUTURE_OPTION).get();
    final ValueProperties futurePriceProperties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, surfaceName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.IR_FUTURE_PRICE).get();
    final ValueRequirement surfaceRequirement = new ValueRequirement(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA, target.toSpecification(), surfaceProperties);
    final ValueRequirement futurePriceRequirement = new ValueRequirement(ValueRequirementNames.FUTURE_PRICE_CURVE_DATA, target.toSpecification(), futurePriceProperties);
    final Object objectSurfaceData = inputs.getValue(surfaceRequirement);
    if (objectSurfaceData == null) {
      throw new OpenGammaRuntimeException("Could not get volatility surface data");
    }
    @SuppressWarnings("unchecked")
    final VolatilitySurfaceData<Number, Double> volatilitySurfaceData = (VolatilitySurfaceData<Number, Double>) objectSurfaceData;
    final Object objectFuturePriceData = inputs.getValue(futurePriceRequirement);
    if (objectFuturePriceData == null) {
      throw new OpenGammaRuntimeException("Could not get futures price data");
    }
    final NodalDoublesCurve futurePriceData = (NodalDoublesCurve) objectFuturePriceData;
    //assumes that the sorting is first x, then y
    if (volatilitySurfaceData.size() == 0) {
      throw new OpenGammaRuntimeException("Interest rate future option volatility surface name=" + surfaceName + " contains no data");
    }
    final SortedSet<Number> xValues = volatilitySurfaceData.getUniqueXValues();
    final DoubleArrayList fittedOptionExpiryList = new DoubleArrayList();
    final DoubleArrayList futureDelayList = new DoubleArrayList();
    final DoubleArrayList alphaList = new DoubleArrayList();
    final DoubleArrayList betaList = new DoubleArrayList();
    final DoubleArrayList nuList = new DoubleArrayList();
    final DoubleArrayList rhoList = new DoubleArrayList();
    final DoubleArrayList chiSqList = new DoubleArrayList();
    final Map<DoublesPair, DoubleMatrix2D> inverseJacobians = new HashMap<DoublesPair, DoubleMatrix2D>();
    final Map<Double, List<Double>> dataPointsForStrip = new HashMap<Double, List<Double>>();
    for (final Number x : xValues) {
      final double t = IRFutureOptionUtils.getTime(x, now);
      final List<Double> fittedPointsForStrip = new ArrayList<Double>();
      final List<ObjectsPair<Double, Double>> strip = volatilitySurfaceData.getYValuesForX(x);
      final DoubleArrayList errors = new DoubleArrayList();
      final DoubleArrayList strikes = new DoubleArrayList();
      final DoubleArrayList blackVols = new DoubleArrayList();
      if (strip.size() > 4) {
        try {
          final Double forward = futurePriceData.getYValue(x.doubleValue());
          for (final ObjectsPair<Double, Double> value : strip) {
            if (value.second != null) {
              strikes.add(1 - value.first);
              blackVols.add(value.second);
              errors.add(ERROR);
              fittedPointsForStrip.add(value.first);
            }
          }
          if (blackVols.size() > 4) {
            final LeastSquareResultsWithTransform fittedResult = new SABRModelFitter(forward, strikes.toDoubleArray(), t, blackVols.toDoubleArray(),
                errors.toDoubleArray(), SABR_FUNCTION).solve(SABR_INITIAL_VALUES, FIXED);
            final DoubleMatrix1D parameters = fittedResult.getModelParameters();
            fittedOptionExpiryList.add(t);
            futureDelayList.add(0);
            alphaList.add(parameters.getEntry(0));
            betaList.add(parameters.getEntry(1));
            nuList.add(parameters.getEntry(2));
            rhoList.add(parameters.getEntry(3));
            inverseJacobians.put(DoublesPair.of(x.doubleValue(), 0.), fittedResult.getModelParameterSensitivityToData());
            chiSqList.add(fittedResult.getChiSq());
            dataPointsForStrip.put(x.doubleValue(), fittedPointsForStrip);
          }
        } catch (final IllegalArgumentException e) {
          s_logger.info("Could not get values for forward for x={}", x);
        }
      }
    }
    if (fittedOptionExpiryList.size() < 4) { //don't have sufficient fits to construct a surface
      throw new OpenGammaRuntimeException("Could not construct SABR parameter surfaces; have less than 3 surface points");
    }
    final double[] fittedOptionExpiry = fittedOptionExpiryList.toDoubleArray();
    final double[] futureDelay = futureDelayList.toDoubleArray();
    final double[] alpha = alphaList.toDoubleArray();
    final double[] beta = betaList.toDoubleArray();
    final double[] nu = nuList.toDoubleArray();
    final double[] rho = rhoList.toDoubleArray();
    final InterpolatedDoublesSurface alphaSurface = InterpolatedDoublesSurface.from(fittedOptionExpiry, futureDelay, alpha, INTERPOLATOR, "SABR alpha surface");
    final InterpolatedDoublesSurface betaSurface = InterpolatedDoublesSurface.from(fittedOptionExpiry, futureDelay, beta, INTERPOLATOR, "SABR beta surface");
    final InterpolatedDoublesSurface nuSurface = InterpolatedDoublesSurface.from(fittedOptionExpiry, futureDelay, nu, INTERPOLATOR, "SABR nu surface");
    final InterpolatedDoublesSurface rhoSurface = InterpolatedDoublesSurface.from(fittedOptionExpiry, futureDelay, rho, INTERPOLATOR, "SABR rho surface");
    final Currency currency = Currency.of(((UniqueId) target.getValue()).getValue());
    final SABRFittedSurfaces fittedSurfaces = new SABRFittedSurfaces(alphaSurface, betaSurface, nuSurface, rhoSurface, inverseJacobians, currency, DAY_COUNT);
    final ValueProperties resultProperties = createValueProperties()
        .with(ValuePropertyNames.CURRENCY, currency.getCode())
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.IR_FUTURE_OPTION).get();
    final ValueSpecification resultSpecification = new ValueSpecification(ValueRequirementNames.SABR_SURFACES, target.toSpecification(), resultProperties);
    final ValueSpecification fittedPointsSpecification = new ValueSpecification(ValueRequirementNames.VOLATILITY_SURFACE_FITTED_POINTS, target.toSpecification(), resultProperties);
    return Sets.newHashSet(new ComputedValue(resultSpecification, fittedSurfaces), new ComputedValue(fittedPointsSpecification, new SurfaceFittedSmileDataPoints(dataPointsForStrip)));
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
    return Currency.OBJECT_SCHEME.equals(target.getUniqueId().getScheme()) && target.getUniqueId().getValue().length() == 3;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final Currency currency = Currency.of(((UniqueId) target.getValue()).getValue());
    final ValueProperties resultProperties = createValueProperties()
        .with(ValuePropertyNames.CURRENCY, currency.getCode())
        .withAny(ValuePropertyNames.SURFACE)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.IR_FUTURE_OPTION).get();
    final ValueSpecification resultSpecification = new ValueSpecification(ValueRequirementNames.SABR_SURFACES, target.toSpecification(), resultProperties);
    final ValueSpecification fittedPointsSpecification = new ValueSpecification(ValueRequirementNames.VOLATILITY_SURFACE_FITTED_POINTS, target.toSpecification(), resultProperties);
    return Sets.newHashSet(resultSpecification, fittedPointsSpecification);
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<String> surfaceNames = desiredValue.getConstraints().getValues(ValuePropertyNames.SURFACE);
    if (surfaceNames == null || surfaceNames.size() != 1) {
      return null;
    }
    final String surfaceName = surfaceNames.iterator().next();
    final ValueProperties surfaceProperties = ValueProperties.builder()
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.IR_FUTURE_OPTION).get();
    final ValueProperties futurePriceProperties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, surfaceName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.IR_FUTURE_PRICE).get();
    final ValueRequirement surfaceRequirement = new ValueRequirement(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA, target.toSpecification(), surfaceProperties);
    final ValueRequirement futurePriceRequirement = new ValueRequirement(ValueRequirementNames.FUTURE_PRICE_CURVE_DATA, target.toSpecification(), futurePriceProperties);
    return Sets.newHashSet(futurePriceRequirement, surfaceRequirement);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    String surfaceName = null;
    for (final Map.Entry<ValueSpecification, ValueRequirement> input : inputs.entrySet()) {
      if (ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA.equals(input.getKey().getValueName())) {
        surfaceName = input.getKey().getProperty(ValuePropertyNames.SURFACE);
        break;
      }
    }
    assert surfaceName != null;
    final Currency currency = Currency.of(((UniqueId) target.getValue()).getValue());
    final ValueProperties resultProperties = createValueProperties()
        .with(ValuePropertyNames.CURRENCY, currency.getCode())
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.IR_FUTURE_OPTION).get();
    final ValueSpecification resultSpecification = new ValueSpecification(ValueRequirementNames.SABR_SURFACES, target.toSpecification(), resultProperties);
    final ValueSpecification fittedPointsSpecification = new ValueSpecification(ValueRequirementNames.VOLATILITY_SURFACE_FITTED_POINTS, target.toSpecification(), resultProperties);
    return Sets.newHashSet(resultSpecification, fittedPointsSpecification);
  }

}
