/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.horizon.deprecated;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.Clock;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.horizon.ConstantSpreadHorizonThetaCalculator;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginTransactionDefinition;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
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
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.conversion.InterestRateFutureOptionSecurityConverter;
import com.opengamma.financial.analytics.conversion.InterestRateFutureOptionTradeConverter;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.horizon.InterestRateFutureOptionConstantSpreadThetaFunction;
import com.opengamma.financial.analytics.model.irfutureoption.InterestRateFutureOptionBlackFunctionDeprecated;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;

/**
 * @deprecated Use the version of the function that does not refer to funding or forward curves
 * @see InterestRateFutureOptionConstantSpreadThetaFunction
 */
@Deprecated
public class InterestRateFutureOptionConstantSpreadThetaFunctionDeprecated extends AbstractFunction.NonCompiledInvoker {

  private static final int DAYS_TO_MOVE_FORWARD = 1; // TODO Add to Value Properties

  private InterestRateFutureOptionTradeConverter _converter;

  @Override
  public void init(final FunctionCompilationContext context) {
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    _converter = new InterestRateFutureOptionTradeConverter(new InterestRateFutureOptionSecurityConverter(holidaySource, conventionSource, regionSource, securitySource));
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
    final HistoricalTimeSeriesSource dataSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
    final SimpleTrade trade = (SimpleTrade) target.getTrade();
    final IRFutureOptionSecurity security = (IRFutureOptionSecurity) trade.getSecurity();
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final Currency currency = FinancialSecurityUtils.getCurrency(security);
    final String forwardCurveName = desiredValue.getConstraint(YieldCurveFunction.PROPERTY_FORWARD_CURVE);
    final String fundingCurveName = desiredValue.getConstraint(YieldCurveFunction.PROPERTY_FUNDING_CURVE);
    final String curveCalculationMethod = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_METHOD);
    final String surfaceName = desiredValue.getConstraint(ValuePropertyNames.SURFACE);
    final String surfaceNameWithPrefix = surfaceName + "_" + InterestRateFutureOptionBlackFunctionDeprecated.getFutureOptionPrefix(target);

    final Object forwardCurveObject = inputs.getValue(YieldCurveFunction.getCurveRequirement(currency, forwardCurveName, forwardCurveName, fundingCurveName, curveCalculationMethod));
    if (forwardCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get forward curve");
    }
    final Object fundingCurveObject = inputs.getValue(YieldCurveFunction.getCurveRequirement(currency, fundingCurveName, forwardCurveName, fundingCurveName, curveCalculationMethod));
    if (fundingCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get funding curve");
    }
    final Object volatilitySurfaceObject = inputs.getValue(getVolatilityRequirement(surfaceNameWithPrefix, currency));
    if (volatilitySurfaceObject == null) {
      throw new OpenGammaRuntimeException("Could not get volatility surface");
    }
    final VolatilitySurface volatilitySurface = (VolatilitySurface) volatilitySurfaceObject;
    if (!(volatilitySurface.getSurface() instanceof InterpolatedDoublesSurface)) {
      throw new OpenGammaRuntimeException("Expecting an InterpolatedDoublesSurface; got " + volatilitySurface.getSurface().getClass());
    }
    final InstrumentDefinition<?> irFutureOptionDefinition = _converter.convert(trade);
    final String[] yieldCurveNames = new String[] {fundingCurveName, forwardCurveName };
    final ExternalIdBundle id = security.getExternalIdBundle();
    final LocalDate startDate = DateUtils.previousWeekDay(now.toLocalDate().minusMonths(1));
    final HistoricalTimeSeries ts = dataSource.getHistoricalTimeSeries(MarketDataRequirementNames.MARKET_VALUE, id, null, null, startDate, true, now.toLocalDate(), false);
    if (ts == null) {
      throw new OpenGammaRuntimeException("Could not get price time series for " + security);
    }
    final int length = ts.getTimeSeries().size();
    if (length == 0) {
      throw new OpenGammaRuntimeException("Price time series for " + security.getUnderlyingId() + " was empty");
    }
    final double lastMarginPrice = ts.getTimeSeries().getLatestValue();
    final YieldCurveBundle curves = getYieldCurves(target, inputs, forwardCurveName, fundingCurveName, curveCalculationMethod);
    final YieldCurveWithBlackCubeBundle data = new YieldCurveWithBlackCubeBundle(volatilitySurface.getSurface(), curves);

