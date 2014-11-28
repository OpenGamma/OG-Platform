/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.integration.server.RemoteServer;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.convention.ConventionDocument;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.master.convention.ManageableConvention;
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
import com.opengamma.master.legalentity.LegalEntityDocument;
import com.opengamma.master.legalentity.LegalEntityMaster;
import com.opengamma.master.legalentity.ManageableLegalEntity;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.ArgumentChecker;

/**
 * Loads the data required to run views from Fudge XML files into an empty database.
 * <p>
 * TODO split this up to allow a subset of data to be dumped and restored?
 */
public class DatabaseRestore {

  /** Attribute name holding a position's original unique ID from the source database. */
  public static final String REGRESSION_ID = "regressionId";

  private static final Logger s_logger = LoggerFactory.getLogger(DatabaseRestore.class);

  private final RegressionIO _io;
  private final SecurityMaster _securityMaster;
  private final PositionMaster _positionMaster;
  private final PortfolioMaster _portfolioMaster;
  private final ConfigMaster _configMaster;
  private final HistoricalTimeSeriesMaster _timeSeriesMaster;
  private final HolidayMaster _holidayMaster;
  private final ExchangeMaster _exchangeMaster;
  private final MarketDataSnapshotMaster _snapshotMaster;
  private final LegalEntityMaster _legalEntityMaster;
  private final ConventionMaster _conventionMaster;

  public DatabaseRestore(String dataDir, SecurityMaster securityMaster, PositionMaster positionMaster, PortfolioMaster portfolioMaster, ConfigMaster configMaster,
      HistoricalTimeSeriesMaster timeSeriesMaster, HolidayMaster holidayMaster, ExchangeMaster exchangeMaster, MarketDataSnapshotMaster snapshotMaster, LegalEntityMaster legalEntityMaster,
      ConventionMaster conventionMaster) {
    this(new File(dataDir), securityMaster, positionMaster, portfolioMaster, configMaster, timeSeriesMaster, holidayMaster, exchangeMaster, snapshotMaster, legalEntityMaster, conventionMaster);
  }

  public DatabaseRestore(File dataDir, SecurityMaster securityMaster, PositionMaster positionMaster, PortfolioMaster portfolioMaster, ConfigMaster configMaster,
      HistoricalTimeSeriesMaster timeSeriesMaster, HolidayMaster holidayMaster, ExchangeMaster exchangeMaster, MarketDataSnapshotMaster snapshotMaster, LegalEntityMaster legalEntityMaster,
      ConventionMaster conventionMaster) {
    this(new SubdirsRegressionIO(dataDir, new FudgeXMLFormat(), false), securityMaster, positionMaster, portfolioMaster, configMaster, timeSeriesMaster, holidayMaster, exchangeMaster,
        snapshotMaster, legalEntityMaster, conventionMaster);
  }

  public DatabaseRestore(RegressionIO io, SecurityMaster securityMaster, PositionMaster positionMaster, PortfolioMaster portfolioMaster, ConfigMaster configMaster,
      HistoricalTimeSeriesMaster timeSeriesMaster, HolidayMaster holidayMaster, ExchangeMaster exchangeMaster, MarketDataSnapshotMaster snapshotMaster, LegalEntityMaster legalEntityMaster,
      ConventionMaster conventionMaster) {
    ArgumentChecker.notNull(io, "io");
    ArgumentChecker.notNull(securityMaster, "securityMaster");
    ArgumentChecker.notNull(positionMaster, "positionMaster");
    ArgumentChecker.notNull(portfolioMaster, "portfolioMaster");
    ArgumentChecker.notNull(configMaster, "configMaster");
    ArgumentChecker.notNull(timeSeriesMaster, "timeSeriesMaster");
    ArgumentChecker.notNull(holidayMaster, "holidayMaster");
    ArgumentChecker.notNull(exchangeMaster, "exchangeMaster");
    ArgumentChecker.notNull(snapshotMaster, "snapshotMaster");
    ArgumentChecker.notNull(legalEntityMaster, "legalEntityMaster");
    _io = io;
    _securityMaster = securityMaster;
    _positionMaster = positionMaster;
    _portfolioMaster = portfolioMaster;
    _configMaster = configMaster;
    _timeSeriesMaster = timeSeriesMaster;
    _holidayMaster = holidayMaster;
    _exchangeMaster = exchangeMaster;
    _snapshotMaster = snapshotMaster;
    _legalEntityMaster = legalEntityMaster;
    _conventionMaster = conventionMaster;
  }

  public static void main(String[] args) throws IOException {
    if (args.length < 2) {
      System.err.println("arguments: dataDirectory serverUrl");
      System.exit(1);
    }
    String dataDir = args[0];
    String serverUrl = args[1];
    try (RemoteServer server = RemoteServer.create(serverUrl)) {
      DatabaseRestore databaseRestore = new DatabaseRestore(dataDir, server.getSecurityMaster(), server.getPositionMaster(), server.getPortfolioMaster(), server.getConfigMaster(),
          server.getHistoricalTimeSeriesMaster(), server.getHolidayMaster(), server.getExchangeMaster(), server.getMarketDataSnapshotMaster(), server.getLegalEntityMaster(),
          server.getConventionMaster());
      databaseRestore.restoreDatabase();
    }
  }

