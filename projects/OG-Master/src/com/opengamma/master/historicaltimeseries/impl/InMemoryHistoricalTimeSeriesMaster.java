/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.time.Instant;
import javax.time.calendar.LocalDate;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.JodaBeanUtils;

import com.google.common.base.Objects;
import com.google.common.base.Supplier;
import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeType;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSummary;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdSupplier;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoHistoryRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoHistoryResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoMetaDataRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoMetaDataResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeries;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.timeseries.DoubleTimeSeriesOperators;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.MutableLocalDateDoubleTimeSeries;

/**
 * An in-memory implementation of a historical time-series master.
 */
public class InMemoryHistoricalTimeSeriesMaster implements HistoricalTimeSeriesMaster {

  /**
   * The default scheme used for each {@link UniqueId}.
   */
  public static final String DEFAULT_OID_SCHEME = "MemHts";

  /**
   * A cache of time-series info by identifier.
   */
  private final ConcurrentMap<ObjectId, HistoricalTimeSeriesInfoDocument> _storeInfo = new ConcurrentHashMap<ObjectId, HistoricalTimeSeriesInfoDocument>();
  /**
   * A cache of time-series points by identifier.
   */
  private final ConcurrentMap<ObjectId, LocalDateDoubleTimeSeries> _storePoints = new ConcurrentHashMap<ObjectId, LocalDateDoubleTimeSeries>();
  /**
   * The supplied of identifiers.
   */
  private final Supplier<ObjectId> _objectIdSupplier;
  /**
   * The change manager.
   */
  private final ChangeManager _changeManager;

  /**
   * Creates an instance.
   */
  public InMemoryHistoricalTimeSeriesMaster() {
    this(new ObjectIdSupplier(DEFAULT_OID_SCHEME));
  }

  /**
   * Creates an instance specifying the change manager.
   * 
   * @param changeManager  the change manager, not null
   */
  public InMemoryHistoricalTimeSeriesMaster(final ChangeManager changeManager) {
    this(new ObjectIdSupplier(DEFAULT_OID_SCHEME), changeManager);
  }

  /**
   * Creates an instance specifying the supplier of object identifiers.
   * 
   * @param objectIdSupplier  the supplier of object identifiers, not null
   */
  public InMemoryHistoricalTimeSeriesMaster(final Supplier<ObjectId> objectIdSupplier) {
    this(objectIdSupplier, new BasicChangeManager());
  }

