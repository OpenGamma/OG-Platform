/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.historicaldata;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.time.calendar.Clock;
import javax.time.calendar.DayOfWeek;
import javax.time.calendar.LocalDate;
import javax.time.calendar.MonthOfYear;
import javax.time.calendar.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.IdentifierBundleWithDates;
import com.opengamma.id.IdentifierWithDates;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.historicaldata.DataPointDocument;
import com.opengamma.master.historicaldata.HistoricalTimeSeriesDocument;
import com.opengamma.master.historicaldata.HistoricalTimeSeriesGetRequest;
import com.opengamma.master.historicaldata.HistoricalTimeSeriesSearchHistoricRequest;
import com.opengamma.master.historicaldata.HistoricalTimeSeriesSearchHistoricResult;
import com.opengamma.master.historicaldata.HistoricalTimeSeriesSearchRequest;
import com.opengamma.master.historicaldata.HistoricalTimeSeriesSearchResult;
import com.opengamma.master.historicaldata.ManageableHistoricalTimeSeries;
import com.opengamma.master.historicaldata.impl.RandomTimeSeriesGenerator;
import com.opengamma.masterdb.historicaldata.hibernate.DataFieldBean;
import com.opengamma.masterdb.historicaldata.hibernate.DataProviderBean;
import com.opengamma.masterdb.historicaldata.hibernate.DataSourceBean;
import com.opengamma.masterdb.historicaldata.hibernate.ObservationTimeBean;
import com.opengamma.masterdb.historicaldata.hibernate.SchemeBean;
import com.opengamma.util.test.DBTest;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.MapLocalDateDoubleTimeSeries;

/**
 * Test DbHistoricalTimeSeriesMaster.
 */
