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
  
  private SchemeBlackList _schemeBlackList;
  
  private DataFieldBlackList _dataFieldBlackList;
    
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

  @Override
  protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
    RedisHtsSnapshotJob job = new RedisHtsSnapshotJob();
    job.setHistoricalTimeSeriesMaster(getHtsMaster());
    job.setDataSource(getDataSource());
    job.setNormalizationRuleSetId(getNormalizationRuleSetId());
    job.setObservationTime(getObservationTime());
    job.setRedisConnector(getRedisConnector());
    job.run();
  }

}
