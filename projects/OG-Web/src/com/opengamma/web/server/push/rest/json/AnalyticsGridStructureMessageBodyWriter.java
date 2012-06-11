/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.rest.json;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import com.opengamma.web.server.push.analytics.AnalyticsGridStructure;

/**
 * Writes an instance of {@link AnalyticsGridStructure} to JSON.
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class AnalyticsGridStructureMessageBodyWriter implements MessageBodyWriter<AnalyticsGridStructure> {

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return type.equals(AnalyticsGridStructure.class);
  }

  @Override
  public long getSize(AnalyticsGridStructure analyticsGridStructure,
                      Class<?> type,
                      Type genericType,
                      Annotation[] annotations,
                      MediaType mediaType) {
    // TODO this means unknown size. is it worth encoding it twice to find out the size?
    return -1;
  }

  @Override
  public void writeTo(AnalyticsGridStructure analyticsGridStructure,
                      Class<?> type,
                      Type genericType,
                      Annotation[] annotations,
                      MediaType mediaType,
                      MultivaluedMap<String, Object> httpHeaders,
                      OutputStream entityStream) throws IOException, WebApplicationException {
    entityStream.write("{\"msg\": \"hello world\"}".getBytes());
  }
}
