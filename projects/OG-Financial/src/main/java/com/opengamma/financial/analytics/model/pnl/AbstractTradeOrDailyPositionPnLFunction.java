/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Clock;
import org.threeten.bp.LocalDate;

import com.google.common.collect.Sets;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.position.PositionOrTrade;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueProperties.Builder;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.model.forex.FXUtils;
import com.opengamma.financial.analytics.timeseries.DateConstraint;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolutionResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MoneyCalculationUtils;

/**
 *
 */
public abstract class AbstractTradeOrDailyPositionPnLFunction extends AbstractFunction.NonCompiledInvoker {

  private static final Logger s_logger = LoggerFactory.getLogger(AbstractTradeOrDailyPositionPnLFunction.class);

  private final String _mark2MarketField;
  private final String _costOfCarryField;
  private final String _resolutionKey;

  /**
   * @param resolutionKey the resolution key, not-null
   * @param mark2MarketField the mark to market data field name, not-null
   * @param costOfCarryField the cost of carry field name, not-null
   */
  public AbstractTradeOrDailyPositionPnLFunction(final String resolutionKey, final String mark2MarketField, final String costOfCarryField) {
    super();
    ArgumentChecker.notNull(resolutionKey, "resolutionKey");
    ArgumentChecker.notNull(mark2MarketField, "mark data field");
    ArgumentChecker.notNull(costOfCarryField, "cost of carry data field");
    _resolutionKey = resolutionKey;
    _mark2MarketField = mark2MarketField;
    _costOfCarryField = costOfCarryField;
  }

  protected abstract LocalDate getPreferredTradeDate(Clock valuationClock, PositionOrTrade positionOrTrade);

  protected abstract DateConstraint getTimeSeriesStartDate(PositionOrTrade positionOrTrade);

  protected abstract DateConstraint getTimeSeriesEndDate(PositionOrTrade positionOrTrade);

  protected abstract LocalDate checkAvailableData(LocalDate originalTradeDate, HistoricalTimeSeries markToMarketSeries, Security security, String markDataField, String resolutionKey);

