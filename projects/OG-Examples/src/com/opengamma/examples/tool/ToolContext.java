/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.tool;

import com.opengamma.component.ComponentManager;
import com.opengamma.component.ComponentRepository;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.exchange.ExchangeSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.analytics.ircurve.YieldCurveConfigPopulator;
import com.opengamma.financial.analytics.volatility.surface.FXOptionVolatilitySurfaceConfigPopulator;
import com.opengamma.financial.analytics.volatility.surface.IRFutureOptionSurfaceConfigPopulator;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.DefaultConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.currency.CurrencyMatrixConfigPopulator;
import com.opengamma.financial.currency.CurrencyPairsConfigPopulator;
import com.opengamma.financial.portfolio.loader.LoaderContext;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.impl.InMemoryConfigMaster;
import com.opengamma.master.config.impl.MasterConfigSource;
import com.opengamma.master.exchange.ExchangeMaster;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.region.RegionMaster;
import com.opengamma.master.region.impl.InMemoryRegionMaster;
import com.opengamma.master.region.impl.MasterRegionSource;
import com.opengamma.master.region.impl.RegionFileReader;
import com.opengamma.master.security.SecurityLoader;
import com.opengamma.master.security.SecurityMaster;

/**
 * A context providing access to standard OpenGamma components for running tools.
 * <p>
 * This class is primarily used by command line tools.
 * It uses the configuration {@code toolcontext.ini}.
 */
public final class ToolContext {

  /**
   * Single instance.
   */
  public static final ToolContext INSTANCE = new ToolContext();

  private ComponentRepository _repo;
  private final ConfigMaster _dbConfigMaster;
  private final ExchangeMaster _dbExchangeMaster;
  private final HolidayMaster _dbHolidayMaster;
  private final SecurityMaster _dbSecurityMaster;
  private final PositionMaster _dbPositionMaster;
  private final PortfolioMaster _dbPortfolioMaster;
  private final MarketDataSnapshotMaster _dbSnapshotMaster;
  private final HistoricalTimeSeriesMaster _htsMaster;

  private final ConfigSource _configSource;
  private final ExchangeSource _exchangeSource;
  private final HolidaySource _holidaySource;
  private final RegionSource _regionSource;
  private final SecuritySource _securitySource;
  private final PositionSource _positionSource;
  private final MarketDataSnapshotSource _snapshotSource;

  private final LoaderContext _loaderContext;
  private final HistoricalTimeSeriesSource _htsSource;
  private final ConventionBundleSource _conventionBundleSource;
  private final SecurityLoader _securityLoader;

  private ToolContext() {
    ComponentManager manager = new ComponentManager();
    manager.start("classpath:toolcontext/toolcontext-example.properties");
    _repo = manager.getRepository();
    
    InMemoryConfigMaster cfgMaster = new InMemoryConfigMaster();
    YieldCurveConfigPopulator.populateCurveConfigMaster(cfgMaster);
    CurrencyMatrixConfigPopulator.populateCurrencyMatrixConfigMaster(cfgMaster);
    CurrencyPairsConfigPopulator.populateCurrencyPairsConfigMaster(cfgMaster);
    FXOptionVolatilitySurfaceConfigPopulator.populateVolatilitySurfaceConfigMaster(cfgMaster);
    IRFutureOptionSurfaceConfigPopulator.populateVolatilitySurfaceConfigMaster(cfgMaster);
    _configSource = new MasterConfigSource(cfgMaster);
    
    RegionMaster regionMaster = new InMemoryRegionMaster();
    RegionFileReader.createPopulated(regionMaster);
    RegionSource regionSource = new MasterRegionSource(regionMaster);
    _regionSource = regionSource;
    
    _dbConfigMaster = _repo.getInstance(ConfigMaster.class, "central");
    _dbExchangeMaster = _repo.getInstance(ExchangeMaster.class, "central");
    _dbHolidayMaster = _repo.getInstance(HolidayMaster.class, "central");
    _dbSecurityMaster = _repo.getInstance(SecurityMaster.class, "central");
    _dbPositionMaster = _repo.getInstance(PositionMaster.class, "central");
    _dbPortfolioMaster = _repo.getInstance(PortfolioMaster.class, "central");
    _htsMaster = _repo.getInstance(HistoricalTimeSeriesMaster.class, "central");
    _dbSnapshotMaster = _repo.getInstance(MarketDataSnapshotMaster.class, "central");
    
    _exchangeSource = _repo.getInstance(ExchangeSource.class, "shared");
    _holidaySource = _repo.getInstance(HolidaySource.class, "shared");
    _securitySource = _repo.getInstance(SecuritySource.class, "shared");
    _positionSource = _repo.getInstance(PositionSource.class, "shared");
    _snapshotSource = _repo.getInstance(MarketDataSnapshotSource.class, "shared");
    _htsSource = _repo.getInstance(HistoricalTimeSeriesSource.class, "shared");
    _conventionBundleSource = new DefaultConventionBundleSource(new InMemoryConventionBundleMaster());
    _securityLoader = _repo.getInstance(SecurityLoader.class, "standard");
    
    _loaderContext = new LoaderContext();
    _loaderContext.setConfigMaster(_dbConfigMaster);
    _loaderContext.setConventionBundleSource(_conventionBundleSource);
    _loaderContext.setExchangeSource(_exchangeSource);
    _loaderContext.setHistoricalTimeSeriesMaster(_htsMaster);
    _loaderContext.setHolidaySource(_holidaySource);
    _loaderContext.setPortfolioMaster(_dbPortfolioMaster);
    _loaderContext.setPositionMaster(_dbPositionMaster);
    _loaderContext.setSecurityMaster(_dbSecurityMaster);
    _loaderContext.setSecuritySource(_securitySource);
    _loaderContext.setHistoricalTimeSeriesSource(_htsSource);
    _loaderContext.setPortfolioMaster(_dbPortfolioMaster);
  }

