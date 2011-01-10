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

import org.fudgemsg.FudgeMsgEnvelope;

/**
 * Register as a JAX-RS provider to support REST request payloads containing XML encoded messages.
 * <p>
 * <b>This class isn't properly implemented as the Fudge library does not have an XML stream reader component yet.</b>
 */
@Provider
@Consumes(MediaType.APPLICATION_XML)
public class FudgeXMLConsumer extends FudgeBase implements MessageBodyReader<FudgeMsgEnvelope> {

  /**
   * Creates the consumer.
   */
  public FudgeXMLConsumer() {
    super();
  }

  @Override
  public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    // TODO 2010-04-24 Andrew -- implement this class properly when there is XML reading support in the Fudge library
    return false;
  }

  @Override
  public FudgeMsgEnvelope readFrom(
      Class<FudgeMsgEnvelope> type,
      Type genericType,
      Annotation[] annotations,
      MediaType mediaType,
      MultivaluedMap<String, String> httpHeaders,
      InputStream entityStream) throws IOException, WebApplicationException {
    // TODO 2010-04-24 Andrew -- implement this class properly when there is XML reading support in the Fudge library
    return null;
  }

}
