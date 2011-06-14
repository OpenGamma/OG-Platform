/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.timeseries;

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
import org.testng.annotations.Test;

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
import com.opengamma.master.timeseries.TimeSeriesSearchHistoricRequest;
import com.opengamma.master.timeseries.TimeSeriesSearchHistoricResult;
import com.opengamma.master.timeseries.TimeSeriesSearchRequest;
import com.opengamma.master.timeseries.TimeSeriesSearchResult;
import com.opengamma.master.timeseries.impl.RandomTimeSeriesGenerator;
import com.opengamma.masterdb.timeseries.hibernate.DataFieldBean;
import com.opengamma.masterdb.timeseries.hibernate.DataProviderBean;
import com.opengamma.masterdb.timeseries.hibernate.DataSourceBean;
import com.opengamma.masterdb.timeseries.hibernate.ObservationTimeBean;
import com.opengamma.masterdb.timeseries.hibernate.SchemeBean;
import com.opengamma.util.test.DBTest;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.MapLocalDateDoubleTimeSeries;

/**
 * Abstract test for time-series masters.
 */
public abstract class DbTimeSeriesMasterTest<T> extends DBTest {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DbTimeSeriesMasterTest.class);
  
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
  private DbTimeSeriesMaster<T> _tsMaster;
  
  public DbTimeSeriesMasterTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {} version {}", databaseType, databaseVersion);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }
  
  abstract protected DbTimeSeriesMaster<T> getTimeSeriesMaster(Map<String, String> namedSQLMap);
  abstract protected DoubleTimeSeries<T> getTimeSeries(MapLocalDateDoubleTimeSeries tsMap);
  abstract protected DoubleTimeSeries<T> getTimeSeries(List<T> dates, List<Double> values);
  abstract protected DoubleTimeSeries<T> getEmptyTimeSeries();
  abstract protected T convert(LocalDate date);
  abstract protected LocalDate convert(T date);
  abstract protected String print(T date);
  
  /**
   * Gets the tsMaster field.
   * @return the tsMaster
   */
  protected TimeSeriesMaster<T> getTsMaster() {
    return _tsMaster;
  }

  /**
   * @throws java.lang.Exception
   */
  @SuppressWarnings("unchecked")
  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();
    
    ApplicationContext context = new FileSystemXmlApplicationContext("src/com/opengamma/masterdb/timeseries/tssQueries.xml");
    Map<String, String> namedSQLMap = (Map<String, String>) context.getBean("tssNamedSQLMap");
    
    _tsMaster = getTimeSeriesMaster(namedSQLMap);
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
      setIdentifiers(identifiers, tsDocument);
      
      tsDocument.setTimeSeries(timeSeries);
      
      tsDocument = _tsMaster.add(tsDocument);
      
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
    TimeSeriesDocument<T> tsDoc = getRandonTimeSeriesDocument(expectedTS);
    
    TimeSeriesSearchResult<T> searchResult = search(null, null, tsDoc.getIdentifiers().asIdentifierBundle(), tsDoc.getDataField(), tsDoc.getDataProvider(), tsDoc.getDataSource(), tsDoc.getObservationTime(), true, false);
    assertNotNull(searchResult);
    List<TimeSeriesDocument<T>> documents = searchResult.getDocuments();
    assertNotNull(documents);
    assertTrue(documents.size() == 1);
    
    TimeSeriesDocument<T> searchedDoc = documents.get(0);
    assertNotNull(searchedDoc);
    
    assertEqualTimeSeriesDocument(tsDoc, searchedDoc);
    
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
    TimeSeriesSearchResult<T> searchResult = _tsMaster.search(request);
    return searchResult;
  }
  
  @Test
  public void searchByIdentifierValue() throws Exception {
    List<TimeSeriesDocument<T>> expectedTS = addAndTestTimeSeries();
    TimeSeriesDocument<T> tsDoc = getRandonTimeSeriesDocument(expectedTS);
    TimeSeriesSearchRequest<T> request = new TimeSeriesSearchRequest<T>();
    IdentifierWithDates identifierWithDates = tsDoc.getIdentifiers().getIdentifiers().iterator().next();
    request.setIdentifierValue(identifierWithDates.asIdentifier().getValue());
    request.setDataField(tsDoc.getDataField());
    request.setDataProvider(tsDoc.getDataProvider());
    request.setDataSource(tsDoc.getDataSource());
    request.setObservationTime(tsDoc.getObservationTime());
    request.setLoadTimeSeries(true);
    
    TimeSeriesSearchResult<T> searchResult = _tsMaster.search(request);
    assertNotNull(searchResult);
    List<TimeSeriesDocument<T>> documents = searchResult.getDocuments();
    assertNotNull(documents);
    assertTrue(documents.size() == 1);
    
    TimeSeriesDocument<T> searchedDoc = documents.get(0);
    assertNotNull(searchedDoc);
    
    assertEqualTimeSeriesDocument(tsDoc, searchedDoc);
    
  }
  
  @Test
  public void searchByFieldProviderSource() throws Exception {
    List<TimeSeriesDocument<T>> tsList = addAndTestTimeSeries();
    TimeSeriesDocument<T> tsDoc = getRandonTimeSeriesDocument(tsList);
    
    TimeSeriesSearchResult<T> searchResult = search(null, null, tsDoc.getIdentifiers().asIdentifierBundle(), tsDoc.getDataField(), tsDoc.getDataProvider(), tsDoc.getDataSource(), tsDoc.getObservationTime(), true, false);
    assertNotNull(searchResult);
    List<TimeSeriesDocument<T>> documents = searchResult.getDocuments();
    assertNotNull(documents);
    assertTrue(documents.size() == 1);
    
    assertEqualTimeSeriesDocument(tsDoc, documents.get(0));
  }
  
  @Test
  public void searchByUID() throws Exception {
    List<TimeSeriesDocument<T>> tsList = addAndTestTimeSeries();
    TimeSeriesDocument<T> tsDoc = getRandonTimeSeriesDocument(tsList);
    
    TimeSeriesSearchResult<T> searchResult = search(null, tsDoc.getUniqueId(), null, null, null, null, null, true, false);
    assertNotNull(searchResult);
    List<TimeSeriesDocument<T>> documents = searchResult.getDocuments();
    assertNotNull(documents);
    assertTrue(documents.size() == 1);
    assertEqualTimeSeriesDocument(tsDoc, documents.get(0));
    
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
            DoubleTimeSeries<T> timeSeries = makeRandomTimeSeries(start, 7);
            assertTrue(timeSeries.size() == 7);
            assertEquals(convert(start), timeSeries.getEarliestTime());
            
            TimeSeriesDocument<T> tsDocument = createTimeSeries(datafield, dataProvider, dataSource, IdentifierBundleWithDates.of(identifiers), timeSeries);
            assertNotNull(tsDocument);
            assertNotNull(tsDocument.getUniqueId());
            
            TimeSeriesDocument<T> actualDoc = _tsMaster.get(tsDocument.getUniqueId());
            assertNotNull(actualDoc);
            assertEquals(timeSeries, actualDoc.getTimeSeries());
            result.add(tsDocument);
          }
        }
      }
    }
    return result;
  }
  
  protected TimeSeriesDocument<T> createTimeSeries(final String datafield, final String dataProvider, final String dataSource, final IdentifierBundleWithDates identifiers, final DoubleTimeSeries<T> timeSeries) {
    TimeSeriesDocument<T> tsDocument = new TimeSeriesDocument<T>();
    tsDocument.setDataField(datafield);
    tsDocument.setDataProvider(dataProvider);
    tsDocument.setDataSource(dataSource);
    tsDocument.setObservationTime(LCLOSE_OBSERVATION_TIME);
    tsDocument.setIdentifiers(identifiers);
    tsDocument.setTimeSeries(timeSeries);
    return _tsMaster.add(tsDocument);    
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
    setIdentifiers(identifiers, tsDocument);
    tsDocument.setTimeSeries(timeSeries);
    
    tsDocument = _tsMaster.add(tsDocument);
    
    assertNotNull(tsDocument);
    assertNotNull(tsDocument.getUniqueId());
    
    TimeSeriesDocument<T> actualDoc = _tsMaster.get(tsDocument.getUniqueId());
    assertNotNull(actualDoc);
    
    assertEqualTimeSeriesDocument(tsDocument, actualDoc);
    
    //try and add another using the same meta data and identifiers
    TimeSeriesDocument<T> otherDoc = new TimeSeriesDocument<T>();
    otherDoc.setDataField(CLOSE_DATA_FIELD);
    otherDoc.setDataProvider(CMPL_DATA_PROVIDER);
    otherDoc.setDataSource(BBG_DATA_SOURCE);
    otherDoc.setObservationTime(LCLOSE_OBSERVATION_TIME);
    setIdentifiers(identifiers, otherDoc);
    otherDoc.setTimeSeries(makeRandomTimeSeries(DEFAULT_START, 7));
    try {
      _tsMaster.add(otherDoc);
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
    
    DoubleTimeSeries<T> ts = makeRandomTimeSeries(7);
    TimeSeriesDocument<T> tsDoc = createTimeSeries("PX_LAST", "CMPL", "BBG", bundle, ts);
    assertNotNull(tsDoc);
    TimeSeriesDocument<T> loadedTs = _tsMaster.get(tsDoc.getUniqueId());
    assertEquals(loadedTs.getTimeSeries(), ts);
    
    ts = makeRandomTimeSeries(7);
    tsDoc = createTimeSeries("VOLUME", "CMPL", "BBG", bundle, ts);
    assertNotNull(tsDoc);
    loadedTs = _tsMaster.get(tsDoc.getUniqueId());
    assertEquals(loadedTs.getTimeSeries(), ts); 
  }
  
  @Test
  public void addMultipleWithNullDates() throws Exception {
    
    IdentifierWithDates edu0 = IdentifierWithDates.of(Identifier.of("BBG", "EDU0"), null, LocalDate.of(2020, 9, 14));
    IdentifierWithDates edu20 = IdentifierWithDates.of(Identifier.of("BBG", "EDU20"), LocalDate.of(2010, 9, 15), null);
    IdentifierWithDates cusip = IdentifierWithDates.of(Identifier.of("CUSIP", "EDU0"), LocalDate.of(2010, 9, 14), LocalDate.of(2020, 9, 14));
    IdentifierWithDates buid = IdentifierWithDates.of(Identifier.of("BUID", "IX-1234"), null, null);
    
    IdentifierBundleWithDates bundle = IdentifierBundleWithDates.of(edu0, edu20, cusip, buid);
    
    DoubleTimeSeries<T> ts = makeRandomTimeSeries(7);
    TimeSeriesDocument<T> tsDoc = createTimeSeries("PX_LAST", "CMPL", "BBG", bundle, ts);
    assertNotNull(tsDoc);
    TimeSeriesDocument<T> loadedTs = _tsMaster.get(tsDoc.getUniqueId());
    assertEquals(loadedTs.getTimeSeries(), ts);
    
    ts = makeRandomTimeSeries(7);
    tsDoc = createTimeSeries("VOLUME", "CMPL", "BBG", bundle, ts);
    assertNotNull(tsDoc);
    loadedTs = _tsMaster.get(tsDoc.getUniqueId());
    assertEquals(loadedTs.getTimeSeries(), ts); 
  }

  /**
   * @param identifiers
   * @param tsDocument
   */
  private void setIdentifiers(IdentifierBundle identifiers, TimeSeriesDocument<T> tsDocument) {
    tsDocument.setIdentifiers(IdentifierBundleWithDates.of(identifiers));
  }
  
  @Test
  public void updateTimeSeries() throws Exception {
    List<TimeSeriesDocument<T>> tsList = addAndTestTimeSeries();
    assertFalse(tsList.isEmpty());
    TimeSeriesDocument<T> tsDoc = getRandonTimeSeriesDocument(tsList);
    
    tsDoc.setTimeSeries(makeRandomTimeSeries(DEFAULT_START, 7));
    TimeSeriesDocument<T> updatedDoc = _tsMaster.update(tsDoc);
    assertNotNull(updatedDoc);
    assertNotNull(updatedDoc.getUniqueId());
    assertEquals(tsDoc.getUniqueId(), updatedDoc.getUniqueId());
    
    assertEqualTimeSeriesDocument(updatedDoc, _tsMaster.get(updatedDoc.getUniqueId()));
    
    //delete dataPoints, set with empty timeseries
    tsDoc.setTimeSeries(getEmptyTimeSeries()); 
    updatedDoc = _tsMaster.update(tsDoc);
    assertNotNull(updatedDoc);
    assertNotNull(updatedDoc.getUniqueId());
    assertEquals(tsDoc.getUniqueId(), updatedDoc.getUniqueId());
    
    assertEqualTimeSeriesDocument(updatedDoc, _tsMaster.get(updatedDoc.getUniqueId()));
    
  }
  
  private TimeSeriesDocument<T> getRandonTimeSeriesDocument(List<TimeSeriesDocument<T>> tsList) {
    int randIndex = _random.nextInt(tsList.size());
    return tsList.get(randIndex);
  }

  @Test
  public void removeTimeSeries() throws Exception {
    List<TimeSeriesDocument<T>> tsList = addAndTestTimeSeries();
    TimeSeriesDocument<T> tsDoc = getRandonTimeSeriesDocument(tsList);
    
    _tsMaster.remove(tsDoc.getUniqueId());
    try {
      _tsMaster.get(tsDoc.getUniqueId());
      fail();
    } catch(DataNotFoundException ex) {
      //do nothing
    }
  }
  
  @Test
  public void removeThenAddTimeseries() throws Exception {
    List<TimeSeriesDocument<T>> tsList = addAndTestTimeSeries();
    TimeSeriesDocument<T> tsDoc = getRandonTimeSeriesDocument(tsList);
    _tsMaster.remove(tsDoc.getUniqueId());
    try {
      _tsMaster.get(tsDoc.getUniqueId());
      fail();
    } catch(DataNotFoundException ex) {
      //do nothing
    }
    DoubleTimeSeries<T> currentTS = makeRandomTimeSeries(7);
    TimeSeriesDocument<T> currentTSDoc = createTimeSeries(tsDoc.getDataField(), tsDoc.getDataProvider(), tsDoc.getDataSource(), tsDoc.getIdentifiers(), currentTS);
    assertNotNull(currentTSDoc);
    assertNotNull(currentTSDoc.getUniqueId());
    
    TimeSeriesDocument<T> actualDoc = _tsMaster.get(currentTSDoc.getUniqueId());
    assertNotNull(actualDoc);
    assertEquals(currentTS, actualDoc.getTimeSeries());
    
    _tsMaster.remove(currentTSDoc.getUniqueId());
    try {
      _tsMaster.get(currentTSDoc.getUniqueId());
      fail();
    } catch(DataNotFoundException ex) {
      //do nothing
    }
    currentTS = makeRandomTimeSeries(7);
    currentTSDoc = createTimeSeries(tsDoc.getDataField(), tsDoc.getDataProvider(), tsDoc.getDataSource(), tsDoc.getIdentifiers(), currentTS);
    assertNotNull(currentTSDoc);
    assertNotNull(currentTSDoc.getUniqueId());
    
    actualDoc = _tsMaster.get(currentTSDoc.getUniqueId());
    assertNotNull(actualDoc);
    assertEquals(currentTS, actualDoc.getTimeSeries());
    
  }
  
  @Test
  public void getUnknownUID() throws Exception {
    addAndTestTimeSeries();
    try {
      _tsMaster.get(UniqueIdentifier.of(DbTimeSeriesMaster.IDENTIFIER_SCHEME_DEFAULT, String.valueOf(Long.MIN_VALUE)));
      fail();
    } catch(DataNotFoundException ex) {
      //do nothing
    }
  }
  
  @Test 
  public void getInvalidUID() throws Exception {
    addAndTestTimeSeries();
    try {
      _tsMaster.get(UniqueIdentifier.of("INVALID", "unknown"));
      fail();
    } catch(IllegalArgumentException ex) {
      //do nothing
    }
  }
  
  @Test
  public void resolveIdentifier() throws Exception {
    List<TimeSeriesDocument<T>> tsList = addAndTestTimeSeries();
    TimeSeriesDocument<T> tsDoc = getRandonTimeSeriesDocument(tsList);
    
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
        
    resolveIdentifier = _tsMaster.resolveIdentifier(IdentifierBundle.of(Identifier.of("Invalid", "Invalid")), BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
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
      setIdentifiers(bundle, tsDocument);
      tsDocument.setTimeSeries(timeSeries);
      
      tsDocument = _tsMaster.add(tsDocument);
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
    TimeSeriesDocument<T> tsDoc = getRandonTimeSeriesDocument(tsList);
    
    DoubleTimeSeries<T> timeSeries = tsDoc.getTimeSeries();
    LocalDate start = convert(timeSeries.getLatestTime()).plusDays(1);
    DoubleTimeSeries<T> appendedTS = makeRandomTimeSeries(start, 7);
    DoubleTimeSeries<T> mergedTS = timeSeries.noIntersectionOperation(appendedTS);
    // append timeseries to existing identifiers in the datastore
    tsDoc.setTimeSeries(appendedTS);
    _tsMaster.appendTimeSeries(tsDoc);
    
    TimeSeriesDocument<T> latestDoc = _tsMaster.get(tsDoc.getUniqueId());
    assertNotNull(latestDoc);
    tsDoc.setTimeSeries(mergedTS);
    assertEqualTimeSeriesDocument(tsDoc, latestDoc);
    
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
    for (TimeSeriesDocument<T> testDoc : documents) {
      assertTrue(closeDataFields.contains(testDoc));
    }

    searchResult = search(null, null, null, null, null, BBG_DATA_SOURCE, null, false, false);
    documents = searchResult.getDocuments();
    assertTrue(bbgDataSources.size() == documents.size());
    for (TimeSeriesDocument<T> testDoc : documents) {
      assertTrue(bbgDataSources.contains(testDoc));
    }
    
    searchResult = search(null, null, null, null, null, null, LCLOSE_OBSERVATION_TIME, false, false);
    documents = searchResult.getDocuments();
    assertTrue(lcloseObservations.size() == documents.size());
    for (TimeSeriesDocument<T> testDoc : documents) {
      assertTrue(lcloseObservations.contains(testDoc));
    }
    
    searchResult = search(null, null, null, null, CMPL_DATA_PROVIDER, null, null, false, false);
    documents = searchResult.getDocuments();
    assertTrue(cmplDataProviders.size() == documents.size());
    for (TimeSeriesDocument<T> testDoc : documents) {
      assertTrue(cmplDataProviders.contains(testDoc));
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
    
    TimeSeriesDocument<T> tsDoc = getRandonTimeSeriesDocument(tsList);
    //set timeseries to null for metadata test and set dates
    tsDoc.setEarliest(tsDoc.getTimeSeries().getEarliestTime());
    tsDoc.setLatest(tsDoc.getTimeSeries().getLatestTime());
    tsDoc.setTimeSeries(null);
    assertTrue(documents.contains(tsDoc));
    
  }
  
  @Test
  public void addDataPoint() throws Exception {
    List<TimeSeriesDocument<T>> tsList = addAndTestTimeSeries();
    TimeSeriesDocument<T> tsDoc = getRandonTimeSeriesDocument(tsList);
    
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
    TimeSeriesDocument<T> updatedDoc = _tsMaster.get(tsDoc.getUniqueId());
    assertNotNull(updatedDoc);
    assertNotNull(updatedDoc.getUniqueId());
    assertEquals(updatedTS, updatedDoc.getTimeSeries());
    
    DataPointDocument<T> actualDDoc = _tsMaster.getDataPoint(dataPointDocument.getDataPointId());
    assertEquals(tsDoc.getUniqueId(), actualDDoc.getTimeSeriesId());
    assertEquals(dataPointDocument.getDataPointId(), actualDDoc.getDataPointId());
    assertEquals(dataPointDocument.getDate(), actualDDoc.getDate());
    assertEquals(dataPointDocument.getValue(), actualDDoc.getValue());
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void addDataPointWithoutTSID() throws Exception {
    DataPointDocument<T> dataPointDocument = new DataPointDocument<T>();
    dataPointDocument.setDate(convert(LocalDate.of(2000, 1, 2)));
    dataPointDocument.setValue(Math.random());
    dataPointDocument = _tsMaster.addDataPoint(dataPointDocument);
  }
  
  
  @Test
  public void updateDataPoint() throws Exception {
    List<TimeSeriesDocument<T>> tsList = addAndTestTimeSeries();
    TimeSeriesDocument<T> tsDoc = getRandonTimeSeriesDocument(tsList);
    
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
    
    TimeSeriesDocument<T> updatedDoc = _tsMaster.get(tsDoc.getUniqueId());
    assertNotNull(updatedDoc);
    assertNotNull(updatedDoc.getUniqueId());
    assertEquals(updatedTS, updatedDoc.getTimeSeries());
    
  }
  
  @Test
  public void removeDataPoint() throws Exception {
    List<TimeSeriesDocument<T>> tsList = addAndTestTimeSeries();
    
    TimeSeriesDocument<T> tsDoc = getRandonTimeSeriesDocument(tsList);
    DoubleTimeSeries<T> timeSeries = tsDoc.getTimeSeries();
    //delete random datapoints
    List<T> dates = timeSeries.times();
    List<Double> values = timeSeries.values();
    int deleteIdx = _random.nextInt(timeSeries.size());
    T deletedDate = dates.remove(deleteIdx);
    values.remove(deleteIdx);
    
    DoubleTimeSeries<T> deletedTS = getTimeSeries(dates, values);
    String scheme = tsDoc.getUniqueId().getScheme();
    String tsId = tsDoc.getUniqueId().getValue();
    _tsMaster.removeDataPoint(UniqueIdentifier.of(scheme, tsId + "/" + print(deletedDate)));
    
    TimeSeriesDocument<T> updatedDoc = _tsMaster.get(tsDoc.getUniqueId());
    assertNotNull(updatedDoc);
    assertNotNull(updatedDoc.getUniqueId());
    assertEquals(deletedTS, updatedDoc.getTimeSeries());
    
  }
  
  @Test
  public void removeDataPoints() throws Exception {
    List<TimeSeriesDocument<T>> tsList = addAndTestTimeSeries();
    TimeSeriesDocument<T> tsDoc = getRandonTimeSeriesDocument(tsList);
    
    DoubleTimeSeries<T> timeSeries = tsDoc.getTimeSeries();
    int originalSize = timeSeries.size();
    int desiredSize = originalSize / 2;
    T firstDateToRetain = timeSeries.getTime(timeSeries.size() - desiredSize);
    _tsMaster.removeDataPoints(tsDoc.getUniqueId(), firstDateToRetain);
    
    TimeSeriesDocument<T> updatedDoc = _tsMaster.get(tsDoc.getUniqueId());

    assertNotNull(updatedDoc);
    assertNotNull(updatedDoc.getUniqueId());
    
    assertEquals(desiredSize, updatedDoc.getTimeSeries().size());
    assertEquals(firstDateToRetain, updatedDoc.getTimeSeries().getEarliestTime());
    assertEquals(timeSeries.getLatestTime(), updatedDoc.getTimeSeries().getLatestTime());
    
  }
  
  @Test
  public void getTimeSeriesSnapShot() throws Exception {
    IdentifierBundle identifiers = IdentifierBundle.of(Identifier.of("sa", "ida"), Identifier.of("sb", "idb"));
    
    SortedMap<ZonedDateTime, DoubleTimeSeries<T>> timeStampTSMap = new TreeMap<ZonedDateTime, DoubleTimeSeries<T>>();
    DoubleTimeSeries<T> timeSeries = makeRandomTimeSeries(DEFAULT_START, 7);
    
    SortedMap<T, Double> currentTimeSeriesMap = new TreeMap<T, Double>();
    for (int i = 0; i < timeSeries.size(); i++) {
      currentTimeSeriesMap.put(timeSeries.getTime(i), timeSeries.getValueAt(i));
    }
    
    TimeSeriesDocument<T> tsDocument = new TimeSeriesDocument<T>();
    tsDocument.setDataField(CLOSE_DATA_FIELD);
    tsDocument.setDataProvider(CMPL_DATA_PROVIDER);
    tsDocument.setDataSource(BBG_DATA_SOURCE);
    tsDocument.setObservationTime(LCLOSE_OBSERVATION_TIME);
    setIdentifiers(identifiers, tsDocument);
    tsDocument.setTimeSeries(timeSeries);
    
    tsDocument = _tsMaster.add(tsDocument);
    
    assertNotNull(tsDocument);
    assertNotNull(tsDocument.getUniqueId());
    
    TimeSeriesDocument<T> actualDoc = _tsMaster.get(tsDocument.getUniqueId());
    assertNotNull(actualDoc);
    assertEqualTimeSeriesDocument(tsDocument, actualDoc);
    
    Thread.sleep(50); // assume system clock resolution < 50ms
    timeStampTSMap.put(Clock.system(javax.time.calendar.TimeZone.UTC).zonedDateTime(), timeSeries);
    
    //update a random datapoint 3 times
    for (int i = 0; i < 3; i++) {
      List<T> times = timeSeries.times();
      int ranIndx = _random.nextInt(times.size());
      T updateDate = times.get(ranIndx);
      Double newValue = _random.nextDouble();
      currentTimeSeriesMap.put(updateDate, newValue);
      //_tsMaster.updateDataPoint(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME, updateDate, newValue);
      DataPointDocument<T> dataPointDocument = new DataPointDocument<T>();
      dataPointDocument.setTimeSeriesId(tsDocument.getUniqueId());
      dataPointDocument.setDate(updateDate);
      dataPointDocument.setValue(newValue);
      _tsMaster.updateDataPoint(dataPointDocument);
      tsDocument = _tsMaster.get(tsDocument.getUniqueId());
      assertNotNull(tsDocument);
      timeSeries = getTimeSeries(new ArrayList<T>(currentTimeSeriesMap.keySet()), new ArrayList<Double>(currentTimeSeriesMap.values()));
      assertEquals(timeSeries, tsDocument.getTimeSeries()); 
      
      Thread.sleep(50); // assume system clock resolution < 50ms
      timeStampTSMap.put(Clock.system(javax.time.calendar.TimeZone.UTC).zonedDateTime(), timeSeries);
    }
    
    //delete a datapoint
    List<T> times = timeSeries.times();
    int ranIndx = _random.nextInt(times.size());
    T deleteDate = times.get(ranIndx);
    currentTimeSeriesMap.remove(deleteDate);
    UniqueIdentifier dataPointId = UniqueIdentifier.of(tsDocument.getUniqueId().getScheme(), tsDocument.getUniqueId().getValue() + "/" + print(deleteDate));
    _tsMaster.removeDataPoint(dataPointId);
    tsDocument = _tsMaster.get(tsDocument.getUniqueId());
    assertNotNull(tsDocument);
    timeSeries = getTimeSeries(new ArrayList<T>(currentTimeSeriesMap.keySet()), new ArrayList<Double>(currentTimeSeriesMap.values()));
    assertEquals(timeSeries, tsDocument.getTimeSeries()); 
    Thread.sleep(50); // assume system clock resolution < 50ms
    timeStampTSMap.put(Clock.system(javax.time.calendar.TimeZone.UTC).zonedDateTime(), timeSeries);
    
    //delete all datapoints
    tsDocument.setTimeSeries(getEmptyTimeSeries());
    _tsMaster.update(tsDocument);
    tsDocument = _tsMaster.get(tsDocument.getUniqueId());
    assertNotNull(tsDocument);
    timeSeries = getEmptyTimeSeries();
    assertEquals(timeSeries, tsDocument.getTimeSeries()); 
    Thread.sleep(50); // assume system clock resolution < 50ms
    timeStampTSMap.put(Clock.system(javax.time.calendar.TimeZone.UTC).zonedDateTime(), timeSeries);
    
    //add new datapoints
    timeSeries = makeRandomTimeSeries(DEFAULT_START, 7);
    tsDocument.setTimeSeries(timeSeries);
    _tsMaster.update(tsDocument);
    tsDocument = _tsMaster.get(tsDocument.getUniqueId());
    assertNotNull(tsDocument);
    assertEquals(timeSeries, tsDocument.getTimeSeries());
    Thread.sleep(50); // assume system clock resolution < 50ms
    timeStampTSMap.put(Clock.system(javax.time.calendar.TimeZone.UTC).zonedDateTime(), timeSeries);
    
    //assert datasnapshots
    for (Entry<ZonedDateTime, DoubleTimeSeries<T>> entry : timeStampTSMap.entrySet()) {
      ZonedDateTime timeStamp = entry.getKey();
      DoubleTimeSeries<T> expectedTS = entry.getValue();
      TimeSeriesDocument<T> snapshotDoc = getTimeSeriesSnapShot(identifiers, timeStamp);
      assertNotNull(snapshotDoc);
      assertEquals(expectedTS.times(), snapshotDoc.getTimeSeries().times());
      assertEquals(expectedTS.values(), snapshotDoc.getTimeSeries().values());
    }
    
    //assert before and after last deltas
    //before 1st delta should return empty timeseries
    ZonedDateTime beforeDelta = timeStampTSMap.firstKey().minusMinutes(1);
    TimeSeriesDocument<T> snapshotDoc = getTimeSeriesSnapShot(identifiers, beforeDelta);
    assertEquals(new ArrayLocalDateDoubleTimeSeries(), snapshotDoc.getTimeSeries());
    //after last delta should return latest timeseries
    ZonedDateTime afterDelta = timeStampTSMap.lastKey().plusMinutes(1);
    tsDocument = _tsMaster.get(tsDocument.getUniqueId());
    assertNotNull(tsDocument);
    assertEquals(timeStampTSMap.get(timeStampTSMap.lastKey()), tsDocument.getTimeSeries());
    snapshotDoc = getTimeSeriesSnapShot(identifiers, afterDelta);
    assertEquals(tsDocument.getTimeSeries(), snapshotDoc.getTimeSeries());
    
  }

  private TimeSeriesDocument<T> getTimeSeriesSnapShot(IdentifierBundle identifiers, ZonedDateTime timeStamp) {
    TimeSeriesSearchHistoricRequest searchHistoricRequest = new TimeSeriesSearchHistoricRequest();
    searchHistoricRequest.setDataProvider(CMPL_DATA_PROVIDER);
    searchHistoricRequest.setDataSource(BBG_DATA_SOURCE);
    searchHistoricRequest.setDataField(CLOSE_DATA_FIELD);
    searchHistoricRequest.setIdentifiers(identifiers);
    searchHistoricRequest.setObservationTime(LCLOSE_OBSERVATION_TIME);
    searchHistoricRequest.setTimestamp(timeStamp.toInstant());
    TimeSeriesSearchHistoricResult<T> searchHistoric = _tsMaster.searchHistoric(searchHistoricRequest);
    assertNotNull(searchHistoric);
    List<TimeSeriesDocument<T>> documents = searchHistoric.getDocuments();
    //should expect one single document back
    assertTrue(documents.size() == 1);
    return documents.get(0);
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
    LocalDate start = DateUtil.previousWeekDay().minusDays(7);
    DoubleTimeSeries<T> timeSeries = makeRandomTimeSeries(start, 7);
    Identifier edu0Id = SecurityUtils.bloombergTickerSecurityId("EDU0 Comdty");
    IdentifierWithDates edu0 = IdentifierWithDates.of(edu0Id, LocalDate.of(2000, MonthOfYear.SEPTEMBER, 19), LocalDate.of(2010, MonthOfYear.SEPTEMBER, 13));
    IdentifierWithDates edu10 = IdentifierWithDates.of(SecurityUtils.bloombergTickerSecurityId("EDU10 Comdty"), LocalDate.of(2010, MonthOfYear.SEPTEMBER, 14), null);
    Identifier edu10Buid = SecurityUtils.bloombergBuidSecurityId("IX613196-0");
    IdentifierWithDates eduBuid = IdentifierWithDates.of(edu10Buid, null, null);
    TimeSeriesDocument<T> tsDocument = createTimeSeries(CLOSE_DATA_FIELD, CMPL_DATA_PROVIDER, BBG_DATA_SOURCE, IdentifierBundleWithDates.of(new IdentifierWithDates[]{edu0, edu10, eduBuid}), timeSeries);
    assertNotNull(tsDocument);
    assertNotNull(tsDocument.getUniqueId());
    
    TimeSeriesDocument<T> actualDoc = _tsMaster.get(tsDocument.getUniqueId());
    assertNotNull(actualDoc);
    assertEquals(timeSeries, actualDoc.getTimeSeries());
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
    
    actualDoc = _tsMaster.get(tsDocument.getUniqueId());
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
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void identifiersWithDatesIntersectLowerBound() throws Exception {
    
    Identifier edu0Id = SecurityUtils.bloombergTickerSecurityId("EDU0 Comdty");
    LocalDate startDate = LocalDate.of(2007, MonthOfYear.SEPTEMBER, 19);
    LocalDate endDate = LocalDate.of(2010, MonthOfYear.SEPTEMBER, 19);
    IdentifierWithDates edu0 = IdentifierWithDates.of(edu0Id, startDate, endDate);
    IdentifierBundleWithDates bundle = IdentifierBundleWithDates.of(new IdentifierWithDates[] {edu0});
    TimeSeriesDocument<T> tsDocument = createTimeSeries(CLOSE_DATA_FIELD, CMPL_DATA_PROVIDER, BBG_DATA_SOURCE, bundle, getEmptyTimeSeries());
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
    TimeSeriesDocument<T> tsDocument = createTimeSeries(CLOSE_DATA_FIELD, CMPL_DATA_PROVIDER, BBG_DATA_SOURCE, bundle, getEmptyTimeSeries());
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
    TimeSeriesDocument<T> tsDocument = createTimeSeries(CLOSE_DATA_FIELD, CMPL_DATA_PROVIDER, BBG_DATA_SOURCE, bundle, getEmptyTimeSeries());
    assertNotNull(tsDocument);
    assertNotNull(tsDocument.getUniqueId());
    
    //add intersecting dates
    edu0 = IdentifierWithDates.of(edu0Id, startDate.plusDays(1), endDate.minusDays(1));
    bundle = IdentifierBundleWithDates.of(new IdentifierWithDates[] {edu0});
    createTimeSeries(CLOSE_DATA_FIELD, CMPL_DATA_PROVIDER, BBG_DATA_SOURCE, bundle, getEmptyTimeSeries());    
  }

}
