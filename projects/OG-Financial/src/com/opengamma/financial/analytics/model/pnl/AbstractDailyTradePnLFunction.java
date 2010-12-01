/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import javax.time.Instant;
import javax.time.calendar.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.core.historicaldata.HistoricalDataSource;
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
import com.opengamma.financial.analytics.model.equity.DailyTradeEquityPnLFunction;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MoneyCalculationUtil;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public abstract class AbstractDailyTradePnLFunction extends AbstractFunction.NonCompiledInvoker {

  private static final Logger s_logger = LoggerFactory.getLogger(DailyTradeEquityPnLFunction.class);
  
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
  public AbstractDailyTradePnLFunction(String markDataSource, String markDataProvider, String markDataField, String costOfCarryField) {
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
    final Object currentTradeValue = inputs.getValue(new ValueRequirement(getValueRequirementName(), ComputationTargetType.SECURITY, trade.getSecurity().getUniqueIdentifier()));
    if (currentTradeValue != null) {
      final Double tradeValue = (Double) currentTradeValue;
      final Security security = trade.getSecurity();
      Instant tradeInstant = trade.getTradeInstant();
      LocalDate tradeDate = LocalDate.ofEpochDays(tradeInstant.getEpochSeconds() / DateUtil.SECONDS_PER_DAY);
      
      final HistoricalDataSource historicalDataSource = OpenGammaExecutionContext.getHistoricalDataSource(executionContext);
      final Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> markToMarketSeries = historicalDataSource.getHistoricalData(security.getIdentifiers(), _markDataSource, _markDataProvider, _markDataField,
          tradeDate, true, tradeDate, false);
      
      final ValueSpecification valueSpecification = new ValueSpecification(new ValueRequirement(ValueRequirementNames.DAILY_PNL, trade), getUniqueIdentifier());
      
      if (markToMarketSeries == null) {
        s_logger.warn("Could not get identifier / mark to market series pair for security {}", security.getIdentifiers());
        return Sets.newHashSet(new ComputedValue(valueSpecification, MoneyCalculationUtil.rounded(new BigDecimal("0.00"))));
      }
      LocalDateDoubleTimeSeries ts = markToMarketSeries.getSecond();
      if (ts == null) {
        s_logger.warn("Could not get mark to market series for security {}", security.getIdentifiers());
        return Sets.newHashSet(new ComputedValue(valueSpecification, MoneyCalculationUtil.rounded(new BigDecimal("0.00"))));
      }
      
      double costOfCarry = 0.0;
      final Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> costOfCarryPair = historicalDataSource.getHistoricalData(security.getIdentifiers(), _markDataSource, _markDataProvider, _costOfCarryField,
          tradeDate, true, tradeDate, false);
      if (costOfCarryPair != null && costOfCarryPair.getSecond() != null && !costOfCarryPair.getSecond().isEmpty()) {
        Double storedCostOfCarry = costOfCarryPair.getSecond().getValue(tradeDate);
        if (storedCostOfCarry != null) {
          costOfCarry = storedCostOfCarry;
        }
      }
      
      Double markToMarket = tradeValue;
      if (!ts.isEmpty()) {
        markToMarket = ts.getValue(tradeDate);
        if (markToMarket == null) {
          s_logger.warn("Could not get mark to market price for security {} for {}", security.getIdentifiers(), tradeDate);
          return Sets.newHashSet(new ComputedValue(valueSpecification, MoneyCalculationUtil.rounded(new BigDecimal("0.00"))));
        }
      } else {
        s_logger.warn("Could not get mark to market price for security {} for {}", security.getIdentifiers(), tradeDate);
        return Sets.newHashSet(new ComputedValue(valueSpecification, MoneyCalculationUtil.rounded(new BigDecimal("0.00"))));
      }
      
      BigDecimal dailyPnL = trade.getQuantity().multiply(new BigDecimal(String.valueOf(tradeValue - markToMarket - costOfCarry)));
      s_logger.debug("{}  security: {} quantity: {} fairValue: {} markToMarket: {} costOfCarry: {} dailyPnL: {}", 
          new Object[]{trade.getUniqueIdentifier(), trade.getSecurity().getIdentifiers(), trade.getQuantity(), tradeValue, markToMarket, costOfCarry, dailyPnL});
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
      requirements.add(new ValueRequirement(getValueRequirementName(), ComputationTargetType.SECURITY, security.getUniqueIdentifier()));
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
      return Sets.newHashSet(new ValueSpecification(new ValueRequirement(ValueRequirementNames.DAILY_PNL, target.getTrade()), getUniqueIdentifier()));
    }
    return null;
  }
  
}
