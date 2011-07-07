/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.historicaltimeseries;

import static org.testng.AssertJUnit.assertEquals;

import java.util.TimeZone;

import javax.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesDocument;
import com.opengamma.util.test.DBTest;

/**
 * Tests DbHistoricalTimeSeriesMaster.
 */
public class DbHistoricalTimeSeriesMasterWorkerRemoveTest extends AbstractDbHistoricalTimeSeriesMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(DbHistoricalTimeSeriesMasterWorkerRemoveTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DBTest.class)
  public DbHistoricalTimeSeriesMasterWorkerRemoveTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_removeHistoricalTimeSeries_versioned_notFoundId() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbHts", "0", "0");
    _htsMaster.remove(uid);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_removeHistoricalTimeSeries_versioned_notFoundVersion() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbHts", "101", "1");
    _htsMaster.remove(uid);
  }

  @Test
  public void test_removeHistoricalTimeSeries_removed() {
    Instant now = Instant.now(_htsMaster.getTimeSource());
    
    UniqueIdentifier uid = UniqueIdentifier.of("DbHts", "102", "0");
    _htsMaster.remove(uid);
    HistoricalTimeSeriesDocument test = _htsMaster.get(uid);
    
    assertEquals(uid, test.getUniqueId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(now, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    assertEquals(uid, test.getSeries().getUniqueId());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_htsMaster.getClass().getSimpleName() + "[DbHts]", _htsMaster.toString());
  }

}
