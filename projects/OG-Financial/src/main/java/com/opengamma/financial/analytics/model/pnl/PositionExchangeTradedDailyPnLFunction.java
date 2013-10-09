/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import org.threeten.bp.Clock;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.position.PositionOrTrade;
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
public class PositionExchangeTradedDailyPnLFunction extends AbstractTradeOrDailyPositionPnLFunction {

  private static final int MAX_DAYS_OLD = 70;

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
    if (!super.canApplyTo(context, target)) {
      return false;
    }
    Security security = target.getPositionOrTrade().getSecurity();
    if (security instanceof FXForwardSecurity || security instanceof FXOptionSecurity || security instanceof FXBarrierOptionSecurity || security instanceof FXDigitalOptionSecurity) {
      return false;
    }
    try {
      return FinancialSecurityUtils.isExchangeTraded(security) || (security instanceof BondSecurity);
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public String getShortName() {
    return "PositionDailyPnL";
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

  @Override
  protected LocalDate getPreferredTradeDate(Clock valuationClock, PositionOrTrade positionOrTrade) {
    return LocalDate.now(valuationClock).minusDays(1);
  }

  @Override
  protected DateConstraint getTimeSeriesStartDate(final PositionOrTrade positionOrTrade) {
    return DateConstraint.VALUATION_TIME.minus(Period.ofDays(MAX_DAYS_OLD + 1)); // yesterday - MAX_DAYS_OLD
  }

  @Override
  protected DateConstraint getTimeSeriesEndDate(final PositionOrTrade positionOrTrade) {
    return DateConstraint.VALUATION_TIME.minus(Period.ofDays(1));
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
