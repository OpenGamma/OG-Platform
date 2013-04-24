/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.historicaltimeseries.impl;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateToIntConverter;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ParallelArrayBinarySort;
import com.opengamma.util.tuple.Pair;

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
 * See <a href="http://jira.opengamma.com/browse/PLAT-3385">PLAT-3385</a> for the original
 * requirement.
 */
public class RedisSimulationSeriesSource implements HistoricalTimeSeriesSource {
  private static final Logger s_logger = LoggerFactory.getLogger(RedisSimulationSeriesSource.class);
  private final JedisPool _jedisPool;
  private LocalDate _currentSimulationExecutionDate = LocalDate.now();
  
  public RedisSimulationSeriesSource(JedisPool jedisPool) {
    ArgumentChecker.notNull(jedisPool, "jedisPool");
    _jedisPool = jedisPool;
  }

  /**
   * Gets the jedisPool.
   * @return the jedisPool
   */
  protected JedisPool getJedisPool() {
    return _jedisPool;
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
  
  public void setTimeSeriesPoint(UniqueId uniqueId, LocalDate simulationExecutionDate, LocalDate valueDate, double value) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(simulationExecutionDate, "simulationExecutionDate");
    ArgumentChecker.notNull(valueDate, "valueDate");
    String redisKey = toRedisKey(uniqueId, simulationExecutionDate);
    
    Jedis jedis = _jedisPool.getResource();
    try {
      jedis.hset(redisKey, valueDate.toString(), Double.toString(value));
    } catch (Exception e) {
      s_logger.error("Unable to set point on " + uniqueId + "_" + simulationExecutionDate, e);
      _jedisPool.returnBrokenResource(jedis);
      throw new OpenGammaRuntimeException("Unable to set point on " + uniqueId + "_" + simulationExecutionDate, e);
    } finally {
      _jedisPool.returnResource(jedis);
    }
  }
  
  // ------------------------------------------------------------------------
  // SUPPORTED HISTORICAL TIME SERIES SOURCE OPERATIONS:
  // ------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(UniqueId uniqueId, LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    // This is the only method that needs implementation.
    
    // We ignore all parameters except for the UniqueId.
    String redisKey = toRedisKey(uniqueId, getCurrentSimulationExecutionDate());
    Map<String, String> valuesFromRedis = null;
    Jedis jedis = _jedisPool.getResource();
    try {
      valuesFromRedis = jedis.hgetAll(redisKey);
    } catch (Exception e) {
      s_logger.error("Unable to load points from redis for " + uniqueId, e);
      _jedisPool.returnBrokenResource(jedis);
      throw new OpenGammaRuntimeException("Unable to load points from redis for " + uniqueId, e);
    } finally {
      _jedisPool.returnResource(jedis);
    }
    
