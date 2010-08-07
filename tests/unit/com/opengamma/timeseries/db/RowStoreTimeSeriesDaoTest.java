/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.db;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

import javax.time.calendar.LocalDate;

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
import com.opengamma.timeseries.DataProviderBean;
import com.opengamma.timeseries.DataSourceBean;
import com.opengamma.timeseries.ObservationTimeBean;
import com.opengamma.timeseries.SchemeBean;
import com.opengamma.timeseries.TimeSeriesDocument;
import com.opengamma.timeseries.TimeSeriesMaster;
import com.opengamma.timeseries.TimeSeriesRequest;
import com.opengamma.timeseries.TimeSeriesSearchResult;
import com.opengamma.util.test.DBTest;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.MapLocalDateDoubleTimeSeries;

/**
 * Test.
 */
public class RowStoreTimeSeriesDaoTest extends DBTest {
  private static final Logger s_logger = LoggerFactory.getLogger(RowStoreTimeSeriesDaoTest.class);
  
  private static final int TS_DATASET_SIZE = 5;
  private static final int TS_MAX_DATA_POINT = 5;

  private static final String LCLOSE_OBSERVATION_TIME = "LCLOSE";
  private static final String CLOSE_DATA_FIELD = "CLOSE";
  private static final String CMPL_DATA_PROVIDER = "CMPL";
  private static final String BBG_DATA_SOURCE = "BLOOMBERG";

  private Random _random = new Random();
  private TimeSeriesMaster _tsMaster;
  
