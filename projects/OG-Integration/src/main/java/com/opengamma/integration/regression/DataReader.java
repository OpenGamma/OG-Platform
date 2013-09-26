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
import java.io.StringReader;
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
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.integration.marketdata.manipulator.dsl.RemoteServer;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ExchangeMaster;
import com.opengamma.master.exchange.ManageableExchange;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeries;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.ManageableHoliday;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.orgs.ManageableOrganization;
import com.opengamma.master.orgs.OrganizationDocument;
import com.opengamma.master.orgs.OrganizationMaster;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 *
 */
/* package */ class DataReader {

  private static final Logger s_logger = LoggerFactory.getLogger(DataReader.class);

  private final File _dataDir;
  private final SecurityMaster _securityMaster;
  private final PositionMaster _positionMaster;
  private final PortfolioMaster _portfolioMaster;
  private final ConfigMaster _configMaster;
  private final HistoricalTimeSeriesMaster _timeSeriesMaster;
  private final HolidayMaster _holidayMaster;
  private final ExchangeMaster _exchangeMaster;
  private final MarketDataSnapshotMaster _snapshotMaster;
  private final OrganizationMaster _organizationMaster;

  public DataReader(File dataDir,
                    SecurityMaster securityMaster,
                    PositionMaster positionMaster,
                    PortfolioMaster portfolioMaster,
                    ConfigMaster configMaster,
                    HistoricalTimeSeriesMaster timeSeriesMaster,
                    HolidayMaster holidayMaster,
                    ExchangeMaster exchangeMaster,
                    MarketDataSnapshotMaster snapshotMaster,
                    OrganizationMaster organizationMaster) {
    _exchangeMaster = exchangeMaster;
    _snapshotMaster = snapshotMaster;
    _organizationMaster = organizationMaster;
    _holidayMaster = holidayMaster;
    _timeSeriesMaster = timeSeriesMaster;
    _securityMaster = securityMaster;
    _positionMaster = positionMaster;
    _portfolioMaster = portfolioMaster;
    _configMaster = configMaster;
    _dataDir = dataDir;
    ArgumentChecker.notNull(dataDir, "dataDir");
    ArgumentChecker.notNull(exchangeMaster, "exchangeMaster");
    ArgumentChecker.notNull(snapshotMaster, "snapshotMaster");
    ArgumentChecker.notNull(organizationMaster, "organizationMaster");
    ArgumentChecker.notNull(timeSeriesMaster, "timeSeriesMaster");
    ArgumentChecker.notNull(securityMaster, "securityMaster");
    ArgumentChecker.notNull(positionMaster, "positionMaster");
    ArgumentChecker.notNull(portfolioMaster, "portfolioMaster");
    ArgumentChecker.notNull(configMaster, "configMaster");
  }

  public static void main(String[] args) throws IOException {
    try {
      RemoteServer server = RemoteServer.create("http://localhost:8080");
      DataReader dataReader = new DataReader(new File("/Users/chris/tmp/regression"),
                                             server.getSecurityMaster(),
                                             server.getPositionMaster(),
                                             server.getPortfolioMaster(),
                                             server.getConfigMaster(),
                                             server.getHistoricalTimeSeriesMaster(),
                                             server.getHolidayMaster(),
                                             server.getExchangeMaster(),
                                             server.getMarketDataSnapshotMaster(),
                                             server.getOrganizationMaster());
      Map<ObjectId, ObjectId> securityIdMappings = dataReader.loadSecurities();
      Map<ObjectId, ObjectId> positionIdMappings = dataReader.loadPositions(securityIdMappings);
      Map<ObjectId, ObjectId> portfolioIdMappings = dataReader.loadPortfolios(positionIdMappings);
      dataReader.loadConfigs(portfolioIdMappings);
      dataReader.loadTimeSeries();
      dataReader.loadHolidays();
      dataReader.loadExchanges();
      dataReader.loadSnapshots();
      dataReader.loadOrganizations();
    } catch (Exception e) {
      s_logger.warn("Failed to read Fudge data", e);
    }
    System.exit(0);
  }

  private Map<ObjectId, ObjectId> loadSecurities() throws IOException {
    List<?> securities = readFromFile("securities.xml");
    Map<ObjectId, ObjectId> ids = Maps.newHashMapWithExpectedSize(securities.size());
    for (Object o : securities) {
      ManageableSecurity security = (ManageableSecurity) o;
      ObjectId oldId = security.getUniqueId().getObjectId();
      security.setUniqueId(null);
      SecurityDocument doc = _securityMaster.add(new SecurityDocument(security));
      ids.put(oldId, doc.getUniqueId().getObjectId());
    }
    return ids;
  }

  private Map<ObjectId, ObjectId> loadPositions(Map<ObjectId, ObjectId> securityIdMappings) throws IOException {
    List<?> positions = readFromFile("positions.xml");
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
      PositionDocument doc = _positionMaster.add(new PositionDocument(position));
      ids.put(oldId, doc.getUniqueId().getObjectId());
    }
    return ids;
  }

  private Map<ObjectId, ObjectId> loadPortfolios(Map<ObjectId, ObjectId> positionIdMappings) throws IOException {
    List<?> portfolios = readFromFile("portfolios.xml");
    Map<ObjectId, ObjectId> idMappings = Maps.newHashMapWithExpectedSize(portfolios.size());
    for (Object o : portfolios) {
      ManageablePortfolio portfolio = (ManageablePortfolio) o;
      UniqueId oldId = portfolio.getUniqueId();
      portfolio.setUniqueId(null);
      replacePositionIds(portfolio.getRootNode(), positionIdMappings);
      UniqueId newId = _portfolioMaster.add(new PortfolioDocument(portfolio)).getUniqueId();
      s_logger.info("Saved portfolio with ID {}, {}", newId, portfolio);
      idMappings.put(oldId.getObjectId(), newId.getObjectId());
    }
    return idMappings;
  }

  private void loadConfigs(Map<ObjectId, ObjectId> portfolioIdMappings) throws IOException {
    List<?> configs = readFromFile("configs.xml");
    List<ViewDefinition> viewDefs = Lists.newArrayList();
    // view definitions refer to other config items by unique ID
    Map<UniqueId, UniqueId> idMappings = Maps.newHashMap();
    for (Object o : configs) {
      ConfigItem<?> config = (ConfigItem<?>) o;
      Object configValue = config.getValue();
      if (configValue instanceof ViewDefinition) {
        viewDefs.add((ViewDefinition) configValue);
      } else {
        UniqueId oldId = config.getUniqueId();
        config.setUniqueId(null);
        UniqueId newId = _configMaster.add(new ConfigDocument(config)).getUniqueId();
        s_logger.info("Saved config of type {} with ID {}", configValue.getClass().getSimpleName(), newId);
        idMappings.put(oldId, newId);
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
        calcConfig.setScenarioId(idMappings.get(calcConfig.getScenarioId()));
        calcConfig.setScenarioParametersId(idMappings.get(calcConfig.getScenarioParametersId()));
      }
      UniqueId newId = _configMaster.add(new ConfigDocument(ConfigItem.of(viewDef))).getUniqueId();
      s_logger.info("Saved view definition with ID {}", newId);
    }
  }

  private void loadTimeSeries() throws IOException {
    List<?> objects = readFromFile("timeseries.xml");
    // TODO check size is even
    for (int i = 0; i < objects.size(); i += 2) {
      ManageableHistoricalTimeSeries timeSeries = (ManageableHistoricalTimeSeries) objects.get(i);
      ManageableHistoricalTimeSeriesInfo timeSeriesInfo = (ManageableHistoricalTimeSeriesInfo) objects.get(i + 1);
      timeSeriesInfo.setUniqueId(null);
      HistoricalTimeSeriesInfoDocument infoDoc = _timeSeriesMaster.add(new HistoricalTimeSeriesInfoDocument(timeSeriesInfo));
      _timeSeriesMaster.updateTimeSeriesDataPoints(infoDoc.getInfo().getTimeSeriesObjectId(), timeSeries.getTimeSeries());
    }
  }

  private void loadHolidays() throws IOException {
    List<?> holidays = readFromFile("holidays.xml");
    for (Object o : holidays) {
      ManageableHoliday holiday = (ManageableHoliday) o;
      holiday.setUniqueId(null);
      _holidayMaster.add(new HolidayDocument(holiday));
    }
  }

  private void loadExchanges() throws IOException {
    List<?> exchanges = readFromFile("exchanges.xml");
    for (Object o : exchanges) {
      ManageableExchange exchange = (ManageableExchange) o;
      exchange.setUniqueId(null);
      _exchangeMaster.add(new ExchangeDocument(exchange));
    }
  }

  private void loadSnapshots() throws IOException {
    List<?> snapshots = readFromFile("snapshots.xml");
    for (Object o : snapshots) {
      ManageableMarketDataSnapshot snapshot = (ManageableMarketDataSnapshot) o;
      snapshot.setUniqueId(null);
      _snapshotMaster.add(new MarketDataSnapshotDocument(snapshot));
    }
  }

  private void loadOrganizations() throws IOException {
    List<?> organizations = readFromFile("organizations.xml");
    for (Object o : organizations) {
      ManageableOrganization organization = (ManageableOrganization) o;
      organization.setUniqueId(null);
      _organizationMaster.add(new OrganizationDocument(organization));
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

  private List<?> readFromFile(String fileName) throws IOException {
    File file = new File(_dataDir, fileName);
    s_logger.info("Reading from {}", file.getAbsolutePath());
    List<Object> objects = Lists.newArrayList();
    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      FudgeContext ctx = OpenGammaFudgeContext.getInstance();
      FudgeDeserializer deserializer = new FudgeDeserializer(ctx);
      String line;
      while ((line = reader.readLine()) != null) {
        FudgeXMLStreamReader streamReader = new FudgeXMLStreamReader(ctx, new StringReader(line));
        FudgeMsgReader fudgeMsgReader = new FudgeMsgReader(streamReader);
        FudgeMsg msg = fudgeMsgReader.nextMessage();
        Object object = deserializer.fudgeMsgToObject(msg);
        s_logger.debug("Read object {}", object);
        objects.add(object);
      }
      s_logger.info("Read {} objects from {}", objects.size(), file.getAbsolutePath());
    }
    return objects;
  }
}
