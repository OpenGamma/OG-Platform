/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaldata.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.historicaldata.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaldata.HistoricalTimeSeriesResolver;
import com.opengamma.master.historicaldata.HistoricalTimeSeriesSearchRequest;
import com.opengamma.master.historicaldata.HistoricalTimeSeriesSearchResult;
import com.opengamma.master.historicaldata.ManageableHistoricalTimeSeries;
import com.opengamma.util.ArgumentChecker;

/**
 * Time-series resolver using configuration to interpret the resolution key.
 * <p>
 * This resolver relies on configuration in the configuration database.
 */
public class DefaultHistoricalTimeSeriesResolver implements HistoricalTimeSeriesResolver {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DefaultHistoricalTimeSeriesResolver.class);

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
  public DefaultHistoricalTimeSeriesResolver(HistoricalTimeSeriesMaster historicalTimeSeriesMaster, ConfigSource configSource) {
    ArgumentChecker.notNull(historicalTimeSeriesMaster, "historicalTimeSeriesMaster");
    ArgumentChecker.notNull(configSource, "configSource");
    _configSource = configSource;
    _master = historicalTimeSeriesMaster;
  }

  //-------------------------------------------------------------------------
  @Override
  public UniqueIdentifier resolve(String dataField, IdentifierBundle identifiers, String resolutionKey) {
    ArgumentChecker.notNull(dataField, "dataField");
    ArgumentChecker.notNull(identifiers, "identifiers");
    resolutionKey = Objects.firstNonNull(resolutionKey, HistoricalTimeSeriesRatingFieldNames.DEFAULT_CONFIG_NAME);
    
    // find all matching time-series
    HistoricalTimeSeriesSearchRequest searchRequest = new HistoricalTimeSeriesSearchRequest(identifiers);
    searchRequest.setDataField(dataField);
    searchRequest.setLoadEarliestLatest(false);
    searchRequest.setLoadTimeSeries(false);
    HistoricalTimeSeriesSearchResult searchResult = _master.search(searchRequest);
    if (searchResult.getDocuments().isEmpty()) {
      s_logger.warn("Resolver failed to find any time-series: {} {}", dataField, identifiers);
      return null;
    }
    
    // pick best using rules from configuration
    HistoricalTimeSeriesRating ruleSet = _configSource.getLatestByName(HistoricalTimeSeriesRating.class, resolutionKey);
    if (ruleSet == null) {
      s_logger.warn("Resolver failed to find configuration: {}", resolutionKey);
      return null;
    }
    return bestMatch(searchResult.getSeriesList(), ruleSet);
  }

  /**
   * Choose the best match using the configured rules.
   * 
   * @param seriesList  the list of series objects, not null
   * @param rating  the configured rules, not null
   * @return the best match, null if no match
   */
  private UniqueIdentifier bestMatch(List<ManageableHistoricalTimeSeries> seriesList, HistoricalTimeSeriesRating rating) {
    s_logger.debug("Find best match using rules: {}", rating);
    
    // pick the highest score
    int currentScore = Integer.MIN_VALUE;
    UniqueIdentifier result = null;
    for (ManageableHistoricalTimeSeries series : seriesList) {
      int score = rating.rate(series);
      s_logger.debug("Score: {} for info: {}", score, series);
      if (score > currentScore) {
        currentScore = score;
        result = series.getUniqueId();
      }
    }
    return result;
  }

}
