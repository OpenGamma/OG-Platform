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
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.integration.marketdata.manipulator.dsl.RemoteServer;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
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

  public DataReader(File dataDir,
                    SecurityMaster securityMaster,
                    PositionMaster positionMaster,
                    PortfolioMaster portfolioMaster,
                    ConfigMaster configMaster) {
    ArgumentChecker.notNull(dataDir, "dataDir");
    _securityMaster = securityMaster;
    _positionMaster = positionMaster;
    _portfolioMaster = portfolioMaster;
    _configMaster = configMaster;
    _dataDir = dataDir;
  }

  public static void main(String[] args) throws IOException {
    try {
      RemoteServer server = RemoteServer.create("http://localhost:8080");
      DataReader dataReader = new DataReader(new File("/Users/chris/tmp/regression"),
                                             server.getSecurityMaster(),
                                             server.getPositionMaster(),
                                             server.getPortfolioMaster(),
                                             server.getConfigMaster());
      //Map<UniqueId, UniqueId> securityIdMappings = dataReader.loadSecurities();
      Map<ObjectId, ObjectId> positionIdMappings = dataReader.loadPositions();
      Map<UniqueId, UniqueId> portfolioIdMappings = dataReader.loadPortfolios(positionIdMappings);
      dataReader.loadConfigs(portfolioIdMappings);
    } catch (Exception e) {
      s_logger.warn("Failed to read Fudge data", e);
    }
    System.exit(0);
  }

  private Map<UniqueId, UniqueId> loadSecurities() throws IOException {
    List<?> securities = readFromFile("securities.xml");
    Map<UniqueId, UniqueId> ids = Maps.newHashMapWithExpectedSize(securities.size());
    for (Object o : securities) {
      ManageableSecurity security = (ManageableSecurity) o;
      UniqueId oldId = security.getUniqueId();
      security.setUniqueId(null);
      SecurityDocument doc = _securityMaster.add(new SecurityDocument(security));
      ids.put(oldId, doc.getUniqueId());
    }
    return ids;
  }

  private Map<ObjectId, ObjectId> loadPositions() throws IOException {
    List<?> positions = readFromFile("positions.xml");
    Map<ObjectId, ObjectId> ids = Maps.newHashMapWithExpectedSize(positions.size());
    for (Object o : positions) {
      ManageablePosition position = (ManageablePosition) o;
      ObjectId oldId = position.getUniqueId().getObjectId();
      position.setUniqueId(null);
      if (position.getSecurityLink().getObjectId() != null) {
        s_logger.warn("Position {} has non-null ObjectId in its SecurityLink", position);
      }
      if (position.getSecurityLink().getExternalId() == null) {
        s_logger.warn("Position {} has null ExternalIdBundle", position);
      }
      PositionDocument doc = _positionMaster.add(new PositionDocument(position));
      ids.put(oldId, doc.getUniqueId().getObjectId());
    }
    return ids;
  }

  private Map<UniqueId, UniqueId> loadPortfolios(Map<ObjectId, ObjectId> positionIdMappings) throws IOException {
    List<?> portfolios = readFromFile("portfolios.xml");
    Map<UniqueId, UniqueId> idMappings = Maps.newHashMapWithExpectedSize(portfolios.size());
    for (Object o : portfolios) {
      ManageablePortfolio portfolio = (ManageablePortfolio) o;
      UniqueId oldId = portfolio.getUniqueId();
      portfolio.setUniqueId(null);
      // TODO this is temporary
      portfolio.setName(portfolio.getName() + " uploaded");
      replacePositionIds(portfolio.getRootNode(), positionIdMappings);
      UniqueId newId = _portfolioMaster.add(new PortfolioDocument(portfolio)).getUniqueId();
      s_logger.info("Saved portfolio with ID {}, {}", newId, portfolio);
      idMappings.put(newId, oldId);
    }
    return idMappings;
  }

  private void loadConfigs(Map<UniqueId, UniqueId> portfolioIdMappings) throws IOException {
    List<?> configs = readFromFile("configs.xml");
    List<ViewDefinition> viewDefs = Lists.newArrayList();
    // view definitions refer to other config items by unique ID
    Map<UniqueId, UniqueId> idMappings = Maps.newHashMap();
    for (Object o : configs) {
      ConfigItem<?> config = (ConfigItem<?>) o;
      config.setUniqueId(null);
      config.setName(config.getName() + " uploaded");
      Object configValue = config.getValue();
      if (configValue instanceof ViewDefinition) {
        viewDefs.add((ViewDefinition) configValue);
      } else {
        UniqueId oldId = config.getUniqueId();
        UniqueId newId = _configMaster.add(new ConfigDocument(config)).getUniqueId();
        s_logger.info("Saved config with ID {} of type {}", newId, configValue.getClass().getSimpleName());
        idMappings.put(oldId, newId);
      }
    }
    // TODO maybe this should be pluggable to handle new config types that need post processing
    for (ViewDefinition viewDef : viewDefs) {
      UniqueId oldPortfolioId = viewDef.getPortfolioId();
      UniqueId newPortfolioId;
      if (oldPortfolioId != null) {
        if (portfolioIdMappings.containsKey(oldPortfolioId)) {
          newPortfolioId = portfolioIdMappings.get(oldPortfolioId);
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