  public RowStoreTimeSeriesDaoTest(String databaseType, String databaseVersion) {
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
    
    List<ObservationTimeBean> enums = _tsMaster.getObservationTimes();
    assertNotNull(enums);
    assertTrue(enums.size() == 2);
    assertTrue(enums.contains(b1));
    assertTrue(enums.contains(b2));
    
    _tsMaster.getOrCreateObservationTime("OBT1", "OBT1");
    _tsMaster.getOrCreateObservationTime("OBT2", "OBT2");
    enums = _tsMaster.getObservationTimes();
    assertNotNull(enums);
    assertTrue(enums.size() == 2);
    
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
  public void add() throws Exception {
    
    addRandonTimeSeriesToDB(2);
    
    for (int i = 0; i < TS_DATASET_SIZE; i++) {
      IdentifierBundle identifiers = IdentifierBundle.of(Identifier.of("sa" + i, "ida" + i), Identifier.of("sb" + i, "idb" + i));
      LocalDateDoubleTimeSeries timeSeries = makeRandomTimeSeries();
      
      TimeSeriesDocument tsDocument = new TimeSeriesDocument();
      tsDocument.setDataField(CLOSE_DATA_FIELD);
      tsDocument.setDataProvider(CMPL_DATA_PROVIDER);
      tsDocument.setDataSource(BBG_DATA_SOURCE);
      tsDocument.setObservationTime(LCLOSE_OBSERVATION_TIME);
      tsDocument.setIdentifiers(identifiers);
      tsDocument.setTimeSeries(timeSeries);
      
      tsDocument = _tsMaster.add(tsDocument);
      
      assertNotNull(tsDocument);
      assertNotNull(tsDocument.getUniqueIdentifier());
      
      TimeSeriesRequest request = new TimeSeriesRequest();
      request.setIdentifiers(identifiers);
      request.setDataField(CLOSE_DATA_FIELD);
      request.setDataProvider(CMPL_DATA_PROVIDER);
      request.setDataSource(BBG_DATA_SOURCE);
      request.setObservationTime(LCLOSE_OBSERVATION_TIME);
      request.setLoadTimeSeries(true);
      
      TimeSeriesSearchResult searchResult = _tsMaster.search(request);
      assertNotNull(searchResult);
      List<TimeSeriesDocument> documents = searchResult.getDocuments();
      assertNotNull(documents);
      assertTrue(documents.size() == 1);
      
      TimeSeriesDocument searchedDoc = documents.get(0);
      assertNotNull(searchedDoc);
      
      assertEquals(tsDocument.getUniqueIdentifier(), searchedDoc.getUniqueIdentifier());
      assertEquals(tsDocument.getTimeSeries(), searchedDoc.getTimeSeries());
      assertEquals(tsDocument.getDataField(), searchedDoc.getDataField());
      assertEquals(tsDocument.getDataProvider(), searchedDoc.getDataProvider());
      assertEquals(tsDocument.getDataSource(), searchedDoc.getDataSource());
      assertEquals(tsDocument.getIdentifiers(), searchedDoc.getIdentifiers());
      assertEquals(tsDocument.getObservationTime(), searchedDoc.getObservationTime());
    
    }
     
//    DoubleTimeSeries<LocalDate> actualTS = _tsMaster.getHistoricalTimeSeries(identifiers, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
//    assertEquals(timeSeries, actualTS);
//    
//    actualTS = _tsMaster.getHistoricalTimeSeries(IdentifierBundle.of(cusipID), BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
//    assertEquals(timeSeries, actualTS);
//    
//    actualTS = _tsMaster.getHistoricalTimeSeries(IdentifierBundle.of(bbgUniqueID), BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
//    assertEquals(timeSeries, actualTS);
//    
//    actualTS = _tsMaster.getHistoricalTimeSeries(uid);
//    assertEquals(timeSeries, actualTS);
    
    
  }
  
  @Test
  public void get() throws Exception {
    addRandonTimeSeriesToDB(2);
    
    for (int i = 0; i < TS_DATASET_SIZE; i++) {
      IdentifierBundle identifiers = IdentifierBundle.of(Identifier.of("sa" + i, "va" + i), Identifier.of("sb" + i, "vb" + i));
      LocalDateDoubleTimeSeries timeSeries = makeRandomTimeSeries();
      
      TimeSeriesDocument tsDocument = new TimeSeriesDocument();
      tsDocument.setDataField(CLOSE_DATA_FIELD);
      tsDocument.setDataProvider(CMPL_DATA_PROVIDER);
      tsDocument.setDataSource(BBG_DATA_SOURCE);
      tsDocument.setObservationTime(LCLOSE_OBSERVATION_TIME);
      tsDocument.setIdentifiers(identifiers);
      tsDocument.setTimeSeries(timeSeries);
      
      tsDocument = _tsMaster.add(tsDocument);
      
      assertNotNull(tsDocument);
      assertNotNull(tsDocument.getUniqueIdentifier());
      
      TimeSeriesDocument tsDocByGet = _tsMaster.get(tsDocument.getUniqueIdentifier());
      assertNotNull(tsDocByGet);
      assertEquals(tsDocument.getUniqueIdentifier(), tsDocByGet.getUniqueIdentifier());
      assertEquals(tsDocument.getTimeSeries(), tsDocByGet.getTimeSeries());
      assertEquals(tsDocument.getDataField(), tsDocByGet.getDataField());
      assertEquals(tsDocument.getDataProvider(), tsDocByGet.getDataProvider());
      assertEquals(tsDocument.getDataSource(), tsDocByGet.getDataSource());
      assertEquals(tsDocument.getIdentifiers(), tsDocByGet.getIdentifiers());
      assertEquals(tsDocument.getObservationTime(), tsDocByGet.getObservationTime());
      
      LocalDateDoubleTimeSeries actualTS = _tsMaster.getHistoricalTimeSeries(tsDocument.getUniqueIdentifier());
      assertNotNull(actualTS);
      assertEquals(tsDocument.getTimeSeries(), actualTS);
      
      actualTS = _tsMaster.getHistoricalTimeSeries(identifiers, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
      assertNotNull(actualTS);
      assertEquals(tsDocument.getTimeSeries(), actualTS);
    }
  }
  
  @Test(expected=DataNotFoundException.class)
  public void getUnknownUID() throws Exception {
    addRandonTimeSeriesToDB(2);
    _tsMaster.get(UniqueIdentifier.of(RowStoreTimeSeriesMaster.IDENTIFIER_SCHEME_DEFAULT, String.valueOf(Long.MIN_VALUE)));
  }
  
  @Test(expected=IllegalArgumentException.class) 
  public void getInvalidUID() throws Exception {
    addRandonTimeSeriesToDB(2);
    _tsMaster.get(UniqueIdentifier.of("INVALID", "unknown"));
  }
  
  @Test
  public void searchByIdentifier() throws Exception {
    
    addRandonTimeSeriesToDB(2);
    
    Identifier bbgtickerID = Identifier.of("bbgTicker", "AAPL US Equity");
    Identifier cusipID = Identifier.of("cusip", "123456789");
    Identifier bbgUniqueID = Identifier.of("bbgUnique", "XI45678-89");
    
    IdentifierBundle identifiers = IdentifierBundle.of(bbgUniqueID, bbgtickerID, cusipID);
    
    LocalDateDoubleTimeSeries timeSeries = makeRandomTimeSeries();
    
    TimeSeriesDocument tsDocument = new TimeSeriesDocument();
    tsDocument.setDataField(CLOSE_DATA_FIELD);
    tsDocument.setDataProvider(CMPL_DATA_PROVIDER);
    tsDocument.setDataSource(BBG_DATA_SOURCE);
    tsDocument.setObservationTime(LCLOSE_OBSERVATION_TIME);
    tsDocument.setIdentifiers(identifiers);
    tsDocument.setTimeSeries(timeSeries);
    
    tsDocument = _tsMaster.add(tsDocument);
    
    assertNotNull(tsDocument);
    assertNotNull(tsDocument.getUniqueIdentifier());
    
    for (Identifier identifier : identifiers.getIdentifiers()) {
      TimeSeriesRequest request = new TimeSeriesRequest();
      request.setIdentifiers(IdentifierBundle.of(identifier));
      request.setDataField(CLOSE_DATA_FIELD);
      request.setDataProvider(CMPL_DATA_PROVIDER);
      request.setDataSource(BBG_DATA_SOURCE);
      request.setObservationTime(LCLOSE_OBSERVATION_TIME);
      request.setLoadTimeSeries(true);
      
      TimeSeriesSearchResult searchResult = _tsMaster.search(request);
      assertNotNull(searchResult);
      List<TimeSeriesDocument> documents = searchResult.getDocuments();
      assertNotNull(documents);
      assertTrue(documents.size() == 1);
      
      TimeSeriesDocument searchedDoc = documents.get(0);
      assertNotNull(searchedDoc);
      
      assertEquals(tsDocument.getUniqueIdentifier(), searchedDoc.getUniqueIdentifier());
      assertEquals(tsDocument.getTimeSeries(), searchedDoc.getTimeSeries());
      assertEquals(tsDocument.getDataField(), searchedDoc.getDataField());
      assertEquals(tsDocument.getDataProvider(), searchedDoc.getDataProvider());
      assertEquals(tsDocument.getDataSource(), searchedDoc.getDataSource());
      assertEquals(tsDocument.getIdentifiers(), searchedDoc.getIdentifiers());
      assertEquals(tsDocument.getObservationTime(), searchedDoc.getObservationTime());
    }
  }
  
  @Test
  public void resolveIdentifier() throws Exception {
    for (int i = 0; i < TS_DATASET_SIZE; i++) {
      
      Identifier ida = Identifier.of("da" + i, "ida" + i);
      Identifier idb = Identifier.of("db" + i, "idb" + i);
      IdentifierBundle identifiers = IdentifierBundle.of(ida, idb);
      LocalDateDoubleTimeSeries timeSeries = makeRandomTimeSeries();
      
      TimeSeriesDocument tsDocument = new TimeSeriesDocument();
      tsDocument.setDataField(CLOSE_DATA_FIELD);
      tsDocument.setDataProvider(CMPL_DATA_PROVIDER);
      tsDocument.setDataSource(BBG_DATA_SOURCE);
      tsDocument.setObservationTime(LCLOSE_OBSERVATION_TIME);
      tsDocument.setIdentifiers(identifiers);
      tsDocument.setTimeSeries(timeSeries);
      
      tsDocument = _tsMaster.add(tsDocument);
      
      assertNotNull(tsDocument);
      assertNotNull(tsDocument.getUniqueIdentifier());
      
      UniqueIdentifier resolveIdentifier = _tsMaster.resolveIdentifier(identifiers, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
      assertNotNull(resolveIdentifier);
      assertEquals(tsDocument.getUniqueIdentifier(), resolveIdentifier);
      
      resolveIdentifier = _tsMaster.resolveIdentifier(IdentifierBundle.of(ida), BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
      assertNotNull(resolveIdentifier);
      assertEquals(tsDocument.getUniqueIdentifier(), resolveIdentifier);
      
      resolveIdentifier = _tsMaster.resolveIdentifier(IdentifierBundle.of(idb), BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
      assertNotNull(resolveIdentifier);
      assertEquals(tsDocument.getUniqueIdentifier(), resolveIdentifier);
      
      resolveIdentifier = _tsMaster.resolveIdentifier(identifiers, BBG_DATA_SOURCE + i, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
      assertNull(resolveIdentifier);
      resolveIdentifier = _tsMaster.resolveIdentifier(identifiers, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER + i, CLOSE_DATA_FIELD);
      assertNull(resolveIdentifier);
      
      resolveIdentifier = _tsMaster.resolveIdentifier(identifiers, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD + i);
      assertNull(resolveIdentifier);
    }
    
    UniqueIdentifier resolveIdentifier = _tsMaster.resolveIdentifier(IdentifierBundle.of(Identifier.of("Invalid", "Invalid")), BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
    assertNull(resolveIdentifier);
    
    //check null identifiers
    try {
      _tsMaster.resolveIdentifier(null, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
      fail();
    } catch (NullPointerException ex) {
      //do nothing
    }
  }
  
  @Test
  public void getHistoricalTimeSeriesWithoutDataProvider() throws Exception {
    addRandonTimeSeriesToDB(2);
    String[] testDataProviders = new String[]{"DP1, DP2, DP3"};
    Map<String, LocalDateDoubleTimeSeries> expectedTSMap = new HashMap<String, LocalDateDoubleTimeSeries>();
    
    IdentifierBundle bundle = IdentifierBundle.of(Identifier.of(IdentificationScheme.BLOOMBERG_TICKER, "id1"));
    for (String dataProvider : testDataProviders) {
      
      LocalDateDoubleTimeSeries timeSeries = makeRandomTimeSeries();
      
      TimeSeriesDocument tsDocument = new TimeSeriesDocument();
      tsDocument.setDataField(CLOSE_DATA_FIELD);
      tsDocument.setDataProvider(dataProvider);
      tsDocument.setDataSource(BBG_DATA_SOURCE);
      tsDocument.setObservationTime(LCLOSE_OBSERVATION_TIME);
      tsDocument.setIdentifiers(bundle);
      tsDocument.setTimeSeries(timeSeries);
      
      tsDocument = _tsMaster.add(tsDocument);
      assertNotNull(tsDocument);
      assertNotNull(tsDocument.getUniqueIdentifier());
      
      expectedTSMap.put(dataProvider, timeSeries);
    }
    
    //check you get expected timeseries with dataProvider specified
    TimeSeriesRequest request = new TimeSeriesRequest();
    for (String dataProvider : testDataProviders) {
      request.setIdentifiers(bundle);
      request.setDataSource(BBG_DATA_SOURCE);
      request.setDataProvider(dataProvider);
      request.setDataField(CLOSE_DATA_FIELD);
      request.setObservationTime(LCLOSE_OBSERVATION_TIME);
      request.setLoadTimeSeries(true);
      
      TimeSeriesSearchResult searchResult = _tsMaster.search(request);
      
      assertNotNull(searchResult);
      List<TimeSeriesDocument> documents = searchResult.getDocuments();
      assertNotNull(documents);
      assertTrue(documents.size() == 1);
      
      TimeSeriesDocument searchedDoc = documents.get(0);
      assertNotNull(searchedDoc);
      
      assertEquals(expectedTSMap.get(dataProvider), searchedDoc.getTimeSeries());
    }
    
    //search without dataProvider
    request = new TimeSeriesRequest();
    request.setIdentifiers(bundle);
    request.setDataSource(BBG_DATA_SOURCE);
    request.setDataField(CLOSE_DATA_FIELD);
    request.setObservationTime(LCLOSE_OBSERVATION_TIME);
    request.setLoadTimeSeries(true);
    
    TimeSeriesSearchResult searchResult = _tsMaster.search(request);
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
  
//  @Test
//  public void update() throws Exception {
//    addRandonTimeSeriesToDB(2);
//    for (int i = 0; i < TS_DATASET_SIZE; i++) {
//      IdentifierBundle identifiers = IdentifierBundle.of(Identifier.of("sa" + i, "ida" + i), Identifier.of("sb" + i, "idb" + i));
//      LocalDateDoubleTimeSeries timeSeries = makeRandomTimeSeries();
//      
//      TimeSeriesDocument tsDocument = new TimeSeriesDocument();
//      tsDocument.setDataField(CLOSE_DATA_FIELD);
//      tsDocument.setDataProvider(CMPL_DATA_PROVIDER);
//      tsDocument.setDataSource(BBG_DATA_SOURCE);
//      tsDocument.setObservationTime(LCLOSE_OBSERVATION_TIME);
//      tsDocument.setIdentifiers(identifiers);
//      tsDocument.setTimeSeries(timeSeries);
//      
//      tsDocument = _tsMaster.add(tsDocument);
//      assertNotNull(tsDocument);
//      assertNotNull(tsDocument.getUniqueIdentifier());
//      
//      LocalDateDoubleTimeSeries actualTS = _tsMaster.getHistoricalTimeSeries(identifiers, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
//      assertEquals(timeSeries, actualTS);
//      
//      LocalDateDoubleTimeSeries updatedTS = makeRandomTimeSeries();
//      tsDocument.setTimeSeries(updatedTS);
//      TimeSeriesDocument updatedTsDocument = _tsMaster.update(tsDocument);
//      assertNotNull(updatedTsDocument);
//      assertEquals(tsDocument.getUniqueIdentifier(), updatedTsDocument.getUniqueIdentifier());
//      
//      actualTS = _tsMaster.getHistoricalTimeSeries(identifiers, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
//      assertEquals(updatedTS, actualTS);
//    }
//    
//  }
  
  @Test
  public void getTimeSeriesWithDateRange() throws Exception {
    addRandonTimeSeriesToDB(2);
    for (int i = 0; i < TS_DATASET_SIZE; i++) {
      IdentifierBundle identifiers = IdentifierBundle.of(Identifier.of("sa" + i, "ida" + i), Identifier.of("sb" + i, "idb" + i));
      LocalDateDoubleTimeSeries timeSeries = makeRandomTimeSeries();
      
      TimeSeriesDocument tsDocument = new TimeSeriesDocument();
      tsDocument.setDataField(CLOSE_DATA_FIELD);
      tsDocument.setDataProvider(CMPL_DATA_PROVIDER);
      tsDocument.setDataSource(BBG_DATA_SOURCE);
      tsDocument.setObservationTime(LCLOSE_OBSERVATION_TIME);
      tsDocument.setIdentifiers(identifiers);
      tsDocument.setTimeSeries(timeSeries);
      
      tsDocument = _tsMaster.add(tsDocument);
      
      assertNotNull(tsDocument);
      assertNotNull(tsDocument.getUniqueIdentifier());
            
      LocalDate earliestDate = timeSeries.getEarliestTime();
      LocalDate latestDate = timeSeries.getLatestTime();
      //test end dates
      DoubleTimeSeries<LocalDate> subSeries = timeSeries.subSeries(earliestDate, latestDate);
      s_logger.debug("earliestDate = {}", earliestDate);
      s_logger.debug("latestDate = {}", latestDate);
      DoubleTimeSeries<LocalDate> actualTS = _tsMaster.getHistoricalTimeSeries(identifiers, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, earliestDate, latestDate);
      assertEquals(subSeries, actualTS);
      
      actualTS = _tsMaster.getHistoricalTimeSeries(tsDocument.getUniqueIdentifier(), earliestDate, latestDate);
      assertEquals(subSeries, actualTS);
      
      //test subSeries
      LocalDate start = earliestDate.plusDays(1);
      LocalDate end = latestDate.minusDays(1);
      if (start.isBefore(end) || start.equals(end)) {
        timeSeries = (LocalDateDoubleTimeSeries)timeSeries.subSeries(start, end);
        actualTS = _tsMaster.getHistoricalTimeSeries(identifiers, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, start, end);
        assertEquals(timeSeries, actualTS);
        
        actualTS = _tsMaster.getHistoricalTimeSeries(tsDocument.getUniqueIdentifier(), start, end);
        assertEquals(timeSeries, actualTS);
      }
    }
  }
  
  @Test
  public void getAllIdentifiers() throws Exception {
    
    List<Identifier> allIdentifiers = _tsMaster.getAllIdentifiers();
    assertNotNull(allIdentifiers);
    assertTrue(allIdentifiers.isEmpty());
    
    List<Identifier> expectedIds = new ArrayList<Identifier>();
    for (int i = 0; i < TS_DATASET_SIZE; i++) {
      Identifier id1 = Identifier.of("sa" + i, "ida" + i);
      Identifier id2 = Identifier.of("sb" + i, "idb" + i);
      IdentifierBundle identifiers = IdentifierBundle.of(id1, id2);
      LocalDateDoubleTimeSeries timeSeries = makeRandomTimeSeries();
      expectedIds.add(id1);
      expectedIds.add(id2);
      
      TimeSeriesDocument tsDocument = new TimeSeriesDocument();
      tsDocument.setDataField(CLOSE_DATA_FIELD);
      tsDocument.setDataProvider(CMPL_DATA_PROVIDER);
      tsDocument.setDataSource(BBG_DATA_SOURCE);
      tsDocument.setObservationTime(LCLOSE_OBSERVATION_TIME);
      tsDocument.setIdentifiers(identifiers);
      tsDocument.setTimeSeries(timeSeries);
      
      tsDocument = _tsMaster.add(tsDocument);
      
      assertNotNull(tsDocument);
      assertNotNull(tsDocument.getUniqueIdentifier());
    }
    
    allIdentifiers = _tsMaster.getAllIdentifiers();
    assertNotNull(allIdentifiers);
    assertTrue(allIdentifiers.size() == TS_DATASET_SIZE*2);
    for (Identifier identifier : expectedIds) {
      assertTrue(allIdentifiers.contains(identifier));
    }
    
  }
  
  @Test
  public void remove() throws Exception {
    List<UniqueIdentifier> uniqueIdentifiers = new ArrayList<UniqueIdentifier>();
    //add time series
    for (int i = 0; i < TS_DATASET_SIZE; i++) {
      IdentifierBundle identifiers = IdentifierBundle.of(Identifier.of("sa" + i, "ida" + i), Identifier.of("sb" + i, "idb" + i));
      LocalDateDoubleTimeSeries timeSeries = makeRandomTimeSeries();
      
      TimeSeriesDocument tsDocument = new TimeSeriesDocument();
      tsDocument.setDataField(CLOSE_DATA_FIELD);
      tsDocument.setDataProvider(CMPL_DATA_PROVIDER);
      tsDocument.setDataSource(BBG_DATA_SOURCE);
      tsDocument.setObservationTime(LCLOSE_OBSERVATION_TIME);
      tsDocument.setIdentifiers(identifiers);
      tsDocument.setTimeSeries(timeSeries);
      
      tsDocument = _tsMaster.add(tsDocument);
      
      assertNotNull(tsDocument);
      assertNotNull(tsDocument.getUniqueIdentifier());
      
      TimeSeriesDocument actualDoc = _tsMaster.get(tsDocument.getUniqueIdentifier());
      assertEquals(timeSeries, actualDoc.getTimeSeries());
      uniqueIdentifiers.add(tsDocument.getUniqueIdentifier());
    }
    for (UniqueIdentifier identifier : uniqueIdentifiers) {
      _tsMaster.remove(identifier);
      try {
        _tsMaster.get(identifier);
        fail();
      } catch(DataNotFoundException ex) {
        //do nothing
      }
    }
  }
//  
//  @Test
//  public void addTimeSeriesToExistingIdentifiers() throws Exception {
//    addRandonTimeSeriesToDB(2);
//    //add time series
//    for (int i = 0; i < TS_DATASET_SIZE; i++) {
//      IdentifierBundle identifier = IdentifierBundle.of(Identifier.of("d" + i, "id" + i));
//      LocalDateDoubleTimeSeries timeSeries = makeRandomTimeSeries();
//      tsMaster.addTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD,
//          LCLOSE_OBSERVATION_TIME, timeSeries);
//      //assert timeseries are in datastore
//      DoubleTimeSeries<LocalDate> actualTS = tsMaster.getHistoricalTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
//      assertEquals(timeSeries, actualTS);
//      //delete timeseries
//      tsMaster.deleteTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME);
//      actualTS = tsMaster.getHistoricalTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
//      assertEquals(new ArrayLocalDateDoubleTimeSeries(), actualTS);
//      // add timeseries to existing identifiers in the datastore
//      timeSeries = makeRandomTimeSeries();
//      tsMaster.addTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD,
//          LCLOSE_OBSERVATION_TIME, timeSeries);
//      actualTS = tsMaster.getHistoricalTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
//      assertEquals(timeSeries, actualTS);
//    }
//  }
  
  @Test
  public void getEmptyTimeSeries() throws Exception {
    addRandonTimeSeriesToDB(2);
    IdentifierBundle bundle = IdentifierBundle.of(Identifier.of("BLOOMBERG_TICKER", "AAPL US Equity"), Identifier.of("BUID", "X-12345678"));
    DoubleTimeSeries<LocalDate> actualTS = _tsMaster.getHistoricalTimeSeries(bundle, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
    assertEquals(new ArrayLocalDateDoubleTimeSeries(), actualTS);
  }
//  
//  @Test
//  public void updateDataPoint() throws Exception {
//    addRandonTimeSeriesToDB(2);
//    for (int i = 0; i < TS_DATASET_SIZE; i++) {
//      IdentifierBundle identifier = IdentifierBundle.of(Identifier.of("d" + i, "id" + i));
//      LocalDateDoubleTimeSeries timeSeries = makeRandomTimeSeries();
//      tsMaster.addTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD,
//          LCLOSE_OBSERVATION_TIME, timeSeries);
//      DoubleTimeSeries<LocalDate> actualTS = tsMaster.getHistoricalTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
//      assertEquals(timeSeries, actualTS);
//      
//      //update datapoint
//      List<LocalDate> dates = timeSeries.times();
//      List<Double> values = timeSeries.values();
//      int updateIdx = _random.nextInt(timeSeries.size());
//      LocalDate date = timeSeries.getTime(updateIdx);
//      double newValue = _random.nextDouble();
//      values.set(updateIdx, newValue);
//      
//      ArrayLocalDateDoubleTimeSeries updatedTS = new ArrayLocalDateDoubleTimeSeries(dates, values);
//      
//      tsMaster.updateDataPoint(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME, date, newValue);
//      actualTS = tsMaster.getHistoricalTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
//      assertEquals(updatedTS, actualTS);
//    }
//  }
//  
//  @Test
//  public void deleteDataPoint() throws Exception {
//    addRandonTimeSeriesToDB(2);
//    for (int i = 0; i < TS_DATASET_SIZE; i++) {
//      IdentifierBundle identifier = IdentifierBundle.of(Identifier.of("d" + i, "id" + i));
//      LocalDateDoubleTimeSeries timeSeries = makeRandomTimeSeries();
//      //add timeseries to datastore
//      tsMaster.addTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD,
//          LCLOSE_OBSERVATION_TIME, timeSeries);
//      //assert timeseries 
//      DoubleTimeSeries<LocalDate> actualTS = tsMaster.getHistoricalTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
//      assertEquals(timeSeries, actualTS);
//      
//      //delete random datapoints
//      List<LocalDate> dates = timeSeries.times();
//      List<Double> values = timeSeries.values();
//      int deleteIdx = _random.nextInt(timeSeries.size());
//      LocalDate deletedDate = dates.remove(deleteIdx);
//      values.remove(deleteIdx);
//      
//      ArrayLocalDateDoubleTimeSeries deletedTS = new ArrayLocalDateDoubleTimeSeries(dates, values);
//      tsMaster.deleteDataPoint(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME, deletedDate);
//      
//      actualTS = tsMaster.getHistoricalTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
//      assertEquals(deletedTS, actualTS);
//    }
//  }
//  
//  @Test
//  public void getTimeSeriesSnapShot() throws Exception {
//    IdentifierBundle identifier = IdentifierBundle.of(Identifier.of("d1", "id1"));
//    
//    SortedMap<ZonedDateTime, DoubleTimeSeries<LocalDate>> timeStampTSMap = new TreeMap<ZonedDateTime, DoubleTimeSeries<LocalDate>>();
//    LocalDateDoubleTimeSeries timeSeries = makeRandomTimeSeries();
//    
//    SortedMap<LocalDate, Double> currentTimeSeriesMap = new TreeMap<LocalDate, Double>();
//    for (int i = 0; i < timeSeries.size(); i++) {
//      currentTimeSeriesMap.put(timeSeries.getTime(i), timeSeries.getValueAt(i));
//    }
//    
//    tsMaster.addTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD,
//        LCLOSE_OBSERVATION_TIME, timeSeries);
//    DoubleTimeSeries<LocalDate> actualTS = tsMaster.getHistoricalTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
//    assertEquals(timeSeries, actualTS);
//    
//    timeStampTSMap.put(Clock.system(javax.time.calendar.TimeZone.UTC).zonedDateTime(), timeSeries);
//    
//    //update a random datapoint 3 times
//    for (int i = 0; i < 3; i++) {
//      List<LocalDate> times = timeSeries.times();
//      int ranIndx = _random.nextInt(times.size());
//      LocalDate updateDate = times.get(ranIndx);
//      Double newValue = _random.nextDouble();
//      currentTimeSeriesMap.put(updateDate, newValue);
//      tsMaster.updateDataPoint(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME, updateDate, newValue);
//      actualTS = tsMaster.getHistoricalTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
//      timeSeries = new ArrayLocalDateDoubleTimeSeries(new ArrayList<LocalDate>(currentTimeSeriesMap.keySet()), new ArrayList<Double>(currentTimeSeriesMap.values()));
//      assertEquals(timeSeries, actualTS); 
//      timeStampTSMap.put(Clock.system(javax.time.calendar.TimeZone.UTC).zonedDateTime(), timeSeries);
//    }
//    
//    //delete a datapoint
//    List<LocalDate> times = timeSeries.times();
//    int ranIndx = _random.nextInt(times.size());
//    LocalDate deleteDate = times.get(ranIndx);
//    currentTimeSeriesMap.remove(deleteDate);
//    tsMaster.deleteDataPoint(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME, deleteDate);
//    actualTS = tsMaster.getHistoricalTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
//    timeSeries = new ArrayLocalDateDoubleTimeSeries(new ArrayList<LocalDate>(currentTimeSeriesMap.keySet()), new ArrayList<Double>(currentTimeSeriesMap.values()));
//    assertEquals(timeSeries, actualTS); 
//    timeStampTSMap.put(Clock.system(javax.time.calendar.TimeZone.UTC).zonedDateTime(), timeSeries);
//    
//    //delete timeSeries
//    tsMaster.deleteTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME);
//    actualTS = tsMaster.getHistoricalTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
//    timeSeries = new ArrayLocalDateDoubleTimeSeries();
//    assertEquals(timeSeries, actualTS); 
//    timeStampTSMap.put(Clock.system(javax.time.calendar.TimeZone.UTC).zonedDateTime(), timeSeries);
//    
//    //add new timeseries
//    timeSeries = makeRandomTimeSeries();
//    tsMaster.addTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME, timeSeries);
//    actualTS = tsMaster.getHistoricalTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
//    assertEquals(timeSeries, actualTS);
//    timeStampTSMap.put(Clock.system(javax.time.calendar.TimeZone.UTC).zonedDateTime(), timeSeries);
//    
//    //assert datasnapshots
//    for (Entry<ZonedDateTime, DoubleTimeSeries<LocalDate>> entry : timeStampTSMap.entrySet()) {
//      ZonedDateTime timeStamp = entry.getKey();
//      DoubleTimeSeries<LocalDate> expectedTS = entry.getValue();
//      DoubleTimeSeries<LocalDate> snapshotTS = tsMaster.getTimeSeriesSnapShot(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME, timeStamp);
//      assertEquals(expectedTS, snapshotTS);
//    }
//    
//    //assert before and after last deltas
//    //before 1st delta should return empty timeseries
//    ZonedDateTime beforeDelta = timeStampTSMap.firstKey().minusMinutes(1);
//    DoubleTimeSeries<LocalDate> snapshotTS = tsMaster.getTimeSeriesSnapShot(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME, beforeDelta);
//    assertEquals(new ArrayLocalDateDoubleTimeSeries(), snapshotTS);
//    //after last delta should return latest timeseries
//    ZonedDateTime afterDelta = timeStampTSMap.lastKey().plusMinutes(1);
//    DoubleTimeSeries<LocalDate> latestTS = tsMaster.getHistoricalTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
//    assertEquals(timeStampTSMap.get(timeStampTSMap.lastKey()), latestTS);
//    snapshotTS = tsMaster.getTimeSeriesSnapShot(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME, afterDelta);
//    assertEquals(latestTS, snapshotTS);
//    
//  }
  
  /**
   * @return
   */
  private LocalDateDoubleTimeSeries makeRandomTimeSeries() {
    MapLocalDateDoubleTimeSeries tsMap = new MapLocalDateDoubleTimeSeries();
    for (int i = 0; i < TS_MAX_DATA_POINT; i++) {
      int year = 1970 + _random.nextInt(40);
      int monthOfYear = 1 + _random.nextInt(12);
      int dayOfMonth = 1 + _random.nextInt(28);
      tsMap.putDataPoint(LocalDate.of(year, monthOfYear, dayOfMonth), _random.nextDouble());
    }
    return new ArrayLocalDateDoubleTimeSeries(tsMap);
  }
  
  private void addRandonTimeSeriesToDB(int size) {
    for (int i = 0; i < size; i++) {
      Identifier identifier = Identifier.of("t" + i, "tid" + i);
      IdentifierBundle identifiers = IdentifierBundle.of(identifier);
      LocalDateDoubleTimeSeries timeSeries = makeRandomTimeSeries();
      
      TimeSeriesDocument tsDocument = new TimeSeriesDocument();
      tsDocument.setDataField(CLOSE_DATA_FIELD + i);
      tsDocument.setDataProvider(CMPL_DATA_PROVIDER + i);
      tsDocument.setDataSource(BBG_DATA_SOURCE + i);
      tsDocument.setObservationTime(LCLOSE_OBSERVATION_TIME + i);
      tsDocument.setIdentifiers(identifiers);
      tsDocument.setTimeSeries(timeSeries);
      
      tsDocument = _tsMaster.add(tsDocument);
      
      assertNotNull(tsDocument);
      assertNotNull(tsDocument.getUniqueIdentifier());
    }
  }

}
