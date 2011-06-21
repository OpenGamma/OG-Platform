/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import java.util.BitSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
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
import com.opengamma.financial.analytics.volatility.sabr.SABRFittedSurfaces;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.volatility.smile.fitting.SABRNonLinearLeastSquareFitter;
import com.opengamma.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.math.interpolation.GridInterpolator2D;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.math.surface.InterpolatedDoublesSurface;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class SABRNonLinearLeastSquaresFittingFunction extends AbstractFunction.NonCompiledInvoker {
  private static final double ERROR = 0.001;
  private static final SABRHaganVolatilityFunction SABR_FUNCTION = new SABRHaganVolatilityFunction();
  private static final SABRNonLinearLeastSquareFitter FITTER = new SABRNonLinearLeastSquareFitter(SABR_FUNCTION);
  private static final double[] SABR_INITIAL_VALUES = new double[] {0.05, 0.2, 0.2, 0.0};
  private static final BitSet FIXED = new BitSet();
  private static final boolean RECOVER_ATM_VOL = true;
  private static final LinearInterpolator1D LINEAR = (LinearInterpolator1D) Interpolator1DFactory.getInterpolator(Interpolator1DFactory.LINEAR);
  private static final GridInterpolator2D INTERPOLATOR = new GridInterpolator2D(LINEAR, LINEAR);
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA");
  private final VolatilityCubeFunctionHelper _volCubeHelper;
  private ValueSpecification _resultSpecification;
  private ValueRequirement _cubeRequirement;

  //TODO forward data helper? or in the cube?

  public SABRNonLinearLeastSquaresFittingFunction(final String currency, final String definitionName) {
    this(Currency.of(currency), definitionName);
  }

  public SABRNonLinearLeastSquaresFittingFunction(final Currency currency, final String definitionName) {
    _volCubeHelper = new VolatilityCubeFunctionHelper(currency, definitionName);
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    final ComputationTargetSpecification currencyTargetSpec = new ComputationTargetSpecification(_volCubeHelper.getKey().getCurrency());
    final ValueProperties cubeProperties = ValueProperties.with(ValuePropertyNames.CUBE, _volCubeHelper.getKey().getName()).get();
    _cubeRequirement = new ValueRequirement(ValueRequirementNames.STANDARD_VOLATILITY_CUBE_DATA, currencyTargetSpec, cubeProperties);
    final ValueProperties resultProperties = createValueProperties()
        .with(ValuePropertyNames.CURRENCY, _volCubeHelper.getKey().getCurrency().getCode())
        .with(ValuePropertyNames.CUBE, _volCubeHelper.getKey().getName()).get();
    _resultSpecification = new ValueSpecification(ValueRequirementNames.SABR_SURFACES, currencyTargetSpec, resultProperties);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Object objectCubeData = inputs.getValue(_cubeRequirement);
    if (objectCubeData == null) {
      throw new OpenGammaRuntimeException("Could not get volatility cube data");
    }
    @SuppressWarnings("unchecked")
    final Map<Tenor, Map<Tenor, Pair<double[], double[]>>> smiles = (Map<Tenor, Map<Tenor, Pair<double[], double[]>>>) objectCubeData;
    final int totalDataPoints = smiles.size();
    final double[] swapMaturities = new double[totalDataPoints];
    final double[] swaptionExpiries = new double[totalDataPoints];
    final double[] alpha = new double[totalDataPoints];
    final double[] beta = new double[totalDataPoints];
    final double[] nu = new double[totalDataPoints];
    final double[] rho = new double[totalDataPoints];
    //TODO convert relative strikes into absolute   
    int count = 0;
    for (final Map.Entry<Tenor, Map<Tenor, Pair<double[], double[]>>> swapMaturityEntry : smiles.entrySet()) {
      final double maturity = swapMaturityEntry.getKey().getPeriod().getYears();
      for (final Map.Entry<Tenor, Pair<double[], double[]>> swaptionMaturityEntry : swapMaturityEntry.getValue().entrySet()) {
        final double swaptionExpiry = swaptionMaturityEntry.getKey().getPeriod().getYears();
        final double[] strikes = swaptionMaturityEntry.getValue().getFirst();
        final double[] blackVols = swaptionMaturityEntry.getValue().getSecond();
        final int n = strikes.length;
        if (n != blackVols.length) {
          throw new OpenGammaRuntimeException("Strike and black volatility arrays were not the same length; should never happen");
        }
        final EuropeanVanillaOption[] options = new EuropeanVanillaOption[n];
        final BlackFunctionData[] data = new BlackFunctionData[n];
        final double[] errors = new double[n];
        final double forward = 0; //TODO
        for (int k = 0; k < n; k++) {
          options[k] = new EuropeanVanillaOption(strikes[k], swaptionExpiry, true);
          data[k] = new BlackFunctionData(forward, 1, blackVols[k] / 100);
          errors[k] = ERROR;
        }
        final double atmVol = .20;
        if (options.length > 4) { //don't fit those smiles with insufficient data 
          final LeastSquareResults fittedResult = FITTER.getFitResult(options, data, errors, SABR_INITIAL_VALUES, FIXED, atmVol, RECOVER_ATM_VOL);
          final DoubleMatrix1D parameters = fittedResult.getParameters();
          swapMaturities[count] = maturity;
          swaptionExpiries[count] = swaptionExpiry;
          alpha[count] = parameters.getEntry(0);
          beta[count] = parameters.getEntry(1);
          nu[count] = parameters.getEntry(2);
          rho[count++] = parameters.getEntry(3);
        }
      }
    }
    final VolatilitySurface alphaSurface = new VolatilitySurface(InterpolatedDoublesSurface.from(swapMaturities, swaptionExpiries, alpha, INTERPOLATOR, "SABR alpha surface"));
    final VolatilitySurface betaSurface = new VolatilitySurface(InterpolatedDoublesSurface.from(swapMaturities, swaptionExpiries, beta, INTERPOLATOR, "SABR beta surface"));
    final VolatilitySurface nuSurface = new VolatilitySurface(InterpolatedDoublesSurface.from(swapMaturities, swaptionExpiries, nu, INTERPOLATOR, "SABR nu surface"));
    final VolatilitySurface rhoSurface = new VolatilitySurface(InterpolatedDoublesSurface.from(swapMaturities, swaptionExpiries, rho, INTERPOLATOR, "SABR rho surface"));
    final SABRFittedSurfaces fittedSurfaces = new SABRFittedSurfaces(alphaSurface, betaSurface, nuSurface, rhoSurface, _volCubeHelper.getKey().getCurrency(), DAY_COUNT);
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
    return ObjectUtils.equals(target.getUniqueId(), _volCubeHelper.getKey().getCurrency().getUniqueId());
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    return Sets.newHashSet(_cubeRequirement);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Sets.newHashSet(_resultSpecification);
  }
}
