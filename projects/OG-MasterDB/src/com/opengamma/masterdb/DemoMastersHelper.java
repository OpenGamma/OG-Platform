/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.opengamma.core.exchange.ExchangeSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
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
import com.opengamma.master.exchange.impl.MasterExchangeSource;
import com.opengamma.master.holiday.impl.MasterHolidaySource;
import com.opengamma.master.region.RegionMaster;
import com.opengamma.master.region.impl.InMemoryRegionMaster;
import com.opengamma.master.region.impl.MasterRegionSource;
import com.opengamma.master.region.impl.RegionFileReader;
import com.opengamma.masterdb.config.DbConfigMaster;
import com.opengamma.masterdb.exchange.DbExchangeMaster;
import com.opengamma.masterdb.holiday.DbHolidayMaster;
import com.opengamma.masterdb.portfolio.DbPortfolioMaster;
import com.opengamma.masterdb.position.DbPositionMaster;
import com.opengamma.masterdb.security.DbSecurityMaster;
import com.opengamma.util.PlatformConfigUtils;

/**
 * Helper for testing loading elements from {@code demoMasters.xml}.
 */
public final class DemoMastersHelper {
  
  
  
  /**
   * Singleton
   */
  public static final DemoMastersHelper INSTANCE = new DemoMastersHelper();

  private final DbPortfolioMaster _portfolioMaster;
  private final DbPositionMaster _positionMaster;
  private final DbConfigMaster _dbConfigMaster;
  private final DbSecurityMaster _secMaster;
  private final DbExchangeMaster _exchangeMaster;
  private final DbHolidayMaster _holidayMaster;
  private final ConfigMaster _configMaster;
  private final MasterConfigSource _configSource;
  private final RegionSource _regionSource;
  private final SecuritySource _secSource;
  private final MarketDataSnapshotSource _snapshotSource;
  private final ExchangeSource _exchangeSource;
  private final HolidaySource _holidaySource;
  private ClassPathXmlApplicationContext _applicationContext;
  
  private DemoMastersHelper() {
    PlatformConfigUtils.configureSystemProperties(PlatformConfigUtils.RunMode.SHAREDDEV);
    _applicationContext = new ClassPathXmlApplicationContext("com/opengamma/financial/demoMasters.xml");
    
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
    
    _secSource = _applicationContext.getBean("sharedSecuritySource", SecuritySource.class);
    _secMaster = _applicationContext.getBean("dbSecurityMaster", DbSecurityMaster.class);
    _snapshotSource = _applicationContext.getBean("sharedSnapshotSource", MarketDataSnapshotSource.class);
    
    _exchangeMaster = _applicationContext.getBean("dbExchangeMaster", DbExchangeMaster.class);
    _exchangeSource = _applicationContext.getBean("sharedExchangeSource", MasterExchangeSource.class);
    _holidayMaster = _applicationContext.getBean("dbHolidayMaster", DbHolidayMaster.class);
    _holidaySource = _applicationContext.getBean("sharedHolidaySource", MasterHolidaySource.class);
    
    _portfolioMaster = _applicationContext.getBean("dbPortfolioMaster", DbPortfolioMaster.class);
    _positionMaster = _applicationContext.getBean("dbPositionMaster", DbPositionMaster.class);
    _dbConfigMaster = _applicationContext.getBean("sharedConfigMaster", DbConfigMaster.class);
  }

  public synchronized void tearDown() {
    if (_applicationContext != null) {
      _applicationContext.close();
      _applicationContext = null;
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
    return _secSource;
  }
  
  /**
   * Gets the security master.
   * @return the security master, not null
   */
  public DbSecurityMaster getSecurityMaster() {
    return _secMaster;
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
  public DbExchangeMaster getExchangeMaster() {
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
  public DbHolidayMaster getHolidayMaster() {
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
  public DbPortfolioMaster getPortfolioMaster() {
    return _portfolioMaster;
  }

  /**
   * Gets the position master.
   * @return the position master, not null
   */
  public DbPositionMaster getPositionMaster() {
    return _positionMaster;
  }

  /**
   * Gets the config master.
   * @return the config master, not null
   */
  public DbConfigMaster getDbConfigMaster() {
    return _dbConfigMaster;
  }

}
