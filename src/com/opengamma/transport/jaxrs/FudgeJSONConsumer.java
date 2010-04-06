/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
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

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.FudgeMsgReader;
import org.fudgemsg.json.FudgeJSONStreamReader;

/**
 * Register as a Jax-RS provider to support REST request payloads containing JSON encoded messages. 
 * 
 * @author Andrew Griffin
 */
@Consumes("application/json")
public class FudgeJSONConsumer extends FudgeBase implements MessageBodyReader<FudgeMsgEnvelope> {
  
  public FudgeJSONConsumer () {
    super ();
  }
  
  @Override
  public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return type.isAssignableFrom (FudgeMsgEnvelope.class);
  }

  @Override
  public FudgeMsgEnvelope readFrom(Class<FudgeMsgEnvelope> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
    return (new FudgeMsgReader (new FudgeJSONStreamReader (getFudgeContext (), new InputStreamReader (entityStream)))).nextMessageEnvelope ();
  }
  
}