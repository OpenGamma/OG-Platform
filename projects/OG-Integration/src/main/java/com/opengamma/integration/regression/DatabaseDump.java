/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.VersionCorrection;
import com.opengamma.integration.server.RemoteServer;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.master.exchange.ExchangeMaster;
import com.opengamma.master.exchange.ExchangeSearchRequest;
import com.opengamma.master.exchange.ExchangeSearchResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeries;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.HolidaySearchRequest;
import com.opengamma.master.holiday.HolidaySearchResult;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchResult;
import com.opengamma.master.orgs.OrganizationMaster;
import com.opengamma.master.orgs.OrganizationSearchRequest;
import com.opengamma.master.orgs.OrganizationSearchResult;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.PositionSearchResult;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
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
  private final IdMappings _idMappings;

  private int _nextId;

  /* package */DatabaseDump(String outputDir, SecurityMaster securityMaster, PositionMaster positionMaster, PortfolioMaster portfolioMaster, ConfigMaster configMaster,
      HistoricalTimeSeriesMaster timeSeriesMaster, HolidayMaster holidayMaster, ExchangeMaster exchangeMaster, MarketDataSnapshotMaster snapshotMaster, OrganizationMaster organizationMaster) {
    this(new SubdirsRegressionIO(new File(outputDir), new FudgeXMLFormat(), true), securityMaster, positionMaster, portfolioMaster, configMaster, timeSeriesMaster, holidayMaster,
        exchangeMaster, snapshotMaster, organizationMaster);
  }

  /* package */DatabaseDump(RegressionIO io, SecurityMaster securityMaster, PositionMaster positionMaster, PortfolioMaster portfolioMaster, ConfigMaster configMaster,
      HistoricalTimeSeriesMaster timeSeriesMaster, HolidayMaster holidayMaster, ExchangeMaster exchangeMaster, MarketDataSnapshotMaster snapshotMaster, OrganizationMaster organizationMaster) {
    ArgumentChecker.notNull(io, "io");
    ArgumentChecker.notNull(securityMaster, "securityMaster");
    ArgumentChecker.notNull(positionMaster, "positionMaster");
    ArgumentChecker.notNull(portfolioMaster, "portfolioMaster");
    ArgumentChecker.notNull(configMaster, "configMaster");
    ArgumentChecker.notNull(timeSeriesMaster, "timeSeriesMaster");
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
    SecuritySearchResult result = _securityMaster.search(new SecuritySearchRequest());
    return write(result.getSecurities(), RegressionUtils.SECURITY_MASTER_DATA, "sec");
  }

  private Map<ObjectId, Integer> writePositions() throws IOException {
    PositionSearchResult result = _positionMaster.search(new PositionSearchRequest());
    return write(result.getPositions(), RegressionUtils.POSITION_MASTER_DATA, "pos");
  }

  private Map<ObjectId, Integer> writeConfig() throws IOException {
    ConfigSearchResult<Object> result = _configMaster.search(new ConfigSearchRequest<>(Object.class));
    return write(result.getValues(), RegressionUtils.CONFIG_MASTER_DATA, "cfg");
  }

  private Map<ObjectId, Integer> writePortfolios() throws IOException {
    PortfolioSearchResult result = _portfolioMaster.search(new PortfolioSearchRequest());
    return write(result.getPortfolios(), RegressionUtils.PORTFOLIO_MASTER_DATA, "prt");
  }

  private Map<ObjectId, Integer> writeTimeSeries() throws IOException {
    List<TimeSeriesWithInfo> objects = Lists.newArrayList();
    HistoricalTimeSeriesInfoSearchResult infoResult = _timeSeriesMaster.search(new HistoricalTimeSeriesInfoSearchRequest());
    for (ManageableHistoricalTimeSeriesInfo info : infoResult.getInfoList()) {
      ManageableHistoricalTimeSeries timeSeries = _timeSeriesMaster.getTimeSeries(info.getTimeSeriesObjectId(), VersionCorrection.LATEST);
      TimeSeriesWithInfo timeSeriesWithInfo = new TimeSeriesWithInfo(info, timeSeries);
      objects.add(timeSeriesWithInfo);
    }
    return write(objects, RegressionUtils.HISTORICAL_TIME_SERIES_MASTER_DATA, "hts");
  }

  private Map<ObjectId, Integer> writeHolidays() throws IOException {
    HolidaySearchResult result = _holidayMaster.search(new HolidaySearchRequest());
    return write(result.getHolidays(), RegressionUtils.HOLIDAY_MASTER_DATA, "hol");
  }

  private Map<ObjectId, Integer> writeExchanges() throws IOException {
    ExchangeSearchResult result = _exchangeMaster.search(new ExchangeSearchRequest());
    return write(result.getExchanges(), RegressionUtils.EXCHANGE_MASTER_DATA, "exg");
  }

  private Map<ObjectId, Integer> writeSnapshots() throws IOException {
    MarketDataSnapshotSearchResult result = _snapshotMaster.search(new MarketDataSnapshotSearchRequest());
    return write(result.getSnapshots(), RegressionUtils.MARKET_DATA_SNAPSHOT_MASTER_DATA, "snp");
  }

  private Map<ObjectId, Integer> writeOrganizations() throws IOException {
    OrganizationSearchResult result = _organizationMaster.search(new OrganizationSearchRequest());
    return write(result.getOrganizations(), RegressionUtils.ORGANIZATION_MASTER_DATA, "org");
  }

  private Map<ObjectId, Integer> write(List<? extends UniqueIdentifiable> objects, String type, String prefix) throws IOException {
    s_logger.info("Writing {} to {}", type, _io.getBaseFile().getAbsolutePath());
    final Map<ObjectId, Integer> ids = Maps.newHashMapWithExpectedSize(objects.size());
    final Map<String, Object> toWrite = Maps.newHashMapWithExpectedSize(objects.size());
    for (UniqueIdentifiable object : objects) {
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
    }
    _io.write(type, toWrite);
    s_logger.info("Wrote {} objects", objects.size());
    return ids;
  }

}
