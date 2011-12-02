/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.portfolio.loader;

import com.opengamma.core.exchange.ExchangeSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.SecurityLoader;
import com.opengamma.master.security.SecurityMaster;

/**
 * Represents the context required for populating the platform with data.
 */
public class LoaderContext {

  /**
   * The portfolio master.
   */
  private PortfolioMaster _portfolioMaster;
  /**
   * The position master.
   */
  private PositionMaster _positionMaster;
  /**
   * The security master.
   */
  private SecurityMaster _securityMaster;
  /**
   * The security loader.
   */
  private SecurityLoader _securityLoader;
  /**
   * The config master.
   */
  private ConfigMaster _configMaster;
  /**
   * The historical time-series source.
   */
  private HistoricalTimeSeriesSource _historicalTimeSeriesSource;
  /**
   * The historical time-series master.
   */
  private HistoricalTimeSeriesMaster _historicalTimeSeriesMaster;
  /**
   * The convention bundle source.
   */
  private ConventionBundleSource _conventionBundleSource;
  /**
   * The holiday source.
   */
  private HolidaySource _holidaySource;
  /**
   * The security source.
   */
  private SecuritySource _securitySource;
  /**
   * The exchange source.
   */
  private ExchangeSource _exchangeSource;
  
  //-------------------------------------------------------------------------
  /**
   * Gets the portfolio master.
   * @return the master, not null
   */
  public PortfolioMaster getPortfolioMaster() {
    return _portfolioMaster;
  }

  /**
   * Sets the portfolio master.
   * @param portfolioMaster  the master, not null
   */
  public void setPortfolioMaster(final PortfolioMaster portfolioMaster) {
    _portfolioMaster = portfolioMaster;
  }

  /**
   * Gets the position master.
   * @return the master, not null
   */
  public PositionMaster getPositionMaster() {
    return _positionMaster;
  }

  /**
   * Sets the position master.
   * @param positionMaster  the master, not null
   */
  public void setPositionMaster(final PositionMaster positionMaster) {
    _positionMaster = positionMaster;
  }

  /**
   * Gets the security master.
   * @return the master, not null
   */
  public SecurityMaster getSecurityMaster() {
    return _securityMaster;
  }

  /**
   * Sets the security master.
   * @param securityMaster  the master, not null
   */
  public void setSecurityMaster(final SecurityMaster securityMaster) {
    _securityMaster = securityMaster;
  }

  /**
   * Gets the security loader.
   * @return the loader, not null
   */
  public SecurityLoader getSecurityLoader() {
    return _securityLoader;
  }

  /**
   * Sets the security loader.
   * @param securityLoader  the loader, not null
   */
  public void setSecurityLoader(final SecurityLoader securityLoader) {
    _securityLoader = securityLoader;
  }
  
  /**
   * Gets the config master.
   * @return the master, not null
   */
  public ConfigMaster getConfigMaster() {
    return _configMaster;
  }

  /**
   * Sets the config master.
   * @param configMaster  the master, not null
   */
  public void setConfigMaster(final ConfigMaster configMaster) {
    _configMaster = configMaster;
  }
  
  /**
   * Gets the historical time-series source
   * @return the source, not null
   */
  public HistoricalTimeSeriesSource getHistoricalTimeSeriesSource() {
    return _historicalTimeSeriesSource;
  }
  
  /**
   * Sets the historical time-series source.
   * @param historicalTimeSeriesSource  the source, not null
   */
  public void setHistoricalTimeSeriesSource(HistoricalTimeSeriesSource historicalTimeSeriesSource) {
    _historicalTimeSeriesSource = historicalTimeSeriesSource;
  }
  
  /**
   * Gets the historicalTimeSeriesMaster field.
   * @return the historicalTimeSeriesMaster
   */
  public HistoricalTimeSeriesMaster getHistoricalTimeSeriesMaster() {
    return _historicalTimeSeriesMaster;
  }

  /**
   * Sets the historicalTimeSeriesMaster field.
   * @param historicalTimeSeriesMaster  the historicalTimeSeriesMaster
   */
  public void setHistoricalTimeSeriesMaster(HistoricalTimeSeriesMaster historicalTimeSeriesMaster) {
    _historicalTimeSeriesMaster = historicalTimeSeriesMaster;
  }

  /**
   * Sets the convention bundle source
   * @param conventionBundleSource the source, not null
   */
  public void setConventionBundleSource(ConventionBundleSource conventionBundleSource) {
    _conventionBundleSource = conventionBundleSource;
  }

  /**
   * Gets the convention bundle source
   * @return the source, not null
   */
  public ConventionBundleSource getConventionBundleSource() {
    return _conventionBundleSource;
  }

  /**
   * Sets the holidaySource.
   * @param holidaySource  the holiday source
   */
  public void setHolidaySource(HolidaySource holidaySource) {
    _holidaySource = holidaySource;
  }

  /**
   * Gets the Holiday Source.
   * @return the holidaySource
   */
  public HolidaySource getHolidaySource() {
    return _holidaySource;
  }

  /**
   * Gets the securitySource.
   * @return the securitySource
   */
  public SecuritySource getSecuritySource() {
    return _securitySource;
  }

  /**
   * Sets the securitySource.
   * @param securitySource  the securitySource
   */
  public void setSecuritySource(final SecuritySource securitySource) {
    _securitySource = securitySource;
  }

  /**
   * Gets the exchangeSource field.
   * @return the exchangeSource
   */
  public ExchangeSource getExchangeSource() {
    return _exchangeSource;
  }

  /**
   * Sets the exchangeSource field.
   * @param exchangeSource  the exchangeSource
   */
  public void setExchangeSource(ExchangeSource exchangeSource) {
    _exchangeSource = exchangeSource;
  }
  
}