  private IdMappings loadIdMappings() throws IOException {
    try {
      return (IdMappings) _io.read(null, RegressionUtils.ID_MAPPINGS_IDENTIFIER);
    } catch (FileNotFoundException e) {
      return null;
    }
  }

  public void restoreDatabase() {
    try {
      _io.beginRead();
      final IdMappings idMappings = loadIdMappings();
      if (idMappings != null) {
        ConfigItem<IdMappings> mappingsItem = RegressionUtils.loadIdMappings(_configMaster);
        if (mappingsItem == null) {
          _configMaster.add(new ConfigDocument(ConfigItem.of(idMappings, RegressionUtils.ID_MAPPINGS)));
        } else {
          ConfigItem<IdMappings> configItem = ConfigItem.of(idMappings, RegressionUtils.ID_MAPPINGS);
          configItem.setUniqueId(mappingsItem.getUniqueId());
          _configMaster.update(new ConfigDocument(configItem));
        }
      }
      Map<ObjectId, ObjectId> securityIdMappings = loadSecurities();
      Map<ObjectId, ObjectId> positionIdMappings = loadPositions(securityIdMappings);
      Map<ObjectId, ObjectId> portfolioIdMappings = loadPortfolios(positionIdMappings);
      loadConfigs(portfolioIdMappings);
      loadTimeSeries();
      loadHolidays();
      loadExchanges();
      loadSnapshots();
      loadLegalEntities();
      loadConventions();
      _io.endRead();
      s_logger.info("Successfully restored database");
    } catch (IOException e) {
      throw new OpenGammaRuntimeException("Failed to restore database", e);
    }
  }

  private Map<ObjectId, ObjectId> loadSecurities() throws IOException {
    List<ManageableSecurity> securities = readAll(RegressionUtils.SECURITY_MASTER_DATA);
    Map<ObjectId, ObjectId> ids = Maps.newHashMapWithExpectedSize(securities.size());
    for (ManageableSecurity security : securities) {
      ObjectId oldId = security.getUniqueId().getObjectId();
      security.setUniqueId(null);
      SecurityDocument doc = _securityMaster.add(new SecurityDocument(security));
      ids.put(oldId, doc.getUniqueId().getObjectId());
    }
    return ids;
  }

  private Map<ObjectId, ObjectId> loadPositions(Map<ObjectId, ObjectId> securityIdMappings) throws IOException {
    List<ManageablePosition> positions = readAll(RegressionUtils.POSITION_MASTER_DATA);
    Map<ObjectId, ObjectId> ids = Maps.newHashMapWithExpectedSize(positions.size());
    for (ManageablePosition position : positions) {
      ObjectId oldId = position.getUniqueId().getObjectId();
      position.setUniqueId(null);
      ObjectId securityObjectId = position.getSecurityLink().getObjectId();
      ObjectId newObjectId = null;

      if (securityObjectId != null) {
        newObjectId = securityIdMappings.get(securityObjectId);
        position.getSecurityLink().setObjectId(newObjectId);
        if (newObjectId == null) {
          s_logger.warn("No security found with ID {} for position {}", securityObjectId, position);
        }
      }
      for (ManageableTrade trade : position.getTrades()) {
        if (!trade.getAttributes().containsKey(REGRESSION_ID)) {
          trade.addAttribute(REGRESSION_ID, trade.getUniqueId().getObjectId().toString());
        }
        trade.setUniqueId(null);
        trade.setParentPositionId(null);
        trade.getSecurityLink().setObjectId(newObjectId);
      }
      // put the old ID on as an attribute. this allows different instances of a position or trade to be identified
      // when they're saved in different databases and therefore have different unique IDs
      if (!position.getAttributes().containsKey(REGRESSION_ID)) {
        position.addAttribute(REGRESSION_ID, oldId.toString());
      }
      PositionDocument doc = _positionMaster.add(new PositionDocument(position));
      ObjectId newId = doc.getUniqueId().getObjectId();
      ids.put(oldId, newId);
    }
    return ids;
  }

  private Map<ObjectId, ObjectId> loadPortfolios(Map<ObjectId, ObjectId> positionIdMappings) throws IOException {
    List<ManageablePortfolio> portfolios = readAll(RegressionUtils.PORTFOLIO_MASTER_DATA);
    Map<ObjectId, ObjectId> idMappings = Maps.newHashMapWithExpectedSize(portfolios.size());
    for (ManageablePortfolio portfolio : portfolios) {
      UniqueId oldId = portfolio.getUniqueId();
      portfolio.setUniqueId(null);
      replacePositionIds(portfolio.getRootNode(), positionIdMappings);
      UniqueId newId = _portfolioMaster.add(new PortfolioDocument(portfolio)).getUniqueId();
      s_logger.debug("Saved portfolio {} with ID {}, old ID {}", portfolio.getName(), newId, oldId);
      idMappings.put(oldId.getObjectId(), newId.getObjectId());
    }
    return idMappings;
  }

