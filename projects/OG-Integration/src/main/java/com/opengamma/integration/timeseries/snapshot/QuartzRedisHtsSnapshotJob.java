/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.timeseries.snapshot;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.util.redis.RedisConnector;

/**
 * Quartz job that starts and executes Redis hts snaphotter
 */
public class QuartzRedisHtsSnapshotJob extends QuartzJobBean {
  
  private HistoricalTimeSeriesMaster _htsMaster;
  
  private String _dataSource;
  
  private String _normalizationRuleSetId;
  
  private String _observationTime;
  
  private RedisConnector _redisConnector;
  
  private BlackList _schemeBlackList;
  
  private BlackList _dataFieldBlackList;
  
  private String _globalPrefix;
  
  private String _baseDir;
    
  /**
   * Gets the htsMaster.
   * @return the htsMaster
   */
  public HistoricalTimeSeriesMaster getHtsMaster() {
    return _htsMaster;
  }

  /**
   * Sets the htsMaster.
   * @param htsMaster  the htsMaster
   */
  public void setHtsMaster(HistoricalTimeSeriesMaster htsMaster) {
    _htsMaster = htsMaster;
  }

  /**
   * Gets the dataSource.
   * @return the dataSource
   */
  public String getDataSource() {
    return _dataSource;
  }

  /**
   * Sets the dataSource.
   * @param dataSource  the dataSource
   */
  public void setDataSource(String dataSource) {
    _dataSource = dataSource;
  }

  /**
   * Gets the normalizationRuleSetId.
   * @return the normalizationRuleSetId
   */
  public String getNormalizationRuleSetId() {
    return _normalizationRuleSetId;
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
   * Sets the observationTime.
   * @param observationTime  the observationTime
   */
  public void setObservationTime(String observationTime) {
    _observationTime = observationTime;
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
   * Gets the baseDir.
   * @return the baseDir
   */
  public String getBaseDir() {
    return _baseDir;
  }

  /**
   * Sets the baseDir.
   * @param baseDir  the baseDir
   */
  public void setBaseDir(String baseDir) {
    _baseDir = baseDir;
  }

  @Override
  protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
    RedisHtsSnapshotJob job = new RedisHtsSnapshotJob();
    job.setHistoricalTimeSeriesMaster(getHtsMaster());
    if (getGlobalPrefix() != null) {
      job.setGlobalPrefix(getGlobalPrefix());
    }
    job.setDataFieldBlackList(getDataFieldBlackList());
    job.setSchemeBlackList(getSchemeBlackList());
    job.setDataSource(getDataSource());
    job.setNormalizationRuleSetId(getNormalizationRuleSetId());
    job.setObservationTime(getObservationTime());
    job.setRedisConnector(getRedisConnector());
    job.setBaseDir(getBaseDir());
    job.run();
  }

}
