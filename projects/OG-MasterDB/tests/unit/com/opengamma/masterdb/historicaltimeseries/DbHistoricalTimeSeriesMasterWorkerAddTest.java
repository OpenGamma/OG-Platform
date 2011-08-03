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
import com.opengamma.id.UniqueId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.util.test.DBTest;

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
    HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument();
    _htsMaster.add(doc);
  }

  @Test
  public void test_add_add() {
    Instant now = Instant.now(_htsMaster.getTimeSource());
    
    ManageableHistoricalTimeSeriesInfo info = new ManageableHistoricalTimeSeriesInfo();
    info.setName("Added");
    info.setDataField("DF");
    info.setDataSource("DS");
    info.setDataProvider("DP");
    info.setObservationTime("OT");
    IdentifierWithDates id = IdentifierWithDates.of(Identifier.of("A", "B"), LocalDate.of(2011, 6, 30), null);
    IdentifierBundleWithDates bundle = IdentifierBundleWithDates.of(id);
    info.setIdentifiers(bundle);
    HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument(info);
    HistoricalTimeSeriesInfoDocument test = _htsMaster.add(doc);
    
    UniqueId uniqueId = test.getUniqueId();
    assertNotNull(uniqueId);
    assertEquals("DbHts", uniqueId.getScheme());
    assertTrue(uniqueId.isVersioned());
    assertTrue(Long.parseLong(uniqueId.getValue()) >= 1000);
    assertEquals("0", uniqueId.getVersion());
    assertEquals(now, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(now, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableHistoricalTimeSeriesInfo testInfo = test.getInfo();
    assertNotNull(testInfo);
    assertEquals(uniqueId, testInfo.getUniqueId());
    assertEquals("Added", testInfo.getName());
    assertEquals("DF", testInfo.getDataField());
    assertEquals("DS", testInfo.getDataSource());
    assertEquals("DP", testInfo.getDataProvider());
    assertEquals("OT", testInfo.getObservationTime());
    assertEquals(1, testInfo.getIdentifiers().size());
    assertTrue(testInfo.getIdentifiers().getIdentifiers().contains(id));
  }

  @Test
  public void test_add_addThenGet() {
    ManageableHistoricalTimeSeriesInfo info = new ManageableHistoricalTimeSeriesInfo();
    info.setName("Added");
    info.setDataField("DF");
    info.setDataSource("DS");
    info.setDataProvider("DP");
    info.setObservationTime("OT");
    IdentifierWithDates id = IdentifierWithDates.of(Identifier.of("A", "B"), LocalDate.of(2011, 6, 30), null);
    IdentifierBundleWithDates bundle = IdentifierBundleWithDates.of(id);
    info.setIdentifiers(bundle);
    HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument(info);
    HistoricalTimeSeriesInfoDocument added = _htsMaster.add(doc);
    
    HistoricalTimeSeriesInfoDocument test = _htsMaster.get(added.getUniqueId());
    assertEquals(added, test);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_htsMaster.getClass().getSimpleName() + "[DbHts]", _htsMaster.toString());
  }
}
