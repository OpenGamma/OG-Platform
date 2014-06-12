/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.opengamma.bbg.referencedata.ReferenceData;
import com.opengamma.bbg.referencedata.impl.InMemoryCachingReferenceDataProvider;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.integration.tool.IntegrationToolContext;
import com.opengamma.master.AbstractDocument;
import com.opengamma.master.AbstractMaster;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.impl.DataTrackingConfigMaster;
import com.opengamma.master.convention.ConventionDocument;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.master.convention.impl.DataTrackingConventionMaster;
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ExchangeMaster;
import com.opengamma.master.exchange.impl.DataTrackingExchangeMaster;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.impl.DataTrackingHistoricalTimeSeriesMaster;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.impl.DataTrackingHolidayMaster;
import com.opengamma.master.legalentity.LegalEntityDocument;
import com.opengamma.master.legalentity.LegalEntityMaster;
import com.opengamma.master.legalentity.impl.DataTrackingLegalEntityMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.impl.DataTrackingMarketDataSnapshotMaster;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.impl.DataTrackingPortfolioMaster;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.impl.DataTrackingPositionMaster;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.impl.DataTrackingSecurityMaster;

/**
 * Executes a DB dump, only including the records which have been accessed.
 */
class GoldenCopyDumpCreator {

  public static final String DB_DUMP_ZIP = "dbdump.zip";

  private final RegressionIO _regressionIO;
  private final DataTrackingSecurityMaster _securityMaster;
  private final DataTrackingPositionMaster _positionMaster;
  private final DataTrackingPortfolioMaster _portfolioMaster;
  private final DataTrackingConfigMaster _configMaster;
  private final DataTrackingHistoricalTimeSeriesMaster _timeSeriesMaster;
  private final DataTrackingHolidayMaster _holidayMaster;
  private final DataTrackingExchangeMaster _exchangeMaster;
  private final DataTrackingMarketDataSnapshotMaster _snapshotMaster;
  private final DataTrackingLegalEntityMaster _legalEntityMaster;
  private final DataTrackingConventionMaster _conventionMaster;


  private InMemoryCachingReferenceDataProvider _referenceDataProvider;

  public GoldenCopyDumpCreator(RegressionIO regressionIO, IntegrationToolContext tc) {
    _regressionIO = regressionIO;
    _securityMaster = (DataTrackingSecurityMaster) tc.getSecurityMaster();
    _positionMaster = (DataTrackingPositionMaster) tc.getPositionMaster();
    _portfolioMaster = (DataTrackingPortfolioMaster) tc.getPortfolioMaster();
    _configMaster = (DataTrackingConfigMaster) tc.getConfigMaster();
    _timeSeriesMaster = (DataTrackingHistoricalTimeSeriesMaster) tc.getHistoricalTimeSeriesMaster();
    _holidayMaster = (DataTrackingHolidayMaster) tc.getHolidayMaster();
    _exchangeMaster = (DataTrackingExchangeMaster) tc.getExchangeMaster();
    _snapshotMaster = (DataTrackingMarketDataSnapshotMaster) tc.getMarketDataSnapshotMaster();
    _legalEntityMaster = (DataTrackingLegalEntityMaster) tc.getLegalEntityMaster();
    _conventionMaster = (DataTrackingConventionMaster) tc.getConventionMaster();
    _referenceDataProvider = (InMemoryCachingReferenceDataProvider) tc.getBloombergReferenceDataProvider();
  }

  /**
   * Run the db dump, building appropriate filters from the passed DataTracking masters.
   * @throws IOException
   */
  public void execute() throws IOException {

    MasterQueryManager filterManager = buildFilterManager();

    _regressionIO.beginWrite();
    try {
      //dump ref data accesses first
      ImmutableMap<String, ReferenceData> dataAccessed;
      if (_referenceDataProvider != null) {
        dataAccessed = _referenceDataProvider.getDataAccessed();
      } else {
        dataAccessed = ImmutableMap.of();
      }
      
      _regressionIO.write(null, RegressionReferenceData.create(dataAccessed), RegressionUtils.REF_DATA_ACCESSES_IDENTIFIER);
      DatabaseDump databaseDump = new DatabaseDump(_regressionIO,
                                                  _securityMaster,
                                                  _positionMaster,
                                                  _portfolioMaster,
                                                  _configMaster,
                                                  _timeSeriesMaster,
                                                  _holidayMaster,
                                                  _exchangeMaster,
                                                  _snapshotMaster,
                                                  _legalEntityMaster,
                                                  _conventionMaster,
                                                  filterManager);

      databaseDump.dumpDatabase();
    } finally {
      _regressionIO.endWrite();
    }
  }

  private MasterQueryManager buildFilterManager() {

    return new MasterQueryManager(
        new UniqueIdentifiableQuery<SecurityDocument, SecurityMaster>(_securityMaster.getIdsAccessed()),
        new UniqueIdentifiableQuery<PositionDocument, PositionMaster>(_positionMaster.getIdsAccessed()),
        new UniqueIdentifiableQuery<PortfolioDocument, PortfolioMaster>(_portfolioMaster.getIdsAccessed()),
        new UniqueIdentifiableQuery<ConfigDocument, ConfigMaster>(_configMaster.getIdsAccessed()),
        new UniqueIdentifiableQuery<HistoricalTimeSeriesInfoDocument, HistoricalTimeSeriesMaster>(_timeSeriesMaster.getIdsAccessed()),
        new UniqueIdentifiableQuery<HolidayDocument, HolidayMaster>(_holidayMaster.getIdsAccessed()),
        new UniqueIdentifiableQuery<ExchangeDocument, ExchangeMaster>(_exchangeMaster.getIdsAccessed()),
        new UniqueIdentifiableQuery<MarketDataSnapshotDocument, MarketDataSnapshotMaster>(_snapshotMaster.getIdsAccessed()),
        new UniqueIdentifiableQuery<LegalEntityDocument, LegalEntityMaster>(_legalEntityMaster.getIdsAccessed()),
        new UniqueIdentifiableQuery<ConventionDocument, ConventionMaster>(_conventionMaster.getIdsAccessed()));
  }

  /**
   * Filter which checks a {@link UniqueIdentifiable} object is identified by one of
   * a set of ids.
   */
  private static class UniqueIdentifiableQuery<D extends AbstractDocument, M extends AbstractMaster<D>> implements Function<M, Collection<D>> {

    private Set<UniqueId> _idsToInclude;

    public UniqueIdentifiableQuery(Set<UniqueId> uniqueId) {
      _idsToInclude = uniqueId;
    }

    @Override
    public Collection<D> apply(M input) {
      return input.get(_idsToInclude).values();
    }

  }

}
