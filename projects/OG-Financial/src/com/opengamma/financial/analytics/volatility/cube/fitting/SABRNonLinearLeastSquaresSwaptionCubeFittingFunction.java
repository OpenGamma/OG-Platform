/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.cube.fitting;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import javax.time.calendar.Period;

import org.apache.commons.lang.ObjectUtils;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.marketdatasnapshot.VolatilityCubeData;
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
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeFunctionHelper;
import com.opengamma.financial.analytics.volatility.fittedresults.SABRFittedSurfaces;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.model.volatility.smile.fitting.SABRModelFitter;
import com.opengamma.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.id.ExternalId;
import com.opengamma.math.interpolation.FlatExtrapolator1D;
import com.opengamma.math.interpolation.GridInterpolator2D;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.statistics.leastsquare.LeastSquareResultsWithTransform;
import com.opengamma.math.surface.InterpolatedDoublesSurface;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class SABRNonLinearLeastSquaresSwaptionCubeFittingFunction extends AbstractFunction.NonCompiledInvoker {
  private static final double ERROR = 0.001;
  private static final BitSet FIXED = new BitSet();
  private static final SABRHaganVolatilityFunction SABR_FUNCTION = new SABRHaganVolatilityFunction();
  private static final DoubleMatrix1D SABR_INITIAL_VALUES = new DoubleMatrix1D(new double[] {0.05, 0.5, 0.7, 0.3});
  private static final LinearInterpolator1D LINEAR = (LinearInterpolator1D) Interpolator1DFactory.getInterpolator(Interpolator1DFactory.LINEAR);
  private static final FlatExtrapolator1D FLAT = new FlatExtrapolator1D();
  private static final GridInterpolator2D INTERPOLATOR = new GridInterpolator2D(LINEAR, LINEAR, FLAT, FLAT);
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA");
  private final VolatilityCubeFunctionHelper _volCubeHelper;
  private ValueSpecification _sabrSurfacesSpecification;
  private ValueSpecification _smileIdsSpecification;
  private ValueRequirement _cubeRequirement;

  static {
    FIXED.set(1);
  }

  public SABRNonLinearLeastSquaresSwaptionCubeFittingFunction(final String currency, final String definitionName) {
    this(Currency.of(currency), definitionName);
  }

  public SABRNonLinearLeastSquaresSwaptionCubeFittingFunction(final Currency currency, final String definitionName) {
    _volCubeHelper = new VolatilityCubeFunctionHelper(currency, definitionName);
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    final ComputationTargetSpecification currencyTargetSpec = new ComputationTargetSpecification(_volCubeHelper.getCurrency());
    final ValueProperties cubeProperties = ValueProperties.with(ValuePropertyNames.CUBE, _volCubeHelper.getDefinitionName()).get();
    _cubeRequirement = new ValueRequirement(ValueRequirementNames.STANDARD_VOLATILITY_CUBE_DATA, currencyTargetSpec, cubeProperties);
    final ValueProperties resultProperties = createValueProperties().with(ValuePropertyNames.CURRENCY, _volCubeHelper.getCurrency().getCode())
        .with(ValuePropertyNames.CUBE, _volCubeHelper.getDefinitionName()).get();
    _sabrSurfacesSpecification = new ValueSpecification(ValueRequirementNames.SABR_SURFACES, currencyTargetSpec, resultProperties);
    _smileIdsSpecification = new ValueSpecification(ValueRequirementNames.VOLATILITY_CUBE_FITTED_POINTS, currencyTargetSpec, resultProperties); //TODO add fitting method information
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Object objectCubeData = inputs.getValue(_cubeRequirement);
    if (objectCubeData == null) {
      throw new OpenGammaRuntimeException("Could not get volatility cube data");
    }
    final VolatilityCubeData volatilityCubeData = (VolatilityCubeData) objectCubeData;
    final SortedMap<Tenor, SortedMap<Tenor, Pair<double[], double[]>>> smiles = volatilityCubeData.getSmiles();
    final SortedMap<Tenor, SortedMap<Tenor, ExternalId[]>> smileIds = volatilityCubeData.getSmileIds();
    final SortedMap<Tenor, SortedMap<Tenor, Double[]>> smileRelativeStrikes = volatilityCubeData.getSmileRelativeStrikes();
    final DoubleArrayList swapMaturitiesList = new DoubleArrayList();
    final DoubleArrayList swaptionExpiriesList = new DoubleArrayList();
    final DoubleArrayList alphaList = new DoubleArrayList();
    final DoubleArrayList betaList = new DoubleArrayList();
    final DoubleArrayList nuList = new DoubleArrayList();
    final DoubleArrayList rhoList = new DoubleArrayList();
    final DoubleArrayList chiSqList = new DoubleArrayList();
    final Map<DoublesPair, DoubleMatrix2D> inverseJacobians = new HashMap<DoublesPair, DoubleMatrix2D>();
    final Map<Pair<Tenor, Tenor>, ExternalId[]> fittedSmileIds = new HashMap<Pair<Tenor, Tenor>, ExternalId[]>();
    final Map<Pair<Tenor, Tenor>, Double[]> fittedRelativeStrikes = new HashMap<Pair<Tenor, Tenor>, Double[]>();
    for (final Map.Entry<Tenor, SortedMap<Tenor, Pair<double[], double[]>>> swapMaturityEntry : smiles.entrySet()) {
      final double maturity = getTime(swapMaturityEntry.getKey());
      for (final Map.Entry<Tenor, Pair<double[], double[]>> swaptionExpiryEntry : swapMaturityEntry.getValue().entrySet()) {
        final double swaptionExpiry = getTime(swaptionExpiryEntry.getKey());
        final double[] strikes = swaptionExpiryEntry.getValue().getFirst();
        final double[] blackVols = swaptionExpiryEntry.getValue().getSecond();
        final int n = strikes.length;
        if (n != blackVols.length) {
          throw new OpenGammaRuntimeException("Strike and Black volatility arrays were not the same length; should never happen");
        }
        ExternalId[] externalIds = new ExternalId[0];
        if (smileIds != null) {
          externalIds = smileIds.get(swapMaturityEntry.getKey()).get(swaptionExpiryEntry.getKey());
          if (n != externalIds.length) {
            throw new OpenGammaRuntimeException("Strike and id arrays were not the same length; should never happen");
          }
        }
        Double[] relativeStrikes = new Double[0];
        if (smileRelativeStrikes != null) {
          relativeStrikes = smileRelativeStrikes.get(swapMaturityEntry.getKey()).get(swaptionExpiryEntry.getKey());
          if (n != relativeStrikes.length) {
            throw new OpenGammaRuntimeException("Strike and relative strike arrays were not the same length; should never happen");
          }
        }
        final double[] errors = new double[n];
        final Pair<Tenor, Tenor> tenorPair = Pair.of(swapMaturityEntry.getKey(), swaptionExpiryEntry.getKey());
        if (volatilityCubeData.getATMStrikes() != null && volatilityCubeData.getATMStrikes().containsKey(tenorPair)) {
          final double forward = volatilityCubeData.getATMStrikes().get(tenorPair);
          for (int k = 0; k < n; k++) {
            errors[k] = ERROR;
          }
          if (strikes.length > 4 && forward > 0) { //don't fit those smiles with insufficient data 
            final LeastSquareResultsWithTransform fittedResult = new SABRModelFitter(forward, strikes, swaptionExpiry, blackVols, errors, SABR_FUNCTION).solve(SABR_INITIAL_VALUES, FIXED);
            final DoubleMatrix1D parameters = fittedResult.getModelParameters();
            swapMaturitiesList.add(maturity);
            swaptionExpiriesList.add(swaptionExpiry);
            alphaList.add(parameters.getEntry(0));
            betaList.add(parameters.getEntry(1));
            rhoList.add(parameters.getEntry(2));
            nuList.add(parameters.getEntry(3));
            DoublesPair expiryMaturityPair = new DoublesPair(swaptionExpiry, maturity);
            inverseJacobians.put(expiryMaturityPair, fittedResult.getModelParameterSensitivityToData());
            chiSqList.add(fittedResult.getChiSq());
            fittedSmileIds.put(tenorPair, externalIds);
            fittedRelativeStrikes.put(tenorPair, relativeStrikes);
          }
        }
      }
    }
    if (swapMaturitiesList.size() < 5) { //don't have sufficient fits to construct a surface
      throw new OpenGammaRuntimeException("Could not construct SABR parameter surfaces; have under 5 surface points");
    }
    final double[] swapMaturities = swapMaturitiesList.toDoubleArray();
    final double[] swaptionExpiries = swaptionExpiriesList.toDoubleArray();
    final double[] alpha = alphaList.toDoubleArray();
    final double[] beta = betaList.toDoubleArray();
    final double[] nu = nuList.toDoubleArray();
    final double[] rho = rhoList.toDoubleArray();
    final InterpolatedDoublesSurface alphaSurface = InterpolatedDoublesSurface.from(swaptionExpiries, swapMaturities, alpha, INTERPOLATOR, "SABR alpha surface");
    final InterpolatedDoublesSurface betaSurface = InterpolatedDoublesSurface.from(swaptionExpiries, swapMaturities, beta, INTERPOLATOR, "SABR beta surface");
    final InterpolatedDoublesSurface nuSurface = InterpolatedDoublesSurface.from(swaptionExpiries, swapMaturities, nu, INTERPOLATOR, "SABR nu surface");
    final InterpolatedDoublesSurface rhoSurface = InterpolatedDoublesSurface.from(swaptionExpiries, swapMaturities, rho, INTERPOLATOR, "SABR rho surface");
    final SABRFittedSurfaces fittedSurfaces = new SABRFittedSurfaces(alphaSurface, betaSurface, nuSurface, rhoSurface, inverseJacobians, _volCubeHelper.getCurrency(), DAY_COUNT);
    return Sets.newHashSet(new ComputedValue(_sabrSurfacesSpecification, fittedSurfaces), new ComputedValue(_smileIdsSpecification, new FittedSmileDataPoints(fittedSmileIds, fittedRelativeStrikes)));
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
    return ObjectUtils.equals(target.getUniqueId(), _volCubeHelper.getCurrency().getUniqueId());
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    return Sets.newHashSet(_cubeRequirement);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Sets.newHashSet(_sabrSurfacesSpecification, _smileIdsSpecification);
  }

  private double getTime(final Tenor tenor) {
    final Period period = tenor.getPeriod();
    final double months = period.totalMonths();
    return months / 12.;
  }
}
