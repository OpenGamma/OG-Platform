/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import org.threeten.bp.Clock;
import org.threeten.bp.LocalDate;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.position.PositionOrTrade;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.timeseries.DateConstraint;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;

/**
 * 
 */
public class TradeExchangeTradedPnLFunction extends AbstractTradeOrDailyPositionPnLFunction {
  
  /**
   * @param resolutionKey the resolution key, not-null
   * @param mark2marketField the mark to market data field name, not-null
   * @param costOfCarryField the cost of carry field name, not-null
   */
  public TradeExchangeTradedPnLFunction(String resolutionKey, String mark2marketField, String costOfCarryField) {
    super(resolutionKey, mark2marketField, costOfCarryField);
  }

  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    if (!super.canApplyTo(context, target)) {
      return false;
    }
    Security security = target.getTrade().getSecurity();
    if (security instanceof FXForwardSecurity || security instanceof FXOptionSecurity || security instanceof FXBarrierOptionSecurity || security instanceof FXDigitalOptionSecurity) {
      return false;
    }
    return FinancialSecurityUtils.isExchangeTraded(security) || (security instanceof BondSecurity);
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
  protected DateConstraint getTimeSeriesStartDate(final PositionOrTrade positionOrTrade) {
    return DateConstraint.of(((Trade) positionOrTrade).getTradeDate());
  }

  @Override
  protected DateConstraint getTimeSeriesEndDate(final PositionOrTrade positionOrTrade) {
    return DateConstraint.of(((Trade) positionOrTrade).getTradeDate());
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
