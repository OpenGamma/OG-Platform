/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Clock;
import org.threeten.bp.LocalDate;

import com.google.common.collect.Maps;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.historicaltimeseries.impl.SimpleHistoricalTimeSeries;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.AbstractMasterSource;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesGetFilter;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolutionResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.OpenGammaClock;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * A {@code HistoricalTimeSeriesSource} implemented using an underlying {@code HistoricalTimeSeriesMaster}.
 * <p>
 * The {@link HistoricalTimeSeriesSource} interface provides time-series to the engine via a narrow API. This class provides the source on top of a standard {@link HistoricalTimeSeriesMaster}.
 */
@PublicSPI
public class MasterHistoricalTimeSeriesSource extends AbstractMasterSource<ManageableHistoricalTimeSeriesInfo, HistoricalTimeSeriesInfoDocument, HistoricalTimeSeriesMaster> implements
    HistoricalTimeSeriesSource {

  /** Loggr. */
  private static final Logger s_logger = LoggerFactory.getLogger(MasterHistoricalTimeSeriesSource.class);
  /**
   * An empty time-series.
   */
  private static final LocalDateDoubleTimeSeries EMPTY_TIMESERIES = ImmutableLocalDateDoubleTimeSeries.EMPTY_SERIES;

  /**
   * The resolver.
   */
  private final HistoricalTimeSeriesResolver _resolver;
  /**
   * The clock.
   */
  private final Clock _clock = OpenGammaClock.getInstance();

  /**
   * Creates an instance with an underlying master.
   * 
   * @param master the master, not null
   * @param resolver the resolver, not null
   */
  public MasterHistoricalTimeSeriesSource(final HistoricalTimeSeriesMaster master, final HistoricalTimeSeriesResolver resolver) {
    super(master);
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
    return doGetHistoricalTimeSeries(uniqueId, null, null, null);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(UniqueId uniqueId, LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    if (start != null && !includeStart) {
      start = start.plusDays(1);
    }
    if (end != null && !includeEnd) {
      end = end.minusDays(1);
    }
    return doGetHistoricalTimeSeries(uniqueId, start, end, null);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(UniqueId uniqueId, LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd, int maxPoints) {
    if (start != null && !includeStart) {
      start = start.plusDays(1);
    }
    if (end != null && !includeEnd) {
      end = end.minusDays(1);
    }
    return doGetHistoricalTimeSeries(uniqueId, start, end, maxPoints);
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    HistoricalTimeSeries hts = doGetHistoricalTimeSeries(uniqueId, null, null, -1);
    if (hts != null) {
      LocalDateDoubleTimeSeries ldmts = hts.getTimeSeries();
      return Pairs.of(ldmts.getLatestTime(), ldmts.getLatestValueFast());
    } else {
      return null;
    }
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(UniqueId uniqueId, LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    if (start != null && !includeStart) {
      start = start.plusDays(1);
    }
    if (end != null && !includeEnd) {
      end = end.minusDays(1);
    }
    HistoricalTimeSeries hts = doGetHistoricalTimeSeries(uniqueId, start, end, -1);
    if (hts != null) {
      LocalDateDoubleTimeSeries ldmts = hts.getTimeSeries();
      return Pairs.of(ldmts.getLatestTime(), ldmts.getLatestValueFast());
    } else {
      return null;
    }
  }

  private HistoricalTimeSeries doGetHistoricalTimeSeries(UniqueId uniqueId, LocalDate start, LocalDate end, Integer maxPoints) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    try {
      return getMaster().getTimeSeries(uniqueId, HistoricalTimeSeriesGetFilter.ofRange(start, end, maxPoints));
    } catch (DataNotFoundException ex) {
      return null;
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(ExternalIdBundle securityBundle, String dataSource, String dataProvider, String dataField) {
    return doGetHistoricalTimeSeries(securityBundle, LocalDate.now(getClock()), dataSource, dataProvider, dataField, (LocalDate) null, (LocalDate) null, null);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(ExternalIdBundle securityBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField) {
    return doGetHistoricalTimeSeries(securityBundle, identifierValidityDate, dataSource, dataProvider, dataField, (LocalDate) null, (LocalDate) null, null);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(ExternalIdBundle securityBundle, String dataSource, String dataProvider, String dataField, LocalDate start, boolean includeStart,
      LocalDate end, boolean includeEnd) {
    if (start != null && !includeStart) {
      start = start.plusDays(1);
    }
    if (end != null && !includeEnd) {
      end = end.minusDays(1);
    }
    return doGetHistoricalTimeSeries(securityBundle, LocalDate.now(getClock()), dataSource, dataProvider, dataField, start, end, null);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(ExternalIdBundle securityBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField,
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    if (start != null && !includeStart) {
      start = start.plusDays(1);
    }
    if (end != null && !includeEnd) {
      end = end.minusDays(1);
    }
    return doGetHistoricalTimeSeries(securityBundle, identifierValidityDate, dataSource, dataProvider, dataField, start, end, null);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(ExternalIdBundle securityBundle, String dataSource, String dataProvider, String dataField, LocalDate start, boolean includeStart,
      LocalDate end, boolean includeEnd, int maxPoints) {
    if (start != null && !includeStart) {
      start = start.plusDays(1);
    }
    if (end != null && !includeEnd) {
      end = end.minusDays(1);
    }
    return doGetHistoricalTimeSeries(securityBundle, LocalDate.now(getClock()), dataSource, dataProvider, dataField, start, end, maxPoints);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(ExternalIdBundle securityBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField,
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd, int maxPoints) {

    if (start != null && !includeStart) {
      start = start.plusDays(1);
    }
    if (end != null && !includeEnd) {
      end = end.minusDays(1);
    }
    return doGetHistoricalTimeSeries(securityBundle, identifierValidityDate, dataSource, dataProvider, dataField, start, end, maxPoints);
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField) {
    HistoricalTimeSeries hts = doGetHistoricalTimeSeries(identifierBundle, identifierValidityDate, dataSource, dataProvider, dataField, null, null, -1);
    if (hts != null) {
      LocalDateDoubleTimeSeries ldmts = hts.getTimeSeries();
      return Pairs.of(ldmts.getLatestTime(), ldmts.getLatestValueFast());
    } else {
      return null;
    }
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField,
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    if (start != null && !includeStart) {
      start = start.plusDays(1);
    }
    if (end != null && !includeEnd) {
      end = end.minusDays(1);
    }
    HistoricalTimeSeries hts = doGetHistoricalTimeSeries(identifierBundle, identifierValidityDate, dataSource, dataProvider, dataField, start, end, -1);
    if (hts != null) {
      LocalDateDoubleTimeSeries lddts = hts.getTimeSeries();
      return Pairs.of(lddts.getLatestTime(), lddts.getLatestValueFast());
    } else {
      return null;
    }
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(ExternalIdBundle identifierBundle, String dataSource, String dataProvider, String dataField) {
    return getLatestDataPoint(identifierBundle, LocalDate.now(getClock()), dataSource, dataProvider, dataField);
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(ExternalIdBundle identifierBundle, String dataSource, String dataProvider, String dataField, LocalDate start, boolean includeStart,
      LocalDate end, boolean includeEnd) {
    return getLatestDataPoint(identifierBundle, LocalDate.now(getClock()), dataSource, dataProvider, dataField, start, includeStart, end, includeEnd);
  }

  private HistoricalTimeSeries doGetHistoricalTimeSeries(ExternalIdBundle identifiers, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField,
      LocalDate start, LocalDate end, Integer maxPoints) {
    ArgumentChecker.notNull(identifiers, "identifiers");
    ArgumentChecker.notNull(dataSource, "dataSource");
    ArgumentChecker.notNull(dataField, "field");
    HistoricalTimeSeriesResolutionResult resolutionResult = getResolver().resolve(identifiers, identifierValidityDate, dataSource, dataProvider, dataField, null);
    if (resolutionResult == null) {
      return null;
    }
    HistoricalTimeSeries hts = doGetHistoricalTimeSeries(resolutionResult.getHistoricalTimeSeriesInfo().getTimeSeriesObjectId(), start, end, maxPoints);
    if (resolutionResult.getAdjuster() != null) {
      hts = resolutionResult.getAdjuster().adjust(resolutionResult.getHistoricalTimeSeriesInfo().getExternalIdBundle().toBundle(), hts);
    }
    return hts;
  }

  private HistoricalTimeSeries doGetHistoricalTimeSeries(ObjectId objectId, LocalDate start, LocalDate end, Integer maxPoints) {
    ArgumentChecker.notNull(objectId, "objectId");
    try {
      return getMaster().getTimeSeries(objectId, VersionCorrection.LATEST, HistoricalTimeSeriesGetFilter.ofRange(start, end, maxPoints));
    } catch (DataNotFoundException ex) {
      return null;
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(String dataField, ExternalIdBundle identifierBundle, String resolutionKey) {
    return doGetHistoricalTimeSeries(dataField, identifierBundle, LocalDate.now(getClock()), resolutionKey, (LocalDate) null, (LocalDate) null, (Integer) null);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(String dataField, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey) {
    return doGetHistoricalTimeSeries(dataField, identifierBundle, identifierValidityDate, resolutionKey, (LocalDate) null, (LocalDate) null, (Integer) null);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(String dataField, ExternalIdBundle identifierBundle, String resolutionKey, LocalDate start, boolean includeStart, LocalDate end,
      boolean includeEnd) {
    if (start != null && !includeStart) {
      start = start.plusDays(1);
    }
    if (end != null && !includeEnd) {
      end = end.minusDays(1);
    }
    return doGetHistoricalTimeSeries(dataField, identifierBundle, LocalDate.now(getClock()), resolutionKey, start, end, (Integer) null);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(String dataField, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey, LocalDate start,
      boolean includeStart, LocalDate end, boolean includeEnd) {
    if (start != null && !includeStart) {
      start = start.plusDays(1);
    }
    if (end != null && !includeEnd) {
      end = end.minusDays(1);
    }
    return doGetHistoricalTimeSeries(dataField, identifierBundle, identifierValidityDate, resolutionKey, start, end, (Integer) null);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(String dataField, ExternalIdBundle identifierBundle, String resolutionKey, LocalDate start, boolean includeStart, LocalDate end,
      boolean includeEnd, int maxPoints) {
    if (start != null && !includeStart) {
      start = start.plusDays(1);
    }
    if (end != null && !includeEnd) {
      end = end.minusDays(1);
    }
    return doGetHistoricalTimeSeries(dataField, identifierBundle, LocalDate.now(getClock()), resolutionKey, start, end, maxPoints);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(String dataField, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey, LocalDate start,
      boolean includeStart, LocalDate end, boolean includeEnd, int maxPoints) {
    if (start != null && !includeStart) {
      start = start.plusDays(1);
    }
    if (end != null && !includeEnd) {
      end = end.minusDays(1);
    }
    return doGetHistoricalTimeSeries(dataField, identifierBundle, identifierValidityDate, resolutionKey, start, end, maxPoints);
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(String dataField, ExternalIdBundle identifierBundle, String resolutionKey) {
    return getLatestDataPoint(dataField, identifierBundle, LocalDate.now(getClock()), resolutionKey);
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(String dataField, ExternalIdBundle identifierBundle, String resolutionKey, LocalDate start, boolean includeStart, LocalDate end,
      boolean includeEnd) {
    return getLatestDataPoint(dataField, identifierBundle, LocalDate.now(getClock()), resolutionKey, start, includeStart, end, includeEnd);
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(String dataField, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey) {
    return getLatestDataPoint(dataField, identifierBundle, identifierValidityDate, resolutionKey, (LocalDate) null, true, (LocalDate) null, true);
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(String dataField, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey, LocalDate start,
      boolean includeStart, LocalDate end, boolean includeEnd) {
    if (start != null && !includeStart) {
      start = start.plusDays(1);
    }
    if (end != null && !includeEnd) {
      end = end.minusDays(1);
    }
    HistoricalTimeSeries hts = doGetHistoricalTimeSeries(dataField, identifierBundle, identifierValidityDate, dataField, start, end, -1);
    if (hts != null) {
      LocalDateDoubleTimeSeries lddts = hts.getTimeSeries();
      return Pairs.of(lddts.getLatestTime(), lddts.getLatestValueFast());
    } else {
      return null;
    }
  }

  private HistoricalTimeSeries doGetHistoricalTimeSeries(String dataField, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey, LocalDate start,
      LocalDate end, Integer maxPoints) {
    ArgumentChecker.notNull(dataField, "dataField");
    ArgumentChecker.notEmpty(identifierBundle, "identifierBundle");
    if (StringUtils.isBlank(resolutionKey)) {
      resolutionKey = HistoricalTimeSeriesRatingFieldNames.DEFAULT_CONFIG_NAME;
    }
    HistoricalTimeSeriesResolutionResult resolutionResult = getResolver().resolve(identifierBundle, identifierValidityDate, null, null, dataField, resolutionKey);
    if (resolutionResult == null) {
      String message = String.format("Unable to resolve hts using resolutionKey[%s] dataField[%s] bundle[%s] date[%s]", resolutionKey, dataField, identifierBundle, identifierValidityDate);
      s_logger.debug(message);
      return null;
    }
    if ((maxPoints == null) || (maxPoints != 0)) {
      HistoricalTimeSeries hts = doGetHistoricalTimeSeries(resolutionResult.getHistoricalTimeSeriesInfo().getUniqueId(), start, end, maxPoints);
      if (resolutionResult.getAdjuster() != null) {
        hts = resolutionResult.getAdjuster().adjust(resolutionResult.getHistoricalTimeSeriesInfo().getExternalIdBundle().toBundle(), hts);
      }
      return hts;
    } else {
      return new SimpleHistoricalTimeSeries(resolutionResult.getHistoricalTimeSeriesInfo().getUniqueId(), EMPTY_TIMESERIES);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public Map<ExternalIdBundle, HistoricalTimeSeries> getHistoricalTimeSeries(Set<ExternalIdBundle> identifierSet, String dataSource, String dataProvider, String dataField, LocalDate start,
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

  @Override
  public ExternalIdBundle getExternalIdBundle(UniqueId uniqueId) {
    HistoricalTimeSeriesInfoDocument historicalTimeSeriesInfoDocument = getMaster().get(uniqueId);
    if (historicalTimeSeriesInfoDocument != null && historicalTimeSeriesInfoDocument.getInfo() != null && historicalTimeSeriesInfoDocument.getInfo().getExternalIdBundle() != null) {
      return historicalTimeSeriesInfoDocument.getInfo().getExternalIdBundle().toBundle();
    } else {
      s_logger.warn("Cannot find time series info document, or info field is null, or id bundle is null, returning null");
      return null;
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public ChangeManager changeManager() {
    return getMaster().changeManager();
  }

}
