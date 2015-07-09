/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.solutions.library.storage;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.opengamma.financial.convention.ConventionBundleMaster;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.impl.InMemoryConfigMaster;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.master.convention.impl.InMemoryConventionMaster;
import com.opengamma.master.exchange.ExchangeMaster;
import com.opengamma.master.exchange.impl.InMemoryExchangeMaster;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.impl.InMemoryHistoricalTimeSeriesMaster;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.impl.InMemoryHolidayMaster;
import com.opengamma.master.legalentity.LegalEntityMaster;
import com.opengamma.master.legalentity.impl.InMemoryLegalEntityMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.impl.InMemorySnapshotMaster;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.impl.InMemoryPortfolioMaster;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.impl.InMemoryPositionMaster;
import com.opengamma.master.region.RegionMaster;
import com.opengamma.master.region.impl.InMemoryRegionMaster;
import com.opengamma.master.region.impl.RegionFileReader;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.impl.InMemorySecurityMaster;

/**
 * Binds the master interfaces to in-memory versions.
 */
public class InMemoryStorageModule extends AbstractModule {

  @Override
  protected void configure() {
  }

  @Provides
  @Singleton
  public SecurityMaster createSecurityMaster() {
    return new InMemorySecurityMaster();
  }
  
  @Provides
  @Singleton
  public ConfigMaster createConfigMaster() {
    return new InMemoryConfigMaster();
  }
  
  @Provides
  @Singleton
  public HistoricalTimeSeriesMaster createHtsMaster() {
    return new InMemoryHistoricalTimeSeriesMaster();
  }
  
  @Provides
  @Singleton
  public ConventionMaster createConventionMaster() {
    return new InMemoryConventionMaster();
  }
 
  @Provides
  @Singleton
  public RegionMaster createRegionMaster() {
    return new InMemoryRegionMaster();
  }

  @Provides
  @Singleton
  public HolidayMaster createHolidayMaster() {
    return new InMemoryHolidayMaster();
  }

  @Provides
  @Singleton
  public MarketDataSnapshotMaster createSnapshotMaster() {
    return new InMemorySnapshotMaster();
  }

  @Provides
  @Singleton
  public LegalEntityMaster createLegalEntityMaster() {
    return new InMemoryLegalEntityMaster();
  }

  @Provides
  @Singleton
  public ExchangeMaster createExchangeMaster() {
    return new InMemoryExchangeMaster();
  }

  @Provides
  @Singleton
  public PositionMaster createPositionMaster() {
    return new InMemoryPositionMaster();
  }

  @Provides
  @Singleton
  public PortfolioMaster createPortfolioMaster() {
    return new InMemoryPortfolioMaster();
  }

  @Provides
  @Singleton
  public ConventionBundleMaster createConventionBundleMaster() {
    return new InMemoryConventionBundleMaster();
  }

}
