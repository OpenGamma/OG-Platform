/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.horizon;

import static com.opengamma.financial.analytics.model.horizon.ThetaPropertyNamesAndValues.PROPERTY_DAYS_TO_MOVE_FORWARD;
import static com.opengamma.financial.analytics.model.horizon.ThetaPropertyNamesAndValues.PROPERTY_THETA_CALCULATION_METHOD;
import static com.opengamma.financial.analytics.model.horizon.ThetaPropertyNamesAndValues.THETA_CONSTANT_SPREAD;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Clock;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.horizon.ConstantSpreadHorizonThetaCalculator;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginTransactionDefinition;
import com.opengamma.analytics.financial.interestrate.PresentValueBlackCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.position.Trade;
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
import com.opengamma.financial.analytics.conversion.InterestRateFutureOptionSecurityConverterDeprecated;
import com.opengamma.financial.analytics.conversion.InterestRateFutureOptionTradeConverterDeprecated;
import com.opengamma.financial.analytics.ircurve.calcconfig.ConfigDBCurveCalculationConfigSource;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.YieldCurveFunctionUtils;
import com.opengamma.financial.analytics.model.irfutureoption.IRFutureOptionFunctionHelper;
import com.opengamma.financial.analytics.timeseries.DateConstraint;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolutionResult;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Function computes the amount a position is expected to gain or lose over a horizon specified by {@link ThetaPropertyNamesAndValues#PROPERTY_DAYS_TO_MOVE_FORWARD}. Horizon calculation is specified
 * in {@link ConstantSpreadHorizonThetaCalculator}.
 * <p>
 * For interest rate future options, {@link PresentValueBlackCalculator} is currently used.
 * <p>
 */
public class InterestRateFutureOptionConstantSpreadThetaFunction extends AbstractFunction.NonCompiledInvoker {
  private static final Logger s_logger = LoggerFactory.getLogger(InterestRateFutureOptionConstantSpreadThetaFunction.class);

  private String _valueRequirement;
  private ConfigDBCurveCalculationConfigSource _curveCalculationConfigSource;

  public InterestRateFutureOptionConstantSpreadThetaFunction() {
    setValueRequirement(ValueRequirementNames.VALUE_THETA);
  }

  public String getValueRequirement() {
    return _valueRequirement;
  }

  protected void setValueRequirement(final String valueRequiremnt) {
    _valueRequirement = valueRequiremnt;
  }

  private InterestRateFutureOptionTradeConverterDeprecated getConverter(final FunctionExecutionContext context) {
    final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaExecutionContext.getRegionSource(context);
    final ConventionBundleSource conventionSource = OpenGammaExecutionContext.getConventionBundleSource(context);
    final SecuritySource securitySource = OpenGammaExecutionContext.getSecuritySource(context);
    return new InterestRateFutureOptionTradeConverterDeprecated(new InterestRateFutureOptionSecurityConverterDeprecated(holidaySource, conventionSource, regionSource, securitySource,
        context.getComputationTargetResolver().getVersionCorrection()));
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    _curveCalculationConfigSource = ConfigDBCurveCalculationConfigSource.init(context, this);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
    final Trade trade = target.getTrade();
    final IRFutureOptionSecurity security = (IRFutureOptionSecurity) trade.getSecurity();
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final Currency currency = FinancialSecurityUtils.getCurrency(security);
    final String curveCalculationConfigName = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    final MultiCurveCalculationConfig curveCalculationConfig = _curveCalculationConfigSource.getConfig(curveCalculationConfigName);
    if (curveCalculationConfig == null) {
      throw new OpenGammaRuntimeException("Could not find curve calculation configuration named " + curveCalculationConfigName);
    }
    final String[] curveNames = curveCalculationConfig.getYieldCurveNames();
    // Append _CCY to be consistent with curve names from YieldCurveFunctionUtils.getAllYieldCurves
    final String[] fullCurveNames = new String[Math.max(2, curveNames.length)];
    for (int i = 0; i < curveNames.length; i++) {
      fullCurveNames[i] = curveNames[i] + "_" + currency.getCode();
    }
    if (curveNames.length == 1) { // MultiCurveCalculationConfig contains just a single curve for discounting and forwarding
      fullCurveNames[1] = fullCurveNames[0];
    }
    final YieldCurveBundle curves = YieldCurveFunctionUtils.getAllYieldCurves(inputs, curveCalculationConfig, _curveCalculationConfigSource);
    final String surfaceName = desiredValue.getConstraint(ValuePropertyNames.SURFACE);
    final String surfaceNameWithPrefix = surfaceName + "_" + IRFutureOptionFunctionHelper.getFutureOptionPrefix(target);

    final Object volatilitySurfaceObject = inputs.getValue(getVolatilityRequirement(surfaceNameWithPrefix, currency));
    if (volatilitySurfaceObject == null) {
      throw new OpenGammaRuntimeException("Could not get volatility surface");
    }
    final VolatilitySurface volatilitySurface = (VolatilitySurface) volatilitySurfaceObject;
    if (!(volatilitySurface.getSurface() instanceof InterpolatedDoublesSurface)) {
      throw new OpenGammaRuntimeException("Expecting an InterpolatedDoublesSurface; got " + volatilitySurface.getSurface().getClass());
    }
    final InstrumentDefinition<?> irFutureOptionDefinition = getConverter(executionContext).convert(trade);
    final HistoricalTimeSeries ts = (HistoricalTimeSeries) inputs.getValue(ValueRequirementNames.HISTORICAL_TIME_SERIES);
    final int length = ts.getTimeSeries().size();
    if (length == 0) {
      throw new OpenGammaRuntimeException("Price time series for " + security.getUnderlyingId() + " was empty");
    }
    final double lastMarginPrice = ts.getTimeSeries().getLatestValue();
    final YieldCurveWithBlackCubeBundle data = new YieldCurveWithBlackCubeBundle(volatilitySurface.getSurface(), curves);

    final String daysForward = desiredValue.getConstraint(PROPERTY_DAYS_TO_MOVE_FORWARD);
    final ConstantSpreadHorizonThetaCalculator calculator = ConstantSpreadHorizonThetaCalculator.getInstance();
    final MultipleCurrencyAmount theta = calculator.getTheta((InterestRateFutureOptionMarginTransactionDefinition) irFutureOptionDefinition, now, fullCurveNames, data, lastMarginPrice,
        Integer.parseInt(daysForward));
    return Collections.singleton(new ComputedValue(getResultSpec(target, curveCalculationConfigName, surfaceName, currency.getCode(), daysForward), getValue(theta, currency)));
  }

  /**
   * This aids child classes to return value in different format, eg Double
   * 
   * @param theta ConstantSpreadHorizonThetaCalculator produced MultipleCurrencyAmount
   * @param currency Allows for function to pull out specified currency
   * @return theta in desired format
   */
  protected Object getValue(final MultipleCurrencyAmount theta, final Currency currency) {
    return theta;
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
    return Collections.singleton(new ValueSpecification(getValueRequirement(), target.toSpecification(), properties.get()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    String surfaceName = constraints.getStrictValue(ValuePropertyNames.SURFACE);
    if (surfaceName == null) {
      return null;
    }
    final String daysForwardName = constraints.getStrictValue(PROPERTY_DAYS_TO_MOVE_FORWARD);
    if (daysForwardName == null) {
      return null;
    }
    final String curveCalculationConfigName = constraints.getStrictValue(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    if (curveCalculationConfigName == null) {
      return null;
    }
    surfaceName = surfaceName + "_" + IRFutureOptionFunctionHelper.getFutureOptionPrefix(target);
    final MultiCurveCalculationConfig curveCalculationConfig = _curveCalculationConfigSource.getConfig(curveCalculationConfigName);
    if (curveCalculationConfig == null) {
      s_logger.error("Could not find curve calculation configuration named " + curveCalculationConfigName);
      return null;
    }
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity());
    if (!ComputationTargetSpecification.of(currency).equals(curveCalculationConfig.getTarget())) {
      s_logger.error("Security currency and curve calculation config id were not equal; have {} and {}", currency, curveCalculationConfig.getTarget());
    }
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    requirements.addAll(YieldCurveFunctionUtils.getCurveRequirements(curveCalculationConfig, _curveCalculationConfigSource));
    requirements.add(getVolatilityRequirement(surfaceName, currency));
    final HistoricalTimeSeriesResolutionResult timeSeries = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context).resolve(
        target.getTrade().getSecurity().getExternalIdBundle(), null, null, null, MarketDataRequirementNames.MARKET_VALUE, null);
    if (timeSeries == null) {
      return null;
    }
    requirements.add(HistoricalTimeSeriesFunctionUtils.createHTSRequirement(timeSeries, MarketDataRequirementNames.MARKET_VALUE, DateConstraint.VALUATION_TIME.minus(Period.ofMonths(1))
        .previousWeekDay(), true, DateConstraint.VALUATION_TIME, false));
    return requirements;
  }

  private ValueProperties.Builder getResultProperties(final String currency) {
    final ValueProperties.Builder properties = createValueProperties().withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG).with(ValuePropertyNames.CURRENCY, currency)
        .withAny(ValuePropertyNames.SURFACE).with(PROPERTY_THETA_CALCULATION_METHOD, THETA_CONSTANT_SPREAD).withAny(PROPERTY_DAYS_TO_MOVE_FORWARD);
    return properties;
  }

  private ValueProperties.Builder getResultProperties(final String currency, final String curveCalculationConfig, final String surfaceName, final String daysForward) {
    final ValueProperties.Builder properties = createValueProperties().with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfig).with(ValuePropertyNames.CURRENCY, currency)
        .with(ValuePropertyNames.SURFACE, surfaceName).with(PROPERTY_THETA_CALCULATION_METHOD, THETA_CONSTANT_SPREAD).with(PROPERTY_DAYS_TO_MOVE_FORWARD, daysForward);
    return properties;
  }

  private ValueSpecification getResultSpec(final ComputationTarget target, final String curveCalculationConfig, final String surfaceName, final String currency, final String daysForward) {
    return new ValueSpecification(getValueRequirement(), target.toSpecification(), getResultProperties(currency, curveCalculationConfig, surfaceName, daysForward).get());
  }

  private ValueRequirement getVolatilityRequirement(final String surface, final Currency currency) {
    final ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.SURFACE, surface)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.IR_FUTURE_OPTION).get();
    return new ValueRequirement(ValueRequirementNames.INTERPOLATED_VOLATILITY_SURFACE, ComputationTargetSpecification.of(currency), properties);
  }

}
