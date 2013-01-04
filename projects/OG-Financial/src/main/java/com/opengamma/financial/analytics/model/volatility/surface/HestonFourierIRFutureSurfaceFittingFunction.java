/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.HestonModelFitter;
import com.opengamma.analytics.financial.model.volatility.smile.function.HestonVolatilityFunction;
import com.opengamma.analytics.math.curve.NodalDoublesCurve;
import com.opengamma.analytics.math.interpolation.FlatExtrapolator1D;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
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
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.volatility.fittedresults.HestonFittedSurfaces;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.ObjectsPair;

/**
 * 
 */
public class HestonFourierIRFutureSurfaceFittingFunction extends AbstractFunction.NonCompiledInvoker {
  private static final Logger s_logger = LoggerFactory.getLogger(HestonFourierIRFutureSurfaceFittingFunction.class);
  private static final double ERROR = 0.001;
  private static final HestonVolatilityFunction HESTON_FUNCTION = new HestonVolatilityFunction();
  private static final DoubleMatrix1D HESTON_INITIAL_VALUES = new DoubleMatrix1D(new double[] {1.5, 0.1, 0.1, 0.5, 0.0});
  private static final LinearInterpolator1D LINEAR = (LinearInterpolator1D) Interpolator1DFactory.getInterpolator(Interpolator1DFactory.LINEAR);
  private static final FlatExtrapolator1D FLAT = new FlatExtrapolator1D();
  private static final GridInterpolator2D INTERPOLATOR = new GridInterpolator2D(LINEAR, LINEAR, FLAT, FLAT);

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ValueRequirement desiredValue = desiredValues.iterator().next();

    //currency
    final Currency currency = Currency.of(((UniqueId) target.getValue()).getValue());

    // future curve
    final Object objectFuturePriceData = inputs.getValue(ValueRequirementNames.FUTURE_PRICE_CURVE_DATA);
    if (objectFuturePriceData == null) {
      throw new OpenGammaRuntimeException("Could not get futures price data");
    }
    final NodalDoublesCurve futurePriceData = (NodalDoublesCurve) objectFuturePriceData;

    // surface
    final String surfaceName = desiredValue.getConstraint(ValuePropertyNames.SURFACE);
    final Object objectSurfaceData = inputs.getValue(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA);
    if (objectSurfaceData == null) {
      throw new OpenGammaRuntimeException("Could not get volatility surface data");
    }
    @SuppressWarnings("unchecked")
    final VolatilitySurfaceData<Double, Double> volatilitySurfaceData = (VolatilitySurfaceData<Double, Double>) objectSurfaceData;

    //assumes that the sorting is first x, then y
    if (volatilitySurfaceData.size() == 0) {
      throw new OpenGammaRuntimeException("Interest rate future option volatility surface definition name=" + futurePriceData.getName() + " contains no data");
    }

