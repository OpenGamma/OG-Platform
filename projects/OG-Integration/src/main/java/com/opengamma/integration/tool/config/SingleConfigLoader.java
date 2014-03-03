/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.config;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.wire.FudgeMsgReader;
import org.fudgemsg.wire.xml.FudgeXMLStreamReader;
import org.joda.beans.Bean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.master.convention.ConventionDocument;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.master.convention.ConventionSearchRequest;
import com.opengamma.master.convention.ConventionSearchResult;
import com.opengamma.master.convention.ManageableConvention;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRating;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchResult;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecurityMasterUtils;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.util.JodaBeanSerialization;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Loads in a file containing either a single JodaXML or FudgeXML encoded config and updates the config master.
 * Can be provided with a hint type if the JodaXML messages don't contain a type attribute on the bean element.
 */
public class SingleConfigLoader {
  private static final Logger s_logger = LoggerFactory.getLogger(SingleConfigLoader.class);
  private ConfigMaster _configMaster;
  private ConventionMaster _conventionMaster;
  private MarketDataSnapshotMaster _marketDataSnapshotMaster;
  private boolean _doNotUpdateExisting;
  private ConfigSource _configSource;
  private SecurityMaster _securityMaster;

  private static final FudgeContext s_fudgeContext = OpenGammaFudgeContext.getInstance();
  private static final String DEFAULT_HTS_RATING_NAME = "DEFAULT_TSS_CONFIG";
  private static final String DEFAULT_CURRENCY_MATRIX_NAME = "BloombergLiveData";

  public SingleConfigLoader(SecurityMaster securityMaster, ConfigMaster configMaster, ConfigSource configSource, ConventionMaster conventionMaster, MarketDataSnapshotMaster marketDataSnapshotMaster, boolean doNotUpdateExisting) {
    _securityMaster = securityMaster;
    _configMaster = configMaster;
    _configSource = configSource;
    _conventionMaster = conventionMaster;
    _marketDataSnapshotMaster = marketDataSnapshotMaster;
    _doNotUpdateExisting = doNotUpdateExisting;
  }
  
  private ManageableConvention addOrUpdateConvention(ManageableConvention convention) {
    ConventionSearchRequest searchReq = new ConventionSearchRequest(convention.getExternalIdBundle());
    ConventionSearchResult searchResult = _conventionMaster.search(searchReq);
    ConventionDocument match = null;
    for (ConventionDocument doc : searchResult.getDocuments()) {
      if (doc.getConvention().getConventionType().equals(convention.getConventionType())) {
        if (match == null) {
          match = doc;
        } else {
          s_logger.warn("Found more than one match for {} with type {}, changing first one", convention.getExternalIdBundle(), convention.getConventionType());
        }
      }
    }
    if (match != null) {
      if (_doNotUpdateExisting) {
        s_logger.info("Found existing convention, skipping update");
        return match.getConvention();
      } else {
        s_logger.info("Found existing convention, updating it");
        match.setConvention(convention);
        return _conventionMaster.update(match).getConvention();
      }
    } else {
      s_logger.info("No existing convention, creating a new one");
      ConventionDocument doc = new ConventionDocument(convention);
      return _conventionMaster.add(doc).getConvention();
    }
  }
  
  private ManageableSecurity addOrUpdateSecurity(ManageableSecurity security) {
    SecuritySearchRequest searchReq = new SecuritySearchRequest(security.getExternalIdBundle());
    SecuritySearchResult search = _securityMaster.search(searchReq);
    if ((search.getDocuments().size() > 0) && _doNotUpdateExisting) {
      s_logger.info("Found existing convention, skipping update");
      return search.getFirstSecurity();
    }
    return SecurityMasterUtils.addOrUpdateSecurity(_securityMaster, security);
  }
  
  private ManageableMarketDataSnapshot addOrUpdateSnapshot(ManageableMarketDataSnapshot snapshot) {
    MarketDataSnapshotSearchRequest searchReq = new MarketDataSnapshotSearchRequest();
    searchReq.setName(snapshot.getName());
    MarketDataSnapshotSearchResult searchResult = _marketDataSnapshotMaster.search(searchReq);
    MarketDataSnapshotDocument match = null;
    for (MarketDataSnapshotDocument doc : searchResult.getDocuments()) {
      if (doc.getSnapshot().getBasisViewName().equals(snapshot.getBasisViewName())) {
        if (match == null) {
          match = doc;
        } else {
          s_logger.warn("Found more than one matching market data snapshot for {} with type {}, changing first one", snapshot.getName(), snapshot.getBasisViewName());
        }
      }
    }
    if (match != null) {
      if (_doNotUpdateExisting) {
        s_logger.info("Found existing market data snapshot, skipping update");
        return match.getSnapshot(); 
      } else {
        s_logger.info("Found existing market data snapshot, updating it");
        match.setSnapshot(snapshot);
        return _marketDataSnapshotMaster.update(match).getSnapshot();
      }
    } else {
      s_logger.info("No existing market data snapshot, creating a new one");
      MarketDataSnapshotDocument doc = new MarketDataSnapshotDocument(snapshot);
      return _marketDataSnapshotMaster.add(doc).getSnapshot();
    }
  }
  