  /**
   * Creates an instance specifying the supplier of object identifiers and change manager.
   * 
   * @param objectIdSupplier  the supplier of object identifiers, not null
   * @param changeManager  the change manager, not null
   */
  public InMemoryHistoricalTimeSeriesMaster(final Supplier<ObjectId> objectIdSupplier, final ChangeManager changeManager) {
    ArgumentChecker.notNull(objectIdSupplier, "objectIdSupplier");
    ArgumentChecker.notNull(changeManager, "changeManager");
    _objectIdSupplier = objectIdSupplier;
    _changeManager = changeManager;
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeriesInfoMetaDataResult metaData(HistoricalTimeSeriesInfoMetaDataRequest request) {
    ArgumentChecker.notNull(request, "request");
    HistoricalTimeSeriesInfoMetaDataResult result = new HistoricalTimeSeriesInfoMetaDataResult();
    if (request.isDataFields()) {
      Set<String> types = new HashSet<String>();
      for (HistoricalTimeSeriesInfoDocument doc : _storeInfo.values()) {
        types.add(doc.getInfo().getDataField());
      }
      result.getDataFields().addAll(types);
    }
    if (request.isDataSources()) {
      Set<String> types = new HashSet<String>();
      for (HistoricalTimeSeriesInfoDocument doc : _storeInfo.values()) {
        types.add(doc.getInfo().getDataSource());
      }
      result.getDataSources().addAll(types);
    }
    if (request.isDataProviders()) {
      Set<String> types = new HashSet<String>();
      for (HistoricalTimeSeriesInfoDocument doc : _storeInfo.values()) {
        types.add(doc.getInfo().getDataProvider());
      }
      result.getDataProviders().addAll(types);
    }
    if (request.isObservationTimes()) {
      Set<String> types = new HashSet<String>();
      for (HistoricalTimeSeriesInfoDocument doc : _storeInfo.values()) {
        types.add(doc.getInfo().getObservationTime());
      }
      result.getObservationTimes().addAll(types);
    }
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeriesInfoSearchResult search(HistoricalTimeSeriesInfoSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    final List<HistoricalTimeSeriesInfoDocument> list = new ArrayList<HistoricalTimeSeriesInfoDocument>();
    for (HistoricalTimeSeriesInfoDocument doc : _storeInfo.values()) {
      if (request.matches(doc)) {
        list.add(doc);
      }
    }
    HistoricalTimeSeriesInfoSearchResult result = new HistoricalTimeSeriesInfoSearchResult();
    result.setPaging(Paging.of(request.getPagingRequest(), list));
    result.getDocuments().addAll(request.getPagingRequest().select(list));
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeriesInfoDocument get(final UniqueId uniqueId) {
    return get(uniqueId, VersionCorrection.LATEST);
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeriesInfoDocument get(final ObjectIdentifiable objectKey, VersionCorrection versionCorrection) {
    validateId(objectKey);
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    final ObjectId objectId = objectKey.getObjectId();
    final HistoricalTimeSeriesInfoDocument document = _storeInfo.get(objectId);
    if (document == null) {
      throw new DataNotFoundException("Historical time-series not found: " + objectId);
    }
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeriesInfoDocument add(final HistoricalTimeSeriesInfoDocument document) {
    validateDocument(document);
    
    final ObjectId objectId = _objectIdSupplier.get();
    final UniqueId uniqueId = objectId.atVersion("");
    final HistoricalTimeSeriesInfoDocument cloned = JodaBeanUtils.clone(document);
    final ManageableHistoricalTimeSeriesInfo info = cloned.getInfo();
    info.setUniqueId(uniqueId);
    final Instant now = Instant.now();
    cloned.setVersionFromInstant(now);
    cloned.setCorrectionFromInstant(now);
    cloned.getInfo().setTimeSeriesObjectId(objectId);
    _storeInfo.put(objectId, cloned);
    _changeManager.entityChanged(ChangeType.ADDED, null, uniqueId, now);
    return cloned;
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeriesInfoDocument update(final HistoricalTimeSeriesInfoDocument document) {
    validateDocument(document);
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    
    final UniqueId uniqueId = document.getUniqueId();
    final Instant now = Instant.now();
    final HistoricalTimeSeriesInfoDocument storedDocument = _storeInfo.get(uniqueId.getObjectId());
    if (storedDocument == null) {
      throw new DataNotFoundException("Historical time-series not found: " + uniqueId);
    }
    document.setVersionFromInstant(now);
    document.setVersionToInstant(null);
    document.setCorrectionFromInstant(now);
    document.setCorrectionToInstant(null);
    if (_storeInfo.replace(uniqueId.getObjectId(), storedDocument, document) == false) {
      throw new IllegalArgumentException("Concurrent modification");
    }
    _changeManager.entityChanged(ChangeType.UPDATED, uniqueId, document.getUniqueId(), now);
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  public void remove(final UniqueId uniqueId) {
    validateId(uniqueId);
    if (_storeInfo.remove(uniqueId.getObjectId()) == null) {
      throw new DataNotFoundException("Historical time-series not found: " + uniqueId);
    }
    _changeManager.entityChanged(ChangeType.REMOVED, uniqueId, null, Instant.now());
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeriesInfoDocument correct(final HistoricalTimeSeriesInfoDocument document) {
    return update(document);
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeriesInfoHistoryResult history(HistoricalTimeSeriesInfoHistoryRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getObjectId(), "request.objectId");
    
    final HistoricalTimeSeriesInfoHistoryResult result = new HistoricalTimeSeriesInfoHistoryResult();
    final HistoricalTimeSeriesInfoDocument doc = get(request.getObjectId(), VersionCorrection.LATEST);
    if (doc != null) {
      result.getDocuments().add(doc);
    }
    result.setPaging(Paging.ofAll(result.getDocuments()));
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public ManageableHistoricalTimeSeries getTimeSeries(UniqueId uniqueId, LocalDate fromDateInclusive, LocalDate toDateInclusive) {
    return getTimeSeries(uniqueId.getObjectId(), VersionCorrection.LATEST, fromDateInclusive, toDateInclusive);
  }

  //-------------------------------------------------------------------------
  @Override
  public ManageableHistoricalTimeSeries getTimeSeries(ObjectIdentifiable objectKey, VersionCorrection versionCorrection, LocalDate fromDateInclusive, LocalDate toDateInclusive) {
    validateId(objectKey);
    fromDateInclusive = Objects.firstNonNull(fromDateInclusive, LocalDate.of(1000, 1, 1));  // TODO: JSR-310 min/max date
    toDateInclusive = Objects.firstNonNull(toDateInclusive, LocalDate.of(9999, 1, 1));
    ArgumentChecker.inOrderOrEqual(fromDateInclusive, toDateInclusive, "fromDateInclusive", "toDateInclusive");
    final ObjectId objectId = objectKey.getObjectId();
    
    final Instant now = Instant.now();
    LocalDateDoubleTimeSeries existingSeries = _storePoints.get(objectId);
    if (existingSeries == null) {
      if (_storeInfo.get(objectId) == null) {
        throw new DataNotFoundException("Historical time-series not found: " + objectId);
      }
      existingSeries = new ArrayLocalDateDoubleTimeSeries();
    }
    final LocalDateDoubleTimeSeries subSeries = existingSeries.subSeries(fromDateInclusive, toDateInclusive).toLocalDateDoubleTimeSeries();
    final ManageableHistoricalTimeSeries result = new ManageableHistoricalTimeSeries();
    result.setUniqueId(objectId.atLatestVersion());
    result.setTimeSeries(subSeries);
//    result.setEarliestDate(subSeries.getEarliestTime());
//    result.setLatestDate(subSeries.getLatestTime());
//    result.setEarliestValue(subSeries.getEarliestValue());
//    result.setLatestValue(subSeries.getLatestValue());
    result.setVersionInstant(now);
    result.setCorrectionInstant(now);
    return result;
  }


  @Override
  public HistoricalTimeSeriesSummary getSummary(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    return getSummary(uniqueId.getObjectId(), VersionCorrection.LATEST);
  }
  
  public HistoricalTimeSeriesSummary getSummary(ObjectIdentifiable objectKey, VersionCorrection versionCorrection) {
    validateId(objectKey);
    final ObjectId objectId = objectKey.getObjectId();
    LocalDateDoubleTimeSeries series = _storePoints.get(objectId);
    if (series == null) {
      if (_storeInfo.get(objectId) == null) {
        throw new DataNotFoundException("Historical time-series not found: " + objectId);
      }
      series = new ArrayLocalDateDoubleTimeSeries();
    }
    
    HistoricalTimeSeriesSummary result = new HistoricalTimeSeriesSummary();  
    try {
      result.setEarliestDate(series.getEarliestTime());
      result.setEarliestValue(series.getEarliestValue());
      result.setLatestDate(series.getLatestTime());
      result.setLatestValue(series.getLatestValue());   
    } catch (NoSuchElementException e) {
      throw new OpenGammaRuntimeException("Could not get earliest/latest data points for time series" + objectId);
    }
    return result;
  }

//  @Override
//  public ManageableHistoricalTimeSeries getTimeSeriesWithoutDataPoints(UniqueId uniqueId, LocalDate fromDateInclusive, LocalDate toDateInclusive) {
//    return getTimeSeriesWithoutDataPoints(uniqueId.getObjectId(), VersionCorrection.LATEST, fromDateInclusive, toDateInclusive);
//  }
//
//  @Override
//  public ManageableHistoricalTimeSeries getTimeSeriesWithoutDataPoints(ObjectIdentifiable objectKey, VersionCorrection versionCorrection, LocalDate fromDateInclusive, LocalDate toDateInclusive) {
//    // This actually fetches the data points anyway, as the cost is minimal with the in-memory implementation
//    return getTimeSeries(objectKey, versionCorrection, fromDateInclusive, toDateInclusive);
//  }
//
//  @Override
//  public LocalDateDoubleTimeSeries getTimeSeriesDataPoints(UniqueId uniqueId, LocalDate fromDateInclusive, LocalDate toDateInclusive) {
//    return getTimeSeriesDataPoints(uniqueId.getObjectId(), VersionCorrection.LATEST, fromDateInclusive, toDateInclusive);
//  }
//
//  @Override
//  public LocalDateDoubleTimeSeries getTimeSeriesDataPoints(ObjectIdentifiable objectKey, VersionCorrection versionCorrection, LocalDate fromDateInclusive, LocalDate toDateInclusive) {
//    validateId(objectKey);
//    fromDateInclusive = Objects.firstNonNull(fromDateInclusive, LocalDate.of(1000, 1, 1));  // TODO: JSR-310 min/max date
//    toDateInclusive = Objects.firstNonNull(toDateInclusive, LocalDate.of(9999, 1, 1));
//    ArgumentChecker.inOrderOrEqual(fromDateInclusive, toDateInclusive, "fromDateInclusive", "toDateInclusive");
//    final ObjectId objectId = objectKey.getObjectId();
//    
//    final Instant now = Instant.now();
//    LocalDateDoubleTimeSeries existingSeries = _storePoints.get(objectId);
//    if (existingSeries == null) {
//      if (_storeInfo.get(objectId) == null) {
//        throw new DataNotFoundException("Historical time-series not found: " + objectId);
//      }
//      existingSeries = new ArrayLocalDateDoubleTimeSeries();
//    }
//    return existingSeries.subSeries(fromDateInclusive, toDateInclusive).toLocalDateDoubleTimeSeries();
//  }
//  
//  @Override
//  public Double getLatestValue(UniqueId uniqueId) {
//    return null; //getTimeSeries(uniqueId, null, LocalDate.now()).getLatestValue();
//  }
//  
//  @Override
//  public Double getEarliestValue(UniqueId uniqueId) {
//    return null; //getTimeSeries(uniqueId, null, LocalDate.now()).getEarliestValue();
//  }
//
//  @Override
//  public LocalDate getLatestDate(UniqueId uniqueId) {
//    return null; //getTimeSeries(uniqueId, null, LocalDate.now()).getLatestDate();
//  }
//
//  @Override
//  public LocalDate getEarliestDate(UniqueId uniqueId) {
//    return null; //getTimeSeries(uniqueId, null, LocalDate.now()).getEarliestDate();
//  }

  //-------------------------------------------------------------------------
  @Override
  public UniqueId updateTimeSeriesDataPoints(ObjectIdentifiable objectKey, LocalDateDoubleTimeSeries series) {
    ArgumentChecker.notNull(objectKey, "objectKey");
    ArgumentChecker.notNull(series, "series");
    final ObjectId objectId = objectKey.getObjectId();
    
    final LocalDateDoubleTimeSeries existingSeries = _storePoints.get(objectId);
    if (existingSeries != null) {
      if (series.getEarliestTime().isBefore(existingSeries.getLatestTime())) {
        throw new IllegalArgumentException("Unable to add time-series as dates overlap");
      }
      LocalDateDoubleTimeSeries newSeries = existingSeries.noIntersectionOperation(series).toLocalDateDoubleTimeSeries();
      if (_storePoints.replace(objectId, existingSeries, newSeries) == false) {
        throw new IllegalArgumentException("Concurrent modification");
      }
    } else {
      if (_storePoints.putIfAbsent(objectId, series) != null) {
        throw new IllegalArgumentException("Concurrent modification");
      }
    }
    final Instant now = Instant.now();
    final UniqueId uniqueId = objectId.atLatestVersion();
    changeManager().entityChanged(ChangeType.UPDATED, uniqueId, uniqueId, now);
    return uniqueId;
  }

  //-------------------------------------------------------------------------
  @Override
  public UniqueId correctTimeSeriesDataPoints(ObjectIdentifiable objectKey, LocalDateDoubleTimeSeries series) {
    ArgumentChecker.notNull(objectKey, "objectKey");
    ArgumentChecker.notNull(series, "series");
    final ObjectId objectId = objectKey.getObjectId();
    
    LocalDateDoubleTimeSeries existingSeries = _storePoints.get(objectId);
    if (existingSeries != null) {
      LocalDateDoubleTimeSeries newSeries = existingSeries.unionOperate(series, DoubleTimeSeriesOperators.SECOND_OPERATOR).toLocalDateDoubleTimeSeries();
      if (_storePoints.replace(objectId, existingSeries, newSeries) == false) {
        throw new IllegalArgumentException("Concurrent modification");
      }
    } else {
      if (_storePoints.putIfAbsent(objectId, series) != null) {
        throw new IllegalArgumentException("Concurrent modification");
      }
    }
    final Instant now = Instant.now();
    final UniqueId uniqueId = objectId.atLatestVersion();
    changeManager().entityChanged(ChangeType.CORRECTED, uniqueId, uniqueId, now);
    return uniqueId;
  }

  //-------------------------------------------------------------------------
  @Override
  public UniqueId removeTimeSeriesDataPoints(ObjectIdentifiable objectKey, LocalDate fromDateInclusive, LocalDate toDateInclusive) {
    ArgumentChecker.notNull(objectKey, "objectKey");
    fromDateInclusive = Objects.firstNonNull(fromDateInclusive, LocalDate.of(1000, 1, 1));  // TODO: JSR-310 min/max date
    toDateInclusive = Objects.firstNonNull(toDateInclusive, LocalDate.of(9999, 1, 1));
    ArgumentChecker.inOrderOrEqual(fromDateInclusive, toDateInclusive, "fromDateInclusive", "toDateInclusive");
    final ObjectId objectId = objectKey.getObjectId();
    
    LocalDateDoubleTimeSeries existingSeries = _storePoints.get(objectId);
    if (existingSeries == null) {
      return objectId.atLatestVersion();
    }
    MutableLocalDateDoubleTimeSeries mutableTS = existingSeries.toMutableLocalDateDoubleTimeSeries();
    for (Iterator<LocalDate> it = mutableTS.timeIterator(); it.hasNext(); ) {
      LocalDate date = it.next();
      if (date.isBefore(fromDateInclusive) == false && date.isAfter(toDateInclusive) == false) {
        it.remove();
      }
    }
    if (_storePoints.replace(objectId, existingSeries, mutableTS.toLocalDateDoubleTimeSeries()) == false) {
      throw new IllegalArgumentException("Concurrent modification");
    }
    return objectId.atLatestVersion();
  }

  //-------------------------------------------------------------------------
  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

  //-------------------------------------------------------------------------
  private long validateId(ObjectIdentifiable objectId) {
    ArgumentChecker.notNull(objectId, "objectId");
    try {
      return Long.parseLong(objectId.getObjectId().getValue());
    } catch (NumberFormatException ex) {
      throw new IllegalArgumentException("Invalid objectId " + objectId);
    }
  }

  private void validateDocument(HistoricalTimeSeriesInfoDocument document) {
    ArgumentChecker.notNull(document, "document");
    if (document.getUniqueId() != null) {
      validateId(document.getUniqueId());
    }
    ArgumentChecker.notNull(document.getInfo(), "document.series");
    ArgumentChecker.notNull(document.getInfo().getExternalIdBundle(), "document.series.identifiers");
    ArgumentChecker.isTrue(document.getInfo().getExternalIdBundle().toBundle().getExternalIds().size() > 0, "document.series.identifiers must not be empty");
    ArgumentChecker.isTrue(StringUtils.isNotBlank(document.getInfo().getDataSource()), "document.series.dataSource must not be blank");
    ArgumentChecker.isTrue(StringUtils.isNotBlank(document.getInfo().getDataProvider()), "document.series.dataProvider must not be blank");
    ArgumentChecker.isTrue(StringUtils.isNotBlank(document.getInfo().getDataField()), "document.series.dataField must not be blank");
    ArgumentChecker.isTrue(StringUtils.isNotBlank(document.getInfo().getObservationTime()), "document.series.observationTime must not be blank");
  }

}
