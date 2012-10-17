/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.futures;

import java.util.Collections;
import java.util.Set;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.equity.future.EquityFutureDataBundle;
import com.opengamma.analytics.financial.equity.future.EquityFuturesRatesSensitivityCalculator;
import com.opengamma.analytics.financial.equity.future.definition.EquityFutureDefinition;
import com.opengamma.analytics.financial.equity.future.derivative.EquityFuture;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.core.position.Trade;
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
import com.opengamma.financial.analytics.conversion.EquityFutureConverter;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.model.YieldCurveNodeSensitivitiesHelper;
import com.opengamma.financial.analytics.timeseries.DateConstraint;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolutionResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class EquityFutureYieldCurveNodeSensitivityFunction extends AbstractFunction.NonCompiledInvoker {

  private static final String DIVIDEND_YIELD_FIELD = "EQY_DVD_YLD_EST";

  private static final EquityFuturesRatesSensitivityCalculator CALCULATOR = EquityFuturesRatesSensitivityCalculator.getInstance();
  private final String _fundingCurveName;
  private EquityFutureConverter _converter;

  public EquityFutureYieldCurveNodeSensitivityFunction(final String fundingCurveName) {
    Validate.notNull(fundingCurveName);
    _fundingCurveName = fundingCurveName;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    _converter = new EquityFutureConverter();
  }

  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) {
    final HistoricalTimeSeriesBundle timeSeries = HistoricalTimeSeriesFunctionUtils.getHistoricalTimeSeriesInputs(executionContext, inputs);
    final Trade trade = target.getTrade();
    final EquityFutureSecurity security = (EquityFutureSecurity) trade.getSecurity();
    final ZonedDateTime valuationTime = executionContext.getValuationClock().zonedDateTime();
    final Double lastMarginPrice = timeSeries.get(MarketDataRequirementNames.MARKET_VALUE, security.getExternalIdBundle()).getTimeSeries().getLatestValue();
    final EquityFutureDefinition definition = _converter.visitEquityFutureTrade(trade, lastMarginPrice);
    final EquityFuture derivative = definition.toDerivative(valuationTime);
    final YieldAndDiscountCurve fundingCurve = getYieldCurve(security, inputs);
    final Double spot = getSpot(security, inputs);
    final Double marketPrice = getMarketPrice(security, inputs);
    double dividendYield = timeSeries.get(DIVIDEND_YIELD_FIELD, security.getUnderlyingId()).getTimeSeries().getLatestValue();
    dividendYield /= 100.0;
    final DoubleMatrix1D sensitivities = CALCULATOR.calcDeltaBucketed(derivative, new EquityFutureDataBundle(fundingCurve, marketPrice, spot, dividendYield, null));
    final Object curveSpecObject = inputs.getValue(getCurveSpecRequirement(security.getCurrency()));
    if (curveSpecObject == null) {
      throw new OpenGammaRuntimeException("Curve specification was null");
    }
    final InterpolatedYieldCurveSpecificationWithSecurities curveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) curveSpecObject;
    final ValueSpecification resultSpec = getValueSpecification(target);
    return YieldCurveNodeSensitivitiesHelper.getSensitivitiesForCurve(fundingCurve, sensitivities, curveSpec, resultSpec);
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.TRADE;
  }

  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    return target.getTrade().getSecurity() instanceof com.opengamma.financial.security.future.EquityFutureSecurity;
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    final Trade trade = target.getTrade();
    final EquityFutureSecurity security = (EquityFutureSecurity) trade.getSecurity();
    final Set<ValueRequirement> requirements = Sets.newHashSetWithExpectedSize(6);
    requirements.add(getSpotAssetRequirement(security));
    requirements.add(getDiscountCurveRequirement(security));
    requirements.add(getMarketPriceRequirement(security));
    requirements.add(getCurveSpecRequirement(security.getCurrency()));
    ValueRequirement requirement = getMarketValueRequirement(context, security);
    if (requirement == null) {
      return null;
    }
    requirements.add(requirement);
    requirement = getDividendYieldRequirement(context, security);
    if (requirement == null) {
      return null;
    }
    requirements.add(requirement);
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    return Collections.singleton(getValueSpecification(target));
  }

  private ValueRequirement getSpotAssetRequirement(EquityFutureSecurity security) {
    ValueRequirement req = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.PRIMITIVE, security.getUnderlyingId());
    return req;
  }

  private ValueRequirement getMarketPriceRequirement(EquityFutureSecurity security) {
    return new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.SECURITY, security.getUniqueId());
  }

  private ValueRequirement getCurveSpecRequirement(final Currency currency) {
    ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, _fundingCurveName).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, ComputationTargetSpecification.of(currency), properties);
  }

  private ValueRequirement getMarketValueRequirement(final FunctionCompilationContext context, final EquityFutureSecurity security) {
    final HistoricalTimeSeriesResolver resolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
    final HistoricalTimeSeriesResolutionResult timeSeries = resolver.resolve(security.getExternalIdBundle(), null, null, null, MarketDataRequirementNames.MARKET_VALUE, null);
    if (timeSeries == null) {
      return null;
    }
    return HistoricalTimeSeriesFunctionUtils.createHTSRequirement(timeSeries, MarketDataRequirementNames.MARKET_VALUE,
        DateConstraint.VALUATION_TIME.minus(Period.ofDays(7)), true, DateConstraint.VALUATION_TIME, true);
  }

  private ValueRequirement getDividendYieldRequirement(final FunctionCompilationContext context, final EquityFutureSecurity security) {
    final HistoricalTimeSeriesResolver resolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
    final HistoricalTimeSeriesResolutionResult timeSeries = resolver.resolve(ExternalIdBundle.of(security.getUnderlyingId()), null, null, null, DIVIDEND_YIELD_FIELD, null);
    if (timeSeries == null) {
      return null;
    }
    return HistoricalTimeSeriesFunctionUtils.createHTSRequirement(timeSeries, DIVIDEND_YIELD_FIELD, DateConstraint.VALUATION_TIME.minus(Period.ofDays(7)),
        true, DateConstraint.VALUATION_TIME, true);
  }

  private Double getSpot(EquityFutureSecurity security, FunctionInputs inputs) {
    ValueRequirement spotRequirement = getSpotAssetRequirement(security);
    final Object spotObject = inputs.getValue(spotRequirement);
    if (spotObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + spotRequirement);
    }
    return (Double) spotObject;
  }

  private Double getMarketPrice(EquityFutureSecurity security, FunctionInputs inputs) {
    ValueRequirement marketPriceRequirement = getMarketPriceRequirement(security);
    final Object marketPriceObject = inputs.getValue(marketPriceRequirement);
    if (marketPriceObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + marketPriceRequirement);
    }
    return (Double) marketPriceObject;
  }

  private YieldAndDiscountCurve getYieldCurve(EquityFutureSecurity security, FunctionInputs inputs) {
    final ValueRequirement curveRequirement = getDiscountCurveRequirement(security);
    final Object curveObject = inputs.getValue(curveRequirement);
    if (curveObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + curveRequirement);
    }
    return (YieldAndDiscountCurve) curveObject;
  }

  private ValueRequirement getDiscountCurveRequirement(EquityFutureSecurity security) {
    ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, _fundingCurveName).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.of(security.getCurrency()), properties);
  }

  private ValueSpecification getValueSpecification(final ComputationTarget target) {
    final EquityFutureSecurity security = (EquityFutureSecurity) target.getTrade().getSecurity();
    final ValueProperties properties = createValueProperties()
        .with(ValuePropertyNames.CURVE, _fundingCurveName)
        .with(ValuePropertyNames.CURVE_CURRENCY, security.getCurrency().getCode())
        .with(ValuePropertyNames.CURRENCY, security.getCurrency().getCode()).get();
    return new ValueSpecification(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, target.toSpecification(), properties);
  }
}
