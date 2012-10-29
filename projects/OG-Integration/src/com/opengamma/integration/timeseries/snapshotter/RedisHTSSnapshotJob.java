/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.timeseries.snapshotter;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.time.calendar.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Pipeline;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.livedata.server.LastKnownValueStoreProvider;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesMasterUtils;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.OpenGammaClock;

/**
 * 
 */
public class RedisHTSSnapshotJob implements Runnable {
  
  private static final Logger s_logger = LoggerFactory.getLogger(RedisHTSSnapshotJob.class);
  
  private final HistoricalTimeSeriesMaster _htsMaster;
  private final String _dataSource;
  private final List<String> _blackListDataFields = Lists.newArrayList();
  private final List<String> _blackListSchemes = Lists.newArrayList();
  private final String _observationTime;
  private final String _normalizationRuleSetId;
  
  private String _redisServer;
  private int _port = 6379;
  private String _globalPrefix = "";
  private volatile boolean _isInitialized;
  private JedisPool _jedisPool;
  
  public RedisHTSSnapshotJob(final LastKnownValueStoreProvider lkvStoreProvider, final HistoricalTimeSeriesMaster htsMaster, 
      final String dataSource, final String observationTime, final String normalizationRuleSetId) {
    ArgumentChecker.notNull(lkvStoreProvider, "LKV store provider");
    ArgumentChecker.notNull(normalizationRuleSetId, "normalization rule set Id");
    ArgumentChecker.notNull(dataSource, "dataSource");
    ArgumentChecker.notNull(observationTime, "observation time");
    ArgumentChecker.notNull(htsMaster, "historical timeseries master");
    
    _normalizationRuleSetId = normalizationRuleSetId;
    _dataSource = dataSource;
    _observationTime = observationTime;
    _htsMaster = htsMaster;
    
  }
  
  /**
   * Gets the port.
   * @return the port
   */
  public int getPort() {
    return _port;
  }

  /**
   * Sets the port.
   * @param port  the port
   */
  public void setPort(int port) {
    _port = port;
  }

  /**
   * Gets the globalPrefix.
   * @return the globalPrefix
   */
  public String getGlobalPrefix() {
    return _globalPrefix;
  }

  /**
   * Sets the globalPrefix.
   * @param globalPrefix  the globalPrefix
   */
  public void setGlobalPrefix(String globalPrefix) {
    _globalPrefix = globalPrefix;
  }

  /**
   * Gets the redisServer.
   * @return the redisServer
   */
  public String getRedisServer() {
    return _redisServer;
  }

  /**
   * Sets the redisServer.
   * @param redisServer  the redisServer
   */
  public void setRedisServer(String redisServer) {
    _redisServer = redisServer;
  }


  /**
   * Gets the htsMaster.
   * @return the htsMaster
   */
  public HistoricalTimeSeriesMaster getHtsMaster() {
    return _htsMaster;
  }

  /**
   * Gets the dataSource.
   * @return the dataSource
   */
  public String getDataSource() {
    return _dataSource;
  }

  /**
   * Gets the blackListDataFields.
   * @return the blackListDataFields
   */
  public List<String> getBlackListDataFields() {
    return ImmutableList.copyOf(_blackListDataFields);
  }

  /**
   * Sets the blackListDataFields.
   * @param blackListDataFields  the blackListDataFields
   */
  public void setBlackListDataFields(List<String> blackListDataFields) {
    for (String dataField : blackListDataFields) {
      _blackListDataFields.add(dataField.toUpperCase());
    }
  }
  
  /**
   * Gets the blackListSchemes.
   * @return the blackListSchemes
   */
  public List<String> getBlackListSchemes() {
    return ImmutableList.copyOf(_blackListSchemes);
  }

  /**
   * Sets the blackListSchemes.
   * @param blackListSchemes  the blackListSchemes
   */
  public void setBlackListSchemes(List<String> blackListSchemes) {
    for (String scheme : blackListSchemes) {
      _blackListDataFields.add(scheme.toUpperCase());
    }
  }

  /**
   * Gets the observationTime.
   * @return the observationTime
   */
  public String getObservationTime() {
    return _observationTime;
  }
  
  /**
   * Gets the normalizationRuleSetId.
   * @return the normalizationRuleSetId
   */
  public String getNormalizationRuleSetId() {
    return _normalizationRuleSetId;
  }
  
  protected void initIfNecessary() {
    if (_isInitialized) {
      return;
    }
    synchronized (this) {
      assert _jedisPool == null;
      s_logger.info("Connecting to {}:{}.", new Object[] {getRedisServer(), getPort()});
      JedisPoolConfig poolConfig = new JedisPoolConfig();
      //poolConfig.set...
      JedisPool pool = new JedisPool(poolConfig, getRedisServer(), getPort());
      _jedisPool = pool;
      
      _isInitialized = true;
    }
  }
  
  @Override
  public void run() {
    initIfNecessary();
    Map<ExternalId, Map<String, String>> redisLKV = loadLastKnownValues();
    for (Entry<ExternalId, Map<String, String>> lkvEntry : redisLKV.entrySet()) {
      ExternalId externalId = lkvEntry.getKey();
      Map<String, String> lkv = lkvEntry.getValue();
      updateTimeSeries(externalId, lkv);
    }
    s_logger.debug("Total loaded lkv: {}", redisLKV.size());
  }