    final ConstantSpreadHorizonThetaCalculator calculator = ConstantSpreadHorizonThetaCalculator.getInstance();
    final MultipleCurrencyAmount theta = calculator.getTheta((InterestRateFutureOptionMarginTransactionDefinition) irFutureOptionDefinition, now, yieldCurveNames, data, lastMarginPrice,
        DAYS_TO_MOVE_FORWARD);
    return Collections.singleton(new ComputedValue(getResultSpec(target, forwardCurveName, fundingCurveName, curveCalculationMethod, surfaceName, currency.getCode()), theta));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.TRADE;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getTrade().getSecurity() instanceof IRFutureOptionSecurity;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final String currency = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity()).getCode();
    final ValueProperties.Builder properties = getResultProperties(currency);
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.VALUE_THETA, target.toSpecification(), properties.get()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<String> forwardCurves = desiredValue.getConstraints().getValues(YieldCurveFunction.PROPERTY_FORWARD_CURVE);
    if (forwardCurves == null || forwardCurves.size() != 1) {
      return null;
    }
    final Set<String> fundingCurves = desiredValue.getConstraints().getValues(YieldCurveFunction.PROPERTY_FUNDING_CURVE);
    if (fundingCurves == null || fundingCurves.size() != 1) {
      return null;
    }
    final Set<String> curveCalculationMethodNames = desiredValue.getConstraints().getValues(ValuePropertyNames.CURVE_CALCULATION_METHOD);
    if (curveCalculationMethodNames == null || curveCalculationMethodNames.size() != 1) {
      return null;
    }
    final Set<String> surfaceNames = desiredValue.getConstraints().getValues(ValuePropertyNames.SURFACE);
    if (surfaceNames == null || surfaceNames.size() != 1) {
      return null;
    }
    final String forwardCurveName = forwardCurves.iterator().next();
    final String fundingCurveName = fundingCurves.iterator().next();
    final String curveCalculationMethod = curveCalculationMethodNames.iterator().next();
    final String surfaceName = surfaceNames.iterator().next() + "_" + InterestRateFutureOptionBlackFunctionDeprecated.getFutureOptionPrefix(target);
    final Set<ValueRequirement> requirements = Sets.newHashSetWithExpectedSize(4);
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity());
    requirements.add(YieldCurveFunction.getCurveRequirement(currency, forwardCurveName, forwardCurveName, fundingCurveName, curveCalculationMethod));
    requirements.add(YieldCurveFunction.getCurveRequirement(currency, fundingCurveName, forwardCurveName, fundingCurveName, curveCalculationMethod));
    requirements.add(getVolatilityRequirement(surfaceName, currency));
    return requirements;

  }

  private ValueProperties.Builder getResultProperties(final String currency) {
    final ValueProperties.Builder properties = createValueProperties()
        .withAny(YieldCurveFunction.PROPERTY_FORWARD_CURVE)
        .withAny(YieldCurveFunction.PROPERTY_FUNDING_CURVE)
        .withAny(ValuePropertyNames.CURVE_CALCULATION_METHOD)
        .with(ValuePropertyNames.CURRENCY, currency)
        .withAny(ValuePropertyNames.SURFACE)
        .with(InterestRateFutureConstantSpreadThetaFunctionDeprecated.PROPERTY_THETA_CALCULATION_METHOD, InterestRateFutureConstantSpreadThetaFunctionDeprecated.THETA_CONSTANT_SPREAD);
    return properties;
  }

  private ValueProperties.Builder getResultProperties(final String currency, final String forwardCurveName, final String fundingCurveName, final String curveCalculationMethod,
      final String surfaceName) {
    final ValueProperties.Builder properties = createValueProperties()
        .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, forwardCurveName)
        .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, curveCalculationMethod)
        .with(ValuePropertyNames.CURRENCY, currency)
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(InterestRateFutureConstantSpreadThetaFunctionDeprecated.PROPERTY_THETA_CALCULATION_METHOD, InterestRateFutureConstantSpreadThetaFunctionDeprecated.THETA_CONSTANT_SPREAD);
    return properties;
  }

  private YieldCurveBundle getYieldCurves(final ComputationTarget target, final FunctionInputs inputs, final String forwardCurveName, final String fundingCurveName,
      final String curveCalculationMethod) {
    final ValueRequirement forwardCurveRequirement = getCurveRequirement(target, forwardCurveName, null, null, curveCalculationMethod);
    final Object forwardCurveObject = inputs.getValue(forwardCurveRequirement);
    if (forwardCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + forwardCurveRequirement);
    }
    Object fundingCurveObject = null;
    if (!forwardCurveName.equals(fundingCurveName)) {
      final ValueRequirement fundingCurveRequirement = getCurveRequirement(target, fundingCurveName, null, null, curveCalculationMethod);
      fundingCurveObject = inputs.getValue(fundingCurveRequirement);
      if (fundingCurveObject == null) {
        throw new OpenGammaRuntimeException("Could not get " + fundingCurveRequirement);
      }
    }
    final YieldAndDiscountCurve forwardCurve = (YieldAndDiscountCurve) forwardCurveObject;
    final YieldAndDiscountCurve fundingCurve = fundingCurveObject == null ? forwardCurve : (YieldAndDiscountCurve) fundingCurveObject;
    return new YieldCurveBundle(new String[] {fundingCurveName, forwardCurveName }, new YieldAndDiscountCurve[] {fundingCurve, forwardCurve });
  }

  private ValueRequirement getCurveRequirement(final ComputationTarget target, final String curveName, final String advisoryForward, final String advisoryFunding,
      final String curveCalculationMethod) {
    return YieldCurveFunction.getCurveRequirement(FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity()), curveName, advisoryForward, advisoryFunding,
        curveCalculationMethod);
  }

  private ValueSpecification getResultSpec(final ComputationTarget target, final String forwardCurveName, final String fundingCurveName, final String curveCalculationMethod,
      final String surfaceName, final String currency) {
    return new ValueSpecification(ValueRequirementNames.VALUE_THETA, target.toSpecification(), getResultProperties(currency, forwardCurveName, fundingCurveName, curveCalculationMethod,
        surfaceName).get());
  }

  private ValueRequirement getVolatilityRequirement(final String surface, final Currency currency) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.SURFACE, surface)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.IR_FUTURE_OPTION).get();
    return new ValueRequirement(ValueRequirementNames.INTERPOLATED_VOLATILITY_SURFACE, ComputationTargetSpecification.of(currency), properties);
  }
}
