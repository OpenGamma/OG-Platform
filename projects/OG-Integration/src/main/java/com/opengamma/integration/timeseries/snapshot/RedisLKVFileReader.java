/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.timeseries.snapshot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.wire.FudgeMsgReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.monitor.OperationTimer;

/**
 * Reads the last known values for all fields from a file written from Redis lkv values 
 * at a specific observation time
 */
public class RedisLKVFileReader {

  private static final Logger s_logger = LoggerFactory.getLogger(RedisLKVFileReader.class);
  private static final FudgeContext s_fudgeContext = OpenGammaFudgeContext.getInstance();  
  
  private final File _inputFile;
  private final Map<String, Boolean> _dataFieldBlackList = Maps.newHashMap();
  private final Map<String, Boolean> _schemeBlackList = Maps.newHashMap();
  
  public RedisLKVFileReader(final File inputFile, final BlackList schemeBlackList, final BlackList dataFieldBlackList) {
    ArgumentChecker.notNull(inputFile, "input file");
    ArgumentChecker.notNull(schemeBlackList, "scheme black list");
    ArgumentChecker.notNull(dataFieldBlackList, "data field black list");
    
    _inputFile = inputFile;
    for (String dataField : dataFieldBlackList.getBlackList()) {
      _dataFieldBlackList.put(dataField.toUpperCase(), Boolean.TRUE);
    }
    for (String scheme : schemeBlackList.getBlackList()) {
      _schemeBlackList.put(scheme.toUpperCase(), Boolean.TRUE);
    }
  }
  
  /**
   * Gets the inputFile.
   * @return the inputFile
   */
  public File getInputFile() {
    return _inputFile;
  }

  public Map<ExternalId, Map<String, Double>> getLastKnownValues() {
    OperationTimer timer = new OperationTimer(s_logger, "Reading LKV from disk");
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(getInputFile());
    } catch (FileNotFoundException ex) {
      throw new OpenGammaRuntimeException("Error opening file " + getInputFile().getAbsolutePath(), ex);
    }
    Map<ExternalId, Map<String, Double>> ticks = Maps.newHashMap();
    FudgeMsgReader reader = s_fudgeContext.createMessageReader(fis);
    try {
      while (reader.hasNext()) {
        FudgeMsg message = reader.nextMessage();
        ExternalId securityId = ExternalId.parse(message.getString(RedisLKVFileWriter.SECURITY));
        if (_schemeBlackList.containsKey(securityId.getScheme().getName())) {
          continue;
        }
        FudgeMsg ticksMsg = message.getMessage(RedisLKVFileWriter.TICKS);
        Map<String, Double> secTicks = Maps.newHashMap();
        for (String fieldName : ticksMsg.getAllFieldNames()) {
          if (_dataFieldBlackList.containsKey(fieldName.toUpperCase())) {
            continue;
          }
          secTicks.put(fieldName, ticksMsg.getDouble(fieldName));
        }
        ticks.put(securityId, secTicks);
      }
    } finally {
      IOUtils.closeQuietly(fis);
    }
    timer.finished();
    return ticks;
  }
  
}
