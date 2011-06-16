/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaldata.impl;

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
import com.opengamma.master.historicaldata.HistoricalDataDocument;
import com.opengamma.master.historicaldata.HistoricalDataGetRequest;
import com.opengamma.master.historicaldata.HistoricalDataInfo;
import com.opengamma.master.historicaldata.HistoricalDataInfoResolver;
import com.opengamma.master.historicaldata.HistoricalDataMaster;
import com.opengamma.master.historicaldata.HistoricalDataSearchRequest;
import com.opengamma.master.historicaldata.HistoricalDataSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicSPI;

/**
 * A {@code HistoricalDataSource} implemented using an underlying {@code HistoricalDataMaster}.
 * <p>
 * The {@link HistoricalDataSource} interface provides time-series to the engine via a narrow API.
 * This class provides the source on top of a standard {@link HistoricalDataMaster}.
 */
@PublicSPI
public class MasterHistoricalDataSource implements HistoricalDataSource {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(MasterHistoricalDataSource.class);
  /**
   * The historical data master.
   */
  private final HistoricalDataMaster _master;
  /**
   * The resolver.
   */
  private final HistoricalDataInfoResolver _resolver;

  /**
   * Creates an instance wrapping an underlying historical data master.
   * 
   * @param master  the historical data master, not null
   * @param resolver  the resolver, not null
   */
  public MasterHistoricalDataSource(HistoricalDataMaster master, HistoricalDataInfoResolver resolver) {
    ArgumentChecker.notNull(master, "master");
    ArgumentChecker.notNull(resolver, "resolver");
    _master = master;
    _resolver = resolver;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the underlying historical data master.
   * 
   * @return the historical data master, not null
   */
  public HistoricalDataMaster getMaster() {
    return _master;
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
    HistoricalDataGetRequest request = new HistoricalDataGetRequest(uniqueId);
    request.setLoadEarliestLatest(false);
    request.setLoadTimeSeries(true);
    request.setStart(start);
    request.setEnd(end);
    
    HistoricalDataDocument doc = getMaster().get(request);
    return new HistoricalTimeSeriesImpl(uniqueId, doc.getTimeSeries());
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeries getHistoricalData(
      IdentifierBundle securityBundle, String dataSource, String dataProvider, String dataField) {
    return getHistoricalData(securityBundle, (LocalDate) null, dataSource, dataProvider, dataField, (LocalDate) null, (LocalDate) null);
  }

  @Override
  public HistoricalTimeSeries getHistoricalData(
      IdentifierBundle securityBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField) {
    return getHistoricalData(securityBundle, identifierValidityDate, dataSource, dataProvider, dataField, (LocalDate) null, (LocalDate) null);
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
      IdentifierBundle securityBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField,
      LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    if (start != null && !inclusiveStart) {
      start = start.plusDays(1);
    }
    if (end != null && exclusiveEnd) {
      end = end.minusDays(1);
    }
    return getHistoricalData(securityBundle, identifierValidityDate, dataSource, dataProvider, dataField, start, end);
  }

  private HistoricalTimeSeries getHistoricalData(
      IdentifierBundle identifiers, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField,
      LocalDate start, LocalDate end) {
    ArgumentChecker.notNull(identifiers, "identifiers");
    ArgumentChecker.notNull(dataSource, "dataSource");
    ArgumentChecker.notNull(dataField, "field");
    
    HistoricalDataSearchRequest request = new HistoricalDataSearchRequest();
    request.setIdentifiers(identifiers);
    request.setIdentifierValidityDate(identifierValidityDate);
    request.setDataSource(dataSource);
    request.setDataProvider(dataProvider);
    request.setDataField(dataField);
    request.setStart(start);
    request.setEnd(end);
    request.setLoadTimeSeries(true);
    
    HistoricalDataSearchResult searchResult = getMaster().search(request);
    List<HistoricalDataDocument> documents = searchResult.getDocuments();
    UniqueIdentifier uniqueId = null;
    if (documents.isEmpty()) {
      return null;
    }
    if (documents.size() > 1) {
      Object[] param = new Object[]{identifiers, dataSource, dataProvider, dataField, start, end};
      s_logger.warn("multiple timeseries returned for identifiers={}, dataSource={}, dataProvider={}, dataField={}, start={} end={}", param);
    }
    HistoricalDataDocument doc = documents.get(0);
    uniqueId = doc.getUniqueId();
    return new HistoricalTimeSeriesImpl(uniqueId, doc.getTimeSeries());
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
      IdentifierBundle securityBundle, LocalDate identifierValidityDate, String configDocName) {
    return getHistoricalData(securityBundle, configDocName, identifierValidityDate, null, null);
  }

  @Override
  public HistoricalTimeSeries getHistoricalData(
      IdentifierBundle securityBundle, LocalDate identifierValidityDate, String configDocName, 
      LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    if (start != null && !inclusiveStart) {
      start = start.plusDays(1);
    }
    if (end != null && exclusiveEnd) {
      end = end.minusDays(1);
    }
    return getHistoricalData(securityBundle, configDocName, identifierValidityDate, start, end);
  }

  private HistoricalTimeSeries getHistoricalData(
      IdentifierBundle securityBundle, String configDocName, LocalDate identifierValidityDate, LocalDate start, LocalDate end) {
    ArgumentChecker.isTrue(securityBundle != null && !securityBundle.getIdentifiers().isEmpty(), "Cannot get historical data with null/empty identifiers");
    if (StringUtils.isBlank(configDocName)) {
      configDocName = HistoricalDataInfoFieldNames.DEFAULT_CONFIG_NAME;
    }
    HistoricalDataInfo info = _resolver.getInfo(securityBundle, configDocName);
    if (info == null) {
      return null;
    }
    return getHistoricalData(securityBundle, identifierValidityDate, info.getDataSource(), info.getDataProvider(), info.getDataField(), start, end);
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
    return "MasterHistoricalDataSource[" + getMaster() + "]";
  }

}
