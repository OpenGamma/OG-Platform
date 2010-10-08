/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries.historicaldata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

import javax.time.Duration;
import javax.time.Instant;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.test.MockView;
import com.opengamma.engine.test.MockViewProcessor;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewComputationResultModelImpl;
import com.opengamma.financial.timeseries.db.DateTimeRowStoreTimeSeriesMaster;
import com.opengamma.financial.timeseries.historicaldata.IntradayComputationCacheImpl;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.msg.UserPrincipal;
import com.opengamma.util.test.DBTest;
import com.opengamma.util.test.Timeout;
import com.opengamma.util.timeseries.date.time.DateTimeDoubleTimeSeries;

/**
 * 
 */
public class IntradayComputationCacheImplTest extends DBTest {
  
  private IntradayComputationCacheImpl _intradayComputationCache;
  
  public IntradayComputationCacheImplTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);    
  }
  
  @Before
  public void setUp() throws Exception {
    super.setUp();
    
    ApplicationContext context = new FileSystemXmlApplicationContext("src/com/opengamma/financial/timeseries/db/tssQueries.xml");
    Map<String, String> namedSQLMap = (Map<String, String>) context.getBean("tssNamedSQLMap");
    
    DateTimeRowStoreTimeSeriesMaster timeSeriesMaster = new DateTimeRowStoreTimeSeriesMaster(
        getDbSource(),
        namedSQLMap,
        true);
    
    MockViewProcessor viewProcessor = new MockViewProcessor();
    MockView view = new MockView("MockView");
    view.setRunning(true);
    viewProcessor.addView(view);
    
    _intradayComputationCache = new IntradayComputationCacheImpl(
        viewProcessor,
        timeSeriesMaster, 
        UserPrincipal.getTestUser());
  }
  
  @Test
  public void addRemoveResolution() {
    
    assertEquals(0, _intradayComputationCache.getResolutions().size());
    _intradayComputationCache.addResolution(Duration.ofStandardMinutes(1), 60); 
    assertEquals(1, _intradayComputationCache.getResolutions().size());
    _intradayComputationCache.addResolution(Duration.ofStandardMinutes(2), 60); 
    assertEquals(2, _intradayComputationCache.getResolutions().size());
    _intradayComputationCache.addResolution(Duration.ofStandardMinutes(2), 70); // update
    assertEquals(2, _intradayComputationCache.getResolutions().size());
    
    _intradayComputationCache.addResolution(Duration.ofStandardMinutes(3), 60);
    assertEquals(3, _intradayComputationCache.getResolutions().size());
    _intradayComputationCache.removeResolution(Duration.ofStandardMinutes(3));
    assertEquals(2, _intradayComputationCache.getResolutions().size());
    
    assertEquals((Integer) 60, _intradayComputationCache.getResolutions().get(Duration.ofStandardMinutes(1)));
    assertEquals((Integer) 70, _intradayComputationCache.getResolutions().get(Duration.ofStandardMinutes(2)));
  }
  
  @Test
  public void getValue() throws Exception {
    assertFalse(_intradayComputationCache.isRunning());    
    
    ComputationTargetSpecification computationTarget = new ComputationTargetSpecification(
        ComputationTargetType.PRIMITIVE,
        UniqueIdentifier.of("foo", "bar"));
    ValueSpecification spec = new ValueSpecification(new ValueRequirement("FV", computationTarget), "1");
    
    ViewComputationResultModelImpl result = new ViewComputationResultModelImpl();
    result.setViewName("MockView");
    result.setCalculationConfigurationNames(Collections.singleton("Default"));
    
    result.setResultTimestamp(Instant.nowSystemClock());
    result.setValuationTime(Instant.nowSystemClock());
    
    result.addValue("Default", new ComputedValue(spec, 11.00));
    
    Duration resolution = Duration.ofMillis(50);
    
    try {
      _intradayComputationCache.getValue("MockView", "Default", spec, resolution);
      fail();
    } catch (IllegalArgumentException e) {
      // ok - resolution does not exist
    }
    
    _intradayComputationCache.addResolution(resolution, 3); // add
    _intradayComputationCache.addResolution(resolution, 5); // update
    
    _intradayComputationCache.start();
    assertTrue(_intradayComputationCache.isRunning());
    
    Thread.sleep(Timeout.standardTimeoutMillis()); // should not populate any points as no result available
    assertNull(_intradayComputationCache.getValue("MockView", "Default", spec, resolution));
    
    _intradayComputationCache.computationResultAvailable(result);
    
    Thread.sleep(Timeout.standardTimeoutMillis()); // should be sufficient to populate full result
    
    DateTimeDoubleTimeSeries timeSeries = _intradayComputationCache.getValue("MockView", "Default", spec, resolution);
    assertNotNull(timeSeries);
    
    assertEquals(5, timeSeries.size());
    for (Map.Entry<Date, Double> entry : timeSeries) {
      assertTrue(entry.getKey().getTime() >= result.getResultTimestamp().toEpochMillisLong()
          && entry.getKey().getTime() <= System.currentTimeMillis());
      assertEquals(11.00, entry.getValue(), 0.0001);      
    }
    
    _intradayComputationCache.stop();
    assertFalse(_intradayComputationCache.isRunning());
    
    result.setResultTimestamp(Instant.nowSystemClock().plusMillis(1));
    timeSeries = _intradayComputationCache.getValue("MockView", "Default", spec, resolution);
    assertNotNull(timeSeries);
    // since result timestamp is now > timestamp of last point in DB, we get a new "real-time" point at the end
    assertEquals(6, timeSeries.size()); 
    
    _intradayComputationCache.start();
    assertTrue(_intradayComputationCache.isRunning());
    
    // A long duration (100 seconds):
    _intradayComputationCache.addResolution(Duration.ofMillis(100000), 3); 
    _intradayComputationCache.addResolution(Duration.ofMillis(100000), 5);
    Thread.sleep(Timeout.standardTimeoutMillis());
    assertNotNull(_intradayComputationCache.getValue("MockView", "Default", spec, Duration.ofMillis(100000))); // first point should have been inserted immediately, not after 100 seconds
    
    // Try to get some non-existent histories
    assertNull(_intradayComputationCache.getValue("MockView2", "Default", spec, resolution));
    assertNull(_intradayComputationCache.getValue("MockView", "Default2", spec, resolution));
    assertNull(_intradayComputationCache.getValue("MockView", "Default",  new ValueSpecification(new ValueRequirement("FV2", computationTarget), "1"), resolution));
    
    _intradayComputationCache.stop(); // important to stop populating the db, otherwise clearing the tables will fail
  }

}
