/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.BitSet;
import java.util.Set;

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
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.math.surface.InterpolatedDoublesSurface;
import com.opengamma.math.surface.NodalObjectsSurface;
import com.opengamma.math.surface.ObjectsSurface;
import com.opengamma.util.CompareUtils;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class SABRNonLinearLeastSquaresIRFutureSurfaceFittingFunction extends AbstractFunction.NonCompiledInvoker {
  private static final Double[] EMPTY_DOUBLE_ARRAY = new Double[0];
  private static final DoubleMatrix2D[] EMPTY_MATRIX_ARRAY = new DoubleMatrix2D[0];
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
  private ValueRequirement _futurePriceRequirement;

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
                                                             .with(IRFutureOptionVolatilitySurfaceAndFuturePriceDataFunction.PROPERTY_INSTRUMENT_TYPE, "IR_FUTURE_OPTION").get();
    final ValueProperties futurePriceProperties = ValueProperties.with(ValuePropertyNames.CURVE, _definitionName)
                                                                 .with(IRFutureOptionVolatilitySurfaceAndFuturePriceDataFunction.PROPERTY_INSTRUMENT_TYPE, "IR_FUTURE_PRICE").get();
    _surfaceRequirement = new ValueRequirement(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA, currencyTargetSpec, surfaceProperties);
    _futurePriceRequirement = new ValueRequirement(ValueRequirementNames.FUTURE_PRICE_CURVE_DATA, currencyTargetSpec, futurePriceProperties);
    final ValueProperties resultProperties = createValueProperties()
          .with(ValuePropertyNames.CURRENCY, _currency.getCode())
          .with(ValuePropertyNames.SURFACE, _definitionName)
          .with(RawVolatilitySurfaceDataFunction.PROPERTY_SURFACE_INSTRUMENT_TYPE, "IR_FUTURE_OPTION").get();
    _resultSpecification = new ValueSpecification(ValueRequirementNames.SABR_SURFACES, currencyTargetSpec, resultProperties);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Object objectSurfaceData = inputs.getValue(_surfaceRequirement);
    if (objectSurfaceData == null) {
      throw new OpenGammaRuntimeException("Could not get volatility surface data");
    }
    @SuppressWarnings("unchecked")
    final VolatilitySurfaceData<Double, Double> volatilitySurfaceData = (VolatilitySurfaceData<Double, Double>) objectSurfaceData;
    final Object objectFuturePriceData = inputs.getValue(_futurePriceRequirement);
    if (objectFuturePriceData == null) {
      throw new OpenGammaRuntimeException("Could not get futures price data");
    }
    @SuppressWarnings("unchecked")
    final FuturePriceCurveData<Double> futurePriceData = (FuturePriceCurveData<Double>) objectFuturePriceData;
    //assumes that the sorting is first x, then y
    final Double[] x = volatilitySurfaceData.getXs();
    final Double[] y = volatilitySurfaceData.getYs();
    DoubleArrayList strikeList = new DoubleArrayList();
    DoubleArrayList volList = new DoubleArrayList();
    final DoubleArrayList fittedOptionExpiryList = new DoubleArrayList();
    final DoubleArrayList futureDelayList = new DoubleArrayList();
    final DoubleArrayList alphaList = new DoubleArrayList();
    final DoubleArrayList betaList = new DoubleArrayList();
    final DoubleArrayList nuList = new DoubleArrayList();
    final DoubleArrayList rhoList = new DoubleArrayList();
    final DoubleArrayList chiSqList = new DoubleArrayList();
    final ObjectArrayList<DoubleMatrix2D> inverseJacobianList = new ObjectArrayList<DoubleMatrix2D>();
    if (x.length > 0 && x[0] != null) {
      double oldX = x[0];
      for (int i = 1; i < x.length; i++) {
        if (x[i] != null) {
          if (!CompareUtils.closeEquals(x[i], oldX)) {
            final double[] strikes = strikeList.toDoubleArray();
            final double[] blackVols = volList.toDoubleArray();
            final int n = strikes.length;
            if (blackVols.length != n) {
              throw new OpenGammaRuntimeException("Strike and Black volatility arrays were not the same length; should never happen");
            }
            final double[] errors = new double[n];
            final EuropeanVanillaOption[] options = new EuropeanVanillaOption[n];
            final BlackFunctionData[] data = new BlackFunctionData[n];
            final double forward = futurePriceData.getFuturePrice(x[i - 1]);
            for (int j = 0; j < n; j++) {
              options[j] = new EuropeanVanillaOption(strikes[j], oldX, true);
              data[j] = new BlackFunctionData(forward, 1, blackVols[j]);
              errors[j] = ERROR;
            }
            if (options.length > 4) {
              final LeastSquareResults fittedResult = FITTER.getFitResult(options, data, errors, SABR_INITIAL_VALUES, FIXED, 0, RECOVER_ATM_VOL);
              final DoubleMatrix1D parameters = fittedResult.getParameters();
              fittedOptionExpiryList.add(oldX);
              futureDelayList.add(0);
              alphaList.add(parameters.getEntry(0));
              betaList.add(parameters.getEntry(1));
              nuList.add(parameters.getEntry(2));
              rhoList.add(parameters.getEntry(3));
              inverseJacobianList.add(fittedResult.getInverseJacobian());
              chiSqList.add(fittedResult.getChiSq());
            }
            oldX = x[i];
            strikeList = new DoubleArrayList();
            volList = new DoubleArrayList();
            strikeList.add(y[i]);
            volList.add(volatilitySurfaceData.getVolatility(x[i], y[i]));
          } else {
            strikeList.add(y[i]);
            volList.add(volatilitySurfaceData.getVolatility(x[i], y[i]));
          }
        }
      }
    }
    if (fittedOptionExpiryList.size() < 5) { //don't have sufficient fits to construct a surface
      throw new OpenGammaRuntimeException("Could not construct SABR parameter surfaces; have under 5 surface points");
    }
    final double[] fittedOptionExpiry = fittedOptionExpiryList.toDoubleArray();
    final double[] futureDelay = futureDelayList.toDoubleArray();
    final double[] alpha = alphaList.toDoubleArray();
    final double[] beta = betaList.toDoubleArray();
    final double[] nu = nuList.toDoubleArray();
    final double[] rho = rhoList.toDoubleArray();
    final VolatilitySurface alphaSurface = new VolatilitySurface(InterpolatedDoublesSurface.from(fittedOptionExpiry, futureDelay, alpha, INTERPOLATOR, "SABR alpha surface"));
    final VolatilitySurface betaSurface = new VolatilitySurface(InterpolatedDoublesSurface.from(fittedOptionExpiry, futureDelay, beta, INTERPOLATOR, "SABR beta surface"));
    final VolatilitySurface nuSurface = new VolatilitySurface(InterpolatedDoublesSurface.from(fittedOptionExpiry, futureDelay, nu, INTERPOLATOR, "SABR nu surface"));
    final VolatilitySurface rhoSurface = new VolatilitySurface(InterpolatedDoublesSurface.from(fittedOptionExpiry, futureDelay, rho, INTERPOLATOR, "SABR rho surface"));
    final ObjectsSurface<Double, Double, DoubleMatrix2D> inverseJacobianSurface = NodalObjectsSurface.from(fittedOptionExpiryList.toArray(EMPTY_DOUBLE_ARRAY),
        futureDelayList.toArray(EMPTY_DOUBLE_ARRAY), inverseJacobianList.toArray(EMPTY_MATRIX_ARRAY));
    final SABRFittedSurfaces fittedSurfaces = new SABRFittedSurfaces(alphaSurface, betaSurface, nuSurface, rhoSurface, _currency, DAY_COUNT);
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
