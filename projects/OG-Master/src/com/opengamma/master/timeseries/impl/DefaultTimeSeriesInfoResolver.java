/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.timeseries.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.master.timeseries.TimeSeriesDocument;
import com.opengamma.master.timeseries.TimeSeriesMaster;
import com.opengamma.master.timeseries.TimeSeriesInfo;
import com.opengamma.master.timeseries.TimeSeriesInfoResolver;
import com.opengamma.master.timeseries.TimeSeriesSearchRequest;
import com.opengamma.master.timeseries.TimeSeriesSearchResult;
import com.opengamma.util.ArgumentChecker;

/**
 * Simple time-series resolver, returns the best match from the time-series info in the data store.
 * <p>
 * This resolver relies on configuration in the configuration database.
 * 
 * @param <T> the type of the time-series, such as LocalDate/LocalDateTime
 */
public class DefaultTimeSeriesInfoResolver<T> implements TimeSeriesInfoResolver {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DefaultTimeSeriesInfoResolver.class);

  /**
   * The time-series master.
   */
  private final TimeSeriesMaster<T> _tsMaster;
  /**
   * The source of configuration.
   */
  private final ConfigSource _configSource;

  /**
   * Creates an instance from a time-series master and configuration source.
   * 
   * @param timeSeriesMaster  the time-series master, not null
   * @param configSource  the configuration source, not null
   */
  public DefaultTimeSeriesInfoResolver(TimeSeriesMaster<T> timeSeriesMaster, ConfigSource configSource) {
    ArgumentChecker.notNull(timeSeriesMaster, "timeseries master");
    ArgumentChecker.notNull(configSource, "configSource");
    _configSource = configSource;
    _tsMaster = timeSeriesMaster;
  }

  //-------------------------------------------------------------------------
  @Override
  public TimeSeriesInfo getInfo(IdentifierBundle securityBundle, String configName) {
    ArgumentChecker.notNull(securityBundle, "securityBundle");
    ArgumentChecker.notNull(configName, "configName");
    
    // find time-series
    TimeSeriesSearchRequest<T> searchRequest = new TimeSeriesSearchRequest<T>(securityBundle);
    searchRequest.setDataField(DEFAULT_DATA_FIELD);
    searchRequest.setLoadTimeSeries(false);
    TimeSeriesSearchResult<T> searchResult = _tsMaster.search(searchRequest);
    
    // pick best using rules from configuration
    TimeSeriesInfoConfiguration ruleSet = _configSource.getLatestByName(TimeSeriesInfoConfiguration.class, configName);
    if (ruleSet != null) {
      List<TimeSeriesInfo> infos = extractTimeSeriesInfo(searchResult);
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
  private List<TimeSeriesInfo> extractTimeSeriesInfo(TimeSeriesSearchResult<T> searchResult) {
    List<TimeSeriesDocument<T>> documents = searchResult.getDocuments();
    List<TimeSeriesInfo> infoList = new ArrayList<TimeSeriesInfo>(documents.size());
    for (TimeSeriesDocument<T> document : documents) {
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
  private TimeSeriesInfo bestMatch(List<TimeSeriesInfo> infoList, TimeSeriesInfoRateProvider ruleSet) {
    if (infoList.isEmpty()) {
      return null;
    }
    TreeMap<Integer, TimeSeriesInfo> scores = new TreeMap<Integer, TimeSeriesInfo>();
    for (TimeSeriesInfo info : infoList) {
      int score = ruleSet.rate(info);
      s_logger.debug("Score: {} for info: {} using rules: {} ", new Object[]{score, info, ruleSet});
      scores.put(score, info);
    }
    return scores.lastEntry().getValue();
  }

}