  public <T> void loadConfig(InputStream is, Class<T> hintType) {
    T config = JodaBeanSerialization.deserializer().xmlReader().read(is, hintType);
    if (config instanceof ManageableConvention) {
      addOrUpdateConvention((ManageableConvention) config);
    } else if (config instanceof ManageableMarketDataSnapshot) {
      addOrUpdateSnapshot((ManageableMarketDataSnapshot) config);
    } else if (config instanceof ManageableSecurity) {
      addOrUpdateSecurity((ManageableSecurity) config);
    } else if (config instanceof CurrencyPairs) {
      ConfigItem<?> item = ConfigItem.of(config, CurrencyPairs.DEFAULT_CURRENCY_PAIRS);
      if (_doNotUpdateExisting  && configExists(item)) {
        s_logger.info("Existing config present, skipping");
      } else {
        ConfigMasterUtils.storeByName(_configMaster, item);          
      }
    } else if (config instanceof HistoricalTimeSeriesRating) {
      ConfigItem<?> item = ConfigItem.of(config, DEFAULT_HTS_RATING_NAME);
      if (_doNotUpdateExisting  && configExists(item)) {
        s_logger.info("Existing config present, skipping");
      } else {
        ConfigMasterUtils.storeByName(_configMaster, item);          
      }  
    } else if (config instanceof CurrencyMatrix) {
      ConfigItem<?> item = ConfigItem.of(config, DEFAULT_CURRENCY_MATRIX_NAME);
      if (_doNotUpdateExisting  && configExists(item)) {
        s_logger.info("Existing config present, skipping");
      } else {
        ConfigMasterUtils.storeByName(_configMaster, item);          
      }
    } else if (config instanceof Bean) {
      ConfigItem<T> item = ConfigItem.of(config);
      if (_doNotUpdateExisting  && configExists(item)) {
        s_logger.info("Existing config present, skipping");
      } else {
        ConfigMasterUtils.storeByName(_configMaster, item);          
      }
    } else {
      s_logger.error("Unsupported type {} is not a JodaBean", config.getClass());
    }
  }
  
  public void loadConfig(InputStream is) {
    Object config = JodaBeanSerialization.deserializer().xmlReader().read(is);
    if (config instanceof ManageableConvention) {
      addOrUpdateConvention((ManageableConvention) config);
    } else if (config instanceof ManageableMarketDataSnapshot) {
      addOrUpdateSnapshot((ManageableMarketDataSnapshot) config);
    } else if (config instanceof ManageableSecurity) {
      addOrUpdateSecurity((ManageableSecurity) config);
    } else if (config instanceof CurrencyPairs) {
      ConfigItem<?> item = ConfigItem.of(config, CurrencyPairs.DEFAULT_CURRENCY_PAIRS);
      if (_doNotUpdateExisting  && configExists(item)) {
        s_logger.info("Existing config present, skipping");
      } else {
        ConfigMasterUtils.storeByName(_configMaster, item);          
      }
    } else if (config instanceof HistoricalTimeSeriesRating) {
      ConfigItem<?> item = ConfigItem.of(config, DEFAULT_HTS_RATING_NAME);
      if (_doNotUpdateExisting  && configExists(item)) {
        s_logger.info("Existing config present, skipping");
      } else {
        ConfigMasterUtils.storeByName(_configMaster, item);          
      }  
    } else if (config instanceof CurrencyMatrix) {
      ConfigItem<?> item = ConfigItem.of(config, DEFAULT_CURRENCY_MATRIX_NAME, CurrencyMatrix.class);
      if (_doNotUpdateExisting  && configExists(item)) {
        s_logger.info("Existing config present, skipping");
      } else {
        ConfigMasterUtils.storeByName(_configMaster, item);          
      }
    } else if (config instanceof Bean) {
      ConfigItem<?> item = ConfigItem.of(config);
      if (_doNotUpdateExisting  && configExists(item)) {
        s_logger.info("Existing config present, skipping");
      } else {
        ConfigMasterUtils.storeByName(_configMaster, item);          
      }
    } else {
      s_logger.error("Unsupported type {} is not a JodaBean", config.getClass());
    }
  }
  
  public <T> void loadFudgeConfig(InputStream is) {
    @SuppressWarnings("resource")
    final FudgeMsgReader fmr = new FudgeMsgReader(new FudgeXMLStreamReader(s_fudgeContext, new InputStreamReader(is)));
    final FudgeMsg message = fmr.nextMessage();

    Object config = s_fudgeContext.fromFudgeMsg(message);
    ConfigItem<?> item;
    if (config instanceof CurrencyPairs) {
      item = ConfigItem.of(config, CurrencyPairs.DEFAULT_CURRENCY_PAIRS);
    } else if (config instanceof HistoricalTimeSeriesRating) {
      item = ConfigItem.of(config, DEFAULT_HTS_RATING_NAME);
    } else if (config instanceof CurrencyMatrix) {
      item = ConfigItem.of(config, DEFAULT_CURRENCY_MATRIX_NAME, CurrencyMatrix.class);
    } else {
      item = ConfigItem.of(config);
    }
    if (_doNotUpdateExisting  && configExists(item)) {
      s_logger.info("Existing config present, skipping");
    } else {
      ConfigMasterUtils.storeByName(_configMaster, item);          
    }
  }
  
  private boolean configExists(ConfigItem<?> configItem) {
    return _configSource.getLatestByName(configItem.getType(), configItem.getName()) != null;
  }
}
