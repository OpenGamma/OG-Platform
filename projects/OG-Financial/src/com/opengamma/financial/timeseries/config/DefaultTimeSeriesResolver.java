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
import com.opengamma.financial.timeseries.TimeSeriesDocument;
import com.opengamma.financial.timeseries.TimeSeriesMaster;
import com.opengamma.financial.timeseries.TimeSeriesMetaData;
import com.opengamma.financial.timeseries.TimeSeriesSearchRequest;
import com.opengamma.financial.timeseries.TimeSeriesSearchResult;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * Simple timeSeries resolver, returns the best match from the timeseries metadata in datastore
 * @param <T> the type of the timeseries i.e LocalDate/LocalDateTime
 */
public class DefaultTimeSeriesResolver<T> implements TimeSeriesMetaDataResolver {
  private static final Logger s_logger = LoggerFactory.getLogger(DefaultTimeSeriesResolver.class);
  private final TimeSeriesMaster<T> _tsMaster;
  private final ConfigSource _configSource;
  /**
   * @param tsMaster the timeseries master, not-null
   * @param configSource the configSource, not-null
   */
  public DefaultTimeSeriesResolver(TimeSeriesMaster<T> tsMaster, ConfigSource configSource) {
    ArgumentChecker.notNull(tsMaster, "timeseries master");
    ArgumentChecker.notNull(configSource, "configSource");
    _configSource = configSource;
    _tsMaster = tsMaster;
  }

  @Override
  public TimeSeriesMetaData getDefaultMetaData(IdentifierBundle identifiers, String configName) {
    ArgumentChecker.notNull(identifiers, "identifiers");
    ArgumentChecker.notNull(configName, "configName");
    TimeSeriesSearchRequest<T> searchRequest = new TimeSeriesSearchRequest<T>();
    searchRequest.getIdentifiers().addAll(identifiers.getIdentifiers());
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
