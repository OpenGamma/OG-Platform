/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.historicaltimeseries.impl;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.id.UniqueId;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleEntryIterator;
import com.opengamma.util.monitor.OperationTimer;

/**
 * 
 */
@Test(enabled=false)
public class RedisSimulationSeriesSourceTest {
  private static final Logger s_logger = LoggerFactory.getLogger(RedisSimulationSeriesSourceTest.class);
  private JedisPool _jedisPool;
  private String _redisPrefix;
  
  @BeforeClass
  public void launchJedisPool() {
    _jedisPool = new JedisPool("localhost");
    _redisPrefix = System.getProperty("user.name") + "_" + System.currentTimeMillis();
  }
  
  @AfterClass
  public void clearJedisPool() {
    if (_jedisPool == null) {
      return;
    }
    _jedisPool.destroy();
  }
  
  @BeforeMethod
  public void clearRedisDb() {
    Jedis jedis = _jedisPool.getResource();
    jedis.flushDB();
    _jedisPool.returnResource(jedis);
  }
  
  public void basicOperation() {
    LocalDate simulationSeriesDate = LocalDate.of(2013, 4, 24);
    RedisSimulationSeriesSource simulationSource = new RedisSimulationSeriesSource(_jedisPool, _redisPrefix);
    simulationSource.setCurrentSimulationExecutionDate(simulationSeriesDate);
    UniqueId id = generateId(5);
    HistoricalTimeSeries hts = null;
    
    hts = simulationSource.getHistoricalTimeSeries(id, null, false, null, false);
    assertNull(hts);
    
    for (int i = 1; i < 30; i++) {
      simulationSource.setTimeSeriesPoint(id, simulationSeriesDate, LocalDate.of(2013,4,i), i);
    }
    
    hts = simulationSource.getHistoricalTimeSeries(id, null, false, null, false);
    assertNotNull(hts);
    assertEquals(id, hts.getUniqueId());
    assertEquals(29, hts.getTimeSeries().size());
    LocalDateDoubleEntryIterator iterator = hts.getTimeSeries().iterator();
    int i = 1;
    while (iterator.hasNext()) {
      iterator.next();
      assertEquals(LocalDate.of(2013, 4, i), iterator.currentTime());
      assertEquals((double)i, iterator.currentValueFast(), 0.001);
      i++;
    }
    
    hts = simulationSource.getHistoricalTimeSeries(generateId(6), null, false, null, false);
    assertNull(hts);
    
    simulationSource.setCurrentSimulationExecutionDate(LocalDate.of(2013,4,25));
    hts = simulationSource.getHistoricalTimeSeries(id, null, false, null, false);
    assertNull(hts);
    
  }
  
  private static UniqueId generateId(int x) {
    return UniqueId.of("TEST", Integer.toString(x));
  }
  
  public void clearSpecificDate() {
    RedisSimulationSeriesSource simulationSource = new RedisSimulationSeriesSource(_jedisPool, _redisPrefix);
    LocalDate simulationSeriesDate = LocalDate.now();
    HistoricalTimeSeries hts = null;
    
    int numDaysHistory = 5;
    for (int i = 0; i < numDaysHistory; i++) {
      writeOneSimulationSeriesDate(simulationSource, simulationSeriesDate, 5);
      simulationSeriesDate = simulationSeriesDate.minusDays(1);
    }
    
    simulationSeriesDate = simulationSeriesDate.plusDays(1);
    simulationSource.setCurrentSimulationExecutionDate(simulationSeriesDate);
    hts = simulationSource.getHistoricalTimeSeries(generateId(3), null, false, null, false);
    assertNotNull(hts);

    simulationSource.clearExecutionDate(simulationSeriesDate);
    hts = simulationSource.getHistoricalTimeSeries(generateId(3), null, false, null, false);
    assertNull(hts);

    simulationSource.setCurrentSimulationExecutionDate(simulationSeriesDate.plusDays(1));
    hts = simulationSource.getHistoricalTimeSeries(generateId(3), null, false, null, false);
    assertNotNull(hts);
  }
  
  @Test(enabled=false)
  public void largePerformanceTest() {
    RedisSimulationSeriesSource simulationSource = new RedisSimulationSeriesSource(_jedisPool);
    LocalDate simulationSeriesDate = LocalDate.now();
    
    int numDaysHistory = 20;
    for (int i = 0; i < numDaysHistory; i++) {
      // 20 points, 10 curves
      writeOneSimulationSeriesDate(simulationSource, simulationSeriesDate, (20*10));
      simulationSeriesDate = simulationSeriesDate.minusDays(1);
    }
    
    OperationTimer timer = new OperationTimer(s_logger, "Loading TS");
    Random random = new Random();
    for (int i = 0; i < 1000; i++) {
      LocalDate seriesDate = simulationSeriesDate.minusDays(random.nextInt(numDaysHistory));
      UniqueId id = generateId(random.nextInt(1260));
      simulationSource.setCurrentSimulationExecutionDate(seriesDate);
      simulationSource.getHistoricalTimeSeries(id, null, false, null, false);
    }
    System.out.println("Loading 1000 TS took " + timer.finished());
  }

  private void writeOneSimulationSeriesDate(RedisSimulationSeriesSource simulationSource, LocalDate simulationSeriesDate, int nSeries) {
    OperationTimer timer = new OperationTimer(s_logger, "Storing many time series");
    
    // 20 points, 10 curves
    for (int i = 0; i < nSeries; i++) {
      performanceWriteOneSeries(simulationSource, generateId(i), simulationSeriesDate);
    }
    
    System.out.println("Writing " + nSeries + " series took " + timer.finished());
  }
  
  private static void performanceWriteOneSeries(RedisSimulationSeriesSource source, UniqueId id, LocalDate simulationSeriesDate) {
    LocalDate valueDate = LocalDate.now();
    for (int i = 1; i <= 1260; i++) {
      source.setTimeSeriesPoint(id, simulationSeriesDate, valueDate, i);
      valueDate = valueDate.minusDays(1);
    }
  }

}
