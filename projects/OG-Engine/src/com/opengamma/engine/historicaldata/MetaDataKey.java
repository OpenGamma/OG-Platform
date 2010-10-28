/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.historicaldata;

import javax.time.calendar.LocalDate;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.id.IdentifierBundle;

/**
 * Represent Timeseries Meta data
 */
/* package */ class MetaDataKey {
  private final IdentifierBundle _dsids;
  private final String _dataSource;
  private final String _dataProvider;
  private final String _field;
  private final LocalDate _currentDate;
  private final String _configName;

  public MetaDataKey(String configName, LocalDate currentDate, IdentifierBundle dsids, String dataSource, String dataProvider, String field) {
    _dsids = dsids;
    _dataSource = dataSource;
    _dataProvider = dataProvider;
    _field = field;
    _currentDate = currentDate;
    _configName = configName;
  }

  @Override
  public int hashCode() {
    return ObjectUtils.hashCode(_dsids) ^ ObjectUtils.hashCode(_field) ^ ObjectUtils.hashCode(_dataProvider) ^ ObjectUtils.hashCode(_dataSource);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if ((obj instanceof MetaDataKey)) {
      MetaDataKey other = (MetaDataKey) obj;
      return ObjectUtils.equals(_field, other._field) &&
          ObjectUtils.equals(_dsids, _dsids) &&
          ObjectUtils.equals(_dataProvider, other._dataProvider) &&
          ObjectUtils.equals(_dataSource, other._dataSource) &&
          ObjectUtils.equals(_currentDate, other._currentDate) &&
          ObjectUtils.equals(_configName, other._configName);
    }
    return false;
  }
}
