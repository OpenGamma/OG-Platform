/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.config;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.wire.FudgeMsgReader;
import org.fudgemsg.wire.xml.FudgeXMLStreamReader;
import org.joda.beans.impl.flexi.FlexiBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.UniqueId;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Class to load configurations from an input stream
 */
public class ConfigLoader {
  private static final Logger s_logger = LoggerFactory.getLogger(ConfigLoader.class);
  private ConfigMaster _configMaster;
  private PortfolioMaster _portfolioMaster;
  private boolean _actuallyStore;
  private boolean _verbose;
  private boolean _attemptToPortPortfolioIds;
  
  public ConfigLoader(ConfigMaster configMaster, PortfolioMaster portfolioMaster, boolean attemptToPortPortfolioIds, 
                      boolean actuallyStore, boolean verbose) {
    _configMaster = configMaster;
    _portfolioMaster = portfolioMaster;
    _attemptToPortPortfolioIds = attemptToPortPortfolioIds;
    _actuallyStore = actuallyStore;
    _verbose = verbose;
  }
  
  public void loadConfig(InputStream inputStream) {
    FudgeXMLStreamReader xmlStreamReader = new FudgeXMLStreamReader(OpenGammaFudgeContext.getInstance(), new InputStreamReader(new BufferedInputStream(inputStream)));
    FudgeMsgReader fudgeMsgReader = new FudgeMsgReader(xmlStreamReader);
    FudgeDeserializer deserializer = new FudgeDeserializer(OpenGammaFudgeContext.getInstance());
    FudgeMsg configsMessage = fudgeMsgReader.nextMessage();
    if (configsMessage == null) {
      s_logger.error("Error reading first message from XML stream");
      return;
    }
    Object object = deserializer.fudgeMsgToObject(FlexiBean.class, configsMessage);
    if (!(object instanceof FlexiBean)) {
      s_logger.error("XML Stream deserialised to object of type " + object.getClass() + ": " + object.toString());
      return;
    }
    FlexiBean wrapper = (FlexiBean) object;
    if (!wrapper.contains("configs")) {
      s_logger.error("File stream does not contain configs element");
      return;
    }
    @SuppressWarnings("unchecked")
    List<ConfigEntry> configs = (List<ConfigEntry>) wrapper.get("configs");
    if (wrapper.contains("idToPortfolioMap")) {
      @SuppressWarnings("unchecked")
      Map<UniqueId, String> idToPortfolioMap = (Map<UniqueId, String>) wrapper.get("idToPortfolioMap");
      if (idToPortfolioMap == null) {
        s_logger.warn("Apparently corrupt portfolio id -> name map, won't attempt to port portfolio ids");
        loadConfigs(configs, Collections.<UniqueId, String>emptyMap());
      } else {
        loadConfigs(configs, idToPortfolioMap);
      }
    } else {
      loadConfigs(configs, Collections.<UniqueId, String>emptyMap());
    }
    
  }

  private void loadConfigs(List<ConfigEntry> configs, Map<UniqueId, String> idNameMap) {
    for (ConfigEntry entry : configs) {
      try {
        Class<?> clazz = Class.forName(entry.getType());
        Object object = entry.getObject();        
        if (object instanceof ViewDefinition) {
          if (_attemptToPortPortfolioIds) {
            object = attemptToPortPortfolioIds((ViewDefinition) object, idNameMap);
          }
        }
        ConfigItem<Object> item = ConfigItem.of(object, entry.getName(), clazz);
        if (_actuallyStore) {
          ConfigMasterUtils.storeByName(_configMaster, item);
          if (_verbose) {
            s_logger.info("Stored " + entry.getName() + " of type " + entry.getType());
          }
        } else {
          if (_verbose) {
            s_logger.info("Simulated store " + entry.getName() + " of type " + entry.getType());
          }
        }

      } catch (ClassNotFoundException ex) {
        s_logger.error("Could not find class called " + entry.getType() + " skipping config " + entry.getName());
      }
    }
  }

  private ViewDefinition attemptToPortPortfolioIds(ViewDefinition viewDefinition, Map<UniqueId, String> idNameMap) {
    if (idNameMap.containsKey(viewDefinition.getPortfolioId())) {
      if (_verbose) {
        s_logger.info("Attempting to port portfolio id " + viewDefinition.getPortfolioId());
      }
      UniqueId replacementId = lookupPortfolioByName(idNameMap.get(viewDefinition.getPortfolioId()));
      if (replacementId != null) {
        if (viewDefinition.getPortfolioId().isLatest()) {
          replacementId = replacementId.toLatest();
        }
        return viewDefinition.copyWith(viewDefinition.getName(), replacementId, viewDefinition.getMarketDataUser());
      }
    }
    return viewDefinition;
  }
  
  private UniqueId lookupPortfolioByName(String name) {
    PortfolioSearchRequest searchRequest = new PortfolioSearchRequest();
    searchRequest.setName(name);
    PortfolioSearchResult searchResult = _portfolioMaster.search(searchRequest);
    try {
      ManageablePortfolio singlePortfolio = searchResult.getSinglePortfolio();
      if (_verbose) {
        s_logger.info("Found portfolio called " + name + " mapping in it's id: " + singlePortfolio.getUniqueId());
      }
      return singlePortfolio.getUniqueId();
    } catch (IllegalStateException ise) {
      s_logger.warn("Found multiple portfolios called " + name + " so skipping");
      return null;
    } catch (OpenGammaRuntimeException ogre) {
      if (searchResult.getDocuments().size() > 1) {
        s_logger.warn("Found multiple portfolios called " + name + " so skipping");
      } else {
        s_logger.warn("Didn't find a portfolio called " + name + " so skipping");
      }
      return null;
    }
  }
}
