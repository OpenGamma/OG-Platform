/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface;

import static com.opengamma.financial.analytics.model.volatility.surface.SABRFittingProperties.PROPERTY_ERROR;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.SABRModelFitter;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.analytics.math.curve.NodalDoublesCurve;
import com.opengamma.analytics.math.interpolation.Interpolator2D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.statistics.leastsquare.LeastSquareResultsWithTransform;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceData;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.PrimitiveComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.volatility.SmileFittingPropertyNamesAndValues;
import com.opengamma.financial.analytics.model.volatility.surface.fitted.SurfaceFittedSmileDataPoints;
import com.opengamma.financial.analytics.volatility.fittedresults.SABRFittedSurfaces;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.ObjectsPair;

/**
 *
 */
public class SABRNonLinearLeastSquaresIRFutureOptionSurfaceFittingFunction extends AbstractFunction.NonCompiledInvoker {
  /** A logger */
  private static final Logger s_logger = LoggerFactory.getLogger(SABRNonLinearLeastSquaresIRFutureOptionSurfaceFittingFunction.class);
  /** Hagan SABR function */
  private static final SABRHaganVolatilityFunction SABR_FUNCTION = new SABRHaganVolatilityFunction();

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final Object objectSurfaceData = inputs.getValue(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA);
    if (objectSurfaceData == null) {
      throw new OpenGammaRuntimeException("Could not get volatility surface data");
    }
    @SuppressWarnings("unchecked")
    final VolatilitySurfaceData<Number, Double> volatilitySurfaceData = (VolatilitySurfaceData<Number, Double>) objectSurfaceData;
    final Object objectFuturePriceData = inputs.getValue(ValueRequirementNames.FUTURE_PRICE_CURVE_DATA);
    if (objectFuturePriceData == null) {
      throw new OpenGammaRuntimeException("Could not get futures price data");
    }
    final NodalDoublesCurve futurePriceData = (NodalDoublesCurve) objectFuturePriceData;
    //assumes that the sorting is first x, then y
    if (volatilitySurfaceData.size() == 0) {
      throw new OpenGammaRuntimeException("Interest rate future option volatility surface name" + desiredValue.getConstraint(ValuePropertyNames.SURFACE) + " contains no data");
    }
    final Double error = Double.parseDouble(desiredValue.getConstraint(PROPERTY_ERROR));
    final DoubleMatrix1D sabrInitialValues = SABRFittingPropertyUtils.getStartingValues(desiredValue);
    final BitSet fixed = SABRFittingPropertyUtils.getFixedValues(desiredValue);
    final Interpolator2D interpolator = SABRFittingPropertyUtils.getInterpolator(desiredValue);
    final SortedSet<Number> timeValues = volatilitySurfaceData.getUniqueXValues();
    final DoubleArrayList fittedOptionExpiryList = new DoubleArrayList();
    final DoubleArrayList futureDelayList = new DoubleArrayList();
    final DoubleArrayList alphaList = new DoubleArrayList();
    final DoubleArrayList betaList = new DoubleArrayList();
    final DoubleArrayList nuList = new DoubleArrayList();
    final DoubleArrayList rhoList = new DoubleArrayList();
    final DoubleArrayList chiSqList = new DoubleArrayList();
    final Map<DoublesPair, DoubleMatrix2D> inverseJacobians = new HashMap<DoublesPair, DoubleMatrix2D>();
    final Map<Double, List<Double>> dataPointsForStrip = new HashMap<Double, List<Double>>();
    for (final Number ttm : timeValues) {
      final List<Double> fittedPointsForStrip = new ArrayList<Double>();
      final List<ObjectsPair<Double, Double>> strip = volatilitySurfaceData.getYValuesForX(ttm);
      final DoubleArrayList errors = new DoubleArrayList();
      final DoubleArrayList strikes = new DoubleArrayList();
      final DoubleArrayList blackVols = new DoubleArrayList();
      if (strip.size() > 4) {
        try {
          final Double forward = futurePriceData.getYValue(ttm.doubleValue());
          for (final ObjectsPair<Double, Double> value : strip) {
            if (value.second != null) {
              strikes.add(1 - value.first);
              blackVols.add(value.second);
              errors.add(error);
              fittedPointsForStrip.add(value.first);
            }
          }
          if (blackVols.size() > 4) {
            final LeastSquareResultsWithTransform fittedResult = new SABRModelFitter(forward, strikes.toDoubleArray(), ttm.doubleValue(), blackVols.toDoubleArray(),
                errors.toDoubleArray(), SABR_FUNCTION).solve(sabrInitialValues, fixed);
            final DoubleMatrix1D parameters = fittedResult.getModelParameters();
            fittedOptionExpiryList.add(ttm.doubleValue());
            futureDelayList.add(0);
            alphaList.add(parameters.getEntry(0));
            betaList.add(parameters.getEntry(1));
            nuList.add(parameters.getEntry(2));
            rhoList.add(parameters.getEntry(3));
            inverseJacobians.put(DoublesPair.of(ttm.doubleValue(), 0.), fittedResult.getModelParameterSensitivityToData());
            chiSqList.add(fittedResult.getChiSq());
            dataPointsForStrip.put(ttm.doubleValue(), fittedPointsForStrip);
          }
        } catch (final IllegalArgumentException e) {
          s_logger.info("Could not get values for forward for x={}", ttm);
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
    final InterpolatedDoublesSurface alphaSurface = InterpolatedDoublesSurface.from(fittedOptionExpiry, futureDelay, alpha, interpolator, "SABR alpha surface");
    final InterpolatedDoublesSurface betaSurface = InterpolatedDoublesSurface.from(fittedOptionExpiry, futureDelay, beta, interpolator, "SABR beta surface");
    final InterpolatedDoublesSurface nuSurface = InterpolatedDoublesSurface.from(fittedOptionExpiry, futureDelay, nu, interpolator, "SABR nu surface");
    final InterpolatedDoublesSurface rhoSurface = InterpolatedDoublesSurface.from(fittedOptionExpiry, futureDelay, rho, interpolator, "SABR rho surface");
    final SABRFittedSurfaces fittedSurfaces = new SABRFittedSurfaces(alphaSurface, betaSurface, nuSurface, rhoSurface, inverseJacobians);
    final ValueProperties resultProperties = desiredValue.getConstraints().copy()
        .withoutAny(ValuePropertyNames.FUNCTION)
        .with(ValuePropertyNames.FUNCTION, getUniqueId()).get();
    final ValueSpecification resultSpecification = new ValueSpecification(ValueRequirementNames.SABR_SURFACES, target.toSpecification(), resultProperties);
    final ValueSpecification fittedPointsSpecification = new ValueSpecification(ValueRequirementNames.VOLATILITY_SURFACE_FITTED_POINTS, target.toSpecification(), resultProperties);
    return Sets.newHashSet(new ComputedValue(resultSpecification, fittedSurfaces), new ComputedValue(fittedPointsSpecification, new SurfaceFittedSmileDataPoints(dataPointsForStrip)));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.CURRENCY;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final Currency currency = target.getValue(PrimitiveComputationTargetType.CURRENCY);
    final ValueProperties resultProperties = SABRFittingPropertyUtils.addNLSSFittingProperties(createValueProperties())
        .with(ValuePropertyNames.CURRENCY, currency.getCode())
        .withAny(ValuePropertyNames.SURFACE)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.IR_FUTURE_OPTION)
        .with(SmileFittingPropertyNamesAndValues.PROPERTY_VOLATILITY_MODEL, SmileFittingPropertyNamesAndValues.SABR)
        .with(SmileFittingPropertyNamesAndValues.PROPERTY_FITTING_METHOD, SmileFittingPropertyNamesAndValues.NON_LINEAR_LEAST_SQUARES).get();
    final ValueSpecification resultSpecification = new ValueSpecification(ValueRequirementNames.SABR_SURFACES, target.toSpecification(), resultProperties);
    final ValueSpecification fittedPointsSpecification = new ValueSpecification(ValueRequirementNames.VOLATILITY_SURFACE_FITTED_POINTS, target.toSpecification(), resultProperties);
    return Sets.newHashSet(resultSpecification, fittedPointsSpecification);
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<String> surfaceNames = desiredValue.getConstraints().getValues(ValuePropertyNames.SURFACE);
    if (surfaceNames == null || surfaceNames.size() != 1) {
      s_logger.error("Need to provide a single surface name; have {}", surfaceNames);
      return null;
    }
    if (!SABRFittingPropertyUtils.ensureNLSSFittingProperties(desiredValue)) {
      return null;
    }
    final String surfaceName = Iterables.getOnlyElement(surfaceNames);
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

}
