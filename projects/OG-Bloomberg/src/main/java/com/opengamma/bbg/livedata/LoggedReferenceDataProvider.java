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
import java.util.concurrent.ConcurrentHashMap;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.wire.FudgeMsgReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.referencedata.ReferenceData;
import com.opengamma.bbg.referencedata.ReferenceDataProviderGetRequest;
import com.opengamma.bbg.referencedata.ReferenceDataProviderGetResult;
import com.opengamma.bbg.referencedata.impl.AbstractReferenceDataProvider;
import com.opengamma.util.ArgumentChecker;

/**
 * A reference data provider which uses reference data logged by {@link LoggingReferenceDataProvider}
 * as its source of data. Requests for data which is not in the log cannot be satisfied. 
 */
public class LoggedReferenceDataProvider extends AbstractReferenceDataProvider {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(LoggedReferenceDataProvider.class); 

  /**
   * The Fudge contxet.
   */
  private final FudgeContext _fudgeContext;
  /**
   * The map of data by security key.
   */
  private final Map<String, ? extends FudgeMsg> _data;

  /**
   * Creates an instance that reads from a file.
   * 
   * @param fudgeContext  the Fudge context, not null
   * @param inputFile  the input file, not null
   */
  public LoggedReferenceDataProvider(FudgeContext fudgeContext, File inputFile) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    ArgumentChecker.notNull(inputFile, "inputFile");
    _fudgeContext = fudgeContext;
    _data = loadFile(fudgeContext, inputFile);
    //logAvailableData(dataMap);
  }

  /**
   * Loads the input file.
   * 
   * @param fudgeContext  the Fudge context, not null
   * @param inputFile  the input file, not null
   */
  private static Map<String, ? extends FudgeMsg> loadFile(FudgeContext fudgeContext, File inputFile) {
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
    } catch (FileNotFoundException ex) {
      throw new OpenGammaRuntimeException("Cannot open " + inputFile + " for reading");
    } finally {
      if (reader != null) {
        reader.close();
      }
    }
    reflectTickersToBUIDs(dataMap);
    return dataMap;
  }

  /**
   * Add extra entries for BUIDs.
   * 
   * @param dataMap  the data map, not null
   */
  private static void reflectTickersToBUIDs(final Map<String, MutableFudgeMsg> dataMap) {
    final Map<String, MutableFudgeMsg> extra = new HashMap<String, MutableFudgeMsg>();
    for (Map.Entry<String, MutableFudgeMsg> entry : dataMap.entrySet()) {
      final String buid = entry.getValue().getString("ID_BB_UNIQUE");
      if (buid != null) {
        extra.put("/buid/" + buid, entry.getValue());
      }
    }
    dataMap.putAll(extra);
  }

//  private void logAvailableData(Map<String, MutableFudgeMsg> dataMap) {
//    if (!s_logger.isDebugEnabled()) {
//      return;
//    }
//    StringBuilder sb = new StringBuilder("The following recorded reference data is available:\n");
//    for (Map.Entry<String, MutableFudgeMsg> dataEntry : dataMap.entrySet()) {
//      sb.append("\t").append(dataEntry.getKey()).append(": ").append(dataEntry.getValue()).append("\n");
//    }
//    s_logger.debug(sb.toString());
//  }

  /**
   * Add data to the map.
   * 
   * @param fudgeContext  the Fudge context, not null
   * @param dataMap  the data map, not null
   * @param loggedData  the logged data, not null
   */
  private static void addDataToMap(FudgeContext fudgeContext, Map<String, MutableFudgeMsg> dataMap, LoggedReferenceData loggedData) {
    MutableFudgeMsg securityData = dataMap.get(loggedData.getSecurity());
    if (securityData == null) {
      securityData = fudgeContext.newMessage();
      dataMap.put(loggedData.getSecurity(), securityData);
    }
    if (securityData.hasField(loggedData.getField())) {
      s_logger.warn("Skipping duplicate field " + loggedData.getField() + " for security " + loggedData.getSecurity());
      return;
    }
    securityData.add(loggedData.getField(), loggedData.getValue());
  }

  //-------------------------------------------------------------------------
  @Override
  protected ReferenceDataProviderGetResult doBulkGet(ReferenceDataProviderGetRequest request) {
    ReferenceDataProviderGetResult result = new ReferenceDataProviderGetResult();
    for (String identifier : request.getIdentifiers()) {
      // copy the requested fields across into a new message
      MutableFudgeMsg fieldData = _fudgeContext.newMessage();
      FudgeMsg allFieldData = _data.get(identifier);
      if (allFieldData != null) {
        for (String fieldName : request.getFields()) {
          Object fieldValue = allFieldData.getValue(fieldName);
          fieldData.add(fieldName, fieldValue);
        }
      }
      ReferenceData refData = new ReferenceData(identifier, fieldData);
      result.addReferenceData(refData);
    }
    return result;
  }

}