@Test
public class DbHistoricalTimeSeriesMasterTest extends DBTest {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DbHistoricalTimeSeriesMasterTest.class);

  private static final int TS_DATASET_SIZE = 1;

  private static final String LCLOSE_OBSERVATION_TIME = "LCLOSE";
  private static final String CLOSE_DATA_FIELD = "CLOSE";
  private static final String CMPL_DATA_PROVIDER = "CMPL";
  private static final String BBG_DATA_SOURCE = "BLOOMBERG";

  private static final String[] DATA_FIELDS = new String[] {CLOSE_DATA_FIELD, "VOLUME"};
  private static final String[] DATA_PROVIDERS = new String[] {CMPL_DATA_PROVIDER, "CMPT"};
  private static final String[] DATA_SOURCES = new String[] {BBG_DATA_SOURCE, "REUTERS"};

  private static final LocalDate DEFAULT_START = DateUtil.previousWeekDay().minusDays(7);

  private Random _random = new Random();
  private DbHistoricalTimeSeriesMaster _master;

  /**
   * Constructor.
   * @param databaseType  the type of database
   * @param databaseVersion  the database script version
   */
  @Factory(dataProvider = "databases", dataProviderClass = DBTest.class)
  public DbHistoricalTimeSeriesMasterTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {} version {}", databaseType, databaseVersion);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  //-------------------------------------------------------------------------
  /**
   * @throws java.lang.Exception
   */
  @SuppressWarnings("unchecked")
  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();
    
    ApplicationContext context = new FileSystemXmlApplicationContext("src/com/opengamma/masterdb/historicaldata/tssQueries.xml");
    Map<String, String> namedSQLMap = (Map<String, String>) context.getBean("tssNamedSQLMap");
    
    _master = getTimeSeriesMaster(namedSQLMap);
  }

  @Test
  public void createDataSource() throws Exception {
    
    DataSourceBean ds1 = _master.getOrCreateDataSource("DS1", "DS1");
    assertNotNull(ds1);
    assertNotNull(ds1.getId());
    assertEquals("DS1", ds1.getName());
    assertEquals("DS1", ds1.getDescription());
    
    DataSourceBean ds2 = _master.getOrCreateDataSource("DS2", "DS2");
    assertNotNull(ds2);
    assertNotNull(ds2.getId());
    assertEquals("DS2", ds2.getName());
    assertEquals("DS2", ds2.getDescription());
    
    List<DataSourceBean> dataSources = _master.getDataSources();
    assertNotNull(dataSources);
    assertTrue(dataSources.size() == 2);
    assertTrue(dataSources.contains(ds1));
    assertTrue(dataSources.contains(ds2));
    
    _master.getOrCreateDataSource("DS1", "DS1");
    _master.getOrCreateDataSource("DS2", "DS2");
    dataSources = _master.getDataSources();
    assertNotNull(dataSources);
    assertTrue(dataSources.size() == 2);
    
  }

  @Test
  public void createDataProvider() throws Exception {
    DataProviderBean dp1 = _master.getOrCreateDataProvider("DP1", "DP1");
    assertNotNull(dp1);
    assertNotNull(dp1.getId());
    assertEquals("DP1", dp1.getName());
    assertEquals("DP1", dp1.getDescription());
    
    DataProviderBean dp2 = _master.getOrCreateDataProvider("DP2", "DP2");
    assertNotNull(dp2);
    assertNotNull(dp2.getId());
    assertEquals("DP2", dp2.getName());
    assertEquals("DP2", dp2.getDescription());
    
    List<DataProviderBean> dataProviders = _master.getDataProviders();
    assertNotNull(dataProviders);
    assertTrue(dataProviders.size() == 2);
    assertTrue(dataProviders.contains(dp1));
    assertTrue(dataProviders.contains(dp2));
    
    _master.getOrCreateDataProvider("DP1", "DP1");
    _master.getOrCreateDataProvider("DP2", "DP2");
    dataProviders = _master.getDataProviders();
    assertNotNull(dataProviders);
    assertTrue(dataProviders.size() == 2);
        
  }
  
  @Test
  public void createDataField() throws Exception {
    
    DataFieldBean df1 = _master.getOrCreateDataField("TSF1", "TSF1");
    assertNotNull(df1);
    assertNotNull(df1.getId());
    assertEquals("TSF1", df1.getName());
    assertEquals("TSF1", df1.getDescription());
    
    DataFieldBean df2 = _master.getOrCreateDataField("TSF2", "TSF2");
    assertNotNull(df2);
    assertNotNull(df2.getId());
    assertEquals("TSF2", df2.getName());
    assertEquals("TSF2", df2.getDescription());
    
    List<DataFieldBean> dataFields = _master.getDataFields();
    assertNotNull(dataFields);
    assertTrue(dataFields.size() == 2);
    assertTrue(dataFields.contains(df1));
    assertTrue(dataFields.contains(df2));
    
    _master.getOrCreateDataField("TSF1", "TSF1");
    _master.getOrCreateDataField("TSF2", "TSF2");
    dataFields = _master.getDataFields();
    assertNotNull(dataFields);
    assertTrue(dataFields.size() == 2);
  }
  
  @Test
  public void createObservationTime() throws Exception {
    
    ObservationTimeBean b1 = _master.getOrCreateObservationTime("OBT1", "OBT1");
    assertNotNull(b1);
    assertNotNull(b1.getId());
    assertEquals("OBT1", b1.getName());
    assertEquals("OBT1", b1.getDescription());
    
    ObservationTimeBean b2 = _master.getOrCreateObservationTime("OBT2", "OBT2");
    assertNotNull(b2);
    assertNotNull(b2.getId());
    assertEquals("OBT2", b2.getName());
    assertEquals("OBT2", b2.getDescription());
    
    List<ObservationTimeBean> observationTimes = _master.getObservationTimes();
    assertNotNull(observationTimes);
    assertTrue(observationTimes.size() == 2);
    assertTrue(observationTimes.contains(b1));
    assertTrue(observationTimes.contains(b2));
    
    _master.getOrCreateObservationTime("OBT1", "OBT1");
    _master.getOrCreateObservationTime("OBT2", "OBT2");
    observationTimes = _master.getObservationTimes();
    assertNotNull(observationTimes);
    assertTrue(observationTimes.size() == 2);
    
  }
  
  
  @Test
  public void createScheme() throws Exception {
    
    SchemeBean b1 = _master.getOrCreateScheme("SCH1", "SCH1");
    assertNotNull(b1);
    assertNotNull(b1.getId());
    assertEquals("SCH1", b1.getName());
    assertEquals("SCH1", b1.getDescription());
    
    SchemeBean b2 = _master.getOrCreateScheme("SCH2", "SCH2");
    assertNotNull(b2);
    assertNotNull(b2.getId());
    assertEquals("SCH2", b2.getName());
    assertEquals("SCH2", b2.getDescription());
    
    List<SchemeBean> enums = _master.getSchemes();
    assertNotNull(enums);
    assertTrue(enums.size() == 2);
    assertTrue(enums.contains(b1));
    assertTrue(enums.contains(b2));
    
    _master.getOrCreateScheme("SCH1", "SCH1");
    _master.getOrCreateScheme("SCH2", "SCH2");
    enums = _master.getSchemes();
    assertNotNull(enums);
    assertTrue(enums.size() == 2);
    
  }
  
  @Test
  public void getAllIdentifiers() throws Exception {
    
    List<IdentifierBundleWithDates> allIdentifiers = _master.getAllIdentifiers();
    assertNotNull(allIdentifiers);
    assertTrue(allIdentifiers.isEmpty());
    
    List<IdentifierBundle> expectedIds = new ArrayList<IdentifierBundle>();
    for (int i = 0; i < TS_DATASET_SIZE; i++) {
      Identifier id1 = Identifier.of("sa" + i, "ida" + i);
      Identifier id2 = Identifier.of("sb" + i, "idb" + i);
      IdentifierBundle identifiers = IdentifierBundle.of(id1, id2);
      LocalDate previousWeekDay = DateUtil.previousWeekDay();
      LocalDateDoubleTimeSeries timeSeries = makeRandomTimeSeries(previousWeekDay, 7);
      expectedIds.add(identifiers);
      
      ManageableHistoricalTimeSeries series = new ManageableHistoricalTimeSeries();
      series.setDataField(CLOSE_DATA_FIELD);
      series.setDataProvider(CMPL_DATA_PROVIDER);
      series.setDataSource(BBG_DATA_SOURCE);
      series.setObservationTime(LCLOSE_OBSERVATION_TIME);
      setIdentifiers(identifiers, series);
      series.setTimeSeries(timeSeries);
      
      HistoricalTimeSeriesDocument doc = _master.add(new HistoricalTimeSeriesDocument(series));
      
      assertNotNull(doc);
      assertNotNull(doc.getUniqueId());
    }
    
    allIdentifiers = _master.getAllIdentifiers();
    assertNotNull(allIdentifiers);
    assertTrue(allIdentifiers.size() == expectedIds.size());
    for (IdentifierBundle identifierBundle : expectedIds) {
      assertTrue(allIdentifiers.contains(IdentifierBundleWithDates.of(identifierBundle)));
    }
    
  }
  
  @Test
  public void searchByIdentifierBundle() throws Exception {
    List<HistoricalTimeSeriesDocument> expectedTS = addAndTestTimeSeries();
    HistoricalTimeSeriesDocument doc = getRandomTimeSeriesDocument(expectedTS);
    
    ManageableHistoricalTimeSeries series = doc.getSeries();
    HistoricalTimeSeriesSearchResult searchResult = search(null, series.getIdentifiers().asIdentifierBundle(),
        series.getDataField(), series.getDataProvider(), series.getDataSource(), series.getObservationTime(), true, false);
    assertNotNull(searchResult);
    List<HistoricalTimeSeriesDocument> documents = searchResult.getDocuments();
    assertNotNull(documents);
    assertTrue(documents.size() == 1);
    
    HistoricalTimeSeriesDocument searchedDoc = documents.get(0);
    assertNotNull(searchedDoc);
    
    assertEqualTimeSeriesDocument(doc, searchedDoc);
    
  }

  private HistoricalTimeSeriesSearchResult search(
      LocalDate identifierValidityDate, IdentifierBundle bundle,
      String dataField, String dataProvider, String dataSource, String observationTime,
      boolean loadTimeSeries, boolean loadEarliestLatest) {    
    HistoricalTimeSeriesSearchRequest request = new HistoricalTimeSeriesSearchRequest();
    request.setIdentifierValidityDate(identifierValidityDate);
    if (bundle != null) {
      request.setIdentifiers(bundle);
    }
    request.setDataField(dataField);
    request.setDataProvider(dataProvider);
    request.setDataSource(dataSource);
    request.setObservationTime(observationTime);
    request.setLoadTimeSeries(loadTimeSeries);
    request.setLoadEarliestLatest(loadEarliestLatest);
    HistoricalTimeSeriesSearchResult searchResult = _master.search(request);
    return searchResult;
  }
  
  @Test
  public void searchByIdentifierValue() throws Exception {
    List<HistoricalTimeSeriesDocument> expectedTS = addAndTestTimeSeries();
    HistoricalTimeSeriesDocument doc = getRandomTimeSeriesDocument(expectedTS);
    HistoricalTimeSeriesSearchRequest request = new HistoricalTimeSeriesSearchRequest();
    IdentifierWithDates identifierWithDates = doc.getSeries().getIdentifiers().getIdentifiers().iterator().next();
    request.setIdentifierValue(identifierWithDates.asIdentifier().getValue());
    request.setDataField(doc.getSeries().getDataField());
    request.setDataProvider(doc.getSeries().getDataProvider());
    request.setDataSource(doc.getSeries().getDataSource());
    request.setObservationTime(doc.getSeries().getObservationTime());
    request.setLoadTimeSeries(true);
    
    HistoricalTimeSeriesSearchResult searchResult = _master.search(request);
    assertNotNull(searchResult);
    List<HistoricalTimeSeriesDocument> documents = searchResult.getDocuments();
    assertNotNull(documents);
    assertTrue(documents.size() == 1);
    
    HistoricalTimeSeriesDocument searchedDoc = documents.get(0);
    assertNotNull(searchedDoc);
    
    assertEqualTimeSeriesDocument(doc, searchedDoc);
    
  }
  
  @Test
  public void searchByFieldProviderSource() throws Exception {
    List<HistoricalTimeSeriesDocument> tsList = addAndTestTimeSeries();
    HistoricalTimeSeriesDocument doc = getRandomTimeSeriesDocument(tsList);
    
    ManageableHistoricalTimeSeries series = doc.getSeries();
    HistoricalTimeSeriesSearchResult searchResult = search(null, series.getIdentifiers().asIdentifierBundle(),
        series.getDataField(), series.getDataProvider(), series.getDataSource(), series.getObservationTime(), true, false);
    assertNotNull(searchResult);
    List<HistoricalTimeSeriesDocument> documents = searchResult.getDocuments();
    assertNotNull(documents);
    assertTrue(documents.size() == 1);
    
    assertEqualTimeSeriesDocument(doc, documents.get(0));
  }

  @Test
  public void getByRequest() throws Exception {
    List<HistoricalTimeSeriesDocument> tsList = addAndTestTimeSeries();
    HistoricalTimeSeriesDocument tsDoc = getRandomTimeSeriesDocument(tsList);
    
    HistoricalTimeSeriesGetRequest request = new HistoricalTimeSeriesGetRequest(tsDoc.getUniqueId());
    request.setLoadTimeSeries(true);
    request.setLoadEarliestLatest(false);
    HistoricalTimeSeriesDocument doc = _master.get(request);
    assertNotNull(doc);
    assertEqualTimeSeriesDocument(tsDoc, doc);
  }

  @Test
  public void addTimeSeries() throws Exception {
    addAndTestTimeSeries();    
  }
  
  protected List<HistoricalTimeSeriesDocument> addAndTestTimeSeries() {
    List<HistoricalTimeSeriesDocument> result = new ArrayList<HistoricalTimeSeriesDocument>(); 
    for (int i = 0; i < TS_DATASET_SIZE; i++) {
      IdentifierBundle identifiers = IdentifierBundle.of(SecurityUtils.bloombergTickerSecurityId("ticker" + i), SecurityUtils.bloombergBuidSecurityId("buid" + i));
      LocalDate start = DateUtil.previousWeekDay().minusDays(7);
      for (String dataSource : DATA_SOURCES) {
        for (String dataProvider : DATA_PROVIDERS) {
          for (String datafield : DATA_FIELDS) {
            LocalDateDoubleTimeSeries timeSeries = makeRandomTimeSeries(start, 7);
            assertTrue(timeSeries.size() == 7);
            assertEquals(start, timeSeries.getEarliestTime());
            
            HistoricalTimeSeriesDocument tsDocument = createTimeSeries(datafield, dataProvider, dataSource, IdentifierBundleWithDates.of(identifiers), timeSeries);
            assertNotNull(tsDocument);
            assertNotNull(tsDocument.getUniqueId());
            
            HistoricalTimeSeriesDocument actualDoc = _master.get(tsDocument.getUniqueId());
            assertNotNull(actualDoc);
            assertEquals(timeSeries, actualDoc.getSeries().getTimeSeries());
            result.add(tsDocument);
          }
        }
      }
    }
    return result;
  }
  
  protected HistoricalTimeSeriesDocument createTimeSeries(final String datafield, final String dataProvider, final String dataSource, final IdentifierBundleWithDates identifiers, final LocalDateDoubleTimeSeries timeSeries) {
    ManageableHistoricalTimeSeries series = new ManageableHistoricalTimeSeries();
    series.setDataField(datafield);
    series.setDataProvider(dataProvider);
    series.setDataSource(dataSource);
    series.setObservationTime(LCLOSE_OBSERVATION_TIME);
    series.setIdentifiers(identifiers);
    series.setTimeSeries(timeSeries);
    return _master.add(new HistoricalTimeSeriesDocument(series));    
  }

  
  @Test
  public void addDuplicateTimeSeries() throws Exception {
    IdentifierBundle identifiers = IdentifierBundle.of(Identifier.of("sa", "ida"), Identifier.of("sb", "idb"));
    LocalDateDoubleTimeSeries timeSeries = makeRandomTimeSeries(DEFAULT_START, 7);
    
    ManageableHistoricalTimeSeries series = new ManageableHistoricalTimeSeries();
    series.setDataField(CLOSE_DATA_FIELD);
    series.setDataProvider(CMPL_DATA_PROVIDER);
    series.setDataSource(BBG_DATA_SOURCE);
    series.setObservationTime(LCLOSE_OBSERVATION_TIME);
    setIdentifiers(identifiers, series);
    series.setTimeSeries(timeSeries);
    
    HistoricalTimeSeriesDocument doc = _master.add(new HistoricalTimeSeriesDocument(series));
    
    assertNotNull(doc);
    assertNotNull(doc.getUniqueId());
    
    HistoricalTimeSeriesDocument actualDoc = _master.get(doc.getUniqueId());
    assertNotNull(actualDoc);
    
    assertEqualTimeSeriesDocument(doc, actualDoc);
    
    //try and add another using the same meta data and identifiers
    ManageableHistoricalTimeSeries otherSeries = new ManageableHistoricalTimeSeries();
    otherSeries.setDataField(CLOSE_DATA_FIELD);
    otherSeries.setDataProvider(CMPL_DATA_PROVIDER);
    otherSeries.setDataSource(BBG_DATA_SOURCE);
    otherSeries.setObservationTime(LCLOSE_OBSERVATION_TIME);
    setIdentifiers(identifiers, otherSeries);
    otherSeries.setTimeSeries(makeRandomTimeSeries(DEFAULT_START, 7));
    try {
      _master.add(new HistoricalTimeSeriesDocument(otherSeries));
      fail();
    } catch (IllegalArgumentException ex) {
      //do nothing
    }
  }

  @Test
  public void addMultipleWithDateNulls() throws Exception {
    
    IdentifierWithDates edu0 = IdentifierWithDates.of(Identifier.of("BBG", "EDU0"), LocalDate.of(2010, 9, 14), LocalDate.of(2020, 9, 14));
    IdentifierWithDates edu20 = IdentifierWithDates.of(Identifier.of("BBG", "EDU20"), LocalDate.of(2010, 9, 15), null);
    IdentifierWithDates cusip = IdentifierWithDates.of(Identifier.of("CUSIP", "EDU0"), LocalDate.of(2010, 9, 14), LocalDate.of(2020, 9, 14));
    IdentifierWithDates buid = IdentifierWithDates.of(Identifier.of("BUID", "IX-1234"), null, null);
    
    IdentifierBundleWithDates bundle = IdentifierBundleWithDates.of(edu0, edu20, cusip, buid);
    
    LocalDateDoubleTimeSeries ts = makeRandomTimeSeries(7);
    HistoricalTimeSeriesDocument tsDoc = createTimeSeries("PX_LAST", "CMPL", "BBG", bundle, ts);
    assertNotNull(tsDoc);
    HistoricalTimeSeriesDocument loadedTs = _master.get(tsDoc.getUniqueId());
    assertEquals(loadedTs.getSeries().getTimeSeries(), ts);
    
    ts = makeRandomTimeSeries(7);
    tsDoc = createTimeSeries("VOLUME", "CMPL", "BBG", bundle, ts);
    assertNotNull(tsDoc);
    loadedTs = _master.get(tsDoc.getUniqueId());
    assertEquals(loadedTs.getSeries().getTimeSeries(), ts); 
  }
  
  @Test
  public void addMultipleWithNullDates() throws Exception {
    
    IdentifierWithDates edu0 = IdentifierWithDates.of(Identifier.of("BBG", "EDU0"), null, LocalDate.of(2020, 9, 14));
    IdentifierWithDates edu20 = IdentifierWithDates.of(Identifier.of("BBG", "EDU20"), LocalDate.of(2010, 9, 15), null);
    IdentifierWithDates cusip = IdentifierWithDates.of(Identifier.of("CUSIP", "EDU0"), LocalDate.of(2010, 9, 14), LocalDate.of(2020, 9, 14));
    IdentifierWithDates buid = IdentifierWithDates.of(Identifier.of("BUID", "IX-1234"), null, null);
    
    IdentifierBundleWithDates bundle = IdentifierBundleWithDates.of(edu0, edu20, cusip, buid);
    
    LocalDateDoubleTimeSeries ts = makeRandomTimeSeries(7);
    HistoricalTimeSeriesDocument tsDoc = createTimeSeries("PX_LAST", "CMPL", "BBG", bundle, ts);
    assertNotNull(tsDoc);
    HistoricalTimeSeriesDocument loadedTs = _master.get(tsDoc.getUniqueId());
    assertEquals(loadedTs.getSeries().getTimeSeries(), ts);
    
    ts = makeRandomTimeSeries(7);
    tsDoc = createTimeSeries("VOLUME", "CMPL", "BBG", bundle, ts);
    assertNotNull(tsDoc);
    loadedTs = _master.get(tsDoc.getUniqueId());
    assertEquals(loadedTs.getSeries().getTimeSeries(), ts); 
  }

  private void setIdentifiers(IdentifierBundle identifiers, ManageableHistoricalTimeSeries series) {
    series.setIdentifiers(IdentifierBundleWithDates.of(identifiers));
  }

  @Test
  public void updateTimeSeries() throws Exception {
    List<HistoricalTimeSeriesDocument> tsList = addAndTestTimeSeries();
    assertFalse(tsList.isEmpty());
    HistoricalTimeSeriesDocument tsDoc = getRandomTimeSeriesDocument(tsList);
    
    tsDoc.getSeries().setTimeSeries(makeRandomTimeSeries(DEFAULT_START, 7));
    HistoricalTimeSeriesDocument updatedDoc = _master.update(tsDoc);
    assertNotNull(updatedDoc);
    assertNotNull(updatedDoc.getUniqueId());
    assertEquals(tsDoc.getUniqueId(), updatedDoc.getUniqueId());
    
    assertEqualTimeSeriesDocument(updatedDoc, _master.get(updatedDoc.getUniqueId()));
    
    //delete dataPoints, set with empty timeseries
    tsDoc.getSeries().setTimeSeries(getEmptyTimeSeries()); 
    updatedDoc = _master.update(tsDoc);
    assertNotNull(updatedDoc);
    assertNotNull(updatedDoc.getUniqueId());
    assertEquals(tsDoc.getUniqueId(), updatedDoc.getUniqueId());
    
    assertEqualTimeSeriesDocument(updatedDoc, _master.get(updatedDoc.getUniqueId()));
    
  }
  
  private HistoricalTimeSeriesDocument getRandomTimeSeriesDocument(List<HistoricalTimeSeriesDocument> tsList) {
    int randIndex = _random.nextInt(tsList.size());
    return tsList.get(randIndex);
  }

  @Test
  public void removeTimeSeries() throws Exception {
    List<HistoricalTimeSeriesDocument> tsList = addAndTestTimeSeries();
    HistoricalTimeSeriesDocument tsDoc = getRandomTimeSeriesDocument(tsList);
    
    _master.remove(tsDoc.getUniqueId());
    try {
      _master.get(tsDoc.getUniqueId());
      fail();
    } catch(DataNotFoundException ex) {
      //do nothing
    }
  }
  
  @Test
  public void removeThenAddTimeseries() throws Exception {
    List<HistoricalTimeSeriesDocument> tsList = addAndTestTimeSeries();
    HistoricalTimeSeriesDocument doc = getRandomTimeSeriesDocument(tsList);
    ManageableHistoricalTimeSeries series = doc.getSeries();
    _master.remove(doc.getUniqueId());
    try {
      _master.get(doc.getUniqueId());
      fail();
    } catch(DataNotFoundException ex) {
      //do nothing
    }
    LocalDateDoubleTimeSeries currentTS = makeRandomTimeSeries(7);
    HistoricalTimeSeriesDocument currentTSDoc = createTimeSeries(series.getDataField(),
        series.getDataProvider(), series.getDataSource(), series.getIdentifiers(), currentTS);
    assertNotNull(currentTSDoc);
    assertNotNull(currentTSDoc.getUniqueId());
    
    HistoricalTimeSeriesDocument actualDoc = _master.get(currentTSDoc.getUniqueId());
    assertNotNull(actualDoc);
    assertEquals(currentTS, actualDoc.getSeries().getTimeSeries());
    
    _master.remove(currentTSDoc.getUniqueId());
    try {
      _master.get(currentTSDoc.getUniqueId());
      fail();
    } catch(DataNotFoundException ex) {
      //do nothing
    }
    currentTS = makeRandomTimeSeries(7);
    currentTSDoc = createTimeSeries(series.getDataField(),
        series.getDataProvider(), series.getDataSource(), series.getIdentifiers(), currentTS);
    assertNotNull(currentTSDoc);
    assertNotNull(currentTSDoc.getUniqueId());
    
    actualDoc = _master.get(currentTSDoc.getUniqueId());
    assertNotNull(actualDoc);
    assertEquals(currentTS, actualDoc.getSeries().getTimeSeries());
  }

  @Test
  public void getUnknownUID() throws Exception {
    addAndTestTimeSeries();
    try {
      _master.get(UniqueIdentifier.of(DbHistoricalTimeSeriesMaster.IDENTIFIER_SCHEME_DEFAULT, String.valueOf(Long.MIN_VALUE)));
      fail();
    } catch(DataNotFoundException ex) {
      //do nothing
    }
  }
  
  @Test 
  public void getInvalidUID() throws Exception {
    addAndTestTimeSeries();
    try {
      _master.get(UniqueIdentifier.of("INVALID", "unknown"));
      fail();
    } catch(IllegalArgumentException ex) {
      //do nothing
    }
  }
  
  @Test
  public void resolveIdentifier() throws Exception {
    List<HistoricalTimeSeriesDocument> tsList = addAndTestTimeSeries();
    HistoricalTimeSeriesDocument doc = getRandomTimeSeriesDocument(tsList);
    
    ManageableHistoricalTimeSeries series = doc.getSeries();
    IdentifierBundle bundle = series.getIdentifiers().asIdentifierBundle();
    UniqueIdentifier resolveIdentifier = _master.resolveIdentifier(bundle, series.getDataSource(), series.getDataProvider(), series.getDataField());
    assertNotNull(resolveIdentifier);
    assertEquals(doc.getUniqueId(), resolveIdentifier);
    
    for (Identifier identifier : bundle) {
      resolveIdentifier = _master.resolveIdentifier(IdentifierBundle.of(identifier), series.getDataSource(), series.getDataProvider(), series.getDataField());
      assertNotNull(resolveIdentifier);
      assertEquals(doc.getUniqueId(), resolveIdentifier);
    }
    
    resolveIdentifier = _master.resolveIdentifier(bundle, "INVALID", CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
    assertNull(resolveIdentifier);
    resolveIdentifier = _master.resolveIdentifier(bundle, BBG_DATA_SOURCE, "INVALID", CLOSE_DATA_FIELD);
    assertNull(resolveIdentifier);
    
    resolveIdentifier = _master.resolveIdentifier(bundle, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, "INVALID");
    assertNull(resolveIdentifier);
        
    resolveIdentifier = _master.resolveIdentifier(IdentifierBundle.of(Identifier.of("Invalid", "Invalid")), BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
    assertNull(resolveIdentifier);
    
    //check null identifiers
    try {
      IdentifierBundle identifiers = null;
      _master.resolveIdentifier(identifiers, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
      fail();
    } catch (IllegalArgumentException ex) {
      //do nothing
    }
  }
  
  @Test
  public void getHistoricalTimeSeriesWithoutDataProvider() throws Exception {
    Map<String, LocalDateDoubleTimeSeries> expectedTSMap = new HashMap<String, LocalDateDoubleTimeSeries>();
    
    IdentifierBundle bundle = IdentifierBundle.of(SecurityUtils.bloombergTickerSecurityId("id1"));
    for (String dataProvider : DATA_PROVIDERS) {
      
      LocalDateDoubleTimeSeries timeSeries = makeRandomTimeSeries(DEFAULT_START, 7);
      
      ManageableHistoricalTimeSeries series = new ManageableHistoricalTimeSeries();
      series.setDataField(CLOSE_DATA_FIELD);
      series.setDataProvider(dataProvider);
      series.setDataSource(BBG_DATA_SOURCE);
      series.setObservationTime(LCLOSE_OBSERVATION_TIME);
      setIdentifiers(bundle, series);
      series.setTimeSeries(timeSeries);
      
      HistoricalTimeSeriesDocument doc = _master.add(new HistoricalTimeSeriesDocument(series));
      assertNotNull(doc);
      assertNotNull(doc.getUniqueId());
      
      expectedTSMap.put(dataProvider, timeSeries);
    }
    
    //check you get expected timeseries with dataProvider specified
    for (String dataProvider : DATA_PROVIDERS) {
      HistoricalTimeSeriesSearchResult searchResult = search(null, bundle, CLOSE_DATA_FIELD, dataProvider, BBG_DATA_SOURCE, LCLOSE_OBSERVATION_TIME, true, false);
      
      assertNotNull(searchResult);
      List<HistoricalTimeSeriesDocument> documents = searchResult.getDocuments();
      assertNotNull(documents);
      assertTrue(documents.size() == 1);
      
      HistoricalTimeSeriesDocument searchedDoc = documents.get(0);
      assertNotNull(searchedDoc);
      
      assertEquals(expectedTSMap.get(dataProvider), searchedDoc.getSeries().getTimeSeries());
    }
    
    //search without dataProvider
    HistoricalTimeSeriesSearchResult searchResult = search(null, bundle, CLOSE_DATA_FIELD, null, BBG_DATA_SOURCE, LCLOSE_OBSERVATION_TIME, true, false);
    assertNotNull(searchResult);
    List<HistoricalTimeSeriesDocument> documents = searchResult.getDocuments();
    assertNotNull(documents);
    assertTrue(documents.size() == expectedTSMap.entrySet().size());
    for (HistoricalTimeSeriesDocument tsDoc : documents) {
      String dataProvider = tsDoc.getSeries().getDataProvider();
      LocalDateDoubleTimeSeries actualTS = tsDoc.getSeries().getTimeSeries();
      assertEquals(expectedTSMap.get(dataProvider), actualTS);
    }
        
  }
    
  @Test
  public void appendTimeSeries() throws Exception {
    List<HistoricalTimeSeriesDocument> tsList = addAndTestTimeSeries();
    HistoricalTimeSeriesDocument tsDoc = getRandomTimeSeriesDocument(tsList);
    
    LocalDateDoubleTimeSeries timeSeries = tsDoc.getSeries().getTimeSeries();
    LocalDate start = timeSeries.getLatestTime().plusDays(1);
    LocalDateDoubleTimeSeries appendedTS = makeRandomTimeSeries(start, 7);
    LocalDateDoubleTimeSeries mergedTS = timeSeries.noIntersectionOperation(appendedTS).toLocalDateDoubleTimeSeries();
    // append timeseries to existing identifiers in the datastore
    tsDoc.getSeries().setTimeSeries(appendedTS);
    _master.appendTimeSeries(tsDoc);
    
    HistoricalTimeSeriesDocument latestDoc = _master.get(tsDoc.getUniqueId());
    assertNotNull(latestDoc);
    tsDoc.getSeries().setTimeSeries(mergedTS);
    assertEqualTimeSeriesDocument(tsDoc, latestDoc);
    
  }
  
  @Test
  public void searchNotAvailableTimeSeries() throws Exception {
    addAndTestTimeSeries();
    IdentifierBundle bundle = IdentifierBundle.of(Identifier.of("BLOOMBERG_TICKER", "AAPL US Equity"), Identifier.of("BUID", "X-12345678"));
    HistoricalTimeSeriesSearchResult searchResult = search(null, bundle, CLOSE_DATA_FIELD, CMPL_DATA_PROVIDER, BBG_DATA_SOURCE, null, true, false);
    assertNotNull(searchResult);
    assertNotNull(searchResult.getDocuments());
    assertTrue(searchResult.getDocuments().isEmpty());
  }
  
  @Test
  public void searchMetaData() throws Exception {
    List<HistoricalTimeSeriesDocument> tsList = addAndTestTimeSeries();
    List<HistoricalTimeSeriesDocument> closeDataFields = new ArrayList<HistoricalTimeSeriesDocument>();
    List<HistoricalTimeSeriesDocument> cmplDataProviders = new ArrayList<HistoricalTimeSeriesDocument>();
    List<HistoricalTimeSeriesDocument> bbgDataSources = new ArrayList<HistoricalTimeSeriesDocument>();
    List<HistoricalTimeSeriesDocument> lcloseObservations = new ArrayList<HistoricalTimeSeriesDocument>();
        
    for (HistoricalTimeSeriesDocument doc : tsList) {
      //set timeseries to null for metadata test
      ManageableHistoricalTimeSeries series = doc.getSeries();
      series.setTimeSeries(null);
      if (series.getDataField().equals(CLOSE_DATA_FIELD)) {
        closeDataFields.add(doc);
      }
      if (series.getDataProvider().equals(CMPL_DATA_PROVIDER)) {
        cmplDataProviders.add(doc);
      }
      if (series.getDataSource().equals(BBG_DATA_SOURCE)) {
        bbgDataSources.add(doc);
      }
      if (series.getObservationTime().equals(LCLOSE_OBSERVATION_TIME)) {
        lcloseObservations.add(doc);
      }
    }
    //return all timeseries meta data without loading timeseries data points
    HistoricalTimeSeriesSearchResult searchResult = search(null, null, null, null, null, null, false, false);
    assertNotNull(searchResult);
    List<HistoricalTimeSeriesDocument> documents = searchResult.getDocuments();
    assertNotNull(documents);
    assertTrue(tsList.size() == documents.size());
    for (HistoricalTimeSeriesDocument expectedDoc : tsList) {
      assertTrue(documents.contains(expectedDoc));
    }
    
    searchResult = search(null, null, CLOSE_DATA_FIELD, null, null, null, false, false);
    documents = searchResult.getDocuments();
    assertTrue(closeDataFields.size() == documents.size());
    for (HistoricalTimeSeriesDocument testDoc : documents) {
      assertTrue(closeDataFields.contains(testDoc));
    }

    searchResult = search(null, null, null, null, BBG_DATA_SOURCE, null, false, false);
    documents = searchResult.getDocuments();
    assertTrue(bbgDataSources.size() == documents.size());
    for (HistoricalTimeSeriesDocument testDoc : documents) {
      assertTrue(bbgDataSources.contains(testDoc));
    }
    
    searchResult = search(null, null, null, null, null, LCLOSE_OBSERVATION_TIME, false, false);
    documents = searchResult.getDocuments();
    assertTrue(lcloseObservations.size() == documents.size());
    for (HistoricalTimeSeriesDocument testDoc : documents) {
      assertTrue(lcloseObservations.contains(testDoc));
    }
    
    searchResult = search(null, null, null, CMPL_DATA_PROVIDER, null, null, false, false);
    documents = searchResult.getDocuments();
    assertTrue(cmplDataProviders.size() == documents.size());
    for (HistoricalTimeSeriesDocument testDoc : documents) {
      assertTrue(cmplDataProviders.contains(testDoc));
    }
  }
  
  @Test
  public void searchMetaDataWithDates() throws Exception {
    List<HistoricalTimeSeriesDocument> tsList = addAndTestTimeSeries();
    //return all timeseries meta data with dates without loading timeseries data points
    HistoricalTimeSeriesSearchResult searchResult = search(null, null, null, null, null, null, false, true);
    assertNotNull(searchResult);
    List<HistoricalTimeSeriesDocument> documents = searchResult.getDocuments();
    assertNotNull(documents);
    assertTrue(tsList.size() == documents.size());
    
    HistoricalTimeSeriesDocument doc = getRandomTimeSeriesDocument(tsList);
    //set timeseries to null for metadata test and set dates
    doc.setEarliest(doc.getSeries().getTimeSeries().getEarliestTime());
    doc.setLatest(doc.getSeries().getTimeSeries().getLatestTime());
    doc.getSeries().setTimeSeries(null);
    assertTrue(documents.contains(doc));
    
  }
  
  @Test
  public void addDataPoint() throws Exception {
    List<HistoricalTimeSeriesDocument> tsList = addAndTestTimeSeries();
    HistoricalTimeSeriesDocument tsDoc = getRandomTimeSeriesDocument(tsList);
    
    LocalDateDoubleTimeSeries timeSeries = tsDoc.getSeries().getTimeSeries();
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
    LocalDateDoubleTimeSeries updatedTS = getTimeSeries(dates, values); 
    
    String scheme = tsDoc.getUniqueId().getScheme();
    String timeSeriesID = tsDoc.getUniqueId().getValue();
    DataPointDocument dataPointDocument = new DataPointDocument();
    dataPointDocument.setHistoricalTimeSeriesId(tsDoc.getUniqueId());
    dataPointDocument.setDate(date);
    dataPointDocument.setValue(value);
    
    dataPointDocument = _master.addDataPoint(dataPointDocument);
    assertNotNull(dataPointDocument);
    assertEquals(UniqueIdentifier.of(scheme, timeSeriesID + "/" + print(date)), dataPointDocument.getDataPointId());
    HistoricalTimeSeriesDocument updatedDoc = _master.get(tsDoc.getUniqueId());
    assertNotNull(updatedDoc);
    assertNotNull(updatedDoc.getUniqueId());
    assertEquals(updatedTS, updatedDoc.getSeries().getTimeSeries());
    
    DataPointDocument actualDDoc = _master.getDataPoint(dataPointDocument.getDataPointId());
    assertEquals(tsDoc.getUniqueId(), actualDDoc.getHistoricalTimeSeriesId());
    assertEquals(dataPointDocument.getDataPointId(), actualDDoc.getDataPointId());
    assertEquals(dataPointDocument.getDate(), actualDDoc.getDate());
    assertEquals(dataPointDocument.getValue(), actualDDoc.getValue());
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void addDataPointWithoutTSID() throws Exception {
    DataPointDocument dataPointDocument = new DataPointDocument();
    dataPointDocument.setDate(LocalDate.of(2000, 1, 2));
    dataPointDocument.setValue(Math.random());
    dataPointDocument = _master.addDataPoint(dataPointDocument);
  }
  
  
  @Test
  public void updateDataPoint() throws Exception {
    List<HistoricalTimeSeriesDocument> tsList = addAndTestTimeSeries();
    HistoricalTimeSeriesDocument tsDoc = getRandomTimeSeriesDocument(tsList);
    
    LocalDateDoubleTimeSeries timeSeries = tsDoc.getSeries().getTimeSeries();
    //update datapoint
    List<LocalDate> dates = timeSeries.times();
    List<Double> values = timeSeries.values();
    int updateIdx = _random.nextInt(timeSeries.size());
    LocalDate date = timeSeries.getTime(updateIdx);
    double newValue = _random.nextDouble();
    values.set(updateIdx, newValue);
    
    LocalDateDoubleTimeSeries updatedTS = getTimeSeries(dates, values);
    
    String scheme = tsDoc.getUniqueId().getScheme();
    String timeSeriesID = tsDoc.getUniqueId().getValue();
    DataPointDocument dataPointDocument = new DataPointDocument();
    dataPointDocument.setHistoricalTimeSeriesId(tsDoc.getUniqueId());
    dataPointDocument.setDataPointId(UniqueIdentifier.of(scheme, timeSeriesID + "/" + print(date)));
    dataPointDocument.setDate(date);
    dataPointDocument.setValue(newValue);
    
    DataPointDocument updated = _master.updateDataPoint(dataPointDocument);
    assertNotNull(updated);
    assertEquals(dataPointDocument.getDataPointId(), updated.getDataPointId());
    assertEquals(dataPointDocument.getHistoricalTimeSeriesId(), updated.getHistoricalTimeSeriesId());
    assertEquals(dataPointDocument.getDate(), updated.getDate());
    assertEquals(dataPointDocument.getValue(), updated.getValue());
    
    HistoricalTimeSeriesDocument updatedDoc = _master.get(tsDoc.getUniqueId());
    assertNotNull(updatedDoc);
    assertNotNull(updatedDoc.getUniqueId());
    assertEquals(updatedTS, updatedDoc.getSeries().getTimeSeries());
    
  }
  
  @Test
  public void removeDataPoint() throws Exception {
    List<HistoricalTimeSeriesDocument> tsList = addAndTestTimeSeries();
    
    HistoricalTimeSeriesDocument tsDoc = getRandomTimeSeriesDocument(tsList);
    LocalDateDoubleTimeSeries timeSeries = tsDoc.getSeries().getTimeSeries();
    //delete random datapoints
    List<LocalDate> dates = timeSeries.times();
    List<Double> values = timeSeries.values();
    int deleteIdx = _random.nextInt(timeSeries.size());
    LocalDate deletedDate = dates.remove(deleteIdx);
    values.remove(deleteIdx);
    
    LocalDateDoubleTimeSeries deletedTS = getTimeSeries(dates, values);
    String scheme = tsDoc.getUniqueId().getScheme();
    String tsId = tsDoc.getUniqueId().getValue();
    _master.removeDataPoint(UniqueIdentifier.of(scheme, tsId + "/" + print(deletedDate)));
    
    HistoricalTimeSeriesDocument updatedDoc = _master.get(tsDoc.getUniqueId());
    assertNotNull(updatedDoc);
    assertNotNull(updatedDoc.getUniqueId());
    assertEquals(deletedTS, updatedDoc.getSeries().getTimeSeries());
    
  }
  
  @Test
  public void removeDataPoints() throws Exception {
    List<HistoricalTimeSeriesDocument> tsList = addAndTestTimeSeries();
    HistoricalTimeSeriesDocument tsDoc = getRandomTimeSeriesDocument(tsList);
    
    LocalDateDoubleTimeSeries timeSeries = tsDoc.getSeries().getTimeSeries();
    int originalSize = timeSeries.size();
    int desiredSize = originalSize / 2;
    LocalDate firstDateToRetain = timeSeries.getTime(timeSeries.size() - desiredSize);
    _master.removeDataPoints(tsDoc.getUniqueId(), firstDateToRetain);
    
    HistoricalTimeSeriesDocument updatedDoc = _master.get(tsDoc.getUniqueId());

    assertNotNull(updatedDoc);
    assertNotNull(updatedDoc.getUniqueId());
    
    assertEquals(desiredSize, updatedDoc.getSeries().getTimeSeries().size());
    assertEquals(firstDateToRetain, updatedDoc.getSeries().getTimeSeries().getEarliestTime());
    assertEquals(timeSeries.getLatestTime(), updatedDoc.getSeries().getTimeSeries().getLatestTime());
    
  }
  
  @Test
  public void getTimeSeriesSnapShot() throws Exception {
    IdentifierBundle identifiers = IdentifierBundle.of(Identifier.of("sa", "ida"), Identifier.of("sb", "idb"));
    
    SortedMap<ZonedDateTime, LocalDateDoubleTimeSeries> timeStampTSMap = new TreeMap<ZonedDateTime, LocalDateDoubleTimeSeries>();
    LocalDateDoubleTimeSeries timeSeries = makeRandomTimeSeries(DEFAULT_START, 7);
    
    SortedMap<LocalDate, Double> currentTimeSeriesMap = new TreeMap<LocalDate, Double>();
    for (int i = 0; i < timeSeries.size(); i++) {
      currentTimeSeriesMap.put(timeSeries.getTime(i), timeSeries.getValueAt(i));
    }
    
    ManageableHistoricalTimeSeries series = new ManageableHistoricalTimeSeries();
    series.setDataField(CLOSE_DATA_FIELD);
    series.setDataProvider(CMPL_DATA_PROVIDER);
    series.setDataSource(BBG_DATA_SOURCE);
    series.setObservationTime(LCLOSE_OBSERVATION_TIME);
    setIdentifiers(identifiers, series);
    series.setTimeSeries(timeSeries);
    HistoricalTimeSeriesDocument doc = new HistoricalTimeSeriesDocument(series);
    
    doc = _master.add(doc);
    
    assertNotNull(doc);
    assertNotNull(doc.getUniqueId());
    
    HistoricalTimeSeriesDocument actualDoc = _master.get(doc.getUniqueId());
    assertNotNull(actualDoc);
    assertEqualTimeSeriesDocument(doc, actualDoc);
    
    Thread.sleep(50); // assume system clock resolution < 50ms
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
      dataPointDocument.setHistoricalTimeSeriesId(doc.getUniqueId());
      dataPointDocument.setDate(updateDate);
      dataPointDocument.setValue(newValue);
      _master.updateDataPoint(dataPointDocument);
      doc = _master.get(doc.getUniqueId());
      assertNotNull(doc);
      timeSeries = getTimeSeries(new ArrayList<LocalDate>(currentTimeSeriesMap.keySet()), new ArrayList<Double>(currentTimeSeriesMap.values()));
      assertEquals(timeSeries, doc.getSeries().getTimeSeries()); 
      
      Thread.sleep(50); // assume system clock resolution < 50ms
      timeStampTSMap.put(Clock.system(javax.time.calendar.TimeZone.UTC).zonedDateTime(), timeSeries);
    }
    
    //delete a datapoint
    List<LocalDate> times = timeSeries.times();
    int ranIndx = _random.nextInt(times.size());
    LocalDate deleteDate = times.get(ranIndx);
    currentTimeSeriesMap.remove(deleteDate);
    UniqueIdentifier dataPointId = UniqueIdentifier.of(doc.getUniqueId().getScheme(), doc.getUniqueId().getValue() + "/" + print(deleteDate));
    _master.removeDataPoint(dataPointId);
    doc = _master.get(doc.getUniqueId());
    assertNotNull(doc);
    timeSeries = getTimeSeries(new ArrayList<LocalDate>(currentTimeSeriesMap.keySet()), new ArrayList<Double>(currentTimeSeriesMap.values()));
    assertEquals(timeSeries, doc.getSeries().getTimeSeries()); 
    Thread.sleep(50); // assume system clock resolution < 50ms
    timeStampTSMap.put(Clock.system(javax.time.calendar.TimeZone.UTC).zonedDateTime(), timeSeries);
    
    //delete all datapoints
    doc.getSeries().setTimeSeries(getEmptyTimeSeries());
    _master.update(doc);
    doc = _master.get(doc.getUniqueId());
    assertNotNull(doc);
    timeSeries = getEmptyTimeSeries();
    assertEquals(timeSeries, doc.getSeries().getTimeSeries()); 
    Thread.sleep(50); // assume system clock resolution < 50ms
    timeStampTSMap.put(Clock.system(javax.time.calendar.TimeZone.UTC).zonedDateTime(), timeSeries);
    
    //add new datapoints
    timeSeries = makeRandomTimeSeries(DEFAULT_START, 7);
    doc.getSeries().setTimeSeries(timeSeries);
    _master.update(doc);
    doc = _master.get(doc.getUniqueId());
    assertNotNull(doc);
    assertEquals(timeSeries, doc.getSeries().getTimeSeries());
    Thread.sleep(50); // assume system clock resolution < 50ms
    timeStampTSMap.put(Clock.system(javax.time.calendar.TimeZone.UTC).zonedDateTime(), timeSeries);
    
    //assert datasnapshots
    for (Entry<ZonedDateTime, LocalDateDoubleTimeSeries> entry : timeStampTSMap.entrySet()) {
      ZonedDateTime timeStamp = entry.getKey();
      LocalDateDoubleTimeSeries expectedTS = entry.getValue();
      HistoricalTimeSeriesDocument snapshotDoc = getTimeSeriesSnapShot(identifiers, timeStamp);
      assertNotNull(snapshotDoc);
      assertEquals(expectedTS.times(), snapshotDoc.getSeries().getTimeSeries().times());
      assertEquals(expectedTS.values(), snapshotDoc.getSeries().getTimeSeries().values());
    }
    
    //assert before and after last deltas
    //before 1st delta should return empty timeseries
    ZonedDateTime beforeDelta = timeStampTSMap.firstKey().minusMinutes(1);
    HistoricalTimeSeriesDocument snapshotDoc = getTimeSeriesSnapShot(identifiers, beforeDelta);
    assertEquals(new ArrayLocalDateDoubleTimeSeries(), snapshotDoc.getSeries().getTimeSeries());
    //after last delta should return latest timeseries
    ZonedDateTime afterDelta = timeStampTSMap.lastKey().plusMinutes(1);
    doc = _master.get(doc.getUniqueId());
    assertNotNull(doc);
    assertEquals(timeStampTSMap.get(timeStampTSMap.lastKey()), doc.getSeries().getTimeSeries());
    snapshotDoc = getTimeSeriesSnapShot(identifiers, afterDelta);
    assertEquals(doc.getSeries().getTimeSeries(), snapshotDoc.getSeries().getTimeSeries());
    
  }

  private HistoricalTimeSeriesDocument getTimeSeriesSnapShot(IdentifierBundle identifiers, ZonedDateTime timeStamp) {
    HistoricalTimeSeriesSearchHistoricRequest searchHistoricRequest = new HistoricalTimeSeriesSearchHistoricRequest();
    searchHistoricRequest.setDataProvider(CMPL_DATA_PROVIDER);
    searchHistoricRequest.setDataSource(BBG_DATA_SOURCE);
    searchHistoricRequest.setDataField(CLOSE_DATA_FIELD);
    searchHistoricRequest.setIdentifiers(identifiers);
    searchHistoricRequest.setObservationTime(LCLOSE_OBSERVATION_TIME);
    searchHistoricRequest.setTimestamp(timeStamp.toInstant());
    HistoricalTimeSeriesSearchHistoricResult searchHistoric = _master.searchHistoric(searchHistoricRequest);
    assertNotNull(searchHistoric);
    List<HistoricalTimeSeriesDocument> documents = searchHistoric.getDocuments();
    //should expect one single document back
    assertTrue(documents.size() == 1);
    return documents.get(0);
  }
  
  private void assertEqualTimeSeriesDocument(HistoricalTimeSeriesDocument expectedDoc, HistoricalTimeSeriesDocument actualDoc) {
    assertNotNull(expectedDoc);
    assertNotNull(actualDoc);
    assertEquals(expectedDoc.getUniqueId(), actualDoc.getUniqueId());
    assertEquals(expectedDoc.getSeries(), actualDoc.getSeries());
  }
  
  public LocalDateDoubleTimeSeries makeRandomTimeSeries(int numDays) {
    LocalDate previousWeekDay = DateUtil.previousWeekDay();
    return makeRandomTimeSeries(previousWeekDay, numDays);
  }
  
  public LocalDateDoubleTimeSeries makeRandomTimeSeries(LocalDate start, int numDays) {
    MapLocalDateDoubleTimeSeries tsMap = RandomTimeSeriesGenerator.makeRandomTimeSeries(start, numDays);
    return getTimeSeries(tsMap);
  }
  
  private static boolean isWeekday(LocalDate day) {
    return (day.getDayOfWeek() != DayOfWeek.SATURDAY && day.getDayOfWeek() != DayOfWeek.SUNDAY);
  }
  
  @Test
  public void identifiersWithDates() throws Exception {
    addAndTestTimeSeries();
    
    Map<Identifier, LocalDateDoubleTimeSeries> expectedTS = new HashMap<Identifier, LocalDateDoubleTimeSeries>();
    
    //add EDU10 Comdty    
    LocalDate start = DateUtil.previousWeekDay().minusDays(7);
    LocalDateDoubleTimeSeries timeSeries = makeRandomTimeSeries(start, 7);
    Identifier edu0Id = SecurityUtils.bloombergTickerSecurityId("EDU0 Comdty");
    IdentifierWithDates edu0 = IdentifierWithDates.of(edu0Id, LocalDate.of(2000, MonthOfYear.SEPTEMBER, 19), LocalDate.of(2010, MonthOfYear.SEPTEMBER, 13));
    IdentifierWithDates edu10 = IdentifierWithDates.of(SecurityUtils.bloombergTickerSecurityId("EDU10 Comdty"), LocalDate.of(2010, MonthOfYear.SEPTEMBER, 14), null);
    Identifier edu10Buid = SecurityUtils.bloombergBuidSecurityId("IX613196-0");
    IdentifierWithDates eduBuid = IdentifierWithDates.of(edu10Buid, null, null);
    HistoricalTimeSeriesDocument tsDocument = createTimeSeries(CLOSE_DATA_FIELD, CMPL_DATA_PROVIDER, BBG_DATA_SOURCE, IdentifierBundleWithDates.of(new IdentifierWithDates[]{edu0, edu10, eduBuid}), timeSeries);
    assertNotNull(tsDocument);
    assertNotNull(tsDocument.getUniqueId());
    
    HistoricalTimeSeriesDocument actualDoc = _master.get(tsDocument.getUniqueId());
    assertNotNull(actualDoc);
    assertEquals(timeSeries, actualDoc.getSeries().getTimeSeries());
    expectedTS.put(edu10Buid, timeSeries);
    
    //add EDU20 Comdty
    timeSeries = makeRandomTimeSeries(start, 7);
    Identifier edu20Buid = SecurityUtils.bloombergBuidSecurityId("IX11084074-0");
    edu0 = IdentifierWithDates.of(edu0Id, LocalDate.of(2010, MonthOfYear.SEPTEMBER, 14), LocalDate.of(2020, MonthOfYear.SEPTEMBER, 14));
    IdentifierWithDates edu20 = IdentifierWithDates.of(SecurityUtils.bloombergTickerSecurityId("EDU20 Comdty"), LocalDate.of(2010, MonthOfYear.SEPTEMBER, 15), null);
    eduBuid = IdentifierWithDates.of(edu20Buid, null, null);
    tsDocument = createTimeSeries(CLOSE_DATA_FIELD, CMPL_DATA_PROVIDER, BBG_DATA_SOURCE, IdentifierBundleWithDates.of(new IdentifierWithDates[]{edu0, edu20, eduBuid}), timeSeries);
    assertNotNull(tsDocument);
    assertNotNull(tsDocument.getUniqueId());
    
    actualDoc = _master.get(tsDocument.getUniqueId());
    assertNotNull(actualDoc);
    assertEquals(timeSeries, actualDoc.getSeries().getTimeSeries());
    expectedTS.put(edu20Buid, timeSeries);
    
    //------------------------------------------------------------------------
    //lookup using edu0 with current date
    
    LocalDate validFrom = LocalDate.of(2000, MonthOfYear.SEPTEMBER, 19);
    LocalDate validTo = LocalDate.of(2010, MonthOfYear.SEPTEMBER, 13);
    
    //search before edu0
    HistoricalTimeSeriesSearchResult searchResult = search(validFrom.minusDays(1), IdentifierBundle.of(edu0Id), CLOSE_DATA_FIELD, CMPL_DATA_PROVIDER, BBG_DATA_SOURCE, LCLOSE_OBSERVATION_TIME, true, false);
    assertNotNull(searchResult);
    List<HistoricalTimeSeriesDocument> documents = searchResult.getDocuments();
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
      searchResult = search(currentDate, IdentifierBundle.of(edu0Id), CLOSE_DATA_FIELD, CMPL_DATA_PROVIDER, BBG_DATA_SOURCE, LCLOSE_OBSERVATION_TIME, true, false);
      assertNotNull(searchResult);
      documents = searchResult.getDocuments();
      assertNotNull(documents);
      assertTrue(documents.size() == 1);
      HistoricalTimeSeriesDocument tsDoc = documents.get(0);
      assertEquals(expectedTS.get(edu10Buid), tsDoc.getSeries().getTimeSeries());
      
      searchResult = search(currentDate, IdentifierBundle.of(edu0Id, edu10Buid), CLOSE_DATA_FIELD, CMPL_DATA_PROVIDER, BBG_DATA_SOURCE, LCLOSE_OBSERVATION_TIME, true, false);
      assertNotNull(searchResult);
      documents = searchResult.getDocuments();
      assertNotNull(documents);
      assertTrue(documents.size() == 1);
      tsDoc = documents.get(0);
      assertEquals(expectedTS.get(edu10Buid), tsDoc.getSeries().getTimeSeries());
      
    }
    
    //search a day after valid_to of edu0 should return edu20 series
    searchResult = search(validTo.plusDays(1), IdentifierBundle.of(edu0Id), CLOSE_DATA_FIELD, CMPL_DATA_PROVIDER, BBG_DATA_SOURCE, LCLOSE_OBSERVATION_TIME, true, false);
    assertNotNull(searchResult);
    documents = searchResult.getDocuments();
    assertNotNull(documents);
    assertTrue(documents.size() == 1);
    HistoricalTimeSeriesDocument tsDoc = documents.get(0);
    assertEquals(expectedTS.get(edu20Buid), tsDoc.getSeries().getTimeSeries());
    
    //search after edu20 should return no series
    searchResult = search(LocalDate.of(2020, MonthOfYear.SEPTEMBER, 15), IdentifierBundle.of(edu0Id), CLOSE_DATA_FIELD, CMPL_DATA_PROVIDER, BBG_DATA_SOURCE, LCLOSE_OBSERVATION_TIME, true, false);
    assertNotNull(searchResult);
    documents = searchResult.getDocuments();
    assertNotNull(documents);
    assertTrue(documents.isEmpty());
    
    //search using buids should return correct series
    searchResult = search(null, IdentifierBundle.of(edu10Buid), CLOSE_DATA_FIELD, CMPL_DATA_PROVIDER, BBG_DATA_SOURCE, LCLOSE_OBSERVATION_TIME, true, false);
    assertNotNull(searchResult);
    assertNotNull(searchResult.getDocuments());
    assertTrue(searchResult.getDocuments().size() == 1);
    assertEquals(expectedTS.get(edu10Buid), searchResult.getDocuments().get(0).getSeries().getTimeSeries());
    
    searchResult = search(null, IdentifierBundle.of(edu20Buid), CLOSE_DATA_FIELD, CMPL_DATA_PROVIDER, BBG_DATA_SOURCE, LCLOSE_OBSERVATION_TIME, true, false);
    assertNotNull(searchResult);
    assertNotNull(searchResult.getDocuments());
    assertTrue(searchResult.getDocuments().size() == 1);
    assertEquals(expectedTS.get(edu20Buid), searchResult.getDocuments().get(0).getSeries().getTimeSeries());
    
    //search using edu0 without current date should return 2 series
    searchResult = search(null, IdentifierBundle.of(edu0Id), CLOSE_DATA_FIELD, CMPL_DATA_PROVIDER, BBG_DATA_SOURCE, LCLOSE_OBSERVATION_TIME, true, false);
    assertNotNull(searchResult);
    assertNotNull(searchResult.getDocuments());
    assertTrue(searchResult.getDocuments().size() == 2);
    LocalDateDoubleTimeSeries ts1 = searchResult.getDocuments().get(0).getSeries().getTimeSeries();
    LocalDateDoubleTimeSeries ts2 = searchResult.getDocuments().get(1).getSeries().getTimeSeries();
    assertFalse(ts1.equals(ts2));
    assertTrue(expectedTS.values().contains(ts1));
    assertTrue(expectedTS.values().contains(ts2));
    
    //search edu10 without date
    searchResult = search(null, IdentifierBundle.of(SecurityUtils.bloombergTickerSecurityId("EDU10 Comdty")), CLOSE_DATA_FIELD, CMPL_DATA_PROVIDER, BBG_DATA_SOURCE, LCLOSE_OBSERVATION_TIME, true, false);
    assertNotNull(searchResult);
    assertNotNull(searchResult.getDocuments());
    assertTrue(searchResult.getDocuments().size() == 1);
    assertEquals(expectedTS.get(edu10Buid), searchResult.getDocuments().get(0).getSeries().getTimeSeries());
    
    //search edu20 without date
    searchResult = search(null, IdentifierBundle.of(SecurityUtils.bloombergTickerSecurityId("EDU20 Comdty")), CLOSE_DATA_FIELD, CMPL_DATA_PROVIDER, BBG_DATA_SOURCE, LCLOSE_OBSERVATION_TIME, true, false);
    assertNotNull(searchResult);
    assertNotNull(searchResult.getDocuments());
    assertTrue(searchResult.getDocuments().size() == 1);
    assertEquals(expectedTS.get(edu20Buid), searchResult.getDocuments().get(0).getSeries().getTimeSeries());
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void identifiersWithDatesIntersectLowerBound() throws Exception {
    
    Identifier edu0Id = SecurityUtils.bloombergTickerSecurityId("EDU0 Comdty");
    LocalDate startDate = LocalDate.of(2007, MonthOfYear.SEPTEMBER, 19);
    LocalDate endDate = LocalDate.of(2010, MonthOfYear.SEPTEMBER, 19);
    IdentifierWithDates edu0 = IdentifierWithDates.of(edu0Id, startDate, endDate);
    IdentifierBundleWithDates bundle = IdentifierBundleWithDates.of(new IdentifierWithDates[] {edu0});
    HistoricalTimeSeriesDocument tsDocument = createTimeSeries(CLOSE_DATA_FIELD, CMPL_DATA_PROVIDER, BBG_DATA_SOURCE, bundle, getEmptyTimeSeries());
    assertNotNull(tsDocument);
    assertNotNull(tsDocument.getUniqueId());
    
    //add intersecting dates
    edu0 = IdentifierWithDates.of(edu0Id, startDate.minusDays(1), endDate);
    bundle = IdentifierBundleWithDates.of(new IdentifierWithDates[] {edu0});
    createTimeSeries(CLOSE_DATA_FIELD, CMPL_DATA_PROVIDER, BBG_DATA_SOURCE, bundle, getEmptyTimeSeries());   
    
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void identifiersWithDatesIntersectUpperBound() throws Exception {

    Identifier edu0Id = SecurityUtils.bloombergTickerSecurityId("EDU0 Comdty");
    LocalDate startDate = LocalDate.of(2007, MonthOfYear.SEPTEMBER, 19);
    LocalDate endDate = LocalDate.of(2010, MonthOfYear.SEPTEMBER, 19);
    IdentifierWithDates edu0 = IdentifierWithDates.of(edu0Id, startDate, endDate);
    IdentifierBundleWithDates bundle = IdentifierBundleWithDates.of(new IdentifierWithDates[] {edu0});
    HistoricalTimeSeriesDocument tsDocument = createTimeSeries(CLOSE_DATA_FIELD, CMPL_DATA_PROVIDER, BBG_DATA_SOURCE, bundle, getEmptyTimeSeries());
    assertNotNull(tsDocument);
    assertNotNull(tsDocument.getUniqueId());
    
    //add intersecting dates
    edu0 = IdentifierWithDates.of(edu0Id, startDate, endDate.plusDays(1));
    bundle = IdentifierBundleWithDates.of(new IdentifierWithDates[] {edu0});
    createTimeSeries(CLOSE_DATA_FIELD, CMPL_DATA_PROVIDER, BBG_DATA_SOURCE, bundle, getEmptyTimeSeries());   
        
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void identifiersWithDatesIntersectContains() throws Exception {

    Identifier edu0Id = SecurityUtils.bloombergTickerSecurityId("EDU0 Comdty");
    LocalDate startDate = LocalDate.of(2007, MonthOfYear.SEPTEMBER, 19);
    LocalDate endDate = LocalDate.of(2010, MonthOfYear.SEPTEMBER, 19);
    IdentifierWithDates edu0 = IdentifierWithDates.of(edu0Id, startDate, endDate);
    IdentifierBundleWithDates bundle = IdentifierBundleWithDates.of(new IdentifierWithDates[] {edu0});
    HistoricalTimeSeriesDocument tsDocument = createTimeSeries(CLOSE_DATA_FIELD, CMPL_DATA_PROVIDER, BBG_DATA_SOURCE, bundle, getEmptyTimeSeries());
    assertNotNull(tsDocument);
    assertNotNull(tsDocument.getUniqueId());
    
    //add intersecting dates
    edu0 = IdentifierWithDates.of(edu0Id, startDate.plusDays(1), endDate.minusDays(1));
    bundle = IdentifierBundleWithDates.of(new IdentifierWithDates[] {edu0});
    createTimeSeries(CLOSE_DATA_FIELD, CMPL_DATA_PROVIDER, BBG_DATA_SOURCE, bundle, getEmptyTimeSeries());    
  }

  //-------------------------------------------------------------------------
  @Test
  public void getTimeSeriesWithDateRange() throws Exception {
    List<HistoricalTimeSeriesDocument> tsList = addAndTestTimeSeries();
    for (HistoricalTimeSeriesDocument doc : tsList) {
      ManageableHistoricalTimeSeries series = doc.getSeries();
      DoubleTimeSeries<LocalDate> timeSeries = series.getTimeSeries();
      
      HistoricalTimeSeriesDocument searchDoc = getHistoricalTimeSeries(series.getIdentifiers(),  series.getDataSource(), series.getDataProvider(), series.getDataField(), null, null);
      assertNotNull(searchDoc);
      assertEquals(series.getUniqueId(), searchDoc.getUniqueId());
      assertEquals(timeSeries, searchDoc.getSeries().getTimeSeries());
      
      // test end dates
      LocalDate earliestDate = timeSeries.getEarliestTime();
      LocalDate latestDate = timeSeries.getLatestTime();
      
      searchDoc = getHistoricalTimeSeries(series.getIdentifiers(),  series.getDataSource(), series.getDataProvider(), series.getDataField(), earliestDate, latestDate);
      assertNotNull(searchDoc);
      assertEquals(series.getUniqueId(), searchDoc.getUniqueId());
      assertEquals(timeSeries, searchDoc.getSeries().getTimeSeries());

      // test subSeries
      LocalDate start = DateUtil.nextWeekDay(earliestDate);
      LocalDate end = DateUtil.previousWeekDay(latestDate);
      if (start.isBefore(end) || start.equals(end)) {
        searchDoc = getHistoricalTimeSeries(series.getIdentifiers(),  series.getDataSource(), series.getDataProvider(), series.getDataField(), start, end);
        assertNotNull(searchDoc);
        assertEquals(series.getUniqueId(), searchDoc.getUniqueId());
        assertEquals(start, searchDoc.getSeries().getTimeSeries().getEarliestTime());
        assertEquals(end, searchDoc.getSeries().getTimeSeries().getLatestTime());
      }
    }
  }

  private HistoricalTimeSeriesDocument getHistoricalTimeSeries(IdentifierBundleWithDates identifierBundleWithDates, String dataSource, String dataProvider, String dataField, LocalDate earliestDate, LocalDate latestDate) {
    HistoricalTimeSeriesSearchRequest request = new HistoricalTimeSeriesSearchRequest();
    request.setIdentifiers(identifierBundleWithDates.asIdentifierBundle());
    request.setDataSource(dataSource);
    request.setDataProvider(dataProvider);
    request.setDataField(dataField);
    request.setStart(earliestDate);
    request.setEnd(latestDate);
    request.setLoadTimeSeries(true);
    
    HistoricalTimeSeriesSearchResult searchResult = _master.search(request);
    return searchResult.getDocuments().get(0);
  }

  //-------------------------------------------------------------------------
  protected DbHistoricalTimeSeriesMaster getTimeSeriesMaster(Map<String, String> namedSQLMap) {
    return new LocalDateDbHistoricalTimeSeriesMaster(
        getDbSource(), 
        namedSQLMap,
        false);
  }

  protected LocalDateDoubleTimeSeries getTimeSeries(MapLocalDateDoubleTimeSeries tsMap) {
    return new ArrayLocalDateDoubleTimeSeries(tsMap);
  }

  protected LocalDateDoubleTimeSeries getEmptyTimeSeries() {
    return new ArrayLocalDateDoubleTimeSeries();
  }

  protected LocalDateDoubleTimeSeries getTimeSeries(List<LocalDate> dates, List<Double> values) {
    return new ArrayLocalDateDoubleTimeSeries(dates, values);
  }

  protected String print(LocalDate date) {
    return DateUtil.printYYYYMMDD(date);
  }

}
