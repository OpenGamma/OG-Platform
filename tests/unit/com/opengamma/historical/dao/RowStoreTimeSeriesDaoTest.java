/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.historical.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static com.opengamma.historical.dao.RowStoreJdbcDao.INVALID_KEY;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.time.calendar.Clock;
import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.dao.DataIntegrityViolationException;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.IdentificationScheme;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.util.test.DBTest;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.MapLocalDateDoubleTimeSeries;

/**
 * 
 * 
 * @author yomi
 */
public class RowStoreTimeSeriesDaoTest extends DBTest {
  private static final Logger s_logger = LoggerFactory.getLogger(RowStoreTimeSeriesDaoTest.class);
  
  private static final int TS_DATASET_SIZE = 5;
  private static final int TS_MAX_SIZE = 5;

  private static final String LCLOSE_OBSERVATION_TIME = "LCLOSE";
  private static final String CLOSE_DATA_FIELD = "CLOSE";
  private static final String CMPL_DATA_PROVIDER = "CMPL";
  private static final String BBG_DATA_SOURCE = "BLOOMBERG";

  private Random _random = new Random();
  private TimeSeriesDao _timeseriesDao;
  
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

    TimeSeriesDao ts = (TimeSeriesDao) context.getBean(getDatabaseType()+"Dao");
    _timeseriesDao = ts;
    
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
    _timeseriesDao = null;
  }

  @Test
  public void createDataProvider() throws Exception {
    int id1 = _timeseriesDao.createDataProvider("DP1", "DP1");

    Set<String> allDataProviders = _timeseriesDao.getAllDataProviders();
    assertNotNull(allDataProviders);
    assertEquals(1, allDataProviders.size());
    assertTrue(allDataProviders.contains("DP1"));

    String actualName = _timeseriesDao.findDataProviderByID(id1);
    assertEquals("DP1", actualName);

    int id2 = _timeseriesDao.createDataProvider("DP2", "DP2");
    allDataProviders = _timeseriesDao.getAllDataProviders();
    assertNotNull(allDataProviders);
    assertEquals(2, allDataProviders.size());
    assertTrue(allDataProviders.contains("DP1"));
    assertTrue(allDataProviders.contains("DP2"));

    assertEquals(id1, _timeseriesDao.getDataProviderID("DP1"));
    assertEquals(id2, _timeseriesDao.getDataProviderID("DP2"));

    actualName = _timeseriesDao.findDataProviderByID(id2);
    assertEquals("DP2", actualName);

    String invalid = _timeseriesDao.findDataProviderByID(-1);
    assertNull(invalid);
    
    int id = _timeseriesDao.getDataProviderID("Invalid");
    assertEquals(INVALID_KEY, id);
    
  }
  
  @Test(expected = DataIntegrityViolationException.class)
  public void uniqueDataProvider() throws Exception {
    _timeseriesDao.createDataProvider("DP1", "DP1");
    // should throw DataIntegrityViolationException
    _timeseriesDao.createDataProvider("DP1", "DP1");
  }

  @Test
  public void createDataSource() throws Exception {
    int id1 = _timeseriesDao.createDataSource("DS1", "DS1");
    Set<String> all = _timeseriesDao.getAllDataSources();
    assertNotNull(all);
    assertEquals(1, all.size());
    assertTrue(all.contains("DS1"));

    String actualName = _timeseriesDao.findDataSourceByID(id1);
    assertEquals("DS1", actualName);

    int id2 = _timeseriesDao.createDataSource("DS2", "DS2");
    all = _timeseriesDao.getAllDataSources();
    assertNotNull(all);
    assertEquals(2, all.size());
    assertTrue(all.contains("DS1"));
    assertTrue(all.contains("DS2"));

    assertEquals(id1, _timeseriesDao.getDataSourceID("DS1"));
    assertEquals(id2, _timeseriesDao.getDataSourceID("DS2"));

    actualName = _timeseriesDao.findDataSourceByID(id2);
    assertEquals("DS2", actualName);
    
    String invalid = _timeseriesDao.findDataSourceByID(-1);
    assertNull(invalid);
    
    int id = _timeseriesDao.getDataSourceID("Invalid");
    assertEquals(-1, id);

  }
  
  @Test(expected = DataIntegrityViolationException.class)
  public void uniqueDataSource() throws Exception {
    _timeseriesDao.createDataSource("DS1", "DS1");
    // should throw DataIntegrityViolationException
    _timeseriesDao.createDataSource("DS1", "DS1");
  }

  @Test
  public void createField() throws Exception {
    int id1 = _timeseriesDao.createDataField("TSF1", "TSF1");
    Set<String> all = _timeseriesDao.getAllTimeSeriesFields();
    assertNotNull(all);
    assertEquals(1, all.size());
    assertTrue(all.contains("TSF1"));

    String actualName = _timeseriesDao.findDataFieldByID(id1);
    assertEquals("TSF1", actualName);

    int id2 = _timeseriesDao.createDataField("TSF2", "TSF2");
    all = _timeseriesDao.getAllTimeSeriesFields();
    assertNotNull(all);
    assertEquals(2, all.size());
    assertTrue(all.contains("TSF1"));
    assertTrue(all.contains("TSF2"));

    assertEquals(id1, _timeseriesDao.getDataFieldID("TSF1"));
    assertEquals(id2, _timeseriesDao.getDataFieldID("TSF2"));

    actualName = _timeseriesDao.findDataFieldByID(id2);
    assertEquals("TSF2", actualName);
    
    String invalid = _timeseriesDao.findDataFieldByID(-1);
    assertNull(invalid);
    
    int id = _timeseriesDao.getDataFieldID("Invalid");
    assertEquals(-1, id);

  }
  
  @Test(expected = DataIntegrityViolationException.class)
  public void uniqueField() throws Exception {
    _timeseriesDao.createDataField("TSF1", "TSF1");
    // should throw DataIntegrityViolationException
    _timeseriesDao.createDataField("TSF1", "TSF1");
  }

  @Test
  public void createObservationTime() throws Exception {
    int id1 = _timeseriesDao.createObservationTime("OBT1", "OBT1");
    Set<String> all = _timeseriesDao.getAllObservationTimes();
    assertNotNull(all);
    assertEquals(1, all.size());
    assertTrue(all.contains("OBT1"));
    
    String actualName = _timeseriesDao.findObservationTimeByID(id1);
    assertEquals("OBT1", actualName);

    int id2 = _timeseriesDao.createObservationTime("OBT2", "OBT2");
    all = _timeseriesDao.getAllObservationTimes();
    assertNotNull(all);
    assertEquals(2, all.size());
    assertTrue(all.contains("OBT1"));
    assertTrue(all.contains("OBT2"));
    
    assertEquals(id1, _timeseriesDao.getObservationTimeID("OBT1"));
    assertEquals(id2, _timeseriesDao.getObservationTimeID("OBT2"));
    
    actualName = _timeseriesDao.findObservationTimeByID(id2);
    assertEquals("OBT2", actualName);
    
    String invalid = _timeseriesDao.findObservationTimeByID(-1);
    assertNull(invalid);
    
    int id = _timeseriesDao.getObservationTimeID("Invalid");
    assertEquals(-1, id);
    
  }
  
  @Test(expected = DataIntegrityViolationException.class)
  public void uniqueObservationTime() throws Exception {
    _timeseriesDao.createObservationTime("OBT1", "OBT1");
    // should throw DataIntegrityViolationException
    _timeseriesDao.createObservationTime("OBT1", "OBT1");
  }

  @Test
  public void createQuotedObject() throws Exception {
    int id1 = _timeseriesDao.createQuotedObject("QO1", "QO1");
    Set<String> all = _timeseriesDao.getAllQuotedObjects();
    assertNotNull(all);
    assertEquals(1, all.size());
    assertTrue(all.contains("QO1"));
    
    String actualName = _timeseriesDao.findQuotedObjectByID(id1);
    assertEquals("QO1", actualName);

    int id2 = _timeseriesDao.createQuotedObject("QO2", "QO2");
    all = _timeseriesDao.getAllQuotedObjects();
    assertNotNull(all);
    assertEquals(2, all.size());
    assertTrue(all.contains("QO1"));
    assertTrue(all.contains("QO2"));

    assertEquals(id1, _timeseriesDao.getQuotedObjectID("QO1"));
    assertEquals(id2, _timeseriesDao.getQuotedObjectID("QO2"));
    
    actualName = _timeseriesDao.findQuotedObjectByID(id2);
    assertEquals("QO2", actualName);
    
    String invalid = _timeseriesDao.findQuotedObjectByID(-1);
    assertNull(invalid);
    
    int id = _timeseriesDao.getQuotedObjectID("Invalid");
    assertEquals(-1, id);
    
  }
  
  @Test(expected = DataIntegrityViolationException.class)
  public void uniqueQuotedObject() throws Exception {
    _timeseriesDao.createQuotedObject("QO1", "QO1");
    // should throw DataIntegrityViolationException
    _timeseriesDao.createQuotedObject("QO1", "QO1");
  }
  
  @Test
  public void createDomain() throws Exception {
    int id1 = _timeseriesDao.createDomain("D1", "D1");
    Set<String> all = _timeseriesDao.getAllDomains();
    assertNotNull(all);
    assertEquals(1, all.size());
    assertTrue(all.contains("D1"));
    
    String actualName = _timeseriesDao.findDomainByID(id1);
    assertEquals("D1", actualName);

    int id2 = _timeseriesDao.createDomain("D2", "D2");
    all = _timeseriesDao.getAllDomains();
    assertNotNull(all);
    assertEquals(2, all.size());
    assertTrue(all.contains("D1"));
    assertTrue(all.contains("D2"));

    assertEquals(id1, _timeseriesDao.getDomainID("D1"));
    assertEquals(id2, _timeseriesDao.getDomainID("D2"));
    
    actualName = _timeseriesDao.findDomainByID(id2);
    assertEquals("D2", actualName);
    
    String invalid = _timeseriesDao.findDomainByID(-1);
    assertNull(invalid);
    
    int id = _timeseriesDao.getDomainID("Invalid");
    assertEquals(-1, id);
    
  }
  
  @Test(expected = DataIntegrityViolationException.class)
  public void uniqueDomain() throws Exception {
    _timeseriesDao.createDomain("D1", "D1");
    // should throw DataIntegrityViolationException
    _timeseriesDao.createDomain("D1", "D1");
  }
  
  @Test
  public void createDomainIdentifiers() throws Exception {
    //1-to-1 mapping between QuotedObj and DomainSpecIdentifier
    Identifier identifier1 = new Identifier("DA", "DA1");
    IdentifierBundle domainIdentifiers = new IdentifierBundle(identifier1);
    
    _timeseriesDao.createDomainSpecIdentifiers(domainIdentifiers, "QO1");
    
    IdentifierBundle actual = _timeseriesDao.findDomainSpecIdentifiersByQuotedObject("QO1");
    assertEquals(domainIdentifiers, actual);
    
    //1-to-Many mapping between QuotedObj and DomainSpecIdentifier
    Identifier identifier2 = new Identifier("DA", "DA2");
    Identifier identifier3 = new Identifier("DB", "DB1");
    domainIdentifiers = new IdentifierBundle(identifier2, identifier3);
    _timeseriesDao.createDomainSpecIdentifiers(domainIdentifiers, "QO2");
    
    actual = _timeseriesDao.findDomainSpecIdentifiersByQuotedObject("QO2");
    assertEquals(domainIdentifiers, actual);
    
    //add DomainSpecIdentifier to existing QuotedObj
    Identifier identifier4 = new Identifier("DC", "DC1");
    domainIdentifiers = new IdentifierBundle(identifier4);
    _timeseriesDao.createDomainSpecIdentifiers(domainIdentifiers, "QO1");
    
    actual = _timeseriesDao.findDomainSpecIdentifiersByQuotedObject("QO1");
    assertEquals(2, actual.size());
    assertTrue(actual.getIdentifiers().contains(identifier1));
    assertTrue(actual.getIdentifiers().contains(identifier4));
    
    //create an existing DomainSpecIdentifier
    Identifier identifier5 = new Identifier("DD", "DD1");
    domainIdentifiers = new IdentifierBundle(identifier2, identifier5);
    _timeseriesDao.createDomainSpecIdentifiers(domainIdentifiers, "QO2");
    
    actual = _timeseriesDao.findDomainSpecIdentifiersByQuotedObject("QO2");
    assertEquals(3, actual.size());
    assertTrue(actual.getIdentifiers().contains(identifier2));
    assertTrue(actual.getIdentifiers().contains(identifier3));
    assertTrue(actual.getIdentifiers().contains(identifier5));
  }
  
  @Test(expected = OpenGammaRuntimeException.class)
  public void createDomainIdentifersWithExistingQuotedObject() throws Exception {
    Identifier da = new Identifier("DA", "DA1");
    Identifier db = new Identifier("DB", "DB1");
    IdentifierBundle domainIdentifiers = new IdentifierBundle(da, db);
    _timeseriesDao.createDomainSpecIdentifiers(domainIdentifiers, "QO1");
    
    Identifier de = new Identifier("DE", "DE1");
    domainIdentifiers = new IdentifierBundle(da, de);
    _timeseriesDao.createDomainSpecIdentifiers(domainIdentifiers, "QO2");
    
  }

  @Test
  public void addTimeSeries() throws Exception {
    addRandonTimeSeriesToDB(2);
    for (int i = 0; i < TS_DATASET_SIZE; i++) {
      IdentifierBundle identifiers = new IdentifierBundle(new Identifier("d" + i, "id" + i));
      LocalDateDoubleTimeSeries timeSeries = makeRandomTimeSeries();
      _timeseriesDao.addTimeSeries(identifiers, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD,
          LCLOSE_OBSERVATION_TIME, timeSeries);
      DoubleTimeSeries<LocalDate> actualTS = _timeseriesDao.getHistoricalTimeSeries(identifiers, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
      assertEquals(timeSeries, actualTS);
    }
    //test set of domain identifiers
    Identifier bbgtickerID = new Identifier("bbgTicker", "AAPL US Equity");
    Identifier cusipID = new Identifier("cusip", "123456789");
    Identifier bbgUniqueID = new Identifier("bbgUnique", "XI45678-89");
    
    IdentifierBundle domainSpeIdentifiers = new IdentifierBundle(bbgUniqueID, bbgtickerID, cusipID);
    
    LocalDateDoubleTimeSeries timeSeries = makeRandomTimeSeries();
    
    _timeseriesDao.addTimeSeries(domainSpeIdentifiers, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD,
        LCLOSE_OBSERVATION_TIME, timeSeries);
    
    DoubleTimeSeries<LocalDate> actualTS = _timeseriesDao.getHistoricalTimeSeries(domainSpeIdentifiers, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
    assertEquals(timeSeries, actualTS);
    
    actualTS = _timeseriesDao.getHistoricalTimeSeries(new IdentifierBundle(cusipID), BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
    assertEquals(timeSeries, actualTS);
    
    actualTS = _timeseriesDao.getHistoricalTimeSeries(new IdentifierBundle(bbgUniqueID), BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
    assertEquals(timeSeries, actualTS);
    
  }
  
  @Test
  public void getTimeSeriesWithDateRange() throws Exception {
    addRandonTimeSeriesToDB(2);
    for (int i = 0; i < TS_DATASET_SIZE; i++) {
      IdentifierBundle bundle = new IdentifierBundle(new Identifier("d" + i, "id" + i));
      LocalDateDoubleTimeSeries timeSeries = makeRandomTimeSeries();
      _timeseriesDao.addTimeSeries(bundle, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD,
          LCLOSE_OBSERVATION_TIME, timeSeries);
      
      LocalDate earliestDate = timeSeries.getEarliestTime();
      LocalDate latestDate = timeSeries.getLatestTime();
      //test end dates
      DoubleTimeSeries<LocalDate> actualTS = _timeseriesDao.getHistoricalTimeSeries(bundle, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, earliestDate, latestDate);
      assertEquals(timeSeries, actualTS);
      //test subSeries
      LocalDate start = earliestDate.plusDays(1);
      LocalDate end = latestDate.minusDays(1);
      if (start.isBefore(end) || start.equals(end)) {
        timeSeries = (LocalDateDoubleTimeSeries)timeSeries.subSeries(start, end);
        actualTS = _timeseriesDao.getHistoricalTimeSeries(bundle, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, start, end);
        assertEquals(timeSeries, actualTS);
      }
    }
  }
  
  @Test
  public void deleteTimeSeries() throws Exception {
    addRandonTimeSeriesToDB(2);
    Set<IdentifierBundle> deletedIdentifiers = new HashSet<IdentifierBundle>();
    //add time series
    for (int i = 0; i < TS_DATASET_SIZE; i++) {
      IdentifierBundle identifier = new IdentifierBundle(new Identifier("d" + i, "id" + i));
      LocalDateDoubleTimeSeries timeSeries = makeRandomTimeSeries();
      //add timeseries to datastore and assert it is in datasource
      _timeseriesDao.addTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD,
          LCLOSE_OBSERVATION_TIME, timeSeries);
      DoubleTimeSeries<LocalDate> actualTS = _timeseriesDao.getHistoricalTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
      assertEquals(timeSeries, actualTS);
      deletedIdentifiers.add(identifier);
    }
    for (IdentifierBundle identifier : deletedIdentifiers) {
      _timeseriesDao.deleteTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME);
      DoubleTimeSeries<LocalDate> actualTS = _timeseriesDao.getHistoricalTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
      assertEquals(ArrayLocalDateDoubleTimeSeries.EMPTY_SERIES, actualTS);
    }
  }
  
  @Test
  public void addTimeSeriesToExistingIdentifiers() throws Exception {
    addRandonTimeSeriesToDB(2);
    //add time series
    for (int i = 0; i < TS_DATASET_SIZE; i++) {
      IdentifierBundle identifier = new IdentifierBundle(new Identifier("d" + i, "id" + i));
      LocalDateDoubleTimeSeries timeSeries = makeRandomTimeSeries();
      _timeseriesDao.addTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD,
          LCLOSE_OBSERVATION_TIME, timeSeries);
      //assert timeseries are in datastore
      DoubleTimeSeries<LocalDate> actualTS = _timeseriesDao.getHistoricalTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
      assertEquals(timeSeries, actualTS);
      //delete timeseries
      _timeseriesDao.deleteTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME);
      actualTS = _timeseriesDao.getHistoricalTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
      assertEquals(ArrayLocalDateDoubleTimeSeries.EMPTY_SERIES, actualTS);
      // add timeseries to existing identifiers in the datastore
      timeSeries = makeRandomTimeSeries();
      _timeseriesDao.addTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD,
          LCLOSE_OBSERVATION_TIME, timeSeries);
      actualTS = _timeseriesDao.getHistoricalTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
      assertEquals(timeSeries, actualTS);
    }
  }
  
  @Test
  public void getEmptyTimeSeries() throws Exception {
    addRandonTimeSeriesToDB(2);
    IdentifierBundle bundle = new IdentifierBundle(new Identifier(IdentificationScheme.BLOOMBERG_TICKER, "AAPL US Equity"));
    DoubleTimeSeries<LocalDate> actualTS = _timeseriesDao.getHistoricalTimeSeries(bundle, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
    assertEquals(ArrayLocalDateDoubleTimeSeries.EMPTY_SERIES, actualTS);
  }
  
  @Test
  public void updateDataPoint() throws Exception {
    addRandonTimeSeriesToDB(2);
    for (int i = 0; i < TS_DATASET_SIZE; i++) {
      IdentifierBundle identifier = new IdentifierBundle(new Identifier("d" + i, "id" + i));
      LocalDateDoubleTimeSeries timeSeries = makeRandomTimeSeries();
      _timeseriesDao.addTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD,
          LCLOSE_OBSERVATION_TIME, timeSeries);
      DoubleTimeSeries<LocalDate> actualTS = _timeseriesDao.getHistoricalTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
      assertEquals(timeSeries, actualTS);
      
      //update datapoint
      List<LocalDate> dates = timeSeries.times();
      List<Double> values = timeSeries.values();
      int updateIdx = _random.nextInt(timeSeries.size());
      LocalDate date = timeSeries.getTime(updateIdx);
      double newValue = _random.nextDouble();
      values.set(updateIdx, newValue);
      
      ArrayLocalDateDoubleTimeSeries updatedTS = new ArrayLocalDateDoubleTimeSeries(dates, values);
      
      _timeseriesDao.updateDataPoint(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME, date, newValue);
      actualTS = _timeseriesDao.getHistoricalTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
      assertEquals(updatedTS, actualTS);
    }
  }
  
  @Test
  public void deleteDataPoint() throws Exception {
    addRandonTimeSeriesToDB(2);
    for (int i = 0; i < TS_DATASET_SIZE; i++) {
      IdentifierBundle identifier = new IdentifierBundle(new Identifier("d" + i, "id" + i));
      LocalDateDoubleTimeSeries timeSeries = makeRandomTimeSeries();
      //add timeseries to datastore
      _timeseriesDao.addTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD,
          LCLOSE_OBSERVATION_TIME, timeSeries);
      //assert timeseries 
      DoubleTimeSeries<LocalDate> actualTS = _timeseriesDao.getHistoricalTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
      assertEquals(timeSeries, actualTS);
      
      //delete random datapoints
      List<LocalDate> dates = timeSeries.times();
      List<Double> values = timeSeries.values();
      int deleteIdx = _random.nextInt(timeSeries.size());
      LocalDate deletedDate = dates.remove(deleteIdx);
      values.remove(deleteIdx);
      
      ArrayLocalDateDoubleTimeSeries deletedTS = new ArrayLocalDateDoubleTimeSeries(dates, values);
      _timeseriesDao.deleteDataPoint(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME, deletedDate);
      
      actualTS = _timeseriesDao.getHistoricalTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
      assertEquals(deletedTS, actualTS);
    }
  }
  
  @Test
  public void getTimeSeriesSnapShot() throws Exception {
    IdentifierBundle identifier = new IdentifierBundle(new Identifier("d1", "id1"));
    
    SortedMap<ZonedDateTime, DoubleTimeSeries<LocalDate>> timeStampTSMap = new TreeMap<ZonedDateTime, DoubleTimeSeries<LocalDate>>();
    LocalDateDoubleTimeSeries timeSeries = makeRandomTimeSeries();
    
    SortedMap<LocalDate, Double> currentTimeSeriesMap = new TreeMap<LocalDate, Double>();
    for (int i = 0; i < timeSeries.size(); i++) {
      currentTimeSeriesMap.put(timeSeries.getTime(i), timeSeries.getValueAt(i));
    }
    
    _timeseriesDao.addTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD,
        LCLOSE_OBSERVATION_TIME, timeSeries);
    DoubleTimeSeries<LocalDate> actualTS = _timeseriesDao.getHistoricalTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
    assertEquals(timeSeries, actualTS);
    
    timeStampTSMap.put(Clock.system(javax.time.calendar.TimeZone.UTC).zonedDateTime(), timeSeries);
    
    //update a random datapoint 3 times
    for (int i = 0; i < 3; i++) {
      List<LocalDate> times = timeSeries.times();
      int ranIndx = _random.nextInt(times.size());
      LocalDate updateDate = times.get(ranIndx);
      Double newValue = _random.nextDouble();
      currentTimeSeriesMap.put(updateDate, newValue);
      _timeseriesDao.updateDataPoint(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME, updateDate, newValue);
      actualTS = _timeseriesDao.getHistoricalTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
      timeSeries = new ArrayLocalDateDoubleTimeSeries(new ArrayList<LocalDate>(currentTimeSeriesMap.keySet()), new ArrayList<Double>(currentTimeSeriesMap.values()));
      assertEquals(timeSeries, actualTS); 
      timeStampTSMap.put(Clock.system(javax.time.calendar.TimeZone.UTC).zonedDateTime(), timeSeries);
    }
    
    //delete a datapoint
    List<LocalDate> times = timeSeries.times();
    int ranIndx = _random.nextInt(times.size());
    LocalDate deleteDate = times.get(ranIndx);
    currentTimeSeriesMap.remove(deleteDate);
    _timeseriesDao.deleteDataPoint(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME, deleteDate);
    actualTS = _timeseriesDao.getHistoricalTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
    timeSeries = new ArrayLocalDateDoubleTimeSeries(new ArrayList<LocalDate>(currentTimeSeriesMap.keySet()), new ArrayList<Double>(currentTimeSeriesMap.values()));
    assertEquals(timeSeries, actualTS); 
    timeStampTSMap.put(Clock.system(javax.time.calendar.TimeZone.UTC).zonedDateTime(), timeSeries);
    
    //delete timeSeries
    _timeseriesDao.deleteTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME);
    actualTS = _timeseriesDao.getHistoricalTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
    timeSeries = ArrayLocalDateDoubleTimeSeries.EMPTY_SERIES;
    assertEquals(timeSeries, actualTS); 
    timeStampTSMap.put(Clock.system(javax.time.calendar.TimeZone.UTC).zonedDateTime(), timeSeries);
    
    //add new timeseries
    timeSeries = makeRandomTimeSeries();
    _timeseriesDao.addTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME, timeSeries);
    actualTS = _timeseriesDao.getHistoricalTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
    assertEquals(timeSeries, actualTS);
    timeStampTSMap.put(Clock.system(javax.time.calendar.TimeZone.UTC).zonedDateTime(), timeSeries);
    
    //assert datasnapshots
    for (Entry<ZonedDateTime, DoubleTimeSeries<LocalDate>> entry : timeStampTSMap.entrySet()) {
      ZonedDateTime timeStamp = entry.getKey();
      DoubleTimeSeries<LocalDate> expectedTS = entry.getValue();
      DoubleTimeSeries<LocalDate> snapshotTS = _timeseriesDao.getTimeSeriesSnapShot(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME, timeStamp);
      assertEquals(expectedTS, snapshotTS);
    }
    
    //assert before and after last deltas
    //before 1st delta should return empty timeseries
    ZonedDateTime beforeDelta = timeStampTSMap.firstKey().minusMinutes(1);
    DoubleTimeSeries<LocalDate> snapshotTS = _timeseriesDao.getTimeSeriesSnapShot(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME, beforeDelta);
    assertEquals(ArrayLocalDateDoubleTimeSeries.EMPTY_SERIES, snapshotTS);
    //after last delta should return latest timeseries
    ZonedDateTime afterDelta = timeStampTSMap.lastKey().plusMinutes(1);
    DoubleTimeSeries<LocalDate> latestTS = _timeseriesDao.getHistoricalTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
    assertEquals(timeStampTSMap.get(timeStampTSMap.lastKey()), latestTS);
    snapshotTS = _timeseriesDao.getTimeSeriesSnapShot(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME, afterDelta);
    assertEquals(latestTS, snapshotTS);
    
  }
  
  /**
   * @return
   */
  private LocalDateDoubleTimeSeries makeRandomTimeSeries() {
    MapLocalDateDoubleTimeSeries tsMap = new MapLocalDateDoubleTimeSeries();
    for (int i = 0; i < TS_MAX_SIZE; i++) {
      int year = 1970 + _random.nextInt(40);
      int monthOfYear = 1 + _random.nextInt(12);
      int dayOfMonth = 1 + _random.nextInt(28);
      tsMap.putDataPoint(LocalDate.of(year, monthOfYear, dayOfMonth), _random.nextDouble());
    }
    return new ArrayLocalDateDoubleTimeSeries(tsMap);
  }
  
  private void addRandonTimeSeriesToDB(int size) {
    for (int i = 0; i < size; i++) {
      Identifier identifier = new Identifier("t" + i, "tid" + i);
      _timeseriesDao.addTimeSeries(new IdentifierBundle(identifier), BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD,
          LCLOSE_OBSERVATION_TIME, makeRandomTimeSeries());
    }
  }

}
