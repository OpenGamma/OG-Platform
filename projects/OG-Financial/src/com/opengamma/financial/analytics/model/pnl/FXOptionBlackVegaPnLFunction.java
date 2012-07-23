/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.Security;
import com.opengamma.core.value.MarketDataRequirementNames;
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
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.InterpolatedDataProperties;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.analytics.model.forex.option.black.FXOptionBlackFunction;
import com.opengamma.financial.analytics.timeseries.DateConstraint;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.fx.FXUtils;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolutionResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 * 
 */
public class FXOptionBlackVegaPnLFunction extends AbstractFunction.NonCompiledInvoker{

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    return null;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Security security = target.getPosition().getSecurity();
    return security instanceof FXOptionSecurity || security instanceof NonDeliverableFXOptionSecurity;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties()
        .with(ValuePropertyNames.CALCULATION_METHOD, FXOptionBlackFunction.BLACK_METHOD)
        .withAny(FXOptionBlackFunction.PUT_CURVE)
        .withAny(FXOptionBlackFunction.PUT_CURVE_CALC_CONFIG)
        .withAny(FXOptionBlackFunction.CALL_CURVE)
        .withAny(FXOptionBlackFunction.CALL_CURVE_CALC_CONFIG)
        .withAny(ValuePropertyNames.SURFACE)
        .withAny(InterpolatedDataProperties.X_INTERPOLATOR_NAME)
        .withAny(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME)
        .withAny(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME)
        .with(ValuePropertyNames.CURRENCY, getResultCurrency(target))
        .withAny(ValuePropertyNames.SAMPLING_PERIOD)
        .withAny(ValuePropertyNames.SCHEDULE_CALCULATOR)
        .withAny(ValuePropertyNames.SAMPLING_FUNCTION)
        .with(YieldCurveNodePnLFunction.PROPERTY_PNL_CONTRIBUTIONS, ValueRequirementNames.VEGA_QUOTE_MATRIX)
        .get();
    return Sets.newHashSet(new ValueSpecification(ValueRequirementNames.PNL_SERIES, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> putCurveNames = constraints.getValues(FXOptionBlackFunction.PUT_CURVE);
    if (putCurveNames == null || putCurveNames.size() != 1) {
      return null;
    }
    final Set<String> putCurveCalculationConfigs = constraints.getValues(FXOptionBlackFunction.PUT_CURVE_CALC_CONFIG);
    if (putCurveCalculationConfigs == null || putCurveCalculationConfigs.size() != 1) {
      return null;
    }
    final Set<String> callCurveNames = constraints.getValues(FXOptionBlackFunction.CALL_CURVE);
    if (callCurveNames == null || callCurveNames.size() != 1) {
      return null;
    }
    final Set<String> callCurveCalculationConfigs = constraints.getValues(FXOptionBlackFunction.CALL_CURVE_CALC_CONFIG);
    if (callCurveCalculationConfigs == null || callCurveCalculationConfigs.size() != 1) {
      return null;
    }
    final Set<String> surfaceNames = constraints.getValues(ValuePropertyNames.SURFACE);
    if (surfaceNames == null || surfaceNames.size() != 1) {
      return null;
    }
    final Set<String> interpolatorNames = constraints.getValues(InterpolatedDataProperties.X_INTERPOLATOR_NAME);
    if (interpolatorNames == null || interpolatorNames.size() != 1) {
      return null;
    }
    final Set<String> leftExtrapolatorNames = constraints.getValues(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME);
    if (leftExtrapolatorNames == null || leftExtrapolatorNames.size() != 1) {
      return null;
    }
    final Set<String> rightExtrapolatorNames = constraints.getValues(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME);
    if (rightExtrapolatorNames == null || rightExtrapolatorNames.size() != 1) {
      return null;
    }
    final Set<String> samplingPeriods = constraints.getValues(ValuePropertyNames.SAMPLING_PERIOD);
    if (samplingPeriods == null || samplingPeriods.size() != 1) {
      return null;
    }
    final FinancialSecurity security = (FinancialSecurity) target.getPosition().getSecurity();
    final Currency putCurrency = security.accept(ForexVisitors.getPutCurrencyVisitor());
    final Currency callCurrency = security.accept(ForexVisitors.getCallCurrencyVisitor());
    final UnorderedCurrencyPair currencies = UnorderedCurrencyPair.of(putCurrency, callCurrency);
    final String surfaceName = Iterables.getOnlyElement(surfaceNames);
    final String samplingPeriod = Iterables.getOnlyElement(samplingPeriods);
    final ValueRequirement vegaMatrixRequirement = new ValueRequirement(ValueRequirementNames.VEGA_QUOTE_MATRIX, security,
        ValueProperties.builder()
        .with(ValuePropertyNames.CALCULATION_METHOD, FXOptionBlackFunction.BLACK_METHOD)
        .with(FXOptionBlackFunction.PUT_CURVE, Iterables.getOnlyElement(putCurveNames))
        .with(FXOptionBlackFunction.PUT_CURVE_CALC_CONFIG, Iterables.getOnlyElement(putCurveCalculationConfigs))
        .with(FXOptionBlackFunction.CALL_CURVE, Iterables.getOnlyElement(callCurveNames))
        .with(FXOptionBlackFunction.CALL_CURVE_CALC_CONFIG, Iterables.getOnlyElement(callCurveCalculationConfigs))
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(InterpolatedDataProperties.X_INTERPOLATOR_NAME, Iterables.getOnlyElement(interpolatorNames))
        .with(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME, Iterables.getOnlyElement(leftExtrapolatorNames))
        .with(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME, Iterables.getOnlyElement(rightExtrapolatorNames)).get());
    final ValueRequirement surfaceHTSRequirement = getVolatilitySurfaceHTSRequirement(currencies, surfaceName, samplingPeriod);
    final HistoricalTimeSeriesResolver historicalTimeSeriesResolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
    final ValueRequirement fxSpotRequirement = getFXSpotRequirement(historicalTimeSeriesResolver, security, samplingPeriod);
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    requirements.add(vegaMatrixRequirement);
    requirements.add(surfaceHTSRequirement);
    requirements.add(fxSpotRequirement);
    return requirements;
  }

  private ValueRequirement getVolatilitySurfaceHTSRequirement(final UnorderedCurrencyPair currencies, final String surfaceName, final String samplingPeriod) {
    return HistoricalTimeSeriesFunctionUtils.createVolatilitySurfaceHTSRequirement(currencies, surfaceName, InstrumentTypeProperties.FOREX,
        MarketDataRequirementNames.MARKET_VALUE, null, DateConstraint.VALUATION_TIME.minus(samplingPeriod), true, DateConstraint.VALUATION_TIME, true);
  }

  private ValueRequirement getFXSpotRequirement(final HistoricalTimeSeriesResolver historicalTimeSeriesResolver, final FinancialSecurity security, final String samplingPeriods) {
    if (security instanceof FXOptionSecurity) {
      final FXOptionSecurity fxOption = (FXOptionSecurity) security;
      final HistoricalTimeSeriesResolutionResult timeSeries = historicalTimeSeriesResolver.resolve(
          ExternalIdBundle.of(FXUtils.getSpotIdentifier(fxOption)), null, null, null, MarketDataRequirementNames.MARKET_VALUE, null);
      if (timeSeries == null) {
        return null;
      }
      return HistoricalTimeSeriesFunctionUtils.createHTSRequirement(timeSeries,
          MarketDataRequirementNames.MARKET_VALUE, DateConstraint.VALUATION_TIME.minus(samplingPeriods), true, DateConstraint.VALUATION_TIME, true);
    } else if (security instanceof NonDeliverableFXOptionSecurity) {
      final NonDeliverableFXOptionSecurity fxOption = (NonDeliverableFXOptionSecurity) security;
      final HistoricalTimeSeriesResolutionResult timeSeries = historicalTimeSeriesResolver.resolve(
          ExternalIdBundle.of(FXUtils.getSpotIdentifier(fxOption)), null, null, null, MarketDataRequirementNames.MARKET_VALUE, null);
      if (timeSeries == null) {
        return null;
      }
      return HistoricalTimeSeriesFunctionUtils.createHTSRequirement(timeSeries,
          MarketDataRequirementNames.MARKET_VALUE, DateConstraint.VALUATION_TIME.minus(samplingPeriods), true, DateConstraint.VALUATION_TIME, true);
    }
    throw new OpenGammaRuntimeException("Security was not a FXOptionSecurity or NonDeliverableFXOptionSecurity");
  }

  private String getResultCurrency(final ComputationTarget target) {
    final FinancialSecurity security = (FinancialSecurity) target.getPosition().getSecurity();
    if (security instanceof FXDigitalOptionSecurity) {
      return ((FXDigitalOptionSecurity) target.getSecurity()).getPaymentCurrency().getCode();
    }
    final Currency putCurrency = security.accept(ForexVisitors.getPutCurrencyVisitor());
    final Currency callCurrency = security.accept(ForexVisitors.getCallCurrencyVisitor());
    Currency ccy;
    if (FXUtils.isInBaseQuoteOrder(putCurrency, callCurrency)) {
      ccy = callCurrency;
    } else {
      ccy = putCurrency;
    }
    return ccy.getCode();
  }
}
