/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.config;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.config.ConfigSource;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.master.timeseries.TimeSeriesDocument;
import com.opengamma.master.timeseries.TimeSeriesMaster;
import com.opengamma.master.timeseries.TimeSeriesMetaData;
import com.opengamma.master.timeseries.TimeSeriesMetaDataResolver;
import com.opengamma.master.timeseries.TimeSeriesSearchRequest;
import com.opengamma.master.timeseries.TimeSeriesSearchResult;
import com.opengamma.util.ArgumentChecker;

/**
 * Simple time-series resolver, returns the best match from the time-series meta-data in the data store.
 * <p>
 * This resolver relies on configuration in the config database.
 * 
 * @param <T> the type of the time-series, such as LocalDate/LocalDateTime
 */
public class DefaultTimeSeriesResolver<T> implements TimeSeriesMetaDataResolver {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DefaultTimeSeriesResolver.class);

  /**
   * The time-series master.
   */
  private final TimeSeriesMaster<T> _tsMaster;
  /**
   * The source of configuration.
   */
  private final ConfigSource _configSource;

  /**
   * Creates an instance from a master and config source.
   * 
   * @param timeSeriesMaster  the time-series master, not null
   * @param configSource  the config source, not null
   */
  public DefaultTimeSeriesResolver(TimeSeriesMaster<T> timeSeriesMaster, ConfigSource configSource) {
    ArgumentChecker.notNull(timeSeriesMaster, "timeseries master");
    ArgumentChecker.notNull(configSource, "configSource");
    _configSource = configSource;
    _tsMaster = timeSeriesMaster;
  }

  //-------------------------------------------------------------------------
  @Override
  public TimeSeriesMetaData getDefaultMetaData(IdentifierBundle securityBundle, String configName) {
    ArgumentChecker.notNull(securityBundle, "securityBundle");
    ArgumentChecker.notNull(configName, "configName");
    TimeSeriesSearchRequest<T> searchRequest = new TimeSeriesSearchRequest<T>();
    searchRequest.getIdentifiers().addAll(securityBundle.getIdentifiers());
    searchRequest.setLoadTimeSeries(false);
    
    TimeSeriesSearchResult<T> searchResult = _tsMaster.searchTimeSeries(searchRequest);
    if (searchResult == null) {
      return null;
    }
    
    List<TimeSeriesDocument<T>> documents = searchResult.getDocuments();
    List<TimeSeriesMetaData> metaDataList = new ArrayList<TimeSeriesMetaData>(documents.size());
    
    for (TimeSeriesDocument<T> tsDocument : documents) {
      if (tsDocument.getDataField().equals(DEFAULT_DATA_FIELD)) {
        TimeSeriesMetaData tsMetaData = new TimeSeriesMetaData();
        tsMetaData.setDataField(DEFAULT_DATA_FIELD);
        tsMetaData.setDataProvider(tsDocument.getDataProvider());
        tsMetaData.setDataSource(tsDocument.getDataSource());
        metaDataList.add(tsMetaData);
      }
    }
    //load rules from config datastore
    TimeSeriesMetaDataConfiguration ruleSet = _configSource.getLatestByName(TimeSeriesMetaDataConfiguration.class, configName);
    if (ruleSet != null) {
      return bestMatch(metaDataList, ruleSet);
    } else {
      s_logger.warn("can not resolve timeseries metadata because rules set with name {} can not be loaded from config database", configName);
      return null;
    }
  }

  private TimeSeriesMetaData bestMatch(List<TimeSeriesMetaData> metaDataList, TimeSeriesMetaDataRateProvider ruleSet) {
    TreeMap<Integer, TimeSeriesMetaData> scores = new TreeMap<Integer, TimeSeriesMetaData>();
    for (TimeSeriesMetaData tsMetaData : metaDataList) {
      int score = ruleSet.rate(tsMetaData);
      s_logger.debug("score: {} for meta: {} using rules: {} ", new Object[]{score, tsMetaData, ruleSet});
      scores.put(score, tsMetaData);
    }
    if (!scores.isEmpty()) {
      Integer max = scores.lastKey();
      return scores.get(max);
    }
    return null;
  }

}
