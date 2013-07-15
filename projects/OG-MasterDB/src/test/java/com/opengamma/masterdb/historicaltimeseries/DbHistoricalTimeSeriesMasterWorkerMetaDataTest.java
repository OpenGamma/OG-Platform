/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.historicaltimeseries;

import static org.testng.AssertJUnit.assertEquals;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoMetaDataRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoMetaDataResult;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests DbHistoricalTimeSeriesMaster.
 */
@Test(groups = TestGroup.UNIT_DB)
public class DbHistoricalTimeSeriesMasterWorkerMetaDataTest extends AbstractDbHistoricalTimeSeriesMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(DbHistoricalTimeSeriesMasterWorkerMetaDataTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public DbHistoricalTimeSeriesMasterWorkerMetaDataTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_metaData_null() {
    _htsMaster.metaData(null);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_metaData() {
    HistoricalTimeSeriesInfoMetaDataRequest request = new HistoricalTimeSeriesInfoMetaDataRequest();
    HistoricalTimeSeriesInfoMetaDataResult test = _htsMaster.metaData(request);
    assertEquals(2, test.getDataFields().size());
    assertEquals("DF11", test.getDataFields().get(0));
    assertEquals("DF12", test.getDataFields().get(1));
    assertEquals(2, test.getDataSources().size());
    assertEquals("DS21", test.getDataSources().get(0));
    assertEquals("DS22", test.getDataSources().get(1));
    assertEquals(2, test.getDataProviders().size());
    assertEquals("DP31", test.getDataProviders().get(0));
    assertEquals("DP32", test.getDataProviders().get(1));
    assertEquals(2, test.getObservationTimes().size());
    assertEquals("OT41", test.getObservationTimes().get(0));
    assertEquals("OT42", test.getObservationTimes().get(1));
  }

  @Test
  public void test_metaData_limited() {
    HistoricalTimeSeriesInfoMetaDataRequest request = new HistoricalTimeSeriesInfoMetaDataRequest();
    request.setDataProviders(false);
    request.setObservationTimes(false);
    HistoricalTimeSeriesInfoMetaDataResult test = _htsMaster.metaData(request);
    assertEquals(2, test.getDataFields().size());
    assertEquals("DF11", test.getDataFields().get(0));
    assertEquals("DF12", test.getDataFields().get(1));
    assertEquals(2, test.getDataSources().size());
    assertEquals("DS21", test.getDataSources().get(0));
    assertEquals("DS22", test.getDataSources().get(1));
    assertEquals(0, test.getDataProviders().size());
    assertEquals(0, test.getObservationTimes().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_htsMaster.getClass().getSimpleName() + "[DbHts]", _htsMaster.toString());
  }

}
