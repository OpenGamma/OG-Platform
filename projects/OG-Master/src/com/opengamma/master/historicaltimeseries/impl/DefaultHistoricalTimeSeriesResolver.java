/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import java.util.List;

import javax.time.calendar.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
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
  public UniqueId resolve(String dataField, ExternalIdBundle identifiers, LocalDate identifierValidityDate, String resolutionKey) {
    ArgumentChecker.notNull(dataField, "dataField");
    ArgumentChecker.notNull(identifiers, "identifiers");
    resolutionKey = Objects.firstNonNull(resolutionKey, HistoricalTimeSeriesRatingFieldNames.DEFAULT_CONFIG_NAME);
    
    // find all matching time-series
    HistoricalTimeSeriesInfoSearchRequest searchRequest = new HistoricalTimeSeriesInfoSearchRequest(identifiers);
    searchRequest.setValidityDate(identifierValidityDate);
    searchRequest.setDataField(dataField);
    HistoricalTimeSeriesInfoSearchResult searchResult = _master.search(searchRequest);
    List<ManageableHistoricalTimeSeriesInfo> candidates = searchResult.getInfoList();
    //IGN-139 - avoid rating unless we have to
    switch (candidates.size()) {
      case 0:
        s_logger.warn("Resolver failed to find any time-series for {} using {}/{}", new Object[] {identifiers, dataField, resolutionKey});
        return null;
      case 1:
        return candidates.get(0).getUniqueId();  
      default:
        // pick best using rules from configuration
        HistoricalTimeSeriesRating ruleSet = _configSource.getLatestByName(HistoricalTimeSeriesRating.class, resolutionKey);
        if (ruleSet == null) {
          s_logger.warn("Resolver failed to find configuration: {}", resolutionKey);
          return null;
        }
        return bestMatch(candidates, ruleSet);
    }
  }

  /**
   * Choose the best match using the configured rules.
   * 
   * @param infoList  the list of series objects, not null
   * @param rating  the configured rules, not null
   * @return the best match, null if no match
   */
  private UniqueId bestMatch(List<ManageableHistoricalTimeSeriesInfo> infoList, HistoricalTimeSeriesRating rating) {
    s_logger.debug("Find best match using rules: {}", rating);
    
    // pick the highest score
    int currentScore = Integer.MIN_VALUE;
    UniqueId result = null;
    for (ManageableHistoricalTimeSeriesInfo info : infoList) {
      int score = rating.rate(info);
      s_logger.debug("Score: {} for info: {}", score, info);
      if (score > currentScore) {
        currentScore = score;
        result = info.getUniqueId();
      }
    }
    return result;
  }

}
