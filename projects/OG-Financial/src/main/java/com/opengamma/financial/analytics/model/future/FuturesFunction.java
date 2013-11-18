/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.future;

import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.simpleinstruments.pricing.SimpleFutureDataBundle;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.position.Trade;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.conversion.FutureTradeConverterDeprecated;
import com.opengamma.financial.analytics.timeseries.DateConstraint;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolutionResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.util.ArgumentChecker;

/**
 * Base class for FuturesSecurity ValueRequirements.
 * FuturesFunctions, as the securities are exchange traded, closely resemble MarkToMarketPnLFunction.
 * @param <T> The type of the data returned from the calculator
 */
public abstract class FuturesFunction<T> extends AbstractFunction.NonCompiledInvoker {

  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(FuturesFunction.class);

  /** The value requirement name */
  private final String _valueRequirementName;
  /** The calculator */
  private final InstrumentDerivativeVisitor<SimpleFutureDataBundle, T> _calculator;
  /** The trade converter */
  private FutureTradeConverterDeprecated _tradeConverter;
  /** The field name of the historical time series for price, e.g. "PX_LAST", "Close" */
  private final String _closingPriceField;
  /** The field name of the historical time series for cost of carry e.g. "COST_OF_CARRY" */
  private final String _costOfCarryField;
  /** key defining how the time series resolution is to occur e.g. "DEFAULT_TSS_CONFIG"*/
  private final String _resolutionKey;

  /**
   * @param valueRequirementName String describes the value requested
   * @param calculator The calculator
   * @param closingPriceField The field name of the historical time series for price, e.g. "PX_LAST", "Close". Set in *FunctionConfiguration
   * @param costOfCarryField The field name of the historical time series for cost of carry e.g. "COST_OF_CARRY". Set in *FunctionConfiguration
   * @param resolutionKey The key defining how the time series resolution is to occur e.g. "DEFAULT_TSS_CONFIG"
   */
  public FuturesFunction(final String valueRequirementName, final InstrumentDerivativeVisitor<SimpleFutureDataBundle, T> calculator,
      final String closingPriceField, final String costOfCarryField, final String resolutionKey) {
    ArgumentChecker.notNull(valueRequirementName, "value requirement name");
    ArgumentChecker.notNull(calculator, "calculator");
    ArgumentChecker.notNull(closingPriceField, "closingPriceField");
    ArgumentChecker.notNull(costOfCarryField, "costOfCarryField");
    ArgumentChecker.notNull(resolutionKey, "resolutionKey");
    _valueRequirementName = valueRequirementName;
    _calculator = calculator;
    _closingPriceField = closingPriceField;
    _costOfCarryField = costOfCarryField;
    _resolutionKey = resolutionKey;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    _tradeConverter = new FutureTradeConverterDeprecated(securitySource, holidaySource, conventionSource, regionSource);
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
    if (timeSeriesBundle == null) {
      throw new OpenGammaRuntimeException("Could not get time series bundle for " + trade);
    }
    Double lastMarginPrice = null;
    try {
      lastMarginPrice = timeSeriesBundle.get(MarketDataRequirementNames.MARKET_VALUE, security.getExternalIdBundle()).getTimeSeries().getLatestValue();
    } catch (final NoSuchElementException e) {
      throw new OpenGammaRuntimeException("Time series for " + security.getExternalIdBundle() + " was empty");
    }
    // Build the analytic's version of the security - the derivative
    final ZonedDateTime valuationTime = ZonedDateTime.now(executionContext.getValuationClock());
    final InstrumentDefinitionWithData<?, Double> tradeDefinition = _tradeConverter.convert(trade);
    double referencePrice = lastMarginPrice; // TODO: Decide if this logic should be here or in toDerivative.
    if (trade.getTradeDate() != null) {
      if (trade.getTradeDate().isEqual(valuationTime.toLocalDate())) { // Transaction is on pricing date.if (trade.getPremium() != null) {
        if (trade.getPremium() != null) {
          referencePrice = trade.getPremium(); // TODO: The trade price is stored in the trade premium. This has to be corrected.
        }
      }
    }
    final InstrumentDerivative derivative = tradeDefinition.toDerivative(valuationTime, referencePrice);
    // Build the DataBundle it requires
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final SimpleFutureDataBundle dataBundle = getFutureDataBundle(security, inputs, timeSeriesBundle, desiredValue);
    // Call OG-Analytics
    final T value = derivative.accept(_calculator, dataBundle);
    final T scaledValue = applyTradeScaling(trade, value);
    final ValueSpecification specification = new ValueSpecification(_valueRequirementName, target.toSpecification(), createValueProperties(target, desiredValue).get());
    return Collections.singleton(new ComputedValue(specification, scaledValue));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.TRADE;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Security security = target.getTrade().getSecurity();
    return security instanceof FutureSecurity;
  }

