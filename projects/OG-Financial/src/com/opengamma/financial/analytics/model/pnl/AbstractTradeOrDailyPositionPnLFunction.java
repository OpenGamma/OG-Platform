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

import javax.time.calendar.Clock;
import javax.time.calendar.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.position.PositionOrTrade;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueProperties.Builder;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.timeseries.DateConstraint;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.fx.FXUtils;
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
  public AbstractTradeOrDailyPositionPnLFunction(String resolutionKey, String mark2MarketField, String costOfCarryField) {
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
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    Double tradeValue = null;
    HistoricalTimeSeries htsMarkToMarket = null;
    HistoricalTimeSeries htsCostOfCarry = null;
    for (ComputedValue input : inputs.getAllValues()) {
      if (ValueRequirementNames.VALUE.equals(input.getSpecification().getValueName())) {
        tradeValue = (Double) input.getValue();
      } else if (ValueRequirementNames.HISTORICAL_TIME_SERIES.equals(input.getSpecification().getValueName())) {
        final String field = input.getSpecification().getProperty(HistoricalTimeSeriesFunctionUtils.DATA_FIELD_PROPERTY);
        if (_costOfCarryField.equals(field)) {
          htsCostOfCarry = (HistoricalTimeSeries) input.getValue();
        } else if (_mark2MarketField.equals(field)) {
          htsMarkToMarket = (HistoricalTimeSeries) input.getValue();
        }
      }
    }
    final PositionOrTrade trade = target.getPositionOrTrade();
    final Security security = trade.getSecurity();
    LocalDate tradeDate = getPreferredTradeDate(executionContext.getValuationClock(), trade);
    tradeDate = checkAvailableData(tradeDate, htsMarkToMarket, security, _mark2MarketField, _resolutionKey);
    final ValueSpecification valueSpecification = new ValueSpecification(getResultValueRequirementName(), desiredValue.getTargetSpecification(), desiredValue.getConstraints());
    double costOfCarry = getCostOfCarry(security, tradeDate, htsCostOfCarry);
    Double markToMarket = htsMarkToMarket.getTimeSeries().getValue(tradeDate);
    if (security instanceof FutureSecurity) {
      FutureSecurity futureSecurity = (FutureSecurity) security;
      markToMarket = markToMarket * futureSecurity.getUnitAmount();
    }
    final BigDecimal dailyPnL = trade.getQuantity().multiply(new BigDecimal(String.valueOf(tradeValue - markToMarket - costOfCarry)));
    s_logger.debug("{}  security: {} quantity: {} fairValue: {} markToMarket: {} costOfCarry: {} dailyPnL: {}",
          new Object[] {trade.getUniqueId(), trade.getSecurity().getExternalIdBundle(), trade.getQuantity(), tradeValue, markToMarket, costOfCarry, dailyPnL });
    final ComputedValue result = new ComputedValue(valueSpecification, MoneyCalculationUtils.rounded(dailyPnL).doubleValue());
    return Sets.newHashSet(result);
  }

  private double getCostOfCarry(final Security security, LocalDate tradeDate, final HistoricalTimeSeries costOfCarryTS) {
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
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    final PositionOrTrade positionOrTrade = target.getPositionOrTrade();
    final Security security = positionOrTrade.getSecurity();
    final ValueRequirement securityValue = new ValueRequirement(ValueRequirementNames.VALUE, ComputationTargetType.SECURITY, security.getUniqueId(), getCurrencyProperty(security));
    final HistoricalTimeSeriesResolver resolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
    final ExternalIdBundle bundle = security.getExternalIdBundle();
    final DateConstraint startDate = getTimeSeriesStartDate(positionOrTrade);
    final DateConstraint endDate = getTimeSeriesEndDate(positionOrTrade);
    final ValueRequirement markToMarketValue = getMarkToMarketSeriesRequirement(resolver, bundle, startDate, endDate);
    final ValueRequirement costOfCarryValue = getCostOfCarrySeriesRequirement(resolver, bundle, endDate);
    
    if (markToMarketValue == null && costOfCarryValue == null) {
      return null;
    }
    
    final Set<ValueRequirement> requirements = Sets.newHashSet(securityValue);
    if (markToMarketValue != null) {
      requirements.add(markToMarketValue);
    }
    if (costOfCarryValue != null) {
      requirements.add(costOfCarryValue);
    }
    return requirements;
  }

  protected ValueProperties getCurrencyProperty(Security security) {
    Currency ccy = FinancialSecurityUtils.getCurrency(security);
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
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(getResultValueRequirementName(), target.toSpecification(), createValueProperties(target).get()));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    if (inputs.isEmpty()) {
      return null;
    }
    UniqueId uidMarkToMarket = null;
    UniqueId uidCostOfCarry = null;
    for (ValueRequirement input : inputs.values()) {
      if (ValueRequirementNames.HISTORICAL_TIME_SERIES.equals(input.getValueName())) {
        if ("MarkToMarket".equals(input.getConstraint(".name"))) {
          uidMarkToMarket = input.getTargetSpecification().getUniqueId();
        } else {
          uidCostOfCarry = input.getTargetSpecification().getUniqueId();
        }
      }
    }
    
    Builder propertiesBuilder = createValueProperties(target);
    if (uidMarkToMarket != null) {
      propertiesBuilder.with("MarkToMarketTimeSeries", uidMarkToMarket.toString());
    }
    if (uidCostOfCarry != null) {
      propertiesBuilder.with("CostOfCarryTimeSeries",  uidCostOfCarry.toString());
    }
    return Collections.singleton(new ValueSpecification(getResultValueRequirementName(), target.toSpecification(), propertiesBuilder.get()));
  }

}
