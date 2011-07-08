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
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.IdentifierBundleWithDates;
import com.opengamma.master.historicaldata.impl.RandomTimeSeriesGenerator;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesDocument;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeries;
import com.opengamma.masterdb.DbMasterTestUtils;
import com.opengamma.util.test.DBTest;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * A performance test of time-series.
 */
@Test
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
      Identifier id1 = Identifier.of("sa" + i, "ida" + i);
      IdentifierBundle identifiers = IdentifierBundle.of(id1);
      LocalDateDoubleTimeSeries randomPoints = RandomTimeSeriesGenerator.makeRandomTimeSeries(1);
      
      ManageableHistoricalTimeSeries series = new ManageableHistoricalTimeSeries();
      series.setName("BLOOMBERG CMPL");
      series.setDataField("CLOSE");
      series.setDataProvider("CMPL");
      series.setDataSource("BLOOMBERG");
      series.setObservationTime("LDN_CLOSE");
      series.setIdentifiers(IdentifierBundleWithDates.of(identifiers));
      series.setTimeSeries(randomPoints);
      HistoricalTimeSeriesDocument doc = new HistoricalTimeSeriesDocument(series);
      s_logger.debug("adding timeseries {}", doc);
      _htsMaster.add(doc);
      
      randomPoints = RandomTimeSeriesGenerator.makeRandomTimeSeries(NUM_POINTS);
      
      for (int j = 1; j < NUM_POINTS; j++) {
        ArrayLocalDateDoubleTimeSeries points = new ArrayLocalDateDoubleTimeSeries(
            Lists.newArrayList(randomPoints.getTime(j)),
            Lists.newArrayList(randomPoints.getValueAt(j)));
        s_logger.debug("adding data points {}", points);
        _htsMaster.updateDataPoints(doc.getUniqueId(), points);
      }
    }
    
    long end = System.nanoTime();
    
    s_logger.info("Creating {} series with {} points each took {} ms",
        new Object[] { NUM_SERIES, NUM_POINTS, (end - start) / 1E6 }); 
  }

}
