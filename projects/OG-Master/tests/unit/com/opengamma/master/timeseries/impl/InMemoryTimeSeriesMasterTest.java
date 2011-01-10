/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.timeseries.impl;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.time.calendar.DayOfWeek;
import javax.time.calendar.LocalDate;
import javax.time.calendar.MonthOfYear;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.IdentifierBundleWithDates;
import com.opengamma.id.IdentifierWithDates;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.timeseries.DataPointDocument;
import com.opengamma.master.timeseries.TimeSeriesDocument;
import com.opengamma.master.timeseries.TimeSeriesMaster;
import com.opengamma.master.timeseries.TimeSeriesSearchRequest;
import com.opengamma.master.timeseries.TimeSeriesSearchResult;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.MapLocalDateDoubleTimeSeries;

/**
 * Abstract in memory test.
 */
@Ignore("Abstract class")
abstract public class InMemoryTimeSeriesMasterTest<T> {
  
  private static final int TS_DATASET_SIZE = 1;

  private static final String LCLOSE_OBSERVATION_TIME = "LCLOSE";
  private static final String CLOSE_DATA_FIELD = "CLOSE";
  private static final String CMPL_DATA_PROVIDER = "CMPL";
  private static final String BBG_DATA_SOURCE = "BLOOMBERG";
  
  private static final String[] DATA_FIELDS = new String[] { CLOSE_DATA_FIELD, "VOLUME" };
  private static final String[] DATA_PROVIDERS = new String[] { "UNKNOWN", CMPL_DATA_PROVIDER, "CMPT" };
  private static final String[] DATA_SOURCES = new String[] { BBG_DATA_SOURCE, "REUTERS", "JPM" };

  private static final LocalDate DEFAULT_START = DateUtil.previousWeekDay().minusDays(7);

  private Random _random = new Random();
  private TimeSeriesMaster<T> _tsMaster;
  
  abstract protected DoubleTimeSeries<T> getTimeSeries(MapLocalDateDoubleTimeSeries tsMap);
  abstract protected DoubleTimeSeries<T> getTimeSeries(List<T> dates, List<Double> values);
  abstract protected DoubleTimeSeries<T> getEmptyTimeSeries();
  abstract protected T convert(LocalDate date);
  abstract protected LocalDate convert(T date);
  abstract protected String print(T date);
  abstract protected TimeSeriesMaster<T> createTimeSeriesMaster();
  
  /**
   * Gets the tsMaster field.
   * @return the tsMaster
   */
  protected TimeSeriesMaster<T> getTsMaster() {
    return _tsMaster;
  }

  @Before
  public void setUp() throws Exception {
    _tsMaster = createTimeSeriesMaster();
  }
  
  @Test
  public void getAllIdentifiers() throws Exception {
    
    List<IdentifierBundleWithDates> allIdentifiers = _tsMaster.getAllIdentifiers();
    assertNotNull(allIdentifiers);
    assertTrue(allIdentifiers.isEmpty());
    
    List<IdentifierBundle> expectedIds = new ArrayList<IdentifierBundle>();
    for (int i = 0; i < TS_DATASET_SIZE; i++) {
      Identifier id1 = Identifier.of("sa" + i, "ida" + i);
      Identifier id2 = Identifier.of("sb" + i, "idb" + i);
      IdentifierBundle identifiers = IdentifierBundle.of(id1, id2);
      LocalDate previousWeekDay = DateUtil.previousWeekDay();
      DoubleTimeSeries<T> timeSeries = makeRandomTimeSeries(previousWeekDay, 7);
      expectedIds.add(identifiers);
      
      TimeSeriesDocument<T> tsDocument = new TimeSeriesDocument<T>();
      tsDocument.setDataField(CLOSE_DATA_FIELD);
      tsDocument.setDataProvider(CMPL_DATA_PROVIDER);
      tsDocument.setDataSource(BBG_DATA_SOURCE);
      tsDocument.setObservationTime(LCLOSE_OBSERVATION_TIME);
      tsDocument.setIdentifiers(IdentifierBundleWithDates.of(identifiers));
      
      tsDocument.setTimeSeries(timeSeries);
      
      tsDocument = _tsMaster.addTimeSeries(tsDocument);
      
      assertNotNull(tsDocument);
      assertNotNull(tsDocument.getUniqueId());
    }
    
    allIdentifiers = _tsMaster.getAllIdentifiers();
    assertNotNull(allIdentifiers);
    assertTrue(allIdentifiers.size() == expectedIds.size());
    for (IdentifierBundle identifierBundle : expectedIds) {
      assertTrue(allIdentifiers.contains(IdentifierBundleWithDates.of(identifierBundle)));
    }
    
  }
  
  @Test
  public void searchByIdentifierBundle() throws Exception {
    List<TimeSeriesDocument<T>> expectedTS = addAndTestTimeSeries();
    for (TimeSeriesDocument<T> expectedTSDoc : expectedTS) {
      TimeSeriesSearchResult<T> searchResult = search(null, null, expectedTSDoc.getIdentifiers().asIdentifierBundle(), expectedTSDoc.getDataField(), expectedTSDoc.getDataProvider(), expectedTSDoc.getDataSource(), expectedTSDoc.getObservationTime(), true, false);
      assertNotNull(searchResult);
      List<TimeSeriesDocument<T>> documents = searchResult.getDocuments();
      assertNotNull(documents);
      assertTrue(documents.size() == 1);
      
      TimeSeriesDocument<T> searchedDoc = documents.get(0);
      assertNotNull(searchedDoc);
      
      assertEqualTimeSeriesDocument(expectedTSDoc, searchedDoc);
    }
  }

