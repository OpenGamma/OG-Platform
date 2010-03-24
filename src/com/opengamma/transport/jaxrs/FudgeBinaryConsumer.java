/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
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

import org.fudgemsg.FudgeMsgEnvelope;

/**
 * Register as a Jax-RS provider to support REST request payloads containing Fudge encoded messages.
 * 
 * @author Andrew Griffin
 */
@Consumes("application/vnd.fudgemsg")
public class FudgeBinaryConsumer extends FudgeBase implements MessageBodyReader<FudgeMsgEnvelope> {
  
  public FudgeBinaryConsumer () {
    super ();
  }
  
  @Override
  public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return type.isAssignableFrom (FudgeMsgEnvelope.class);
  }

  @Override
  public FudgeMsgEnvelope readFrom(Class<FudgeMsgEnvelope> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
    return getFudgeContext ().deserialize (entityStream);
  }
  
}