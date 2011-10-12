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
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Partial implementation of {@link JSONBuilder}
 */
/* package */abstract class AbstractJSONBuilder<T> implements JSONBuilder<T> {
  
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractJSONBuilder.class);

  private final FudgeContext _fudgeContext = OpenGammaFudgeContext.getInstance();
  private final FudgeSerializer _serializer = new FudgeSerializer(_fudgeContext);
  private final FudgeDeserializer _deserializer = new FudgeDeserializer(_fudgeContext);

  /**
   * Gets the fudgeContext.
   * @return the fudgeContext
   */
  protected FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  /**
   * Gets the serializer.
   * @return the serializer
   */
  protected FudgeSerializer getSerializer() {
    return _serializer;
  }

  /**
   * Gets the deserializer.
   * @return the deserializer
   */
  protected FudgeDeserializer getDeserializer() {
    return _deserializer;
  }

  protected <E> E fromJSON(Class<E> clazz, String json) {
    FudgeMsg fudgeMsg = toFudgeMsg(json);
    return _deserializer.fudgeMsgToObject(clazz, fudgeMsg);
  }
  
  private FudgeMsg toFudgeMsg(final String json) {
    FudgeMsgJSONReader fudgeJSONReader = new FudgeMsgJSONReader(_fudgeContext, new StringReader(json));
    return fudgeJSONReader.readMessage();
  }

  protected String toJSON(final Object object, Class<?> clazz) {
    s_logger.debug("converting {} to JSON", object);
    
    MutableFudgeMsg fudgeMsg = getSerializer().objectToFudgeMsg(object);
    FudgeSerializer.addClassHeader(fudgeMsg, clazz);
    s_logger.debug("to fudgeMsg: {}", fudgeMsg);
    
    StringWriter sw = new StringWriter();
    FudgeMsgJSONWriter fudgeJSONWriter = new FudgeMsgJSONWriter(_fudgeContext, sw);
    fudgeJSONWriter.writeMessage(fudgeMsg);
    
    String result = sw.toString();
    s_logger.debug("to JSON: {}", result);
    return result;
  }

}
