/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import javax.time.calendar.Clock;
import javax.time.calendar.LocalDate;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.position.PositionOrTrade;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.id.ExternalIdBundle;

/**
 * 
 */
public class TradeExchangeTradedDailyPnLFunction extends AbstractTradeOrDailyPositionPnLFunction {
  
  private static final long MAX_DAYS_OLD = 7;
  
  /**
   * @param resolutionKey the resolution key, not-null
   * @param markDataField the mark to market data field name, not-null
   * @param costOfCarryField the cost of carry field name, not-null
   */
  public TradeExchangeTradedDailyPnLFunction(String resolutionKey, String markDataField, String costOfCarryField) {
    super(resolutionKey, markDataField, costOfCarryField);
  }

  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    Security security = target.getTrade().getSecurity();
    return (target.getType() == ComputationTargetType.TRADE && (FinancialSecurityUtils.isExchangedTraded(security)) || security instanceof BondSecurity);
  }

  @Override
  public String getShortName() {
    return "TradeDailyPnL";
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.TRADE;
  }

  @Override
  protected LocalDate getPreferredTradeDate(Clock valuationClock, PositionOrTrade positionOrTrade) {
    return valuationClock.yesterday();
  }

  @Override
  protected HistoricalTimeSeries getMarkToMarketSeries(HistoricalTimeSeriesSource historicalSource, String fieldName, ExternalIdBundle bundle, String resolutionKey, LocalDate tradeDate) {
    return historicalSource.getHistoricalTimeSeries(fieldName, bundle, resolutionKey,
                                                    tradeDate.minusDays(MAX_DAYS_OLD), true, tradeDate, true);
  }

  @Override
  protected LocalDate checkAvailableData(LocalDate originalTradeDate, HistoricalTimeSeries markToMarketSeries, Security security, String markDataField, String resolutionKey) {
    if (markToMarketSeries.getTimeSeries().isEmpty() || markToMarketSeries.getTimeSeries().getLatestValue() == null) {
      throw new NullPointerException("Could not get mark to market value for security " + 
          security.getExternalIdBundle() + " for " + markDataField + " using " + resolutionKey + " for " + MAX_DAYS_OLD + " back from " + originalTradeDate);          
    } else {
      return markToMarketSeries.getTimeSeries().getLatestTime();
    }
  }


  @Override
  protected String getResultValueRequirementName() {
    return ValueRequirementNames.DAILY_PNL;
  }



}
