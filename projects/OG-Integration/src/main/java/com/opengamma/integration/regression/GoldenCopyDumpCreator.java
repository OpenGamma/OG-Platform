/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import java.io.IOException;
import java.util.Set;

import com.google.common.base.Predicate;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.impl.DataTrackingConfigMaster;
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.impl.DataTrackingExchangeMaster;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.impl.DataTrackingHistoricalTimeSeriesMaster;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.impl.DataTrackingHolidayMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.impl.DataTrackingMarketDataSnapshotMaster;
import com.opengamma.master.organization.impl.DataTrackingOrganizationMaster;
import com.opengamma.master.orgs.OrganizationDocument;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.impl.DataTrackingPortfolioMaster;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.impl.DataTrackingPositionMaster;
import com.opengamma.master.security.SecurityDocument;
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
  private final DataTrackingOrganizationMaster _organizationMaster;
  
  
  
  public GoldenCopyDumpCreator(RegressionIO regressionIO, DataTrackingSecurityMaster securityMaster, DataTrackingPositionMaster positionMaster, DataTrackingPortfolioMaster portfolioMaster,
      DataTrackingConfigMaster configMaster, DataTrackingHistoricalTimeSeriesMaster timeSeriesMaster, DataTrackingHolidayMaster holidayMaster, DataTrackingExchangeMaster exchangeMaster,
      DataTrackingMarketDataSnapshotMaster snapshotMaster, DataTrackingOrganizationMaster organizationMaster) {
    _regressionIO = regressionIO;
    _securityMaster = securityMaster;
    _positionMaster = positionMaster;
    _portfolioMaster = portfolioMaster;
    _configMaster = configMaster;
    _timeSeriesMaster = timeSeriesMaster;
    _holidayMaster = holidayMaster;
    _exchangeMaster = exchangeMaster;
    _snapshotMaster = snapshotMaster;
    _organizationMaster = organizationMaster;
  }

  
  /**
   * Run the db dump, building appropriate filters from the passed DataTracking masters.
   * @throws IOException
   */
  public void execute() throws IOException {
    
    MasterFilterManager filterManager = buildFilterManager();
    
    DatabaseDump databaseDump = new DatabaseDump(_regressionIO, 
                                                _securityMaster, 
                                                _positionMaster, 
                                                _portfolioMaster, 
                                                _configMaster, 
                                                _timeSeriesMaster, 
                                                _holidayMaster, 
                                                _exchangeMaster, 
                                                _snapshotMaster, 
                                                _organizationMaster,
                                                filterManager);
    
    
    databaseDump.dumpDatabase();
    
  }
  
  
  private MasterFilterManager buildFilterManager() {
    //specific predicate types declared here for guaranteed type-safety in the MasterFilterManager constructor call
    Predicate<? super SecurityDocument> secFilter                 = new UniqueIdentifiableFilter(_securityMaster.getIdsAccessed());
    Predicate<? super PositionDocument> posFilter                 = new UniqueIdentifiableFilter(_positionMaster.getIdsAccessed());
    Predicate<? super PortfolioDocument> porFilter                = new UniqueIdentifiableFilter(_portfolioMaster.getIdsAccessed());
    Predicate<? super ConfigDocument> conFilter                   = new UniqueIdentifiableFilter(_configMaster.getIdsAccessed());
    Predicate<? super HistoricalTimeSeriesInfoDocument> htsFilter = new UniqueIdentifiableFilter(_timeSeriesMaster.getIdsAccessed());
    Predicate<? super HolidayDocument> holFilter                  = new UniqueIdentifiableFilter(_holidayMaster.getIdsAccessed());
    Predicate<? super ExchangeDocument> exhFilter                 = new UniqueIdentifiableFilter(_exchangeMaster.getIdsAccessed());
    Predicate<? super MarketDataSnapshotDocument> snpFilter       = new UniqueIdentifiableFilter(_snapshotMaster.getIdsAccessed());
    Predicate<? super OrganizationDocument> orgFilter             = new UniqueIdentifiableFilter(_organizationMaster.getIdsAccessed());
    
    return new MasterFilterManager(secFilter, posFilter, porFilter, conFilter, htsFilter, holFilter, exhFilter, snpFilter, orgFilter);
  }


  /**
   * Filter which checks a {@link UniqueIdentifiable} object is identified by one of
   * a set of ids.
   */
  private static class UniqueIdentifiableFilter implements Predicate<UniqueIdentifiable> {
    
    private Set<UniqueId> _idsToInclude;
    
    public UniqueIdentifiableFilter(Set<UniqueId> uniqueId) {
      _idsToInclude = uniqueId;
    }

    @Override
    public boolean apply(UniqueIdentifiable input) {
      return _idsToInclude.contains(input.getUniqueId());
    }
    
  }
  
}
