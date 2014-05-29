/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.historicaltimeseries.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.DummyChangeManager;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeriesBuilder;
import com.opengamma.timeseries.date.localdate.LocalDateToIntConverter;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.metric.OpenGammaMetricRegistry;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * An extremely minimal and lightweight {@code HistoricalTimeSeriesSource} that pulls data
 * directly from Redis for situations where versioning, multiple fields, multiple data sources,
 * and identifier management is not necessary.
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
 */
public class NonVersionedRedisHistoricalTimeSeriesSource implements HistoricalTimeSeriesSource {
  private static final Logger s_logger = LoggerFactory.getLogger(NonVersionedRedisHistoricalTimeSeriesSource.class);
  private final JedisPool _jedisPool;
  private final String _redisPrefix;
  // ChangeManager is only returned to satisfy the interface and allow this source to be used with the engine, no notifications will be sent
  private final ChangeManager _changeManager = DummyChangeManager.INSTANCE;
  
  private Timer _getSeriesTimer = new Timer();
  private Timer _updateSeriesTimer = new Timer();
  private Timer _existsSeriesTimer = new Timer();
  
  public NonVersionedRedisHistoricalTimeSeriesSource(JedisPool jedisPool) {
    this(jedisPool, "");
  }
    
  public NonVersionedRedisHistoricalTimeSeriesSource(JedisPool jedisPool, String redisPrefix) {
    this(jedisPool, redisPrefix, "NonVersionedRedisHistoricalTimeSeriesSource");
  }

  protected NonVersionedRedisHistoricalTimeSeriesSource(JedisPool jedisPool, String redisPrefix, String metricsName) {
    ArgumentChecker.notNull(jedisPool, "jedisPool");
    ArgumentChecker.notNull(redisPrefix, "redisPrefix");
    ArgumentChecker.notNull(metricsName, "metricsName");
    _jedisPool = jedisPool;
    _redisPrefix = redisPrefix;
    registerMetrics(OpenGammaMetricRegistry.getSummaryInstance(), OpenGammaMetricRegistry.getDetailedInstance(), metricsName);
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
  
  public void registerMetrics(MetricRegistry summaryRegistry, MetricRegistry detailRegistry, String namePrefix) {
    _getSeriesTimer = summaryRegistry.timer(namePrefix + ".get");
    _updateSeriesTimer = summaryRegistry.timer(namePrefix + ".update");
    _existsSeriesTimer = summaryRegistry.timer(namePrefix + ".exists");
  }
  
  /**
   * Add a timeseries to Redis.
   * 
   * If the timerseries does not exist, it is created otherwise updated.
   * 
   * @param uniqueId the uniqueId, not null.
   * @param timeseries the timeseries, not null.
   */
  public void updateTimeSeries(UniqueId uniqueId, LocalDateDoubleTimeSeries timeseries) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(timeseries, "timeseries");
    
    updateTimeSeries(toRedisKey(uniqueId), timeseries, false);
  }
  
  /**
   * Remove all current entries for the given ID, and store the given time series.
   * 
   * If the timeseries does not exist, it is created.
   * 
   * @param uniqueId the uniqueId of the timeseries, not null.
   * @param timeSeries the timeseries to store
   */
  public void replaceTimeSeries(UniqueId uniqueId, LocalDateDoubleTimeSeries timeSeries) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(timeSeries, "timeSeries");
    
