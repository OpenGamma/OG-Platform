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

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesInfo;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesResolutionResult;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdBundleWithDates;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateToIntConverter;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ParallelArrayBinarySort;
import com.opengamma.util.metric.MetricProducer;
import com.opengamma.lambdava.tuple.Pair;

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
 *   <li>Each time series has a <b>single</b> {@link ExternalId} when then acts
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
public class RedisSimulationSeriesSource implements HistoricalTimeSeriesSource, HistoricalTimeSeriesResolver, MetricProducer {
  private static final Logger s_logger = LoggerFactory.getLogger(RedisSimulationSeriesSource.class);
  private final JedisPool _jedisPool;
  private final String _redisPrefix;
  private LocalDate _currentSimulationExecutionDate = LocalDate.now();
  private Timer _getSeriesTimer = new Timer();
  
  public RedisSimulationSeriesSource(JedisPool jedisPool) {
    this(jedisPool, "");
  }
  
  public RedisSimulationSeriesSource(JedisPool jedisPool, String redisPrefix) {
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
  // METRICS:
  // ------------------------------------------------------------------------
  
  @Override
  public void registerMetrics(MetricRegistry summaryRegistry, MetricRegistry detailRegistry, String namePrefix) {
    _getSeriesTimer = summaryRegistry.timer(namePrefix + ".get");
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
  
  /**
   * Completely empty the underlying Redis server.
   * You should only call this if you really know what you're doing.
   */
  public void completelyClearRedis() {
    Jedis jedis = _jedisPool.getResource();
    try {
      jedis.flushDB();
    } catch (Exception e) {
      s_logger.error("Unable to clear database", e);
      _jedisPool.returnBrokenResource(jedis);
      throw new OpenGammaRuntimeException("Unable to clear database", e);
    } finally {
      _jedisPool.returnResource(jedis);
    }
  }
  
  public void clearExecutionDate(LocalDate simulationExecutionDate) {
    final String keysPattern = _redisPrefix + "*_" + simulationExecutionDate.toString();
    Jedis jedis = _jedisPool.getResource();
    try {
      Set<String> keys = jedis.keys(keysPattern);
      if (!keys.isEmpty()) {
        jedis.del(keys.toArray(new String[0]));
      }
    } catch (Exception e) {
      s_logger.error("Unable to clear execution date " + simulationExecutionDate, e);
      _jedisPool.returnBrokenResource(jedis);
      throw new OpenGammaRuntimeException("Unable to clear execution date " + simulationExecutionDate, e);
    } finally {
      _jedisPool.returnResource(jedis);
    }
  }
  
  // ------------------------------------------------------------------------
  // SUPPORTED HISTORICAL TIME SERIES SOURCE OPERATIONS:
  // ------------------------------------------------------------------------
  @Override
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

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(UniqueId uniqueId) {
    LocalDateDoubleTimeSeries ts = loadTimeSeriesFromRedis(uniqueId);
    if (ts == null) {
      return null;
    } else {
      return new SimpleHistoricalTimeSeries(uniqueId, ts);
    }
  }
  
  private LocalDateDoubleTimeSeries loadTimeSeriesFromRedis(UniqueId uniqueId) {
    // This is the only method that needs implementation.
    
    try (Timer.Context context = _getSeriesTimer.time()) {
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
      
      if ((valuesFromRedis == null) || valuesFromRedis.isEmpty()) {
        return null;
      }
      
      LocalDateDoubleTimeSeries ts = composeFromRedisValues(valuesFromRedis);
      return ts;
    }
    
  }

  private static LocalDateDoubleTimeSeries composeFromRedisValues(Map<String, String> valuesFromRedis) {
    
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

  // ------------------------------------------------------------------------
  // UTILITY METHODS:
  // ------------------------------------------------------------------------
  
  @Override
  public HistoricalTimeSeriesResolutionResult resolve(ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField,
      String resolutionKey) {
    if (identifierBundle.size() > 1) {
      s_logger.warn("Attempted to call RedisSimulationSeriesSource with bundle {}. Calls with more than 1 entry in ID bundle are probably misuse of this class.", identifierBundle);
    }
    ExternalId externalId = identifierBundle.getExternalIds().iterator().next();
    final UniqueId uniqueId = UniqueId.of(externalId);
    HistoricalTimeSeriesInfo htsInfo = new HistoricalTimeSeriesInfo() {
      @Override
      public UniqueId getUniqueId() {
        return uniqueId;
      }

      @Override
      public ExternalIdBundleWithDates getExternalIdBundle() {
        throw new UnsupportedOperationException("Unsupported operation.");
      }

      @Override
      public String getName() {
        throw new UnsupportedOperationException("Unsupported operation.");
      }

      @Override
      public String getDataField() {
        throw new UnsupportedOperationException("Unsupported operation.");
      }

      @Override
      public String getDataSource() {
        throw new UnsupportedOperationException("Unsupported operation.");
      }

      @Override
      public String getDataProvider() {
        throw new UnsupportedOperationException("Unsupported operation.");
      }

      @Override
      public String getObservationTime() {
        throw new UnsupportedOperationException("Unsupported operation.");
      }

      @Override
      public ObjectId getTimeSeriesObjectId() {
        throw new UnsupportedOperationException("Unsupported operation.");
      }
      
    };
    HistoricalTimeSeriesResolutionResult result = new HistoricalTimeSeriesResolutionResult(htsInfo);
    return result;
  }

  // ------------------------------------------------------------------------
  // UTILITY METHODS:
  // ------------------------------------------------------------------------
  
  private String toRedisKey(UniqueId uniqueId, LocalDate simulationExecutionDate) {
    StringBuilder sb = new StringBuilder();
    
    sb.append(_redisPrefix);
    sb.append('_');
    sb.append(uniqueId);
    sb.append('_');
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
