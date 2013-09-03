/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
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

import org.json.JSONObject;

import com.google.common.collect.ImmutableMap;
import com.opengamma.web.analytics.ErrorInfo;

/**
 * Writes an instance of {@link ErrorInfo} to JSON.
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class ErrorInfoMessageBodyWriter implements MessageBodyWriter<ErrorInfo> {

  private static final String ERROR_MESSAGE = "errorMessage";

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return type.equals(ErrorInfo.class);
  }

  @Override
  public long getSize(ErrorInfo errorInfo,
                      Class<?> type,
                      Type genericType,
                      Annotation[] annotations,
                      MediaType mediaType) {
    return -1; // unknown
  }

  @Override
  public void writeTo(ErrorInfo errorInfo,
                      Class<?> type,
                      Type genericType,
                      Annotation[] annotations,
                      MediaType mediaType,
                      MultivaluedMap<String, Object> httpHeaders,
                      OutputStream entityStream) throws IOException, WebApplicationException {
    // TODO include more details? stack trace?
    entityStream.write(new JSONObject(ImmutableMap.of(ERROR_MESSAGE, errorInfo.getMessage())).toString().getBytes());
  }
}
