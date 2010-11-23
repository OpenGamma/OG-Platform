/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.historicaldata;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.time.calendar.LocalDate;

import com.google.common.base.Supplier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.UniqueIdentifierSupplier;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * In memory HDP for testing.
 */
public class InMemoryHistoricalDataProvider implements HistoricalDataSource {

  /**
   * The default scheme used for each {@link UniqueIdentifier}.
   */
  public static final String DEFAULT_UID_SCHEME = "MemoryTS";

  /**
   * The store of unique identifiers.
   */
  private Map<MetaDataKey, UniqueIdentifier> _metaUniqueIdentifierStore = new ConcurrentHashMap<MetaDataKey, UniqueIdentifier>();
  /**
   * The store of unique time-series.
   */
  private Map<UniqueIdentifier, LocalDateDoubleTimeSeries> _timeSeriesStore = new ConcurrentHashMap<UniqueIdentifier, LocalDateDoubleTimeSeries>();
  /**
   * The supplied of identifiers.
   */
  private final Supplier<UniqueIdentifier> _uidSupplier;

  /**
   * Creates an empty TimeSeriesSource using the default scheme for any {@link UniqueIdentifier}s created.
   */
  public InMemoryHistoricalDataProvider() {
    this(new UniqueIdentifierSupplier(DEFAULT_UID_SCHEME));
  }

  /**
   * Creates an instance specifying the supplier of unique identifiers.
   * 
   * @param uidSupplier  the supplier of unique identifiers, not null
   */
  public InMemoryHistoricalDataProvider(final Supplier<UniqueIdentifier> uidSupplier) {
    ArgumentChecker.notNull(uidSupplier, "uidSupplier");
    _uidSupplier = uidSupplier;
  }

  @Override
  public Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getHistoricalData(
      IdentifierBundle identifiers, String dataSource, String dataProvider,
      String dataField) {
    return getHistoricalData(identifiers, (LocalDate) null, dataSource, dataProvider, dataField);
  }

  @Override
  public LocalDateDoubleTimeSeries getHistoricalData(UniqueIdentifier uid) {
    LocalDateDoubleTimeSeries timeSeries = _timeSeriesStore.get(uid);
    if (timeSeries == null) {
      return new ArrayLocalDateDoubleTimeSeries();
    } else {
      return timeSeries;
    }
  }

  public void storeHistoricalTimeSeries(IdentifierBundle dsids, String dataSource, String dataProvider, String field, LocalDateDoubleTimeSeries dts) {
    MetaDataKey metaKey = new MetaDataKey(null, null, dsids, dataSource, dataProvider, field);
    UniqueIdentifier uid = null;
    synchronized (this) {
      uid = _metaUniqueIdentifierStore.get(metaKey);
      if (uid == null) {
        uid = _uidSupplier.get();
        _metaUniqueIdentifierStore.put(metaKey, uid);
      }
    }
    _timeSeriesStore.put(uid, dts);
  }

  @Override
  public Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getHistoricalData(IdentifierBundle identifiers, String configDocName) {
    throw new UnsupportedOperationException(getClass().getName() + " does not support getHistorical without metadata");
  }

  @Override
  public Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getHistoricalData(IdentifierBundle identifiers, String configName, 
      LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    throw new UnsupportedOperationException(getClass().getName() + " does not support getHistorical without metadata");
  }

  @Override
  public Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getHistoricalData(IdentifierBundle identifiers, String dataSource, String dataProvider, String field, LocalDate start,
      boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> tsPair = getHistoricalData(identifiers, dataSource, dataProvider, field);
    if (tsPair != null) {
      LocalDateDoubleTimeSeries timeSeries = (LocalDateDoubleTimeSeries) tsPair.getSecond().subSeries(start, inclusiveStart, end, exclusiveEnd);
      return Pair.of(tsPair.getKey(), timeSeries);
    } else {
      return null;
    }
  }

  @Override
  public LocalDateDoubleTimeSeries getHistoricalData(UniqueIdentifier uid, LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    LocalDateDoubleTimeSeries timeseries = getHistoricalData(uid);
    if (timeseries != null) {
      return (LocalDateDoubleTimeSeries) timeseries.subSeries(start, inclusiveStart, end, exclusiveEnd);
    } else {
      return null;
    }
  }

  @Override
  public Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getHistoricalData(IdentifierBundle identifiers, LocalDate currentDate, String dataSource, String dataProvider, String dataField) {
    UniqueIdentifier uid = _metaUniqueIdentifierStore.get(new MetaDataKey(null, currentDate, identifiers, dataSource, dataProvider, dataField));
    if (uid == null) {
      return new ObjectsPair<UniqueIdentifier, LocalDateDoubleTimeSeries>(null, new ArrayLocalDateDoubleTimeSeries());
    } else {
      return new ObjectsPair<UniqueIdentifier, LocalDateDoubleTimeSeries>(uid, _timeSeriesStore.get(uid));
    }
  }

  @Override
  public Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getHistoricalData(IdentifierBundle identifiers, LocalDate currentDate, String dataSource, String dataProvider, String field,
      LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> tsPair = getHistoricalData(identifiers, currentDate, dataSource, dataProvider, field);
    if (tsPair != null) {
      LocalDateDoubleTimeSeries timeSeries = (LocalDateDoubleTimeSeries) tsPair.getSecond().subSeries(start, inclusiveStart, end, exclusiveEnd);
      return Pair.of(tsPair.getKey(), timeSeries);
    } else {
      return null;
    }
  }

  @Override
  public Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getHistoricalData(IdentifierBundle identifiers, LocalDate currentDate, String configDocName) {
    throw new UnsupportedOperationException(getClass().getName() + " does not support getHistorical without metadata");
  }

  @Override
  public Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getHistoricalData(IdentifierBundle identifiers, LocalDate currentDate, String configDocName,
      LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    throw new UnsupportedOperationException(getClass().getName() + " does not support getHistorical without metadata");
  }

}