    updateTimeSeries(toRedisKey(uniqueId), timeSeries, true, 5);
  }

  protected void updateTimeSeries(String redisKey, LocalDateDoubleTimeSeries timeseries, boolean clear) {
    updateTimeSeries(redisKey, timeseries, clear, 5);
  }

  protected void updateTimeSeries(String redisKey, LocalDateDoubleTimeSeries timeseries, boolean clear, int attempts) {
    try (Timer.Context context = _updateSeriesTimer.time()) {
      Jedis jedis = getJedisPool().getResource();
      try {
        Map<String, String> htsMap = Maps.newHashMap();
        BiMap<Double, String> dates = HashBiMap.create();
        for (Entry<LocalDate, Double> entry : timeseries) {
          String dateAsIntText = Integer.toString(LocalDateToIntConverter.convertToInt(entry.getKey()));
          htsMap.put(dateAsIntText, Double.toString(entry.getValue()));
          dates.put(Double.valueOf(dateAsIntText), dateAsIntText);
        }

        String redisHtsDatapointKey = toRedisHtsDatapointKey(redisKey);
        jedis.hmset(redisHtsDatapointKey, htsMap);
        
        String redisHtsDaysKey = toRedisHtsDaysKey(redisKey);
        if (clear) {
          jedis.del(redisHtsDaysKey);
        } else {
          jedis.zrem(redisHtsDaysKey, dates.inverse().keySet().toArray(new String[dates.size()]));
        }
        
        jedis.zadd(redisHtsDaysKey, dates);
        
        getJedisPool().returnResource(jedis);
      } catch (Throwable e) {
        getJedisPool().returnBrokenResource(jedis);
        if (attempts > 0) {
          s_logger.warn("Unable to put timeseries with id, will retry: " + redisKey, e);
          updateTimeSeries(redisKey, timeseries, clear, attempts - 1);
        }
        throw new OpenGammaRuntimeException("Unable to put timeseries with id: " + redisKey, e);
      }
    }
  }
  
  private String toRedisHtsDaysKey(String redisKey) {
    return redisKey + ":hts.days";
  }

  private String toRedisHtsDatapointKey(String redisKey) {
    return redisKey + ":hts.datapoint";
  }

  /**
   * Updates a datapoint in a timeseries.
   * 
   * If the timeseries does not exist, one is created with the single data point.
   * 
   * @param uniqueId the uniqueId of the timeseries, not null.
   * @param valueDate the data point date, not null
   * @param value the data point value
   */
  public void updateTimeSeriesPoint(UniqueId uniqueId, LocalDate valueDate, double value) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(valueDate, "valueDate");
    
    updateTimeSeriesPoint(toRedisKey(uniqueId), valueDate, value);
  }
  
  protected void updateTimeSeriesPoint(String redisKey, LocalDate valueDate, double value) {
    LocalDateDoubleTimeSeriesBuilder builder = ImmutableLocalDateDoubleTimeSeries.builder();
    builder.put(valueDate, value);
    updateTimeSeries(redisKey, builder.build(), false);
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
  
  protected String toRedisKey(UniqueId uniqueId) {
    return toRedisKey(uniqueId, null);
  }
  
  protected String toRedisKey(ExternalIdBundle identifierBundle) {
    return toRedisKey(toUniqueId(identifierBundle));
  }
  
  protected String toRedisKey(UniqueId uniqueId, LocalDate simulationExecutionDate) {
    StringBuilder sb = new StringBuilder();
    String redisPrefix = StringUtils.trimToNull(getRedisPrefix());
    if (redisPrefix != null) {
      sb.append(getRedisPrefix());
      sb.append(':');
    }
    sb.append(LocalDateDoubleTimeSeries.class.getSimpleName());
    sb.append(':');
    sb.append(uniqueId.getScheme());
    sb.append('~');
    sb.append(uniqueId.getValue());
    if (simulationExecutionDate != null) {
      sb.append(':');
      sb.append(simulationExecutionDate.toString());
    }
    
    return sb.toString();
  }
  
  protected UniqueId toUniqueId(ExternalIdBundle identifierBundle) {
    if (identifierBundle.size() != 1) {
      s_logger.warn("Using NonVersionedRedisHistoricalTimeSeriesSource with bundle {} other than 1. Probable misuse.", identifierBundle);
    }
    ExternalId id = identifierBundle.iterator().next();
    UniqueId uniqueId = UniqueId.of(id.getScheme().getName(), id.getValue());
    return uniqueId;
  }
  
  public boolean exists(UniqueId uniqueId, LocalDate simulationExecutionDate) {
    try (Timer.Context context = _existsSeriesTimer.time()) {
      String redisKey = toRedisKey(uniqueId, simulationExecutionDate);
      String redisHtsDaysKey = toRedisHtsDaysKey(redisKey);
      boolean exists = false;
      Jedis jedis = getJedisPool().getResource();
      try {
        exists = jedis.exists(redisHtsDaysKey);
        getJedisPool().returnResource(jedis);
      } catch (Exception e) {
        s_logger.error("Unable to check for existance", e);
        getJedisPool().returnBrokenResource(jedis);
        throw new OpenGammaRuntimeException("Unable to check for existance", e);
      }
      return exists;
    }
  }
  
  public boolean exists(UniqueId uniqueId) {
    return exists(uniqueId, null);
  }
  
  public boolean exists(ExternalIdBundle identifierBundle) {
    return exists(toUniqueId(identifierBundle));
  }
    
  // ------------------------------------------------------------------------
  // SUPPORTED HISTORICAL TIME SERIES SOURCE OPERATIONS:
  // ------------------------------------------------------------------------
    
  protected LocalDateDoubleTimeSeries loadTimeSeriesFromRedis(String redisKey, LocalDate start, LocalDate end) {
    // This is the only method that needs implementation.
    try (Timer.Context context = _getSeriesTimer.time()) {
      Jedis jedis = getJedisPool().getResource();
      LocalDateDoubleTimeSeries ts = null;
      try {
        String redisHtsDaysKey = toRedisHtsDaysKey(redisKey);
        double min = Double.NEGATIVE_INFINITY;
        double max = Double.POSITIVE_INFINITY;
        if (start != null) {
          min = localDateToDouble(start);
        }
        if (end != null) {
          max = localDateToDouble(end);
        }
        Set<String> dateTexts = jedis.zrangeByScore(redisHtsDaysKey, min, max);
        if (!dateTexts.isEmpty()) {
          String redisHtsDatapointKey = toRedisHtsDatapointKey(redisKey);
          List<String> valueTexts = jedis.hmget(redisHtsDatapointKey, dateTexts.toArray(new String[dateTexts.size()]));
          
          List<Integer> times = Lists.newArrayListWithCapacity(dateTexts.size());
          List<Double> values = Lists.newArrayListWithCapacity(valueTexts.size());
          
          Iterator<String> dateItr = dateTexts.iterator();
          Iterator<String> valueItr = valueTexts.iterator();
          
          while (dateItr.hasNext()) {
            String dateAsIntText = dateItr.next();
            String valueText = StringUtils.trimToNull(valueItr.next());
            if (valueText != null) {
              times.add(Integer.parseInt(dateAsIntText));
              values.add(Double.parseDouble(valueText));
            }
          }
          ts = ImmutableLocalDateDoubleTimeSeries.of(ArrayUtils.toPrimitive(times.toArray(new Integer[times.size()])), ArrayUtils.toPrimitive(values.toArray(new Double[values.size()])));
        }
        getJedisPool().returnResource(jedis);
      } catch (Exception e) {
        s_logger.error("Unable to load points from redis for " + redisKey, e);
        getJedisPool().returnBrokenResource(jedis);
        throw new OpenGammaRuntimeException("Unable to load points from redis for " + redisKey, e);
      }
      return ts;
    }
  }

  private double localDateToDouble(final LocalDate date) {
    String dateAsIntText = Integer.toString(LocalDateToIntConverter.convertToInt(date));
    return Double.parseDouble(dateAsIntText);
  }

  public HistoricalTimeSeries getHistoricalTimeSeries(UniqueId uniqueId, LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    
    LocalDate actualStart = null;
    LocalDate actualEnd = null;
    
    if (start != null) {
      if (includeStart) {
        actualStart = start;
      } else {
        actualStart = start.plusDays(1);
      }
    }
    
    if (end != null) {
      if (includeEnd) {
        actualEnd = end;
      } else {
        actualEnd = end.minusDays(1);
      }
    }
    
    LocalDateDoubleTimeSeries ts = loadTimeSeriesFromRedis(toRedisKey(uniqueId), actualStart, actualEnd);
    SimpleHistoricalTimeSeries result = null;
    if (ts != null) {
      result = new SimpleHistoricalTimeSeries(uniqueId, ts);
    }
    return result;
  }

  public HistoricalTimeSeries getHistoricalTimeSeries(String dataField, ExternalIdBundle identifierBundle, String resolutionKey, LocalDate start, boolean includeStart, LocalDate end,
                                                      boolean includeEnd, int maxPoints) {
    ArgumentChecker.notNull(identifierBundle, "identifierBundle");
    
    if (identifierBundle.isEmpty()) {
      return null;
    }
    final ExternalId id = identifierBundle.iterator().next();
    final UniqueId uniqueId = UniqueId.of(id.getScheme().getName(), id.getValue());
    return getHistoricalTimeSeries(uniqueId, start, includeStart, end, includeEnd);
  }

  public HistoricalTimeSeries getHistoricalTimeSeries(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    
    LocalDateDoubleTimeSeries ts = loadTimeSeriesFromRedis(toRedisKey(uniqueId), null, null);
    if (ts == null) {
      return null;
    } else {
      return new SimpleHistoricalTimeSeries(uniqueId, ts);
    }
  }
  
  public ExternalIdBundle getExternalIdBundle(UniqueId uniqueId) {
    return ExternalId.of(uniqueId.getScheme(), uniqueId.getValue()).toBundle();
  }
  
  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    
    Pair<LocalDate, Double> latestPoint = null;
    LocalDateDoubleTimeSeries ts = loadTimeSeriesFromRedis(toRedisKey(uniqueId), null, null);
    if (ts != null) {
      latestPoint = Pairs.of(ts.getLatestTime(), ts.getLatestValue());
    }
    return latestPoint;
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(UniqueId uniqueId, LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    
    HistoricalTimeSeries hts = getHistoricalTimeSeries(uniqueId, start, includeStart, end, includeEnd);
    Pair<LocalDate, Double> latestPoint = null;
    if (hts != null && hts.getTimeSeries() != null) {
      latestPoint = Pairs.of(hts.getTimeSeries().getLatestTime(), hts.getTimeSeries().getLatestValue());
    }
    return latestPoint;
  }
  
  protected LocalDateDoubleTimeSeries getLocalDateDoubleTimeSeries(ExternalIdBundle identifierBundle) {
    return loadTimeSeriesFromRedis(toRedisKey(identifierBundle), null, null);
  }
  
  protected HistoricalTimeSeries getHistoricalTimeSeries(ExternalIdBundle identifierBundle) {
    UniqueId uniqueId = toUniqueId(identifierBundle);
    
    LocalDateDoubleTimeSeries ts = getLocalDateDoubleTimeSeries(identifierBundle);
    if (ts == null) {
      return null;
    }
    HistoricalTimeSeries hts = new SimpleHistoricalTimeSeries(uniqueId, ts);
    return hts;
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(ExternalIdBundle identifierBundle, String dataSource, String dataProvider, String dataField) {
    return getHistoricalTimeSeries(identifierBundle);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField) {
    return getHistoricalTimeSeries(identifierBundle);
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField) {
    UniqueId uniqueId = toUniqueId(identifierBundle);
    return getLatestDataPoint(uniqueId);
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(ExternalIdBundle identifierBundle, String dataSource, String dataProvider, String dataField) {
    UniqueId uniqueId = toUniqueId(identifierBundle);
    return getLatestDataPoint(uniqueId);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(String dataField, ExternalIdBundle identifierBundle, String resolutionKey) {
    UniqueId uniqueId = toUniqueId(identifierBundle);
    return getHistoricalTimeSeries(uniqueId);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(String dataField, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey) {
    UniqueId uniqueId = toUniqueId(identifierBundle);
    return getHistoricalTimeSeries(uniqueId);
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(String dataField, ExternalIdBundle identifierBundle, String resolutionKey) {
    UniqueId uniqueId = toUniqueId(identifierBundle);
    return getLatestDataPoint(uniqueId);
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(String dataField, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey) {
    UniqueId uniqueId = toUniqueId(identifierBundle);
    return getLatestDataPoint(uniqueId);
  }

  public ChangeManager changeManager() {
    return _changeManager;
  }

  public HistoricalTimeSeries getHistoricalTimeSeries(ExternalIdBundle identifierBundle, String dataSource, String dataProvider, String dataField, LocalDate start, boolean includeStart,
                                                      LocalDate end, boolean includeEnd) {
    UniqueId uniqueId = toUniqueId(identifierBundle);
    return getHistoricalTimeSeries(uniqueId, start, includeStart, end, includeEnd);
  }

  public HistoricalTimeSeries getHistoricalTimeSeries(String dataField, ExternalIdBundle identifierBundle, String resolutionKey, LocalDate start, boolean includeStart, LocalDate end,
                                                      boolean includeEnd) {
    UniqueId uniqueId = toUniqueId(identifierBundle);
    return getHistoricalTimeSeries(uniqueId, start, includeStart, end, includeEnd);
  }

  public HistoricalTimeSeries getHistoricalTimeSeries(String dataField, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey, LocalDate start,
                                                      boolean includeStart, LocalDate end, boolean includeEnd) {
    UniqueId uniqueId = toUniqueId(identifierBundle);
    return getHistoricalTimeSeries(uniqueId, start, includeStart, end, includeEnd);
  }

  public HistoricalTimeSeries getHistoricalTimeSeries(UniqueId uniqueId, LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd, int maxPoints) {
    return getHistoricalTimeSeries(uniqueId, start, includeStart, end, includeEnd);
  }

  public HistoricalTimeSeries getHistoricalTimeSeries(ExternalIdBundle identifierBundle, String dataSource, String dataProvider, String dataField, LocalDate start, boolean includeStart,
      LocalDate end, boolean includeEnd, int maxPoints) {
    UniqueId uniqueId = toUniqueId(identifierBundle);
    return getHistoricalTimeSeries(uniqueId, start, includeStart, end, includeEnd);
  }

  public HistoricalTimeSeries getHistoricalTimeSeries(ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField, LocalDate start,
      boolean includeStart, LocalDate end, boolean includeEnd) {
    UniqueId uniqueId = toUniqueId(identifierBundle);
    return getHistoricalTimeSeries(uniqueId, start, includeStart, end, includeEnd);
  }

  public HistoricalTimeSeries getHistoricalTimeSeries(ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField, LocalDate start,
      boolean includeStart, LocalDate end, boolean includeEnd, int maxPoints) {
    UniqueId uniqueId = toUniqueId(identifierBundle);
    return getHistoricalTimeSeries(uniqueId, start, includeStart, end, includeEnd);
  }

  public Pair<LocalDate, Double> getLatestDataPoint(ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField, LocalDate start,
      boolean includeStart, LocalDate end, boolean includeEnd) {
    UniqueId uniqueId = toUniqueId(identifierBundle);
    return getLatestDataPoint(uniqueId, start, includeStart, end, includeEnd);
  }

  public Pair<LocalDate, Double> getLatestDataPoint(ExternalIdBundle identifierBundle, String dataSource, String dataProvider, String dataField, LocalDate start, boolean includeStart, LocalDate end,
      boolean includeEnd) {
    UniqueId uniqueId = toUniqueId(identifierBundle);
    return getLatestDataPoint(uniqueId, start, includeStart, end, includeEnd);
  }

  public HistoricalTimeSeries getHistoricalTimeSeries(String dataField, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey, LocalDate start,
      boolean includeStart, LocalDate end, boolean includeEnd, int maxPoints) {
    UniqueId uniqueId = toUniqueId(identifierBundle);
    return getHistoricalTimeSeries(uniqueId, start, includeStart, end, includeEnd);
  }

  public Pair<LocalDate, Double> getLatestDataPoint(String dataField, ExternalIdBundle identifierBundle, String resolutionKey,
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    UniqueId uniqueId = toUniqueId(identifierBundle);
    return getLatestDataPoint(uniqueId, start, includeStart, end, includeEnd);
  }

  public Pair<LocalDate, Double> getLatestDataPoint(String dataField, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey, LocalDate start, boolean includeStart,
      LocalDate end, boolean includeEnd) {
    UniqueId uniqueId = toUniqueId(identifierBundle);
    return getLatestDataPoint(uniqueId, start, includeStart, end, includeEnd);
  }

  public Map<ExternalIdBundle, HistoricalTimeSeries> getHistoricalTimeSeries(Set<ExternalIdBundle> identifierSet, String dataSource, String dataProvider, String dataField, LocalDate start,
      boolean includeStart, LocalDate end, boolean includeEnd) {
    ImmutableMap.Builder<ExternalIdBundle, HistoricalTimeSeries> map = ImmutableMap.builder();
    for (ExternalIdBundle bundle : identifierSet) {
      HistoricalTimeSeries series = getHistoricalTimeSeries(bundle, dataSource, dataProvider, dataField, start, includeStart, end, includeEnd);
      map.put(bundle, series);
    }
    return map.build();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NonVersionedRedisHistoricalTimeSeriesSource that = (NonVersionedRedisHistoricalTimeSeriesSource) o;
    return _jedisPool.equals(that._jedisPool) && _redisPrefix.equals(that._redisPrefix);
  }

  @Override
  public int hashCode() {
    int result = _jedisPool.hashCode();
    return 31 * result + _redisPrefix.hashCode();
  }
}
