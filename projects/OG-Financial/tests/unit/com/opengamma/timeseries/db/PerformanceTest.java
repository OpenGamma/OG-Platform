/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries.db;

import java.util.Map;

import javax.time.calendar.LocalDate;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.opengamma.financial.timeseries.DataPointDocument;
import com.opengamma.financial.timeseries.TimeSeriesDocument;
import com.opengamma.financial.timeseries.TimeSeriesMaster;
import com.opengamma.financial.timeseries.db.LocalDateRowStoreTimeSeriesMaster;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.util.test.DBTest;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * 
 */
@Ignore
public class PerformanceTest extends DBTest {
  
  private static final Logger s_logger = LoggerFactory.getLogger(PerformanceTest.class);
  
  private TimeSeriesMaster<LocalDate> _tsMaster;
  
  public PerformanceTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
  }
  
  @Before
  public void setUp() throws Exception {
    super.setUp();
    
    ApplicationContext context = new FileSystemXmlApplicationContext("src/com/opengamma/financial/timeseries/db/tssQueries.xml");
    Map<String, String> namedSQLMap = (Map<String, String>) context.getBean("tssNamedSQLMap");
    
    TimeSeriesMaster<LocalDate> ts = new LocalDateRowStoreTimeSeriesMaster(
        getTransactionManager(), 
        namedSQLMap,
        false);
    _tsMaster = ts;
  }
  
  @Test
  public void createUpdateReadLotsOfTimeSeries() {
    long start = System.nanoTime();
    
    int NUM_SERIES = 100;
    int NUM_POINTS = 100;
    
    for (int i = 0; i < NUM_SERIES; i++) {
      TimeSeriesDocument<LocalDate> tsDocument = new TimeSeriesDocument<LocalDate>();
      
      Identifier id1 = Identifier.of("sa" + i, "ida" + i);
      IdentifierBundle identifiers = IdentifierBundle.of(id1);
      LocalDateDoubleTimeSeries timeSeries = TimeSeriesMasterTest.makeRandomTimeSeriesStatic(1);
      
      tsDocument.setDataField("CLOSE");
      tsDocument.setDataProvider("CMPL");
      tsDocument.setDataSource("BLOOMBERG");
      tsDocument.setObservationTime("LDN_CLOSE");
      tsDocument.setIdentifiers(identifiers);
      tsDocument.setTimeSeries(timeSeries);
      s_logger.debug("adding timeseries {}", tsDocument);
      _tsMaster.addTimeSeries(tsDocument);
      
      timeSeries = TimeSeriesMasterTest.makeRandomTimeSeriesStatic(NUM_POINTS);
      
      for (int j = 1; j < NUM_POINTS; j++) {
        DataPointDocument<LocalDate> dataPointDocument = new DataPointDocument<LocalDate>();
        dataPointDocument.setTimeSeriesId(tsDocument.getUniqueIdentifier());
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
