/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.wire.FudgeMsgWriter;
import org.fudgemsg.wire.xml.FudgeXMLStreamWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.OpenGammaRuntimeException;
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
import com.opengamma.master.legalentity.LegalEntityMaster;
import com.opengamma.master.legalentity.LegalEntitySearchRequest;
import com.opengamma.master.legalentity.LegalEntitySearchResult;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchResult;
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
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Dumps all the data required to run views from the database into Fudge XML files.
 * TODO split this up to allow a subset of data to be dumped and restored?
 */
/* package */ class DatabaseDump {

  private static final Logger s_logger = LoggerFactory.getLogger(DatabaseDump.class);

  private final File _outputDir;
  private final SecurityMaster _securityMaster;
  private final PositionMaster _positionMaster;
  private final PortfolioMaster _portfolioMaster;
  private final ConfigMaster _configMaster;
  private final HistoricalTimeSeriesMaster _timeSeriesMaster;
  private final HolidayMaster _holidayMaster;
  private final ExchangeMaster _exchangeMaster;
  private final MarketDataSnapshotMaster _snapshotMaster;
  private final LegalEntityMaster _legalEntityMaster;
  private final IdMappings _idMappings;
  private final FudgeContext _ctx = new FudgeContext(OpenGammaFudgeContext.getInstance());
  private final FudgeSerializer _serializer = new FudgeSerializer(OpenGammaFudgeContext.getInstance());

  private int _nextId;

  /* package */ DatabaseDump(String outputDir,
                             SecurityMaster securityMaster,
                             PositionMaster positionMaster,
                             PortfolioMaster portfolioMaster,
                             ConfigMaster configMaster,
                             HistoricalTimeSeriesMaster timeSeriesMaster,
                             HolidayMaster holidayMaster,
                             ExchangeMaster exchangeMaster,
                             MarketDataSnapshotMaster snapshotMaster,
                             LegalEntityMaster legalEntityMaster) {
    ArgumentChecker.notNull(securityMaster, "securityMaster");
    ArgumentChecker.notNull(outputDir, "outputDir");
    ArgumentChecker.notNull(positionMaster, "positionMaster");
    ArgumentChecker.notNull(portfolioMaster, "portfolioMaster");
    ArgumentChecker.notNull(configMaster, "configMaster");
    ArgumentChecker.notNull(timeSeriesMaster, "timeSeriesMaster");
    _legalEntityMaster = legalEntityMaster;
    _snapshotMaster = snapshotMaster;
    _exchangeMaster = exchangeMaster;
    _holidayMaster = holidayMaster;
    _timeSeriesMaster = timeSeriesMaster;
    _positionMaster = positionMaster;
    _portfolioMaster = portfolioMaster;
    _configMaster = configMaster;
    _outputDir = new File(outputDir);
    _securityMaster = securityMaster;
    ConfigItem<IdMappings> mappingsConfigItem = RegressionUtils.loadIdMappings(_configMaster);
    if (mappingsConfigItem != null) {
      _idMappings = mappingsConfigItem.getValue();
    } else {
      _idMappings = new IdMappings();
    }
    _nextId = _idMappings.getMaxId() + 1;
    if (!_outputDir.exists()) {
      boolean success = _outputDir.mkdirs();
      if (!success) {
        throw new OpenGammaRuntimeException("Output directory " + outputDir + " couldn't be created");
      }
      s_logger.info("Created output directory {}", _outputDir.getAbsolutePath());
    }
    s_logger.info("Dumping database to {}", _outputDir.getAbsolutePath());
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
      DatabaseDump databaseDump = new DatabaseDump(dataDir,
                                                   server.getSecurityMaster(),
                                                   server.getPositionMaster(),
                                                   server.getPortfolioMaster(),
                                                   server.getConfigMaster(),
                                                   server.getHistoricalTimeSeriesMaster(),
                                                   server.getHolidayMaster(),
                                                   server.getExchangeMaster(),
                                                   server.getMarketDataSnapshotMaster(),
                                                   server.getLegalEntityMaster());
      databaseDump.dumpDatabase();
    } catch (Exception e) {
      s_logger.warn("Failed to write data", e);
      exitCode = 1;
    }
    System.exit(exitCode);
  }

  public void dumpDatabase() throws IOException {
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
    writeToFudge(_outputDir, idMappings, RegressionUtils.ID_MAPPINGS_FILE);
  }

  private Map<ObjectId, Integer> writeSecurities() throws IOException {
    SecuritySearchResult result = _securityMaster.search(new SecuritySearchRequest());
    return writeToDirectory(result.getSecurities(), "securities", "sec");
  }

  private Map<ObjectId, Integer> writePositions() throws IOException {
    PositionSearchResult result = _positionMaster.search(new PositionSearchRequest());
    return writeToDirectory(result.getPositions(), "positions", "pos");
  }

  private Map<ObjectId, Integer> writeConfig() throws IOException {
    ConfigSearchResult<Object> result = _configMaster.search(new ConfigSearchRequest<>(Object.class));
    return writeToDirectory(result.getValues(), "configs", "cfg");
  }

  private Map<ObjectId, Integer> writePortfolios() throws IOException {
    PortfolioSearchResult result = _portfolioMaster.search(new PortfolioSearchRequest());
    return writeToDirectory(result.getPortfolios(), "portfolios", "prt");
  }

  private Map<ObjectId, Integer> writeTimeSeries() throws IOException {
    List<TimeSeriesWithInfo> objects = Lists.newArrayList();
    HistoricalTimeSeriesInfoSearchResult infoResult = _timeSeriesMaster.search(new HistoricalTimeSeriesInfoSearchRequest());
    for (ManageableHistoricalTimeSeriesInfo info : infoResult.getInfoList()) {
      ManageableHistoricalTimeSeries timeSeries =
          _timeSeriesMaster.getTimeSeries(info.getTimeSeriesObjectId(), VersionCorrection.LATEST);
      TimeSeriesWithInfo timeSeriesWithInfo = new TimeSeriesWithInfo(info, timeSeries);
      objects.add(timeSeriesWithInfo);
    }
    return writeToDirectory(objects, "timeseries", "hts");
  }

  private Map<ObjectId, Integer> writeHolidays() throws IOException {
    HolidaySearchResult result = _holidayMaster.search(new HolidaySearchRequest());
    return writeToDirectory(result.getHolidays(), "holidays", "hol");
  }

  private Map<ObjectId, Integer> writeExchanges() throws IOException {
    ExchangeSearchResult result = _exchangeMaster.search(new ExchangeSearchRequest());
    return writeToDirectory(result.getExchanges(), "exchanges", "exg");
  }

  private Map<ObjectId, Integer> writeSnapshots() throws IOException {
    MarketDataSnapshotSearchResult result = _snapshotMaster.search(new MarketDataSnapshotSearchRequest());
    return writeToDirectory(result.getSnapshots(), "snapshots", "snp");
  }

  private Map<ObjectId, Integer> writeOrganizations() throws IOException {
    LegalEntitySearchResult result = _legalEntityMaster.search(new LegalEntitySearchRequest());
    return writeToDirectory(result.getLegalEntities(), "legalentities", "org");
  }

  private Map<ObjectId, Integer> writeToDirectory(List<? extends UniqueIdentifiable> objects,
                                                  String outputSubDirName,
                                                  String prefix) throws IOException {
    File outputDir = new File(_outputDir, outputSubDirName);
    if (!outputDir.exists()) {
      boolean success = outputDir.mkdir();
      if (success) {
        s_logger.debug("Created directory {}", outputDir);
      } else {
        throw new OpenGammaRuntimeException("Failed to create directory " + outputDir);
      }
    }
    s_logger.info("Writing to {}", outputDir.getAbsolutePath());
    Map<ObjectId, Integer> ids = Maps.newHashMap();
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
      String fileName = prefix + id + ".xml";
      writeToFudge(outputDir, object, fileName);
    }
    s_logger.info("Wrote {} objects to {}", objects.size(), outputDir.getAbsolutePath());
    return ids;
  }

  private void writeToFudge(File outputDir, Object object, String fileName) throws IOException {
    try (FileWriter writer = new FileWriter(new File(outputDir, fileName))) {
      FudgeXMLStreamWriter streamWriter = new FudgeXMLStreamWriter(_ctx, writer);
      FudgeMsgWriter fudgeMsgWriter = new FudgeMsgWriter(streamWriter);
      MutableFudgeMsg msg = _serializer.objectToFudgeMsg(object);
      FudgeSerializer.addClassHeader(msg, object.getClass());
      fudgeMsgWriter.writeMessage(msg);
      writer.append("\n");
      s_logger.debug("Wrote object {}", object);
      fudgeMsgWriter.flush();
    }
  }
}
