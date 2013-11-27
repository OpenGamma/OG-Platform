/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.config;

import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.wire.FudgeMsgWriter;
import org.fudgemsg.wire.xml.FudgeXMLStreamWriter;
import org.joda.beans.impl.flexi.FlexiBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.UniqueId;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.master.config.ConfigSearchSortOrder;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Class that saves a range of configuration objects into a file in XML format
 */
public class ConfigSaver {

  private static final Logger s_logger = LoggerFactory.getLogger(ConfigSaver.class);
  private ConfigMaster _configMaster;
  private PortfolioMaster _portfolioMaster;
  private List<String> _names;
  private List<String> _types;
  private boolean _portPortfolioRefs;
  private boolean _verbose;
  private final ConfigSearchSortOrder _order;

  public ConfigSaver(ConfigMaster configMaster, PortfolioMaster portfolioMaster, List<String> names, List<String> types, 
                     boolean portPortfolioRefs, boolean verbose, ConfigSearchSortOrder order) {
    _configMaster = configMaster;
    _portfolioMaster = portfolioMaster;
    _names = names;
    _types = types;
    _portPortfolioRefs = portPortfolioRefs;
    _verbose = verbose;
    _order = order;
  }
  
  public void saveConfigs(PrintStream outputStream) {
    List<ConfigEntry> allConfigs = getAllConfigs();
    if (_verbose) {
      s_logger.info("Matched " + allConfigs.size() + " configurations");
    }
    FudgeXMLStreamWriter xmlStreamWriter = new FudgeXMLStreamWriter(OpenGammaFudgeContext.getInstance(), new OutputStreamWriter(outputStream));
    FudgeSerializer serializer = new FudgeSerializer(OpenGammaFudgeContext.getInstance());
    FlexiBean wrapper = new FlexiBean();
    wrapper.set("configs", allConfigs);
    if (_portPortfolioRefs) {
      Map<UniqueId, String> idToPortfolioMap = getPortfolioNameMap(allConfigs);
      wrapper.set("idToPortfolioMap", idToPortfolioMap);
    }
    MutableFudgeMsg msg = serializer.objectToFudgeMsg(wrapper);
    FudgeMsgWriter fudgeMsgWriter = new FudgeMsgWriter(xmlStreamWriter);
    fudgeMsgWriter.writeMessage(msg);
    fudgeMsgWriter.close();
  }
  
  private Map<UniqueId, String> getPortfolioNameMap(List<ConfigEntry> configEntries) {
    Map<UniqueId, String> idToPortfolioNameMap = Maps.newHashMap();
    for (ConfigEntry configEntry : configEntries) {
      if (configEntry.getObject() instanceof ViewDefinition) {
        ViewDefinition viewDefinition = (ViewDefinition) configEntry.getObject();
        String portfolioName = getPortfolioName(viewDefinition.getPortfolioId());
        if (portfolioName != null) {
          idToPortfolioNameMap.put(viewDefinition.getPortfolioId(), portfolioName);
        } else {
          if (_verbose) {
            s_logger.warn("Couldn't find portfolio for id in view definition called " + viewDefinition.getName());
          }
        }
      }
    }
    return idToPortfolioNameMap;
  }
  
  private String getPortfolioName(UniqueId uniqueId) {
    if (uniqueId != null) {
      try {
        PortfolioDocument portfolioDocument = _portfolioMaster.get(uniqueId);
        if (portfolioDocument != null) {
          return portfolioDocument.getPortfolio().getName();
        }
      } catch (DataNotFoundException dnfe) {
        if (_verbose) {
          s_logger.warn("Couldn't find portfolio for " + uniqueId);
        }
      }
    }
    return null;
  }
  
  private List<ConfigEntry> getAllConfigs() {
    List<ConfigEntry> configsToSave = new ArrayList<ConfigEntry>();
    if (_types.size() > 0) {
      for (String type : _types) {
        try {
          Class<?> clazz = Class.forName(type);
          if (_names.size() > 0) {
            for (String name : _names) {
              configsToSave.addAll(getConfigs(clazz, name));        
            }
          } else {
            configsToSave.addAll(getConfigs(clazz));
          }
        } catch (ClassNotFoundException cnfe) {
          s_logger.error("Could not find class called " + type + " aborting");
          System.exit(1);
        }
      }
    } else {
      if (_names.size() > 0) {
        for (String name : _names) {
          configsToSave.addAll(getConfigs(name));
        }
      } else {
        configsToSave.addAll(getConfigs());
      }
    }
    return configsToSave;
  }

  private List<ConfigEntry> getConfigs(Class<?> type, String name) {
    ConfigSearchRequest<Object> searchReq = createSearchRequest();
    searchReq.setType(type);
    searchReq.setName(name);
    ConfigSearchResult<Object> searchResult = _configMaster.search(searchReq);
    return docsToConfigEntries(searchResult);
  }
  
  private List<ConfigEntry> getConfigs(Class<?> type) {
    ConfigSearchRequest<Object> searchReq = createSearchRequest();
    searchReq.setType(type);
    ConfigSearchResult<Object> searchResult = _configMaster.search(searchReq);
    return docsToConfigEntries(searchResult);    
  }
  
  private List<ConfigEntry> getConfigs(String name) {
    ConfigSearchRequest<Object> searchReq = createSearchRequest();
    searchReq.setName(name);
    searchReq.setType(Object.class);
    ConfigSearchResult<Object> searchResult = _configMaster.search(searchReq);
    return docsToConfigEntries(searchResult);
  }
  
  private List<ConfigEntry> getConfigs() {
    ConfigSearchRequest<Object> searchReq = createSearchRequest();
    searchReq.setType(Object.class);
    ConfigSearchResult<Object> searchResult = _configMaster.search(searchReq);
    return docsToConfigEntries(searchResult);    
  }

  
  /**
   * @return a search request with defaults set
   */
  private ConfigSearchRequest<Object> createSearchRequest() {
    ConfigSearchRequest<Object> searchRequest = new ConfigSearchRequest<Object>();
    searchRequest.setSortOrder(_order);
    return searchRequest;
  }
  
  private List<ConfigEntry> docsToConfigEntries(ConfigSearchResult<Object> searchResult) {
    List<ConfigEntry> results = new ArrayList<ConfigEntry>();
    for (ConfigItem<Object> doc : searchResult.getValues()) {
      ConfigEntry configEntry = new ConfigEntry();
      configEntry.setName(doc.getName());
      configEntry.setType(doc.getType().getCanonicalName());
      configEntry.setObject(doc.getValue());
      results.add(configEntry);
    }
    return results;
  }

}
