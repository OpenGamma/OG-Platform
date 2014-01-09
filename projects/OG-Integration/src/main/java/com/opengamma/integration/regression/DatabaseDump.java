/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import static com.google.common.collect.Iterators.filter;
import static com.google.common.collect.Iterators.transform;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.VersionCorrection;
import com.opengamma.integration.server.RemoteServer;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.impl.ConfigSearchIterator;
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ExchangeMaster;
import com.opengamma.master.exchange.ExchangeSearchRequest;
import com.opengamma.master.exchange.ManageableExchange;
import com.opengamma.master.exchange.impl.ExchangeSearchIterator;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeries;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesInfoSearchIterator;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.HolidaySearchRequest;
import com.opengamma.master.holiday.ManageableHoliday;
import com.opengamma.master.holiday.impl.HolidaySearchIterator;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;
import com.opengamma.master.marketdatasnapshot.impl.MarketDataSnapshotSearchIterator;
import com.opengamma.master.orgs.ManageableOrganization;
import com.opengamma.master.orgs.OrganizationDocument;
import com.opengamma.master.orgs.OrganizationMaster;
import com.opengamma.master.orgs.OrganizationSearchRequest;
import com.opengamma.master.orgs.impl.OrganizationSearchIterator;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.impl.PortfolioSearchIterator;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.impl.PositionSearchIterator;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.impl.SecuritySearchIterator;
import com.opengamma.util.ArgumentChecker;

/**
 * Dumps all the data required to run views from the database into Fudge XML files.
 * <p>
 * TODO split this up to allow a subset of data to be dumped and restored?
 */
