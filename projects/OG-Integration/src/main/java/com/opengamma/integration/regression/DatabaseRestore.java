/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.wire.FudgeMsgReader;
import org.fudgemsg.wire.xml.FudgeXMLStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.integration.marketdata.manipulator.dsl.RemoteServer;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ManageableExchange;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeries;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.ManageableHoliday;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.orgs.ManageableOrganization;
import com.opengamma.master.orgs.OrganizationDocument;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Loads the data required to run views from Fudge XML files into an empty database.
 * TODO split this up to allow a subset of data to be dumped and restored
 */
/* package */ class DatabaseRestore {

  /** Attribute name holding a position's original unique ID from the source database. */
  public static final String REGRESSION_ID = "regression:id";

  private static final Logger s_logger = LoggerFactory.getLogger(DatabaseRestore.class);

  private final File _dataDir;
  private final ToolContext _toolContext;

  // TODO server connection details. presumably http://localhost, what about port?
  public static void restoreDatabase(String dataDir, ToolContext toolContext) {
    try {
      DatabaseRestore databaseRestore = new DatabaseRestore(new File(dataDir), toolContext);
      Map<ObjectId, ObjectId> securityIdMappings = databaseRestore.loadSecurities();
      Map<ObjectId, ObjectId> positionIdMappings = databaseRestore.loadPositions(securityIdMappings);
      Map<ObjectId, ObjectId> portfolioIdMappings = databaseRestore.loadPortfolios(positionIdMappings);
      databaseRestore.loadConfigs(portfolioIdMappings);
      databaseRestore.loadTimeSeries();
      databaseRestore.loadHolidays();
      databaseRestore.loadExchanges();
      databaseRestore.loadSnapshots();
      databaseRestore.loadOrganizations();
      s_logger.info("Successfully restored database");
    } catch (IOException e) {
      throw new OpenGammaRuntimeException("Failed to restore database", e);
    }
  }

  private DatabaseRestore(File dataDir, ToolContext toolContext) {
    ArgumentChecker.notNull(dataDir, "dataDir");
    ArgumentChecker.notNull(toolContext, "toolContext");
    _toolContext = toolContext;
    _dataDir = dataDir;
  }

  public static void main(String[] args) throws IOException {
    String dataDir = "/Users/chris/tmp/regression";
    try (RemoteServer server = RemoteServer.create("http://localhost:8080")) {
      DatabaseRestore.restoreDatabase(dataDir, server);
    }
  }

  private Map<ObjectId, ObjectId> loadSecurities() throws IOException {
    List<?> securities = readFromDirectory("securities");
    Map<ObjectId, ObjectId> ids = Maps.newHashMapWithExpectedSize(securities.size());
    for (Object o : securities) {
      ManageableSecurity security = (ManageableSecurity) o;
      ObjectId oldId = security.getUniqueId().getObjectId();
      security.setUniqueId(null);
      SecurityDocument doc = _toolContext.getSecurityMaster().add(new SecurityDocument(security));
      ids.put(oldId, doc.getUniqueId().getObjectId());
    }
    return ids;
  }

  private Map<ObjectId, ObjectId> loadPositions(Map<ObjectId, ObjectId> securityIdMappings) throws IOException {
    List<?> positions = readFromDirectory("positions");
    Map<ObjectId, ObjectId> ids = Maps.newHashMapWithExpectedSize(positions.size());
    for (Object o : positions) {
      ManageablePosition position = (ManageablePosition) o;
      ObjectId oldId = position.getUniqueId().getObjectId();
      position.setUniqueId(null);
      ObjectId securityObjectId = position.getSecurityLink().getObjectId();
      if (securityObjectId != null) {
        ObjectId newObjectId = securityIdMappings.get(securityObjectId);
        position.getSecurityLink().setObjectId(newObjectId);
        if (newObjectId == null) {
          s_logger.warn("No security found with ID {} for position {}", securityObjectId, position);
        }
      }
      for (ManageableTrade trade : position.getTrades()) {
        trade.addAttribute(REGRESSION_ID, trade.getUniqueId().getObjectId().toString());
        trade.setUniqueId(null);
        trade.setParentPositionId(null);
      }
      // put the old ID on as an attribute. this allows different instances of a position or trade to be identified
      // when they're saved in different databases and therefore have different unique IDs
      position.addAttribute(REGRESSION_ID, oldId.toString());
      PositionDocument doc = _toolContext.getPositionMaster().add(new PositionDocument(position));
      ObjectId newId = doc.getUniqueId().getObjectId();
      ids.put(oldId, newId);
    }
    return ids;
  }

  private Map<ObjectId, ObjectId> loadPortfolios(Map<ObjectId, ObjectId> positionIdMappings) throws IOException {
    List<?> portfolios = readFromDirectory("portfolios");
    Map<ObjectId, ObjectId> idMappings = Maps.newHashMapWithExpectedSize(portfolios.size());
    for (Object o : portfolios) {
      ManageablePortfolio portfolio = (ManageablePortfolio) o;
      UniqueId oldId = portfolio.getUniqueId();
      portfolio.setUniqueId(null);
      replacePositionIds(portfolio.getRootNode(), positionIdMappings);
      UniqueId newId = _toolContext.getPortfolioMaster().add(new PortfolioDocument(portfolio)).getUniqueId();
      s_logger.debug("Saved portfolio {} with ID {}, old ID {}", portfolio.getName(), newId, oldId);
      idMappings.put(oldId.getObjectId(), newId.getObjectId());
    }
    return idMappings;
  }

  private void loadConfigs(Map<ObjectId, ObjectId> portfolioIdMappings) throws IOException {
    List<?> configs = readFromDirectory("configs");
    List<ViewDefinition> viewDefs = Lists.newArrayList();
    // view definitions refer to other config items by unique ID
    Map<ObjectId, ObjectId> idMappings = Maps.newHashMap();
    for (Object o : configs) {
      ConfigItem<?> config = (ConfigItem<?>) o;
      Object configValue = config.getValue();
      if (configValue instanceof ViewDefinition) {
        viewDefs.add((ViewDefinition) configValue);
      } else {
        UniqueId oldId = config.getUniqueId();
        config.setUniqueId(null);
        UniqueId newId = _toolContext.getConfigMaster().add(new ConfigDocument(config)).getUniqueId();
        s_logger.debug("Saved config of type {} with ID {}", configValue.getClass().getSimpleName(), newId);
        idMappings.put(oldId.getObjectId(), newId.getObjectId());
      }
    }
    // TODO maybe this should be pluggable to handle new config types that need post processing
    for (ViewDefinition viewDef : viewDefs) {
      ObjectId oldPortfolioId = viewDef.getPortfolioId().getObjectId();
      UniqueId newPortfolioId;
      if (oldPortfolioId != null) {
        if (portfolioIdMappings.containsKey(oldPortfolioId)) {
          newPortfolioId = portfolioIdMappings.get(oldPortfolioId).atLatestVersion();
        } else {
          newPortfolioId = null;
          s_logger.warn("No mapping found for view def portfolio ID {}", oldPortfolioId);
        }
      } else {
        newPortfolioId = null;
      }
      ViewDefinition newViewDef = viewDef.copyWith(viewDef.getName(), newPortfolioId, viewDef.getMarketDataUser());
      for (ViewCalculationConfiguration calcConfig : newViewDef.getAllCalculationConfigurations()) {
        calcConfig.setScenarioId(getNewId(calcConfig.getScenarioId(), idMappings));
        calcConfig.setScenarioParametersId(getNewId(calcConfig.getScenarioParametersId(), idMappings));
      }
      UniqueId newId = _toolContext.getConfigMaster().add(new ConfigDocument(ConfigItem.of(newViewDef))).getUniqueId();
      s_logger.debug("Saved view definition with ID {}", newId);
    }
  }

  private static UniqueId getNewId(UniqueId oldId, Map<ObjectId, ObjectId> idMappings) {
    if (oldId == null) {
      return null;
    }
    ObjectId newObjectId = idMappings.get(oldId.getObjectId());
    if (newObjectId == null) {
      return null;
    } else {
      return newObjectId.atLatestVersion();
    }
  }

  private void loadTimeSeries() throws IOException {
    List<?> objects = readFromDirectory("timeseries");
    for (Object o : objects) {
      TimeSeriesWithInfo timeSeriesWithInfo = (TimeSeriesWithInfo) o;
      ManageableHistoricalTimeSeriesInfo info = timeSeriesWithInfo.getInfo();
      ManageableHistoricalTimeSeries timeSeries = timeSeriesWithInfo.getTimeSeries();
      info.setUniqueId(null);
      HistoricalTimeSeriesMaster timeSeriesMaster = _toolContext.getHistoricalTimeSeriesMaster();
      HistoricalTimeSeriesInfoDocument infoDoc = timeSeriesMaster.add(new HistoricalTimeSeriesInfoDocument(info));
      timeSeriesMaster.updateTimeSeriesDataPoints(infoDoc.getInfo().getTimeSeriesObjectId(), timeSeries.getTimeSeries());
    }
  }

  private void loadHolidays() throws IOException {
    List<?> holidays = readFromDirectory("holidays");
    for (Object o : holidays) {
      ManageableHoliday holiday = (ManageableHoliday) o;
      holiday.setUniqueId(null);
      _toolContext.getHolidayMaster().add(new HolidayDocument(holiday));
    }
  }

  private void loadExchanges() throws IOException {
    List<?> exchanges = readFromDirectory("exchanges");
    for (Object o : exchanges) {
      ManageableExchange exchange = (ManageableExchange) o;
      exchange.setUniqueId(null);
      _toolContext.getExchangeMaster().add(new ExchangeDocument(exchange));
    }
  }

  private void loadSnapshots() throws IOException {
    List<?> snapshots = readFromDirectory("snapshots");
    for (Object o : snapshots) {
      ManageableMarketDataSnapshot snapshot = (ManageableMarketDataSnapshot) o;
      snapshot.setUniqueId(null);
      _toolContext.getMarketDataSnapshotMaster().add(new MarketDataSnapshotDocument(snapshot));
    }
  }

  private void loadOrganizations() throws IOException {
    List<?> organizations = readFromDirectory("organizations");
    for (Object o : organizations) {
      ManageableOrganization organization = (ManageableOrganization) o;
      organization.setUniqueId(null);
      _toolContext.getOrganizationMaster().add(new OrganizationDocument(organization));
    }
  }

  private void replacePositionIds(ManageablePortfolioNode node, Map<ObjectId, ObjectId> positionIdMappings) {
    node.setUniqueId(null);
    node.setParentNodeId(null);
    node.setPortfolioId(null);
    List<ObjectId> oldPositionIds = node.getPositionIds();
    List<ObjectId> positionsIds = Lists.newArrayListWithCapacity(oldPositionIds.size());
    for (ObjectId oldPositionId : oldPositionIds) {
      ObjectId newPositionId = positionIdMappings.get(oldPositionId);
      if (newPositionId != null) {
        positionsIds.add(newPositionId);
      } else {
        s_logger.warn("No position ID mapping for {}", oldPositionId);
      }
    }
    node.setPositionIds(positionsIds);
    for (ManageablePortfolioNode childNode : node.getChildNodes()) {
      replacePositionIds(childNode, positionIdMappings);
    }
  }

  private List<?> readFromDirectory(String subDirName) throws IOException {
    File subDir = new File(_dataDir, subDirName);
    if (!subDir.exists()) {
      boolean success = subDir.mkdir();
      if (!success) {
        throw new OpenGammaRuntimeException("Failed to create directory " + subDir);
      }
    }
    s_logger.info("Reading from {}", subDir.getAbsolutePath());
    FudgeContext ctx = OpenGammaFudgeContext.getInstance();
    FudgeDeserializer deserializer = new FudgeDeserializer(ctx);
    List<Object> objects = Lists.newArrayList();
    File[] files = subDir.listFiles();
    if (files == null) {
      throw new OpenGammaRuntimeException("No files found in " + subDir);
    }
    for (File file : files) {
      try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
        FudgeXMLStreamReader streamReader = new FudgeXMLStreamReader(ctx, reader);
        FudgeMsgReader fudgeMsgReader = new FudgeMsgReader(streamReader);
        FudgeMsg msg = fudgeMsgReader.nextMessage();
        Object object = deserializer.fudgeMsgToObject(msg);
        s_logger.debug("Read object {}", object);
        objects.add(object);
      }
    }
    s_logger.info("Read {} objects from {}", objects.size(), subDir.getAbsolutePath());
    return objects;
  }
}