  private void loadConfigs(Map<ObjectId, ObjectId> portfolioIdMappings) throws IOException {
    List<ConfigItem<?>> configs = readAll(RegressionUtils.CONFIG_MASTER_DATA);
    List<ViewDefinition> viewDefs = Lists.newArrayList();
    // view definitions refer to other config items by unique ID
    Map<ObjectId, ObjectId> idMappings = Maps.newHashMap();
    for (ConfigItem<?> config : configs) {
      Object configValue = config.getValue();
      if (configValue instanceof ViewDefinition) {
        viewDefs.add((ViewDefinition) configValue);
      } else {
        UniqueId oldId = config.getUniqueId();
        config.setUniqueId(null);
        UniqueId newId = _configMaster.add(new ConfigDocument(config)).getUniqueId();
        s_logger.debug("Saved config of type {} with ID {}", configValue.getClass().getSimpleName(), newId);
        idMappings.put(oldId.getObjectId(), newId.getObjectId());
      }
    }
    // TODO maybe this should be pluggable to handle new config types that need post processing
    for (ViewDefinition viewDef : viewDefs) {
      ObjectId oldPortfolioId = (viewDef.getPortfolioId() != null) ? viewDef.getPortfolioId().getObjectId() : null;
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
      UniqueId newId = _configMaster.add(new ConfigDocument(ConfigItem.of(newViewDef))).getUniqueId();
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
    List<TimeSeriesWithInfo> objects = readAll(RegressionUtils.HISTORICAL_TIME_SERIES_MASTER_DATA);
    for (TimeSeriesWithInfo timeSeriesWithInfo : objects) {
      ManageableHistoricalTimeSeriesInfo info = timeSeriesWithInfo.getInfo();
      ManageableHistoricalTimeSeries timeSeries = timeSeriesWithInfo.getTimeSeries();
      info.setUniqueId(null);
      HistoricalTimeSeriesMaster timeSeriesMaster = _timeSeriesMaster;
      HistoricalTimeSeriesInfoDocument infoDoc = timeSeriesMaster.add(new HistoricalTimeSeriesInfoDocument(info));
      timeSeriesMaster.updateTimeSeriesDataPoints(infoDoc.getInfo().getTimeSeriesObjectId(), timeSeries.getTimeSeries());
    }
  }

  private void loadHolidays() throws IOException {
    List<ManageableHoliday> holidays = readAll(RegressionUtils.HOLIDAY_MASTER_DATA);
    for (ManageableHoliday holiday : holidays) {
      holiday.setUniqueId(null);
      _holidayMaster.add(new HolidayDocument(holiday));
    }
  }

  private void loadExchanges() throws IOException {
    List<ManageableExchange> exchanges = readAll(RegressionUtils.EXCHANGE_MASTER_DATA);
    for (ManageableExchange exchange : exchanges) {
      exchange.setUniqueId(null);
      _exchangeMaster.add(new ExchangeDocument(exchange));
    }
  }

  private void loadSnapshots() throws IOException {
    List<ManageableMarketDataSnapshot> snapshots = readAll(RegressionUtils.MARKET_DATA_SNAPSHOT_MASTER_DATA);
    for (ManageableMarketDataSnapshot snapshot : snapshots) {
      snapshot.setUniqueId(null);
      _snapshotMaster.add(new MarketDataSnapshotDocument(snapshot));
    }
  }

  private void loadLegalEntities() throws IOException {
    List<ManageableLegalEntity> legalEntities = readAll(RegressionUtils.LEGAL_ENTITY_MASTER_DATA);
    for (ManageableLegalEntity legalEntity : legalEntities) {
      legalEntity.setUniqueId(null);
      _legalEntityMaster.add(new LegalEntityDocument(legalEntity));
    }
  }

  private void loadConventions() throws IOException {
    List<ManageableConvention> conventions = readAll(RegressionUtils.CONVENTION_MASTER_DATA);
    for (ManageableConvention convention : conventions) {
      convention.setUniqueId(null);
      _conventionMaster.add(new ConventionDocument(convention));
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

  @SuppressWarnings({"rawtypes", "unchecked" })
  private <T extends UniqueIdentifiable> List<T> readAll(final String type) throws IOException {
    final List objects = new ArrayList(_io.readAll(type).values());
    // [PLAT-5410] The objects are sorted by unique ID to give a consistent load; this is probably hiding other faults
    Collections.sort(objects, new Comparator() {
      @Override
      public int compare(final Object o1, final Object o2) {
        final UniqueId id1 = ((T) o1).getUniqueId();
        final UniqueId id2 = ((T) o2).getUniqueId();
        return ObjectUtils.compare(id1, id2);
      }
    });
    s_logger.info("Read {} {}", objects.size(), type);
    return objects;
  }

}
