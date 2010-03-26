/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.historicaldata;

import java.util.HashMap;
import java.util.Map;

import javax.time.calendar.LocalDate;

import com.opengamma.id.DomainSpecificIdentifiers;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * In memory HDP for testing.
 * @author jim
 */
public class InMemoryHistoricalDataProvider implements HistoricalDataProvider {
  private Map<CacheKey, LocalDateDoubleTimeSeries> _timeSeriesStore = new HashMap<CacheKey, LocalDateDoubleTimeSeries>();
  private static final boolean INCLUDE_LAST_DAY = true;
  @Override
  public LocalDateDoubleTimeSeries getHistoricalTimeSeries(
      DomainSpecificIdentifiers dsids, String dataSource, String dataProvider,
      String field) {
    LocalDateDoubleTimeSeries entry = _timeSeriesStore.get(new CacheKey(dsids, dataSource, dataProvider, field));
    return entry;
  }

  @Override
  public LocalDateDoubleTimeSeries getHistoricalTimeSeries(
      DomainSpecificIdentifiers dsids, String dataSource, String dataProvider,
      String field, LocalDate start, LocalDate end) {
    LocalDateDoubleTimeSeries dts = getHistoricalTimeSeries(dsids, dataSource, dataProvider, field);
    return (LocalDateDoubleTimeSeries) dts.subSeries(start, true, end, INCLUDE_LAST_DAY);
  }
  
  public void storeHistoricalTimeSeries(DomainSpecificIdentifiers dsids, String dataSource, String dataProvider, String field, LocalDateDoubleTimeSeries dts) {
    _timeSeriesStore.put(new CacheKey(dsids, dataSource, dataProvider, field), dts);
  }
  
  private class CacheKey {
    private DomainSpecificIdentifiers _dsids;
    private String _dataSource;
    private String _dataProvider;
    private String _field;
    
    public CacheKey(DomainSpecificIdentifiers dsids, String dataSource, String dataProvider, String field) {
      _dsids = dsids;
      _dataSource = dataSource;
      _dataProvider = dataProvider;
      _field = field;
    }
    
    @Override
    public int hashCode() {
      return _dsids.hashCode() ^ _field.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (!(obj instanceof CacheKey)) {
        return false;
      }
      CacheKey other = (CacheKey) obj;
      if (_field == null) {
        if (other._field != null) {
          return false;
        }
      } else if (!_field.equals(other._field)) {
        return false;
      }
      if (_dsids == null) {
        if (other._dsids != null) {
          return false;
        }
      } else if (!_dsids.equals(other._dsids)) {
        return false;
      }
      if (_dataProvider == null) {
        if (other._dataProvider != null) {
          return false;
        }
      } else if (!_dataProvider.equals(other._dataProvider)) {
        return false;
      }
      if (_dataSource == null) {
        if (other._dataSource != null) {
          return false;
        }
      } else if (!_dataSource.equals(other._dataSource)) {
        return false;
      }
      return true;
    }
  }

}
