/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.historicaltimeseries;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdBundleWithDates;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.master.historicaltimeseries.impl.RandomTimeSeriesGenerator;
import com.opengamma.masterdb.DbMasterTestUtils;
import com.opengamma.util.test.DBTest;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * A performance test of time-series.
 */
@Test(enabled = false)
public class PerformanceTest extends DBTest {

  private static final Logger s_logger = LoggerFactory.getLogger(PerformanceTest.class);

  private DbHistoricalTimeSeriesMaster _htsMaster;

  @Factory(dataProvider = "databases", dataProviderClass = DBTest.class)
  public PerformanceTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
  }

  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();
    ConfigurableApplicationContext context = DbMasterTestUtils.getContext(getDatabaseType());
    _htsMaster = (DbHistoricalTimeSeriesMaster) context.getBean(getDatabaseType() + "DbHistoricalTimeSeriesMaster");
  }

  @AfterMethod
  public void tearDown() throws Exception {
    super.tearDown();
    _htsMaster = null;
  }

  //-------------------------------------------------------------------------
  public void createUpdateReadLotsOfTimeSeries() {
    long start = System.nanoTime();
    
    int NUM_SERIES = 100;
    int NUM_POINTS = 100;
    
    for (int i = 0; i < NUM_SERIES; i++) {
      ExternalId id1 = ExternalId.of("sa" + i, "ida" + i);
      ExternalIdBundle identifiers = ExternalIdBundle.of(id1);
      
      ManageableHistoricalTimeSeriesInfo info = new ManageableHistoricalTimeSeriesInfo();
      info.setName("BLOOMBERG CMPL");
      info.setDataField("CLOSE");
      info.setDataProvider("CMPL");
      info.setDataSource("BLOOMBERG");
      info.setObservationTime("LDN_CLOSE");
      info.setExternalIdBundle(ExternalIdBundleWithDates.of(identifiers));
      HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument(info);
      s_logger.debug("adding timeseries {}", doc);
      doc = _htsMaster.add(doc);
      
      LocalDateDoubleTimeSeries randomPoints = RandomTimeSeriesGenerator.makeRandomTimeSeries(1);
      _htsMaster.updateTimeSeriesDataPoints(doc.getInfo().getTimeSeriesObjectId(), randomPoints);
      randomPoints = RandomTimeSeriesGenerator.makeRandomTimeSeries(NUM_POINTS);
      
      for (int j = 1; j < NUM_POINTS; j++) {
        ArrayLocalDateDoubleTimeSeries points = new ArrayLocalDateDoubleTimeSeries(
            Lists.newArrayList(randomPoints.getTimeAt(j)),
            Lists.newArrayList(randomPoints.getValueAt(j)));
        s_logger.debug("adding data points {}", points);
        _htsMaster.updateTimeSeriesDataPoints(doc.getInfo().getTimeSeriesObjectId(), points);
      }
    }
    
    long end = System.nanoTime();
    
    s_logger.info("Creating {} series with {} points each took {} ms",
        new Object[] { NUM_SERIES, NUM_POINTS, (end - start) / 1E6 }); 
  }

}
