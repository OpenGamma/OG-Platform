/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.livedata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.wire.FudgeMsgReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.PerSecurityReferenceDataResult;
import com.opengamma.bbg.ReferenceDataProvider;
import com.opengamma.bbg.ReferenceDataResult;

/**
 * A reference data provider which uses reference data logged by {@link LoggingReferenceDataProvider} as its source of
 * data. Requests for data which is not in the log cannot be satisfied. 
 */
public class LoggedReferenceDataProvider implements ReferenceDataProvider {

  private static final Logger s_logger = LoggerFactory.getLogger(LoggedReferenceDataProvider.class); 
  
  private final FudgeContext _fudgeContext;
  private final Map<String, ? extends FudgeMsg> _data;
  
  public LoggedReferenceDataProvider(FudgeContext fudgeContext, File inputFile) {
    Map<String, MutableFudgeMsg> dataMap = new ConcurrentHashMap<String, MutableFudgeMsg>();
    FudgeMsgReader reader = null;
    try {
      FileInputStream fis = new FileInputStream(inputFile);
      reader = fudgeContext.createMessageReader(fis);
      while (reader.hasNext()) {
        FudgeMsg msg = reader.nextMessage();
        LoggedReferenceData loggedData = fudgeContext.fromFudgeMsg(LoggedReferenceData.class, msg);
        addDataToMap(fudgeContext, dataMap, loggedData);
      }
    } catch (FileNotFoundException e) {
      throw new OpenGammaRuntimeException("Cannot open " + inputFile + " for reading");
    } finally {
      if (reader != null) {
        reader.close();
      }
    }
    reflectTickersToBUIDs(dataMap);
    //logAvailableData(dataMap);
    
    _fudgeContext = fudgeContext;
    _data = dataMap;
  }

  private void logAvailableData(Map<String, MutableFudgeMsg> dataMap) {
    if (!s_logger.isDebugEnabled()) {
      return;
    }
    
    StringBuilder sb = new StringBuilder("The following recorded reference data is available:\n");
    for (Map.Entry<String, MutableFudgeMsg> dataEntry : dataMap.entrySet()) {
      sb.append("\t").append(dataEntry.getKey()).append(": ").append(dataEntry.getValue()).append("\n");
    }
    s_logger.debug(sb.toString());
  }
  
  private void addDataToMap(FudgeContext fudgeContext, Map<String, MutableFudgeMsg> map, LoggedReferenceData loggedData) {
    MutableFudgeMsg securityData = map.get(loggedData.getSecurity());
    if (securityData == null) {
      securityData = fudgeContext.newMessage();
      map.put(loggedData.getSecurity(), securityData);
    }
    if (securityData.hasField(loggedData.getField())) {
      s_logger.warn("Skipping duplicate field " + loggedData.getField() + " for security " + loggedData.getSecurity());
      return;
    }
    securityData.add(loggedData.getField(), loggedData.getValue());
  }
  
  @Override
  public ReferenceDataResult getFields(Set<String> securities, Set<String> fields) {
    ReferenceDataResult result = new ReferenceDataResult();
    for (String security : securities) {
      PerSecurityReferenceDataResult securityResult = new PerSecurityReferenceDataResult(security);
      // Copy the requested fields across into a new message
      MutableFudgeMsg fieldData = _fudgeContext.newMessage();
      FudgeMsg allFieldData = _data.get(security);
      if (allFieldData != null) {
        for (String fieldName : fields) {
          Object fieldValue = allFieldData.getValue(fieldName);
          fieldData.add(fieldName, fieldValue);
        }
      }
      securityResult.setFieldData(fieldData);
      result.addResult(securityResult);
    }
    return result;
  }

  private static void reflectTickersToBUIDs(final Map<String, MutableFudgeMsg> data) {
    final Map<String, MutableFudgeMsg> extra = new HashMap<String, MutableFudgeMsg>();
    for (Map.Entry<String, MutableFudgeMsg> entry : data.entrySet()) {
      final String buid = entry.getValue().getString("ID_BB_UNIQUE");
      if (buid != null) {
        extra.put("/buid/" + buid, entry.getValue());
      }
    }
    data.putAll(extra);
  }

}
