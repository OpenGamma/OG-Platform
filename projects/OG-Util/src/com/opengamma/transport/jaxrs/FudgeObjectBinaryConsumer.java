/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jaxrs;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;

/**
 * A JAX-RS provider to convert RESTful responses to Fudge binary encoded messages.
 * <p>
 * This converts directly to Fudge from the RESTful resource without the need to manually
 * create the message in application code.
 */
@Provider
@Consumes(FudgeRest.MEDIA)
public class FudgeObjectBinaryConsumer extends FudgeBase implements MessageBodyReader<Object> {

  /**
   * Creates the consumer.
   */
  public FudgeObjectBinaryConsumer() {
    super();
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return getFudgeContext().getObjectDictionary().getMessageBuilder(type) != null;
  }

  @Override
  public Object readFrom(
      Class<Object> type,
      Type genericType,
      Annotation[] annotations,
      MediaType mediaType,
      MultivaluedMap<String, String> httpHeaders,
      InputStream entityStream) throws IOException, WebApplicationException {
    FudgeFieldContainer message = getFudgeContext().createMessageReader(entityStream).nextMessage();
    if (message == null) {
      return null;
    }
    FudgeDeserializationContext deser = new FudgeDeserializationContext(getFudgeContext());
    return deser.fudgeMsgToObject(type, message);
  }

}
