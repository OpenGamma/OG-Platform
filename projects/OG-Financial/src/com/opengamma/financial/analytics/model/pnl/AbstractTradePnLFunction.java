/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.time.calendar.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.position.Trade;
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
public abstract class AbstractTradePnLFunction extends AbstractFunction.NonCompiledInvoker {

  private static final Logger s_logger = LoggerFactory.getLogger(AbstractTradePnLFunction.class);
  
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
  public AbstractTradePnLFunction(String resolutionKey, String markDataField, String costOfCarryField) {
    super();
    ArgumentChecker.notNull(resolutionKey, "resolutionKey");
    ArgumentChecker.notNull(markDataField, "mark data field");
    ArgumentChecker.notNull(costOfCarryField, "cost of carry data field");
    
    _resolutionKey = resolutionKey;
    _markDataField = markDataField;
    _costOfCarryField = costOfCarryField;
  }

  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) {
    final Trade trade = target.getTrade();
    final Object currentTradeValue = inputs.getValue(new ValueRequirement(getValueRequirementName(), ComputationTargetType.SECURITY, trade.getSecurity().getUniqueId()));
    if (currentTradeValue != null) {
      final Double tradeValue = (Double) currentTradeValue;
      final Security security = trade.getSecurity();
      LocalDate tradeDate = trade.getTradeDate();
      
      final HistoricalTimeSeriesSource historicalSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
      final HistoricalTimeSeries markToMarketSeries = historicalSource.getHistoricalTimeSeries(_markDataField, security.getIdentifiers(), _resolutionKey,
          tradeDate, true, tradeDate, false);
      
      if (markToMarketSeries == null || markToMarketSeries.getTimeSeries() == null) {
        throw new NullPointerException("Could not get identifier / mark to market series pair for security " + security.getIdentifiers() + " for " + _markDataField + " using " + _resolutionKey);
      }
      if (markToMarketSeries.getTimeSeries().isEmpty() || markToMarketSeries.getTimeSeries().getValue(tradeDate) == null) {
        throw new NullPointerException("Could not get mark to market value for security " + security.getIdentifiers() + " for " + _markDataField + " using " + _resolutionKey + " for " + tradeDate);
      }
      final Currency ccy = FinancialSecurityUtils.getCurrency(trade.getSecurity());
      final ValueSpecification valueSpecification;
      if (ccy == null) {
        valueSpecification = new ValueSpecification(new ValueRequirement(ValueRequirementNames.PNL, trade), getUniqueId());
      } else {
        valueSpecification = new ValueSpecification(new ValueRequirement(ValueRequirementNames.PNL, trade, ValueProperties.with(ValuePropertyNames.CURRENCY, ccy.getCode()).get()), getUniqueId());
      }
      
      double costOfCarry = getCostOfCarry(security, tradeDate, historicalSource);
      Double markToMarket = markToMarketSeries.getTimeSeries().getValue(tradeDate);
      
      BigDecimal dailyPnL = trade.getQuantity().multiply(new BigDecimal(String.valueOf(tradeValue - markToMarket - costOfCarry)));
      s_logger.debug("{}  security: {} quantity: {} fairValue: {} markToMarket: {} costOfCarry: {} dailyPnL: {}", 
          new Object[]{trade.getUniqueId(), trade.getSecurity().getIdentifiers(), trade.getQuantity(), tradeValue, markToMarket, costOfCarry, dailyPnL});
      final ComputedValue result = new ComputedValue(valueSpecification, MoneyCalculationUtils.rounded(dailyPnL).doubleValue());
      return Sets.newHashSet(result);
    }
    return null;
  }

  private double getCostOfCarry(final Security security, LocalDate tradeDate, final HistoricalTimeSeriesSource historicalSource) {
    Double cachedCost = _costOfCarryCache.get(Pair.of(security.getIdentifiers(), tradeDate));
    if (cachedCost == null) {
      cachedCost = UN_AVAILABLE_COST;
      final HistoricalTimeSeries costOfCarryPair = historicalSource.getHistoricalTimeSeries(_costOfCarryField, security.getIdentifiers(), _resolutionKey,
          tradeDate, true, tradeDate, false);
      if (costOfCarryPair != null && costOfCarryPair.getTimeSeries() != null && !costOfCarryPair.getTimeSeries().isEmpty()) {
        Double histCost = costOfCarryPair.getTimeSeries().getValue(tradeDate);
        if (histCost != null) {
          cachedCost = histCost;
        }         
      }
      _costOfCarryCache.put(Pair.of(security.getIdentifiers(), tradeDate), cachedCost);
    }
    return cachedCost == UN_AVAILABLE_COST ? 0.0d : cachedCost;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.TRADE;
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    if (canApplyTo(context, target)) {
      final Trade trade = target.getTrade();
      final Security security = trade.getSecurity();
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
      Currency ccy = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity());
      if (ccy == null) {
        return Sets.newHashSet(new ValueSpecification(new ValueRequirement(ValueRequirementNames.PNL, target.getTrade()), getUniqueId()));
      } else {
        return Sets.newHashSet(new ValueSpecification(new ValueRequirement(ValueRequirementNames.PNL, target.getTrade(), 
                                 ValueProperties.with(ValuePropertyNames.CURRENCY, ccy.getCode()).get()), getUniqueId()));
      }
    }
    return null;
  }
  
}
