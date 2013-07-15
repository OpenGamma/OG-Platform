/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.timeseries.snapshot;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.wire.FudgeMsgWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import com.google.common.collect.Lists;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.OpenGammaClock;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.monitor.OperationTimer;
import com.opengamma.util.redis.RedisConnector;

/**
 * Write last know values in Redis to disk 
 * <p>
 * output file will be <pre>baseDir/yyyy/mm/dd/observationTime.dump</pre>
 */
public class RedisLKVFileWriter implements Runnable {
  
  private static final Logger s_logger = LoggerFactory.getLogger(RedisLKVFileWriter.class);
  private static final FudgeContext s_fudgeContext = OpenGammaFudgeContext.getInstance();
  /**
   * Ticks field name
   */
  public static final String TICKS = "1";
  /**
   * Security field name
   */
  public static final String SECURITY = "2";
  
  private BlackList _dataFieldBlackList;
  private BlackList _schemeBlackList;
  private String _observationTime;
  private String _normalizationRuleSetId;
  private String _globalPrefix = "";
  private RedisConnector _redisConnector;
  private File _baseDir;
  
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
   * Gets the baseDir.
   * @return the baseDir
   */
  public File getBaseDir() {
    return _baseDir;
  }

  /**
   * Sets the baseDir.
   * @param baseDir  the baseDir
   */
  public void setBaseDir(File baseDir) {
    _baseDir = baseDir;
  }

  @Override
  public void run() {
    validateState();
    RedisLKVSnapshotter redisReader = new RedisLKVSnapshotter(getDataFieldBlackList(), getSchemeBlackList(), getNormalizationRuleSetId(), 
        getGlobalPrefix(), getRedisConnector());
    List<FudgeMsg> messages = toFudgeMsg(redisReader.getLastKnownValues());
    
    final File outputFile = getOutputFile();
    ensureParentDirectory(outputFile);
    OperationTimer timer = new OperationTimer(s_logger, "Writing LKV for {} securities to disk", messages.size());
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(outputFile);
      BufferedOutputStream bos = new BufferedOutputStream(fos, 4096);
      FudgeMsgWriter fmsw = s_fudgeContext.createMessageWriter(bos);
      for (FudgeMsg tick : messages) {
        fmsw.writeMessage(tick);
      }
      fmsw.flush();
    } catch (FileNotFoundException ex) {
      throw new OpenGammaRuntimeException("Could not open RedisLKVSnaphot file '" + outputFile.getAbsolutePath() + "'", ex);
    } finally {
      IOUtils.closeQuietly(fos);
    }
    timer.finished();
  }

  private void ensureParentDirectory(final File outputFile) {
    try {
      s_logger.debug("creating directory {}", outputFile.getParent());
      FileUtils.forceMkdir(outputFile.getParentFile());
      s_logger.debug("directory created");
    } catch (IOException ex) {
      throw new OpenGammaRuntimeException("Error creating directory " + outputFile.getParent(), ex);
    }
  }
  
  private List<FudgeMsg> toFudgeMsg(Map<ExternalId, Map<String, String>> redisLKV) {
    List<FudgeMsg> result = Lists.newArrayList();
    for (Entry<ExternalId, Map<String, String>> ticksEntry : redisLKV.entrySet()) {
      MutableFudgeMsg msg = s_fudgeContext.newMessage();
      ExternalId externalId = ticksEntry.getKey();
      msg.add(SECURITY, externalId.toString());
      msg.add(TICKS, ticksToFudgeMsg(ticksEntry.getValue()));
      result.add(msg);
    }
    return result;
  }

  public File getOutputFile() {
    LocalDate today = LocalDate.now(OpenGammaClock.getInstance());
    String dateStr = today.toString(DateTimeFormatter.ISO_LOCAL_DATE);
    String[] dateParts = StringUtils.split(dateStr, "-");
    String year = dateParts[0];
    String month = dateParts[1];
    String day = dateParts[2];
    String filename = getObservationTime() + ".dump";
    return FileUtils.getFile(getBaseDir(), year, month, day, filename);
  }

  private FudgeMsg ticksToFudgeMsg(Map<String, String> ticks) {
    MutableFudgeMsg ticksMsg = s_fudgeContext.newMessage();
    for (Entry<String, String> tickEntry : ticks.entrySet()) {
      ticksMsg.add(tickEntry.getKey(), Double.valueOf(tickEntry.getValue()));
    }
    return ticksMsg;
  }
  
  private void validateState() {
    ArgumentChecker.notNull(getNormalizationRuleSetId(), "normalization rule set Id");
    ArgumentChecker.notNull(getObservationTime(), "observation time");
    ArgumentChecker.notNull(getRedisConnector(), "redis connector");
    ArgumentChecker.notNull(getBaseDir(), "base directory");
  }

}
