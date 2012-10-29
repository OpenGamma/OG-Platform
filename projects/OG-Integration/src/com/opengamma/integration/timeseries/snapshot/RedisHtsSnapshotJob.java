/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.timeseries.snapshot;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.time.calendar.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesMasterUtils;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.OpenGammaClock;
import com.opengamma.util.redis.RedisConnector;

/**
 * Job that snapshot lastest market values in RedisServer and updates the timeseries master 
 */
public class RedisHtsSnapshotJob implements Runnable {
  
  private static final Logger s_logger = LoggerFactory.getLogger(RedisHtsSnapshotJob.class);
  
  private HistoricalTimeSeriesMaster _htsMaster;
  private String _dataSource;
  private DataFieldBlackList _dataFieldBlackList;
  private SchemeBlackList _schemeBlackList;
  private String _observationTime;
  private String _normalizationRuleSetId;
  
  private String _globalPrefix = "";
  private RedisConnector _redisConnector;
  
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
   * Gets the dataFieldBlackList.
   * @return the dataFieldBlackList
   */
  public DataFieldBlackList getDataFieldBlackList() {
    return _dataFieldBlackList;
  }

  /**
   * Sets the dataFieldBlackList.
   * @param dataFieldBlackList  the dataFieldBlackList
   */
  public void setDataFieldBlackList(DataFieldBlackList dataFieldBlackList) {
    _dataFieldBlackList = dataFieldBlackList;
  }

  /**
   * Gets the schemeBlackList.
   * @return the schemeBlackList
   */
  public SchemeBlackList getSchemeBlackList() {
    return _schemeBlackList;
  }

  /**
   * Sets the schemeBlackList.
   * @param schemeBlackList  the schemeBlackList
   */
  public void setSchemeBlackList(SchemeBlackList schemeBlackList) {
    _schemeBlackList = schemeBlackList;
  }

  /**
   * Gets the redisConnector.
   * @return the redisConnector
   */
  public RedisConnector getRedisConnector() {
    return _redisConnector;
  }

  /**
   * Sets the redisConnector.
   * @param redisConnector  the redisConnector
   */
  public void setRedisConnector(RedisConnector redisConnector) {
    _redisConnector = redisConnector;
  }

  /**
   * Sets the dataSource.
   * @param dataSource  the dataSource
   */
  public void setDataSource(String dataSource) {
    _dataSource = dataSource;
  }

  /**
   * Sets the observationTime.
   * @param observationTime  the observationTime
   */
  public void setObservationTime(String observationTime) {
    _observationTime = observationTime;
  }

  /**
   * Sets the normalizationRuleSetId.
   * @param normalizationRuleSetId  the normalizationRuleSetId
   */
  public void setNormalizationRuleSetId(String normalizationRuleSetId) {
    _normalizationRuleSetId = normalizationRuleSetId;
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
    
  @Override
  public void run() {
    validateState();
    Map<ExternalId, Map<String, String>> redisLKV = loadLastKnownValues();
    for (Entry<ExternalId, Map<String, String>> lkvEntry : redisLKV.entrySet()) {
      ExternalId externalId = lkvEntry.getKey();
      Map<String, String> lkv = lkvEntry.getValue();
      updateTimeSeries(externalId, lkv);
    }
    s_logger.debug("Total loaded lkv: {}", redisLKV.size());
  }

  private void validateState() {
    ArgumentChecker.notNull(getNormalizationRuleSetId(), "normalization rule set Id");
    ArgumentChecker.notNull(getDataSource(), "dataSource");
    ArgumentChecker.notNull(getObservationTime(), "observation time");
    ArgumentChecker.notNull(getHtsMaster(), "historical timeseries master");
    ArgumentChecker.notNull(getRedisConnector(), "redis connector");
  }

  private void updateTimeSeries(ExternalId externalId, Map<String, String> lkv) {
    HistoricalTimeSeriesMasterUtils htsMaster = new HistoricalTimeSeriesMasterUtils(getHtsMaster());
    LocalDate today = LocalDate.now(OpenGammaClock.getInstance());
    for (Entry<String, String> lkvEntry : lkv.entrySet()) {
      String fieldName = lkvEntry.getKey();
      Double value = Double.parseDouble(lkvEntry.getValue());
      
      if (haveDataFieldBlackList() && _dataFieldBlackList.getDataFieldBlackList().contains(fieldName.toUpperCase())) {
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

  private boolean haveDataFieldBlackList() {
    return _dataFieldBlackList != null && _dataFieldBlackList.getDataFieldBlackList() != null;
  }

  private Map<ExternalId, Map<String, String>> loadLastKnownValues() {
    Map<ExternalId, Map<String, String>> result = Maps.newHashMap();
    
    Set<String> allSchemes = getAllSchemes();
    for (String scheme : allSchemes) {
      if (haveSchemeBlackList() && _schemeBlackList.getSchemeBlackList().contains(scheme.toUpperCase())) {
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

  private boolean haveSchemeBlackList() {
    return _schemeBlackList != null && _schemeBlackList.getSchemeBlackList() != null;
  }
    
  @SuppressWarnings("unchecked")
  private Map<ExternalId, Map<String, String>> loadSubListLKValues(final List<String> subList, final String scheme) {
    Map<ExternalId, Map<String, String>> result = Maps.newHashMap();
    JedisPool jedisPool = _redisConnector.getJedisPool();
    Jedis jedis = jedisPool.getResource();
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
    jedisPool.returnResource(jedis);
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
    JedisPool jedisPool = _redisConnector.getJedisPool();
    Jedis jedis = jedisPool.getResource();
    Set<String> allMembers = jedis.smembers(generateAllSchemesKey());
    jedisPool.returnResource(jedis);
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
    JedisPool jedisPool = _redisConnector.getJedisPool();
    Jedis jedis = jedisPool.getResource();
    Set<String> allMembers = jedis.smembers(generatePerSchemeKey(identifierScheme));
    jedisPool.returnResource(jedis);
    s_logger.info("Loaded {} identifiers from Jedis (full contents in Debug level log)", allMembers.size());
    if (s_logger.isDebugEnabled()) {
      s_logger.debug("Loaded identifiers from Jedis: {}", allMembers);
    }
    return allMembers;
  }

  public void setHistoricalTimeSeriesMaster(HistoricalTimeSeriesMaster htsMaster) {
    _htsMaster = htsMaster;
  }
}
