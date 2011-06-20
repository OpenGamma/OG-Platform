/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaldata.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.master.historicaldata.HistoricalTimeSeriesInfo;
import com.opengamma.util.ArgumentChecker;

/**
 * The set of configured rules used to choose the best matching time-series.
 * <p>
 * This class is immutable and thread-safe.
 */
public class HistoricalTimeSeriesInfoConfiguration {

  /**
   * The set of rules.
   */
  private final Set<HistoricalTimeSeriesInfoRating> _rules = new HashSet<HistoricalTimeSeriesInfoRating>();
  /**
   * The rules grouped by field type.
   */
  private final Map<String, Map<String, Integer>> _rulesByFieldType = new HashMap<String, Map<String, Integer>>();
  /**
   * The cached hash code.
   */
  private final int _hashCode;

  /**
   * Creates an instance.
   * 
   * @param rules  the rules, not null and not empty
   */
  public HistoricalTimeSeriesInfoConfiguration(Collection<HistoricalTimeSeriesInfoRating> rules) {
    ArgumentChecker.notEmpty(rules, "rules");
    _rules.addAll(rules);
    buildRuleDb();
    _hashCode = calcHashCode();
  }

  private void buildRuleDb() {
    for (HistoricalTimeSeriesInfoRating rule : _rules) {
      String fieldName = rule.getFieldName();
      Map<String, Integer> ruleDb = _rulesByFieldType.get(fieldName);
      if (ruleDb == null) {
        ruleDb = new HashMap<String, Integer>();
        _rulesByFieldType.put(fieldName, ruleDb);
      }
      ruleDb.put(rule.getFieldValue(), rule.getRating());
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the rules field.
   * 
   * @return the rules, not null
   */
  public Collection<HistoricalTimeSeriesInfoRating> getRules() {
    return Collections.unmodifiableCollection(_rules);
  }

  //-------------------------------------------------------------------------
  /**
   * Rates historical time-series info based on its rules.
   * 
   * @param info  the time-series info, not null
   * @return the rating
   */
  public int rate(HistoricalTimeSeriesInfo info) {
    String dataSource = info.getDataSource();
    Map<String, Integer> dataSourceMap = _rulesByFieldType.get(HistoricalTimeSeriesInfoFieldNames.DATA_SOURCE_NAME);
    Integer dsRating = dataSourceMap.get(dataSource);
    if (dsRating == null) {
      dsRating = dataSourceMap.get(HistoricalTimeSeriesInfoFieldNames.STAR_VALUE);
      if (dsRating == null) {
        throw new OpenGammaRuntimeException("There must be a star match if no match with given dataSource: " + dataSource);
      }
    }
    String dataProvider = info.getDataProvider();
    Map<String, Integer> dataProviderMap = _rulesByFieldType.get(HistoricalTimeSeriesInfoFieldNames.DATA_PROVIDER_NAME);
    Integer dpRating = dataProviderMap.get(dataProvider);
    if (dpRating == null) {
      dpRating = dataProviderMap.get(HistoricalTimeSeriesInfoFieldNames.STAR_VALUE);
      if (dpRating == null) {
        throw new OpenGammaRuntimeException("There must be a star match if no match with given dataProvider: " + dataProvider);
      }
    }
    return dsRating * dpRating;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof HistoricalTimeSeriesInfoConfiguration) {
      HistoricalTimeSeriesInfoConfiguration other = (HistoricalTimeSeriesInfoConfiguration) obj;
      return _rules.equals(other._rules);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return _hashCode;
  }

  private int calcHashCode() {
    return 31 + _rules.hashCode();
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE, false);
  }

}
