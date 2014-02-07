/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.id;

import java.util.Comparator;
import java.util.Map;

import com.google.common.collect.Maps;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;

/**
 * Comparator to define order in which schemes are sorted in ExternalIdBundles that work best for display purposes.
 * Here we're defining a table of scores for each scheme
 */
@SuppressWarnings("deprecation")
public class ExternalIdDisplayComparator implements Comparator<ExternalId> {

  /**
   * The map of scores.
   */
  static final Map<ExternalScheme, Integer> s_scoreMap = Maps.newHashMap();
  static {
    s_scoreMap.put(ExternalSchemes.BLOOMBERG_TCM, 20); // beacuse if there's both ticker and tcm, you want to see tcm.
    s_scoreMap.put(ExternalSchemes.BLOOMBERG_TICKER, 19);
    s_scoreMap.put(ExternalSchemes.RIC, 17);
    s_scoreMap.put(ExternalSchemes.BLOOMBERG_TICKER_WEAK, 16);
    s_scoreMap.put(ExternalSchemes.ACTIVFEED_TICKER, 15);
    s_scoreMap.put(ExternalSchemes.SURF, 14);
    s_scoreMap.put(ExternalSchemes.ISIN, 13);
    s_scoreMap.put(ExternalSchemes.CUSIP, 12);
    s_scoreMap.put(ExternalSchemes.SEDOL1, 11);
    s_scoreMap.put(ExternalSchemes.OG_SYNTHETIC_TICKER, 10);
    s_scoreMap.put(ExternalSchemes.BLOOMBERG_BUID, 5);
    s_scoreMap.put(ExternalSchemes.BLOOMBERG_BUID_WEAK, 4);
  }

  /**
   * The map of scores.
   */
  private Map<ExternalScheme, Integer> _scoreMap;

  /**
   * Uses hard-coded default information about scores.
   */
  public ExternalIdDisplayComparator() {
    _scoreMap = s_scoreMap;
  }

  /**
   * Initialize comparator using configuration object stored in config database.
   * 
   * @param orderConfig  sourced from a ConfigSource
   */
  public ExternalIdDisplayComparator(ExternalIdOrderConfig orderConfig) {
    // TODO: code missing!
  }

  private int scoreExternalId(ExternalId id) {
    if (_scoreMap.containsKey(id.getScheme())) {
      return _scoreMap.get(id.getScheme());
    } else {
      return 0;
    }
  }

  @Override
  public int compare(ExternalId id0, ExternalId id1) {
    int score0 = scoreExternalId(id0);
    int score1 = scoreExternalId(id1);
    if ((score1 - score0) != 0) {
      return score1 - score0;
    } else {
      return id0.compareTo(id1);
    }
  }

}
