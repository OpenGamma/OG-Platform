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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.time.calendar.Clock;
import javax.time.calendar.TimeZone;
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
import com.opengamma.id.DomainSpecificIdentifier;
import com.opengamma.timeseries.ArrayDoubleTimeSeries;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.MapDoubleTimeSeries;
import com.opengamma.util.test.DBTest;
import com.opengamma.util.time.DateUtil;

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
  private static final String BBG_DATA_SOURCE = "BBG";

  private Random _random = new Random();
  private TimeSeriesDao _timeseriesDao;
  
  public RowStoreTimeSeriesDaoTest(String databaseType) {
    super(databaseType);
    s_logger.info("running testcases for {}", databaseType);
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
    assertEquals(-1, id);
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
    Set<DomainSpecificIdentifier> domainIdentifiers = new HashSet<DomainSpecificIdentifier>();
    DomainSpecificIdentifier identifier1 = new DomainSpecificIdentifier("DA", "DA1");
    domainIdentifiers.add(identifier1);
    _timeseriesDao.createDomainSpecIdentifiers(domainIdentifiers, "QO1");
    
    Set<DomainSpecificIdentifier> actual = _timeseriesDao.findDomainSpecIdentifiersByQuotedObject("QO1");
    assertEquals(domainIdentifiers, actual);
    
    //1-to-Many mapping between QuotedObj and DomainSpecIdentifier
    domainIdentifiers.clear();
    DomainSpecificIdentifier identifier2 = new DomainSpecificIdentifier("DA", "DA2");
    domainIdentifiers.add(identifier2);
    DomainSpecificIdentifier identifier3 = new DomainSpecificIdentifier("DB", "DB1");
    domainIdentifiers.add(identifier3);
    _timeseriesDao.createDomainSpecIdentifiers(domainIdentifiers, "QO2");
    
    actual = _timeseriesDao.findDomainSpecIdentifiersByQuotedObject("QO2");
    assertEquals(domainIdentifiers, actual);
    
    //add DomainSpecIdentifier to existing QuotedObj
    domainIdentifiers.clear();
    DomainSpecificIdentifier identifier4 = new DomainSpecificIdentifier("DC", "DC1");
    domainIdentifiers.add(identifier4);
    _timeseriesDao.createDomainSpecIdentifiers(domainIdentifiers, "QO1");
    
    actual = _timeseriesDao.findDomainSpecIdentifiersByQuotedObject("QO1");
    assertEquals(2, actual.size());
    assertTrue(actual.contains(identifier1));
    assertTrue(actual.contains(identifier4));
    
    //create an existing DomainSpecIdentifier
    domainIdentifiers.clear();
    domainIdentifiers.add(identifier2);
    DomainSpecificIdentifier identifier5 = new DomainSpecificIdentifier("DD", "DD1");
    domainIdentifiers.add(identifier5);
    _timeseriesDao.createDomainSpecIdentifiers(domainIdentifiers, "QO2");
    
    actual = _timeseriesDao.findDomainSpecIdentifiersByQuotedObject("QO2");
    assertEquals(3, actual.size());
    assertTrue(actual.contains(identifier2));
    assertTrue(actual.contains(identifier3));
    assertTrue(actual.contains(identifier5));
  }
  
  @Test(expected = OpenGammaRuntimeException.class)
  public void createDomainIdentifersWithExistingQuotedObject() throws Exception {
    Set<DomainSpecificIdentifier> domainIdentifiers = new HashSet<DomainSpecificIdentifier>();
    domainIdentifiers.add(new DomainSpecificIdentifier("DA", "DA1"));
    domainIdentifiers.add(new DomainSpecificIdentifier("DB", "DB1"));
    _timeseriesDao.createDomainSpecIdentifiers(domainIdentifiers, "QO1");
    
    domainIdentifiers.clear();
    domainIdentifiers.add(new DomainSpecificIdentifier("DA", "DA1"));
    domainIdentifiers.add(new DomainSpecificIdentifier("DE", "DE1"));
    _timeseriesDao.createDomainSpecIdentifiers(domainIdentifiers, "QO2");
    
  }

  @Test
  public void addTimeSeries() throws Exception {
    
    Map<DomainSpecificIdentifier, DoubleTimeSeries> tsMap = new HashMap<DomainSpecificIdentifier, DoubleTimeSeries>();
    for (int i = 0; i < TS_DATASET_SIZE; i++) {
      tsMap.put(new DomainSpecificIdentifier("d" + i, "id" + i), makeRandomTimeSeries());
    }
        
    for (Entry<DomainSpecificIdentifier, DoubleTimeSeries> entry : tsMap.entrySet()) {
      DomainSpecificIdentifier domainSpecificIdentifier = entry.getKey();
      DoubleTimeSeries timeSeries = entry.getValue();
      _timeseriesDao.addTimeSeries(Collections.singleton(domainSpecificIdentifier), BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD,
          LCLOSE_OBSERVATION_TIME, timeSeries);
      DoubleTimeSeries actualTS = _timeseriesDao.getTimeSeries(domainSpecificIdentifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME);
      assertEquals(timeSeries, actualTS);
    }
    
    //test set of domain identifiers
    DomainSpecificIdentifier bbgtickerID = new DomainSpecificIdentifier("bbgTicker", "AAPL US Equity");
    DomainSpecificIdentifier cusipID = new DomainSpecificIdentifier("cusip", "123456789");
    DomainSpecificIdentifier bbgUniqueID = new DomainSpecificIdentifier("bbgUnique", "XI45678-89");
    
    Set<DomainSpecificIdentifier> domainSpeIdentifiers = new HashSet<DomainSpecificIdentifier>();
    domainSpeIdentifiers.add(bbgtickerID);
    domainSpeIdentifiers.add(cusipID);
    domainSpeIdentifiers.add(bbgUniqueID);
    
    DoubleTimeSeries timeSeries = makeRandomTimeSeries();
    
    _timeseriesDao.addTimeSeries(domainSpeIdentifiers, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD,
        LCLOSE_OBSERVATION_TIME, timeSeries);
    
    DoubleTimeSeries actualTS = _timeseriesDao.getTimeSeries(bbgtickerID, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME);
    assertEquals(timeSeries, actualTS);
    
    actualTS = _timeseriesDao.getTimeSeries(cusipID, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME);
    assertEquals(timeSeries, actualTS);
    
    actualTS = _timeseriesDao.getTimeSeries(bbgUniqueID, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME);
    assertEquals(timeSeries, actualTS);
    
  }
  
  @Test
  public void getTimeSeriesWithDateRange() throws Exception {
    Map<DomainSpecificIdentifier, DoubleTimeSeries> tsMap = new HashMap<DomainSpecificIdentifier, DoubleTimeSeries>();
    for (int i = 0; i < TS_DATASET_SIZE; i++) {
      tsMap.put(new DomainSpecificIdentifier("d" + i, "id" + i), makeRandomTimeSeries());
    }
        
    for (Entry<DomainSpecificIdentifier, DoubleTimeSeries> entry : tsMap.entrySet()) {
      DomainSpecificIdentifier domainSpecificIdentifier = entry.getKey();
      DoubleTimeSeries timeSeries = entry.getValue();
      _timeseriesDao.addTimeSeries(Collections.singleton(domainSpecificIdentifier), BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD,
          LCLOSE_OBSERVATION_TIME, timeSeries);
      
      ZonedDateTime earliestTime = timeSeries.getEarliestTime();
      ZonedDateTime latestTime = timeSeries.getLatestTime();
      //test end dates
      DoubleTimeSeries actualTS = _timeseriesDao.getTimeSeries(domainSpecificIdentifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME, earliestTime, latestTime);
      assertEquals(timeSeries, actualTS);
      //test subSeries
      ZonedDateTime start = earliestTime.plusDays(1);
      ZonedDateTime end = latestTime.minusDays(1);
      if (start.isBefore(end) || start.equals(end)) {
        timeSeries = timeSeries.subSeries(start, end);
        actualTS = _timeseriesDao.getTimeSeries(domainSpecificIdentifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME, start, end);
        assertEquals(timeSeries, actualTS);
      }
    }
  }
  
  @Test
  public void deleteTimeSeries() throws Exception {
    Map<DomainSpecificIdentifier, DoubleTimeSeries> tsMap = new HashMap<DomainSpecificIdentifier, DoubleTimeSeries>();
    Set<DomainSpecificIdentifier> deletedIdentifiers = new HashSet<DomainSpecificIdentifier>();
    //add time series
    for (int i = 0; i < TS_DATASET_SIZE; i++) {
      DomainSpecificIdentifier identifier = new DomainSpecificIdentifier("d" + i, "id" + i);
      DoubleTimeSeries timeSeries = makeRandomTimeSeries();
      tsMap.put(identifier, timeSeries);
      _timeseriesDao.addTimeSeries(Collections.singleton(identifier), BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD,
          LCLOSE_OBSERVATION_TIME, timeSeries);
    }
    //assert timeseries are in datastore
    for (Entry<DomainSpecificIdentifier, DoubleTimeSeries> entry : tsMap.entrySet()) {
      DomainSpecificIdentifier identifier = entry.getKey();
      DoubleTimeSeries timeSeries = entry.getValue();
      DoubleTimeSeries actualTS = _timeseriesDao.getTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME);
      assertEquals(timeSeries, actualTS);
    }
    //delete random timeseries
    for (DomainSpecificIdentifier key : tsMap.keySet()) {
      int delete = _random.nextInt(2);
      if (delete == 1) {
        deletedIdentifiers.add(key);
        _timeseriesDao.deleteTimeSeries(key, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME);
      }
    }
    //assert deleted timeseries return empty timeseries
    for (Entry<DomainSpecificIdentifier, DoubleTimeSeries> entry : tsMap.entrySet()) {
      DomainSpecificIdentifier identifier = entry.getKey();
      DoubleTimeSeries timeSeries = entry.getValue();
      DoubleTimeSeries actualTS = _timeseriesDao.getTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME);
      if (deletedIdentifiers.contains(identifier)) {
        assertEquals(ArrayDoubleTimeSeries.EMPTY_SERIES, actualTS);
      } else {
        assertEquals(timeSeries, actualTS);
      }
    }
  }
  
  @Test
  public void addTimeSeriesToExistingIdentifiers() throws Exception {
    Map<DomainSpecificIdentifier, DoubleTimeSeries> tsMap = new HashMap<DomainSpecificIdentifier, DoubleTimeSeries>();
    Set<DomainSpecificIdentifier> deletedIdentifiers = new HashSet<DomainSpecificIdentifier>();
    //add time series
    for (int i = 0; i < TS_DATASET_SIZE; i++) {
      DomainSpecificIdentifier identifier = new DomainSpecificIdentifier("d" + i, "id" + i);
      DoubleTimeSeries timeSeries = makeRandomTimeSeries();
      tsMap.put(identifier, timeSeries);
      _timeseriesDao.addTimeSeries(Collections.singleton(identifier), BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD,
          LCLOSE_OBSERVATION_TIME, timeSeries);
    }
    //assert timeseries are in datastore
    for (Entry<DomainSpecificIdentifier, DoubleTimeSeries> entry : tsMap.entrySet()) {
      DomainSpecificIdentifier identifier = entry.getKey();
      DoubleTimeSeries timeSeries = entry.getValue();
      DoubleTimeSeries actualTS = _timeseriesDao.getTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME);
      assertEquals(timeSeries, actualTS);
    }
    //delete random timeseries
    for (DomainSpecificIdentifier key : tsMap.keySet()) {
      int delete = _random.nextInt(2);
      if (delete == 1) {
        deletedIdentifiers.add(key);
        _timeseriesDao.deleteTimeSeries(key, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME);
      }
    }
    //assert deleted timeseries return empty timeseries
    for (Entry<DomainSpecificIdentifier, DoubleTimeSeries> entry : tsMap.entrySet()) {
      DomainSpecificIdentifier identifier = entry.getKey();
      DoubleTimeSeries timeSeries = entry.getValue();
      DoubleTimeSeries actualTS = _timeseriesDao.getTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME);
      if (deletedIdentifiers.contains(identifier)) {
        assertEquals(ArrayDoubleTimeSeries.EMPTY_SERIES, actualTS);
      } else {
        assertEquals(timeSeries, actualTS);
      }
    }
    // add timeseries to existing identifiers in the datastore
    for (DomainSpecificIdentifier domainSpecificIdentifier : deletedIdentifiers) {
      DoubleTimeSeries timeSeries = tsMap.get(domainSpecificIdentifier);
      _timeseriesDao.addTimeSeries(Collections.singleton(domainSpecificIdentifier), BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD,
          LCLOSE_OBSERVATION_TIME, timeSeries);
    }
    //assert timeseries are in datastore
    for (Entry<DomainSpecificIdentifier, DoubleTimeSeries> entry : tsMap.entrySet()) {
      DomainSpecificIdentifier identifier = entry.getKey();
      DoubleTimeSeries timeSeries = entry.getValue();
      DoubleTimeSeries actualTS = _timeseriesDao.getTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME);
      assertEquals(timeSeries, actualTS);
    }
  }
  
  @Test
  public void getEmptyTimeSeries() throws Exception {
    DomainSpecificIdentifier bbgtickerID = new DomainSpecificIdentifier("bbgTicker", "AAPL US Equity");
    DoubleTimeSeries actualTS = _timeseriesDao.getTimeSeries(bbgtickerID, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME);
    assertEquals(ArrayDoubleTimeSeries.EMPTY_SERIES, actualTS);
  }
  
  @Test
  public void updateDataPoint() throws Exception {
    Map<DomainSpecificIdentifier, DoubleTimeSeries> tsMap = new HashMap<DomainSpecificIdentifier, DoubleTimeSeries>();
    //add time series
    for (int i = 0; i < TS_DATASET_SIZE; i++) {
      DomainSpecificIdentifier identifier = new DomainSpecificIdentifier("d" + i, "id" + i);
      DoubleTimeSeries timeSeries = makeRandomTimeSeries();
      tsMap.put(identifier, timeSeries);
      _timeseriesDao.addTimeSeries(Collections.singleton(identifier), BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD,
          LCLOSE_OBSERVATION_TIME, timeSeries);
    }
    //assert timeseries are in datastore
    for (Entry<DomainSpecificIdentifier, DoubleTimeSeries> entry : tsMap.entrySet()) {
      DomainSpecificIdentifier identifier = entry.getKey();
      DoubleTimeSeries timeSeries = entry.getValue();
      DoubleTimeSeries actualTS = _timeseriesDao.getTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME);
      assertEquals(timeSeries, actualTS);
    }
    //update random datapoints
    for (Entry<DomainSpecificIdentifier, DoubleTimeSeries> entry : tsMap.entrySet()) {
      DomainSpecificIdentifier identifier = entry.getKey();
      DoubleTimeSeries timeSeries = entry.getValue();
      
      List<ZonedDateTime> times = timeSeries.times();
      List<Double> values = timeSeries.values();
      
      int updateIdx = _random.nextInt(timeSeries.size());
      ZonedDateTime date = timeSeries.getTime(updateIdx);
      double newValue = _random.nextDouble();
      values.set(updateIdx, newValue);
      
      // put updated timeseries in map
      tsMap.put(identifier, new ArrayDoubleTimeSeries(times, values));
      _timeseriesDao.updateDataPoint(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME, date, newValue);
    }
    //assert datapoints are updated in datastore
    for (Entry<DomainSpecificIdentifier, DoubleTimeSeries> entry : tsMap.entrySet()) {
      DomainSpecificIdentifier identifier = entry.getKey();
      DoubleTimeSeries timeSeries = entry.getValue();
      DoubleTimeSeries actualTS = _timeseriesDao.getTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME);
      assertEquals(timeSeries, actualTS);
    }
  }
  
  @Test
  public void deleteDataPoint() throws Exception {
    Map<DomainSpecificIdentifier, DoubleTimeSeries> tsMap = new HashMap<DomainSpecificIdentifier, DoubleTimeSeries>();
    //add time series
    for (int i = 0; i < TS_DATASET_SIZE; i++) {
      DomainSpecificIdentifier identifier = new DomainSpecificIdentifier("d" + i, "id" + i);
      DoubleTimeSeries timeSeries = makeRandomTimeSeries();
      tsMap.put(identifier, timeSeries);
      _timeseriesDao.addTimeSeries(Collections.singleton(identifier), BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD,
          LCLOSE_OBSERVATION_TIME, timeSeries);
    }
    //assert timeseries are in datastore
    for (Entry<DomainSpecificIdentifier, DoubleTimeSeries> entry : tsMap.entrySet()) {
      DomainSpecificIdentifier identifier = entry.getKey();
      DoubleTimeSeries timeSeries = entry.getValue();
      DoubleTimeSeries actualTS = _timeseriesDao.getTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME);
      assertEquals(timeSeries, actualTS);
    }
    //delete random datapoints
    for (Entry<DomainSpecificIdentifier, DoubleTimeSeries> entry : tsMap.entrySet()) {
      DomainSpecificIdentifier identifier = entry.getKey();
      DoubleTimeSeries timeSeries = entry.getValue();
      
      SortedMap<ZonedDateTime, Double> ts = new TreeMap<ZonedDateTime, Double>();
      for (int i = 0; i < timeSeries.size(); i++) {
        ts.put(timeSeries.getTime(i), timeSeries.getValue(i));
      }
      
      int deleteIdx = _random.nextInt(timeSeries.size());
      ZonedDateTime date = timeSeries.getTime(deleteIdx);
      
      ts.remove(date);
      // put updated timeseries in map
      tsMap.put(identifier, new MapDoubleTimeSeries(ts));
      _timeseriesDao.deleteDataPoint(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME, date);
    }
    //assert datapoints are updated in datastore
    for (Entry<DomainSpecificIdentifier, DoubleTimeSeries> entry : tsMap.entrySet()) {
      DomainSpecificIdentifier identifier = entry.getKey();
      DoubleTimeSeries timeSeries = entry.getValue();
      DoubleTimeSeries actualTS = _timeseriesDao.getTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME);
      assertEquals(timeSeries, actualTS);
    }
  }
  
  @Test
  public void getTimeSeriesSnapShot() throws Exception {
    DomainSpecificIdentifier identifier = new DomainSpecificIdentifier("d1", "id1");
    
    SortedMap<ZonedDateTime, DoubleTimeSeries> timeStampTSMap = new TreeMap<ZonedDateTime, DoubleTimeSeries>();
    DoubleTimeSeries timeSeries = makeRandomTimeSeries();
    
    SortedMap<ZonedDateTime, Double> tsMap = new TreeMap<ZonedDateTime, Double>();
    for (int i = 0; i < timeSeries.size(); i++) {
      tsMap.put(timeSeries.getTime(i), timeSeries.getValue(i));
    }
    
    _timeseriesDao.addTimeSeries(Collections.singleton(identifier), BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD,
        LCLOSE_OBSERVATION_TIME, timeSeries);
    DoubleTimeSeries actualTS = _timeseriesDao.getTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME);
    assertEquals(timeSeries, actualTS);
    
    timeStampTSMap.put(Clock.system(TimeZone.UTC).zonedDateTime(), timeSeries);
    
    //update a random datapoint 10 times
    for (int i = 0; i < 3; i++) {
      List<ZonedDateTime> times = timeSeries.times();
      int ranIndx = _random.nextInt(times.size());
      ZonedDateTime updateDate = times.get(ranIndx);
      Double newValue = _random.nextDouble();
      tsMap.put(updateDate, newValue);
      _timeseriesDao.updateDataPoint(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME, updateDate, newValue);
      actualTS = _timeseriesDao.getTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME);
      timeSeries = new MapDoubleTimeSeries(tsMap);
      assertEquals(timeSeries, actualTS); 
      timeStampTSMap.put(Clock.system(TimeZone.UTC).zonedDateTime(), timeSeries);
    }
    
    //delete a datapoint
    List<ZonedDateTime> times = timeSeries.times();
    int ranIndx = _random.nextInt(times.size());
    ZonedDateTime deleteDate = times.get(ranIndx);
    tsMap.remove(deleteDate);
    _timeseriesDao.deleteDataPoint(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME, deleteDate);
    actualTS = _timeseriesDao.getTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME);
    timeSeries = new MapDoubleTimeSeries(tsMap);
    assertEquals(timeSeries, actualTS); 
    timeStampTSMap.put(Clock.system(TimeZone.UTC).zonedDateTime(), timeSeries);
    
    //delete timeSeries
    _timeseriesDao.deleteTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME);
    actualTS = _timeseriesDao.getTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME);
    timeSeries = ArrayDoubleTimeSeries.EMPTY_SERIES;
    assertEquals(timeSeries, actualTS); 
    timeStampTSMap.put(Clock.system(TimeZone.UTC).zonedDateTime(), timeSeries);
    
    //add new timeseries
    timeSeries = makeRandomTimeSeries();
    _timeseriesDao.addTimeSeries(Collections.singleton(identifier), BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD,
        LCLOSE_OBSERVATION_TIME, timeSeries);
    actualTS = _timeseriesDao.getTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME);
    assertEquals(timeSeries, actualTS);
    timeStampTSMap.put(Clock.system(TimeZone.UTC).zonedDateTime(), timeSeries);
    
    //assert datasnapshots
    for (Entry<ZonedDateTime, DoubleTimeSeries> entry : timeStampTSMap.entrySet()) {
      ZonedDateTime timeStamp = entry.getKey();
      DoubleTimeSeries expectedTS = entry.getValue();
      DoubleTimeSeries snapshotTS = _timeseriesDao.getTimeSeriesSnapShot(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME, timeStamp);
      assertEquals(expectedTS, snapshotTS);
    }
    
    //assert before and after last deltas
    //before 1st delta should return empty timeseries
    ZonedDateTime beforeDelta = timeStampTSMap.firstKey().minusMinutes(1);
    DoubleTimeSeries snapshotTS = _timeseriesDao.getTimeSeriesSnapShot(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME, beforeDelta);
    assertEquals(ArrayDoubleTimeSeries.EMPTY_SERIES, snapshotTS);
    //after last delta should return latest timeseries
    ZonedDateTime afterDelta = timeStampTSMap.lastKey().plusMinutes(1);
    DoubleTimeSeries latestTS = _timeseriesDao.getTimeSeries(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME);
    assertEquals(timeStampTSMap.get(timeStampTSMap.lastKey()), latestTS);
    snapshotTS = _timeseriesDao.getTimeSeriesSnapShot(identifier, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME, afterDelta);
    assertEquals(latestTS, snapshotTS);
    
  }
  
  /**
   * @return
   */
  private DoubleTimeSeries makeRandomTimeSeries() {
    
    SortedMap<ZonedDateTime, Double> tsMap = new TreeMap<ZonedDateTime, Double>();
    for (int i = 0; i < TS_MAX_SIZE; i++) {
      int year = 1970 + _random.nextInt(40);
      int month = 1 + _random.nextInt(11);
      int day = 1 + _random.nextInt(28);
      tsMap.put(DateUtil.getUTCDate(year, month, day), _random.nextDouble());
    }
    return new MapDoubleTimeSeries(tsMap);
  }

}
