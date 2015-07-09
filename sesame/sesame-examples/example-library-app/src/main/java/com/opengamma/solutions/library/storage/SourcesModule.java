/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.solutions.library.storage;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.exchange.ExchangeSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.legalentity.LegalEntitySource;
import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.convention.ConventionBundleMaster;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.DefaultConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.impl.MasterConfigSource;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.master.convention.impl.MasterConventionSource;
import com.opengamma.master.exchange.ExchangeMaster;
import com.opengamma.master.exchange.impl.MasterExchangeSource;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesSelector;
import com.opengamma.master.historicaltimeseries.impl.DefaultHistoricalTimeSeriesResolver;
import com.opengamma.master.historicaltimeseries.impl.DefaultHistoricalTimeSeriesSelector;
import com.opengamma.master.historicaltimeseries.impl.MasterHistoricalTimeSeriesSource;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.impl.MasterHolidaySource;
import com.opengamma.master.legalentity.LegalEntityMaster;
import com.opengamma.master.legalentity.impl.MasterLegalEntitySource;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.impl.MasterSnapshotSource;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.impl.MasterPositionSource;
import com.opengamma.master.region.RegionMaster;
import com.opengamma.master.region.impl.MasterRegionSource;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.impl.MasterSecuritySource;

/**
 * Configures the Source wrappers. Source instances tend to be
 * minimal wrappers around corresponding masters and are 
 * agnostic to the underlying master implementation. Therefore
 * it makes sense to configure these one level above the 
 * actual storage module.
 */
public class SourcesModule extends AbstractModule {

  private final Provider<HistoricalTimeSeriesSource> _externalTimeSeriesSource;

  public SourcesModule(Provider<HistoricalTimeSeriesSource> externalTimeSeriesSource) {
    _externalTimeSeriesSource = externalTimeSeriesSource;
  }

  public SourcesModule() {
    _externalTimeSeriesSource = null;
  }
  
  @Override
  protected void configure() {
  }

  @Provides
  @Singleton
  public SecuritySource createSecuritySource(SecurityMaster securityMaster) {
    return new MasterSecuritySource(securityMaster);
  }
  
  @Provides
  @Singleton
  public ConfigSource createConfigSource(ConfigMaster configMaster) {
    return new MasterConfigSource(configMaster);
  }
  
  @Provides
  @Singleton
  //not strictly a source, but is required to create the HtsSource below.
  //exposed as an injectable instance since it is frequently
  //needed in its own right, independently of the source and master.
  public HistoricalTimeSeriesResolver createHtsResolver(ConfigSource configSource, HistoricalTimeSeriesMaster htsMaster) {
    HistoricalTimeSeriesSelector selector = new DefaultHistoricalTimeSeriesSelector(configSource);
    return new DefaultHistoricalTimeSeriesResolver(selector, htsMaster);
  }
  
  @Provides
  @Singleton
  public HistoricalTimeSeriesSource createHtsSource(HistoricalTimeSeriesMaster timeSeriesMaster, 
      HistoricalTimeSeriesResolver resolver) {
    if (_externalTimeSeriesSource == null) {
      return new MasterHistoricalTimeSeriesSource(timeSeriesMaster, resolver);
    } else {
      return _externalTimeSeriesSource.get();
    }
  }
  
  @Provides
  @Singleton
  public ConventionSource createConventionSource(ConventionMaster conventionMaster) {
    return new MasterConventionSource(conventionMaster);
  }

  @Provides
  @Singleton
  public ConventionBundleSource createConventionBundleSource(ConventionBundleMaster conventionBundleMaster) {
    return new DefaultConventionBundleSource(conventionBundleMaster);
  }

  @Provides
  @Singleton
  public RegionSource createRegionSource(RegionMaster regionMaster) {
    return new MasterRegionSource(regionMaster);
  }

  @Provides
  @Singleton
  public HolidaySource createHolidaySource(HolidayMaster holidayMaster) {
    return new MasterHolidaySource(holidayMaster);
  }

  @Provides
  @Singleton
  public MarketDataSnapshotSource createSnapshotSource(MarketDataSnapshotMaster snapshotMaster) {
    return new MasterSnapshotSource(snapshotMaster);
  }

  @Provides
  @Singleton
  public LegalEntitySource createLegalEntitySource(LegalEntityMaster legalEntityMaster) {
    return new MasterLegalEntitySource(legalEntityMaster);
  }

  @Provides
  @Singleton
  public ExchangeSource createExchangeSource(ExchangeMaster exchangeMaster) {
    return new MasterExchangeSource(exchangeMaster);
  }

  @Provides
  @Singleton
  public PositionSource createPositionSource(PortfolioMaster portfolioMaster, PositionMaster positionMaster) {
    return new MasterPositionSource(portfolioMaster, positionMaster);
  }


}
