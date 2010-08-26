/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.time.calendar.Clock;
import javax.time.calendar.DayOfWeek;
import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.IdentificationScheme;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.timeseries.DataFieldBean;
import com.opengamma.timeseries.DataPointDocument;
import com.opengamma.timeseries.DataProviderBean;
import com.opengamma.timeseries.DataSourceBean;
import com.opengamma.timeseries.ObservationTimeBean;
import com.opengamma.timeseries.SchemeBean;
import com.opengamma.timeseries.TimeSeriesDocument;
import com.opengamma.timeseries.TimeSeriesMaster;
import com.opengamma.timeseries.TimeSeriesSearchHistoricRequest;
import com.opengamma.timeseries.TimeSeriesSearchHistoricResult;
import com.opengamma.timeseries.TimeSeriesSearchRequest;
import com.opengamma.timeseries.TimeSeriesSearchResult;
import com.opengamma.util.test.DBTest;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.FastBackedDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.MapLocalDateDoubleTimeSeries;

/**
 * Test.
 */
public class TimeSeriesMasterTest extends DBTest {
  private static final Logger s_logger = LoggerFactory.getLogger(TimeSeriesMasterTest.class);
  
  private static final int TS_DATASET_SIZE = 1;

  private static final String LCLOSE_OBSERVATION_TIME = "LCLOSE";
  private static final String CLOSE_DATA_FIELD = "CLOSE";
  private static final String CMPL_DATA_PROVIDER = "CMPL";
  private static final String BBG_DATA_SOURCE = "BLOOMBERG";
  
  private static final String[] DATA_FIELDS = new String[] { CLOSE_DATA_FIELD, "VOLUME" };
  private static final String[] DATA_PROVIDERS = new String[] { "UNKNOWN", CMPL_DATA_PROVIDER, "CMPT" };
  private static final String[] DATA_SOURCES = new String[] { BBG_DATA_SOURCE, "REUTERS", "JPM" };

  private static final LocalDate DEFAULT_START = DateUtil.previousWeekDay().minusDays(7);
  private static final LocalDate DEFAULT_END = DateUtil.previousWeekDay();

  private Random _random = new Random();
  private TimeSeriesMaster _tsMaster;
  
