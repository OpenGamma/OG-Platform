/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.timeseries.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.LocalDate;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.historicaldata.HistoricalDataSource;
import com.opengamma.core.historicaldata.HistoricalTimeSeries;
import com.opengamma.core.historicaldata.impl.HistoricalTimeSeriesImpl;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.timeseries.TimeSeriesDocument;
import com.opengamma.master.timeseries.TimeSeriesInfo;
import com.opengamma.master.timeseries.TimeSeriesInfoResolver;
import com.opengamma.master.timeseries.TimeSeriesMaster;
import com.opengamma.master.timeseries.TimeSeriesSearchRequest;
import com.opengamma.master.timeseries.TimeSeriesSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * A {@code HistoricalDataSource} implemented using an underlying {@code TimeSeriesMaster}.
 * <p>
 * The {@link HistoricalDataSource} interface provides time-series to the engine via a narrow API.
 * This class provides the source on top of a standard {@link TimeSeriesMaster}.
 */
@PublicSPI
public class MasterHistoricalDataSource implements HistoricalDataSource {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(MasterHistoricalDataSource.class);
  /**
   * The time-series master.
   */
  private final TimeSeriesMaster<LocalDate> _timeSeriesMaster;
  /**
   * The time-series resolver.
   */
  private final TimeSeriesInfoResolver _timeSeriesResolver;

