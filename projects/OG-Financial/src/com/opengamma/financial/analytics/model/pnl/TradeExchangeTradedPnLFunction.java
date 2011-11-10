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
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.id.ExternalIdBundle;

/**
 * 
 */
public class TradeExchangeTradedPnLFunction extends AbstractTradeOrDailyPositionPnLFunction {
  
  /**
   * @param resolutionKey the resolution key, not-null
   * @param markDataField the mark to market data field name, not-null
   * @param costOfCarryField the cost of carry field name, not-null
   */
  public TradeExchangeTradedPnLFunction(String resolutionKey, String markDataField, String costOfCarryField) {
    super(resolutionKey, markDataField, costOfCarryField);
  }

  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    Security security = target.getTrade().getSecurity();
    return (target.getType() == ComputationTargetType.TRADE && FinancialSecurityUtils.isExchangedTraded(security));
  }

  @Override
  public String getShortName() {
    return "TradePnL";
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.TRADE;
  }

  @Override
  protected LocalDate getPreferredTradeDate(Clock valuationClock, PositionOrTrade positionOrTrade) {
    return ((Trade) positionOrTrade).getTradeDate();
  }

  @Override
  protected HistoricalTimeSeries getMarkToMarketSeries(HistoricalTimeSeriesSource historicalSource, String fieldName, ExternalIdBundle bundle, String resolutionKey, LocalDate tradeDate) {
    return historicalSource.getHistoricalTimeSeries(fieldName, bundle, resolutionKey, tradeDate, true, tradeDate, true);
  }

  @Override
  protected LocalDate checkAvailableData(LocalDate originalTradeDate, HistoricalTimeSeries markToMarketSeries, Security security, 
                                         String markDataField, String resolutionKey) {
    if (markToMarketSeries.getTimeSeries().isEmpty() || markToMarketSeries.getTimeSeries().getValue(originalTradeDate) == null) {
      throw new NullPointerException("Could not get mark to market value for security " + 
          security.getExternalIdBundle() + " for " + markDataField + " using " + resolutionKey + " for " + originalTradeDate);
    }
    return originalTradeDate;
  }

  @Override
  protected String getResultValueRequirementName() {
    return ValueRequirementNames.PNL;
  }


}
