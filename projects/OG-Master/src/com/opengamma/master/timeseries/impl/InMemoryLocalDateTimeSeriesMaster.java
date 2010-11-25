/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.master.timeseries.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.time.calendar.LocalDate;
import javax.time.calendar.format.CalendricalParseException;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.Collections2;
import com.opengamma.DataNotFoundException;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.IdentifierBundleWithDates;
import com.opengamma.id.IdentifierWithDates;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.UniqueIdentifierSupplier;
import com.opengamma.master.timeseries.DataFieldBean;
import com.opengamma.master.timeseries.DataPointDocument;
import com.opengamma.master.timeseries.DataProviderBean;
import com.opengamma.master.timeseries.DataSourceBean;
import com.opengamma.master.timeseries.ObservationTimeBean;
import com.opengamma.master.timeseries.SchemeBean;
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
  private final Supplier<UniqueIdentifier> _uidSupplier;

  /**
   * Creates an empty time-series master using the default scheme for any {@link UniqueIdentifier}s created.
   */
  public InMemoryLocalDateTimeSeriesMaster() {
    this(new UniqueIdentifierSupplier(DEFAULT_UID_SCHEME));
  }

  /**
   * Creates an instance specifying the supplier of unique identifiers.
   * 
   * @param uidSupplier  the supplier of unique identifiers, not null
   */
  private InMemoryLocalDateTimeSeriesMaster(final Supplier<UniqueIdentifier> uidSupplier) {
    ArgumentChecker.notNull(uidSupplier, "uidSupplier");
    _uidSupplier = uidSupplier;
  }

  //-------------------------------------------------------------------------
  @Override
  public DataSourceBean getOrCreateDataSource(String dataSource, String description) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<DataSourceBean> getDataSources() {
    throw new UnsupportedOperationException();
  }

  @Override
  public DataProviderBean getOrCreateDataProvider(String dataProvider, String description) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<DataProviderBean> getDataProviders() {
    throw new UnsupportedOperationException();
  }

  @Override
  public DataFieldBean getOrCreateDataField(String field, String description) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<DataFieldBean> getDataFields() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ObservationTimeBean getOrCreateObservationTime(String observationTime, String description) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<ObservationTimeBean> getObservationTimes() {
    throw new UnsupportedOperationException();
  }

  @Override
  public SchemeBean getOrCreateScheme(String scheme, String descrption) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<SchemeBean> getSchemes() {
    throw new UnsupportedOperationException();
  }

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
  public TimeSeriesSearchResult<LocalDate> searchTimeSeries(final TimeSeriesSearchRequest<LocalDate> request) {
    ArgumentChecker.notNull(request, "request");
    final TimeSeriesSearchResult<LocalDate> result = new TimeSeriesSearchResult<LocalDate>();
    Collection<TimeSeriesDocument<LocalDate>> docs = _timeseriesDb.values();
    
    if (request.getTimeSeriesId() != null) {
      docs = Collections.singleton(getTimeSeries(request.getTimeSeriesId()));
    }
    
    if (request.getDataField() != null) {
      docs = Collections2.filter(docs, new Predicate<TimeSeriesDocument<LocalDate>>() {
        @Override
        public boolean apply(final TimeSeriesDocument<LocalDate> doc) {
          return request.getDataField().equals(doc.getDataField());
        }
      });
    }
    
    if (request.getDataProvider() != null) {
      docs = Collections2.filter(docs, new Predicate<TimeSeriesDocument<LocalDate>>() {
        @Override
        public boolean apply(final TimeSeriesDocument<LocalDate> doc) {
          return request.getDataProvider().equals(doc.getDataProvider());
        }
      });
    }
    
    if (request.getDataSource() != null) {
      docs = Collections2.filter(docs, new Predicate<TimeSeriesDocument<LocalDate>>() {
        @Override
        public boolean apply(final TimeSeriesDocument<LocalDate> doc) {
          return request.getDataSource().equals(doc.getDataSource());
        }
      });
    }
    
    if (request.getObservationTime() != null) {
      docs = Collections2.filter(docs, new Predicate<TimeSeriesDocument<LocalDate>>() {
        @Override
        public boolean apply(final TimeSeriesDocument<LocalDate> doc) {
          return request.getObservationTime().equals(doc.getObservationTime());
        }
      });
    }
    
    if (request.getIdentifiers() != null && !request.getIdentifiers().isEmpty()) {
      docs = Collections2.filter(docs, new Predicate<TimeSeriesDocument<LocalDate>>() {
        @Override
        public boolean apply(final TimeSeriesDocument<LocalDate> doc) {
          IdentifierBundleWithDates bundleWithDates = doc.getIdentifiers();
          List<Identifier> requestIdentifiers = request.getIdentifiers();
          LocalDate currentDate = request.getCurrentDate();
          for (IdentifierWithDates idWithDates : bundleWithDates) {
            LocalDate validFrom = idWithDates.getValidFrom();
            LocalDate validTo = idWithDates.getValidTo();
            if (requestIdentifiers.contains(idWithDates.asIdentifier())) {
              if (currentDate != null) {
                if (validFrom == null && validTo != null) {
                  return currentDate.isBefore(validTo) || currentDate.equals(validTo);
                }
                if (validFrom != null && validTo == null) {
                  return currentDate.isAfter(validFrom) || currentDate.equals(validFrom);
                }
                if (validFrom != null && validTo != null) {
                  return currentDate.equals(validFrom) || currentDate.equals(validTo) || (currentDate.isAfter(validFrom) && currentDate.isBefore(validTo));
                }
              } else {
                return true;
              }
            }
          }
          return false;
        }
      });
    }
        
    if (request.getIdentifierValue() != null) {
      docs = Collections2.filter(docs, new Predicate<TimeSeriesDocument<LocalDate>>() {
        @Override
        public boolean apply(final TimeSeriesDocument<LocalDate> doc) {
          IdentifierBundle bundle = doc.getIdentifiers().asIdentifierBundle();
          Set<String> identifierValues = new HashSet<String>();
          for (Identifier identifier : bundle.getIdentifiers()) {
            identifierValues.add(identifier.getValue());
          }
          return identifierValues.contains(request.getIdentifierValue());
        }
      });
    }
    
    if (request.isLoadDates()) {
      for (TimeSeriesDocument<LocalDate> tsDocument : docs) {
        assert tsDocument.getTimeSeries() != null;
        tsDocument.setLatest(tsDocument.getTimeSeries().getLatestTime());
        tsDocument.setEarliest(tsDocument.getTimeSeries().getEarliestTime());
      }
    }
    
    if (!request.isLoadTimeSeries()) {
      List<TimeSeriesDocument<LocalDate>> noSeries = new ArrayList<TimeSeriesDocument<LocalDate>>();
      for (TimeSeriesDocument<LocalDate> tsDocument : docs) {
        TimeSeriesDocument<LocalDate> doc = new TimeSeriesDocument<LocalDate>();
        doc.setDataField(tsDocument.getDataField());
        doc.setDataProvider(tsDocument.getDataProvider());
        doc.setDataSource(tsDocument.getDataSource());
        doc.setEarliest(tsDocument.getEarliest());
        doc.setIdentifiers(tsDocument.getIdentifiers());
        doc.setLatest(tsDocument.getLatest());
        doc.setObservationTime(tsDocument.getObservationTime());
        doc.setUniqueIdentifier(tsDocument.getUniqueIdentifier());
        noSeries.add(doc);
      }
      docs = noSeries;
    }
    
    if (request.getStart() != null && request.getEnd() != null) {
      List<TimeSeriesDocument<LocalDate>> subseriesList = new ArrayList<TimeSeriesDocument<LocalDate>>();
      for (TimeSeriesDocument<LocalDate> tsDocument : docs) {
        TimeSeriesDocument<LocalDate> doc = new TimeSeriesDocument<LocalDate>();
        doc.setDataField(tsDocument.getDataField());
        doc.setDataProvider(tsDocument.getDataProvider());
        doc.setDataSource(tsDocument.getDataSource());
        doc.setEarliest(tsDocument.getEarliest());
        doc.setIdentifiers(tsDocument.getIdentifiers());
        doc.setLatest(tsDocument.getLatest());
        doc.setObservationTime(tsDocument.getObservationTime());
        doc.setUniqueIdentifier(tsDocument.getUniqueIdentifier());
        DoubleTimeSeries<LocalDate> subseries = tsDocument.getTimeSeries().subSeries(request.getStart(), true, request.getEnd(), false);
        doc.setTimeSeries(subseries);
        subseriesList.add(doc);
      }
      docs = subseriesList;
    }
    
    result.getDocuments().addAll(docs);
    result.setPaging(Paging.of(docs));
    return result;
    
  }
  
  @Override
  public TimeSeriesDocument<LocalDate> getTimeSeries(UniqueIdentifier uid) {
    validateUId(uid);
    final TimeSeriesDocument<LocalDate> document = _timeseriesDb.get(uid);
    if (document == null) {
      throw new DataNotFoundException("Timeseries not found: " + uid);
    }
    return document;
  }
  
  private void validateUId(UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "TimeSeries UID");
    ArgumentChecker.isTrue(uid.getScheme().equals(DEFAULT_UID_SCHEME), "UID not " + DEFAULT_UID_SCHEME);
    ArgumentChecker.isTrue(uid.getValue() != null, "Uid value cannot be null");
    
    try {
      Long.parseLong(uid.getValue());
    } catch (NumberFormatException ex) {
      throw new IllegalArgumentException("Invalid UID " + uid);
    }
  }

  @Override
  public TimeSeriesDocument<LocalDate> addTimeSeries(TimeSeriesDocument<LocalDate> document) {
    validateTimeSeriesDocument(document);
    if (!contains(document)) {
      final UniqueIdentifier uid = _uidSupplier.get();
      final TimeSeriesDocument<LocalDate> doc = new TimeSeriesDocument<LocalDate>();
      doc.setUniqueIdentifier(uid);
      doc.setDataField(document.getDataField());
      doc.setDataProvider(document.getDataProvider());
      doc.setDataSource(document.getDataSource());
      doc.setIdentifiers(document.getIdentifiers());
      doc.setObservationTime(document.getObservationTime());
      doc.setTimeSeries(document.getTimeSeries());
      _timeseriesDb.put(uid, doc);  // unique identifier should be unique
      document.setUniqueIdentifier(uid);
      return document;
    } else {
      throw new IllegalArgumentException("cannot add duplicate TimeSeries for identifiers " + document.getIdentifiers());
    }
  }
  
  private void validateTimeSeriesDocument(TimeSeriesDocument<LocalDate> document) {
    ArgumentChecker.notNull(document, "timeseries document");
    ArgumentChecker.notNull(document.getTimeSeries(), "Timeseries");
    ArgumentChecker.notNull(document.getIdentifiers(), "identifiers");
    ArgumentChecker.isTrue(!document.getIdentifiers().asIdentifierBundle().getIdentifiers().isEmpty(), "cannot add timeseries with empty identifiers");
    ArgumentChecker.isTrue(!StringUtils.isBlank(document.getDataSource()), "cannot add timeseries with blank dataSource");
    ArgumentChecker.isTrue(!StringUtils.isBlank(document.getDataProvider()), "cannot add timeseries with blank dataProvider");
    ArgumentChecker.isTrue(!StringUtils.isBlank(document.getDataProvider()), "cannot add timeseries with blank dataProvider");
    ArgumentChecker.isTrue(!StringUtils.isBlank(document.getDataField()), "cannot add timeseries with blank field");
    ArgumentChecker.isTrue(!StringUtils.isBlank(document.getObservationTime()), "cannot add timeseries with blank observationTime");
    ArgumentChecker.isTrue(!StringUtils.isBlank(document.getDataProvider()), "cannot add timeseries with blank dataProvider");
  }
  
  private boolean contains(TimeSeriesDocument<LocalDate> document) {
    for (IdentifierWithDates identifierWithDates : document.getIdentifiers()) {
      Identifier identifier = identifierWithDates.asIdentifier();
      UniqueIdentifier uid = resolveIdentifier(
          IdentifierBundle.of(identifier), 
          identifierWithDates.getValidFrom(), 
          document.getDataSource(), 
          document.getDataProvider(), 
          document.getDataField());
      if (uid != null) {
        return true;
      }
    }
    return false;
  }
  
  @Override
  public TimeSeriesDocument<LocalDate> updateTimeSeries(TimeSeriesDocument<LocalDate> document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getTimeSeries(), "document.timeseries");
    ArgumentChecker.notNull(document.getDataField(), "document.dataField");
    ArgumentChecker.notNull(document.getDataProvider(), "document.dataProvider");
    ArgumentChecker.notNull(document.getDataSource(), "document.dataSource");
    ArgumentChecker.notNull(document.getObservationTime(), "document.observationTime");
    ArgumentChecker.notNull(document.getUniqueIdentifier(), "document.uid");
    
    final UniqueIdentifier uid = document.getUniqueIdentifier();
    final TimeSeriesDocument<LocalDate> storedDocument = _timeseriesDb.get(uid);
    if (storedDocument == null) {
      throw new DataNotFoundException("Timeseries not found: " + uid);
    }
    if (_timeseriesDb.replace(uid, storedDocument, document) == false) {
      throw new IllegalArgumentException("Concurrent modification");
    }
    return document;
  }
  
  @Override
  public void removeTimeSeries(UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    if (_timeseriesDb.remove(uid) == null) {
      throw new DataNotFoundException("Timeseries not found: " + uid);
    }
  }

  @Override
  public TimeSeriesSearchHistoricResult<LocalDate> searchHistoric(final TimeSeriesSearchHistoricRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getTimeSeriesId(), "request.timeseriesId");
    
    final TimeSeriesSearchHistoricResult<LocalDate> result = new TimeSeriesSearchHistoricResult<LocalDate>();
    TimeSeriesDocument<LocalDate> doc = getTimeSeries(request.getTimeSeriesId());
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
    validateUId(timeSeriesId);
    
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
    validateUId(timeSeriesId);
    
    TimeSeriesDocument<LocalDate> storedDoc = _timeseriesDb.get(timeSeriesId);
    MapLocalDateDoubleTimeSeries mutableTS = new MapLocalDateDoubleTimeSeries();
    mutableTS.putDataPoint(document.getDate(), document.getValue());
    DoubleTimeSeries<LocalDate> mergedTS = storedDoc.getTimeSeries().noIntersectionOperation(mutableTS);
    storedDoc.setTimeSeries(mergedTS);
    
    String uid = new StringBuilder(timeSeriesId.getValue()).append("/").append(DateUtil.printYYYYMMDD(document.getDate())).toString();
    document.setDataPointId(UniqueIdentifier.of(DEFAULT_UID_SCHEME, uid));
    return document;
    
  }
  
  @Override
  public DataPointDocument<LocalDate> getDataPoint(UniqueIdentifier uid) {
    Pair<Long, LocalDate> uidPair = validateAndGetDataPointId(uid);
    
    Long tsId = uidPair.getFirst();
    LocalDate date = uidPair.getSecond();
    
    final DataPointDocument<LocalDate> result = new DataPointDocument<LocalDate>();
    result.setDate(uidPair.getSecond());
    result.setTimeSeriesId(UniqueIdentifier.of(DEFAULT_UID_SCHEME, String.valueOf(tsId)));
    result.setDataPointId(uid);
    
    TimeSeriesDocument<LocalDate> storedDoc = _timeseriesDb.get(UniqueIdentifier.of(DEFAULT_UID_SCHEME, String.valueOf(tsId)));
    Double value = storedDoc.getTimeSeries().getValue(date);
    result.setValue(value);
       
    return result;
  }
  
  private Pair<Long, LocalDate> validateAndGetDataPointId(UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "DataPoint UID");
    ArgumentChecker.isTrue(uid.getScheme().equals(DEFAULT_UID_SCHEME), "UID not TssMemory");
    ArgumentChecker.isTrue(uid.getValue() != null, "Uid value cannot be null");
    String[] tokens = StringUtils.split(uid.getValue(), '/');
    if (tokens.length != 2) {
      throw new IllegalArgumentException("UID not expected format<12345/date> " + uid);
    }
    String id = tokens[0];
    String dateStr = tokens[1];
    LocalDate date = null;
    Long tsId = Long.MIN_VALUE;
    if (id != null && dateStr != null) {
      try {
        date = DateUtil.toLocalDate(dateStr);
      } catch (CalendricalParseException ex) {
        throw new IllegalArgumentException("UID not expected format<12345/date> " + uid, ex);
      }
      try {
        tsId = Long.parseLong(id);
      } catch (NumberFormatException ex) {
        throw new IllegalArgumentException("UID not expected format<12345/date> " + uid, ex);
      }
    } else {
      throw new IllegalArgumentException("UID not expected format<12345/date> " + uid);
    }
    return Pair.of(tsId, date);
  }

  @Override
  public void removeDataPoint(UniqueIdentifier uid) {
    Pair<Long, LocalDate> uidPair = validateAndGetDataPointId(uid);
    
    Long tsId = uidPair.getFirst();
    TimeSeriesDocument<LocalDate> storedDoc = _timeseriesDb.get(UniqueIdentifier.of(DEFAULT_UID_SCHEME, String.valueOf(tsId)));
    
    MapLocalDateDoubleTimeSeries mutableTS = new MapLocalDateDoubleTimeSeries(storedDoc.getTimeSeries());
    mutableTS.removeDataPoint(uidPair.getSecond());
    storedDoc.setTimeSeries(mutableTS);
  }

  

  @Override
  public void appendTimeSeries(TimeSeriesDocument<LocalDate> document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getIdentifiers(), "identifiers");
    ArgumentChecker.notNull(document.getDataSource(), "dataSource");
    ArgumentChecker.notNull(document.getDataProvider(), "dataProvider");
    ArgumentChecker.notNull(document.getDataField(), "dataField");
    
    validateUId(document.getUniqueIdentifier());
    UniqueIdentifier uid = document.getUniqueIdentifier();
    TimeSeriesDocument<LocalDate> storedDoc = _timeseriesDb.get(uid);
    DoubleTimeSeries<LocalDate> mergedTS = storedDoc.getTimeSeries().noIntersectionOperation(document.getTimeSeries());
    storedDoc.setTimeSeries(mergedTS);
  }
  
  @Override
  public UniqueIdentifier resolveIdentifier(IdentifierBundle securityBundle, String dataSource, String dataProvider, String dataField) {
    return resolveIdentifier(securityBundle, (LocalDate) null, dataSource, dataProvider, dataField);
  }

  @Override
  public UniqueIdentifier resolveIdentifier(IdentifierBundle securityBundle, LocalDate currentDate, String dataSource, String dataProvider, String dataField) {
    ArgumentChecker.notNull(securityBundle, "securityBundle");
    ArgumentChecker.notNull(dataSource, "dataSource");
    ArgumentChecker.notNull(dataProvider, "dataProvider");
    ArgumentChecker.notNull(dataField, "dataField");
    
    for (Entry<UniqueIdentifier, TimeSeriesDocument<LocalDate>> entry : _timeseriesDb.entrySet()) {
      UniqueIdentifier uid = entry.getKey();
      TimeSeriesDocument<LocalDate> tsDoc = entry.getValue();
      if (tsDoc.getDataSource().equals(dataSource) && tsDoc.getDataProvider().equals(dataProvider) && tsDoc.getDataField().equals(dataField)) {
        for (IdentifierWithDates idWithDates : tsDoc.getIdentifiers()) {
          if (securityBundle.contains(idWithDates.asIdentifier())) {
            LocalDate validFrom = idWithDates.getValidFrom();
            LocalDate validTo = idWithDates.getValidTo();
            if (currentDate != null) {
              if (currentDate.equals(validFrom) || (currentDate.isAfter(validFrom) && currentDate.isBefore(validTo)) || currentDate.equals(validTo)) {
                return uid;
              }
            } else {
              return uid;
            }
          }
        }
      }
    }
    return null;
  }

  @Override
  public void removeDataPoints(UniqueIdentifier timeSeriesUid, LocalDate firstDateToRetain) {
    validateUId(timeSeriesUid);
    TimeSeriesDocument<LocalDate> storedDoc = _timeseriesDb.get(timeSeriesUid);
    DoubleTimeSeries<LocalDate> timeSeries = storedDoc.getTimeSeries();
    DoubleTimeSeries<LocalDate> subSeries = timeSeries.subSeries(firstDateToRetain, true, timeSeries.getLatestTime(), false);
    storedDoc.setTimeSeries(subSeries);
  }

}
