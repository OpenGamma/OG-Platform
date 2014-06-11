/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.historicaltimeseries.impl;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;

/**
 * An extremely minimal and lightweight {@code HistoricalTimeSeriesSource} that pulls data
 * directly from Redis for the purpose of historical simulations, where the full market data
 * series for that simulation can change every day..
 * This is <em>only</em> appropriate for use in conjunction with {@code HistoricalTimeSeriesFunction}
 * and requires its own specific API for publishing data. It is <strong>not</strong>
 * a general purpose component.
 * <p/>
 * Effectively, there is a double-time series involved:
 * <ul>
 *   <li>The {@code SimulationExecution} series is one time series, representing the date
 *       that the simulation series was performed (and/or expected to be used).</li>
 *   <li>The {@code Value} series is one point inside a particular simulation.</li>
 * </ul>
 * So, for example, assume that every day a system generates a whole new time series,
 * where that time series is the simulation points that should be run. In that case,
 * this class may be appropriate.
 * <p/>
 * The following constraints must hold for this Source to be of any utility whatsoever:
 * <ul>
 *   <li>Historical lookups are not required. Because they are not supported.</li>
 *   <li>Version corrections are not required. Because they are not supported.</li>
 *   <li>Each time series has a <b>single</b> {@link ExternalId} which then acts
 *       as the {@link UniqueId} internally.</li>
 *   <li>Each external ID has a single time series (thus there is not the capacity to store
 *       different Data Source, Data Provider, Observation Time, Data Field series).</li>
 * </ul>
 * <p/>
 * Where a method is not supported semantically, an {@link UnsupportedOperationException}
 * will be thrown. Where use indicates that this class may be being used incorrectly,
 * a log message will be written at {@code WARN} level.
 * <p/>
 * See <a href="http://jira.opengamma.com/browse/PLAT-3385">PLAT-3385</a> for the original
 * requirement.
 */
public class RedisSimulationSeriesSource extends NonVersionedRedisHistoricalTimeSeriesSource implements SimulationSeriesSource {
  private static final Logger s_logger = LoggerFactory.getLogger(RedisSimulationSeriesSource.class);
  private LocalDate _currentSimulationExecutionDate = LocalDate.now();

  public RedisSimulationSeriesSource(JedisPool jedisPool) {
    this(jedisPool, "");
  }
  
  public RedisSimulationSeriesSource(JedisPool jedisPool, String redisPrefix) {
    super(jedisPool, redisPrefix, "RedisSimulationSeriesSource");
  }

  public RedisSimulationSeriesSource withSimulationDate(LocalDate date) {
    RedisSimulationSeriesSource redisSimulationSeriesSource = new RedisSimulationSeriesSource(getJedisPool(), getRedisPrefix());
    redisSimulationSeriesSource.setCurrentSimulationExecutionDate(date);
    return redisSimulationSeriesSource;
  }

  /**
   * Gets the currentSimulationExecutionDate.
   * @return the currentSimulationExecutionDate
   */
  public LocalDate getCurrentSimulationExecutionDate() {
    return _currentSimulationExecutionDate;
  }

  /**
   * Sets the currentSimulationExecutionDate.
   * This will be used in calls to load the simulation series.
   * @param currentSimulationExecutionDate  the currentSimulationExecutionDate
   */
  public void setCurrentSimulationExecutionDate(LocalDate currentSimulationExecutionDate) {
    _currentSimulationExecutionDate = currentSimulationExecutionDate;
  }

  // ------------------------------------------------------------------------
  // REDIS MANIPULATION OPERATIONS:
  // ------------------------------------------------------------------------
  
  public void updateTimeSeriesPoint(UniqueId uniqueId, LocalDate simulationExecutionDate, LocalDate valueDate, double value) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(simulationExecutionDate, "simulationExecutionDate");
    ArgumentChecker.notNull(valueDate, "valueDate");
    
    String redisKey = toRedisKey(uniqueId, simulationExecutionDate);
    updateTimeSeriesPoint(redisKey, valueDate, value);
  }
  
  public void updateTimeSeries(UniqueId uniqueId, LocalDate simulationExecutionDate, LocalDateDoubleTimeSeries timeseries) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(simulationExecutionDate, "simulationExecutionDate");
    ArgumentChecker.notNull(timeseries, "timeseries");
    
    String redisKey = toRedisKey(uniqueId, simulationExecutionDate);
    updateTimeSeries(redisKey, timeseries, false);
  }
  
  public void replaceTimeSeries(UniqueId uniqueId, LocalDate simulationExecutionDate, LocalDateDoubleTimeSeries timeSeries) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(timeSeries, "timeSeries");
    
    String redisKey = toRedisKey(uniqueId, simulationExecutionDate);
    updateTimeSeries(redisKey, timeSeries, true);
  }
  
  public void clearExecutionDate(LocalDate simulationExecutionDate) {
    final String keysPattern = getRedisPrefix() + "*_" + simulationExecutionDate.toString();
    Jedis jedis = getJedisPool().getResource();
    try {
      Set<String> keys = jedis.keys(keysPattern);
      if (!keys.isEmpty()) {
        jedis.del(keys.toArray(new String[0]));
      }
      getJedisPool().returnResource(jedis);
    } catch (Exception e) {
      s_logger.error("Unable to clear execution date " + simulationExecutionDate, e);
      getJedisPool().returnBrokenResource(jedis);
      throw new OpenGammaRuntimeException("Unable to clear execution date " + simulationExecutionDate, e);
    }
  }

  @Override
  protected String toRedisKey(UniqueId uniqueId) {
    return toRedisKey(uniqueId, getCurrentSimulationExecutionDate());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    RedisSimulationSeriesSource that = (RedisSimulationSeriesSource) o;
    return _currentSimulationExecutionDate.equals(that._currentSimulationExecutionDate);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    return 31 * result + _currentSimulationExecutionDate.hashCode();
  }
}
