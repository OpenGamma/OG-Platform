/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.json;

import java.io.StringReader;
import java.io.StringWriter;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;

import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Partial implementation of {@link JSONBuilder}
 * 
 * @param <T> the config document parameter type
 */
public abstract class AbstractJSONBuilder<T> implements JSONBuilder<T> {
  
  private static final FudgeContext s_fudgeContext = OpenGammaFudgeContext.getInstance();
  
  protected <E> E fromJSON(Class<E> clazz, String json) {
    FudgeMsg fudgeMsg = toFudgeMsg(json);
    return new FudgeDeserializer(s_fudgeContext).fudgeMsgToObject(clazz, fudgeMsg);
  }
  
  private FudgeMsg toFudgeMsg(final String json) {
    FudgeMsgJSONReader fudgeJSONReader = new FudgeMsgJSONReader(s_fudgeContext, new StringReader(json));
    return fudgeJSONReader.readMessage();
  }

  public static String fudgeToJson(final Object configObj) {
    FudgeMsg fudgeMsg = s_fudgeContext.toFudgeMsg(configObj).getMessage();   
        
    StringWriter sw = new StringWriter();
    FudgeMsgJSONWriter fudgeJSONWriter = new FudgeMsgJSONWriter(s_fudgeContext, sw);
    fudgeJSONWriter.writeMessage(fudgeMsg);
    
    return sw.toString();
  }

}