  private TimeSeriesSearchResult<T> search(LocalDate currentDate, UniqueIdentifier timeSeriesId, IdentifierBundle bundle, String dataField, String dataProvider, String dataSource, String observationTime, boolean loadTimeSeries, boolean loadDates) {    
    TimeSeriesSearchRequest<T> request = new TimeSeriesSearchRequest<T>();
    request.setCurrentDate(currentDate);
    request.setTimeSeriesId(timeSeriesId);
    if (bundle != null) {
      request.getIdentifiers().addAll(bundle.getIdentifiers());
    }
    request.setDataField(dataField);
    request.setDataProvider(dataProvider);
    request.setDataSource(dataSource);
    request.setObservationTime(observationTime);
    request.setLoadTimeSeries(loadTimeSeries);
    request.setLoadDates(loadDates);
    TimeSeriesSearchResult<T> searchResult = _tsMaster.searchTimeSeries(request);
    return searchResult;
  }
  
  @Test
  public void searchByIdentifierValue() throws Exception {
    List<TimeSeriesDocument<T>> expectedTS = addAndTestTimeSeries();
    for (TimeSeriesDocument<T> expectedTSDoc : expectedTS) {
      TimeSeriesSearchRequest<T> request = new TimeSeriesSearchRequest<T>();
      IdentifierWithDates identifierWithDates = expectedTSDoc.getIdentifiers().getIdentifiers().iterator().next();
      request.setIdentifierValue(identifierWithDates.asIdentifier().getValue());
      request.setDataField(expectedTSDoc.getDataField());
      request.setDataProvider(expectedTSDoc.getDataProvider());
      request.setDataSource(expectedTSDoc.getDataSource());
      request.setObservationTime(expectedTSDoc.getObservationTime());
      request.setLoadTimeSeries(true);
      
      TimeSeriesSearchResult<T> searchResult = _tsMaster.searchTimeSeries(request);
      assertNotNull(searchResult);
      List<TimeSeriesDocument<T>> documents = searchResult.getDocuments();
      assertNotNull(documents);
      assertTrue(documents.size() == 1);
      
      TimeSeriesDocument<T> searchedDoc = documents.get(0);
      assertNotNull(searchedDoc);
      
      assertEqualTimeSeriesDocument(expectedTSDoc, searchedDoc);
    }
  }
  
  @Test
  public void searchByFieldProviderSource() throws Exception {
    List<TimeSeriesDocument<T>> tsList = addAndTestTimeSeries();
    for (TimeSeriesDocument<T> tsDoc : tsList) {
      TimeSeriesSearchResult<T> searchResult = search(null, null, tsDoc.getIdentifiers().asIdentifierBundle(), tsDoc.getDataField(), tsDoc.getDataProvider(), tsDoc.getDataSource(), tsDoc.getObservationTime(), true, false);
      assertNotNull(searchResult);
      List<TimeSeriesDocument<T>> documents = searchResult.getDocuments();
      assertNotNull(documents);
      assertTrue(documents.size() == 1);
      
      assertEqualTimeSeriesDocument(tsDoc, documents.get(0));
    }
  }
  
  @Test
  public void searchByUID() throws Exception {
    List<TimeSeriesDocument<T>> tsList = addAndTestTimeSeries();
    for (TimeSeriesDocument<T> tsDoc : tsList) {
      TimeSeriesSearchResult<T> searchResult = search(null, tsDoc.getUniqueId(), null, null, null, null, null, true, false);
      assertNotNull(searchResult);
      List<TimeSeriesDocument<T>> documents = searchResult.getDocuments();
      assertNotNull(documents);
      assertTrue(documents.size() == 1);
      assertEqualTimeSeriesDocument(tsDoc, documents.get(0));
    }
  }
  
  @Test
  public void addTimeSeries() throws Exception {
    addAndTestTimeSeries();    
  }
  
  protected List<TimeSeriesDocument<T>> addAndTestTimeSeries() {
    List<TimeSeriesDocument<T>> result = new ArrayList<TimeSeriesDocument<T>>(); 
    for (int i = 0; i < TS_DATASET_SIZE; i++) {
      IdentifierBundle identifiers = IdentifierBundle.of(SecurityUtils.bloombergTickerSecurityId("ticker" + i), SecurityUtils.bloombergBuidSecurityId("buid" + i));
      LocalDate start = DateUtil.previousWeekDay().minusDays(7);
      for (String dataSource : DATA_SOURCES) {
        for (String dataProvider : DATA_PROVIDERS) {
          for (String datafield : DATA_FIELDS) {
            TimeSeriesDocument<T> tsDocument = new TimeSeriesDocument<T>();
            tsDocument.setDataField(datafield);
            tsDocument.setDataProvider(dataProvider);
            tsDocument.setDataSource(dataSource);
            tsDocument.setObservationTime(LCLOSE_OBSERVATION_TIME);
            tsDocument.setIdentifiers(IdentifierBundleWithDates.of(identifiers));
            DoubleTimeSeries<T> timeSeries = makeRandomTimeSeries(start, 7);
            assertTrue(timeSeries.size() == 7);
            assertEquals(convert(start), timeSeries.getEarliestTime());
            tsDocument.setTimeSeries(timeSeries);
            
            tsDocument = _tsMaster.addTimeSeries(tsDocument);
            
            assertNotNull(tsDocument);
            assertNotNull(tsDocument.getUniqueId());
            
            TimeSeriesDocument<T> actualDoc = _tsMaster.getTimeSeries(tsDocument.getUniqueId());
            assertNotNull(actualDoc);
            assertEquals(timeSeries, actualDoc.getTimeSeries());
            result.add(tsDocument);
          }
        }
      }
    }
    return result;
  }

  
  @Test
  public void addDuplicateTimeSeries() throws Exception {
    IdentifierBundle identifiers = IdentifierBundle.of(Identifier.of("sa", "ida"), Identifier.of("sb", "idb"));
    DoubleTimeSeries<T> timeSeries = makeRandomTimeSeries(DEFAULT_START, 7);
    
    TimeSeriesDocument<T> tsDocument = new TimeSeriesDocument<T>();
    tsDocument.setDataField(CLOSE_DATA_FIELD);
    tsDocument.setDataProvider(CMPL_DATA_PROVIDER);
    tsDocument.setDataSource(BBG_DATA_SOURCE);
    tsDocument.setObservationTime(LCLOSE_OBSERVATION_TIME);
    tsDocument.setIdentifiers(IdentifierBundleWithDates.of(identifiers));
    tsDocument.setTimeSeries(timeSeries);
    
    tsDocument = _tsMaster.addTimeSeries(tsDocument);
    
    assertNotNull(tsDocument);
    assertNotNull(tsDocument.getUniqueId());
    
    TimeSeriesDocument<T> actualDoc = _tsMaster.getTimeSeries(tsDocument.getUniqueId());
    assertNotNull(actualDoc);
    
    assertEqualTimeSeriesDocument(tsDocument, actualDoc);
    
    //try and add another using the same meta data and identifiers
    TimeSeriesDocument<T> otherDoc = new TimeSeriesDocument<T>();
    otherDoc.setDataField(CLOSE_DATA_FIELD);
    otherDoc.setDataProvider(CMPL_DATA_PROVIDER);
    otherDoc.setDataSource(BBG_DATA_SOURCE);
    otherDoc.setObservationTime(LCLOSE_OBSERVATION_TIME);
    tsDocument.setIdentifiers(IdentifierBundleWithDates.of(identifiers));
    otherDoc.setTimeSeries(makeRandomTimeSeries(DEFAULT_START, 7));
    try {
      _tsMaster.addTimeSeries(otherDoc);
      fail();
    } catch (IllegalArgumentException ex) {
      //do nothing
    }
  }
  
