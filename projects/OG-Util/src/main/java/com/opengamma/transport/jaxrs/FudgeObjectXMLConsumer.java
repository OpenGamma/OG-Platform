/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jaxrs;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeMessageBuilder;
import org.fudgemsg.wire.FudgeMsgReader;
import org.fudgemsg.wire.xml.FudgeXMLStreamReader;

import com.google.common.base.Charsets;

/**
 * A JAX-RS provider to convert RESTful responses to Fudge XML encoded messages.
 * <p>
 * This converts directly to Fudge from the RESTful resource without the need to manually
 * create the message in application code.
 */
@Provider
@Consumes(MediaType.APPLICATION_XML)
public class FudgeObjectXMLConsumer extends FudgeBase implements MessageBodyReader<Object> {

  /**
   * Creates the consumer.
   */
  public FudgeObjectXMLConsumer() {
    super();
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    FudgeMessageBuilder<?> builder = getFudgeContext().getObjectDictionary().getMessageBuilder(type);
    return type != String.class &&  // allow manually created JSON string to work
        ((builder != null && !builder.getClass().getCanonicalName().equals("org.fudgemsg.mapping.JavaBeanBuilder")) ||
          type == FudgeMsgEnvelope.class);
  }

  @Override
  public Object readFrom(
      Class<Object> type,
      Type genericType,
      Annotation[] annotations,
      MediaType mediaType,
      MultivaluedMap<String, String> httpHeaders,
      InputStream entityStream) throws IOException, WebApplicationException {
    
    InputStreamReader entityReader = new InputStreamReader(entityStream, Charsets.UTF_8);
    @SuppressWarnings("resource")  // wraps stream that cannot be closed here
    FudgeMsgReader reader = new FudgeMsgReader(new FudgeXMLStreamReader(getFudgeContext(), entityReader));
    FudgeMsg message = reader.nextMessage();
    if (message == null) {
      return null;
    }
    FudgeDeserializer deser = new FudgeDeserializer(getFudgeContext());
    return deser.fudgeMsgToObject(type, message);
  }

}
