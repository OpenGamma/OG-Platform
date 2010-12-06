/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.historicaldata;

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
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.test.MockView;
import com.opengamma.engine.test.MockViewProcessor;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewComputationResultModelImpl;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.masterdb.timeseries.DateTimeDbTimeSeriesMaster;
import com.opengamma.util.test.DBTest;
import com.opengamma.util.timeseries.date.time.DateTimeDoubleTimeSeries;

/**
 * Test IntradayComputationCacheImpl.
 */
public class IntradayComputationCacheImplTest extends DBTest {

  private IntradayComputationCacheImpl _intradayComputationCache;

  public IntradayComputationCacheImplTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);    
  }
  
  @Before
  public void setUp() throws Exception {
    super.setUp();
    
    ApplicationContext context = new ClassPathXmlApplicationContext("com/opengamma/masterdb/timeseries/tssQueries.xml");
    @SuppressWarnings("unchecked")
    Map<String, String> namedSQLMap = (Map<String, String>) context.getBean("tssNamedSQLMap");
    
    DateTimeDbTimeSeriesMaster timeSeriesMaster = new DateTimeDbTimeSeriesMaster(
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
  public void startStop() {
    Duration resolution = Duration.ofMillis(100);
    _intradayComputationCache.addResolution(resolution, 2);

    _intradayComputationCache.start();
    assertTrue(_intradayComputationCache.isRunning());
    _intradayComputationCache.stop();
    assertFalse(_intradayComputationCache.isRunning());
  }
  
  @Test
  public void getValue() throws Exception {
    Instant now = Instant.now();
    
    assertFalse(_intradayComputationCache.isRunning());    
    
    ComputationTargetSpecification computationTarget = new ComputationTargetSpecification(
        ComputationTargetType.PRIMITIVE,
        UniqueIdentifier.of("foo", "bar"));
    ValueSpecification spec = new ValueSpecification(new ValueRequirement("FV", computationTarget), "1");
    
    ViewComputationResultModelImpl result = new ViewComputationResultModelImpl();
    result.setViewName("MockView");
    result.setCalculationConfigurationNames(Collections.singleton("Default"));
    
    result.setResultTimestamp(Instant.now());
    result.setValuationTime(Instant.now());
    
    result.addValue("Default", new ComputedValue(spec, 11.00));
    
    Duration resolution = Duration.ofMillis(100);
    
    try {
      _intradayComputationCache.getValue("MockView", "Default", spec, resolution);
      fail();
    } catch (IllegalArgumentException e) {
      // ok - resolution does not exist
    }
    
    _intradayComputationCache.addResolution(resolution, 2); // add
    _intradayComputationCache.addResolution(resolution, 3); // update
    
    _intradayComputationCache.save(resolution, now); // should not populate any points as no result available
    assertNull(_intradayComputationCache.getValue("MockView", "Default", spec, resolution));
    
    _intradayComputationCache.computationResultAvailable(result);
    
    _intradayComputationCache.save(resolution, now); 
    _intradayComputationCache.save(resolution, now.plusMillis(99)); // approx 100 millis, but a little bit of clock wobbliness 
    _intradayComputationCache.save(resolution, now.plusMillis(201));
    _intradayComputationCache.save(resolution, now.plusMillis(300));
    _intradayComputationCache.save(resolution, now.plusMillis(402));
    _intradayComputationCache.save(resolution, now.plusMillis(498));
    
    DateTimeDoubleTimeSeries timeSeries = _intradayComputationCache.getValue("MockView", "Default", spec, resolution);
    assertNotNull(timeSeries);
    
    assertEquals(timeSeries.toString(), 3, timeSeries.size());
    for (Map.Entry<Date, Double> entry : timeSeries) {
      assertTrue(entry.getKey().getTime() >= now.toEpochMillisLong()
          && entry.getKey().getTime() <= now.plusMillis(498).toEpochMillisLong());
      assertEquals(11.00, entry.getValue(), 0.0001);      
    }
    
    result.setResultTimestamp(now.plusMillis(499));
    timeSeries = _intradayComputationCache.getValue("MockView", "Default", spec, resolution);
    assertNotNull(timeSeries);
    // since result timestamp is now > timestamp of last point in DB, we get a new "real-time" point at the end
    assertEquals(4, timeSeries.size()); 
    
    // A long duration (100 seconds):
    _intradayComputationCache.addResolution(Duration.ofMillis(100000), 2); 
    _intradayComputationCache.addResolution(Duration.ofMillis(100000), 3);
    
    _intradayComputationCache.save(Duration.ofMillis(100000), now); 
    
    assertNotNull(_intradayComputationCache.getValue("MockView", "Default", spec, Duration.ofMillis(100000))); // first point should have been inserted immediately, not after 100 seconds
    
    // Try to get some non-existent histories
    assertNull(_intradayComputationCache.getValue("MockView2", "Default", spec, resolution));
    assertNull(_intradayComputationCache.getValue("MockView", "Default2", spec, resolution));
    assertNull(_intradayComputationCache.getValue("MockView", "Default",  new ValueSpecification(new ValueRequirement("FV2", computationTarget), "1"), resolution));
  }

}
