/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.json;

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

import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.analytics.ViewportResults;
import com.opengamma.web.analytics.ViewportResultsJsonWriter;

/**
 * Writes an instance of {@link ViewportResults} to an HTTP response as JSON.
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class ViewportResultsMessageBodyWriter implements MessageBodyWriter<ViewportResults> {

  private final ViewportResultsJsonWriter _jsonWriter;

  public ViewportResultsMessageBodyWriter(ViewportResultsJsonWriter jsonWriter) {
    ArgumentChecker.notNull(jsonWriter, "jsonWriter");
    _jsonWriter = jsonWriter;
  }

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return type.equals(ViewportResults.class);
  }

  @Override
  public long getSize(ViewportResults results,
                      Class<?> type,
                      Type genericType,
                      Annotation[] annotations,
                      MediaType mediaType) {
    return -1;
  }

  @Override
  public void writeTo(ViewportResults results,
                      Class<?> type,
                      Type genericType,
                      Annotation[] annotations,
                      MediaType mediaType,
                      MultivaluedMap<String, Object> httpHeaders,
                      OutputStream entityStream) throws IOException, WebApplicationException {
    entityStream.write(_jsonWriter.getJson(results).getBytes());
  }
}
