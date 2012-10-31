/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.timeseries.snapshot;

import java.util.Iterator;
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
import redis.clients.jedis.Response;

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
  private BlackList _dataFieldBlackList;
  private BlackList _schemeBlackList;
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
  public BlackList getDataFieldBlackList() {
    return _dataFieldBlackList;
  }

  /**
   * Sets the dataFieldBlackList.
   * @param dataFieldBlackList  the dataFieldBlackList
   */
  public void setDataFieldBlackList(BlackList dataFieldBlackList) {
    _dataFieldBlackList = dataFieldBlackList;
  }

  /**
   * Gets the schemeBlackList.
   * @return the schemeBlackList
   */
  public BlackList getSchemeBlackList() {
    return _schemeBlackList;
  }

  /**
   * Sets the schemeBlackList.
   * @param schemeBlackList  the schemeBlackList
   */
  public void setSchemeBlackList(BlackList schemeBlackList) {
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
    Map<ExternalId, Map<String, String>> redisLKV = getRedisLKValues();
    for (Entry<ExternalId, Map<String, String>> lkvEntry : redisLKV.entrySet()) {
      updateTimeSeries(lkvEntry.getKey(), lkvEntry.getValue());
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
      
      if (haveDataFieldBlackList() && _dataFieldBlackList.getBlackList().contains(fieldName.toUpperCase())) {
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
    return _dataFieldBlackList != null && _dataFieldBlackList.getBlackList() != null;
  }

  private Map<ExternalId, Map<String, String>> getRedisLKValues() {
    List<ExternalId> allSecurities = getAllSecurities();
    return getLastKnownValues(allSecurities);
  }
  
  @SuppressWarnings("unchecked")
  private Map<ExternalId, Map<String, String>> getLastKnownValues(final List<ExternalId> allSecurities) {
    Map<ExternalId, Map<String, String>> result = Maps.newHashMap();
    JedisPool jedisPool = _redisConnector.getJedisPool();
    Jedis jedis = jedisPool.getResource();
    Pipeline pipeline = jedis.pipelined();
    //start transaction
    pipeline.multi();
    for (ExternalId identifier : allSecurities) {
      String redisKey = generateRedisKey(identifier.getScheme().getName(), identifier.getValue(), getNormalizationRuleSetId());
      pipeline.hgetAll(redisKey);
    }
    Response<List<Object>> response = pipeline.exec();
    pipeline.sync();
    
    final Iterator<ExternalId> allSecItr = allSecurities.iterator();
    final Iterator<Object> responseItr = response.get().iterator();
    while (responseItr.hasNext() && allSecItr.hasNext()) {
      result.put(allSecItr.next(), (Map<String, String>) responseItr.next());
    }
    jedisPool.returnResource(jedis);
    return result;
  }

  private List<ExternalId> getAllSecurities() {
    List<ExternalId> securities = Lists.newArrayList();
    Set<String> allSchemes = getAllSchemes();
    for (String scheme : allSchemes) {
      if (haveSchemeBlackList() && _schemeBlackList.getBlackList().contains(scheme.toUpperCase())) {
        continue;
      }
      Set<String> allIdentifiers = getAllIdentifiers(scheme);
      for (String identifier : allIdentifiers) {
        securities.add(ExternalId.of(scheme, identifier));
      }
    }
    return securities;
  }

  private boolean haveSchemeBlackList() {
    return _schemeBlackList != null && _schemeBlackList.getBlackList() != null;
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