/* package */class DatabaseDump {

  private static final Logger s_logger = LoggerFactory.getLogger(DatabaseDump.class);

  private final RegressionIO _io;
  private final SecurityMaster _securityMaster;
  private final PositionMaster _positionMaster;
  private final PortfolioMaster _portfolioMaster;
  private final ConfigMaster _configMaster;
  private final HistoricalTimeSeriesMaster _timeSeriesMaster;
  private final HolidayMaster _holidayMaster;
  private final ExchangeMaster _exchangeMaster;
  private final MarketDataSnapshotMaster _snapshotMaster;
  private final OrganizationMaster _organizationMaster;
  
  private final MasterFilterManager _masterFilterManager;
  
  
  private final IdMappings _idMappings;

  private int _nextId;

  /* package */DatabaseDump(String outputDir, SecurityMaster securityMaster, PositionMaster positionMaster, PortfolioMaster portfolioMaster, ConfigMaster configMaster,
      HistoricalTimeSeriesMaster timeSeriesMaster, HolidayMaster holidayMaster, ExchangeMaster exchangeMaster, MarketDataSnapshotMaster snapshotMaster, OrganizationMaster organizationMaster) {
    this(outputDir, securityMaster, positionMaster, portfolioMaster, configMaster, timeSeriesMaster, holidayMaster,
        exchangeMaster, snapshotMaster, organizationMaster, MasterFilterManager.alwaysTrue());
  }

  /* package */DatabaseDump(String outputDir, SecurityMaster securityMaster, PositionMaster positionMaster, PortfolioMaster portfolioMaster, ConfigMaster configMaster,
      HistoricalTimeSeriesMaster timeSeriesMaster, HolidayMaster holidayMaster, ExchangeMaster exchangeMaster, MarketDataSnapshotMaster snapshotMaster, OrganizationMaster organizationMaster,
      MasterFilterManager masterFilterManager) {
    this(new SubdirsRegressionIO(new File(outputDir), new FudgeXMLFormat(), true), securityMaster, positionMaster, portfolioMaster, configMaster, timeSeriesMaster, holidayMaster,
        exchangeMaster, snapshotMaster, organizationMaster, masterFilterManager);
  }

  
  /* package */DatabaseDump(RegressionIO io, SecurityMaster securityMaster, PositionMaster positionMaster, PortfolioMaster portfolioMaster, ConfigMaster configMaster,
      HistoricalTimeSeriesMaster timeSeriesMaster, HolidayMaster holidayMaster, ExchangeMaster exchangeMaster, MarketDataSnapshotMaster snapshotMaster, OrganizationMaster organizationMaster, 
      MasterFilterManager masterFilterManager) {
    ArgumentChecker.notNull(io, "io");
    ArgumentChecker.notNull(securityMaster, "securityMaster");
    ArgumentChecker.notNull(positionMaster, "positionMaster");
    ArgumentChecker.notNull(portfolioMaster, "portfolioMaster");
    ArgumentChecker.notNull(configMaster, "configMaster");
    ArgumentChecker.notNull(timeSeriesMaster, "timeSeriesMaster");
    ArgumentChecker.notNull(masterFilterManager, "_masterFilterManager");
    _io = io;
    _organizationMaster = organizationMaster;
    _snapshotMaster = snapshotMaster;
    _exchangeMaster = exchangeMaster;
    _holidayMaster = holidayMaster;
    _timeSeriesMaster = timeSeriesMaster;
    _positionMaster = positionMaster;
    _portfolioMaster = portfolioMaster;
    _configMaster = configMaster;
    _securityMaster = securityMaster;
    _masterFilterManager = masterFilterManager;
    ConfigItem<IdMappings> mappingsConfigItem = RegressionUtils.loadIdMappings(_configMaster);
    if (mappingsConfigItem != null) {
      _idMappings = mappingsConfigItem.getValue();
    } else {
      _idMappings = new IdMappings();
    }
    _nextId = _idMappings.getMaxId() + 1;
    s_logger.info("Dumping database to {}", _io.getBaseFile().getAbsolutePath());
  }

  public static void main(String[] args) throws IOException {
    if (args.length < 2) {
      System.err.println("arguments: dataDirectory serverUrl");
      System.exit(1);
    }
    String dataDir = args[0];
    String serverUrl = args[1];
    int exitCode = 0;
    try (RemoteServer server = RemoteServer.create(serverUrl)) {
      DatabaseDump databaseDump = new DatabaseDump(dataDir, server.getSecurityMaster(), server.getPositionMaster(), server.getPortfolioMaster(), server.getConfigMaster(),
          server.getHistoricalTimeSeriesMaster(), server.getHolidayMaster(), server.getExchangeMaster(), server.getMarketDataSnapshotMaster(), server.getOrganizationMaster());
      databaseDump.dumpDatabase();
    } catch (Exception e) {
      s_logger.warn("Failed to write data", e);
      exitCode = 1;
    }
    System.exit(exitCode);
  }

  public void dumpDatabase() throws IOException {
    _io.beginWrite();
    Map<ObjectId, Integer> ids = Maps.newHashMap(_idMappings.getIds());
    ids.putAll(writeSecurities());
    ids.putAll(writePositions());
    ids.putAll(writePortfolios());
    ids.putAll(writeConfig());
    ids.putAll(writeTimeSeries());
    ids.putAll(writeHolidays());
    ids.putAll(writeExchanges());
    ids.putAll(writeSnapshots());
    ids.putAll(writeOrganizations());
    int maxId = _idMappings.getMaxId();
    for (Integer id : ids.values()) {
      if (id > maxId) {
        maxId = id;
      }
    }
    IdMappings idMappings = new IdMappings(ids, maxId);
    _io.write(null, idMappings, RegressionUtils.ID_MAPPINGS_IDENTIFIER);
    _io.endWrite();
  }

  private Map<ObjectId, Integer> writeSecurities() throws IOException {
    SecuritySearchIterator searchIterator = new SecuritySearchIterator(_securityMaster, new SecuritySearchRequest());
    Iterator<SecurityDocument> filteredDocuments = filter(searchIterator, _masterFilterManager.getSecurityFilter());
    return write(transform(filteredDocuments, new SecurityTransformer()), "securities", "sec");
  }

  private Map<ObjectId, Integer> writePositions() throws IOException {
    PositionSearchIterator searchIterator = new PositionSearchIterator(_positionMaster, new PositionSearchRequest());
    Iterator<PositionDocument> filteredDocuments = filter(searchIterator, _masterFilterManager.getPositionFilter());
    return write(transform(filteredDocuments, new PositionTransformer()), "positions", "pos");
  }

  private Map<ObjectId, Integer> writeConfig() throws IOException {
    ConfigSearchIterator<Object> searchIterator = new ConfigSearchIterator<>(_configMaster, new ConfigSearchRequest<>(Object.class));
    Iterator<ConfigDocument> filteredDocuments = filter(searchIterator, _masterFilterManager.getConfigFilter());
    return write(transform(filteredDocuments, new ConfigTransformer()), "configs", "cfg");
  }

  private Map<ObjectId, Integer> writePortfolios() throws IOException {
    PortfolioSearchIterator searchIterator = new PortfolioSearchIterator(_portfolioMaster, new PortfolioSearchRequest());
    Iterator<PortfolioDocument> filteredDocuments = filter(searchIterator, _masterFilterManager.getPortfolioFilter());
    return write(transform(filteredDocuments, new PortfolioTransformer()), "portfolios", "prt");
  }

  private Map<ObjectId, Integer> writeTimeSeries() throws IOException {
    HistoricalTimeSeriesInfoSearchIterator searchIterator = new HistoricalTimeSeriesInfoSearchIterator(_timeSeriesMaster, new HistoricalTimeSeriesInfoSearchRequest());
    Iterator<HistoricalTimeSeriesInfoDocument> filteredDocuments = filter(searchIterator, _masterFilterManager.getHtsFilter());
    return write(transform(filteredDocuments, new TimeSeriesTransformer()), "timeseries", "hts");
  }

  private Map<ObjectId, Integer> writeHolidays() throws IOException {
    HolidaySearchIterator searchIterator = new HolidaySearchIterator(_holidayMaster, new HolidaySearchRequest());
    Iterator<HolidayDocument> filteredDocuments = filter(searchIterator, _masterFilterManager.getHolidayFilter());
    return write(transform(filteredDocuments, new HolidayTransformer()), "holidays", "hol");
  }

  private Map<ObjectId, Integer> writeExchanges() throws IOException {
    ExchangeSearchIterator searchIterator = new ExchangeSearchIterator(_exchangeMaster, new ExchangeSearchRequest());
    Iterator<ExchangeDocument> filteredDocuments = filter(searchIterator, _masterFilterManager.getExchangeFilter());
    return write(transform(filteredDocuments, new ExchangeTransformer()), "exchanges", "exg");
  }

  private Map<ObjectId, Integer> writeSnapshots() throws IOException {
    MarketDataSnapshotSearchIterator searchIterator = new MarketDataSnapshotSearchIterator(_snapshotMaster, new MarketDataSnapshotSearchRequest());
    Iterator<MarketDataSnapshotDocument> filteredDocuments = filter(searchIterator, _masterFilterManager.getMarketDataSnapshotFilter());
    return write(transform(filteredDocuments, new SnapshotTransformer()), "snapshots", "snp");
  }

  private Map<ObjectId, Integer> writeOrganizations() throws IOException {
    OrganizationSearchIterator searchIterator = new OrganizationSearchIterator(_organizationMaster, new OrganizationSearchRequest());
    Iterator<OrganizationDocument> filteredDocuments = filter(searchIterator, _masterFilterManager.getOrganizationFilter());
    return write(transform(filteredDocuments, new OrganizationTransformer()), "organizations", "org");
  }

  private Map<ObjectId, Integer> write(Iterator<? extends UniqueIdentifiable> objects, String type, String prefix) throws IOException {
    s_logger.info("Writing {} to {}", type, _io.getBaseFile().getAbsolutePath());
    final Map<ObjectId, Integer> ids = Maps.newHashMap();
    final Map<String, Object> toWrite = Maps.newHashMap();
    int count = 0;
    while (objects.hasNext()) {
      UniqueIdentifiable object = objects.next();
      ObjectId objectId = object.getUniqueId().getObjectId();
      Integer previousId = _idMappings.getId(objectId);
      int id;
      if (previousId == null) {
        id = _nextId++;
        ids.put(objectId, id);
      } else {
        id = previousId;
      }
      toWrite.put(prefix + id, object);
      count++;
    }
    _io.write(type, toWrite);
    s_logger.info("Wrote {} objects", count);
    return ids;
  }

  private class SecurityTransformer implements Function<SecurityDocument, ManageableSecurity> {
    @Override
    public ManageableSecurity apply(SecurityDocument input) {
      return input.getSecurity();
    }
  }
  
  private class PositionTransformer implements Function<PositionDocument, ManageablePosition> {
    @Override
    public ManageablePosition apply(PositionDocument input) {
      return input.getPosition();
    }
  }
  
  private class PortfolioTransformer implements Function<PortfolioDocument, ManageablePortfolio> {
    @Override
    public ManageablePortfolio apply(PortfolioDocument input) {
      return input.getPortfolio();
    }
  }
  
  private class ConfigTransformer implements Function<ConfigDocument, ConfigItem<?>> {

    @Override
    public ConfigItem<?> apply(ConfigDocument input) {
      return input.getConfig();
    }
  }
  
  private class TimeSeriesTransformer implements Function<HistoricalTimeSeriesInfoDocument, TimeSeriesWithInfo> {
    
    @Override
    public TimeSeriesWithInfo apply(HistoricalTimeSeriesInfoDocument infoDoc) {
      ManageableHistoricalTimeSeriesInfo info = infoDoc.getInfo();
      ManageableHistoricalTimeSeries timeSeries =
          _timeSeriesMaster.getTimeSeries(info.getTimeSeriesObjectId(), VersionCorrection.LATEST);
      TimeSeriesWithInfo timeSeriesWithInfo = new TimeSeriesWithInfo(info, timeSeries);
      return timeSeriesWithInfo;
    }
  }
  
  private class HolidayTransformer implements Function<HolidayDocument, ManageableHoliday> {
    @Override
    public ManageableHoliday apply(HolidayDocument input) {
      return input.getHoliday();
    }
  }
  
  private class ExchangeTransformer implements Function<ExchangeDocument, ManageableExchange> {
    @Override
    public ManageableExchange apply(ExchangeDocument input) {
      return input.getExchange();
    }
  }
  
  private class SnapshotTransformer implements Function<MarketDataSnapshotDocument, ManageableMarketDataSnapshot> {
    @Override
    public ManageableMarketDataSnapshot apply(MarketDataSnapshotDocument input) {
      return input.getSnapshot();
    }
  }
  
  private class OrganizationTransformer implements Function<OrganizationDocument, ManageableOrganization> {
    @Override
    public ManageableOrganization apply(OrganizationDocument input) {
      return input.getOrganization();
    }
  }
  
  
}