  @Test
  public void updateTimeSeries() throws Exception {
    List<TimeSeriesDocument<T>> tsList = addAndTestTimeSeries();
    for (TimeSeriesDocument<T> tsDoc : tsList) {
      tsDoc.setTimeSeries(makeRandomTimeSeries(DEFAULT_START, 7));
      TimeSeriesDocument<T> updatedDoc = _tsMaster.updateTimeSeries(tsDoc);
      assertNotNull(updatedDoc);
      assertNotNull(updatedDoc.getUniqueId());
      assertEquals(tsDoc.getUniqueId(), updatedDoc.getUniqueId());
      
      assertEqualTimeSeriesDocument(updatedDoc, _tsMaster.getTimeSeries(updatedDoc.getUniqueId()));
      
      //delete dataPoints, set with empty timeseries
      tsDoc.setTimeSeries(getEmptyTimeSeries()); 
      updatedDoc = _tsMaster.updateTimeSeries(tsDoc);
      assertNotNull(updatedDoc);
      assertNotNull(updatedDoc.getUniqueId());
      assertEquals(tsDoc.getUniqueId(), updatedDoc.getUniqueId());
      
      assertEqualTimeSeriesDocument(updatedDoc, _tsMaster.getTimeSeries(updatedDoc.getUniqueId()));
    }
  }
  
  @Test
  public void removeTimeSeries() throws Exception {
    List<TimeSeriesDocument<T>> tsList = addAndTestTimeSeries();
    for (TimeSeriesDocument<T> tsDoc : tsList) {
      _tsMaster.removeTimeSeries(tsDoc.getUniqueId());
      try {
        _tsMaster.getTimeSeries(tsDoc.getUniqueId());
        fail();
      } catch(DataNotFoundException ex) {
        //do nothing
      }
    }
  }
  
  @Test
  public void getUnknownUID() throws Exception {
    List<TimeSeriesDocument<T>> tsList = addAndTestTimeSeries();
    try {
      TimeSeriesDocument<T> tsDoc = tsList.get(0);
      String scheme = tsDoc.getUniqueId().getScheme();
      _tsMaster.getTimeSeries(UniqueIdentifier.of(scheme, String.valueOf(Long.MIN_VALUE)));
      fail();
    } catch(DataNotFoundException ex) {
      //do nothing
    }
  }
  
  @Test 
  public void getInvalidUID() throws Exception {
    addAndTestTimeSeries();
    try {
      _tsMaster.getTimeSeries(UniqueIdentifier.of("INVALID", "unknown"));
      fail();
    } catch(IllegalArgumentException ex) {
      //do nothing
    }
  }
  
