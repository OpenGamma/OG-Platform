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

import com.codahale.metrics.Timer;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateToIntConverter;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ParallelArrayBinarySort;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public abstract class AbstractRedisHistoricalTimeSeriesSource implements HistoricalTimeSeriesSource {
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractRedisHistoricalTimeSeriesSource.class);
  private final JedisPool _jedisPool;
  private final String _redisPrefix;
  
  protected AbstractRedisHistoricalTimeSeriesSource(JedisPool jedisPool, String redisPrefix) {
    ArgumentChecker.notNull(jedisPool, "jedisPool");
    ArgumentChecker.notNull(redisPrefix, "redisPrefix");
    _jedisPool = jedisPool;
    _redisPrefix = redisPrefix;
  }

  /**
   * Gets the jedisPool.
   * @return the jedisPool
   */
  protected JedisPool getJedisPool() {
    return _jedisPool;
  }

  /**
   * Gets the redisPrefix.
   * @return the redisPrefix
   */
  protected String getRedisPrefix() {
    return _redisPrefix;
  }

  protected void setTimeSeriesPoint(String redisKey, LocalDate valueDate, double value) {
    ArgumentChecker.notNull(redisKey, "redisKey");
    ArgumentChecker.notNull(valueDate, "valueDate");
    
    Jedis jedis = getJedisPool().getResource();
    try {
      jedis.hset(redisKey, valueDate.toString(), Double.toString(value));
      getJedisPool().returnResource(jedis);
    } catch (Exception e) {
      s_logger.error("Unable to set point on " + redisKey, e);
      getJedisPool().returnBrokenResource(jedis);
      throw new OpenGammaRuntimeException("Unable to set point on " + redisKey, e);
    }
  }
  
  /**
   * Completely empty the underlying Redis server.
   * You should only call this if you really know what you're doing.
   */
  public void completelyClearRedis() {
    Jedis jedis = getJedisPool().getResource();
    try {
      jedis.flushDB();
      getJedisPool().returnResource(jedis);
    } catch (Exception e) {
      s_logger.error("Unable to clear database", e);
      getJedisPool().returnBrokenResource(jedis);
      throw new OpenGammaRuntimeException("Unable to clear database", e);
    }
  }
  
  protected static LocalDateDoubleTimeSeries composeFromRedisValues(Map<String, String> valuesFromRedis) {
    
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
    return ts;
  }

  protected String toRedisKey(UniqueId uniqueId) {
    return toRedisKey(uniqueId, null);
  }
  
  protected String toRedisKey(UniqueId uniqueId, LocalDate simulationExecutionDate) {
    StringBuilder sb = new StringBuilder();
    
    sb.append(getRedisPrefix());
    sb.append('_');
    sb.append(uniqueId);
    if (simulationExecutionDate != null) {
      sb.append('_');
      sb.append(simulationExecutionDate.toString());
    }
    
    return sb.toString();
  }
  
  protected Map<String, String> loadValuesFromRedis(UniqueId uniqueId) {
    String redisKey = toRedisKey(uniqueId);
    Map<String, String> valuesFromRedis = null;
    Jedis jedis = getJedisPool().getResource();
    try {
      valuesFromRedis = jedis.hgetAll(redisKey);
      getJedisPool().returnResource(jedis);
    } catch (Exception e) {
      s_logger.error("Unable to load points from redis for " + uniqueId, e);
      getJedisPool().returnBrokenResource(jedis);
      throw new OpenGammaRuntimeException("Unable to load points from redis for " + uniqueId, e);
    }
    return valuesFromRedis;
  }
  
  // ------------------------------------------------------------------------
  // SUPPORTED HISTORICAL TIME SERIES SOURCE OPERATIONS:
  // ------------------------------------------------------------------------
  
  protected abstract Timer getSeriesLoadTimer();
  
  protected LocalDateDoubleTimeSeries loadTimeSeriesFromRedis(UniqueId uniqueId) {
    // This is the only method that needs implementation.
    
    try (Timer.Context context = getSeriesLoadTimer().time()) {
      Map<String, String> valuesFromRedis = loadValuesFromRedis(uniqueId);
      
      if ((valuesFromRedis == null) || valuesFromRedis.isEmpty()) {
        return null;
      }
      
      LocalDateDoubleTimeSeries ts = composeFromRedisValues(valuesFromRedis);
      return ts;
    }
    
  }

  public HistoricalTimeSeries getHistoricalTimeSeries(UniqueId uniqueId, LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    LocalDateDoubleTimeSeries ts = loadTimeSeriesFromRedis(uniqueId);
    if (ts == null) {
      return null;
    }
    
    if (start != null) {
      ArgumentChecker.notNull(end, "end");
      ts = ts.subSeries(start, includeStart, end, includeEnd);
    }
    return new SimpleHistoricalTimeSeries(uniqueId, ts);
  }

  public HistoricalTimeSeries getHistoricalTimeSeries(String dataField, ExternalIdBundle identifierBundle, String resolutionKey, LocalDate start, boolean includeStart, LocalDate end,
                                                      boolean includeEnd, int maxPoints) {
    if (identifierBundle.isEmpty()) {
      return null;
    }
    final ExternalId id = identifierBundle.iterator().next();
    final UniqueId uniqueId = UniqueId.of(id.getScheme().getName(), id.getValue());
    return getHistoricalTimeSeries(uniqueId, start, includeStart, end, includeEnd);
  }

  public HistoricalTimeSeries getHistoricalTimeSeries(UniqueId uniqueId) {
    LocalDateDoubleTimeSeries ts = loadTimeSeriesFromRedis(uniqueId);
    if (ts == null) {
      return null;
    } else {
      return new SimpleHistoricalTimeSeries(uniqueId, ts);
    }
  }
  
  public ExternalIdBundle getExternalIdBundle(UniqueId uniqueId) {
    return ExternalId.of(uniqueId.getScheme(), uniqueId.getValue()).toBundle();
  }

  // ------------------------------------------------------------------------
  // UNSUPPORTED OPERATIONS:
  // ------------------------------------------------------------------------
  public ChangeManager changeManager() {
    throw new UnsupportedOperationException("Unsupported operation.");
  }

  public HistoricalTimeSeries getHistoricalTimeSeries(UniqueId uniqueId, LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd, int maxPoints) {
    throw new UnsupportedOperationException("Unsupported operation.");
  }

  public Pair<LocalDate, Double> getLatestDataPoint(UniqueId uniqueId) {
    return null;
  }

  public Pair<LocalDate, Double> getLatestDataPoint(UniqueId uniqueId, LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    throw new UnsupportedOperationException("Unsupported operation.");
  }

  public HistoricalTimeSeries getHistoricalTimeSeries(ExternalIdBundle identifierBundle, String dataSource, String dataProvider, String dataField) {
    throw new UnsupportedOperationException("Unsupported operation.");
  }

  public HistoricalTimeSeries getHistoricalTimeSeries(ExternalIdBundle identifierBundle, String dataSource, String dataProvider, String dataField, LocalDate start, boolean includeStart,
      LocalDate end, boolean includeEnd) {
    throw new UnsupportedOperationException("Unsupported operation.");
  }

  public HistoricalTimeSeries getHistoricalTimeSeries(ExternalIdBundle identifierBundle, String dataSource, String dataProvider, String dataField, LocalDate start, boolean includeStart,
      LocalDate end, boolean includeEnd, int maxPoints) {
    throw new UnsupportedOperationException("Unsupported operation.");
  }

  public HistoricalTimeSeries getHistoricalTimeSeries(ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField) {
    throw new UnsupportedOperationException("Unsupported operation.");
  }

  public HistoricalTimeSeries getHistoricalTimeSeries(ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField, LocalDate start,
      boolean includeStart, LocalDate end, boolean includeEnd) {
    throw new UnsupportedOperationException("Unsupported operation.");
  }

  public HistoricalTimeSeries getHistoricalTimeSeries(ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField, LocalDate start,
      boolean includeStart, LocalDate end, boolean includeEnd, int maxPoints) {
    throw new UnsupportedOperationException("Unsupported operation.");
  }

  public Pair<LocalDate, Double> getLatestDataPoint(ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField) {
    throw new UnsupportedOperationException("Unsupported operation.");
  }

  public Pair<LocalDate, Double> getLatestDataPoint(ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField, LocalDate start,
      boolean includeStart, LocalDate end, boolean includeEnd) {
    throw new UnsupportedOperationException("Unsupported operation.");
  }

  public Pair<LocalDate, Double> getLatestDataPoint(ExternalIdBundle identifierBundle, String dataSource, String dataProvider, String dataField) {
    throw new UnsupportedOperationException("Unsupported operation.");
  }

  public Pair<LocalDate, Double> getLatestDataPoint(ExternalIdBundle identifierBundle, String dataSource, String dataProvider, String dataField, LocalDate start, boolean includeStart, LocalDate end,
      boolean includeEnd) {
    throw new UnsupportedOperationException("Unsupported operation.");
  }

  public HistoricalTimeSeries getHistoricalTimeSeries(String dataField, ExternalIdBundle identifierBundle, String resolutionKey) {
    throw new UnsupportedOperationException("Unsupported operation.");
  }

  public HistoricalTimeSeries getHistoricalTimeSeries(String dataField, ExternalIdBundle identifierBundle, String resolutionKey, LocalDate start, boolean includeStart, LocalDate end,
      boolean includeEnd) {
    throw new UnsupportedOperationException("Unsupported operation.");
  }

  public HistoricalTimeSeries getHistoricalTimeSeries(String dataField, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey) {
    throw new UnsupportedOperationException("Unsupported operation.");
  }

  public HistoricalTimeSeries getHistoricalTimeSeries(String dataField, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey, LocalDate start,
      boolean includeStart, LocalDate end, boolean includeEnd) {
    throw new UnsupportedOperationException("Unsupported operation.");
  }

  public HistoricalTimeSeries getHistoricalTimeSeries(String dataField, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey, LocalDate start,
      boolean includeStart, LocalDate end, boolean includeEnd, int maxPoints) {
    throw new UnsupportedOperationException("Unsupported operation.");
  }

  public Pair<LocalDate, Double> getLatestDataPoint(String dataField, ExternalIdBundle identifierBundle, String resolutionKey) {
    throw new UnsupportedOperationException("Unsupported operation.");
  }

  public Pair<LocalDate, Double> getLatestDataPoint(
      String dataField, ExternalIdBundle identifierBundle, String resolutionKey,
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    throw new UnsupportedOperationException("Unsupported operation.");
  }

  public Pair<LocalDate, Double> getLatestDataPoint(String dataField, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey) {
    throw new UnsupportedOperationException("Unsupported operation.");
  }

  public Pair<LocalDate, Double> getLatestDataPoint(String dataField, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey, LocalDate start, boolean includeStart,
      LocalDate end, boolean includeEnd) {
    throw new UnsupportedOperationException("Unsupported operation.");
  }

  public Map<ExternalIdBundle, HistoricalTimeSeries> getHistoricalTimeSeries(Set<ExternalIdBundle> identifierSet, String dataSource, String dataProvider, String dataField, LocalDate start,
      boolean includeStart, LocalDate end, boolean includeEnd) {
    throw new UnsupportedOperationException("Unsupported operation.");
  }

}
