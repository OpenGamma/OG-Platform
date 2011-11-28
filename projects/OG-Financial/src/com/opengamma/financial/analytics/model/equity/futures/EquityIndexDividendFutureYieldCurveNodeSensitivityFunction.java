/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.futures;

import java.util.Collections;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
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
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.conversion.EquityIndexDividendFutureSecurityConverter;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.model.YieldCurveNodeSensitivitiesHelper;
import com.opengamma.financial.equity.future.EquityFutureDataBundle;
import com.opengamma.financial.equity.future.EquityFuturesRatesSensitivityCalculator;
import com.opengamma.financial.equity.future.definition.EquityFutureDefinition;
import com.opengamma.financial.equity.future.derivative.EquityFuture;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.security.future.EquityIndexDividendFutureSecurity;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class EquityIndexDividendFutureYieldCurveNodeSensitivityFunction extends AbstractFunction.NonCompiledInvoker {
  private static final String DIVIDEND_YIELD_FIELD = "EQY_DVD_YLD_EST";
  private static final EquityFuturesRatesSensitivityCalculator CALCULATOR = EquityFuturesRatesSensitivityCalculator.getInstance();
  private final String _fundingCurveName;
  private EquityIndexDividendFutureSecurityConverter _converter;
  
  public EquityIndexDividendFutureYieldCurveNodeSensitivityFunction(final String fundingCurveName) {
    Validate.notNull(fundingCurveName);
    _fundingCurveName = fundingCurveName;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    _converter = new EquityIndexDividendFutureSecurityConverter();
  }
  
  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) {
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final EquityIndexDividendFutureSecurity security = (EquityIndexDividendFutureSecurity) target.getSecurity();
    final ZonedDateTime valuationTime = executionContext.getValuationClock().zonedDateTime();
    //final Double lastMarginPrice = getLatestValueFromTimeSeries(HistoricalTimeSeriesFields.LAST_PRICE, executionContext, security.getExternalIdBundle(), now);
    //trade.setPremium(lastMarginPrice); // TODO !!! Issue of futures and margining
    final EquityFutureDefinition definition = _converter.visitEquityIndexDividendFutureSecurity(security);
    final EquityFuture derivative = definition.toDerivative(valuationTime);
    final YieldAndDiscountCurve fundingCurve = getYieldCurve(security, inputs);
    final Double spot = getSpot(security, inputs);
    final double marketPrice = getMarketPrice(security, inputs);
    //double dividendYield = getLatestValueFromTimeSeries(DIVIDEND_YIELD_FIELD, executionContext, ExternalIdBundle.of(security.getUnderlyingId()), now);
    //dividendYield /= 100.0;
    final DoubleMatrix1D sensitivities = CALCULATOR.calcDeltaBucketed(derivative, new EquityFutureDataBundle(fundingCurve, marketPrice, spot, null, null));
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
    return ComputationTargetType.SECURITY;
  }

  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    return target.getSecurity() instanceof EquityIndexDividendFutureSecurity;
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    final EquityIndexDividendFutureSecurity security = (EquityIndexDividendFutureSecurity) target.getSecurity();
    return Sets.newHashSet(getSpotAssetRequirement(security), getDiscountCurveRequirement(security), getMarketPriceRequirement(security), getCurveSpecRequirement(security.getCurrency()));
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    return Collections.singleton(getValueSpecification(target));
  }
  
  private Double getLatestValueFromTimeSeries(final String field, final FunctionExecutionContext executionContext, final ExternalIdBundle idBundle, final ZonedDateTime now) {
    final ZonedDateTime startDate = now.minusDays(7);
    final HistoricalTimeSeriesSource dataSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
    final HistoricalTimeSeries ts = dataSource.getHistoricalTimeSeries(field, idBundle, null, null, startDate.toLocalDate(), true, now.toLocalDate(), true);

    if (ts == null) {
      throw new OpenGammaRuntimeException("Could not get " + field + " time series for " + idBundle.toString());
    }
    return ts.getTimeSeries().getLatestValue();
  }
  
  private ValueRequirement getSpotAssetRequirement(EquityIndexDividendFutureSecurity security) {
    ValueRequirement req = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, security.getUnderlyingId());
    return req;
  }
  
  private ValueRequirement getMarketPriceRequirement(EquityIndexDividendFutureSecurity security) {
    return new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.SECURITY, security.getUniqueId());
  }
  
  private ValueRequirement getCurveSpecRequirement(final Currency currency) {
    ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, _fundingCurveName).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, ComputationTargetType.PRIMITIVE, currency.getUniqueId(), properties);
  }
  
  private Double getSpot(EquityIndexDividendFutureSecurity security, FunctionInputs inputs) {
    ValueRequirement spotRequirement = getSpotAssetRequirement(security);
    final Object spotObject = inputs.getValue(spotRequirement);
    if (spotObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + spotRequirement);
    }
    return (Double) spotObject;
  }

  private Double getMarketPrice(EquityIndexDividendFutureSecurity security, FunctionInputs inputs) {
    ValueRequirement marketPriceRequirement = getMarketPriceRequirement(security);
    final Object marketPriceObject = inputs.getValue(marketPriceRequirement);
    if (marketPriceObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + marketPriceRequirement);
    }
    return (Double) marketPriceObject;
  }
  
  private YieldAndDiscountCurve getYieldCurve(EquityIndexDividendFutureSecurity security, FunctionInputs inputs) {
    final ValueRequirement curveRequirement = getDiscountCurveRequirement(security);
    final Object curveObject = inputs.getValue(curveRequirement);
    if (curveObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + curveRequirement);
    }
    return (YieldAndDiscountCurve) curveObject;
  }
  
  private ValueRequirement getDiscountCurveRequirement(EquityIndexDividendFutureSecurity security) {
    ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, _fundingCurveName).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, security.getCurrency().getUniqueId(), properties);
  }
  
  private ValueSpecification getValueSpecification(final ComputationTarget target) {
    final EquityIndexDividendFutureSecurity security = (EquityIndexDividendFutureSecurity) target.getSecurity();
    final ValueProperties properties = createValueProperties()
        .with(ValuePropertyNames.CURVE, _fundingCurveName)
        .with(ValuePropertyNames.CURVE_CURRENCY, security.getCurrency().getCode())
        .with(ValuePropertyNames.CURRENCY, security.getCurrency().getCode()).get();
    return new ValueSpecification(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, target.toSpecification(), properties);
  }
}
