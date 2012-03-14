/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.livedata;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.wire.FudgeMsgWriter;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.PerSecurityReferenceDataResult;
import com.opengamma.bbg.ReferenceDataProvider;
import com.opengamma.bbg.ReferenceDataResult;

/**
 * Wraps a {@link ReferenceDataProvider} to keep track of requests and responses, incrementally writing anything new
 * to a file. The intention is for this wrapper to be used for a period while certain reference data requests are made
 * (e.g. when a view starts being processed). Subsequently the data file can replace the 'real' source of reference
 * data for the same requests, by using {@link LoggedReferenceDataProvider}.
 */
public class LoggingReferenceDataProvider implements ReferenceDataProvider {
 
  private final ReferenceDataProvider _underlying;
  private final ConcurrentMap<String, Set<String>> _alreadyLogged = new ConcurrentHashMap<String, Set<String>>();
  private final FudgeContext _fudgeContext;
  private final FudgeMsgWriter _fudgeMsgWriter;
  
  public LoggingReferenceDataProvider(ReferenceDataProvider underlying, FudgeContext fudgeContext, File outputFile) {
    _underlying = underlying;
    _fudgeContext = fudgeContext;
    try {
      FileOutputStream fos = new FileOutputStream(outputFile);
      BufferedOutputStream bos = new BufferedOutputStream(fos, 4096);
      _fudgeMsgWriter = fudgeContext.createMessageWriter(bos);
    } catch (FileNotFoundException ex) {
      throw new OpenGammaRuntimeException("Cannot open " + outputFile + " for writing");
    }
  }
  
  @Override
  public ReferenceDataResult getFields(Set<String> securities, Set<String> fields) {   
    ReferenceDataResult result = _underlying.getFields(securities, fields);
    processResult(result, fields);
    return result;
  }
  
  private void processResult(ReferenceDataResult result, Set<String> fields) {
    for (String security : result.getSecurities()) {
      Set<String> freshFieldsLogged = new HashSet<String>();
      Set<String> fieldsLogged = _alreadyLogged.putIfAbsent(security, freshFieldsLogged);
      if (fieldsLogged == null) {
        fieldsLogged = freshFieldsLogged;
      }
      
      PerSecurityReferenceDataResult securityResult = result.getResult(security);
      synchronized (fieldsLogged) {
        for (String field : fields) {
          if (!fieldsLogged.contains(field)) {
            Object value = securityResult.getFieldData().getValue(field);
            log(security, field, value);
          }
        }
      }
    }
  }
  
  private void log(String security, String field, Object value) {
    LoggedReferenceData loggedReferenceData = new LoggedReferenceData(security, field, value);
    _fudgeMsgWriter.writeMessage(loggedReferenceData.toFudgeMsg(new FudgeSerializer(_fudgeContext)));
    _fudgeMsgWriter.flush();
  }

}
