/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.timeseries.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.time.calendar.LocalDate;
import javax.time.calendar.format.CalendricalParseException;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.MetaProperty;

import com.google.common.base.Supplier;
import com.opengamma.DataNotFoundException;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.IdentifierBundleWithDates;
import com.opengamma.id.IdentifierWithDates;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.UniqueIdentifierSupplier;
import com.opengamma.master.timeseries.DataPointDocument;
import com.opengamma.master.timeseries.TimeSeriesDocument;
import com.opengamma.master.timeseries.TimeSeriesGetRequest;
import com.opengamma.master.timeseries.TimeSeriesMaster;
import com.opengamma.master.timeseries.TimeSeriesSearchHistoricRequest;
import com.opengamma.master.timeseries.TimeSeriesSearchHistoricResult;
import com.opengamma.master.timeseries.TimeSeriesSearchRequest;
import com.opengamma.master.timeseries.TimeSeriesSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.Paging;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.MapLocalDateDoubleTimeSeries;
import com.opengamma.util.tuple.Pair;

/**
 * An in-memory implementation of a time-series master.
 */
public class InMemoryHistoricalTimeSeriesMaster implements TimeSeriesMaster {

  /**
   * A cache of LocalDate time-series by identifier.
   */
  private final ConcurrentHashMap<UniqueIdentifier, TimeSeriesDocument> _timeseriesDb = new ConcurrentHashMap<UniqueIdentifier, TimeSeriesDocument>();
  /**
   * The default scheme used for each {@link UniqueIdentifier}.
   */
  public static final String DEFAULT_UID_SCHEME = "TssMemory";
  /**
   * The supplied of identifiers.
   */
  private final Supplier<UniqueIdentifier> _uniqueIdSupplier;

  /**
   * Creates an empty time-series master using the default scheme for any {@link UniqueIdentifier}s created.
   */
  public InMemoryHistoricalTimeSeriesMaster() {
    this(new UniqueIdentifierSupplier(DEFAULT_UID_SCHEME));
  }

  /**
   * Creates an instance specifying the supplier of unique identifiers.
   * 
   * @param uniqueIdSupplier  the supplier of unique identifiers, not null
   */
  private InMemoryHistoricalTimeSeriesMaster(final Supplier<UniqueIdentifier> uniqueIdSupplier) {
    ArgumentChecker.notNull(uniqueIdSupplier, "uniqueIdSupplier");
    _uniqueIdSupplier = uniqueIdSupplier;
  }

