/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.historicaltimeseries;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.TimeZone;

import javax.time.Instant;
import javax.time.calendar.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundleWithDates;
import com.opengamma.id.IdentifierWithDates;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesDocument;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeries;
import com.opengamma.util.test.DBTest;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;

/**
 * Tests DbHistoricalTimeSeriesMaster.
 */
public class DbHistoricalTimeSeriesMasterWorkerAddTest extends AbstractDbHistoricalTimeSeriesMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(DbHistoricalTimeSeriesMasterWorkerAddTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DBTest.class)
  public DbHistoricalTimeSeriesMasterWorkerAddTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_add_nullDocument() {
    _htsMaster.add(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_add_noHistoricalTimeSeries() {
    HistoricalTimeSeriesDocument doc = new HistoricalTimeSeriesDocument();
    _htsMaster.add(doc);
  }

  @Test
  public void test_add_add() {
    Instant now = Instant.now(_htsMaster.getTimeSource());
    
    ManageableHistoricalTimeSeries hts = new ManageableHistoricalTimeSeries();
    hts.setName("Added");
    hts.setDataField("DF");
    hts.setDataSource("DS");
    hts.setDataProvider("DP");
    hts.setObservationTime("OT");
    IdentifierWithDates id = IdentifierWithDates.of(Identifier.of("A", "B"), LocalDate.of(2011, 6, 30), null);
    IdentifierBundleWithDates bundle = IdentifierBundleWithDates.of(id);
    hts.setIdentifiers(bundle);
    HistoricalTimeSeriesDocument doc = new HistoricalTimeSeriesDocument(hts);
    HistoricalTimeSeriesDocument test = _htsMaster.add(doc);
    
    UniqueIdentifier uid = test.getUniqueId();
    assertNotNull(uid);
    assertEquals("DbHts", uid.getScheme());
    assertTrue(uid.isVersioned());
    assertTrue(Long.parseLong(uid.getValue()) >= 1000);
    assertEquals("0", uid.getVersion());
    assertEquals(now, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(now, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableHistoricalTimeSeries testHistoricalTimeSeries = test.getSeries();
    assertNotNull(testHistoricalTimeSeries);
    assertEquals(uid, testHistoricalTimeSeries.getUniqueId());
    assertEquals("Added", testHistoricalTimeSeries.getName());
    assertEquals("DF", testHistoricalTimeSeries.getDataField());
    assertEquals("DS", testHistoricalTimeSeries.getDataSource());
    assertEquals("DP", testHistoricalTimeSeries.getDataProvider());
    assertEquals("OT", testHistoricalTimeSeries.getObservationTime());
    assertEquals(1, testHistoricalTimeSeries.getIdentifiers().size());
    assertTrue(testHistoricalTimeSeries.getIdentifiers().getIdentifiers().contains(id));
  }

  @Test
  public void test_add_addThenGet() {
    ManageableHistoricalTimeSeries hts = new ManageableHistoricalTimeSeries();
    hts.setName("Added");
    hts.setDataField("DF");
    hts.setDataSource("DS");
    hts.setDataProvider("DP");
    hts.setObservationTime("OT");
    hts.setTimeSeries(new ArrayLocalDateDoubleTimeSeries());
    IdentifierWithDates id = IdentifierWithDates.of(Identifier.of("A", "B"), LocalDate.of(2011, 6, 30), null);
    IdentifierBundleWithDates bundle = IdentifierBundleWithDates.of(id);
    hts.setIdentifiers(bundle);
    HistoricalTimeSeriesDocument doc = new HistoricalTimeSeriesDocument(hts);
    HistoricalTimeSeriesDocument added = _htsMaster.add(doc);
    
    HistoricalTimeSeriesDocument test = _htsMaster.get(added.getUniqueId());
    assertEquals(added, test);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_htsMaster.getClass().getSimpleName() + "[DbHts]", _htsMaster.toString());
  }
}
