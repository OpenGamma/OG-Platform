/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bbg.loader.hts;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pair;

/**
 * Test {@link BloombergHistoricalTimeSeriesLoader}
 */
@Test(groups = TestGroup.UNIT_DB, singleThreaded = true)
public class BloombergHistoricalTimeSeriesLoaderTest extends AbstractHistoricalTimeSeriesDBTest {
  
  private static final Logger s_logger = LoggerFactory.getLogger(BloombergHistoricalTimeSeriesLoaderTest.class);
  
  /**
   * Creates an instance specifying the database to run.
   * @param databaseType  the database type
   * @param databaseVersion  the database version
   */
  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public BloombergHistoricalTimeSeriesLoaderTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.debug("running test for database = {}", databaseType);
  }

  //-------------------------------------------------------------------------
  public void updateTimeSeries() throws Exception {
    HistoricalTimeSeriesMaster htsMaster = getHtsMaster();
    BloombergHistoricalTimeSeriesLoader loader = getLoader();
    
    List<Pair<HistoricalTimeSeriesInfoDocument, HistoricalTimeSeries>> previousSeriesDocs = addTimeSeries();
    assertNotNull(previousSeriesDocs);
    assertFalse(previousSeriesDocs.isEmpty());
    HistoricalTimeSeries previousHts = htsMaster.getTimeSeries(previousSeriesDocs.get(0).getSecond().getUniqueId());
    previousHts.getUniqueId();
    assertTrue(loader.updateTimeSeries(previousHts.getUniqueId()));
    HistoricalTimeSeries updatedTS = htsMaster.getTimeSeries(previousHts.getUniqueId(), VersionCorrection.LATEST);
    assertTrue(!previousHts.getUniqueId().equals(updatedTS.getUniqueId()));
    assertTrue(!previousHts.getTimeSeries().equals(updatedTS.getTimeSeries()));
  }

  
}
