/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool;

import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.config.impl.DataTrackingConfigMaster;
import com.opengamma.master.convention.impl.DataTrackingConventionMaster;
import com.opengamma.master.exchange.impl.DataTrackingExchangeMaster;
import com.opengamma.master.historicaltimeseries.impl.DataTrackingHistoricalTimeSeriesMaster;
import com.opengamma.master.holiday.impl.DataTrackingHolidayMaster;
import com.opengamma.master.legalentity.impl.DataTrackingLegalEntityMaster;
import com.opengamma.master.marketdatasnapshot.impl.DataTrackingMarketDataSnapshotMaster;
import com.opengamma.master.portfolio.impl.DataTrackingPortfolioMaster;
import com.opengamma.master.position.impl.DataTrackingPositionMaster;
import com.opengamma.master.security.impl.DataTrackingSecurityMaster;

/**
 * A tool context which provides DataTracking masters.
 */
public class DataTrackingToolContext extends ToolContext {

  @Override
  public DataTrackingConfigMaster getConfigMaster() {
    return (DataTrackingConfigMaster) super.getConfigMaster();
  }

  @Override
  public DataTrackingExchangeMaster getExchangeMaster() {
    return (DataTrackingExchangeMaster) super.getExchangeMaster();
  }

  @Override
  public DataTrackingHolidayMaster getHolidayMaster() {
    return (DataTrackingHolidayMaster) super.getHolidayMaster();
  }

  @Override
  public DataTrackingPositionMaster getPositionMaster() {
    return (DataTrackingPositionMaster) super.getPositionMaster();
  }

  @Override
  public DataTrackingPortfolioMaster getPortfolioMaster() {
    return (DataTrackingPortfolioMaster) super.getPortfolioMaster();
  }

  @Override
  public DataTrackingSecurityMaster getSecurityMaster() {
    return (DataTrackingSecurityMaster) super.getSecurityMaster();
  }

  @Override
  public DataTrackingLegalEntityMaster getLegalEntityMaster() {
    return (DataTrackingLegalEntityMaster) super.getLegalEntityMaster();
  }

  @Override
  public DataTrackingHistoricalTimeSeriesMaster getHistoricalTimeSeriesMaster() {
    return (DataTrackingHistoricalTimeSeriesMaster) super.getHistoricalTimeSeriesMaster();
  }

  @Override
  public DataTrackingMarketDataSnapshotMaster getMarketDataSnapshotMaster() {
    return (DataTrackingMarketDataSnapshotMaster) super.getMarketDataSnapshotMaster();
  }

  @Override
  public DataTrackingConventionMaster getConventionMaster() {
    return (DataTrackingConventionMaster) super.getConventionMaster();
  }
  
}