  public TimeSeriesMasterTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }
  
  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    super.setUp();
    String fileSeparator = System.getProperty("file.separator");
    String contextLocation =  "config" + fileSeparator + "test-historical-dao-context.xml";
    ApplicationContext context = new FileSystemXmlApplicationContext(contextLocation);

    TimeSeriesMaster ts = (TimeSeriesMaster) context.getBean(getDatabaseType()+"Dao");
    _tsMaster = ts;
    
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
    _tsMaster = null;
  }
  
  @Test
  public void createDataSource() throws Exception {
    
    DataSourceBean ds1 = _tsMaster.getOrCreateDataSource("DS1", "DS1");
    assertNotNull(ds1);
    assertNotNull(ds1.getId());
    assertEquals("DS1", ds1.getName());
    assertEquals("DS1", ds1.getDescription());
    
    DataSourceBean ds2 = _tsMaster.getOrCreateDataSource("DS2", "DS2");
    assertNotNull(ds2);
    assertNotNull(ds2.getId());
    assertEquals("DS2", ds2.getName());
    assertEquals("DS2", ds2.getDescription());
    
    List<DataSourceBean> dataSources = _tsMaster.getDataSources();
    assertNotNull(dataSources);
    assertTrue(dataSources.size() == 2);
    assertTrue(dataSources.contains(ds1));
    assertTrue(dataSources.contains(ds2));
    
    _tsMaster.getOrCreateDataSource("DS1", "DS1");
    _tsMaster.getOrCreateDataSource("DS2", "DS2");
    dataSources = _tsMaster.getDataSources();
    assertNotNull(dataSources);
    assertTrue(dataSources.size() == 2);
    
  }

  @Test
  public void createDataProvider() throws Exception {
    DataProviderBean dp1 = _tsMaster.getOrCreateDataProvider("DP1", "DP1");
    assertNotNull(dp1);
    assertNotNull(dp1.getId());
    assertEquals("DP1", dp1.getName());
    assertEquals("DP1", dp1.getDescription());
    
    DataProviderBean dp2 = _tsMaster.getOrCreateDataProvider("DP2", "DP2");
    assertNotNull(dp2);
    assertNotNull(dp2.getId());
    assertEquals("DP2", dp2.getName());
    assertEquals("DP2", dp2.getDescription());
    
    List<DataProviderBean> dataProviders = _tsMaster.getDataProviders();
    assertNotNull(dataProviders);
    assertTrue(dataProviders.size() == 2);
    assertTrue(dataProviders.contains(dp1));
    assertTrue(dataProviders.contains(dp2));
    
    _tsMaster.getOrCreateDataProvider("DP1", "DP1");
    _tsMaster.getOrCreateDataProvider("DP2", "DP2");
    dataProviders = _tsMaster.getDataProviders();
    assertNotNull(dataProviders);
    assertTrue(dataProviders.size() == 2);
        
  }
  
  @Test
  public void createDataField() throws Exception {
    
    DataFieldBean df1 = _tsMaster.getOrCreateDataField("TSF1", "TSF1");
    assertNotNull(df1);
    assertNotNull(df1.getId());
    assertEquals("TSF1", df1.getName());
    assertEquals("TSF1", df1.getDescription());
    
    DataFieldBean df2 = _tsMaster.getOrCreateDataField("TSF2", "TSF2");
    assertNotNull(df2);
    assertNotNull(df2.getId());
    assertEquals("TSF2", df2.getName());
    assertEquals("TSF2", df2.getDescription());
    
    List<DataFieldBean> dataFields = _tsMaster.getDataFields();
    assertNotNull(dataFields);
    assertTrue(dataFields.size() == 2);
    assertTrue(dataFields.contains(df1));
    assertTrue(dataFields.contains(df2));
    
    _tsMaster.getOrCreateDataField("TSF1", "TSF1");
    _tsMaster.getOrCreateDataField("TSF2", "TSF2");
    dataFields = _tsMaster.getDataFields();
    assertNotNull(dataFields);
    assertTrue(dataFields.size() == 2);
  }
  
  @Test
  public void createObservationTime() throws Exception {
    
    ObservationTimeBean b1 = _tsMaster.getOrCreateObservationTime("OBT1", "OBT1");
    assertNotNull(b1);
    assertNotNull(b1.getId());
    assertEquals("OBT1", b1.getName());
    assertEquals("OBT1", b1.getDescription());
    
    ObservationTimeBean b2 = _tsMaster.getOrCreateObservationTime("OBT2", "OBT2");
    assertNotNull(b2);
    assertNotNull(b2.getId());
    assertEquals("OBT2", b2.getName());
    assertEquals("OBT2", b2.getDescription());
    
    List<ObservationTimeBean> observationTimes = _tsMaster.getObservationTimes();
    assertNotNull(observationTimes);
    assertTrue(observationTimes.size() == 2);
    assertTrue(observationTimes.contains(b1));
    assertTrue(observationTimes.contains(b2));
    
    _tsMaster.getOrCreateObservationTime("OBT1", "OBT1");
    _tsMaster.getOrCreateObservationTime("OBT2", "OBT2");
    observationTimes = _tsMaster.getObservationTimes();
    assertNotNull(observationTimes);
    assertTrue(observationTimes.size() == 2);
    
  }
  
  
  @Test
  public void createScheme() throws Exception {
    
    SchemeBean b1 = _tsMaster.getOrCreateScheme("SCH1", "SCH1");
    assertNotNull(b1);
    assertNotNull(b1.getId());
    assertEquals("SCH1", b1.getName());
    assertEquals("SCH1", b1.getDescription());
    
    SchemeBean b2 = _tsMaster.getOrCreateScheme("SCH2", "SCH2");
    assertNotNull(b2);
    assertNotNull(b2.getId());
    assertEquals("SCH2", b2.getName());
    assertEquals("SCH2", b2.getDescription());
    
    List<SchemeBean> enums = _tsMaster.getSchemes();
    assertNotNull(enums);
    assertTrue(enums.size() == 2);
    assertTrue(enums.contains(b1));
    assertTrue(enums.contains(b2));
    
    _tsMaster.getOrCreateScheme("SCH1", "SCH1");
    _tsMaster.getOrCreateScheme("SCH2", "SCH2");
    enums = _tsMaster.getSchemes();
    assertNotNull(enums);
    assertTrue(enums.size() == 2);
    
  }
  
  @Test
  public void getAllIdentifiers() throws Exception {
    
    List<IdentifierBundle> allIdentifiers = _tsMaster.getAllIdentifiers();
    assertNotNull(allIdentifiers);
    assertTrue(allIdentifiers.isEmpty());
    
    List<IdentifierBundle> expectedIds = new ArrayList<IdentifierBundle>();
    for (int i = 0; i < TS_DATASET_SIZE; i++) {
      Identifier id1 = Identifier.of("sa" + i, "ida" + i);
      Identifier id2 = Identifier.of("sb" + i, "idb" + i);
      IdentifierBundle identifiers = IdentifierBundle.of(id1, id2);
      LocalDate previousWeekDay = DateUtil.previousWeekDay();
      LocalDateDoubleTimeSeries timeSeries = makeRandomTimeSeries(previousWeekDay.minusDays(7), previousWeekDay);
      expectedIds.add(identifiers);
      
      TimeSeriesDocument tsDocument = new TimeSeriesDocument();
      tsDocument.setDataField(CLOSE_DATA_FIELD);
      tsDocument.setDataProvider(CMPL_DATA_PROVIDER);
      tsDocument.setDataSource(BBG_DATA_SOURCE);
      tsDocument.setObservationTime(LCLOSE_OBSERVATION_TIME);
      tsDocument.setIdentifiers(identifiers);
      tsDocument.setTimeSeries(timeSeries);
      
      tsDocument = _tsMaster.addTimeSeries(tsDocument);
      
      assertNotNull(tsDocument);
      assertNotNull(tsDocument.getUniqueIdentifier());
    }
    
    allIdentifiers = _tsMaster.getAllIdentifiers();
    assertNotNull(allIdentifiers);
    assertTrue(allIdentifiers.size() == expectedIds.size());
    for (IdentifierBundle identifierBundle : expectedIds) {
      assertTrue(allIdentifiers.contains(identifierBundle));
    }
    
  }
  
  @Test
  public void searchByIdentifierBundle() throws Exception {
    List<TimeSeriesDocument> expectedTS = addAndTestTimeSeries();
    for (TimeSeriesDocument expectedTSDoc : expectedTS) {
      TimeSeriesSearchRequest request = new TimeSeriesSearchRequest();
      request.getIdentifiers().addAll(expectedTSDoc.getIdentifiers().getIdentifiers());
      request.setDataField(expectedTSDoc.getDataField());
      request.setDataProvider(expectedTSDoc.getDataProvider());
      request.setDataSource(expectedTSDoc.getDataSource());
      request.setObservationTime(expectedTSDoc.getObservationTime());
      request.setLoadTimeSeries(true);
      
      TimeSeriesSearchResult searchResult = _tsMaster.searchTimeSeries(request);
      assertNotNull(searchResult);
      List<TimeSeriesDocument> documents = searchResult.getDocuments();
      assertNotNull(documents);
      assertTrue(documents.size() == 1);
      
      TimeSeriesDocument searchedDoc = documents.get(0);
      assertNotNull(searchedDoc);
      
      assertEqualTimeSeriesDocument(expectedTSDoc, searchedDoc);
    }
  }
  
  @Test
  public void searchByFieldProviderSource() throws Exception {
    List<TimeSeriesDocument> tsList = addAndTestTimeSeries();
    for (TimeSeriesDocument tsDoc : tsList) {
      TimeSeriesSearchRequest request = new TimeSeriesSearchRequest();
      request.getIdentifiers().addAll(tsDoc.getIdentifiers().getIdentifiers());
      request.setDataField(tsDoc.getDataField());
      request.setDataProvider(tsDoc.getDataProvider());
      request.setDataSource(tsDoc.getDataSource());
      request.setObservationTime(tsDoc.getObservationTime());
      request.setLoadTimeSeries(true);
      
      TimeSeriesSearchResult searchResult = _tsMaster.searchTimeSeries(request);
      assertNotNull(searchResult);
      List<TimeSeriesDocument> documents = searchResult.getDocuments();
      assertNotNull(documents);
      assertTrue(documents.size() == 1);
      
      assertEqualTimeSeriesDocument(tsDoc, documents.get(0));
    }
  }
  
  @Test
  public void searchByUID() throws Exception {
    List<TimeSeriesDocument> tsList = addAndTestTimeSeries();
    for (TimeSeriesDocument tsDoc : tsList) {
      TimeSeriesSearchRequest request = new TimeSeriesSearchRequest();
      request.setTimeSeriesId(tsDoc.getUniqueIdentifier());
      request.setLoadTimeSeries(true);
      
      TimeSeriesSearchResult searchResult = _tsMaster.searchTimeSeries(request);
      assertNotNull(searchResult);
      List<TimeSeriesDocument> documents = searchResult.getDocuments();
      assertNotNull(documents);
      assertTrue(documents.size() == 1);
      
      assertEqualTimeSeriesDocument(tsDoc, documents.get(0));
    }
  }
  
  @Test
  public void addTimeSeries() throws Exception {
    addAndTestTimeSeries();    
  }
  
  private List<TimeSeriesDocument> addAndTestTimeSeries() {
    List<TimeSeriesDocument> result = new ArrayList<TimeSeriesDocument>(); 
    for (int i = 0; i < TS_DATASET_SIZE; i++) {
      IdentifierBundle identifiers = IdentifierBundle.of(Identifier.of(IdentificationScheme.BLOOMBERG_TICKER, "ticker" + i), Identifier.of(IdentificationScheme.BLOOMBERG_BUID, "buid" + i));
      LocalDate previousWeekDay = DateUtil.previousWeekDay();
      LocalDate start = previousWeekDay.minusDays(7);
      LocalDate end = previousWeekDay;
      
      for (String dataSource : DATA_SOURCES) {
        for (String dataProvider : DATA_PROVIDERS) {
          for (String datafield : DATA_FIELDS) {
            TimeSeriesDocument tsDocument = new TimeSeriesDocument();
            tsDocument.setDataField(datafield);
            tsDocument.setDataProvider(dataProvider);
            tsDocument.setDataSource(dataSource);
            tsDocument.setObservationTime(LCLOSE_OBSERVATION_TIME);
            tsDocument.setIdentifiers(identifiers);
            LocalDateDoubleTimeSeries timeSeries = makeRandomTimeSeries(start, end);
            assertEquals(start, timeSeries.getEarliestTime());
            assertEquals(end, timeSeries.getLatestTime());
            tsDocument.setTimeSeries(timeSeries);
            
            tsDocument = _tsMaster.addTimeSeries(tsDocument);
            
            assertNotNull(tsDocument);
            assertNotNull(tsDocument.getUniqueIdentifier());
            
            TimeSeriesDocument actualDoc = _tsMaster.getTimeSeries(tsDocument.getUniqueIdentifier());
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
    LocalDateDoubleTimeSeries timeSeries = makeRandomTimeSeries(DEFAULT_START, DEFAULT_END);
    
    TimeSeriesDocument tsDocument = new TimeSeriesDocument();
    tsDocument.setDataField(CLOSE_DATA_FIELD);
    tsDocument.setDataProvider(CMPL_DATA_PROVIDER);
    tsDocument.setDataSource(BBG_DATA_SOURCE);
    tsDocument.setObservationTime(LCLOSE_OBSERVATION_TIME);
    tsDocument.setIdentifiers(identifiers);
    tsDocument.setTimeSeries(timeSeries);
    
    tsDocument = _tsMaster.addTimeSeries(tsDocument);
    
    assertNotNull(tsDocument);
    assertNotNull(tsDocument.getUniqueIdentifier());
    
    TimeSeriesDocument actualDoc = _tsMaster.getTimeSeries(tsDocument.getUniqueIdentifier());
    assertNotNull(actualDoc);
    
    assertEqualTimeSeriesDocument(tsDocument, actualDoc);
    
    //try and add another using the same meta data and identifiers
    TimeSeriesDocument otherDoc = new TimeSeriesDocument();
    otherDoc.setDataField(CLOSE_DATA_FIELD);
    otherDoc.setDataProvider(CMPL_DATA_PROVIDER);
    otherDoc.setDataSource(BBG_DATA_SOURCE);
    otherDoc.setObservationTime(LCLOSE_OBSERVATION_TIME);
    otherDoc.setIdentifiers(identifiers);
    otherDoc.setTimeSeries(makeRandomTimeSeries(DEFAULT_START, DEFAULT_END));
    try {
      _tsMaster.addTimeSeries(otherDoc);
      fail();
    } catch (IllegalArgumentException ex) {
      //do nothing
    }
  }
  
  @Test
  public void updateTimeSeries() throws Exception {
    List<TimeSeriesDocument> tsList = addAndTestTimeSeries();
    for (TimeSeriesDocument tsDoc : tsList) {
      tsDoc.setTimeSeries(makeRandomTimeSeries(DEFAULT_START, DEFAULT_END));
      TimeSeriesDocument updatedDoc = _tsMaster.updateTimeSeries(tsDoc);
      assertNotNull(updatedDoc);
      assertNotNull(updatedDoc.getUniqueIdentifier());
      assertEquals(tsDoc.getUniqueIdentifier(), updatedDoc.getUniqueIdentifier());
      
      assertEqualTimeSeriesDocument(updatedDoc, _tsMaster.getTimeSeries(updatedDoc.getUniqueIdentifier()));
      
      //delete dataPoints, set with empty timeseries
      tsDoc.setTimeSeries(new ArrayLocalDateDoubleTimeSeries());
      updatedDoc = _tsMaster.updateTimeSeries(tsDoc);
      assertNotNull(updatedDoc);
      assertNotNull(updatedDoc.getUniqueIdentifier());
      assertEquals(tsDoc.getUniqueIdentifier(), updatedDoc.getUniqueIdentifier());
      
      assertEqualTimeSeriesDocument(updatedDoc, _tsMaster.getTimeSeries(updatedDoc.getUniqueIdentifier()));
    }
  }
  
  @Test
  public void removeTimeSeries() throws Exception {
    List<TimeSeriesDocument> tsList = addAndTestTimeSeries();
    for (TimeSeriesDocument tsDoc : tsList) {
      _tsMaster.removeTimeSeries(tsDoc.getUniqueIdentifier());
      try {
        _tsMaster.getTimeSeries(tsDoc.getUniqueIdentifier());
        fail();
      } catch(DataNotFoundException ex) {
        //do nothing
      }
    }
  }
  
  @Test
  public void getUnknownUID() throws Exception {
    addAndTestTimeSeries();
    try {
      _tsMaster.getTimeSeries(UniqueIdentifier.of(RowStoreTimeSeriesMaster.IDENTIFIER_SCHEME_DEFAULT, String.valueOf(Long.MIN_VALUE)));
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
    List<TimeSeriesDocument> tsList = addAndTestTimeSeries();
    for (TimeSeriesDocument tsDoc : tsList) {
      IdentifierBundle identifiers = tsDoc.getIdentifiers();
      UniqueIdentifier resolveIdentifier = _tsMaster.resolveIdentifier(identifiers, tsDoc.getDataSource(), tsDoc.getDataProvider(), tsDoc.getDataField());
      assertNotNull(resolveIdentifier);
      assertEquals(tsDoc.getUniqueIdentifier(), resolveIdentifier);
      
      for (Identifier identifier : identifiers) {
        resolveIdentifier = _tsMaster.resolveIdentifier(IdentifierBundle.of(identifier), tsDoc.getDataSource(), tsDoc.getDataProvider(), tsDoc.getDataField());
        assertNotNull(resolveIdentifier);
        assertEquals(tsDoc.getUniqueIdentifier(), resolveIdentifier);
      }
      
      resolveIdentifier = _tsMaster.resolveIdentifier(identifiers, "INVALID", CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
      assertNull(resolveIdentifier);
      resolveIdentifier = _tsMaster.resolveIdentifier(identifiers, BBG_DATA_SOURCE, "INVALID", CLOSE_DATA_FIELD);
      assertNull(resolveIdentifier);
      
      resolveIdentifier = _tsMaster.resolveIdentifier(identifiers, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, "INVALID");
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
    Map<String, LocalDateDoubleTimeSeries> expectedTSMap = new HashMap<String, LocalDateDoubleTimeSeries>();
    
    IdentifierBundle bundle = IdentifierBundle.of(Identifier.of(IdentificationScheme.BLOOMBERG_TICKER, "id1"));
    for (String dataProvider : DATA_PROVIDERS) {
      
      LocalDateDoubleTimeSeries timeSeries = makeRandomTimeSeries(DEFAULT_START, DEFAULT_END);
      
      TimeSeriesDocument tsDocument = new TimeSeriesDocument();
      tsDocument.setDataField(CLOSE_DATA_FIELD);
      tsDocument.setDataProvider(dataProvider);
      tsDocument.setDataSource(BBG_DATA_SOURCE);
      tsDocument.setObservationTime(LCLOSE_OBSERVATION_TIME);
      tsDocument.setIdentifiers(bundle);
      tsDocument.setTimeSeries(timeSeries);
      
      tsDocument = _tsMaster.addTimeSeries(tsDocument);
      assertNotNull(tsDocument);
      assertNotNull(tsDocument.getUniqueIdentifier());
      
      expectedTSMap.put(dataProvider, timeSeries);
    }
    
    //check you get expected timeseries with dataProvider specified
    TimeSeriesSearchRequest request = new TimeSeriesSearchRequest();
    for (String dataProvider : DATA_PROVIDERS) {
      request.getIdentifiers().addAll(bundle.getIdentifiers());
      request.setDataSource(BBG_DATA_SOURCE);
      request.setDataProvider(dataProvider);
      request.setDataField(CLOSE_DATA_FIELD);
      request.setObservationTime(LCLOSE_OBSERVATION_TIME);
      request.setLoadTimeSeries(true);
      
      TimeSeriesSearchResult searchResult = _tsMaster.searchTimeSeries(request);
      
      assertNotNull(searchResult);
      List<TimeSeriesDocument> documents = searchResult.getDocuments();
      assertNotNull(documents);
      assertTrue(documents.size() == 1);
      
      TimeSeriesDocument searchedDoc = documents.get(0);
      assertNotNull(searchedDoc);
      
      assertEquals(expectedTSMap.get(dataProvider), searchedDoc.getTimeSeries());
    }
    
    //search without dataProvider
    request = new TimeSeriesSearchRequest();
    request.getIdentifiers().addAll(bundle.getIdentifiers());
    request.setDataSource(BBG_DATA_SOURCE);
    request.setDataField(CLOSE_DATA_FIELD);
    request.setObservationTime(LCLOSE_OBSERVATION_TIME);
    request.setLoadTimeSeries(true);
    
    TimeSeriesSearchResult searchResult = _tsMaster.searchTimeSeries(request);
    assertNotNull(searchResult);
    List<TimeSeriesDocument> documents = searchResult.getDocuments();
    assertNotNull(documents);
    assertTrue(documents.size() == expectedTSMap.entrySet().size());
    for (TimeSeriesDocument tsDoc : documents) {
      String dataProvider = tsDoc.getDataProvider();
      LocalDateDoubleTimeSeries actualTS = tsDoc.getTimeSeries();
      assertEquals(expectedTSMap.get(dataProvider), actualTS);
    }
        
  }
    
  @Test
  public void appendTimeSeries() throws Exception {
    List<TimeSeriesDocument> tsList = addAndTestTimeSeries();
    for (TimeSeriesDocument tsDoc : tsList) {
      LocalDateDoubleTimeSeries timeSeries = tsDoc.getTimeSeries();
      LocalDate start = timeSeries.getLatestTime().plusDays(1);
      LocalDate end = start.plusDays(7);
      LocalDateDoubleTimeSeries appendedTS = makeRandomTimeSeries(start, end);
      FastBackedDoubleTimeSeries<LocalDate> mergedTS = timeSeries.noIntersectionOperation(appendedTS);
      // append timeseries to existing identifiers in the datastore
      tsDoc.setTimeSeries(appendedTS);
      _tsMaster.appendTimeSeries(tsDoc);
      
      TimeSeriesDocument latestDoc = _tsMaster.getTimeSeries(tsDoc.getUniqueIdentifier());
      assertNotNull(latestDoc);
      tsDoc.setTimeSeries(mergedTS.toLocalDateDoubleTimeSeries());
      assertEqualTimeSeriesDocument(tsDoc, latestDoc);
    }
  }
  
  @Test
  public void searchNotAvailableTimeSeries() throws Exception {
    addAndTestTimeSeries();
    IdentifierBundle bundle = IdentifierBundle.of(Identifier.of("BLOOMBERG_TICKER", "AAPL US Equity"), Identifier.of("BUID", "X-12345678"));
    TimeSeriesSearchRequest request = new TimeSeriesSearchRequest();
    request.getIdentifiers().addAll(bundle.getIdentifiers());
    request.setDataField(CLOSE_DATA_FIELD);
    request.setDataProvider(CMPL_DATA_PROVIDER);
    request.setDataSource(BBG_DATA_SOURCE);
    request.setLoadTimeSeries(true);
    TimeSeriesSearchResult searchResult = _tsMaster.searchTimeSeries(request);
    assertNotNull(searchResult);
    assertNotNull(searchResult.getDocuments());
    assertTrue(searchResult.getDocuments().isEmpty());
  }
  
  @Test
  public void searchMetaData() throws Exception {
    List<TimeSeriesDocument> tsList = addAndTestTimeSeries();
    List<TimeSeriesDocument> closeDataFields = new ArrayList<TimeSeriesDocument>();
    List<TimeSeriesDocument> cmplDataProviders = new ArrayList<TimeSeriesDocument>();
    List<TimeSeriesDocument> bbgDataSources = new ArrayList<TimeSeriesDocument>();
    List<TimeSeriesDocument> lcloseObservations = new ArrayList<TimeSeriesDocument>();
    
    for (TimeSeriesDocument tsDoc : tsList) {
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
    TimeSeriesSearchRequest request = new TimeSeriesSearchRequest();
    request.setLoadTimeSeries(false); 
    //return all timeseries meta data without loading timeseries data points
    TimeSeriesSearchResult searchResult = _tsMaster.searchTimeSeries(request);
    assertNotNull(searchResult);
    List<TimeSeriesDocument> documents = searchResult.getDocuments();
    assertNotNull(documents);
    assertTrue(tsList.size() == documents.size());
    for (TimeSeriesDocument expectedDoc : tsList) {
      assertTrue(documents.contains(expectedDoc));
    }
    
    request = new TimeSeriesSearchRequest();
    request.setLoadTimeSeries(false);
    request.setDataField(CLOSE_DATA_FIELD);
    searchResult = _tsMaster.searchTimeSeries(request);
    documents = searchResult.getDocuments();
    assertTrue(closeDataFields.size() == documents.size());
    for (TimeSeriesDocument tsDoc : documents) {
      assertTrue(closeDataFields.contains(tsDoc));
    }

    request = new TimeSeriesSearchRequest();
    request.setLoadTimeSeries(false);
    request.setDataSource(BBG_DATA_SOURCE);
    searchResult = _tsMaster.searchTimeSeries(request);
    documents = searchResult.getDocuments();
    assertTrue(bbgDataSources.size() == documents.size());
    for (TimeSeriesDocument tsDoc : documents) {
      assertTrue(bbgDataSources.contains(tsDoc));
    }
    
    request = new TimeSeriesSearchRequest();
    request.setLoadTimeSeries(false);
    request.setObservationTime(LCLOSE_OBSERVATION_TIME);
    searchResult = _tsMaster.searchTimeSeries(request);
    documents = searchResult.getDocuments();
    assertTrue(lcloseObservations.size() == documents.size());
    for (TimeSeriesDocument tsDoc : documents) {
      assertTrue(lcloseObservations.contains(tsDoc));
    }
    
    request = new TimeSeriesSearchRequest();
    request.setLoadTimeSeries(false);
    request.setDataProvider(CMPL_DATA_PROVIDER);
    searchResult = _tsMaster.searchTimeSeries(request);
    documents = searchResult.getDocuments();
    assertTrue(cmplDataProviders.size() == documents.size());
    for (TimeSeriesDocument tsDoc : documents) {
      assertTrue(cmplDataProviders.contains(tsDoc));
    }
  }
  
  @Test
  public void searchMetaDataWithDates() throws Exception {
    List<TimeSeriesDocument> tsList = addAndTestTimeSeries();
    for (TimeSeriesDocument tsDoc : tsList) {
      //set timeseries to null for metadata test and set dates
      tsDoc.setEarliest(tsDoc.getTimeSeries().getEarliestTime());
      tsDoc.setLatest(tsDoc.getTimeSeries().getLatestTime());
      tsDoc.setTimeSeries(null);
    }
    TimeSeriesSearchRequest request = new TimeSeriesSearchRequest();
    request.setLoadTimeSeries(false); 
    request.setLoadDates(true);
    //return all timeseries meta data with dates without loading timeseries data points
    TimeSeriesSearchResult searchResult = _tsMaster.searchTimeSeries(request);
    assertNotNull(searchResult);
    List<TimeSeriesDocument> documents = searchResult.getDocuments();
    assertNotNull(documents);
    assertTrue(tsList.size() == documents.size());
    for (TimeSeriesDocument expectedDoc : tsList) {
      assertTrue(documents.contains(expectedDoc));
    }
  }
  
  @Test
  public void addDataPoint() throws Exception {
    List<TimeSeriesDocument> tsList = addAndTestTimeSeries();
    for (TimeSeriesDocument tsDoc : tsList) {
      LocalDateDoubleTimeSeries timeSeries = tsDoc.getTimeSeries();
      //add datapoint
      LocalDate latestTime = timeSeries.getLatestTime();
      LocalDate date = latestTime.plusDays(1);
      while (!isWeekday(date)) {
        date = date.plusDays(1);
      }
      double value = Math.random();
      List<LocalDate> dates = timeSeries.times();
      List<Double> values = timeSeries.values();
      dates.add(date);
      values.add(value);
      ArrayLocalDateDoubleTimeSeries updatedTS = new ArrayLocalDateDoubleTimeSeries(dates, values);
      
      String scheme = tsDoc.getUniqueIdentifier().getScheme();
      String timeSeriesID = tsDoc.getUniqueIdentifier().getValue();
      DataPointDocument dataPointDocument = new DataPointDocument();
      dataPointDocument.setTimeSeriesId(tsDoc.getUniqueIdentifier());
      dataPointDocument.setDate(date);
      dataPointDocument.setValue(value);
      
      dataPointDocument = _tsMaster.addDataPoint(dataPointDocument);
      assertNotNull(dataPointDocument);
      assertEquals(UniqueIdentifier.of(scheme, timeSeriesID + "-" + DateUtil.printYYYYMMDD(date)), dataPointDocument.getDataPointId());
      TimeSeriesDocument updatedDoc = _tsMaster.getTimeSeries(tsDoc.getUniqueIdentifier());
      assertNotNull(updatedDoc);
      assertNotNull(updatedDoc.getUniqueIdentifier());
      assertEquals(updatedTS, updatedDoc.getTimeSeries());
      
      DataPointDocument actualDDoc = _tsMaster.getDataPoint(dataPointDocument.getDataPointId());
      assertEquals(tsDoc.getUniqueIdentifier(), actualDDoc.getTimeSeriesId());
      assertEquals(dataPointDocument.getDataPointId(), actualDDoc.getDataPointId());
      assertEquals(dataPointDocument.getDate(), actualDDoc.getDate());
      assertEquals(dataPointDocument.getValue(), actualDDoc.getValue());
    }
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void addDataPointWithoutTSID() throws Exception {
    DataPointDocument dataPointDocument = new DataPointDocument();
    dataPointDocument.setDate(LocalDate.of(2000, 1, 2));
    dataPointDocument.setValue(Math.random());
    dataPointDocument = _tsMaster.addDataPoint(dataPointDocument);
  }
  
  
  @Test
  public void updateDataPoint() throws Exception {
    List<TimeSeriesDocument> tsList = addAndTestTimeSeries();
    for (TimeSeriesDocument tsDoc : tsList) {
      LocalDateDoubleTimeSeries timeSeries = tsDoc.getTimeSeries();
      //update datapoint
      List<LocalDate> dates = timeSeries.times();
      List<Double> values = timeSeries.values();
      int updateIdx = _random.nextInt(timeSeries.size());
      LocalDate date = timeSeries.getTime(updateIdx);
      double newValue = _random.nextDouble();
      values.set(updateIdx, newValue);
      
      ArrayLocalDateDoubleTimeSeries updatedTS = new ArrayLocalDateDoubleTimeSeries(dates, values);
      
      String scheme = tsDoc.getUniqueIdentifier().getScheme();
      String timeSeriesID = tsDoc.getUniqueIdentifier().getValue();
      DataPointDocument dataPointDocument = new DataPointDocument();
      dataPointDocument.setTimeSeriesId(tsDoc.getUniqueIdentifier());
      dataPointDocument.setDataPointId(UniqueIdentifier.of(scheme, timeSeriesID + "-" + DateUtil.printYYYYMMDD(date)));
      dataPointDocument.setDate(date);
      dataPointDocument.setValue(newValue);
      
      DataPointDocument updated = _tsMaster.updateDataPoint(dataPointDocument);
      assertNotNull(updated);
      assertEquals(dataPointDocument.getDataPointId(), updated.getDataPointId());
      assertEquals(dataPointDocument.getTimeSeriesId(), updated.getTimeSeriesId());
      assertEquals(dataPointDocument.getDate(), updated.getDate());
      assertEquals(dataPointDocument.getValue(), updated.getValue());
      
      TimeSeriesDocument updatedDoc = _tsMaster.getTimeSeries(tsDoc.getUniqueIdentifier());
      assertNotNull(updatedDoc);
      assertNotNull(updatedDoc.getUniqueIdentifier());
      assertEquals(updatedTS, updatedDoc.getTimeSeries());
    }
    
  }
  
  @Test
  public void removeDataPoint() throws Exception {
    List<TimeSeriesDocument> tsList = addAndTestTimeSeries();
    for (TimeSeriesDocument tsDocument : tsList) {
      LocalDateDoubleTimeSeries timeSeries = tsDocument.getTimeSeries();
      //delete random datapoints
      List<LocalDate> dates = timeSeries.times();
      List<Double> values = timeSeries.values();
      int deleteIdx = _random.nextInt(timeSeries.size());
      LocalDate deletedDate = dates.remove(deleteIdx);
      values.remove(deleteIdx);
      
      ArrayLocalDateDoubleTimeSeries deletedTS = new ArrayLocalDateDoubleTimeSeries(dates, values);
      String scheme = tsDocument.getUniqueIdentifier().getScheme();
      String tsId = tsDocument.getUniqueIdentifier().getValue();
      _tsMaster.removeDataPoint(UniqueIdentifier.of(scheme, tsId + "-" + DateUtil.printYYYYMMDD(deletedDate)));
      
      TimeSeriesDocument updatedDoc = _tsMaster.getTimeSeries(tsDocument.getUniqueIdentifier());
      assertNotNull(updatedDoc);
      assertNotNull(updatedDoc.getUniqueIdentifier());
      assertEquals(deletedTS, updatedDoc.getTimeSeries());
    }
  }
  
  @Test
  public void getTimeSeriesSnapShot() throws Exception {
    IdentifierBundle identifiers = IdentifierBundle.of(Identifier.of("sa", "ida"), Identifier.of("sb", "idb"));
    
    SortedMap<ZonedDateTime, DoubleTimeSeries<LocalDate>> timeStampTSMap = new TreeMap<ZonedDateTime, DoubleTimeSeries<LocalDate>>();
    LocalDateDoubleTimeSeries timeSeries = makeRandomTimeSeries(DEFAULT_START, DEFAULT_END);
    
    SortedMap<LocalDate, Double> currentTimeSeriesMap = new TreeMap<LocalDate, Double>();
    for (int i = 0; i < timeSeries.size(); i++) {
      currentTimeSeriesMap.put(timeSeries.getTime(i), timeSeries.getValueAt(i));
    }
    
    TimeSeriesDocument tsDocument = new TimeSeriesDocument();
    tsDocument.setDataField(CLOSE_DATA_FIELD);
    tsDocument.setDataProvider(CMPL_DATA_PROVIDER);
    tsDocument.setDataSource(BBG_DATA_SOURCE);
    tsDocument.setObservationTime(LCLOSE_OBSERVATION_TIME);
    tsDocument.setIdentifiers(identifiers);
    tsDocument.setTimeSeries(timeSeries);
    
    tsDocument = _tsMaster.addTimeSeries(tsDocument);
    
    assertNotNull(tsDocument);
    assertNotNull(tsDocument.getUniqueIdentifier());
    
    TimeSeriesDocument actualDoc = _tsMaster.getTimeSeries(tsDocument.getUniqueIdentifier());
    assertNotNull(actualDoc);
    assertEqualTimeSeriesDocument(tsDocument, actualDoc);
    
    timeStampTSMap.put(Clock.system(javax.time.calendar.TimeZone.UTC).zonedDateTime(), timeSeries);
    
    //update a random datapoint 3 times
    for (int i = 0; i < 3; i++) {
      List<LocalDate> times = timeSeries.times();
      int ranIndx = _random.nextInt(times.size());
      LocalDate updateDate = times.get(ranIndx);
      Double newValue = _random.nextDouble();
      currentTimeSeriesMap.put(updateDate, newValue);
      //_tsMaster.updateDataPoint(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME, updateDate, newValue);
      DataPointDocument dataPointDocument = new DataPointDocument();
      dataPointDocument.setTimeSeriesId(tsDocument.getUniqueIdentifier());
      dataPointDocument.setDate(updateDate);
      dataPointDocument.setValue(newValue);
      _tsMaster.updateDataPoint(dataPointDocument);
      tsDocument = _tsMaster.getTimeSeries(tsDocument.getUniqueIdentifier());
      assertNotNull(tsDocument);
      timeSeries = new ArrayLocalDateDoubleTimeSeries(new ArrayList<LocalDate>(currentTimeSeriesMap.keySet()), new ArrayList<Double>(currentTimeSeriesMap.values()));
      assertEquals(timeSeries, tsDocument.getTimeSeries()); 
      timeStampTSMap.put(Clock.system(javax.time.calendar.TimeZone.UTC).zonedDateTime(), timeSeries);
    }
    
    //delete a datapoint
    List<LocalDate> times = timeSeries.times();
    int ranIndx = _random.nextInt(times.size());
    LocalDate deleteDate = times.get(ranIndx);
    currentTimeSeriesMap.remove(deleteDate);
    UniqueIdentifier dataPointId = UniqueIdentifier.of(tsDocument.getUniqueIdentifier().getScheme(), tsDocument.getUniqueIdentifier().getValue() + "-" + DateUtil.printYYYYMMDD(deleteDate));
    _tsMaster.removeDataPoint(dataPointId);
    tsDocument = _tsMaster.getTimeSeries(tsDocument.getUniqueIdentifier());
    assertNotNull(tsDocument);
    timeSeries = new ArrayLocalDateDoubleTimeSeries(new ArrayList<LocalDate>(currentTimeSeriesMap.keySet()), new ArrayList<Double>(currentTimeSeriesMap.values()));
    assertEquals(timeSeries, tsDocument.getTimeSeries()); 
    timeStampTSMap.put(Clock.system(javax.time.calendar.TimeZone.UTC).zonedDateTime(), timeSeries);
    
    //delete all datapoints
    tsDocument.setTimeSeries(new ArrayLocalDateDoubleTimeSeries());
    _tsMaster.updateTimeSeries(tsDocument);
    tsDocument = _tsMaster.getTimeSeries(tsDocument.getUniqueIdentifier());
    assertNotNull(tsDocument);
    timeSeries = new ArrayLocalDateDoubleTimeSeries();
    assertEquals(timeSeries, tsDocument.getTimeSeries()); 
    timeStampTSMap.put(Clock.system(javax.time.calendar.TimeZone.UTC).zonedDateTime(), timeSeries);
    
    //add new datapoints
    timeSeries = makeRandomTimeSeries(DEFAULT_START, DEFAULT_END);
    tsDocument.setTimeSeries(timeSeries);
    _tsMaster.updateTimeSeries(tsDocument);
    tsDocument = _tsMaster.getTimeSeries(tsDocument.getUniqueIdentifier());
    assertNotNull(tsDocument);
    assertEquals(timeSeries, tsDocument.getTimeSeries());
    timeStampTSMap.put(Clock.system(javax.time.calendar.TimeZone.UTC).zonedDateTime(), timeSeries);
    
    //assert datasnapshots
    for (Entry<ZonedDateTime, DoubleTimeSeries<LocalDate>> entry : timeStampTSMap.entrySet()) {
      ZonedDateTime timeStamp = entry.getKey();
      DoubleTimeSeries<LocalDate> expectedTS = entry.getValue();
      TimeSeriesDocument snapshotDoc = getTimeSeriesSnapShot(identifiers, timeStamp);
      assertNotNull(snapshotDoc);
      assertEquals(expectedTS, snapshotDoc.getTimeSeries());
    }
    
    //assert before and after last deltas
    //before 1st delta should return empty timeseries
    ZonedDateTime beforeDelta = timeStampTSMap.firstKey().minusMinutes(1);
    TimeSeriesDocument snapshotDoc = getTimeSeriesSnapShot(identifiers, beforeDelta);
    assertEquals(new ArrayLocalDateDoubleTimeSeries(), snapshotDoc.getTimeSeries());
    //after last delta should return latest timeseries
    ZonedDateTime afterDelta = timeStampTSMap.lastKey().plusMinutes(1);
    tsDocument = _tsMaster.getTimeSeries(tsDocument.getUniqueIdentifier());
    assertNotNull(tsDocument);
    assertEquals(timeStampTSMap.get(timeStampTSMap.lastKey()), tsDocument.getTimeSeries());
    snapshotDoc = getTimeSeriesSnapShot(identifiers, afterDelta);
    assertEquals(tsDocument.getTimeSeries(), snapshotDoc.getTimeSeries());
    
  }

  private TimeSeriesDocument getTimeSeriesSnapShot(IdentifierBundle identifiers, ZonedDateTime timeStamp) {
    TimeSeriesSearchHistoricRequest searchHistoricRequest = new TimeSeriesSearchHistoricRequest();
    searchHistoricRequest.setDataProvider(CMPL_DATA_PROVIDER);
    searchHistoricRequest.setDataSource(BBG_DATA_SOURCE);
    searchHistoricRequest.setDataField(CLOSE_DATA_FIELD);
    searchHistoricRequest.setIdentifiers(identifiers);
    searchHistoricRequest.setObservationTime(LCLOSE_OBSERVATION_TIME);
    searchHistoricRequest.setTimeStamp(timeStamp.toInstant());
    TimeSeriesSearchHistoricResult searchHistoric = _tsMaster.searchHistoric(searchHistoricRequest);
    assertNotNull(searchHistoric);
    List<TimeSeriesDocument> documents = searchHistoric.getDocuments();
    //should expect one single document back
    assertTrue(documents.size() == 1);
    return documents.get(0);
  }
  
  private void assertEqualTimeSeriesDocument(TimeSeriesDocument expectedDoc, TimeSeriesDocument actualDoc) {
    assertNotNull(expectedDoc);
    assertNotNull(actualDoc);
    assertEquals(expectedDoc.getUniqueIdentifier(), actualDoc.getUniqueIdentifier());
    assertEquals(expectedDoc.getTimeSeries(), actualDoc.getTimeSeries());
    assertEquals(expectedDoc.getDataField(), actualDoc.getDataField());
    assertEquals(expectedDoc.getDataProvider(), actualDoc.getDataProvider());
    assertEquals(expectedDoc.getDataSource(), actualDoc.getDataSource());
    assertEquals(expectedDoc.getIdentifiers(), actualDoc.getIdentifiers());
    assertEquals(expectedDoc.getObservationTime(), actualDoc.getObservationTime());
  }
    
  private LocalDateDoubleTimeSeries makeRandomTimeSeries(LocalDate start, LocalDate end) {
    MapLocalDateDoubleTimeSeries tsMap = new MapLocalDateDoubleTimeSeries();
    LocalDate current = start;
    tsMap.putDataPoint(current, Math.random());
    while (current.isBefore(end)) {
      current = current.plusDays(1);
      if (isWeekday(current)) {
        tsMap.putDataPoint(current, Math.random());
      }
    }
    return new ArrayLocalDateDoubleTimeSeries(tsMap);
  }
  
  private boolean isWeekday(LocalDate day) {
    return (day.getDayOfWeek() != DayOfWeek.SATURDAY && day.getDayOfWeek() != DayOfWeek.SUNDAY);
  }

}
