/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.historicaldata;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.IdentifierBundleWithDates;
import com.opengamma.master.historicaldata.DataPointDocument;
import com.opengamma.master.historicaldata.HistoricalTimeSeriesDocument;
import com.opengamma.master.historicaldata.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaldata.impl.RandomTimeSeriesGenerator;
import com.opengamma.masterdb.historicaldata.LocalDateDbHistoricalTimeSeriesMaster;
import com.opengamma.util.test.DBTest;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * A performance test of time-series.
 */
@Test(enabled = false)
public class PerformanceTest extends DBTest {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(PerformanceTest.class);

  /**
   * The master.
   */
  private HistoricalTimeSeriesMaster _tsMaster;

  @Factory(dataProvider = "databasesMoreVersions", dataProviderClass = DBTest.class)
  public PerformanceTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
  }

  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();
    
    ApplicationContext context = new FileSystemXmlApplicationContext("src/com/opengamma/masterdb/timeseries/tssQueries.xml");
    
    @SuppressWarnings("unchecked")
    Map<String, String> namedSQLMap = (Map<String, String>) context.getBean("tssNamedSQLMap");
    
    HistoricalTimeSeriesMaster ts = new LocalDateDbHistoricalTimeSeriesMaster(
        getDbSource(), 
        namedSQLMap,
        false);
    _tsMaster = ts;
  }

  //-------------------------------------------------------------------------
  public void createUpdateReadLotsOfTimeSeries() {
    long start = System.nanoTime();
    
    int NUM_SERIES = 100;
    int NUM_POINTS = 100;
    
    for (int i = 0; i < NUM_SERIES; i++) {
      HistoricalTimeSeriesDocument tsDocument = new HistoricalTimeSeriesDocument();
      
      Identifier id1 = Identifier.of("sa" + i, "ida" + i);
      IdentifierBundle identifiers = IdentifierBundle.of(id1);
      LocalDateDoubleTimeSeries timeSeries = RandomTimeSeriesGenerator.makeRandomTimeSeries(1);
      
      tsDocument.setDataField("CLOSE");
      tsDocument.setDataProvider("CMPL");
      tsDocument.setDataSource("BLOOMBERG");
      tsDocument.setObservationTime("LDN_CLOSE");
      tsDocument.setIdentifiers(IdentifierBundleWithDates.of(identifiers));
      tsDocument.setTimeSeries(timeSeries);
      s_logger.debug("adding timeseries {}", tsDocument);
      _tsMaster.add(tsDocument);
      
      timeSeries = RandomTimeSeriesGenerator.makeRandomTimeSeries(NUM_POINTS);
      
      for (int j = 1; j < NUM_POINTS; j++) {
        DataPointDocument dataPointDocument = new DataPointDocument();
        dataPointDocument.setHistoricalTimeSeriesId(tsDocument.getUniqueId());
        dataPointDocument.setDate(timeSeries.getTime(j));
        dataPointDocument.setValue(timeSeries.getValueAt(j));
        s_logger.debug("adding data points {}", dataPointDocument);
        _tsMaster.addDataPoint(dataPointDocument);
      }
    }
    
    long end = System.nanoTime();
    
    s_logger.info("Creating {} series with {} points each took {} ms",
        new Object[] { NUM_SERIES, NUM_POINTS, (end - start) / 1E6 }); 
  }

}
