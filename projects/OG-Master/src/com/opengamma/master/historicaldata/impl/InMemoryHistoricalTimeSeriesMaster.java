/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaldata.impl;

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
import com.opengamma.master.historicaldata.DataPointDocument;
import com.opengamma.master.historicaldata.HistoricalTimeSeriesDocument;
import com.opengamma.master.historicaldata.HistoricalTimeSeriesGetRequest;
import com.opengamma.master.historicaldata.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaldata.HistoricalTimeSeriesSearchHistoricRequest;
import com.opengamma.master.historicaldata.HistoricalTimeSeriesSearchHistoricResult;
import com.opengamma.master.historicaldata.HistoricalTimeSeriesSearchRequest;
import com.opengamma.master.historicaldata.HistoricalTimeSeriesSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.Paging;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.MapLocalDateDoubleTimeSeries;
import com.opengamma.util.tuple.Pair;

/**
 * An in-memory implementation of a historical time-series master.
 */
public class InMemoryHistoricalTimeSeriesMaster implements HistoricalTimeSeriesMaster {

  /**
   * The default scheme used for each {@link UniqueIdentifier}.
   */
  public static final String DEFAULT_UID_SCHEME = "TssMemory";

  /**
   * The store of historical time-series.
   */
  private final ConcurrentHashMap<UniqueIdentifier, HistoricalTimeSeriesDocument> _store = new ConcurrentHashMap<UniqueIdentifier, HistoricalTimeSeriesDocument>();
  /**
   * The supplied of identifiers.
   */
  private final Supplier<UniqueIdentifier> _uniqueIdSupplier;

