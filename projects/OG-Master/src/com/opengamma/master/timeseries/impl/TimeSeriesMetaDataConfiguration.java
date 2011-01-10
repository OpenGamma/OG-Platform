/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.timeseries.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.master.timeseries.TimeSeriesMetaData;
import com.opengamma.util.ArgumentChecker;

/**
 * The set of rules to use when loading time-series from a master.
 */
public class TimeSeriesMetaDataConfiguration implements TimeSeriesMetaDataRateProvider {

  /**
   * The set of rules.
   */
  private final Set<TimeSeriesMetaDataRating> _rules = new HashSet<TimeSeriesMetaDataRating>();
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
  public TimeSeriesMetaDataConfiguration(Collection<TimeSeriesMetaDataRating> rules) {
    ArgumentChecker.notEmpty(rules, "rules");
    _rules.addAll(rules);
    buildRuleDb();
    _hashCode = calcHashCode();
  }

  private void buildRuleDb() {
    for (TimeSeriesMetaDataRating rule : _rules) {
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
  public Collection<TimeSeriesMetaDataRating> getRules() {
    return Collections.unmodifiableCollection(_rules);
  }

  //-------------------------------------------------------------------------
  @Override
  public int rate(TimeSeriesMetaData metaData) {
    String dataSource = metaData.getDataSource();
    Map<String, Integer> dataSourceMap = _rulesByFieldType.get(TimeSeriesMetaDataFieldNames.DATA_SOURCE_NAME);
    Integer dsRating = dataSourceMap.get(dataSource);
    if (dsRating == null) {
      dsRating = dataSourceMap.get(TimeSeriesMetaDataFieldNames.STAR_VALUE);
      if (dsRating == null) {
        throw new OpenGammaRuntimeException("There must be a star match if no match with given dataSource: " + dataSource);
      }
    }
    String dataProvider = metaData.getDataProvider();
    Map<String, Integer> dataProviderMap = _rulesByFieldType.get(TimeSeriesMetaDataFieldNames.DATA_PROVIDER_NAME);
    Integer dpRating = dataProviderMap.get(dataProvider);
    if (dpRating == null) {
      dpRating = dataProviderMap.get(TimeSeriesMetaDataFieldNames.STAR_VALUE);
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
    if (obj instanceof TimeSeriesMetaDataConfiguration) {
      TimeSeriesMetaDataConfiguration other = (TimeSeriesMetaDataConfiguration) obj;
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