    return composeFromRedisValues(uniqueId, valuesFromRedis);
  }

  private static HistoricalTimeSeries composeFromRedisValues(UniqueId uniqueId, Map<String, String> valuesFromRedis) {
    
    int[] dates = new int[valuesFromRedis.size()];
    double[] values = new double[valuesFromRedis.size()];
    
    int i = 0;
    for (Map.Entry<String, String> entry : valuesFromRedis.entrySet()) {
      dates[i] = LocalDateToIntConverter.convertToInt(LocalDate.parse(entry.getKey()));
      values[i] = Double.parseDouble(entry.getValue());
      i++;
    }
    
    ParallelArrayBinarySort.parallelBinarySort(dates, values);
    
    LocalDateDoubleTimeSeries ts = ImmutableLocalDateDoubleTimeSeries.of(dates, values);
    HistoricalTimeSeries hts = new SimpleHistoricalTimeSeries(uniqueId, ts);
    return hts;
  }

  // ------------------------------------------------------------------------
  // UTILITY METHODS:
  // ------------------------------------------------------------------------
  
  private static String toRedisKey(UniqueId uniqueId, LocalDate simulationExecutionDate) {
    StringBuilder sb = new StringBuilder();
    
    sb.append(uniqueId);
    sb.append('*');
    sb.append(simulationExecutionDate.toString());
    
    return sb.toString();
  }

  // ------------------------------------------------------------------------
  // UNSUPPORTED OPERATIONS:
  // ------------------------------------------------------------------------
  @Override
  public ChangeManager changeManager() {
    throw new UnsupportedOperationException("Unsupported operation.");
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(UniqueId uniqueId) {
    throw new UnsupportedOperationException("Unsupported operation.");
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(UniqueId uniqueId, LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd, int maxPoints) {
    throw new UnsupportedOperationException("Unsupported operation.");
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(UniqueId uniqueId) {
    return null;
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(UniqueId uniqueId, LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    throw new UnsupportedOperationException("Unsupported operation.");
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(ExternalIdBundle identifierBundle, String dataSource, String dataProvider, String dataField) {
    throw new UnsupportedOperationException("Unsupported operation.");
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(ExternalIdBundle identifierBundle, String dataSource, String dataProvider, String dataField, LocalDate start, boolean includeStart,
      LocalDate end, boolean includeEnd) {
    throw new UnsupportedOperationException("Unsupported operation.");
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(ExternalIdBundle identifierBundle, String dataSource, String dataProvider, String dataField, LocalDate start, boolean includeStart,
      LocalDate end, boolean includeEnd, int maxPoints) {
    throw new UnsupportedOperationException("Unsupported operation.");
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField) {
    throw new UnsupportedOperationException("Unsupported operation.");
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField, LocalDate start,
      boolean includeStart, LocalDate end, boolean includeEnd) {
    throw new UnsupportedOperationException("Unsupported operation.");
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField, LocalDate start,
      boolean includeStart, LocalDate end, boolean includeEnd, int maxPoints) {
    throw new UnsupportedOperationException("Unsupported operation.");
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField) {
    throw new UnsupportedOperationException("Unsupported operation.");
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField, LocalDate start,
      boolean includeStart, LocalDate end, boolean includeEnd) {
    throw new UnsupportedOperationException("Unsupported operation.");
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(ExternalIdBundle identifierBundle, String dataSource, String dataProvider, String dataField) {
    throw new UnsupportedOperationException("Unsupported operation.");
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(ExternalIdBundle identifierBundle, String dataSource, String dataProvider, String dataField, LocalDate start, boolean includeStart, LocalDate end,
      boolean includeEnd) {
    throw new UnsupportedOperationException("Unsupported operation.");
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(String dataField, ExternalIdBundle identifierBundle, String resolutionKey) {
    throw new UnsupportedOperationException("Unsupported operation.");
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(String dataField, ExternalIdBundle identifierBundle, String resolutionKey, LocalDate start, boolean includeStart, LocalDate end,
      boolean includeEnd) {
    throw new UnsupportedOperationException("Unsupported operation.");
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(String dataField, ExternalIdBundle identifierBundle, String resolutionKey, LocalDate start, boolean includeStart, LocalDate end,
      boolean includeEnd, int maxPoints) {
    throw new UnsupportedOperationException("Unsupported operation.");
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(String dataField, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey) {
    throw new UnsupportedOperationException("Unsupported operation.");
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(String dataField, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey, LocalDate start,
      boolean includeStart, LocalDate end, boolean includeEnd) {
    throw new UnsupportedOperationException("Unsupported operation.");
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(String dataField, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey, LocalDate start,
      boolean includeStart, LocalDate end, boolean includeEnd, int maxPoints) {
    throw new UnsupportedOperationException("Unsupported operation.");
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(String dataField, ExternalIdBundle identifierBundle, String resolutionKey) {
    throw new UnsupportedOperationException("Unsupported operation.");
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(
      String dataField, ExternalIdBundle identifierBundle, String resolutionKey,
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    throw new UnsupportedOperationException("Unsupported operation.");
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(String dataField, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey) {
    throw new UnsupportedOperationException("Unsupported operation.");
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(String dataField, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey, LocalDate start, boolean includeStart,
      LocalDate end, boolean includeEnd) {
    throw new UnsupportedOperationException("Unsupported operation.");
  }

  @Override
  public Map<ExternalIdBundle, HistoricalTimeSeries> getHistoricalTimeSeries(Set<ExternalIdBundle> identifierSet, String dataSource, String dataProvider, String dataField, LocalDate start,
      boolean includeStart, LocalDate end, boolean includeEnd) {
    throw new UnsupportedOperationException("Unsupported operation.");
  }

  @Override
  public ExternalIdBundle getExternalIdBundle(UniqueId uniqueId) {
    throw new UnsupportedOperationException("Unsupported operation.");
  }

}
