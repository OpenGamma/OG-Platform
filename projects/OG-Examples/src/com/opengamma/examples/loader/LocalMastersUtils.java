/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.loader;

import java.util.Collection;

import net.sf.ehcache.CacheManager;

import com.google.common.collect.ImmutableList;
import com.opengamma.component.ComponentManager;
import com.opengamma.component.ComponentRepository;
import com.opengamma.core.exchange.ExchangeSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.historicaltimeseries.impl.EHCachingHistoricalTimeSeriesSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.examples.historical.normalization.MockHistoricalTimeSeriesFieldAdjustmentMapFactoryBean;
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
import com.opengamma.master.exchange.impl.MasterExchangeSource;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesSelector;
import com.opengamma.master.historicaltimeseries.impl.DefaultHistoricalTimeSeriesSelector;
import com.opengamma.master.historicaltimeseries.impl.FieldMappingHistoricalTimeSeriesResolver;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesFieldAdjustmentMap;
import com.opengamma.master.historicaltimeseries.impl.MasterHistoricalTimeSeriesSource;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.impl.MasterHolidaySource;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.impl.MasterSnapshotSource;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.impl.MasterPositionSource;
import com.opengamma.master.region.RegionMaster;
import com.opengamma.master.region.impl.InMemoryRegionMaster;
import com.opengamma.master.region.impl.MasterRegionSource;
import com.opengamma.master.region.impl.RegionFileReader;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.impl.MasterSecuritySource;

/**
 * Helper for creating local masters
 */
public final class LocalMastersUtils {
  
  /**
   * Single instance.
   */
  public static final LocalMastersUtils INSTANCE = new LocalMastersUtils();

  private final PortfolioMaster _portfolioMaster;
  private final PositionMaster _positionMaster;
  private final ConfigMaster _dbConfigMaster;
  private final SecurityMaster _securityMaster;
  private final ExchangeMaster _exchangeMaster;
  private final HolidayMaster _holidayMaster;
  private final ConfigMaster _configMaster;
  private final MasterConfigSource _configSource;
  private final RegionSource _regionSource;
  private final SecuritySource _securitySource;
  private final MarketDataSnapshotSource _snapshotSource;
  private final ExchangeSource _exchangeSource;
  private final HolidaySource _holidaySource;
  private final PositionSource _positionSource;
  private final LoaderContext _loaderContext;
  private final HistoricalTimeSeriesMaster _htsMaster;
  private final HistoricalTimeSeriesSource _htsSource;
  private final ConventionBundleSource _conventionBundleSource;
  private ComponentRepository _repo;
  
  private LocalMastersUtils() {
    
    InMemoryConfigMaster cfgMaster = new InMemoryConfigMaster();
    YieldCurveConfigPopulator.populateCurveConfigMaster(cfgMaster);
    CurrencyMatrixConfigPopulator.populateCurrencyMatrixConfigMaster(cfgMaster);
    CurrencyPairsConfigPopulator.populateCurrencyPairsConfigMaster(cfgMaster);
    FXOptionVolatilitySurfaceConfigPopulator.populateVolatilitySurfaceConfigMaster(cfgMaster);
    IRFutureOptionSurfaceConfigPopulator.populateVolatilitySurfaceConfigMaster(cfgMaster);
    _configMaster = cfgMaster;
    _configSource = new MasterConfigSource(cfgMaster);
    
    RegionMaster regionMaster = new InMemoryRegionMaster();
    RegionFileReader.createPopulated(regionMaster);
    RegionSource regionSource = new MasterRegionSource(regionMaster);
    _regionSource = regionSource;
    
    ComponentManager manager = new ComponentManager();
    manager.start("classpath:localmasters/localmasters-example.properties");
    _repo = manager.getRepository();
    _dbConfigMaster = _repo.getInstance(ConfigMaster.class, "central");
    _exchangeMaster = _repo.getInstance(ExchangeMaster.class, "central");
    _holidayMaster = _repo.getInstance(HolidayMaster.class, "central");
    _securityMaster = _repo.getInstance(SecurityMaster.class, "central");
    _positionMaster = _repo.getInstance(PositionMaster.class, "central");
    _portfolioMaster = _repo.getInstance(PortfolioMaster.class, "central");
    _htsMaster = _repo.getInstance(HistoricalTimeSeriesMaster.class, "central");
    MarketDataSnapshotMaster snapshotMaster = _repo.getInstance(MarketDataSnapshotMaster.class, "central");
    
    _exchangeSource = new MasterExchangeSource(_exchangeMaster);
    _holidaySource = new MasterHolidaySource(_holidayMaster);
    _securitySource = new MasterSecuritySource(_securityMaster);
    _positionSource = new MasterPositionSource(_portfolioMaster, _positionMaster);
    _snapshotSource = new MasterSnapshotSource(snapshotMaster);
    _conventionBundleSource = new DefaultConventionBundleSource(new InMemoryConventionBundleMaster());
    
    CacheManager cacheManager = _repo.getInstance(CacheManager.class, "standard");
    HistoricalTimeSeriesResolver resolver = initResolver();
    HistoricalTimeSeriesSource htsSource = new MasterHistoricalTimeSeriesSource(_htsMaster, resolver);
    if (cacheManager != null) {
      htsSource = new EHCachingHistoricalTimeSeriesSource(htsSource, cacheManager);
    }
    
    _htsSource = htsSource;
    _loaderContext = new LoaderContext();
    _loaderContext.setConfigMaster(_dbConfigMaster);
    _loaderContext.setConventionBundleSource(_conventionBundleSource);
    _loaderContext.setExchangeSource(_exchangeSource);
    _loaderContext.setHistoricalTimeSeriesMaster(_htsMaster);
    _loaderContext.setHolidaySource(_holidaySource);
    _loaderContext.setPortfolioMaster(_portfolioMaster);
    _loaderContext.setPositionMaster(_positionMaster);
    _loaderContext.setSecurityMaster(_securityMaster);
    _loaderContext.setSecuritySource(_securitySource);
    _loaderContext.setHistoricalTimeSeriesSource(_htsSource);
    _loaderContext.setPortfolioMaster(_portfolioMaster);
  }

