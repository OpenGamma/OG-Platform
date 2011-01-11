/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jaxrs;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.fudgemsg.FudgeMsgEnvelope;

/**
 * Register as a JAX-RS provider to support REST responses that are Fudge encoded messages. 
 */
@Provider
@Produces(FudgeRest.MEDIA)
public class FudgeBinaryProducer extends FudgeProducer {

  /**
   * Creates the producer.
   */
  public FudgeBinaryProducer() {
    super();
  }

  @Override
  public void writeTo(
      FudgeMsgEnvelope t,
      Class<?> type,
      Type genericType,
      Annotation[] annotations,
      MediaType mediaType,
      MultivaluedMap<String, Object> httpHeaders,
      OutputStream entityStream) throws IOException, WebApplicationException {
    getFudgeContext().createMessageWriter(entityStream).writeMessageEnvelope(t, getFudgeTaxonomyId());
  }

}
