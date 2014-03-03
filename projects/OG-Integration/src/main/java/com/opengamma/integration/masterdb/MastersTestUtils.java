/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.masterdb;

import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.exchange.ExchangeSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.legalentity.LegalEntitySource;
import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.analytics.ircurve.YieldCurveConfigPopulator;
import com.opengamma.financial.analytics.volatility.surface.FXOptionVolatilitySurfaceConfigPopulator;
import com.opengamma.financial.analytics.volatility.surface.IRFutureOptionSurfaceConfigPopulator;
import com.opengamma.financial.currency.CurrencyMatrixConfigPopulator;
import com.opengamma.financial.currency.CurrencyPairsConfigPopulator;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.impl.InMemoryConfigMaster;
import com.opengamma.master.config.impl.MasterConfigSource;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.master.exchange.ExchangeMaster;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.SecurityMaster;

/**
 * Helper for building a test framework.
 */
public abstract class MastersTestUtils {

  private final ConfigMaster _memConfigMaster;
  private final MasterConfigSource _memConfigSource;

  public MastersTestUtils() {
    final InMemoryConfigMaster cfgMaster = new InMemoryConfigMaster();
    YieldCurveConfigPopulator.populateCurveConfigMaster(cfgMaster);
    CurrencyPairsConfigPopulator.populateCurrencyPairsConfigMaster(cfgMaster);
    CurrencyMatrixConfigPopulator.populateCurrencyMatrixConfigMaster(cfgMaster);
    FXOptionVolatilitySurfaceConfigPopulator.populateVolatilitySurfaceConfigMaster(cfgMaster);
    IRFutureOptionSurfaceConfigPopulator.populateVolatilitySurfaceConfigMaster(cfgMaster);
    _memConfigMaster = cfgMaster;
    _memConfigSource = new MasterConfigSource(cfgMaster);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the config master.
   *
   * @return the config master, not null
   */
  public ConfigMaster getTestConfigMaster() {
    return _memConfigMaster;
  }

  /**
   * Gets the config source.
   *
   * @return the config source, not null
   */
  public MasterConfigSource getTestConfigSource() {
    return _memConfigSource;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the config master.
   *
   * @return the config master, not null
   */
  public abstract ConfigMaster getConfigMaster();

  //-------------------------------------------------------------------------
  /**
   * Gets the region source.
   *
   * @return the region source, not null
   */
  public abstract RegionSource getRegionSource();

  //-------------------------------------------------------------------------
  /**
   * Gets the organization source.
   *
   * @return the organization source, not null
   */
  public abstract LegalEntitySource getLegalEntitySource();

  //-------------------------------------------------------------------------
  /**
   * Gets the security source.
   *
   * @return the security source, not null
   */
  public abstract SecuritySource getSecuritySource();

  /**
   * Gets the security master.
   *
   * @return the security master, not null
   */
  public abstract SecurityMaster getSecurityMaster();

  //-------------------------------------------------------------------------
  /**
   * Gets the convention source.
   *
   * @return the convention source, not null
   */
  public abstract ConventionSource getConventionSource();

  /**
   * Gets the convention master.
   *
   * @return the convention master, not null
   */
  public abstract ConventionMaster getConventionMaster();

  //-------------------------------------------------------------------------
  /**
   * Gets the snapshot source.
   *
   * @return the snapshot source, not null
   */
  public abstract MarketDataSnapshotSource getSnapshotSource();

  /**
   * Gets the snapshot master.
   *
   * @return the snapshot master, not null
   */
  public abstract MarketDataSnapshotMaster getMarketDataSnapshotMaster();

  //-------------------------------------------------------------------------
  /**
   * Gets the exchange source.
   *
   * @return the exchange source, not null
   */
  public abstract ExchangeSource getExchangeSource();

  /**
   * Gets the exchange master.
   *
   * @return the exchange master, not null
   */
  public abstract ExchangeMaster getExchangeMaster();

  //-------------------------------------------------------------------------
  /**
   * Gets the holiday source.
   *
   * @return the holiday source, not null
   */
  public abstract HolidaySource getHolidaySource();

  /**
   * Gets the holiday master.
   *
   * @return the holiday master, not null
   */
  public abstract HolidayMaster getHolidayMaster();

  //-------------------------------------------------------------------------
  /**
   * Gets the portfolio master.
   *
   * @return the portfolio master, not null
   */
  public abstract PortfolioMaster getPortfolioMaster();

  //-------------------------------------------------------------------------
  /**
   * Gets the position source.
   *
   * @return the position source
   */
  public abstract PositionSource getPositionSource();

  /**
   * Gets the position master.
   *
   * @return the position master, not null
   */
  public abstract PositionMaster getPositionMaster();

  //-------------------------------------------------------------------------
  /**
   * Gets the time-series master.
   *
   * @return the time-series master, not null
   */
  public abstract HistoricalTimeSeriesMaster getHistoricalTimeSeriesMaster();

}