  private HistoricalTimeSeriesResolver initResolver() {
    MockHistoricalTimeSeriesFieldAdjustmentMapFactoryBean factory = new MockHistoricalTimeSeriesFieldAdjustmentMapFactoryBean();
    Collection<HistoricalTimeSeriesFieldAdjustmentMap> fieldAdjustmentMaps = ImmutableList.of(factory.getObjectCreating());
    HistoricalTimeSeriesSelector selector = new DefaultHistoricalTimeSeriesSelector(_configSource);
    return new FieldMappingHistoricalTimeSeriesResolver(fieldAdjustmentMaps, selector, _htsMaster);
  }
  
  public synchronized void tearDown() {
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
  public ConfigMaster getConfigMaster() {
    return _configMaster;
  }

  /**
   * Gets the config source.
   * @return the config source, not null
   */
  public MasterConfigSource getConfigSource() {
    return _configSource;
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
   * Gets the security master.
   * @return the security master, not null
   */
  public SecurityMaster getSecurityMaster() {
    return _securityMaster;
  }
  
  /**
   * Gets the snapshot source.
   * @return the snapshot source, not null
   */
  public MarketDataSnapshotSource getSnapshotSource() {
    return _snapshotSource;
  }

  /**
   * Gets the exchange master.
   * @return the exchange master, not null
   */
  public ExchangeMaster getExchangeMaster() {
    return _exchangeMaster;
  }
  
  /**
   * Gets the exchange source.
   * @return the exchange source, not null
   */
  public ExchangeSource getExchangeSource() {
    return _exchangeSource;
  }

  /**
   * Gets the holiday master.
   * @return the holiday master, not null
   */
  public HolidayMaster getHolidayMaster() {
    return _holidayMaster;
  }

  /**
   * Gets the holiday source.
   * @return the holiday source, not null
   */
  public HolidaySource getHolidaySource() {
    return _holidaySource;
  }

  /**
   * Gets the portfolio master.
   * @return the portfolio master, not null
   */
  public PortfolioMaster getPortfolioMaster() {
    return _portfolioMaster;
  }

  /**
   * Gets the position master.
   * @return the position master, not null
   */
  public PositionMaster getPositionMaster() {
    return _positionMaster;
  }

  /**
   * Gets the config master.
   * @return the config master, not null
   */
  public ConfigMaster getDbConfigMaster() {
    return _dbConfigMaster;
  }

  /**
   * Gets the positionSource.
   * @return the positionSource
   */
  public PositionSource getPositionSource() {
    return _positionSource;
  }

  /**
   * Gets the loaderContext.
   * @return the loaderContext
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
   * Gets the htsMaster.
   * @return the htsMaster
   */
  public HistoricalTimeSeriesMaster getHistoricalTimeSeriesMaster() {
    return _htsMaster;
  }

  /**
   * Gets the htsSource.
   * @return the htsSource
   */
  public HistoricalTimeSeriesSource getHistoricalTimeSeriesSource() {
    return _htsSource;
  }
  
}