  /**
   * Creates general value properties
   * @param target The target
   * @return The value properties
   */
  protected abstract ValueProperties.Builder createValueProperties(final ComputationTarget target);

  /**
   * Creates value properties with the property values set
   * @param target The target
   * @param desiredValue The desired value
   * @return The value properties
   */
  protected ValueProperties.Builder createValueProperties(final ComputationTarget target, final ValueRequirement desiredValue) {
    return createValueProperties(target);
  }

  /**
   * Creates the data bundle used in futures pricing
   * @param security The security
   * @param inputs The market data inputs
   * @param timeSeriesBundle A bundle containing time series
   * @param desiredValue The desired value
   * @return The data bundle used for pricing futures
   */
  protected abstract SimpleFutureDataBundle getFutureDataBundle(final FutureSecurity security, final FunctionInputs inputs,
      final HistoricalTimeSeriesBundle timeSeriesBundle, final ValueRequirement desiredValue);

  /**
   * @return The calculator
   */
  protected InstrumentDerivativeVisitor<SimpleFutureDataBundle, T> getCalculator() {
    return _calculator;
  }

  /**
   * Gets the spot value requirement
   * @param security The security
   * @return The spot asset value requirement if the future has a spot asset id, null otherwise
   */
  protected ValueRequirement getSpotAssetRequirement(final FutureSecurity security) {
    try {
      final ExternalId spotAssetId = getSpotAssetId(security);
      if (spotAssetId == null) {
        return null;
      }
      final ValueRequirement req = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.PRIMITIVE, spotAssetId);
      return req;
    } catch (final UnsupportedOperationException e) {
      s_logger.info(e.getMessage());
      return null;
    }
  }

  /**
   * Gets the spot asset id from a security
   * @param sec The security
   * @return The spot asset id, null if not available
   */
  protected ExternalId getSpotAssetId(final FutureSecurity sec) {
    final ExternalId spotAssetId = FinancialSecurityUtils.getUnderlyingId(sec);
    if (spotAssetId == null) {
      s_logger.info("Failed to find spot asset id (category = {}) for future with id bundle {}", sec.getContractCategory(), sec.getExternalIdBundle());
      return null;
    }
    return spotAssetId;
  }

  /**
   * Gets the spot value
   * @param inputs The market data inputs
   * @return The spot value
   * @throws OpenGammaRuntimeException If the spot value is null
   */
  protected Double getSpot(final FunctionInputs inputs) {
    final Object spotObject = inputs.getValue(MarketDataRequirementNames.MARKET_VALUE);
    if (spotObject == null) {
      throw new OpenGammaRuntimeException("Could not get the spot value");
    }
    return (Double) spotObject;
  }

  /**
   * Gets the historical time series of the future price
   * @param context The compilation context
   * @param security The security
   * @return The value requirement for the time series of future price
   */
  protected ValueRequirement getReferencePriceRequirement(final FunctionCompilationContext context, final FutureSecurity security) {
    final HistoricalTimeSeriesResolver resolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
    final ExternalIdBundle idBundle = security.getExternalIdBundle();
    // TODO CASE: Test that you can change field to MARKET_VALUE because of the FieldAdjustment in ActivHistoricalTimeSeriesSourceComponentFactory.createResolver
    final HistoricalTimeSeriesResolutionResult timeSeries = resolver.resolve(security.getExternalIdBundle(), null, null, null, MarketDataRequirementNames.MARKET_VALUE, getResolutionKey());
    if (timeSeries == null) {
      s_logger.warn("Failed to find time series for: " + idBundle.toString());
      return null;
    }
    return HistoricalTimeSeriesFunctionUtils.createHTSRequirement(timeSeries, MarketDataRequirementNames.MARKET_VALUE,
        DateConstraint.VALUATION_TIME.minus(Period.ofDays(7)), true, DateConstraint.VALUATION_TIME, true);
  }
  /**
   * FuturesFunction acts upon ComputationTargetType.TRADE, but many of the calculators used in the execute() method really operate
   * as if they acted upon ComputationTargetType.SECUIRITY. 
   * For this reason, it is the responsibility of the Function here to scale by trade.getQuantity() if appropriate.
   * @param trade Computation Target
   * @param value Computed Result to be scaled
   * @return Scaled result
   */
  protected T applyTradeScaling(final Trade trade, T value) {
    return value;
  }

  protected String getClosingPriceField() {
    return _closingPriceField;
  }

  protected String getCostOfCarryField() {
    return _costOfCarryField;
  }

  protected String getResolutionKey() {
    return _resolutionKey;
  }

}
