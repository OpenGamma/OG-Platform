/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.position.Trade;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
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
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.InterestRateFutureOptionSecurityConverter;
import com.opengamma.financial.analytics.conversion.InterestRateFutureOptionTradeConverter;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.YieldCurveNodeSensitivitiesHelper;
import com.opengamma.financial.analytics.model.curve.interestrate.InterpolatedYieldCurveFunction;
import com.opengamma.financial.analytics.volatility.fittedresults.SABRFittedSurfaces;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.instrument.InstrumentDefinition;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.InstrumentSensitivityCalculator;
import com.opengamma.financial.interestrate.PresentValueCurveSensitivitySABRCalculator;
import com.opengamma.financial.interestrate.PresentValueNodeSensitivityCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.financial.model.volatility.smile.function.VolatilityFunctionFactory;
import com.opengamma.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.surface.InterpolatedDoublesSurface;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class InterestRateFutureOptionInterpolatedYieldCurveNodeSensitivitiesFunction extends AbstractFunction.NonCompiledInvoker {
  @SuppressWarnings("unchecked")
  private static final VolatilityFunctionProvider<SABRFormulaData> SABR_FUNCTION = (VolatilityFunctionProvider<SABRFormulaData>) VolatilityFunctionFactory
  .getCalculator(VolatilityFunctionFactory.HAGAN);
  private static final PresentValueNodeSensitivityCalculator NSC = PresentValueNodeSensitivityCalculator.using(PresentValueCurveSensitivitySABRCalculator.getInstance());
  private static final InstrumentSensitivityCalculator CALCULATOR = InstrumentSensitivityCalculator.getInstance();
  private static final String VALUE_REQUIREMENT = ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES;
  private static final String CALCULATION_METHOD = InterpolatedYieldCurveFunction.CALCULATION_METHOD_NAME;
  private InterestRateFutureOptionTradeConverter _converter;
  private FixedIncomeConverterDataProvider _dataConverter;

  @Override
  public void init(final FunctionCompilationContext context) {
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    _converter = new InterestRateFutureOptionTradeConverter(new InterestRateFutureOptionSecurityConverter(holidaySource, conventionSource, regionSource, securitySource));
    _dataConverter = new FixedIncomeConverterDataProvider(conventionSource);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Trade trade = target.getTrade();
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final ValueProperties constraints = desiredValues.iterator().next().getConstraints();
    final String curveName = constraints.getValues(ValuePropertyNames.CURVE).iterator().next();
    final String surfaceName = constraints.getValues(ValuePropertyNames.SURFACE).iterator().next();
    final HistoricalTimeSeriesSource dataSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
    final ValueRequirement curveSpecRequirement = getCurveSpecRequirement(target, curveName);
    final Object curveSpecObject = inputs.getValue(curveSpecRequirement);
    if (curveSpecObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + curveSpecRequirement);
    }
    final InstrumentDefinition<?> definition = _converter.convert(trade);
    if (definition == null) {
      throw new OpenGammaRuntimeException("Definition for trade " + trade + " was null");
    }
    final FinancialSecurity security = (FinancialSecurity) trade.getSecurity();
    final InstrumentDerivative derivative = _dataConverter.convert(security, definition, now, new String[] {curveName, curveName}, dataSource);
    final InterpolatedYieldCurveSpecificationWithSecurities curveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) curveSpecObject;
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity());
    final SABRInterestRateDataBundle bundle = new SABRInterestRateDataBundle(getModelParameters(target, inputs, surfaceName),
        getYieldCurves(target, inputs, curveName));
    final DoubleMatrix1D sensitivities = CALCULATOR.calculateFromSimpleInterpolatedCurve(derivative, bundle, NSC);
    return YieldCurveNodeSensitivitiesHelper.getSensitivitiesForCurve(curveName, bundle, sensitivities, curveSpec,
        getResultSpec(target, currency, curveName, surfaceName));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.TRADE;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.TRADE) {
      return false;
    }
    return target.getTrade().getSecurity() instanceof IRFutureOptionSecurity;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final Currency ccy = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity());
    return Sets.newHashSet(getResultSpec(target, ccy));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<String> surfaceNames = desiredValue.getConstraints().getValues(ValuePropertyNames.SURFACE);
    if (surfaceNames == null || surfaceNames.size() != 1) {
      return null;
    }
    final String curveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
    if (curveName == null) {
      throw new OpenGammaRuntimeException("Must specify a curve against which to calculate the node sensitivities");
    }
    final String surfaceName = surfaceNames.iterator().next();
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    requirements.add(getSurfaceRequirement(target, surfaceName));
    requirements.add(getCurveRequirement(target, curveName));
    requirements.add(getCurveSpecRequirement(target, curveName));
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    String curveName = null;
    String surfaceName = null;
    for (final Map.Entry<ValueSpecification, ValueRequirement> input : inputs.entrySet()) {
      final ValueProperties properties = input.getKey().getProperties();
      if (ValueRequirementNames.YIELD_CURVE.equals(input.getKey().getValueName())) {
        curveName = properties.getValues(ValuePropertyNames.CURVE).iterator().next();
        if (surfaceName != null) {
          break;
        }
      }
      if (ValueRequirementNames.SABR_SURFACES.equals(input.getKey().getValueName())) {
        surfaceName = properties.getValues(ValuePropertyNames.SURFACE).iterator().next();
        if (curveName != null) {
          break;
        }
      }
    }
    assert curveName != null;
    assert surfaceName != null;
    final Currency ccy = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity());
    return Sets.newHashSet(getResultSpec(target, ccy, curveName, surfaceName));
  }

  private static ValueRequirement getCurveRequirement(final ComputationTarget target, final String curveName) {
    return YieldCurveFunction.getCurveRequirement(FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity()), curveName, null, null, CALCULATION_METHOD);
  }

  protected static ValueRequirement getSurfaceRequirement(final ComputationTarget target, final String surfaceName) {
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity());
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.CURRENCY, currency.getCode())
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.IR_FUTURE_OPTION).get();
    return new ValueRequirement(ValueRequirementNames.SABR_SURFACES, currency, properties);
  }

  protected static YieldCurveBundle getYieldCurves(final ComputationTarget target, final FunctionInputs inputs, final String curveName) {
    final ValueRequirement curveRequirement = getCurveRequirement(target, curveName);
    final Object curveObject = inputs.getValue(curveRequirement);
    if (curveObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + curveRequirement);
    }
    final YieldAndDiscountCurve curve = (YieldAndDiscountCurve) curveObject;
    return new YieldCurveBundle(new String[] {curveName}, new YieldAndDiscountCurve[] {curve});
  }

  private static SABRInterestRateParameters getModelParameters(final ComputationTarget target, final FunctionInputs inputs, final String surfaceName) {
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity());
    final ValueRequirement surfacesRequirement = getSurfaceRequirement(target, surfaceName);
    final Object surfacesObject = inputs.getValue(surfacesRequirement);
    if (surfacesObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + surfacesRequirement);
    }
    final SABRFittedSurfaces surfaces = (SABRFittedSurfaces) surfacesObject;
    if (!surfaces.getCurrency().equals(currency)) {
      throw new OpenGammaRuntimeException("Don't know how this happened");
    }
    final InterpolatedDoublesSurface alphaSurface = surfaces.getAlphaSurface();
    final InterpolatedDoublesSurface betaSurface = surfaces.getBetaSurface();
    final InterpolatedDoublesSurface nuSurface = surfaces.getNuSurface();
    final InterpolatedDoublesSurface rhoSurface = surfaces.getRhoSurface();
    final DayCount dayCount = surfaces.getDayCount();
    return new SABRInterestRateParameters(alphaSurface, betaSurface, rhoSurface, nuSurface, dayCount, SABR_FUNCTION);
  }

  private ValueSpecification getResultSpec(final ComputationTarget target, final Currency ccy) {
    final ValueProperties result = createValueProperties()
        .with(ValuePropertyNames.CURRENCY, ccy.getCode())
        .with(ValuePropertyNames.CURVE_CURRENCY, ccy.getCode())
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, CALCULATION_METHOD)
        .withAny(ValuePropertyNames.CURVE)
        .withAny(ValuePropertyNames.SURFACE)
        .with(ValuePropertyNames.SMILE_FITTING_METHOD, InterestRateFutureOptionFunction.SURFACE_FITTING_NAME).get();
    return new ValueSpecification(VALUE_REQUIREMENT, target.toSpecification(), result);
  }

  private ValueSpecification getResultSpec(final ComputationTarget target, final Currency ccy, final String curveName, final String surfaceName) {
    final ValueProperties result = createValueProperties()
        .with(ValuePropertyNames.CURRENCY, ccy.getCode())
        .with(ValuePropertyNames.CURVE_CURRENCY, ccy.getCode())
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, CALCULATION_METHOD)
        .with(ValuePropertyNames.CURVE, curveName)
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(ValuePropertyNames.SMILE_FITTING_METHOD, InterestRateFutureOptionFunction.SURFACE_FITTING_NAME).get();
    return new ValueSpecification(VALUE_REQUIREMENT, target.toSpecification(), result);
  }


  private static ValueRequirement getCurveSpecRequirement(final ComputationTarget target, final String curveName) {
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity());
    final ValueProperties.Builder properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, curveName);
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, ComputationTargetType.PRIMITIVE, currency.getUniqueId(), properties.get());
  }
}
