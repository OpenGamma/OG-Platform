/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.historicaldata.impl;

import javax.time.calendar.LocalDate;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.id.IdentifierBundle;

/**
 * Key to represent time-series data in a hash-map or cache.
 */
/* package */ final class HistoricalDataKey {

  private final IdentifierBundle _identifiers;
  private final LocalDate _currentDate;
  private final String _dataSource;
  private final String _dataProvider;
  private final String _dataField;
  private final String _configName;

  /* package */ HistoricalDataKey(String configName, LocalDate currentDate, IdentifierBundle dsids, String dataSource, String dataProvider, String field) {
    _identifiers = dsids;
    _dataSource = dataSource;
    _dataProvider = dataProvider;
    _dataField = field;
    _currentDate = currentDate;
    _configName = configName;
  }

  //-------------------------------------------------------------------------
  public IdentifierBundle getIdentifiers() {
    return _identifiers;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object object) {
    if (object == this) {
      return true;
    }
    if ((object instanceof HistoricalDataKey)) {
      HistoricalDataKey other = (HistoricalDataKey) object;
      return
          ObjectUtils.equals(_identifiers, _identifiers) &&
          ObjectUtils.equals(_currentDate, other._currentDate) &&
          ObjectUtils.equals(_dataProvider, other._dataProvider) &&
          ObjectUtils.equals(_dataSource, other._dataSource) &&
          ObjectUtils.equals(_dataField, other._dataField) &&
          ObjectUtils.equals(_configName, other._configName);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return ObjectUtils.hashCode(_identifiers) ^
            ObjectUtils.hashCode(_currentDate) ^
            ObjectUtils.hashCode(_dataProvider) ^
            ObjectUtils.hashCode(_dataSource) ^
            ObjectUtils.hashCode(_dataField) ^
            ObjectUtils.hashCode(_configName);
  }

}
