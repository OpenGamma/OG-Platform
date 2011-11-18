/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.LocalDate;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSummary;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.AbstractMasterSource;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicSPI;

/**
 * A {@code HistoricalTimeSeriesSource} implemented using an underlying {@code HistoricalTimeSeriesMaster}.
 * <p>
 * The {@link HistoricalTimeSeriesSource} interface provides time-series to the engine via a narrow API.
 * This class provides the source on top of a standard {@link HistoricalTimeSeriesMaster}.
 */
@PublicSPI
public class MasterHistoricalTimeSeriesSource
    extends AbstractMasterSource<HistoricalTimeSeriesInfoDocument, HistoricalTimeSeriesMaster>
    implements HistoricalTimeSeriesSource {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(MasterHistoricalTimeSeriesSource.class);

  /**
   * The resolver.
   */
  private final HistoricalTimeSeriesResolver _resolver;
  /**
   * The clock.
   */
  private final Clock _clock = Clock.systemDefaultZone();  // TODO: TIMEZONE

  /**
   * Creates an instance with an underlying master which does not override versions.
   * 
   * @param master  the master, not null
   * @param resolver  the resolver, not null
   */
  public MasterHistoricalTimeSeriesSource(final HistoricalTimeSeriesMaster master, HistoricalTimeSeriesResolver resolver) {
    super(master);
    ArgumentChecker.notNull(resolver, "resolver");
    _resolver = resolver;
  }

  /**
   * Creates an instance with an underlying master optionally overriding the requested version.
   * 
   * @param master  the master, not null
   * @param resolver  the resolver, not null
   * @param versionCorrection  the version-correction locator to search at, null to not override versions
   */
  public MasterHistoricalTimeSeriesSource(final HistoricalTimeSeriesMaster master, HistoricalTimeSeriesResolver resolver, VersionCorrection versionCorrection) {
    super(master, versionCorrection);
    ArgumentChecker.notNull(resolver, "resolver");
    _resolver = resolver;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the time-series resolver.
   * 
   * @return the historical time-series resolver, not null
   */
  public HistoricalTimeSeriesResolver getResolver() {
    return _resolver;
  }

  /**
   * Gets the clock.
   * 
   * @return the clock, not null
   */
  public Clock getClock() {
    return _clock;
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(UniqueId uniqueId) {
    return doGetHistoricalTimeSeries(uniqueId, null, null);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(UniqueId uniqueId, LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    if (start != null && !includeStart) {
      start = start.plusDays(1);
    }
    if (end != null && !includeEnd) {
      end = end.minusDays(1);
    }
    return doGetHistoricalTimeSeries(uniqueId, start, end);
  }

  private HistoricalTimeSeries doGetHistoricalTimeSeries(UniqueId uniqueId, LocalDate start, LocalDate end) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    final VersionCorrection vc = getVersionCorrection();  // lock against change
    try {
      if (vc != null) {
        return getMaster().getTimeSeries(uniqueId, vc, start, end);
      } else {
        return getMaster().getTimeSeries(uniqueId, start, end);
      }
    } catch (DataNotFoundException ex) {
      return null;
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      ExternalIdBundle securityBundle, String dataSource, String dataProvider, String dataField) {
    // TODO: TIMEZONE
    return doGetHistoricalTimeSeries(securityBundle, LocalDate.now(getClock()), dataSource, dataProvider, dataField, (LocalDate) null, (LocalDate) null);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      ExternalIdBundle securityBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField) {
    return doGetHistoricalTimeSeries(securityBundle, identifierValidityDate, dataSource, dataProvider, dataField, (LocalDate) null, (LocalDate) null);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      ExternalIdBundle securityBundle, String dataSource, String dataProvider, String dataField,
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    if (start != null && !includeStart) {
      start = start.plusDays(1);
    }
    if (end != null && !includeEnd) {
      end = end.minusDays(1);
    }
    // TODO: TIMEZONE
    return doGetHistoricalTimeSeries(securityBundle, LocalDate.now(getClock()), dataSource, dataProvider, dataField, start, end);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      ExternalIdBundle securityBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField,
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    if (start != null && !includeStart) {
      start = start.plusDays(1);
    }
    if (end != null && !includeEnd) {
      end = end.minusDays(1);
    }
    return doGetHistoricalTimeSeries(securityBundle, identifierValidityDate, dataSource, dataProvider, dataField, start, end);
  }

  private HistoricalTimeSeries doGetHistoricalTimeSeries(
      ExternalIdBundle identifiers, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField,
      LocalDate start, LocalDate end) {
    ArgumentChecker.notNull(identifiers, "identifiers");
    ArgumentChecker.notNull(dataSource, "dataSource");
    ArgumentChecker.notNull(dataField, "field");
    
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest(identifiers);
    request.setValidityDate(identifierValidityDate);
    request.setDataSource(dataSource);
    request.setDataProvider(dataProvider);
    request.setDataField(dataField);
    
    HistoricalTimeSeriesInfoSearchResult searchResult = getMaster().search(request);
    List<HistoricalTimeSeriesInfoDocument> documents = searchResult.getDocuments();
    if (documents.isEmpty()) {
      return null;
    }
    if (documents.size() > 1) {
      Object[] param = new Object[]{identifiers, dataSource, dataProvider, dataField, start, end};
      s_logger.warn("Multiple time-series returned for identifiers={}, dataSource={}, dataProvider={}, dataField={}, start={} end={}", param);
    }
    HistoricalTimeSeriesInfoDocument doc = documents.get(0);
    return doGetHistoricalTimeSeries(doc.getInfo().getTimeSeriesObjectId(), start, end);
  }

  private HistoricalTimeSeries doGetHistoricalTimeSeries(ObjectId objectId, LocalDate start, LocalDate end) {
    ArgumentChecker.notNull(objectId, "objectId");
    VersionCorrection vc = getVersionCorrection();  // lock against change
    vc = Objects.firstNonNull(vc, VersionCorrection.LATEST);
    try {
      return getMaster().getTimeSeries(objectId, vc, start, end);
    } catch (DataNotFoundException ex) {
      return null;
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      String dataField, ExternalIdBundle identifierBundle, String resolutionKey) {
    // TODO: TIMEZONE
    return doGetHistoricalTimeSeries(dataField, identifierBundle, LocalDate.now(getClock()), resolutionKey, (LocalDate) null, (LocalDate) null);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      String dataField, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey) {
    return doGetHistoricalTimeSeries(dataField, identifierBundle, identifierValidityDate, resolutionKey, null, null);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      String dataField, ExternalIdBundle identifierBundle, String resolutionKey, 
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    if (start != null && !includeStart) {
      start = start.plusDays(1);
    }
    if (end != null && !includeEnd) {
      end = end.minusDays(1);
    }
    // TODO: TIMEZONE
    return doGetHistoricalTimeSeries(dataField, identifierBundle, LocalDate.now(getClock()), resolutionKey, start, end);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      String dataField, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey, 
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    if (start != null && !includeStart) {
      start = start.plusDays(1);
    }
    if (end != null && !includeEnd) {
      end = end.minusDays(1);
    }
    return doGetHistoricalTimeSeries(dataField, identifierBundle, identifierValidityDate, resolutionKey, start, end);
  }

  private HistoricalTimeSeries doGetHistoricalTimeSeries(
      String dataField, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey,
      LocalDate start, LocalDate end) {
    ArgumentChecker.notNull(dataField, "dataField");
    ArgumentChecker.notEmpty(identifierBundle, "identifierBundle");
    if (StringUtils.isBlank(resolutionKey)) {
      resolutionKey = HistoricalTimeSeriesRatingFieldNames.DEFAULT_CONFIG_NAME;
    }
    UniqueId uniqueId = getResolver().resolve(dataField, identifierBundle, identifierValidityDate, resolutionKey);
    if (uniqueId == null) {
      return null;
    }
    return doGetHistoricalTimeSeries(uniqueId, start, end);
  }

  //-------------------------------------------------------------------------
  @Override
  public Map<ExternalIdBundle, HistoricalTimeSeries> getHistoricalTimeSeries(
      Set<ExternalIdBundle> identifierSet, String dataSource, String dataProvider, String dataField, LocalDate start,
      boolean includeStart, LocalDate end, boolean includeEnd) {
    ArgumentChecker.notNull(identifierSet, "identifierSet");
    Map<ExternalIdBundle, HistoricalTimeSeries> result = Maps.newHashMap();
    for (ExternalIdBundle externalIdBundle : identifierSet) {
      HistoricalTimeSeries historicalTimeSeries = getHistoricalTimeSeries(externalIdBundle, dataSource, dataProvider, dataField, start, includeStart, end, includeEnd);
      result.put(externalIdBundle, historicalTimeSeries);
    }
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return "MasterHistoricalTimeSeriesSource[" + getMaster() + "]";
  }

  public HistoricalTimeSeriesSummary getSummary(UniqueId uniqueId) {
    return getMaster().getSummary(uniqueId);
  }
  
  public HistoricalTimeSeriesSummary getSummary(ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    return getMaster().getSummary(objectId, versionCorrection);
  }

//  @Override
//  public LocalDate getEarliestDate(UniqueId uniqueId) {
//    return getMaster().getEarliestDate(uniqueId);
//    
//  }
//
//  @Override
//  public LocalDate getLatestDate(UniqueId uniqueId) {
//    return getMaster().getEarliestDate(uniqueId);
//  }
//
//  @Override
//  public Double getEarliestValue(UniqueId uniqueId) {
//    return getMaster().getEarliestValue(uniqueId);
//  }
//
//  @Override
//  public Double getLatestValue(UniqueId uniqueId) {
//    return getMaster().getLatestValue(uniqueId);
//  }
  
}