  protected abstract String getResultValueRequirementName();

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    BigDecimal tradeValue = null;
    HistoricalTimeSeries htsMarkToMarket = null;
    HistoricalTimeSeries htsCostOfCarry = null;
    final PositionOrTrade trade = target.getPositionOrTrade();
    for (final ComputedValue input : inputs.getAllValues()) {
      if (ValueRequirementNames.VALUE.equals(input.getSpecification().getValueName())) {
        tradeValue = BigDecimal.valueOf((Double) input.getValue());
        if (trade instanceof Trade) {
          // Need to scale the value by the trade quantity
          tradeValue = ((Trade) trade).getQuantity().multiply(tradeValue);
        }
      } else if (ValueRequirementNames.HISTORICAL_TIME_SERIES.equals(input.getSpecification().getValueName())) {
        final String field = input.getSpecification().getProperty(HistoricalTimeSeriesFunctionUtils.DATA_FIELD_PROPERTY);
        if (_costOfCarryField.equals(field)) {
          htsCostOfCarry = (HistoricalTimeSeries) input.getValue();
        } else if (_mark2MarketField.equals(field)) {
          htsMarkToMarket = (HistoricalTimeSeries) input.getValue();
        }
      }
    }
    final Security security = trade.getSecurity();
    LocalDate tradeDate = getPreferredTradeDate(executionContext.getValuationClock(), trade);
    tradeDate = checkAvailableData(tradeDate, htsMarkToMarket, security, _mark2MarketField, _resolutionKey);
    final ValueSpecification valueSpecification = new ValueSpecification(getResultValueRequirementName(), target.toSpecification(), desiredValue.getConstraints());
    final double costOfCarry = getCostOfCarry(security, tradeDate, htsCostOfCarry);
    Double markToMarket = htsMarkToMarket.getTimeSeries().getValue(tradeDate);
    if (security instanceof FutureSecurity) {
      final FutureSecurity futureSecurity = (FutureSecurity) security;
      markToMarket = markToMarket * futureSecurity.getUnitAmount();
    }
    final BigDecimal dailyPnL = tradeValue.subtract(trade.getQuantity().multiply(BigDecimal.valueOf(markToMarket + costOfCarry)));
    s_logger.debug("{}  security: {} quantity: {} fairValue: {} markToMarket: {} costOfCarry: {} dailyPnL: {}",
          new Object[] {trade.getUniqueId(), trade.getSecurity().getExternalIdBundle(), trade.getQuantity(), tradeValue, markToMarket, costOfCarry, dailyPnL });
    final ComputedValue result = new ComputedValue(valueSpecification, MoneyCalculationUtils.rounded(dailyPnL).doubleValue());
    return Sets.newHashSet(result);
  }

  private double getCostOfCarry(final Security security, final LocalDate tradeDate, final HistoricalTimeSeries costOfCarryTS) {
    double result = 0.0d;
    if (costOfCarryTS != null) {
      final Double histCost = costOfCarryTS.getTimeSeries().getValue(tradeDate);
      if (histCost != null) {
        result = histCost;
      }
    }
    return result;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final PositionOrTrade positionOrTrade = target.getPositionOrTrade();
    final Security security = positionOrTrade.getSecurity();
    final ValueRequirement securityOrTradeValue;
    if (positionOrTrade instanceof Trade) {
      // If a TRADE, request the SECURITY's value and scale up during the execute
      securityOrTradeValue = new ValueRequirement(ValueRequirementNames.VALUE, ComputationTargetType.SECURITY, security.getUniqueId(), getCurrencyProperty(security));
    } else {
      // If a POSITION, request the POSITION's value and DON'T scale during the execute
      securityOrTradeValue = new ValueRequirement(ValueRequirementNames.VALUE, target.toSpecification(), getCurrencyProperty(security));
    }
    final HistoricalTimeSeriesResolver resolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
    final ExternalIdBundle bundle = security.getExternalIdBundle();
    final DateConstraint startDate = getTimeSeriesStartDate(positionOrTrade);
    final DateConstraint endDate = getTimeSeriesEndDate(positionOrTrade);
    final ValueRequirement markToMarketValue = getMarkToMarketSeriesRequirement(resolver, bundle, startDate, endDate);
    final ValueRequirement costOfCarryValue = getCostOfCarrySeriesRequirement(resolver, bundle, endDate);

    if (markToMarketValue == null && costOfCarryValue == null) {
      return null;
    }

    final Set<ValueRequirement> requirements = Sets.newHashSet(securityOrTradeValue);
    if (markToMarketValue != null) {
      requirements.add(markToMarketValue);
    }
    if (costOfCarryValue != null) {
      requirements.add(costOfCarryValue);
    }
    return requirements;
  }

  protected ValueProperties getCurrencyProperty(final Security security) {
    final Currency ccy = FinancialSecurityUtils.getCurrency(security);
    if (ccy != null) {
      return ValueProperties.with(ValuePropertyNames.CURRENCY, ccy.getCode()).get();
    } else {
      return ValueProperties.none();
    }
  }

  private ValueRequirement getMarkToMarketSeriesRequirement(final HistoricalTimeSeriesResolver resolver, final ExternalIdBundle bundle, final DateConstraint startDate, final DateConstraint endDate) {
    final HistoricalTimeSeriesResolutionResult timeSeries = resolver.resolve(bundle, null, null, null, _mark2MarketField, _resolutionKey);
    if (timeSeries == null) {
      return null;
    }
    return HistoricalTimeSeriesFunctionUtils.createHTSRequirement(timeSeries, _mark2MarketField, startDate, true, endDate, true);
  }

  private ValueRequirement getCostOfCarrySeriesRequirement(final HistoricalTimeSeriesResolver resolver, final ExternalIdBundle bundle, final DateConstraint endDate) {
    final HistoricalTimeSeriesResolutionResult timeSeries = resolver.resolve(bundle, null, null, null, _costOfCarryField, _resolutionKey);
    if (timeSeries == null) {
      return null;
    }
    return HistoricalTimeSeriesFunctionUtils.createHTSRequirement(timeSeries, _costOfCarryField, endDate, true, endDate, true);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return !FXUtils.isFXSecurity(target.getPositionOrTrade().getSecurity());
  }

  protected ValueProperties.Builder createValueProperties(final ComputationTarget target) {
    final ValueProperties.Builder properties = createValueProperties();
    final Currency ccy = FinancialSecurityUtils.getCurrency(target.getPositionOrTrade().getSecurity());
    if (ccy != null) {
      properties.with(ValuePropertyNames.CURRENCY, ccy.getCode());
    }
    return properties;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(getResultValueRequirementName(), target.toSpecification(), createValueProperties(target).get()));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    if (inputs.isEmpty()) {
      return null;
    }
    UniqueId uidMarkToMarket = null;
    UniqueId uidCostOfCarry = null;
    for (final Map.Entry<ValueSpecification, ValueRequirement> input : inputs.entrySet()) {
      if (ValueRequirementNames.HISTORICAL_TIME_SERIES.equals(input.getValue().getValueName())) {
        if ("MarkToMarket".equals(input.getValue().getConstraint(".name"))) {
          uidMarkToMarket = input.getKey().getTargetSpecification().getUniqueId();
        } else {
          uidCostOfCarry = input.getKey().getTargetSpecification().getUniqueId();
        }
      }
    }

    final Builder propertiesBuilder = createValueProperties(target);
    if (uidMarkToMarket != null) {
      propertiesBuilder.with("MarkToMarketTimeSeries", uidMarkToMarket.toString());
    }
    if (uidCostOfCarry != null) {
      propertiesBuilder.with("CostOfCarryTimeSeries",  uidCostOfCarry.toString());
    }
    return Collections.singleton(new ValueSpecification(getResultValueRequirementName(), target.toSpecification(), propertiesBuilder.get()));
  }

}
