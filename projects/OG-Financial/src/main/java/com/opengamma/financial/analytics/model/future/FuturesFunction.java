/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.future;

import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.simpleinstruments.pricing.SimpleFutureDataBundle;
import com.opengamma.core.position.Trade;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.conversion.FutureSecurityConverter;
import com.opengamma.financial.analytics.timeseries.DateConstraint;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolutionResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.util.ArgumentChecker;

/**
 * @param <T> The type of the data returned from the calculator
 */
public abstract class FuturesFunction<T> extends AbstractFunction.NonCompiledInvoker {
  private static final Logger s_logger = LoggerFactory.getLogger(FuturesFunction.class);
  private static final FutureSecurityConverter CONVERTER = new FutureSecurityConverter();

  private final String _valueRequirementName;
  private final InstrumentDerivativeVisitor<SimpleFutureDataBundle, T> _calculator;

  /**
   * @param valueRequirementName String describes the value requested
   * @param calculator The calculator
   */
  public FuturesFunction(final String valueRequirementName, final InstrumentDerivativeVisitor<SimpleFutureDataBundle, T> calculator)  {
    ArgumentChecker.notNull(valueRequirementName, "value requirement name");
    ArgumentChecker.notNull(calculator, "calculator");
    _valueRequirementName = valueRequirementName;
    _calculator = calculator;
  }


  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(_valueRequirementName, target.toSpecification(), createValueProperties(target).get()));
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Trade trade = target.getTrade();
    final FutureSecurity security = (FutureSecurity) trade.getSecurity();
    // Get reference price
    final HistoricalTimeSeriesBundle timeSeriesBundle = HistoricalTimeSeriesFunctionUtils.getHistoricalTimeSeriesInputs(executionContext, inputs);
    Double lastMarginPrice = null;
    try {
      lastMarginPrice = timeSeriesBundle.get(MarketDataRequirementNames.MARKET_VALUE, security.getExternalIdBundle()).getTimeSeries().getLatestValue();
    } catch (final NoSuchElementException e) {
      throw new OpenGammaRuntimeException("Time series for " + security.getExternalIdBundle() + " was empty");
    }
    // Build the analytic's version of the security - the derivative
    final ZonedDateTime valuationTime = executionContext.getValuationClock().zonedDateTime();
    final InstrumentDefinitionWithData<?, Double> definition = security.accept(CONVERTER);
    final InstrumentDerivative derivative = definition.toDerivative(valuationTime, lastMarginPrice);
    // Build the DataBundle it requires
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final SimpleFutureDataBundle dataBundle = getFutureDataBundle(security, inputs, timeSeriesBundle, desiredValue);
    // Call OG-Analytics
    final T value = derivative.accept(_calculator, dataBundle);
    final ValueSpecification specification = new ValueSpecification(_valueRequirementName, target.toSpecification(), createValueProperties(target, desiredValue).get());
    return Collections.singleton(new ComputedValue(specification, value));
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
    return target.getTrade().getSecurity() instanceof FutureSecurity;
  }

  protected abstract ValueProperties.Builder createValueProperties(final ComputationTarget target);

  protected ValueProperties.Builder createValueProperties(final ComputationTarget target, final ValueRequirement desiredValue) {
    return createValueProperties(target);
  }

  protected abstract SimpleFutureDataBundle getFutureDataBundle(final FutureSecurity security, final FunctionInputs inputs,
      final HistoricalTimeSeriesBundle timeSeriesBundle, final ValueRequirement desiredValue);

  protected InstrumentDerivativeVisitor<SimpleFutureDataBundle, T> getCalculator() {
    return _calculator;
  }

  protected ValueRequirement getSpotAssetRequirement(final FutureSecurity security) {
    final ExternalId spotAssetId = getSpotAssetId(security);
    final ValueRequirement req = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, spotAssetId);
    return req;
  }

  protected ExternalId getSpotAssetId(final FutureSecurity sec) {
    final ExternalId spotAssetId = FinancialSecurityUtils.getUnderlyingId(sec);
    if (spotAssetId == null) {
      throw new OpenGammaRuntimeException(sec.getContractCategory() + " failed to find spot asset id.");
    }
    return spotAssetId;
  }

  protected Double getSpot(final FutureSecurity security, final FunctionInputs inputs) {
    final ValueRequirement spotRequirement = getSpotAssetRequirement(security);
    final Object spotObject = inputs.getValue(spotRequirement);
    if (spotObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + spotRequirement);
    }
    return (Double) spotObject;
  }

  protected ValueRequirement getReferencePriceRequirement(final FunctionCompilationContext context, final FutureSecurity security) {
    final HistoricalTimeSeriesResolver resolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
    final ExternalIdBundle idBundle = security.getExternalIdBundle();
    final HistoricalTimeSeriesResolutionResult timeSeries = resolver.resolve(security.getExternalIdBundle(), null, null, null, MarketDataRequirementNames.MARKET_VALUE, null);
    if (timeSeries == null) {
      s_logger.warn("Failed to find time series for: " + idBundle.toString());
      return null;
    }
    return HistoricalTimeSeriesFunctionUtils.createHTSRequirement(timeSeries, MarketDataRequirementNames.MARKET_VALUE,
        DateConstraint.VALUATION_TIME.minus(Period.ofDays(7)), true, DateConstraint.VALUATION_TIME, true);
  }

}