  private void updateTimeSeries(ExternalId externalId, Map<String, String> lkv) {
    HistoricalTimeSeriesMasterUtils htsMaster = new HistoricalTimeSeriesMasterUtils(getHtsMaster());
    LocalDate today = LocalDate.now(OpenGammaClock.getInstance());
    for (Entry<String, String> lkvEntry : lkv.entrySet()) {
      String fieldName = lkvEntry.getKey();
      Double value = Double.parseDouble(lkvEntry.getValue());
      if (_blackListDataFields.contains(fieldName.toUpperCase())) {
        continue;
      }
      if (value != null) {
        String dataField = makeDataField(fieldName);
        String dataProvider = externalId.getScheme().getName();
        if ("SURF".equals(dataProvider.toUpperCase())) {
          dataProvider = "TULLETTPREBON";
        }
        s_logger.debug("updating ts {}:{}/{}/{}/{} with {}:{}", 
            new Object[] {externalId, getDataSource(), dataProvider, dataField, getObservationTime(), today, value});
        htsMaster.writeTimeSeriesPoint(makeDescription(externalId, dataField), getDataSource(), dataProvider, 
            dataField, getObservationTime(), ExternalIdBundle.of(externalId), today, value);
      }
    }
  }

  private Map<ExternalId, Map<String, String>> loadLastKnownValues() {
    Map<ExternalId, Map<String, String>> result = Maps.newHashMap();
    
    Set<String> allSchemes = getAllSchemes();
    for (String scheme : allSchemes) {
      if (_blackListSchemes.contains(scheme.toUpperCase())) {
        continue;
      }
     
      Set<String> allIdentifiers = getAllIdentifiers(scheme);
      // batch in groups of 20000
      for (List<String> partition : Iterables.partition(allIdentifiers, 20000)) {
        result.putAll(loadSubListLKValues(Lists.newArrayList(partition), scheme));
      }
    }
    return result;
  }
  
  public void destroy() {
    synchronized (this) {
      if (_jedisPool != null) {
        _jedisPool.destroy();
        _isInitialized = false;
        _jedisPool = null;
      }
    }
  }
  
  @SuppressWarnings("unchecked")
  private Map<ExternalId, Map<String, String>> loadSubListLKValues(final List<String> subList, final String scheme) {
    Map<ExternalId, Map<String, String>> result = Maps.newHashMap();
    
    Jedis jedis = _jedisPool.getResource();
    Pipeline pipeline = jedis.pipelined();
    for (String identifier : subList) {
      String redisKey = generateRedisKey(scheme, identifier, getNormalizationRuleSetId());
      pipeline.hgetAll(redisKey);
    }
    List<Object> lkvalues = pipeline.syncAndReturnAll();
  
    int count = 0;
    for (Object lkvObj : lkvalues) {
      String identifier = subList.get(count++);
      result.put(ExternalId.of(scheme, identifier), (Map<String, String>) lkvObj);
    }    
    _jedisPool.returnResource(jedis);
    return result;
  }

  private String generateRedisKey(String scheme, String identifier, String normalizationRuleSetId) {
    StringBuilder sb = new StringBuilder();
    if (getGlobalPrefix() != null) {
      sb.append(getGlobalPrefix());
    }
    sb.append(scheme);
    sb.append("-");
    sb.append(identifier);
    sb.append("[");
    sb.append(normalizationRuleSetId);
    sb.append("]");
    return sb.toString();
  }

  private Set<String> getAllSchemes() {
    Jedis jedis = _jedisPool.getResource();
    Set<String> allMembers = jedis.smembers(generateAllSchemesKey());
    _jedisPool.returnResource(jedis);
    s_logger.info("Loaded {} schemes from Jedis (full contents in Debug level log)", allMembers.size());
    if (s_logger.isDebugEnabled()) {
      s_logger.debug("Loaded schemes from Jedis: {}", allMembers);
    }
    return allMembers;
  }

  private String makeDataField(String fieldName) {
    return fieldName.replaceAll("\\s+", "_").toUpperCase();
  }

  private String makeDescription(final ExternalId externalId, final String dataField) {
    return getDataSource() + "_" + externalId.getScheme() + "_" + externalId.getValue() + "_" + dataField;
  }
  
  private String generateAllSchemesKey() {
    StringBuilder sb = new StringBuilder();
    if (getGlobalPrefix() != null) {
      sb.append(getGlobalPrefix());
    }
    sb.append("-<ALL_SCHEMES>");
    return sb.toString();
  }
  
  private String generatePerSchemeKey(String scheme) {
    StringBuilder sb = new StringBuilder();
    if (getGlobalPrefix() != null) {
      sb.append(getGlobalPrefix());
    }
    sb.append(scheme);
    sb.append("-");
    sb.append("<ALL_IDENTIFIERS>");
    return sb.toString();
  }
  
  private Set<String> getAllIdentifiers(String identifierScheme) {
    initIfNecessary();
    Jedis jedis = _jedisPool.getResource();
    Set<String> allMembers = jedis.smembers(generatePerSchemeKey(identifierScheme));
    _jedisPool.returnResource(jedis);
    s_logger.info("Loaded {} identifiers from Jedis (full contents in Debug level log)", allMembers.size());
    if (s_logger.isDebugEnabled()) {
      s_logger.debug("Loaded identifiers from Jedis: {}", allMembers);
    }
    return allMembers;
  }

}
