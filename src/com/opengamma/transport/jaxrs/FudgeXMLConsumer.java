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

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;

/**
 * <p>Register as a Jax-RS provider to support REST request payloads containing XML encoded messages.</p>
 * 
 * <p><b>This class isn't properly implemented as the Fudge library does not have an XML stream reader component yet.</b></p>
 * 
 * @author Andrew Griffin
 */
@Consumes("application/xml")
public class FudgeXMLConsumer extends FudgeBase implements MessageBodyReader<FudgeMsgEnvelope> {
  
  public FudgeXMLConsumer () {
    super ();
  }
  
  @Override
  public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    // TODO 2010-04-24 Andrew -- implement this class properly when there is XML reading support in the Fudge library
    return false;
  }

  @Override
  public FudgeMsgEnvelope readFrom(Class<FudgeMsgEnvelope> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
    // TODO 2010-04-24 Andrew -- implement this class properly when there is XML reading support in the Fudge library
    return null;
  }
  
}