  //-------------------------------------------------------------------------
  @Override
  public List<IdentifierBundleWithDates> getAllIdentifiers() {
    List<IdentifierBundleWithDates> result = new ArrayList<IdentifierBundleWithDates>();
    Collection<TimeSeriesDocument> docs = _timeseriesDb.values();
    for (TimeSeriesDocument tsDoc : docs) {
      result.add(tsDoc.getIdentifiers());
    }
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public TimeSeriesSearchResult search(final TimeSeriesSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    List<TimeSeriesDocument> list = new ArrayList<TimeSeriesDocument>();
    for (TimeSeriesDocument doc : _timeseriesDb.values()) {
      if (request.matches(doc)) {
        list.add(filter(
            doc, request.isLoadEarliestLatest(), request.isLoadTimeSeries(), request.getStart(), request.getEnd()));
      }
    }
    final TimeSeriesSearchResult result = new TimeSeriesSearchResult();
    result.setPaging(Paging.of(request.getPagingRequest(), list));
    result.getDocuments().addAll(request.getPagingRequest().select(list));
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public TimeSeriesDocument get(UniqueIdentifier uniqueId) {
    validateTimeSeriesId(uniqueId);
    final TimeSeriesDocument document = _timeseriesDb.get(uniqueId);
    if (document == null) {
      throw new DataNotFoundException("Time-series not found: " + uniqueId);
    }
    return document;
  }

  public TimeSeriesDocument get(TimeSeriesGetRequest request) {
    final TimeSeriesDocument document = _timeseriesDb.get(request.getUniqueId());
    return filter(document, request.isLoadEarliestLatest(), request.isLoadTimeSeries(), request.getStart(), request.getEnd());
  }

  private TimeSeriesDocument filter(
      TimeSeriesDocument original, boolean loadEarliestLatest, boolean loadTimeSeries, LocalDate start, LocalDate end) {
    TimeSeriesDocument copy = original;
    if (loadEarliestLatest) {
      copy = clone(original, copy);
      copy.setLatest(copy.getTimeSeries().getLatestTime());
      copy.setEarliest(copy.getTimeSeries().getEarliestTime());
    }
    if (loadTimeSeries) {
      if (start != null && end != null) {
        copy = clone(original, copy);
        LocalDateDoubleTimeSeries subseries = copy.getTimeSeries().subSeries(start, true, end, false).toLocalDateDoubleTimeSeries();
        copy.setTimeSeries(subseries);
      }
    } else {
      copy = clone(original, copy);
      copy.setTimeSeries(null);
    }
    return copy;
  }

  private TimeSeriesDocument clone(TimeSeriesDocument original, TimeSeriesDocument copy) {
    if (copy != original) {
      return copy;
    }
    copy = new TimeSeriesDocument();
    for (MetaProperty<Object> mp : original.metaBean().metaPropertyIterable()) {
      mp.set(copy, mp.get(original));
    }
    return copy;
  }

  //-------------------------------------------------------------------------
  @Override
  public TimeSeriesDocument add(TimeSeriesDocument document) {
    validateTimeSeriesDocument(document);
    if (!contains(document)) {
      final UniqueIdentifier uniqueId = _uniqueIdSupplier.get();
      final TimeSeriesDocument doc = new TimeSeriesDocument();
      doc.setUniqueId(uniqueId);
      doc.setDataField(document.getDataField());
      doc.setDataProvider(document.getDataProvider());
      doc.setDataSource(document.getDataSource());
      doc.setIdentifiers(document.getIdentifiers());
      doc.setObservationTime(document.getObservationTime());
      doc.setTimeSeries(document.getTimeSeries());
      _timeseriesDb.put(uniqueId, doc);  // unique identifier should be unique
      document.setUniqueId(uniqueId);
      return document;
    } else {
      throw new IllegalArgumentException("Cannot add duplicate time-series for identifiers " + document.getIdentifiers());
    }
  }

  private boolean contains(TimeSeriesDocument document) {
    for (IdentifierWithDates identifierWithDates : document.getIdentifiers()) {
      Identifier identifier = identifierWithDates.asIdentifier();
      UniqueIdentifier uniqueId = resolveIdentifier(
          IdentifierBundle.of(identifier), 
          identifierWithDates.getValidFrom(), 
          document.getDataSource(), 
          document.getDataProvider(), 
          document.getDataField());
      if (uniqueId != null) {
        return true;
      }
    }
    return false;
  }
  
  @Override
  public TimeSeriesDocument update(TimeSeriesDocument document) {
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    validateTimeSeriesDocument(document);
    
    final UniqueIdentifier uniqueId = document.getUniqueId();
    final TimeSeriesDocument storedDocument = _timeseriesDb.get(uniqueId);
    if (storedDocument == null) {
      throw new DataNotFoundException("Time-series not found: " + uniqueId);
    }
    if (_timeseriesDb.replace(uniqueId, storedDocument, document) == false) {
      throw new IllegalArgumentException("Concurrent modification");
    }
    return document;
  }
  
  @Override
  public void remove(UniqueIdentifier uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    if (_timeseriesDb.remove(uniqueId) == null) {
      throw new DataNotFoundException("Time-series not found: " + uniqueId);
    }
  }

  @Override
  public TimeSeriesSearchHistoricResult searchHistoric(final TimeSeriesSearchHistoricRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getTimeSeriesId(), "request.timeSeriesId");
    
    final TimeSeriesSearchHistoricResult result = new TimeSeriesSearchHistoricResult();
    TimeSeriesDocument doc = get(request.getTimeSeriesId());
    if (doc != null) {
      result.getDocuments().add(doc);
    }
    result.setPaging(Paging.of(result.getDocuments()));
    return result;
  }
  
  @Override
  public DataPointDocument updateDataPoint(DataPointDocument document) {
    ArgumentChecker.notNull(document, "dataPoint document");
    ArgumentChecker.notNull(document.getDate(), "data point date");
    ArgumentChecker.notNull(document.getValue(), "data point value");
    
    UniqueIdentifier timeSeriesId = document.getTimeSeriesId();
    validateTimeSeriesId(timeSeriesId);
    
    TimeSeriesDocument storeDoc = _timeseriesDb.get(timeSeriesId);
    LocalDateDoubleTimeSeries timeSeries = storeDoc.getTimeSeries();
    MapLocalDateDoubleTimeSeries mutableTS = new MapLocalDateDoubleTimeSeries(timeSeries);
    mutableTS.putDataPoint(document.getDate(), document.getValue());
    storeDoc.setTimeSeries(mutableTS);
    return document;
  }

  @Override
  public DataPointDocument addDataPoint(DataPointDocument document) {
    ArgumentChecker.notNull(document, "dataPoint document");
    ArgumentChecker.notNull(document.getDate(), "data point date");
    ArgumentChecker.notNull(document.getValue(), "data point value");
    UniqueIdentifier timeSeriesId = document.getTimeSeriesId();
    validateTimeSeriesId(timeSeriesId);
    
    TimeSeriesDocument storedDoc = _timeseriesDb.get(timeSeriesId);
    MapLocalDateDoubleTimeSeries mutableTS = new MapLocalDateDoubleTimeSeries();
    mutableTS.putDataPoint(document.getDate(), document.getValue());
    LocalDateDoubleTimeSeries mergedTS = storedDoc.getTimeSeries().noIntersectionOperation(mutableTS).toLocalDateDoubleTimeSeries();
    storedDoc.setTimeSeries(mergedTS);
    
    String uniqueId = new StringBuilder(timeSeriesId.getValue()).append("/").append(DateUtil.printYYYYMMDD(document.getDate())).toString();
    document.setDataPointId(UniqueIdentifier.of(DEFAULT_UID_SCHEME, uniqueId));
    return document;
    
  }
  
  @Override
  public DataPointDocument getDataPoint(UniqueIdentifier uniqueId) {
    Pair<Long, LocalDate> uniqueIdPair = validateAndGetDataPointId(uniqueId);
    
    Long tsId = uniqueIdPair.getFirst();
    LocalDate date = uniqueIdPair.getSecond();
    
    final DataPointDocument result = new DataPointDocument();
    result.setDate(uniqueIdPair.getSecond());
    result.setTimeSeriesId(UniqueIdentifier.of(DEFAULT_UID_SCHEME, String.valueOf(tsId)));
    result.setDataPointId(uniqueId);
    
    TimeSeriesDocument storedDoc = _timeseriesDb.get(UniqueIdentifier.of(DEFAULT_UID_SCHEME, String.valueOf(tsId)));
    Double value = storedDoc.getTimeSeries().getValue(date);
    result.setValue(value);
       
    return result;
  }
  
  private Pair<Long, LocalDate> validateAndGetDataPointId(UniqueIdentifier uniqueId) {
    ArgumentChecker.notNull(uniqueId, "DataPoint UID");
    ArgumentChecker.isTrue(uniqueId.getScheme().equals(DEFAULT_UID_SCHEME), "UID not TssMemory");
    ArgumentChecker.isTrue(uniqueId.getValue() != null, "Uid value cannot be null");
    String[] tokens = StringUtils.split(uniqueId.getValue(), '/');
    if (tokens.length != 2) {
      throw new IllegalArgumentException("UID not expected format<12345/date> " + uniqueId);
    }
    String id = tokens[0];
    String dateStr = tokens[1];
    LocalDate date = null;
    Long tsId = Long.MIN_VALUE;
    if (id != null && dateStr != null) {
      try {
        date = DateUtil.toLocalDate(dateStr);
      } catch (CalendricalParseException ex) {
        throw new IllegalArgumentException("UID not expected format<12345/date> " + uniqueId, ex);
      }
      try {
        tsId = Long.parseLong(id);
      } catch (NumberFormatException ex) {
        throw new IllegalArgumentException("UID not expected format<12345/date> " + uniqueId, ex);
      }
    } else {
      throw new IllegalArgumentException("UID not expected format<12345/date> " + uniqueId);
    }
    return Pair.of(tsId, date);
  }

  @Override
  public void removeDataPoint(UniqueIdentifier uniqueId) {
    Pair<Long, LocalDate> uniqueIdPair = validateAndGetDataPointId(uniqueId);
    
    Long tsId = uniqueIdPair.getFirst();
    TimeSeriesDocument storedDoc = _timeseriesDb.get(UniqueIdentifier.of(DEFAULT_UID_SCHEME, String.valueOf(tsId)));
    
    MapLocalDateDoubleTimeSeries mutableTS = new MapLocalDateDoubleTimeSeries(storedDoc.getTimeSeries());
    mutableTS.removeDataPoint(uniqueIdPair.getSecond());
    storedDoc.setTimeSeries(mutableTS);
  }

  @Override
  public void appendTimeSeries(TimeSeriesDocument document) {
    validateTimeSeriesDocument(document);
    
    validateTimeSeriesId(document.getUniqueId());
    UniqueIdentifier uniqueId = document.getUniqueId();
    TimeSeriesDocument storedDoc = _timeseriesDb.get(uniqueId);
    LocalDateDoubleTimeSeries mergedTS = storedDoc.getTimeSeries().noIntersectionOperation(document.getTimeSeries()).toLocalDateDoubleTimeSeries();
    storedDoc.setTimeSeries(mergedTS);
  }

  @Override
  public UniqueIdentifier resolveIdentifier(IdentifierBundle securityBundle, String dataSource, String dataProvider, String dataField) {
    return resolveIdentifier(securityBundle, (LocalDate) null, dataSource, dataProvider, dataField);
  }

  @Override
  public UniqueIdentifier resolveIdentifier(IdentifierBundle securityKey, LocalDate currentDate, String dataSource, String dataProvider, String dataField) {
    ArgumentChecker.notNull(securityKey, "securityBundle");
    ArgumentChecker.notNull(dataSource, "dataSource");
    ArgumentChecker.notNull(dataProvider, "dataProvider");
    ArgumentChecker.notNull(dataField, "dataField");
    
    for (Entry<UniqueIdentifier, TimeSeriesDocument> entry : _timeseriesDb.entrySet()) {
      UniqueIdentifier uniqueId = entry.getKey();
      TimeSeriesDocument tsDoc = entry.getValue();
      if (tsDoc.getDataSource().equals(dataSource) && tsDoc.getDataProvider().equals(dataProvider) && tsDoc.getDataField().equals(dataField)) {
        for (IdentifierWithDates idWithDates : tsDoc.getIdentifiers()) {
          if (securityKey.contains(idWithDates.asIdentifier())) {
            LocalDate validFrom = idWithDates.getValidFrom();
            LocalDate validTo = idWithDates.getValidTo();
            if (currentDate != null) {
              if (currentDate.equals(validFrom) || (currentDate.isAfter(validFrom) && currentDate.isBefore(validTo)) || currentDate.equals(validTo)) {
                return uniqueId;
              }
            } else {
              return uniqueId;
            }
          }
        }
      }
    }
    return null;
  }

  @Override
  public void removeDataPoints(UniqueIdentifier timeSeriesUid, LocalDate firstDateToRetain) {
    validateTimeSeriesId(timeSeriesUid);
    TimeSeriesDocument storedDoc = _timeseriesDb.get(timeSeriesUid);
    LocalDateDoubleTimeSeries timeSeries = storedDoc.getTimeSeries();
    LocalDateDoubleTimeSeries subSeries = timeSeries.subSeries(firstDateToRetain, true, timeSeries.getLatestTime(), false).toLocalDateDoubleTimeSeries();
    storedDoc.setTimeSeries(subSeries);
  }

  //-------------------------------------------------------------------------
  private long validateTimeSeriesId(UniqueIdentifier uniqueId) {
    ArgumentChecker.notNull(uniqueId, "timeSeriesId");
    ArgumentChecker.isTrue(uniqueId.getScheme().equals(DEFAULT_UID_SCHEME), "timeSeriesId scheme invalid");
    try {
      return Long.parseLong(uniqueId.getValue());
    } catch (NumberFormatException ex) {
      throw new IllegalArgumentException("Invalid uniqueId " + uniqueId);
    }
  }

  private void validateTimeSeriesDocument(TimeSeriesDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getTimeSeries(), "document.timeSeries");
    ArgumentChecker.notNull(document.getIdentifiers(), "document.identifiers");
    ArgumentChecker.isTrue(document.getIdentifiers().asIdentifierBundle().getIdentifiers().size() > 0, "document.identifiers must not be empty");
    ArgumentChecker.isTrue(StringUtils.isNotBlank(document.getDataSource()), "document.dataSource must not be blank");
    ArgumentChecker.isTrue(StringUtils.isNotBlank(document.getDataProvider()), "document.dataProvider must not be blank");
    ArgumentChecker.isTrue(StringUtils.isNotBlank(document.getDataField()), "document.dataField must not be blank");
    ArgumentChecker.isTrue(StringUtils.isNotBlank(document.getObservationTime()), "document.observationTime must not be blank");
  }

}
