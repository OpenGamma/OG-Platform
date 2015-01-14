/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.solutions.library.storage;

import com.google.inject.AbstractModule;
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
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.integration.regression.DatabaseRestore;
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
import com.opengamma.master.holiday.impl.SimpleInMemoryHolidayStore;
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
import com.opengamma.util.ArgumentChecker;

/**
 * Provide the DatabaseRestore utility to populate data in the various masters
 */
public class DataLoadModule extends AbstractModule {

  private final String _path;

  /**
   * Initialise the DataLoadModule
   * @param path the full path to the data resources
   */
  public DataLoadModule(String path) {
    _path = ArgumentChecker.notNull(path, "path");
  }

  @Provides
  @Singleton
  public DatabaseRestore createDatabaseRestore(SecurityMaster securityMaster,
                                               PositionMaster positionMaster,
                                               PortfolioMaster portfolioMaster,
                                               ConfigMaster configMaster,
                                               HistoricalTimeSeriesMaster historicalTimeSeriesMaster,
                                               HolidayMaster holidayMaster,
                                               ExchangeMaster exchangeMaster,
                                               MarketDataSnapshotMaster marketDataSnapshotMaster,
                                               LegalEntityMaster legalEntityMaster,
                                               ConventionMaster conventionMaster) {

    return new DatabaseRestore(_path,
                               securityMaster,
                               positionMaster,
                               portfolioMaster,
                               configMaster,
                               historicalTimeSeriesMaster,
                               holidayMaster,
                               exchangeMaster,
                               marketDataSnapshotMaster,
                               legalEntityMaster,
                               conventionMaster);
  }

  @Override
  protected void configure() {
    //Nothing to do
  }
}
