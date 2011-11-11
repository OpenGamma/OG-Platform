/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.time.calendar.Clock;
import javax.time.calendar.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
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
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MoneyCalculationUtils;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public abstract class AbstractTradeOrDailyPositionPnLFunction extends AbstractFunction.NonCompiledInvoker {

  private static final Logger s_logger = LoggerFactory.getLogger(AbstractTradeOrDailyPositionPnLFunction.class);

  private static final Double UN_AVAILABLE_COST = Double.NaN;

  private final String _markDataField;
  private final String _costOfCarryField;
  private final String _resolutionKey;
  
  
  private final Map<Pair<ExternalIdBundle, LocalDate>, Double> _costOfCarryCache = new ConcurrentHashMap<Pair<ExternalIdBundle, LocalDate>, Double>();

  /**
   * @param resolutionKey the resolution key, not-null
   * @param markDataField the mark to market data field name, not-null
   * @param costOfCarryField the cost of carry field name, not-null
   */
  public AbstractTradeOrDailyPositionPnLFunction(String resolutionKey, String markDataField, String costOfCarryField) {
    super();
    ArgumentChecker.notNull(resolutionKey, "resolutionKey");
    ArgumentChecker.notNull(markDataField, "mark data field");
    ArgumentChecker.notNull(costOfCarryField, "cost of carry data field");
    
    _resolutionKey = resolutionKey;
    _markDataField = markDataField;
    _costOfCarryField = costOfCarryField;
  }
  
  protected abstract LocalDate getPreferredTradeDate(Clock valuationClock, PositionOrTrade positionOrTrade);
  
  protected abstract HistoricalTimeSeries getMarkToMarketSeries(HistoricalTimeSeriesSource historicalSource, String fieldName, ExternalIdBundle bundle, String resolutionKey, LocalDate tradeDate);
  
  protected abstract LocalDate checkAvailableData(LocalDate originalTradeDate, HistoricalTimeSeries markToMarketSeries, Security security, String markDataField, String resolutionKey);
  
  protected abstract String getResultValueRequirementName();
  
  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) {
    final PositionOrTrade trade = target.getPositionOrTrade();
    final Object currentTradeValue = inputs.getValue(new ValueRequirement(getValueRequirementName(), ComputationTargetType.SECURITY, trade.getSecurity().getUniqueId()));
    if (currentTradeValue != null) {
      final Double tradeValue = (Double) currentTradeValue;
      final Security security = trade.getSecurity();
      
      LocalDate tradeDate = getPreferredTradeDate(executionContext.getValuationClock(), trade);

      final HistoricalTimeSeriesSource historicalSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
      final HistoricalTimeSeries markToMarketSeries = getMarkToMarketSeries(historicalSource, _markDataField, security.getExternalIdBundle(), _resolutionKey, tradeDate);

      if (markToMarketSeries == null || markToMarketSeries.getTimeSeries() == null) {
        s_logger.debug("Could not get identifier / mark to market series pair for security {} for {} using {}",
            new Object[]{security.getExternalIdBundle(), _markDataField, _resolutionKey});
        return Collections.emptySet();
      }
      
      tradeDate = checkAvailableData(tradeDate, markToMarketSeries, security, _markDataField, _resolutionKey);

      final Currency ccy = FinancialSecurityUtils.getCurrency(trade.getSecurity());
      final String valueRequirementName = getResultValueRequirementName();
      final ValueSpecification valueSpecification;
      if (ccy == null) {
        valueSpecification = new ValueSpecification(new ValueRequirement(valueRequirementName, trade), getUniqueId());
      } else {
        valueSpecification = new ValueSpecification(new ValueRequirement(valueRequirementName, trade, ValueProperties.with(ValuePropertyNames.CURRENCY, ccy.getCode()).get()), getUniqueId());
      }

      double costOfCarry = getCostOfCarry(security, tradeDate, historicalSource);
      Double markToMarket = markToMarketSeries.getTimeSeries().getValue(tradeDate);

      BigDecimal dailyPnL = trade.getQuantity().multiply(new BigDecimal(String.valueOf(tradeValue - markToMarket - costOfCarry)));
      s_logger.debug("{}  security: {} quantity: {} fairValue: {} markToMarket: {} costOfCarry: {} dailyPnL: {}",
          new Object[] {trade.getUniqueId(), trade.getSecurity().getExternalIdBundle(), trade.getQuantity(), tradeValue, markToMarket, costOfCarry, dailyPnL });
      final ComputedValue result = new ComputedValue(valueSpecification, MoneyCalculationUtils.rounded(dailyPnL).doubleValue());
      return Sets.newHashSet(result);
    }
    return null;
  }

  private double getCostOfCarry(final Security security, LocalDate tradeDate, final HistoricalTimeSeriesSource historicalSource) {
    Double cachedCost = _costOfCarryCache.get(Pair.of(security.getExternalIdBundle(), tradeDate));
    if (cachedCost == null) {
      cachedCost = UN_AVAILABLE_COST;
      final HistoricalTimeSeries costOfCarryPair = historicalSource.getHistoricalTimeSeries(_costOfCarryField, security.getExternalIdBundle(), _resolutionKey,
          tradeDate, true, tradeDate, true);
      if (costOfCarryPair != null && costOfCarryPair.getTimeSeries() != null && !costOfCarryPair.getTimeSeries().isEmpty()) {
        Double histCost = costOfCarryPair.getTimeSeries().getValue(tradeDate);
        if (histCost != null) {
          cachedCost = histCost;
        }
      }
      _costOfCarryCache.put(Pair.of(security.getExternalIdBundle(), tradeDate), cachedCost);
    }
    return cachedCost == UN_AVAILABLE_COST ? 0.0d : cachedCost;
  }

//  @Override
//  public ComputationTargetType getTargetType() {
//    return _dailyPositionPnL ? ComputationTargetType.POSITION : ComputationTargetType.TRADE;
//  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    if (canApplyTo(context, target)) {
      final PositionOrTrade positionOrTrade = target.getPositionOrTrade();
      final Security security = positionOrTrade.getSecurity();
      final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
      requirements.add(new ValueRequirement(getValueRequirementName(), ComputationTargetType.SECURITY, security.getUniqueId()));
      return requirements;
    }
    return null;
  }

  /**
   * @return the value requirement name
   */
  protected String getValueRequirementName() {
    return ValueRequirementNames.FAIR_VALUE;
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    if (canApplyTo(context, target)) {
      Currency ccy = FinancialSecurityUtils.getCurrency(target.getPositionOrTrade().getSecurity());
      final String valueRequirementName = getResultValueRequirementName();
      if (ccy == null) {
        return Sets.newHashSet(new ValueSpecification(new ValueRequirement(valueRequirementName, target.getPositionOrTrade()), getUniqueId()));
      } else {
        return Sets.newHashSet(new ValueSpecification(new ValueRequirement(valueRequirementName, target.getPositionOrTrade(),
                                 ValueProperties.with(ValuePropertyNames.CURRENCY, ccy.getCode()).get()), getUniqueId()));
      }
    }
    return null;
  }

}
