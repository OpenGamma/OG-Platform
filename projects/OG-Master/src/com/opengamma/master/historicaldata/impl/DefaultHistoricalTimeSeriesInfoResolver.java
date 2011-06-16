/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaldata.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.master.historicaldata.HistoricalTimeSeriesDocument;
import com.opengamma.master.historicaldata.HistoricalTimeSeriesInfo;
import com.opengamma.master.historicaldata.HistoricalTimeSeriesInfoResolver;
import com.opengamma.master.historicaldata.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaldata.HistoricalTimeSeriesSearchRequest;
import com.opengamma.master.historicaldata.HistoricalTimeSeriesSearchResult;
import com.opengamma.util.ArgumentChecker;

/**
 * Simple historical time-series resolver, returns the best match from
 * the info in the data store.
 * <p>
 * This resolver relies on configuration in the configuration database.
 */
public class DefaultHistoricalTimeSeriesInfoResolver implements HistoricalTimeSeriesInfoResolver {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DefaultHistoricalTimeSeriesInfoResolver.class);

  /**
   * The master.
   */
  private final HistoricalTimeSeriesMaster _master;
  /**
   * The source of configuration.
   */
  private final ConfigSource _configSource;

  /**
   * Creates an instance from a master and configuration source.
   * 
   * @param historicalTimeSeriesMaster  the historical time-series master, not null
   * @param configSource  the configuration source, not null
   */
  public DefaultHistoricalTimeSeriesInfoResolver(HistoricalTimeSeriesMaster historicalTimeSeriesMaster, ConfigSource configSource) {
    ArgumentChecker.notNull(historicalTimeSeriesMaster, "historicalTimeSeriesMaster");
    ArgumentChecker.notNull(configSource, "configSource");
    _configSource = configSource;
    _master = historicalTimeSeriesMaster;
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeriesInfo getInfo(IdentifierBundle securityBundle, String configName) {
    ArgumentChecker.notNull(securityBundle, "securityBundle");
    ArgumentChecker.notNull(configName, "configName");
    
    // find time-series
    HistoricalTimeSeriesSearchRequest searchRequest = new HistoricalTimeSeriesSearchRequest(securityBundle);
    searchRequest.setDataField(DEFAULT_DATA_FIELD);
    searchRequest.setLoadTimeSeries(false);
    HistoricalTimeSeriesSearchResult searchResult = _master.search(searchRequest);
    
    // pick best using rules from configuration
    HistoricalTimeSeriesInfoConfiguration ruleSet = _configSource.getLatestByName(HistoricalTimeSeriesInfoConfiguration.class, configName);
    if (ruleSet != null) {
      List<HistoricalTimeSeriesInfo> infos = extractInfo(searchResult);
      return bestMatch(infos, ruleSet);
    } else {
      s_logger.warn("Unable to resolve time-series info because rules set with name {} can not be loaded from config database", configName);
      return null;
    }
  }

  /**
   * Converts the time-series results to the simpler info object for matching purposes.
   * 
   * @param searchResult  the search result, not null
   * @return the list of info objects, not null
   */
  private List<HistoricalTimeSeriesInfo> extractInfo(HistoricalTimeSeriesSearchResult searchResult) {
    List<HistoricalTimeSeriesDocument> documents = searchResult.getDocuments();
    List<HistoricalTimeSeriesInfo> infoList = new ArrayList<HistoricalTimeSeriesInfo>(documents.size());
    for (HistoricalTimeSeriesDocument document : documents) {
      infoList.add(document.toInfo());
    }
    return infoList;
  }

  /**
   * Choose the best match using the configured rules.
   * 
   * @param infoList  the list of info objects, not null
   * @param ruleSet  the configured rules, not null
   * @return the best match, null if no match
   */
  private HistoricalTimeSeriesInfo bestMatch(List<HistoricalTimeSeriesInfo> infoList, HistoricalTimeSeriesInfoRateProvider ruleSet) {
    if (infoList.isEmpty()) {
      return null;
    }
    TreeMap<Integer, HistoricalTimeSeriesInfo> scores = new TreeMap<Integer, HistoricalTimeSeriesInfo>();
    for (HistoricalTimeSeriesInfo info : infoList) {
      int score = ruleSet.rate(info);
      s_logger.debug("Score: {} for info: {} using rules: {} ", new Object[]{score, info, ruleSet});
      scores.put(score, info);
    }
    return scores.lastEntry().getValue();
  }

}
