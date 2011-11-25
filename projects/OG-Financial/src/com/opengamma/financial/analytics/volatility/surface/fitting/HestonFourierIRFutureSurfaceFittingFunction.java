/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface.fitting;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.apache.commons.lang.ObjectUtils;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceData;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
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
import com.opengamma.financial.analytics.volatility.fittedresults.HestonFittedSurfaces;
import com.opengamma.financial.analytics.volatility.surface.FuturePriceCurveData;
import com.opengamma.financial.analytics.volatility.surface.RawVolatilitySurfaceDataFunction;
import com.opengamma.financial.model.volatility.smile.fitting.HestonModelFitter;
import com.opengamma.financial.model.volatility.smile.function.HestonVolatilityFunction;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
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
public class HestonFourierIRFutureSurfaceFittingFunction extends AbstractFunction.NonCompiledInvoker {
  private static final double ERROR = 0.001;
  private static final HestonVolatilityFunction HESTON_FUNCTION = new HestonVolatilityFunction();
  private static final DoubleMatrix1D HESTON_INITIAL_VALUES = new DoubleMatrix1D(new double[] {1.5, 0.1, 0.1, 0.5, 0.0 });
  private static final LinearInterpolator1D LINEAR = (LinearInterpolator1D) Interpolator1DFactory.getInterpolator(Interpolator1DFactory.LINEAR);
  private static final FlatExtrapolator1D FLAT = new FlatExtrapolator1D();
  private static final GridInterpolator2D INTERPOLATOR = new GridInterpolator2D(LINEAR, LINEAR,
        FLAT, FLAT);
  private ValueSpecification _resultSpecification;
  private Currency _currency;
  private String _definitionName;
  private ValueRequirement _surfaceRequirement;
  private ValueRequirement _futurePriceRequirement;

  public HestonFourierIRFutureSurfaceFittingFunction(final String currency, final String definitionName) {
    this(Currency.of(currency), definitionName);
  }

  public HestonFourierIRFutureSurfaceFittingFunction(final Currency currency, final String definitionName) {
    _currency = currency;
    _definitionName = definitionName;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    final ComputationTargetSpecification currencyTargetSpec = new ComputationTargetSpecification(_currency);
    final ValueProperties surfaceProperties = ValueProperties.with(ValuePropertyNames.SURFACE, _definitionName)
                                                             .with(RawVolatilitySurfaceDataFunction.PROPERTY_SURFACE_INSTRUMENT_TYPE, "IR_FUTURE_OPTION").get();
    final ValueProperties futurePriceProperties = ValueProperties.with(ValuePropertyNames.CURVE, _definitionName)
                                                                 .with(RawVolatilitySurfaceDataFunction.PROPERTY_SURFACE_INSTRUMENT_TYPE, "IR_FUTURE_PRICE").get();
    _surfaceRequirement = new ValueRequirement(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA, currencyTargetSpec, surfaceProperties);
    _futurePriceRequirement = new ValueRequirement(ValueRequirementNames.FUTURE_PRICE_CURVE_DATA, currencyTargetSpec, futurePriceProperties);
    final ValueProperties resultProperties = createValueProperties()
          .with(ValuePropertyNames.CURRENCY, _currency.getCode())
          .with(ValuePropertyNames.SURFACE, _definitionName)
          .with(RawVolatilitySurfaceDataFunction.PROPERTY_SURFACE_INSTRUMENT_TYPE, "IR_FUTURE_OPTION").get();
    _resultSpecification = new ValueSpecification(ValueRequirementNames.HESTON_SURFACES, currencyTargetSpec, resultProperties);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Object objectSurfaceData = inputs.getValue(_surfaceRequirement);
    if (objectSurfaceData == null) {
      throw new OpenGammaRuntimeException("Could not get volatility surface data");
    }
    @SuppressWarnings("unchecked")
    final VolatilitySurfaceData<Double, Double> volatilitySurfaceData = (VolatilitySurfaceData<Double, Double>) objectSurfaceData;
    //TODO transform data to Map<Double, Map<Double, Double>> here
    final Object objectFuturePriceData = inputs.getValue(_futurePriceRequirement);
    if (objectFuturePriceData == null) {
      throw new OpenGammaRuntimeException("Could not get futures price data");
    }
    @SuppressWarnings("unchecked")
    final FuturePriceCurveData<Double> futurePriceData = (FuturePriceCurveData<Double>) objectFuturePriceData;
    //assumes that the sorting is first x, then y
    if (volatilitySurfaceData.size() == 0) {
      throw new OpenGammaRuntimeException("Interest rate future option volatility surface definition name=" + _definitionName + " contains no data");
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
    for (Double t : x) {
      List<ObjectsPair<Double, Double>> strip = volatilitySurfaceData.getYValuesForX(t);
      int n = strip.size();
      int strikeIndex = n - 1;
      final double[] strikes = new double[n];
      final double[] sigma = new double[n];
      final double[] errors = new double[n];
      double forward = 1 - futurePriceData.getFuturePrice(t);
      if (strip.size() > 4) {
        for (ObjectsPair<Double, Double> value : strip) {
          strikes[strikeIndex] = 1 - value.first / 100;
          sigma[strikeIndex] = value.second;
          errors[strikeIndex--] = ERROR;
        }
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
    final VolatilitySurface kappaSurface = new VolatilitySurface(InterpolatedDoublesSurface.from(fittedOptionExpiry, futureDelay, kappa, INTERPOLATOR, "Heston kappa surface"));
    final VolatilitySurface thetaSurface = new VolatilitySurface(InterpolatedDoublesSurface.from(fittedOptionExpiry, futureDelay, theta, INTERPOLATOR, "Heston theta surface"));
    final VolatilitySurface vol0Surface = new VolatilitySurface(InterpolatedDoublesSurface.from(fittedOptionExpiry, futureDelay, vol0, INTERPOLATOR, "Heston vol0 surface"));
    final VolatilitySurface omegaSurface = new VolatilitySurface(InterpolatedDoublesSurface.from(fittedOptionExpiry, futureDelay, omega, INTERPOLATOR, "Heston omega surface"));
    final VolatilitySurface rhoSurface = new VolatilitySurface(InterpolatedDoublesSurface.from(fittedOptionExpiry, futureDelay, rho, INTERPOLATOR, "Heston rho surface"));
    final HestonFittedSurfaces fittedSurfaces = new HestonFittedSurfaces(kappaSurface, thetaSurface, vol0Surface, omegaSurface, rhoSurface, inverseJacobians, _currency);
    return Sets.newHashSet(new ComputedValue(_resultSpecification, fittedSurfaces));
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
    return ObjectUtils.equals(target.getUniqueId(), _currency.getUniqueId());
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    return Sets.newHashSet(_futurePriceRequirement, _surfaceRequirement);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Sets.newHashSet(_resultSpecification);
  }

}
