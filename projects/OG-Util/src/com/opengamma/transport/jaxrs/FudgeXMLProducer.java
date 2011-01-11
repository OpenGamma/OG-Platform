/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jaxrs;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.FudgeMsgWriter;
import org.fudgemsg.xml.FudgeXMLStreamWriter;

/**
 * Register as a JAX-RS provider to support REST responses that are XML encoded messages. 
 */
@Provider
@Produces(MediaType.APPLICATION_XML)
public class FudgeXMLProducer extends FudgeProducer {

  /**
   * Creates the producer.
   */
  public FudgeXMLProducer() {
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
    final FudgeMsgWriter writer = new FudgeMsgWriter(new FudgeXMLStreamWriter(getFudgeContext(), new OutputStreamWriter(entityStream)));
    writer.writeMessageEnvelope(t, getFudgeTaxonomyId());
    writer.flush();
  }

}
