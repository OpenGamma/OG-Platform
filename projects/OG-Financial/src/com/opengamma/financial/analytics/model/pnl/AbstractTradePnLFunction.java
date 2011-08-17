/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

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
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.TradeEquityPnLFunction;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MoneyCalculationUtil;

/**
 * 
 */
public abstract class AbstractTradePnLFunction extends AbstractFunction.NonCompiledInvoker {

  private static final Logger s_logger = LoggerFactory.getLogger(TradeEquityPnLFunction.class);
  
  private final String _markDataSource;
  private final String _markDataProvider;
  private final String _markDataField;
  private final String _costOfCarryField;
  
  /**
   * @param markDataSource the mark to market data source name, not-null
   * @param markDataProvider the mark to market data provider name
   * @param markDataField the mark to market data field name, not-null
   * @param costOfCarryField the cost of carry field name, not-null
   */
  public AbstractTradePnLFunction(String markDataSource, String markDataProvider, String markDataField, String costOfCarryField) {
    super();
    ArgumentChecker.notNull(markDataSource, "data source");
    ArgumentChecker.notNull(markDataField, "mark data field");
    ArgumentChecker.notNull(costOfCarryField, "cost of carry data field");
    
    _markDataSource = markDataSource;
    _markDataProvider = markDataProvider;
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
      final HistoricalTimeSeries markToMarketSeries = historicalSource.getHistoricalTimeSeries(security.getIdentifiers(), _markDataSource, _markDataProvider, _markDataField,
          tradeDate, true, tradeDate, false);
      
      if (markToMarketSeries == null || markToMarketSeries.getTimeSeries() == null) {
        throw new NullPointerException("Could not get identifier / mark to market series pair for security " + security + " for " + _markDataSource + "/" + _markDataProvider + "/" + _markDataField);
      }
      if (markToMarketSeries.getTimeSeries().isEmpty() || markToMarketSeries.getTimeSeries().getValue(tradeDate) == null) {
        throw new NullPointerException("Could not get mark to market value for security " + security + " for " + _markDataSource + "/" + _markDataProvider + "/" + _markDataField + "/" + tradeDate);
      }
      
      final ValueSpecification valueSpecification = new ValueSpecification(new ValueRequirement(ValueRequirementNames.PNL, trade), getUniqueId());
      
      double costOfCarry = 0.0;
      final HistoricalTimeSeries costOfCarryPair = historicalSource.getHistoricalTimeSeries(security.getIdentifiers(), _markDataSource, _markDataProvider, _costOfCarryField,
          tradeDate, true, tradeDate, false);
      if (costOfCarryPair != null && costOfCarryPair.getTimeSeries() != null && !costOfCarryPair.getTimeSeries().isEmpty()) {
        Double storedCostOfCarry = costOfCarryPair.getTimeSeries().getValue(tradeDate);
        if (storedCostOfCarry != null) {
          costOfCarry = storedCostOfCarry;
        }
      }
      Double markToMarket = markToMarketSeries.getTimeSeries().getValue(tradeDate);
      
      BigDecimal dailyPnL = trade.getQuantity().multiply(new BigDecimal(String.valueOf(tradeValue - markToMarket - costOfCarry)));
      s_logger.debug("{}  security: {} quantity: {} fairValue: {} markToMarket: {} costOfCarry: {} dailyPnL: {}", 
          new Object[]{trade.getUniqueId(), trade.getSecurity().getIdentifiers(), trade.getQuantity(), tradeValue, markToMarket, costOfCarry, dailyPnL});
      final ComputedValue result = new ComputedValue(valueSpecification, MoneyCalculationUtil.rounded(dailyPnL));
      return Sets.newHashSet(result);
    }
    return null;
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
  protected abstract String getValueRequirementName();

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    if (canApplyTo(context, target)) {
      return Sets.newHashSet(new ValueSpecification(new ValueRequirement(ValueRequirementNames.PNL, target.getTrade()), getUniqueId()));
    }
    return null;
  }
  
}
