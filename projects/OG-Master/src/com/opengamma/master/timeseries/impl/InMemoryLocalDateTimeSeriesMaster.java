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
import com.opengamma.master.timeseries.TimeSeriesMaster;
import com.opengamma.master.timeseries.TimeSeriesSearchHistoricRequest;
import com.opengamma.master.timeseries.TimeSeriesSearchHistoricResult;
import com.opengamma.master.timeseries.TimeSeriesSearchRequest;
import com.opengamma.master.timeseries.TimeSeriesSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.Paging;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.MapLocalDateDoubleTimeSeries;
import com.opengamma.util.tuple.Pair;

/**
 * An in-memory implementation of a time-series master.
 */
public class InMemoryLocalDateTimeSeriesMaster implements TimeSeriesMaster<LocalDate> {

  /**
   * A cache of LocalDate time-series by identifier.
   */
  private final ConcurrentHashMap<UniqueIdentifier, TimeSeriesDocument<LocalDate>> _timeseriesDb = new ConcurrentHashMap<UniqueIdentifier, TimeSeriesDocument<LocalDate>>();
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
  public InMemoryLocalDateTimeSeriesMaster() {
    this(new UniqueIdentifierSupplier(DEFAULT_UID_SCHEME));
  }

  /**
   * Creates an instance specifying the supplier of unique identifiers.
   * 
   * @param uniqueIdSupplier  the supplier of unique identifiers, not null
   */
  private InMemoryLocalDateTimeSeriesMaster(final Supplier<UniqueIdentifier> uniqueIdSupplier) {
    ArgumentChecker.notNull(uniqueIdSupplier, "uniqueIdSupplier");
    _uniqueIdSupplier = uniqueIdSupplier;
  }

  //-------------------------------------------------------------------------
  @Override
  public List<IdentifierBundleWithDates> getAllIdentifiers() {
    List<IdentifierBundleWithDates> result = new ArrayList<IdentifierBundleWithDates>();
    Collection<TimeSeriesDocument<LocalDate>> docs = _timeseriesDb.values();
    for (TimeSeriesDocument<LocalDate> tsDoc : docs) {
      result.add(tsDoc.getIdentifiers());
    }
    return result;
  }

