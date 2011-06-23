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
import com.opengamma.master.historicaldata.ManageableHistoricalTimeSeries;
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
    
    ApplicationContext context = new FileSystemXmlApplicationContext("src/com/opengamma/masterdb/historicaldata/tssQueries.xml");
    
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
      Identifier id1 = Identifier.of("sa" + i, "ida" + i);
      IdentifierBundle identifiers = IdentifierBundle.of(id1);
      LocalDateDoubleTimeSeries timeSeries = RandomTimeSeriesGenerator.makeRandomTimeSeries(1);
      
      ManageableHistoricalTimeSeries series = new ManageableHistoricalTimeSeries();
      series.setDataField("CLOSE");
      series.setDataProvider("CMPL");
      series.setDataSource("BLOOMBERG");
      series.setObservationTime("LDN_CLOSE");
      series.setIdentifiers(IdentifierBundleWithDates.of(identifiers));
      series.setTimeSeries(timeSeries);
      HistoricalTimeSeriesDocument doc = new HistoricalTimeSeriesDocument(series);
      s_logger.debug("adding timeseries {}", doc);
      _tsMaster.add(doc);
      
      timeSeries = RandomTimeSeriesGenerator.makeRandomTimeSeries(NUM_POINTS);
      
      for (int j = 1; j < NUM_POINTS; j++) {
        DataPointDocument dataPointDocument = new DataPointDocument();
        dataPointDocument.setHistoricalTimeSeriesId(doc.getUniqueId());
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
