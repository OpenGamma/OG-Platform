/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.masterdb;

import com.opengamma.component.tool.ToolContextUtils;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.exchange.ExchangeSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.legalentity.LegalEntitySource;
import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.master.exchange.ExchangeMaster;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.PlatformConfigUtils;

/**
 * Helper for testing a local engine.
 */
public final class LocalMastersTestUtils extends MastersTestUtils {

  /**
   * Single instance.
   */
  public static final LocalMastersTestUtils INSTANCE = new LocalMastersTestUtils();

  private ToolContext _toolContext;

  /**
   * Creates an instance.
   */
  LocalMastersTestUtils() {
    PlatformConfigUtils.configureSystemProperties();
    _toolContext = ToolContextUtils.getToolContext("classpath:/toolcontext/toolcontext-tests.properties",
        ToolContext.class);
  }

  public synchronized void tearDown() {
    if (_toolContext != null) {
      _toolContext.close();
      _toolContext = null;
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the config master.
   * 
   * @return the config master, not null
   */
  @Override
  public ConfigMaster getConfigMaster() {
    return _toolContext.getConfigMaster();
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the region source.
   * 
   * @return the region source, not null
   */
  @Override
  public RegionSource getRegionSource() {
    return _toolContext.getRegionSource();
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the organization source.
   * 
   * @return the organization source, not null
   */
  @Override
  public LegalEntitySource getLegalEntitySource() {
    return _toolContext.getLegalEntitySource();
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the security source.
   * 
   * @return the security source, not null
   */
  @Override
  public SecuritySource getSecuritySource() {
    return _toolContext.getSecuritySource();
  }

  /**
   * Gets the security master.
   * 
   * @return the security master, not null
   */
  @Override
  public SecurityMaster getSecurityMaster() {
    return _toolContext.getSecurityMaster();
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the convention source.
   *
   * @return the convention source, not null
   */
  public ConventionSource getConventionSource() {
    return _toolContext.getConventionSource();
  }

  /**
   * Gets the convention master.
   *
   * @return the convention master, not null
   */
  public ConventionMaster getConventionMaster() {
    return _toolContext.getConventionMaster();
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the snapshot source.
   * 
   * @return the snapshot source, not null
   */
  @Override
  public MarketDataSnapshotSource getSnapshotSource() {
    return _toolContext.getMarketDataSnapshotSource();
  }

  /**
   * Gets the snapshot master.
   * 
   * @return the snapshot master, not null
   */
  @Override
  public MarketDataSnapshotMaster getMarketDataSnapshotMaster() {
    return _toolContext.getMarketDataSnapshotMaster();
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the exchange source.
   * 
   * @return the exchange source, not null
   */
  @Override
  public ExchangeSource getExchangeSource() {
    return _toolContext.getExchangeSource();
  }

  /**
   * Gets the exchange master.
   * 
   * @return the exchange master, not null
   */
  @Override
  public ExchangeMaster getExchangeMaster() {
    return _toolContext.getExchangeMaster();
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the holiday source.
   * 
   * @return the holiday source, not null
   */
  @Override
  public HolidaySource getHolidaySource() {
    return _toolContext.getHolidaySource();
  }

  /**
   * Gets the holiday master.
   * 
   * @return the holiday master, not null
   */
  @Override
  public HolidayMaster getHolidayMaster() {
    return _toolContext.getHolidayMaster();
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the portfolio master.
   * 
   * @return the portfolio master, not null
   */
  @Override
  public PortfolioMaster getPortfolioMaster() {
    return _toolContext.getPortfolioMaster();
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the position source.
   * 
   * @return the position source, not null
   */
  @Override
  public PositionSource getPositionSource() {
    return _toolContext.getPositionSource();
  }

  /**
   * Gets the position master.
   * 
   * @return the position master, not null
   */
  @Override
  public PositionMaster getPositionMaster() {
    return _toolContext.getPositionMaster();
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the time-series master.
   * 
   * @return the time-series master, not null
   */
  @Override
  public HistoricalTimeSeriesMaster getHistoricalTimeSeriesMaster() {
    return _toolContext.getHistoricalTimeSeriesMaster();
  }

}
