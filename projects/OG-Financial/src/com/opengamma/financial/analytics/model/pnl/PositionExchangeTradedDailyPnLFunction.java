/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import javax.time.calendar.Clock;
import javax.time.calendar.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class PositionExchangeTradedDailyPnLFunction extends AbstractTradeOrDailyPositionPnLFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(PositionExchangeTradedDailyPnLFunction.class);
  private static final long MAX_DAYS_OLD = 70;
  
  /**
   * @param resolutionKey the resolution key, not-null
   * @param markDataField the mark to market data field name, not-null
   * @param costOfCarryField the cost of carry field name, not-null
   */
  public PositionExchangeTradedDailyPnLFunction(String resolutionKey, String markDataField, String costOfCarryField) {
    super(resolutionKey, markDataField, costOfCarryField);
  }

  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    Security security = target.getPositionOrTrade().getSecurity();
    return (target.getType() == ComputationTargetType.POSITION && (FinancialSecurityUtils.isExchangedTraded(security)) || security instanceof BondSecurity);
  }

  @Override
  public String getShortName() {
    return "TradeDailyPnL";
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

  @Override
  protected LocalDate getPreferredTradeDate(Clock valuationClock, PositionOrTrade positionOrTrade) {
    return valuationClock.yesterday();
  }

  @Override
  protected HistoricalTimeSeries getMarkToMarketSeries(HistoricalTimeSeriesSource historicalSource, String fieldName, ExternalIdBundle bundle, String resolutionKey, LocalDate tradeDate) {
    LocalDate from = tradeDate.minusDays(MAX_DAYS_OLD);
    HistoricalTimeSeries hts = historicalSource.getHistoricalTimeSeries(fieldName, bundle, resolutionKey,
                                                    from, true, tradeDate, true);
    if (hts == null || hts.getTimeSeries() == null) {
      s_logger.debug("Could not get identifier / mark to market series pair for security {} for {} using {} from {} to {}",
                    new Object[] {bundle, fieldName, resolutionKey, from, tradeDate});
    }
    return hts;
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
