/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

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
import com.opengamma.financial.analytics.ircurve.MarketInstrumentImpliedYieldCurveFunction;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.model.FunctionUtils;
import com.opengamma.financial.analytics.model.YieldCurveNodeSensitivitiesHelper;
import com.opengamma.financial.analytics.volatility.sabr.SABRFittedSurfaces;
import com.opengamma.financial.analytics.volatility.surface.RawVolatilitySurfaceDataFunction;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.instrument.FixedIncomeInstrumentDefinition;
import com.opengamma.financial.interestrate.InstrumentSensitivityCalculator;
import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.PresentValueCurveSensitivitySABRCalculator;
import com.opengamma.financial.interestrate.PresentValueNodeSensitivityCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.financial.model.volatility.smile.function.VolatilityFunctionFactory;
import com.opengamma.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class InterestRateFutureOptionYieldCurveNodeSensitivitiesFunction extends AbstractFunction.NonCompiledInvoker {
  @SuppressWarnings("unchecked")
  private static final VolatilityFunctionProvider<SABRFormulaData> SABR_FUNCTION = (VolatilityFunctionProvider<SABRFormulaData>) VolatilityFunctionFactory
    .getCalculator(VolatilityFunctionFactory.HAGAN);
  private static final PresentValueNodeSensitivityCalculator NSC = PresentValueNodeSensitivityCalculator.using(PresentValueCurveSensitivitySABRCalculator.getInstance());
  private static final InstrumentSensitivityCalculator CALCULATOR = InstrumentSensitivityCalculator.getInstance();
  private static final String VALUE_REQUIREMENT = ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES;
  private InterestRateFutureOptionTradeConverter _converter;
  private FixedIncomeConverterDataProvider _dataConverter;
  private final String _forwardCurveName;
  private final String _fundingCurveName;
  private final String _surfaceName;

  public InterestRateFutureOptionYieldCurveNodeSensitivitiesFunction(final String forwardCurveName, final String fundingCurveName, final String surfaceName) {
    Validate.notNull(forwardCurveName, "forward curve name");
    Validate.notNull(fundingCurveName, "funding curve name");
    Validate.notNull(surfaceName, "surface name");
    _forwardCurveName = forwardCurveName;    
    _fundingCurveName = fundingCurveName;
    _surfaceName = surfaceName;
  }

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
    final HistoricalTimeSeriesSource dataSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
    final ValueRequirement forwardCurveSpecRequirement = getCurveSpecRequirement(target, _forwardCurveName);
    final Object forwardCurveSpecObject = inputs.getValue(forwardCurveSpecRequirement);
    if (forwardCurveSpecObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + forwardCurveSpecRequirement);
    }
    Object fundingCurveSpecObject = null;
    if (!_forwardCurveName.equals(_fundingCurveName)) {
      final ValueRequirement fundingCurveSpecRequirement = getCurveSpecRequirement(target, _fundingCurveName);
      fundingCurveSpecObject = inputs.getValue(fundingCurveSpecRequirement);
      if (fundingCurveSpecObject == null) {
        throw new OpenGammaRuntimeException("Could not get " + fundingCurveSpecRequirement);
      }
    }
    final Object jacobianObject = inputs.getValue(ValueRequirementNames.YIELD_CURVE_JACOBIAN);
    if (jacobianObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + ValueRequirementNames.YIELD_CURVE_JACOBIAN);
    }
    final InterpolatedYieldCurveSpecificationWithSecurities forwardCurveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) forwardCurveSpecObject;
    final InterpolatedYieldCurveSpecificationWithSecurities fundingCurveSpec = fundingCurveSpecObject == null ? forwardCurveSpec
        : (InterpolatedYieldCurveSpecificationWithSecurities) fundingCurveSpecObject;
    final FixedIncomeInstrumentDefinition<?> definition = _converter.convert(trade);
    if (definition == null) {
      throw new OpenGammaRuntimeException("Definition for trade " + trade + " was null");
    }
    FinancialSecurity security = (FinancialSecurity) trade.getSecurity();
    final InterestRateDerivative derivative = _dataConverter.convert(security, definition, now, new String[] {_fundingCurveName, _forwardCurveName}, dataSource);
    final double[][] array = FunctionUtils.decodeJacobian(jacobianObject);
    final DoubleMatrix2D jacobian = new DoubleMatrix2D(array);
    DoubleMatrix1D sensitivitiesForCurves;
    final Object couponSensitivityObject = inputs.getValue(getCouponSensitivityRequirement(target));
    if (couponSensitivityObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + ValueRequirementNames.PRESENT_VALUE_COUPON_SENSITIVITY);
    }
    final SABRInterestRateDataBundle bundle = new SABRInterestRateDataBundle(getModelParameters(target, inputs), getYieldCurves(target, inputs));
    final DoubleMatrix1D couponSensitivity = (DoubleMatrix1D) couponSensitivityObject;
    sensitivitiesForCurves = CALCULATOR.calculateFromPresentValue(derivative, null, bundle, couponSensitivity, jacobian, NSC);
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity());
    if (_fundingCurveName.equals(_forwardCurveName)) {
      return YieldCurveNodeSensitivitiesHelper.getSensitivitiesForCurve(_forwardCurveName, bundle, sensitivitiesForCurves, forwardCurveSpec, getForwardResultSpec(target, currency));
    }
    final Map<String, InterpolatedYieldCurveSpecificationWithSecurities> curveSpecs = new HashMap<String, InterpolatedYieldCurveSpecificationWithSecurities>();
    curveSpecs.put(_forwardCurveName, forwardCurveSpec);
    curveSpecs.put(_fundingCurveName, fundingCurveSpec);
    return YieldCurveNodeSensitivitiesHelper.getSensitivitiesForMultipleCurves(_forwardCurveName, _fundingCurveName, getForwardResultSpec(target, currency), 
        getFundingResultSpec(target, currency), bundle, sensitivitiesForCurves, curveSpecs);
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    requirements.add(getSurfaceRequirement(target));
    if (_forwardCurveName.equals(_fundingCurveName)) {
      requirements.add(getCurveRequirement(target, _forwardCurveName, null, null));
      requirements.add(getCouponSensitivityRequirement(target));
      requirements.add(getCurveSpecRequirement(target, _forwardCurveName));
      requirements.add(getJacobianRequirement(target));
      return requirements;
    }
    requirements.add(getCurveRequirement(target, _forwardCurveName, _forwardCurveName, _fundingCurveName));
    requirements.add(getCurveRequirement(target, _fundingCurveName, _forwardCurveName, _fundingCurveName));
    requirements.add(getSurfaceRequirement(target));
    requirements.add(getCouponSensitivityRequirement(target));
    requirements.add(getCurveSpecRequirement(target, _forwardCurveName));
    requirements.add(getCurveSpecRequirement(target, _fundingCurveName));
    requirements.add(getJacobianRequirement(target));
    return requirements;
  }

  private ValueRequirement getCurveRequirement(final ComputationTarget target, final String curveName,
      final String advisoryForward, final String advisoryFunding) {
    return YieldCurveFunction.getCurveRequirement(FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity()), curveName,
        advisoryForward, advisoryFunding);
  }

  protected ValueRequirement getSurfaceRequirement(final ComputationTarget target) {
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity());
    final ValueProperties properties = ValueProperties.with(ValuePropertyNames.CURRENCY, currency.getCode())
                                                      .with(ValuePropertyNames.SURFACE, _surfaceName)
                                                      .with(RawVolatilitySurfaceDataFunction.PROPERTY_SURFACE_INSTRUMENT_TYPE, "IR_FUTURE_OPTION").get();
    return new ValueRequirement(ValueRequirementNames.SABR_SURFACES, currency, properties);
  }

  protected YieldCurveBundle getYieldCurves(final ComputationTarget target, final FunctionInputs inputs) {
    final ValueRequirement forwardCurveRequirement = getCurveRequirement(target, _forwardCurveName, null, null);
    final Object forwardCurveObject = inputs.getValue(forwardCurveRequirement);
    if (forwardCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + forwardCurveRequirement);
    }
    Object fundingCurveObject = null;
    if (!_forwardCurveName.equals(_fundingCurveName)) {
      final ValueRequirement fundingCurveRequirement = getCurveRequirement(target, _fundingCurveName, null, null);
      fundingCurveObject = inputs.getValue(fundingCurveRequirement);
      if (fundingCurveObject == null) {
        throw new OpenGammaRuntimeException("Could not get " + fundingCurveRequirement);
      }
    }
    final YieldAndDiscountCurve forwardCurve = (YieldAndDiscountCurve) forwardCurveObject;
    final YieldAndDiscountCurve fundingCurve = fundingCurveObject == null ? forwardCurve
        : (YieldAndDiscountCurve) fundingCurveObject;
    return new YieldCurveBundle(new String[] {_fundingCurveName, _forwardCurveName},
        new YieldAndDiscountCurve[] {fundingCurve, forwardCurve});
  }

  private SABRInterestRateParameters getModelParameters(final ComputationTarget target, final FunctionInputs inputs) {
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity());
    final ValueRequirement surfacesRequirement = getSurfaceRequirement(target);
    final Object surfacesObject = inputs.getValue(surfacesRequirement);
    if (surfacesObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + surfacesRequirement);
    }
    final SABRFittedSurfaces surfaces = (SABRFittedSurfaces) surfacesObject;
    if (!surfaces.getCurrency().equals(currency)) {
      throw new OpenGammaRuntimeException("Don't know how this happened");
    }
    final VolatilitySurface alphaSurface = surfaces.getAlphaSurface();
    final VolatilitySurface betaSurface = surfaces.getBetaSurface();
    final VolatilitySurface nuSurface = surfaces.getNuSurface();
    final VolatilitySurface rhoSurface = surfaces.getRhoSurface();
    final DayCount dayCount = surfaces.getDayCount();
    return new SABRInterestRateParameters(alphaSurface, betaSurface, rhoSurface, nuSurface, dayCount, SABR_FUNCTION);
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
    return Sets.newHashSet(getFundingResultSpec(target, ccy), getForwardResultSpec(target, ccy));
  }

  private ValueSpecification getForwardResultSpec(final ComputationTarget target, final Currency ccy) {
    ValueProperties result = createValueProperties()
        .with(ValuePropertyNames.CURRENCY, ccy.getCode())
        .with(ValuePropertyNames.CURVE_CURRENCY, ccy.getCode())
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING)
        .with(ValuePropertyNames.CURVE, _forwardCurveName).get();
    return new ValueSpecification(VALUE_REQUIREMENT, target.toSpecification(), result);
  }

  private ValueSpecification getFundingResultSpec(final ComputationTarget target, final Currency ccy) {
    ValueProperties result = createValueProperties()
        .with(ValuePropertyNames.CURRENCY, ccy.getCode())
        .with(ValuePropertyNames.CURVE_CURRENCY, ccy.getCode())
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING)
        .with(ValuePropertyNames.CURVE, _fundingCurveName).get();
    return new ValueSpecification(VALUE_REQUIREMENT, target.toSpecification(), result);
  }

  protected ValueRequirement getCurveRequirement(final ComputationTarget target, final String curveName) {
    return YieldCurveFunction.getCurveRequirement(FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity()), curveName, _forwardCurveName, _fundingCurveName, 
        MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING);
  }

  protected ValueRequirement getJacobianRequirement(final ComputationTarget target) {
    return YieldCurveFunction.getJacobianRequirement(FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity()), _forwardCurveName, _fundingCurveName, 
        MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING);
  }

  protected ValueRequirement getCouponSensitivityRequirement(final ComputationTarget target) {
    return YieldCurveFunction.getCouponSensitivityRequirement(FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity()), _forwardCurveName, _fundingCurveName);
  }

  protected ValueRequirement getCurveSpecRequirement(final ComputationTarget target, final String curveName) {
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity());
    final ValueProperties.Builder properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, curveName);
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, ComputationTargetType.PRIMITIVE, currency.getUniqueId(), properties.get());
  }
}