  //-------------------------------------------------------------------------
  /**
   * Closes the context.
   */
  public synchronized void close() {
    if (_repo != null) {
      _repo.stop();
      _repo = null;
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the config master.
   * @return the config master, not null
   */
  public ConfigMaster getDbConfigMaster() {
    return _dbConfigMaster;
  }

  /**
   * Gets the exchange master.
   * @return the exchange master, not null
   */
  public ExchangeMaster getDbExchangeMaster() {
    return _dbExchangeMaster;
  }

  /**
   * Gets the holiday master.
   * @return the holiday master, not null
   */
  public HolidayMaster getDbHolidayMaster() {
    return _dbHolidayMaster;
  }

  /**
   * Gets the security master.
   * @return the security master, not null
   */
  public SecurityMaster getDbSecurityMaster() {
    return _dbSecurityMaster;
  }

  /**
   * Gets the position master.
   * @return the position master, not null
   */
  public PositionMaster getDbPositionMaster() {
    return _dbPositionMaster;
  }

  /**
   * Gets the portfolio master.
   * @return the portfolio master, not null
   */
  public PortfolioMaster getDbPortfolioMaster() {
    return _dbPortfolioMaster;
  }

  /**
   * Gets the snapshot master.
   * @return the snapshot master, not null
   */
  public MarketDataSnapshotMaster getDbMarketDataSnapshotMaster() {
    return _dbSnapshotMaster;
  }

  /**
   * Gets the time-series master.
   * @return the time-series master, not null
   */
  public HistoricalTimeSeriesMaster getDbHistoricalTimeSeriesMaster() {
    return _htsMaster;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the config source.
   * @return the config source, not null
   */
  public ConfigSource getConfigSource() {
    return _configSource;
  }

  /**
   * Gets the exchange source.
   * @return the exchange source, not null
   */
  public ExchangeSource getExchangeSource() {
    return _exchangeSource;
  }

  /**
   * Gets the holiday source.
   * @return the holiday source, not null
   */
  public HolidaySource getHolidaySource() {
    return _holidaySource;
  }

  /**
   * Gets the region source.
   * @return the region source, not null
   */
  public RegionSource getRegionSource() {
    return _regionSource;
  }

  /**
   * Gets the security source.
   * @return the security source, not null
   */
  public SecuritySource getSecuritySource() {
    return _securitySource;
  }
  
  /**
   * Gets the positionSource.
   * @return the positionSource, not null
   */
  public PositionSource getPositionSource() {
    return _positionSource;
  }

  /**
   * Gets the snapshot source.
   * @return the snapshot source, not null
   */
  public MarketDataSnapshotSource getMarketDataSnapshotSource() {
    return _snapshotSource;
  }

  /**
   * Gets the time-series source.
   * @return the time-series source
   */
  public HistoricalTimeSeriesSource getHistoricalTimeSeriesSource() {
    return _htsSource;
  }
  
  //-------------------------------------------------------------------------
  /**
   * Gets the loaderContext.
   * @return the loaderContext, not null
   */
  public LoaderContext getLoaderContext() {
    return _loaderContext;
  }

  /**
   * Gets the conventionBundleSource.
   * @return the conventionBundleSource
   */
  public ConventionBundleSource getConventionBundleSource() {
    return _conventionBundleSource;
  }

  /**
   * Gets the securityLoader.
   * @return the securityLoader
   */
  public SecurityLoader getSecurityLoader() {
    return _securityLoader;
  }

}