    final SortedSet<Double> x = volatilitySurfaceData.getUniqueXValues();
    final DoubleArrayList fittedOptionExpiryList = new DoubleArrayList();
    final DoubleArrayList futureDelayList = new DoubleArrayList();
    final DoubleArrayList kappaList = new DoubleArrayList();
    final DoubleArrayList thetaList = new DoubleArrayList();
    final DoubleArrayList vol0List = new DoubleArrayList();
    final DoubleArrayList omegaList = new DoubleArrayList();
    final DoubleArrayList rhoList = new DoubleArrayList();
    final DoubleArrayList chiSqList = new DoubleArrayList();
    final Map<DoublesPair, DoubleMatrix2D> inverseJacobians = new HashMap<DoublesPair, DoubleMatrix2D>();
    for (final Double t : x) {
      final List<ObjectsPair<Double, Double>> strip = volatilitySurfaceData.getYValuesForX(t);
      final int n = strip.size();
      final DoubleArrayList strikesList = new DoubleArrayList(n);
      final DoubleArrayList sigmaList = new DoubleArrayList(n);
      final DoubleArrayList errorsList = new DoubleArrayList(n);
      final Double futurePrice = futurePriceData.getYValue(t);
      if (strip.size() > 4 && futurePrice != null) {
        final double forward = 1 - futurePrice / 100;
        for (final ObjectsPair<Double, Double> value : strip) {
          if (value.first != null && value.second != null) {
            strikesList.add(1 - value.first / 100);
            sigmaList.add(value.second);
            errorsList.add(ERROR);
          }
        }
        if (!strikesList.isEmpty()) {
          final double[] strikes = strikesList.toDoubleArray();
          final double[] sigma = sigmaList.toDoubleArray();
          final double[] errors = errorsList.toDoubleArray();
          ArrayUtils.reverse(strikes);
          ArrayUtils.reverse(sigma);
          ArrayUtils.reverse(errors);
          final LeastSquareResultsWithTransform fittedResult = new HestonModelFitter(forward, strikes, t, sigma, errors, HESTON_FUNCTION).solve(HESTON_INITIAL_VALUES);
          final DoubleMatrix1D parameters = fittedResult.getModelParameters();
          fittedOptionExpiryList.add(t);
          futureDelayList.add(0);
          kappaList.add(parameters.getEntry(0));
          thetaList.add(parameters.getEntry(1));
          vol0List.add(parameters.getEntry(2));
          omegaList.add(parameters.getEntry(3));
          rhoList.add(parameters.getEntry(4));
          inverseJacobians.put(DoublesPair.of(t.doubleValue(), 0.), fittedResult.getModelParameterSensitivityToData());
          chiSqList.add(fittedResult.getChiSq());
        }
      }
    }
    if (fittedOptionExpiryList.size() < 5) { //don't have sufficient fits to construct a surface
      throw new OpenGammaRuntimeException("Could not construct Heston parameter surfaces; have under 5 surface points");
    }
    final double[] fittedOptionExpiry = fittedOptionExpiryList.toDoubleArray();
    final double[] futureDelay = futureDelayList.toDoubleArray();
    final double[] kappa = kappaList.toDoubleArray();
    final double[] theta = thetaList.toDoubleArray();
    final double[] vol0 = vol0List.toDoubleArray();
    final double[] omega = omegaList.toDoubleArray();
    final double[] rho = rhoList.toDoubleArray();
    final InterpolatedDoublesSurface kappaSurface = InterpolatedDoublesSurface.from(fittedOptionExpiry, futureDelay, kappa, INTERPOLATOR, "Heston kappa surface");
    final InterpolatedDoublesSurface thetaSurface = InterpolatedDoublesSurface.from(fittedOptionExpiry, futureDelay, theta, INTERPOLATOR, "Heston theta surface");
    final InterpolatedDoublesSurface vol0Surface = InterpolatedDoublesSurface.from(fittedOptionExpiry, futureDelay, vol0, INTERPOLATOR, "Heston vol0 surface");
    final InterpolatedDoublesSurface omegaSurface = InterpolatedDoublesSurface.from(fittedOptionExpiry, futureDelay, omega, INTERPOLATOR, "Heston omega surface");
    final InterpolatedDoublesSurface rhoSurface = InterpolatedDoublesSurface.from(fittedOptionExpiry, futureDelay, rho, INTERPOLATOR, "Heston rho surface");
    final HestonFittedSurfaces fittedSurfaces = new HestonFittedSurfaces(kappaSurface, thetaSurface, vol0Surface, omegaSurface, rhoSurface, inverseJacobians, currency);
    final ValueProperties resultProperties = createValueProperties()
        .with(ValuePropertyNames.CURRENCY, currency.getCode())
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.IR_FUTURE_OPTION).get();
    final ValueSpecification resultSpecification = new ValueSpecification(ValueRequirementNames.HESTON_SURFACES, target.toSpecification(), resultProperties);
    return Sets.newHashSet(new ComputedValue(resultSpecification, fittedSurfaces));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.CURRENCY;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<String> surfaceNames = desiredValue.getConstraints().getValues(ValuePropertyNames.SURFACE);
    if (surfaceNames == null || surfaceNames.size() != 1) {
      return null;
    }
    final String surfaceName = surfaceNames.iterator().next();
    final ValueProperties surfaceProperties = ValueProperties.with(ValuePropertyNames.SURFACE, surfaceName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.IR_FUTURE_OPTION).get();
    final ValueRequirement surfaceRequirement = new ValueRequirement(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA, target.toSpecification(), surfaceProperties);
    final ValueProperties futurePriceProperties = ValueProperties.with(ValuePropertyNames.CURVE, surfaceName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.IR_FUTURE_PRICE).get();
    final ValueRequirement futurePriceRequirement = new ValueRequirement(ValueRequirementNames.FUTURE_PRICE_CURVE_DATA, target.toSpecification(), futurePriceProperties);
    return Sets.newHashSet(futurePriceRequirement, surfaceRequirement);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties resultProperties = createValueProperties()
        .with(ValuePropertyNames.CURRENCY, target.getUniqueId().getValue())
        .withAny(ValuePropertyNames.SURFACE)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.IR_FUTURE_OPTION).get();
    final ValueSpecification resultSpecification = new ValueSpecification(ValueRequirementNames.HESTON_SURFACES, target.toSpecification(), resultProperties);
    return Sets.newHashSet(resultSpecification);
  }

}