  @Test
  public void resolveIdentifier() throws Exception {
    List<TimeSeriesDocument<T>> tsList = addAndTestTimeSeries();
    for (TimeSeriesDocument<T> tsDoc : tsList) {
      IdentifierBundle bundle = tsDoc.getIdentifiers().asIdentifierBundle();
      UniqueIdentifier resolveIdentifier = _tsMaster.resolveIdentifier(bundle, tsDoc.getDataSource(), tsDoc.getDataProvider(), tsDoc.getDataField());
      assertNotNull(resolveIdentifier);
      assertEquals(tsDoc.getUniqueId(), resolveIdentifier);
      
      for (Identifier identifier : bundle) {
        resolveIdentifier = _tsMaster.resolveIdentifier(IdentifierBundle.of(identifier), tsDoc.getDataSource(), tsDoc.getDataProvider(), tsDoc.getDataField());
        assertNotNull(resolveIdentifier);
        assertEquals(tsDoc.getUniqueId(), resolveIdentifier);
      }
      
      resolveIdentifier = _tsMaster.resolveIdentifier(bundle, "INVALID", CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
      assertNull(resolveIdentifier);
      resolveIdentifier = _tsMaster.resolveIdentifier(bundle, BBG_DATA_SOURCE, "INVALID", CLOSE_DATA_FIELD);
      assertNull(resolveIdentifier);
      
      resolveIdentifier = _tsMaster.resolveIdentifier(bundle, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, "INVALID");
      assertNull(resolveIdentifier);
    }
    
    UniqueIdentifier resolveIdentifier = _tsMaster.resolveIdentifier(IdentifierBundle.of(Identifier.of("Invalid", "Invalid")), BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
    assertNull(resolveIdentifier);
    
    //check null identifiers
    try {
      IdentifierBundle identifiers = null;
      _tsMaster.resolveIdentifier(identifiers, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
      fail();
    } catch (IllegalArgumentException ex) {
      //do nothing
    }
  }
  
  @Test
  public void getHistoricalTimeSeriesWithoutDataProvider() throws Exception {
    Map<String, DoubleTimeSeries<T>> expectedTSMap = new HashMap<String, DoubleTimeSeries<T>>();
    
    IdentifierBundle bundle = IdentifierBundle.of(SecurityUtils.bloombergTickerSecurityId("id1"));
    for (String dataProvider : DATA_PROVIDERS) {
      
      DoubleTimeSeries<T> timeSeries = makeRandomTimeSeries(DEFAULT_START, 7);
      
      TimeSeriesDocument<T> tsDocument = new TimeSeriesDocument<T>();
      tsDocument.setDataField(CLOSE_DATA_FIELD);
      tsDocument.setDataProvider(dataProvider);
      tsDocument.setDataSource(BBG_DATA_SOURCE);
      tsDocument.setObservationTime(LCLOSE_OBSERVATION_TIME);
      tsDocument.setIdentifiers(IdentifierBundleWithDates.of(bundle));
      tsDocument.setTimeSeries(timeSeries);
      
      tsDocument = _tsMaster.addTimeSeries(tsDocument);
      assertNotNull(tsDocument);
      assertNotNull(tsDocument.getUniqueId());
      
      expectedTSMap.put(dataProvider, timeSeries);
    }
    
    //check you get expected timeseries with dataProvider specified
    for (String dataProvider : DATA_PROVIDERS) {
      TimeSeriesSearchResult<T> searchResult = search(null, null, bundle, CLOSE_DATA_FIELD, dataProvider, BBG_DATA_SOURCE, LCLOSE_OBSERVATION_TIME, true, false);
      
      assertNotNull(searchResult);
      List<TimeSeriesDocument<T>> documents = searchResult.getDocuments();
      assertNotNull(documents);
      assertTrue(documents.size() == 1);
      
      TimeSeriesDocument<T> searchedDoc = documents.get(0);
      assertNotNull(searchedDoc);
      
      assertEquals(expectedTSMap.get(dataProvider), searchedDoc.getTimeSeries());
    }
    
    //search without dataProvider
    TimeSeriesSearchResult<T> searchResult = search(null, null, bundle, CLOSE_DATA_FIELD, null, BBG_DATA_SOURCE, LCLOSE_OBSERVATION_TIME, true, false);
    assertNotNull(searchResult);
    List<TimeSeriesDocument<T>> documents = searchResult.getDocuments();
    assertNotNull(documents);
    assertTrue(documents.size() == expectedTSMap.entrySet().size());
    for (TimeSeriesDocument<T> tsDoc : documents) {
      String dataProvider = tsDoc.getDataProvider();
      DoubleTimeSeries<T> actualTS = tsDoc.getTimeSeries();
      assertEquals(expectedTSMap.get(dataProvider), actualTS);
    }
        
  }
    
  @Test
  public void appendTimeSeries() throws Exception {
    List<TimeSeriesDocument<T>> tsList = addAndTestTimeSeries();
    for (TimeSeriesDocument<T> tsDoc : tsList) {
      DoubleTimeSeries<T> timeSeries = tsDoc.getTimeSeries();
      LocalDate start = convert(timeSeries.getLatestTime()).plusDays(1);
      DoubleTimeSeries<T> appendedTS = makeRandomTimeSeries(start, 7);
      DoubleTimeSeries<T> mergedTS = timeSeries.noIntersectionOperation(appendedTS);
      // append timeseries to existing identifiers in the datastore
      tsDoc.setTimeSeries(appendedTS);
      _tsMaster.appendTimeSeries(tsDoc);
      
      TimeSeriesDocument<T> latestDoc = _tsMaster.getTimeSeries(tsDoc.getUniqueId());
      assertNotNull(latestDoc);
      tsDoc.setTimeSeries(mergedTS);
      assertEqualTimeSeriesDocument(tsDoc, latestDoc);
    }
  }
  
  @Test
  public void searchNotAvailableTimeSeries() throws Exception {
    addAndTestTimeSeries();
    IdentifierBundle bundle = IdentifierBundle.of(Identifier.of("BLOOMBERG_TICKER", "AAPL US Equity"), Identifier.of("BUID", "X-12345678"));
    TimeSeriesSearchResult<T> searchResult = search(null, null, bundle, CLOSE_DATA_FIELD, CMPL_DATA_PROVIDER, BBG_DATA_SOURCE, null, true, false);
    assertNotNull(searchResult);
    assertNotNull(searchResult.getDocuments());
    assertTrue(searchResult.getDocuments().isEmpty());
  }
  
  @Test
  public void searchMetaData() throws Exception {
    List<TimeSeriesDocument<T>> tsList = addAndTestTimeSeries();
    List<TimeSeriesDocument<T>> closeDataFields = new ArrayList<TimeSeriesDocument<T>>();
    List<TimeSeriesDocument<T>> cmplDataProviders = new ArrayList<TimeSeriesDocument<T>>();
    List<TimeSeriesDocument<T>> bbgDataSources = new ArrayList<TimeSeriesDocument<T>>();
    List<TimeSeriesDocument<T>> lcloseObservations = new ArrayList<TimeSeriesDocument<T>>();
    
    for (TimeSeriesDocument<T> tsDoc : tsList) {
      //set timeseries to null for metadata test
      tsDoc.setTimeSeries(null);
      if (tsDoc.getDataField().equals(CLOSE_DATA_FIELD)) {
        closeDataFields.add(tsDoc);
      }
      if (tsDoc.getDataProvider().equals(CMPL_DATA_PROVIDER)) {
        cmplDataProviders.add(tsDoc);
      }
      if (tsDoc.getDataSource().equals(BBG_DATA_SOURCE)) {
        bbgDataSources.add(tsDoc);
      }
      if (tsDoc.getObservationTime().equals(LCLOSE_OBSERVATION_TIME)) {
        lcloseObservations.add(tsDoc);
      }
    }
    //return all timeseries meta data without loading timeseries data points
    TimeSeriesSearchResult<T> searchResult = search(null, null, null, null, null, null, null, false, false);
    assertNotNull(searchResult);
    List<TimeSeriesDocument<T>> documents = searchResult.getDocuments();
    assertNotNull(documents);
    assertTrue(tsList.size() == documents.size());
    for (TimeSeriesDocument<T> expectedDoc : tsList) {
      assertTrue(documents.contains(expectedDoc));
    }
    
    searchResult = search(null, null, null, CLOSE_DATA_FIELD, null, null, null, false, false);
    documents = searchResult.getDocuments();
    assertTrue(closeDataFields.size() == documents.size());
    for (TimeSeriesDocument<T> tsDoc : documents) {
      assertTrue(closeDataFields.contains(tsDoc));
    }

    searchResult = search(null, null, null, null, null, BBG_DATA_SOURCE, null, false, false);
    documents = searchResult.getDocuments();
    assertTrue(bbgDataSources.size() == documents.size());
    for (TimeSeriesDocument<T> tsDoc : documents) {
      assertTrue(bbgDataSources.contains(tsDoc));
    }
    
    searchResult = search(null, null, null, null, null, null, LCLOSE_OBSERVATION_TIME, false, false);
    documents = searchResult.getDocuments();
    assertTrue(lcloseObservations.size() == documents.size());
    for (TimeSeriesDocument<T> tsDoc : documents) {
      assertTrue(lcloseObservations.contains(tsDoc));
    }
    
    searchResult = search(null, null, null, null, CMPL_DATA_PROVIDER, null, null, false, false);
    documents = searchResult.getDocuments();
    assertTrue(cmplDataProviders.size() == documents.size());
    for (TimeSeriesDocument<T> tsDoc : documents) {
      assertTrue(cmplDataProviders.contains(tsDoc));
    }
  }
  
  @Test
  public void searchMetaDataWithDates() throws Exception {
    List<TimeSeriesDocument<T>> tsList = addAndTestTimeSeries();
    //return all timeseries meta data with dates without loading timeseries data points
    TimeSeriesSearchResult<T> searchResult = search(null, null, null, null, null, null, null, false, true);
    assertNotNull(searchResult);
    List<TimeSeriesDocument<T>> documents = searchResult.getDocuments();
    assertNotNull(documents);
    assertTrue(tsList.size() == documents.size());
    for (TimeSeriesDocument<T> tsDoc : tsList) {
      tsDoc.setEarliest(tsDoc.getTimeSeries().getEarliestTime());
      tsDoc.setLatest(tsDoc.getTimeSeries().getLatestTime());
      tsDoc.setTimeSeries(null);
      assertTrue(documents.contains(tsDoc));
    }
  }
  
  @Test
  public void addDataPoint() throws Exception {
    List<TimeSeriesDocument<T>> tsList = addAndTestTimeSeries();
    for (TimeSeriesDocument<T> tsDoc : tsList) {
      DoubleTimeSeries<T> timeSeries = tsDoc.getTimeSeries();
      //add datapoint
      T latestTime = timeSeries.getLatestTime();
      LocalDate date = convert(latestTime).plusDays(1);
      while (!isWeekday(date)) {
        date = date.plusDays(1);
      }
      double value = Math.random();
      List<T> dates = timeSeries.times();
      List<Double> values = timeSeries.values();
      dates.add(convert(date));
      values.add(value);
      DoubleTimeSeries<T> updatedTS = getTimeSeries(dates, values); 
      
      String scheme = tsDoc.getUniqueId().getScheme();
      String timeSeriesID = tsDoc.getUniqueId().getValue();
      DataPointDocument<T> dataPointDocument = new DataPointDocument<T>();
      dataPointDocument.setTimeSeriesId(tsDoc.getUniqueId());
      dataPointDocument.setDate(convert(date));
      dataPointDocument.setValue(value);
      
      dataPointDocument = _tsMaster.addDataPoint(dataPointDocument);
      assertNotNull(dataPointDocument);
      assertEquals(UniqueIdentifier.of(scheme, timeSeriesID + "/" + print(convert(date))), dataPointDocument.getDataPointId());
      TimeSeriesDocument<T> updatedDoc = _tsMaster.getTimeSeries(tsDoc.getUniqueId());
      assertNotNull(updatedDoc);
      assertNotNull(updatedDoc.getUniqueId());
      assertEquals(updatedTS, updatedDoc.getTimeSeries());
      
      DataPointDocument<T> actualDDoc = _tsMaster.getDataPoint(dataPointDocument.getDataPointId());
      assertEquals(tsDoc.getUniqueId(), actualDDoc.getTimeSeriesId());
      assertEquals(dataPointDocument.getDataPointId(), actualDDoc.getDataPointId());
      assertEquals(dataPointDocument.getDate(), actualDDoc.getDate());
      assertEquals(dataPointDocument.getValue(), actualDDoc.getValue());
    }
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void addDataPointWithoutTSID() throws Exception {
    DataPointDocument<T> dataPointDocument = new DataPointDocument<T>();
    dataPointDocument.setDate(convert(LocalDate.of(2000, 1, 2)));
    dataPointDocument.setValue(Math.random());
    dataPointDocument = _tsMaster.addDataPoint(dataPointDocument);
  }
  
  
  @Test
  public void updateDataPoint() throws Exception {
    List<TimeSeriesDocument<T>> tsList = addAndTestTimeSeries();
    for (TimeSeriesDocument<T> tsDoc : tsList) {
      DoubleTimeSeries<T> timeSeries = tsDoc.getTimeSeries();
      //update datapoint
      List<T> dates = timeSeries.times();
      List<Double> values = timeSeries.values();
      int updateIdx = _random.nextInt(timeSeries.size());
      T date = timeSeries.getTime(updateIdx);
      double newValue = _random.nextDouble();
      values.set(updateIdx, newValue);
      
      DoubleTimeSeries<T> updatedTS = getTimeSeries(dates, values);
      
      String scheme = tsDoc.getUniqueId().getScheme();
      String timeSeriesID = tsDoc.getUniqueId().getValue();
      DataPointDocument<T> dataPointDocument = new DataPointDocument<T>();
      dataPointDocument.setTimeSeriesId(tsDoc.getUniqueId());
      dataPointDocument.setDataPointId(UniqueIdentifier.of(scheme, timeSeriesID + "/" + print(date)));
      dataPointDocument.setDate(date);
      dataPointDocument.setValue(newValue);
      
      DataPointDocument<T> updated = _tsMaster.updateDataPoint(dataPointDocument);
      assertNotNull(updated);
      assertEquals(dataPointDocument.getDataPointId(), updated.getDataPointId());
      assertEquals(dataPointDocument.getTimeSeriesId(), updated.getTimeSeriesId());
      assertEquals(dataPointDocument.getDate(), updated.getDate());
      assertEquals(dataPointDocument.getValue(), updated.getValue());
      
      TimeSeriesDocument<T> updatedDoc = _tsMaster.getTimeSeries(tsDoc.getUniqueId());
      assertNotNull(updatedDoc);
      assertNotNull(updatedDoc.getUniqueId());
      assertEquals(updatedTS, updatedDoc.getTimeSeries());
    }
    
  }
  
  @Test
  public void removeDataPoint() throws Exception {
    List<TimeSeriesDocument<T>> tsList = addAndTestTimeSeries();
    for (TimeSeriesDocument<T> tsDocument : tsList) {
      DoubleTimeSeries<T> timeSeries = tsDocument.getTimeSeries();
      //delete random datapoints
      List<T> dates = timeSeries.times();
      List<Double> values = timeSeries.values();
      int deleteIdx = _random.nextInt(timeSeries.size());
      T deletedDate = dates.remove(deleteIdx);
      values.remove(deleteIdx);
      
      DoubleTimeSeries<T> deletedTS = getTimeSeries(dates, values);
      String scheme = tsDocument.getUniqueId().getScheme();
      String tsId = tsDocument.getUniqueId().getValue();
      _tsMaster.removeDataPoint(UniqueIdentifier.of(scheme, tsId + "/" + print(deletedDate)));
      
      TimeSeriesDocument<T> updatedDoc = _tsMaster.getTimeSeries(tsDocument.getUniqueId());
      assertNotNull(updatedDoc);
      assertNotNull(updatedDoc.getUniqueId());
      assertEquals(deletedTS, updatedDoc.getTimeSeries());
    }
  }
  
  @Test
  public void removeDataPoints() throws Exception {
    List<TimeSeriesDocument<T>> tsList = addAndTestTimeSeries();
    for (TimeSeriesDocument<T> tsDocument : tsList) {
      DoubleTimeSeries<T> timeSeries = tsDocument.getTimeSeries();
      int originalSize = timeSeries.size();
      int desiredSize = originalSize / 2;
      T firstDateToRetain = timeSeries.getTime(timeSeries.size() - desiredSize);
      _tsMaster.removeDataPoints(tsDocument.getUniqueId(), firstDateToRetain);
      
      TimeSeriesDocument<T> updatedDoc = _tsMaster.getTimeSeries(tsDocument.getUniqueId());

      assertNotNull(updatedDoc);
      assertNotNull(updatedDoc.getUniqueId());
      
      assertEquals(desiredSize, updatedDoc.getTimeSeries().size());
      assertEquals(firstDateToRetain, updatedDoc.getTimeSeries().getEarliestTime());
      assertEquals(timeSeries.getLatestTime(), updatedDoc.getTimeSeries().getLatestTime());
    }
  }
    
  private void assertEqualTimeSeriesDocument(TimeSeriesDocument<T> expectedDoc, TimeSeriesDocument<T> actualDoc) {
    assertNotNull(expectedDoc);
    assertNotNull(actualDoc);
    assertEquals(expectedDoc.getUniqueId(), actualDoc.getUniqueId());
    assertEquals(expectedDoc.getTimeSeries(), actualDoc.getTimeSeries());
    assertEquals(expectedDoc.getDataField(), actualDoc.getDataField());
    assertEquals(expectedDoc.getDataProvider(), actualDoc.getDataProvider());
    assertEquals(expectedDoc.getDataSource(), actualDoc.getDataSource());
    assertEquals(expectedDoc.getIdentifiers(), actualDoc.getIdentifiers());
    assertEquals(expectedDoc.getObservationTime(), actualDoc.getObservationTime());
  }
  
  public DoubleTimeSeries<T> makeRandomTimeSeries(int numDays) {
    LocalDate previousWeekDay = DateUtil.previousWeekDay();
    return makeRandomTimeSeries(previousWeekDay, numDays);
  }
  
  public static MapLocalDateDoubleTimeSeries makeRandomTimeSeriesStatic(int numDays) {
    LocalDate previousWeekDay = DateUtil.previousWeekDay();
    return RandomTimeSeriesGenerator.makeRandomTimeSeries(previousWeekDay, numDays);
  }
  
  public DoubleTimeSeries<T> makeRandomTimeSeries(LocalDate start, int numDays) {
    MapLocalDateDoubleTimeSeries tsMap = RandomTimeSeriesGenerator.makeRandomTimeSeries(start, numDays);
    return getTimeSeries(tsMap);
  }
  
  private static boolean isWeekday(LocalDate day) {
    return (day.getDayOfWeek() != DayOfWeek.SATURDAY && day.getDayOfWeek() != DayOfWeek.SUNDAY);
  }
  
  @Test
  public void identifiersWithDates() throws Exception {
    addAndTestTimeSeries();
    
    Map<Identifier, DoubleTimeSeries<T>> expectedTS = new HashMap<Identifier, DoubleTimeSeries<T>>();
    
    //add EDU10 Comdty
    Identifier edu10Buid = SecurityUtils.bloombergBuidSecurityId("IX613196-0");
    Identifier edu0Id = SecurityUtils.bloombergTickerSecurityId("EDU0 Comdty");
    TimeSeriesDocument<T> tsDocument = new TimeSeriesDocument<T>();
    tsDocument.setDataField(CLOSE_DATA_FIELD);
    tsDocument.setDataProvider(CMPL_DATA_PROVIDER);
    tsDocument.setDataSource(BBG_DATA_SOURCE);
    tsDocument.setObservationTime(LCLOSE_OBSERVATION_TIME);
    IdentifierWithDates edu0 = IdentifierWithDates.of(edu0Id, LocalDate.of(2000, MonthOfYear.SEPTEMBER, 19), LocalDate.of(2010, MonthOfYear.SEPTEMBER, 13));
    IdentifierWithDates edu10 = IdentifierWithDates.of(SecurityUtils.bloombergTickerSecurityId("EDU10 Comdty"), LocalDate.of(2010, MonthOfYear.SEPTEMBER, 14), null);
    IdentifierWithDates eduBuid = IdentifierWithDates.of(edu10Buid, null, null);
    tsDocument.setIdentifiers(IdentifierBundleWithDates.of(new IdentifierWithDates[]{edu0, edu10, eduBuid}));
    
    LocalDate start = DateUtil.previousWeekDay().minusDays(7);
    
    DoubleTimeSeries<T> timeSeries = makeRandomTimeSeries(start, 7);
    assertTrue(timeSeries.size() == 7);
    assertEquals(convert(start), timeSeries.getEarliestTime());
    tsDocument.setTimeSeries(timeSeries);
    
    tsDocument = _tsMaster.addTimeSeries(tsDocument);
    
    assertNotNull(tsDocument);
    assertNotNull(tsDocument.getUniqueId());
    
    TimeSeriesDocument<T> actualDoc = _tsMaster.getTimeSeries(tsDocument.getUniqueId());
    assertNotNull(actualDoc);
    assertEquals(timeSeries, actualDoc.getTimeSeries());
    expectedTS.put(edu10Buid, timeSeries);
    
    //add EDU20 Comdty
    Identifier edu20Buid = SecurityUtils.bloombergBuidSecurityId("IX11084074-0");
    tsDocument = new TimeSeriesDocument<T>();
    tsDocument.setDataField(CLOSE_DATA_FIELD);
    tsDocument.setDataProvider(CMPL_DATA_PROVIDER);
    tsDocument.setDataSource(BBG_DATA_SOURCE);
    tsDocument.setObservationTime(LCLOSE_OBSERVATION_TIME);
    edu0 = IdentifierWithDates.of(edu0Id, LocalDate.of(2010, MonthOfYear.SEPTEMBER, 14), LocalDate.of(2020, MonthOfYear.SEPTEMBER, 14));
    IdentifierWithDates edu20 = IdentifierWithDates.of(SecurityUtils.bloombergTickerSecurityId("EDU20 Comdty"), LocalDate.of(2010, MonthOfYear.SEPTEMBER, 15), null);
    eduBuid = IdentifierWithDates.of(edu20Buid, null, null);
    tsDocument.setIdentifiers(IdentifierBundleWithDates.of(new IdentifierWithDates[]{edu0, edu20, eduBuid}));
    
    start = DateUtil.previousWeekDay().minusDays(7);
    
    timeSeries = makeRandomTimeSeries(start, 7);
    assertTrue(timeSeries.size() == 7);
    assertEquals(convert(start), timeSeries.getEarliestTime());
    tsDocument.setTimeSeries(timeSeries);
    
    tsDocument = _tsMaster.addTimeSeries(tsDocument);
    
    assertNotNull(tsDocument);
    assertNotNull(tsDocument.getUniqueId());
    
    actualDoc = _tsMaster.getTimeSeries(tsDocument.getUniqueId());
    assertNotNull(actualDoc);
    assertEquals(timeSeries, actualDoc.getTimeSeries());
    expectedTS.put(edu20Buid, timeSeries);
    
    //------------------------------------------------------------------------
    //lookup using edu0 with current date
    
    LocalDate validFrom = LocalDate.of(2000, MonthOfYear.SEPTEMBER, 19);
    LocalDate validTo = LocalDate.of(2010, MonthOfYear.SEPTEMBER, 13);
    
    //search before edu0
    TimeSeriesSearchResult<T> searchResult = search(validFrom.minusDays(1), null, IdentifierBundle.of(edu0Id), CLOSE_DATA_FIELD, CMPL_DATA_PROVIDER, BBG_DATA_SOURCE, LCLOSE_OBSERVATION_TIME, true, false);
    assertNotNull(searchResult);
    List<TimeSeriesDocument<T>> documents = searchResult.getDocuments();
    assertNotNull(documents);
    assertTrue(documents.isEmpty());
    
    List<LocalDate> datesToLookup = new ArrayList<LocalDate>();
    //start
    datesToLookup.add(validFrom);
    datesToLookup.add(validFrom.plusDays(1));
    //end
    datesToLookup.add(validTo);
    datesToLookup.add(validTo.minusDays(1));
    //midpoint
    datesToLookup.add(validFrom.plusYears(5));
    
    for (LocalDate currentDate : datesToLookup) {
      searchResult = search(currentDate, null, IdentifierBundle.of(edu0Id), CLOSE_DATA_FIELD, CMPL_DATA_PROVIDER, BBG_DATA_SOURCE, LCLOSE_OBSERVATION_TIME, true, false);
      assertNotNull(searchResult);
      documents = searchResult.getDocuments();
      assertNotNull(documents);
      assertTrue(documents.size() == 1);
      TimeSeriesDocument<T> tsDoc = documents.get(0);
      assertEquals(expectedTS.get(edu10Buid), tsDoc.getTimeSeries());
      
      searchResult = search(currentDate, null, IdentifierBundle.of(edu0Id, edu10Buid), CLOSE_DATA_FIELD, CMPL_DATA_PROVIDER, BBG_DATA_SOURCE, LCLOSE_OBSERVATION_TIME, true, false);
      assertNotNull(searchResult);
      documents = searchResult.getDocuments();
      assertNotNull(documents);
      assertTrue(documents.size() == 1);
      tsDoc = documents.get(0);
      assertEquals(expectedTS.get(edu10Buid), tsDoc.getTimeSeries());
      
    }
    
    //search a day after valid_to of edu0 should return edu20 series
    searchResult = search(validTo.plusDays(1), null, IdentifierBundle.of(edu0Id), CLOSE_DATA_FIELD, CMPL_DATA_PROVIDER, BBG_DATA_SOURCE, LCLOSE_OBSERVATION_TIME, true, false);
    assertNotNull(searchResult);
    documents = searchResult.getDocuments();
    assertNotNull(documents);
    assertTrue(documents.size() == 1);
    TimeSeriesDocument<T> tsDoc = documents.get(0);
    assertEquals(expectedTS.get(edu20Buid), tsDoc.getTimeSeries());
    
    //search after edu20 should return no series
    searchResult = search(LocalDate.of(2020, MonthOfYear.SEPTEMBER, 15), null, IdentifierBundle.of(edu0Id), CLOSE_DATA_FIELD, CMPL_DATA_PROVIDER, BBG_DATA_SOURCE, LCLOSE_OBSERVATION_TIME, true, false);
    assertNotNull(searchResult);
    documents = searchResult.getDocuments();
    assertNotNull(documents);
    assertTrue(documents.isEmpty());
    
    //search using buids should return correct series
    searchResult = search(null, null, IdentifierBundle.of(edu10Buid), CLOSE_DATA_FIELD, CMPL_DATA_PROVIDER, BBG_DATA_SOURCE, LCLOSE_OBSERVATION_TIME, true, false);
    assertNotNull(searchResult);
    assertNotNull(searchResult.getDocuments());
    assertTrue(searchResult.getDocuments().size() == 1);
    assertEquals(expectedTS.get(edu10Buid), searchResult.getDocuments().get(0).getTimeSeries());
    
    searchResult = search(null, null, IdentifierBundle.of(edu20Buid), CLOSE_DATA_FIELD, CMPL_DATA_PROVIDER, BBG_DATA_SOURCE, LCLOSE_OBSERVATION_TIME, true, false);
    assertNotNull(searchResult);
    assertNotNull(searchResult.getDocuments());
    assertTrue(searchResult.getDocuments().size() == 1);
    assertEquals(expectedTS.get(edu20Buid), searchResult.getDocuments().get(0).getTimeSeries());
    
    //search using edu0 without current date should return 2 series
    searchResult = search(null, null, IdentifierBundle.of(edu0Id), CLOSE_DATA_FIELD, CMPL_DATA_PROVIDER, BBG_DATA_SOURCE, LCLOSE_OBSERVATION_TIME, true, false);
    assertNotNull(searchResult);
    assertNotNull(searchResult.getDocuments());
    assertTrue(searchResult.getDocuments().size() == 2);
    DoubleTimeSeries<T> ts1 = searchResult.getDocuments().get(0).getTimeSeries();
    DoubleTimeSeries<T> ts2 = searchResult.getDocuments().get(1).getTimeSeries();
    assertFalse(ts1.equals(ts2));
    assertTrue(expectedTS.values().contains(ts1));
    assertTrue(expectedTS.values().contains(ts2));
    
    //search edu10 without date
    searchResult = search(null, null, IdentifierBundle.of(SecurityUtils.bloombergTickerSecurityId("EDU10 Comdty")), CLOSE_DATA_FIELD, CMPL_DATA_PROVIDER, BBG_DATA_SOURCE, LCLOSE_OBSERVATION_TIME, true, false);
    assertNotNull(searchResult);
    assertNotNull(searchResult.getDocuments());
    assertTrue(searchResult.getDocuments().size() == 1);
    assertEquals(expectedTS.get(edu10Buid), searchResult.getDocuments().get(0).getTimeSeries());
    
    //search edu20 without date
    searchResult = search(null, null, IdentifierBundle.of(SecurityUtils.bloombergTickerSecurityId("EDU20 Comdty")), CLOSE_DATA_FIELD, CMPL_DATA_PROVIDER, BBG_DATA_SOURCE, LCLOSE_OBSERVATION_TIME, true, false);
    assertNotNull(searchResult);
    assertNotNull(searchResult.getDocuments());
    assertTrue(searchResult.getDocuments().size() == 1);
    assertEquals(expectedTS.get(edu20Buid), searchResult.getDocuments().get(0).getTimeSeries());
  }
}