  @Override
  public TimeSeriesSearchResult<LocalDate> search(final TimeSeriesSearchRequest<LocalDate> request) {
    ArgumentChecker.notNull(request, "request");
    List<TimeSeriesDocument<LocalDate>> list = new ArrayList<TimeSeriesDocument<LocalDate>>();
    for (TimeSeriesDocument<LocalDate> doc : _timeseriesDb.values()) {
      if (request.matches(doc)) {
        list.add(doc);
      }
    }
    
    if (request.isLoadDates()) {
      for (TimeSeriesDocument<LocalDate> tsDocument : list) {
        assert tsDocument.getTimeSeries() != null;
        tsDocument.setLatest(tsDocument.getTimeSeries().getLatestTime());
        tsDocument.setEarliest(tsDocument.getTimeSeries().getEarliestTime());
      }
    }
    if (!request.isLoadTimeSeries()) {
      List<TimeSeriesDocument<LocalDate>> noSeries = new ArrayList<TimeSeriesDocument<LocalDate>>();
      for (TimeSeriesDocument<LocalDate> tsDocument : list) {
        TimeSeriesDocument<LocalDate> doc = new TimeSeriesDocument<LocalDate>();
        doc.setDataField(tsDocument.getDataField());
        doc.setDataProvider(tsDocument.getDataProvider());
        doc.setDataSource(tsDocument.getDataSource());
        doc.setEarliest(tsDocument.getEarliest());
        doc.setIdentifiers(tsDocument.getIdentifiers());
        doc.setLatest(tsDocument.getLatest());
        doc.setObservationTime(tsDocument.getObservationTime());
        doc.setUniqueId(tsDocument.getUniqueId());
        noSeries.add(doc);
      }
      list = noSeries;
    }
    if (request.getStart() != null && request.getEnd() != null) {
      List<TimeSeriesDocument<LocalDate>> subseriesList = new ArrayList<TimeSeriesDocument<LocalDate>>();
      for (TimeSeriesDocument<LocalDate> tsDocument : list) {
        TimeSeriesDocument<LocalDate> doc = new TimeSeriesDocument<LocalDate>();
        doc.setDataField(tsDocument.getDataField());
        doc.setDataProvider(tsDocument.getDataProvider());
        doc.setDataSource(tsDocument.getDataSource());
        doc.setEarliest(tsDocument.getEarliest());
        doc.setIdentifiers(tsDocument.getIdentifiers());
        doc.setLatest(tsDocument.getLatest());
        doc.setObservationTime(tsDocument.getObservationTime());
        doc.setUniqueId(tsDocument.getUniqueId());
        DoubleTimeSeries<LocalDate> subseries = tsDocument.getTimeSeries().subSeries(request.getStart(), true, request.getEnd(), false);
        doc.setTimeSeries(subseries);
        subseriesList.add(doc);
      }
      list = subseriesList;
    }
    
    final TimeSeriesSearchResult<LocalDate> result = new TimeSeriesSearchResult<LocalDate>();
    result.setPaging(Paging.of(request.getPagingRequest(), list));
    result.getDocuments().addAll(request.getPagingRequest().select(list));
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public TimeSeriesDocument<LocalDate> get(UniqueIdentifier uniqueId) {
    validateUid(uniqueId);
    final TimeSeriesDocument<LocalDate> document = _timeseriesDb.get(uniqueId);
    if (document == null) {
      throw new DataNotFoundException("Time-series not found: " + uniqueId);
    }
    return document;
  }
  
  private void validateUid(UniqueIdentifier uniqueId) {
    ArgumentChecker.notNull(uniqueId, "TimeSeries UID");
    ArgumentChecker.isTrue(uniqueId.getScheme().equals(DEFAULT_UID_SCHEME), "UID not " + DEFAULT_UID_SCHEME);
    ArgumentChecker.isTrue(uniqueId.getValue() != null, "Uid value cannot be null");
    
    try {
      Long.parseLong(uniqueId.getValue());
    } catch (NumberFormatException ex) {
      throw new IllegalArgumentException("Invalid UID " + uniqueId);
    }
  }

  @Override
  public TimeSeriesDocument<LocalDate> add(TimeSeriesDocument<LocalDate> document) {
    validateTimeSeriesDocument(document);
    if (!contains(document)) {
      final UniqueIdentifier uniqueId = _uniqueIdSupplier.get();
      final TimeSeriesDocument<LocalDate> doc = new TimeSeriesDocument<LocalDate>();
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
      throw new IllegalArgumentException("cannot add duplicate TimeSeries for identifiers " + document.getIdentifiers());
    }
  }

  private boolean contains(TimeSeriesDocument<LocalDate> document) {
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
  public TimeSeriesDocument<LocalDate> update(TimeSeriesDocument<LocalDate> document) {
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    validateTimeSeriesDocument(document);
    
    final UniqueIdentifier uniqueId = document.getUniqueId();
    final TimeSeriesDocument<LocalDate> storedDocument = _timeseriesDb.get(uniqueId);
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
  public TimeSeriesSearchHistoricResult<LocalDate> searchHistoric(final TimeSeriesSearchHistoricRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getTimeSeriesId(), "request.timeSeriesId");
    
    final TimeSeriesSearchHistoricResult<LocalDate> result = new TimeSeriesSearchHistoricResult<LocalDate>();
    TimeSeriesDocument<LocalDate> doc = get(request.getTimeSeriesId());
    if (doc != null) {
      result.getDocuments().add(doc);
    }
    result.setPaging(Paging.of(result.getDocuments()));
    return result;
  }
  
  @Override
  public DataPointDocument<LocalDate> updateDataPoint(DataPointDocument<LocalDate> document) {
    ArgumentChecker.notNull(document, "dataPoint document");
    ArgumentChecker.notNull(document.getDate(), "data point date");
    ArgumentChecker.notNull(document.getValue(), "data point value");
    
    UniqueIdentifier timeSeriesId = document.getTimeSeriesId();
    validateUid(timeSeriesId);
    
    TimeSeriesDocument<LocalDate> storeDoc = _timeseriesDb.get(timeSeriesId);
    DoubleTimeSeries<LocalDate> timeSeries = storeDoc.getTimeSeries();
    MapLocalDateDoubleTimeSeries mutableTS = new MapLocalDateDoubleTimeSeries(timeSeries);
    mutableTS.putDataPoint(document.getDate(), document.getValue());
    storeDoc.setTimeSeries(mutableTS);
    return document;
  }

  @Override
  public DataPointDocument<LocalDate> addDataPoint(DataPointDocument<LocalDate> document) {
    ArgumentChecker.notNull(document, "dataPoint document");
    ArgumentChecker.notNull(document.getDate(), "data point date");
    ArgumentChecker.notNull(document.getValue(), "data point value");
    UniqueIdentifier timeSeriesId = document.getTimeSeriesId();
    validateUid(timeSeriesId);
    
    TimeSeriesDocument<LocalDate> storedDoc = _timeseriesDb.get(timeSeriesId);
    MapLocalDateDoubleTimeSeries mutableTS = new MapLocalDateDoubleTimeSeries();
    mutableTS.putDataPoint(document.getDate(), document.getValue());
    DoubleTimeSeries<LocalDate> mergedTS = storedDoc.getTimeSeries().noIntersectionOperation(mutableTS);
    storedDoc.setTimeSeries(mergedTS);
    
    String uniqueId = new StringBuilder(timeSeriesId.getValue()).append("/").append(DateUtil.printYYYYMMDD(document.getDate())).toString();
    document.setDataPointId(UniqueIdentifier.of(DEFAULT_UID_SCHEME, uniqueId));
    return document;
    
  }
  
  @Override
  public DataPointDocument<LocalDate> getDataPoint(UniqueIdentifier uniqueId) {
    Pair<Long, LocalDate> uniqueIdPair = validateAndGetDataPointId(uniqueId);
    
    Long tsId = uniqueIdPair.getFirst();
    LocalDate date = uniqueIdPair.getSecond();
    
    final DataPointDocument<LocalDate> result = new DataPointDocument<LocalDate>();
    result.setDate(uniqueIdPair.getSecond());
    result.setTimeSeriesId(UniqueIdentifier.of(DEFAULT_UID_SCHEME, String.valueOf(tsId)));
    result.setDataPointId(uniqueId);
    
    TimeSeriesDocument<LocalDate> storedDoc = _timeseriesDb.get(UniqueIdentifier.of(DEFAULT_UID_SCHEME, String.valueOf(tsId)));
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
    TimeSeriesDocument<LocalDate> storedDoc = _timeseriesDb.get(UniqueIdentifier.of(DEFAULT_UID_SCHEME, String.valueOf(tsId)));
    
    MapLocalDateDoubleTimeSeries mutableTS = new MapLocalDateDoubleTimeSeries(storedDoc.getTimeSeries());
    mutableTS.removeDataPoint(uniqueIdPair.getSecond());
    storedDoc.setTimeSeries(mutableTS);
  }

  

  @Override
  public void appendTimeSeries(TimeSeriesDocument<LocalDate> document) {
    validateTimeSeriesDocument(document);
    
    validateUid(document.getUniqueId());
    UniqueIdentifier uniqueId = document.getUniqueId();
    TimeSeriesDocument<LocalDate> storedDoc = _timeseriesDb.get(uniqueId);
    DoubleTimeSeries<LocalDate> mergedTS = storedDoc.getTimeSeries().noIntersectionOperation(document.getTimeSeries());
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
    
    for (Entry<UniqueIdentifier, TimeSeriesDocument<LocalDate>> entry : _timeseriesDb.entrySet()) {
      UniqueIdentifier uniqueId = entry.getKey();
      TimeSeriesDocument<LocalDate> tsDoc = entry.getValue();
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
    validateUid(timeSeriesUid);
    TimeSeriesDocument<LocalDate> storedDoc = _timeseriesDb.get(timeSeriesUid);
    DoubleTimeSeries<LocalDate> timeSeries = storedDoc.getTimeSeries();
    DoubleTimeSeries<LocalDate> subSeries = timeSeries.subSeries(firstDateToRetain, true, timeSeries.getLatestTime(), false);
    storedDoc.setTimeSeries(subSeries);
  }

  //-------------------------------------------------------------------------
  private void validateTimeSeriesDocument(TimeSeriesDocument<LocalDate> document) {
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
