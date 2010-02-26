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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;

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
import com.opengamma.util.test.DBTest;
import com.opengamma.util.time.DateUtil;

/**
 * 
 * 
 * @author yomi
 */
public class RowStoreTimeSeriesDaoTest extends DBTest {
  private static final Logger s_logger = LoggerFactory.getLogger(RowStoreTimeSeriesDaoTest.class);
  
  private static final int TIMESERIES_SIZE = 100;

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
    for (int i = 0; i < TIMESERIES_SIZE; i++) {
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
  
  /**
   * @return
   */
  private DoubleTimeSeries makeRandomTimeSeries() {
    
    List<ZonedDateTime> times = new ArrayList<ZonedDateTime>();
    List<Double> values = new ArrayList<Double>();
    
    int tsSize = 10 + _random.nextInt(18);
    int year = 1970 + _random.nextInt(40);
    int month = 1 + _random.nextInt(11);
    for (int i = 1; i < tsSize; i++) {
      times.add(DateUtil.getUTCDate(year, month, i));
      values.add(_random.nextDouble());
    }
    
    ArrayDoubleTimeSeries timeSeries = new ArrayDoubleTimeSeries(times, values);
    return timeSeries;
  }

  @Test
  public void getEmptyTimeSeries() throws Exception {
    DomainSpecificIdentifier bbgtickerID = new DomainSpecificIdentifier("bbgTicker", "AAPL US Equity");
    DoubleTimeSeries actualTS = _timeseriesDao.getTimeSeries(bbgtickerID, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, LCLOSE_OBSERVATION_TIME);
    assertEquals(ArrayDoubleTimeSeries.EMPTY_SERIES, actualTS);
    
  }

}
