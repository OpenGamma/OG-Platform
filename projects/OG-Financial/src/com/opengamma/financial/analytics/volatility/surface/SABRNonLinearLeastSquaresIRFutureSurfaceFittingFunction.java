/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import java.util.BitSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.marketdatasnapshot.VolatilityCubeData;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceData;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
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
import com.opengamma.financial.analytics.volatility.sabr.SABRFittedSurfaces;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.volatility.smile.fitting.SABRNonLinearLeastSquareFitter;
import com.opengamma.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.math.interpolation.FlatExtrapolator1D;
import com.opengamma.math.interpolation.GridInterpolator2D;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.math.surface.InterpolatedDoublesSurface;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class SABRNonLinearLeastSquaresIRFutureSurfaceFittingFunction extends AbstractFunction.NonCompiledInvoker {
  private static final double ERROR = 0.001;
  private static final SABRHaganVolatilityFunction SABR_FUNCTION = new SABRHaganVolatilityFunction();
  private static final SABRNonLinearLeastSquareFitter FITTER = new SABRNonLinearLeastSquareFitter(SABR_FUNCTION);
  private static final double[] SABR_INITIAL_VALUES = new double[] {0.05, 0.5, 0.2, 0.0};
  private static final BitSet FIXED = new BitSet();
  private static final boolean RECOVER_ATM_VOL = false;
  private static final LinearInterpolator1D LINEAR = (LinearInterpolator1D) Interpolator1DFactory.getInterpolator(Interpolator1DFactory.LINEAR);
  private static final FlatExtrapolator1D<Interpolator1DDataBundle> FLAT = new FlatExtrapolator1D<Interpolator1DDataBundle>();
  private static final GridInterpolator2D<Interpolator1DDataBundle, Interpolator1DDataBundle> INTERPOLATOR = new GridInterpolator2D<Interpolator1DDataBundle, Interpolator1DDataBundle>(LINEAR, LINEAR,
        FLAT, FLAT);
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA");
  private ValueSpecification _resultSpecification;
  private Currency _currency;
  private String _definitionName;
  private ValueRequirement _surfaceRequirement;

  static {
    FIXED.set(1);
  }

  public SABRNonLinearLeastSquaresIRFutureSurfaceFittingFunction(final String currency, final String definitionName) {
    this(Currency.of(currency), definitionName);
  }

  public SABRNonLinearLeastSquaresIRFutureSurfaceFittingFunction(final Currency currency, final String definitionName) {
    _currency = currency;
    _definitionName = definitionName;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    final ComputationTargetSpecification currencyTargetSpec = new ComputationTargetSpecification(_currency);
    final ValueProperties surfaceProperties = ValueProperties.with(ValuePropertyNames.SURFACE, _definitionName)
                                                               .with(RawVolatilitySurfaceDataFunction.PROPERTY_SURFACE_INSTRUMENT_TYPE, "IR_FUTURE").get();
    _surfaceRequirement = new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE_DATA, currencyTargetSpec, surfaceProperties);
    final ValueProperties resultProperties = createValueProperties()
          .with(ValuePropertyNames.CURRENCY, _currency.getCode())
          .with(ValuePropertyNames.SURFACE, _definitionName)
          .with(RawVolatilitySurfaceDataFunction.PROPERTY_SURFACE_INSTRUMENT_TYPE, "IR_FUTURE").get();
    _resultSpecification = new ValueSpecification(ValueRequirementNames.SABR_SURFACES, currencyTargetSpec, resultProperties);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Object objectSurfaceData = inputs.getValue(_surfaceRequirement);
    if (objectSurfaceData == null) {
      throw new OpenGammaRuntimeException("Could not get volatility cube data");
    }
    final VolatilitySurfaceData<?, ?> volatilitySurfaceData = (VolatilityCubeData) objectSurfaceData;
    final Map<Tenor, Map<Tenor, Pair<double[], double[]>>> smiles = volatilitySurfaceData.getSmiles();
    final DoubleArrayList swapMaturitiesList = new DoubleArrayList();
    final DoubleArrayList swaptionExpiriesList = new DoubleArrayList();
    final DoubleArrayList alphaList = new DoubleArrayList();
    final DoubleArrayList betaList = new DoubleArrayList();
    final DoubleArrayList nuList = new DoubleArrayList();
    final DoubleArrayList rhoList = new DoubleArrayList();
    final DoubleArrayList chiSqList = new DoubleArrayList();
    for (final Map.Entry<Tenor, Map<Tenor, Pair<double[], double[]>>> swapMaturityEntry : smiles.entrySet()) {
      final double maturity = getTime(swapMaturityEntry.getKey());
      for (final Map.Entry<Tenor, Pair<double[], double[]>> swaptionExpiryEntry : swapMaturityEntry.getValue().entrySet()) {
        final double swaptionExpiry = getTime(swaptionExpiryEntry.getKey());
        final double[] strikes = swaptionExpiryEntry.getValue().getFirst();
        final double[] blackVols = swaptionExpiryEntry.getValue().getSecond();
        final int n = strikes.length;
        if (n != blackVols.length) {
          throw new OpenGammaRuntimeException("Strike and black volatility arrays were not the same length; should never happen");
        }
        final EuropeanVanillaOption[] options = new EuropeanVanillaOption[n];
        final BlackFunctionData[] data = new BlackFunctionData[n];
        final double[] errors = new double[n];
        final Pair<Tenor, Tenor> tenorPair = Pair.of(swapMaturityEntry.getKey(), swaptionExpiryEntry.getKey());
        if (volatilitySurfaceData.getStrikes() != null && volatilitySurfaceData.getStrikes().containsKey(tenorPair)) {
          final double forward = volatilitySurfaceData.getStrikes().get(tenorPair);
          final double atmVol = volatilitySurfaceData.getATMVolatilities().get(tenorPair);
          for (int k = 0; k < n; k++) {
            options[k] = new EuropeanVanillaOption(strikes[k], swaptionExpiry, true);
            data[k] = new BlackFunctionData(forward, 1, blackVols[k]);
            errors[k] = ERROR;
          }
          if (options.length > 4 && forward > 0) { //don't fit those smiles with insufficient data 
            final LeastSquareResults fittedResult = FITTER.getFitResult(options, data, errors, SABR_INITIAL_VALUES, FIXED, atmVol, RECOVER_ATM_VOL);
            final DoubleMatrix1D parameters = fittedResult.getParameters();
            swapMaturitiesList.add(maturity);
            swaptionExpiriesList.add(swaptionExpiry);
            alphaList.add(parameters.getEntry(0));
            betaList.add(parameters.getEntry(1));
            nuList.add(parameters.getEntry(2));
            rhoList.add(parameters.getEntry(3));
            chiSqList.add(fittedResult.getChiSq());
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
    final VolatilitySurface alphaSurface = new VolatilitySurface(InterpolatedDoublesSurface.from(swapMaturities, swaptionExpiries, alpha, INTERPOLATOR, "SABR alpha surface"));
    final VolatilitySurface betaSurface = new VolatilitySurface(InterpolatedDoublesSurface.from(swapMaturities, swaptionExpiries, beta, INTERPOLATOR, "SABR beta surface"));
    final VolatilitySurface nuSurface = new VolatilitySurface(InterpolatedDoublesSurface.from(swapMaturities, swaptionExpiries, nu, INTERPOLATOR, "SABR nu surface"));
    final VolatilitySurface rhoSurface = new VolatilitySurface(InterpolatedDoublesSurface.from(swapMaturities, swaptionExpiries, rho, INTERPOLATOR, "SABR rho surface"));
    final SABRFittedSurfaces fittedSurfaces = new SABRFittedSurfaces(alphaSurface, betaSurface, nuSurface, rhoSurface, _volCubeHelper.getCurrency(), DAY_COUNT);
    return Sets.newHashSet(new ComputedValue(_resultSpecification, fittedSurfaces));
  }
}
