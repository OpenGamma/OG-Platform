/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesSelector;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.util.ArgumentChecker;

/**
 * A time-series resolver for a specific data source which uses configuration to interpret the resolution key.
 */
public class DefaultHistoricalTimeSeriesSelector implements HistoricalTimeSeriesSelector {

  /**
   * Logger.
   */
  private static final Logger s_logger = LoggerFactory.getLogger(DefaultHistoricalTimeSeriesSelector.class);

  /**
   * The source of configuration.
   */
  private final ConfigSource _configSource;

  public DefaultHistoricalTimeSeriesSelector(ConfigSource configSource) {
    ArgumentChecker.notNull(configSource, "configSource");
    _configSource = configSource;
  }

  //-------------------------------------------------------------------------
  @Override
  public ManageableHistoricalTimeSeriesInfo select(Collection<ManageableHistoricalTimeSeriesInfo> candidates, String selectionKey) {
    selectionKey = Objects.firstNonNull(selectionKey, HistoricalTimeSeriesRatingFieldNames.DEFAULT_CONFIG_NAME);
    
    //IGN-139 - avoid rating unless we have to
    switch (candidates.size()) {
      case 0:
        return null;
      case 1:
        return Iterables.getOnlyElement(candidates);  
      default:
        // Pick best using rules from configuration
        HistoricalTimeSeriesRating rating = _configSource.getLatestByName(HistoricalTimeSeriesRating.class, selectionKey);
        if (rating == null) {
          s_logger.warn("Resolver failed to find configuration: {}", selectionKey);
          return null;
        }
        return bestMatch(candidates, rating);
    }
  }
  
  //-------------------------------------------------------------------------
  /**
   * Choose the best match using the configured rules.
   * 
   * @param matches  the list of matches, not null
   * @param rating  the rules for scoring the matches, not null
   * @return the best match, null if no match
   */
  private ManageableHistoricalTimeSeriesInfo bestMatch(Collection<ManageableHistoricalTimeSeriesInfo> matches, HistoricalTimeSeriesRating rating) {
    s_logger.debug("Find best match using rules: {}", rating);
    int currentScore = Integer.MIN_VALUE;
    ManageableHistoricalTimeSeriesInfo bestMatch = null;
    for (ManageableHistoricalTimeSeriesInfo match : matches) {
      int score = rating.rate(match);
      s_logger.debug("Score: {} for info: {}", score, match);
      if (score > currentScore) {
        currentScore = score;
        bestMatch = match;
      }
    }
    return bestMatch;
  }
  
}