  /**
   * Creates an instance using the default scheme for any {@code UniqueIdentifier} created.
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
    Collection<HistoricalTimeSeriesDocument> docs = _store.values();
    for (HistoricalTimeSeriesDocument tsDoc : docs) {
      result.add(tsDoc.getIdentifiers());
    }
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeriesSearchResult search(final HistoricalTimeSeriesSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    List<HistoricalTimeSeriesDocument> list = new ArrayList<HistoricalTimeSeriesDocument>();
    for (HistoricalTimeSeriesDocument doc : _store.values()) {
      if (request.matches(doc)) {
        list.add(filter(
            doc, request.isLoadEarliestLatest(), request.isLoadTimeSeries(), request.getStart(), request.getEnd()));
      }
    }
    final HistoricalTimeSeriesSearchResult result = new HistoricalTimeSeriesSearchResult();
    result.setPaging(Paging.of(request.getPagingRequest(), list));
    result.getDocuments().addAll(request.getPagingRequest().select(list));
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeriesDocument get(UniqueIdentifier uniqueId) {
    validateId(uniqueId);
    final HistoricalTimeSeriesDocument document = _store.get(uniqueId);
    if (document == null) {
      throw new DataNotFoundException("Historical time-series not found: " + uniqueId);
    }
    return document;
  }

  public HistoricalTimeSeriesDocument get(HistoricalTimeSeriesGetRequest request) {
    final HistoricalTimeSeriesDocument document = _store.get(request.getUniqueId());
    return filter(document, request.isLoadEarliestLatest(), request.isLoadTimeSeries(), request.getStart(), request.getEnd());
  }

  private HistoricalTimeSeriesDocument filter(
      HistoricalTimeSeriesDocument original, boolean loadEarliestLatest, boolean loadTimeSeries, LocalDate start, LocalDate end) {
    HistoricalTimeSeriesDocument copy = original;
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

  private HistoricalTimeSeriesDocument clone(HistoricalTimeSeriesDocument original, HistoricalTimeSeriesDocument copy) {
    if (copy != original) {
      return copy;
    }
    copy = new HistoricalTimeSeriesDocument();
    for (MetaProperty<Object> mp : original.metaBean().metaPropertyIterable()) {
      mp.set(copy, mp.get(original));
    }
    return copy;
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeriesDocument add(HistoricalTimeSeriesDocument document) {
    validateDocument(document);
    if (!contains(document)) {
      final UniqueIdentifier uniqueId = _uniqueIdSupplier.get();
      final HistoricalTimeSeriesDocument doc = new HistoricalTimeSeriesDocument();
      doc.setUniqueId(uniqueId);
      doc.setDataField(document.getDataField());
      doc.setDataProvider(document.getDataProvider());
      doc.setDataSource(document.getDataSource());
      doc.setIdentifiers(document.getIdentifiers());
      doc.setObservationTime(document.getObservationTime());
      doc.setTimeSeries(document.getTimeSeries());
      _store.put(uniqueId, doc);  // unique identifier should be unique
      document.setUniqueId(uniqueId);
      return document;
    } else {
      throw new IllegalArgumentException("Cannot add duplicate time-series for identifiers " + document.getIdentifiers());
    }
  }

  private boolean contains(HistoricalTimeSeriesDocument document) {
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
  public HistoricalTimeSeriesDocument update(HistoricalTimeSeriesDocument document) {
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    validateDocument(document);
    
    final UniqueIdentifier uniqueId = document.getUniqueId();
    final HistoricalTimeSeriesDocument storedDocument = _store.get(uniqueId);
    if (storedDocument == null) {
      throw new DataNotFoundException("Historical time-series not found: " + uniqueId);
    }
    if (_store.replace(uniqueId, storedDocument, document) == false) {
      throw new IllegalArgumentException("Concurrent modification");
    }
    return document;
  }
  
  @Override
  public void remove(UniqueIdentifier uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    if (_store.remove(uniqueId) == null) {
      throw new DataNotFoundException("Historical time-series not found: " + uniqueId);
    }
  }

  @Override
  public HistoricalTimeSeriesSearchHistoricResult searchHistoric(final HistoricalTimeSeriesSearchHistoricRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getHistoricalTimeSeriesId(), "request.timeSeriesId");
    
    final HistoricalTimeSeriesSearchHistoricResult result = new HistoricalTimeSeriesSearchHistoricResult();
    HistoricalTimeSeriesDocument doc = get(request.getHistoricalTimeSeriesId());
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
    
    UniqueIdentifier timeSeriesId = document.getHistoricalTimeSeriesId();
    validateId(timeSeriesId);
    
    HistoricalTimeSeriesDocument storeDoc = _store.get(timeSeriesId);
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
    UniqueIdentifier timeSeriesId = document.getHistoricalTimeSeriesId();
    validateId(timeSeriesId);
    
    HistoricalTimeSeriesDocument storedDoc = _store.get(timeSeriesId);
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
    result.setHistoricalTimeSeriesId(UniqueIdentifier.of(DEFAULT_UID_SCHEME, String.valueOf(tsId)));
    result.setDataPointId(uniqueId);
    
    HistoricalTimeSeriesDocument storedDoc = _store.get(UniqueIdentifier.of(DEFAULT_UID_SCHEME, String.valueOf(tsId)));
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
    HistoricalTimeSeriesDocument storedDoc = _store.get(UniqueIdentifier.of(DEFAULT_UID_SCHEME, String.valueOf(tsId)));
    
    MapLocalDateDoubleTimeSeries mutableTS = new MapLocalDateDoubleTimeSeries(storedDoc.getTimeSeries());
    mutableTS.removeDataPoint(uniqueIdPair.getSecond());
    storedDoc.setTimeSeries(mutableTS);
  }

  @Override
  public void appendTimeSeries(HistoricalTimeSeriesDocument document) {
    validateDocument(document);
    
    validateId(document.getUniqueId());
    UniqueIdentifier uniqueId = document.getUniqueId();
    HistoricalTimeSeriesDocument storedDoc = _store.get(uniqueId);
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
    
    for (Entry<UniqueIdentifier, HistoricalTimeSeriesDocument> entry : _store.entrySet()) {
      UniqueIdentifier uniqueId = entry.getKey();
      HistoricalTimeSeriesDocument tsDoc = entry.getValue();
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
    validateId(timeSeriesUid);
    HistoricalTimeSeriesDocument storedDoc = _store.get(timeSeriesUid);
    LocalDateDoubleTimeSeries timeSeries = storedDoc.getTimeSeries();
    LocalDateDoubleTimeSeries subSeries = timeSeries.subSeries(firstDateToRetain, true, timeSeries.getLatestTime(), false).toLocalDateDoubleTimeSeries();
    storedDoc.setTimeSeries(subSeries);
  }

  //-------------------------------------------------------------------------
  private long validateId(UniqueIdentifier uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.isTrue(uniqueId.getScheme().equals(DEFAULT_UID_SCHEME), "historicalTimeSeriesId scheme invalid");
    try {
      return Long.parseLong(uniqueId.getValue());
    } catch (NumberFormatException ex) {
      throw new IllegalArgumentException("Invalid uniqueId " + uniqueId);
    }
  }

  private void validateDocument(HistoricalTimeSeriesDocument document) {
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