  /**
   * Creates an instance wrapping an underlying time-series master.
   * 
   * @param timeSeriesMaster the time-series master, not null
   * @param timeSeriesResolver the time-series resolver, not null
   */
  public MasterHistoricalDataSource(TimeSeriesMaster<LocalDate> timeSeriesMaster, TimeSeriesInfoResolver timeSeriesResolver) {
    ArgumentChecker.notNull(timeSeriesMaster, "timeSeriesMaster");
    ArgumentChecker.notNull(timeSeriesResolver, "timeSeriesResolver");
    _timeSeriesMaster = timeSeriesMaster;
    _timeSeriesResolver = timeSeriesResolver;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the underlying time-series master.
   * 
   * @return the time-series master, not null
   */
  public TimeSeriesMaster<LocalDate> getTimeSeriesMaster() {
    return _timeSeriesMaster;
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeries getHistoricalData(UniqueIdentifier uniqueId) {
    return getHistoricalData(uniqueId, null, null);
  }

  @Override
  public HistoricalTimeSeries getHistoricalData(UniqueIdentifier uniqueId, LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    if (start != null && !inclusiveStart) {
      start = start.plusDays(1);
    }
    if (end != null && exclusiveEnd) {
      end = end.minusDays(1);
    }
    return getHistoricalData(uniqueId, start, end);
  }

  private HistoricalTimeSeries getHistoricalData(UniqueIdentifier uniqueId, LocalDate start, LocalDate end) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    TimeSeriesSearchRequest<LocalDate> request = new TimeSeriesSearchRequest<LocalDate>();
    request.setLoadTimeSeries(true);
    request.setStart(start);
    request.setEnd(end);
    request.setTimeSeriesId(uniqueId);
    request.setLoadTimeSeries(true);
    
    LocalDateDoubleTimeSeries result = new ArrayLocalDateDoubleTimeSeries();
    TimeSeriesSearchResult<LocalDate> searchResult = getTimeSeriesMaster().search(request);
    List<TimeSeriesDocument<LocalDate>> documents = searchResult.getDocuments();
    if (!documents.isEmpty()) {
      if (documents.size() > 1) {
        s_logger.warn("multiple timeseries return for uniqueId={}", uniqueId);
      }
      result = documents.get(0).getTimeSeries().toLocalDateDoubleTimeSeries();
    }
    return new HistoricalTimeSeriesImpl(uniqueId, result);
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeries getHistoricalData(
      IdentifierBundle securityBundle, String dataSource, String dataProvider, String dataField) {
    return getHistoricalData(securityBundle, (LocalDate) null, dataSource, dataProvider, dataField, (LocalDate) null, (LocalDate) null);
  }

  @Override
  public HistoricalTimeSeries getHistoricalData(
      IdentifierBundle securityBundle, LocalDate currentDate, String dataSource, String dataProvider, String dataField) {
    return getHistoricalData(securityBundle, currentDate, dataSource, dataProvider, dataField, (LocalDate) null, (LocalDate) null);
  }

  @Override
  public HistoricalTimeSeries getHistoricalData(
      IdentifierBundle securityBundle, String dataSource, String dataProvider, String dataField,
      LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    if (start != null && !inclusiveStart) {
      start = start.plusDays(1);
    }
    if (end != null && exclusiveEnd) {
      end = end.minusDays(1);
    }
    return getHistoricalData(securityBundle, (LocalDate) null, dataSource, dataProvider, dataField, start, end);
  }

  @Override
  public HistoricalTimeSeries getHistoricalData(
      IdentifierBundle securityBundle, LocalDate currentDate, String dataSource, String dataProvider, String dataField,
      LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    if (start != null && !inclusiveStart) {
      start = start.plusDays(1);
    }
    if (end != null && exclusiveEnd) {
      end = end.minusDays(1);
    }
    return getHistoricalData(securityBundle, currentDate, dataSource, dataProvider, dataField, start, end);
  }

  private HistoricalTimeSeries getHistoricalData(
      IdentifierBundle identifiers, LocalDate currentDate, String dataSource, String dataProvider, String dataField,
      LocalDate start, LocalDate end) {
    ArgumentChecker.notNull(identifiers, "identifiers");
    ArgumentChecker.notNull(dataSource, "dataSource");
    ArgumentChecker.notNull(dataField, "field");
    
    TimeSeriesSearchRequest<LocalDate> request = new TimeSeriesSearchRequest<LocalDate>();
    request.setIdentifiers(identifiers);
    request.setDataSource(dataSource);
    request.setDataProvider(dataProvider);
    request.setDataField(dataField);
    request.setStart(start);
    request.setEnd(end);
    request.setCurrentDate(currentDate);
    request.setLoadTimeSeries(true);
    
    TimeSeriesSearchResult<LocalDate> searchResult = getTimeSeriesMaster().search(request);
    List<TimeSeriesDocument<LocalDate>> documents = searchResult.getDocuments();
    UniqueIdentifier uniqueId = null;
    if (documents.isEmpty()) {
      return null;
    }
    if (documents.size() > 1) {
      Object[] param = new Object[]{identifiers, dataSource, dataProvider, dataField, start, end};
      s_logger.warn("multiple timeseries returned for identifiers={}, dataSource={}, dataProvider={}, dataField={}, start={} end={}", param);
    }
    TimeSeriesDocument<LocalDate> timeSeriesDocument = documents.get(0);
    LocalDateDoubleTimeSeries ts = timeSeriesDocument.getTimeSeries().toLocalDateDoubleTimeSeries();
    uniqueId = timeSeriesDocument.getUniqueId();
    return new HistoricalTimeSeriesImpl(uniqueId, ts);
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeries getHistoricalData(
      IdentifierBundle securityBundle, String configDocName) {
    return getHistoricalData(securityBundle, configDocName, (LocalDate) null, (LocalDate) null, (LocalDate) null);
  }

  @Override
  public HistoricalTimeSeries getHistoricalData(
      IdentifierBundle securityBundle, String configDocName, 
      LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    if (start != null && !inclusiveStart) {
      start = start.plusDays(1);
    }
    if (end != null && exclusiveEnd) {
      end = end.minusDays(1);
    }
    return getHistoricalData(securityBundle, configDocName, (LocalDate) null, start, end);
  }

  @Override
  public HistoricalTimeSeries getHistoricalData(
      IdentifierBundle securityBundle, LocalDate currentDate, String configDocName) {
    return getHistoricalData(securityBundle, configDocName, currentDate, null, null);
  }

  @Override
  public HistoricalTimeSeries getHistoricalData(
      IdentifierBundle securityBundle, LocalDate currentDate, String configDocName, 
      LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    if (start != null && !inclusiveStart) {
      start = start.plusDays(1);
    }
    if (end != null && exclusiveEnd) {
      end = end.minusDays(1);
    }
    return getHistoricalData(securityBundle, configDocName, currentDate, start, end);
  }

  private HistoricalTimeSeries getHistoricalData(
      IdentifierBundle securityBundle, String configDocName, LocalDate currentDate, LocalDate start, LocalDate end) {
    ArgumentChecker.isTrue(securityBundle != null && !securityBundle.getIdentifiers().isEmpty(), "Cannot get historical data with null/empty identifiers");
    if (StringUtils.isBlank(configDocName)) {
      configDocName = TimeSeriesInfoFieldNames.DEFAULT_CONFIG_NAME;
    }
    TimeSeriesInfo info = _timeSeriesResolver.getInfo(securityBundle, configDocName);
    if (info == null) {
      return null;
    }
    return getHistoricalData(securityBundle, currentDate, info.getDataSource(), info.getDataProvider(), info.getDataField(), start, end);
  }

  //-------------------------------------------------------------------------
  @Override
  public Map<IdentifierBundle, HistoricalTimeSeries> getHistoricalData(
      Set<IdentifierBundle> identifierSet, String dataSource, String dataProvider, String dataField, LocalDate start,
      boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    // TODO [PLAT-1046]
    throw new NotImplementedException();
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return "MasterHistoricalDataSource[" + getTimeSeriesMaster() + "]";
  }

}
