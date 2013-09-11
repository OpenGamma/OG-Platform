/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.json;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.json.JSONArray;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.opengamma.web.analytics.ErrorInfo;

/**
 * Writes a list of {@link ErrorInfo} to JSON.
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class ErrorInfoMessageBodyWriter implements MessageBodyWriter<List<ErrorInfo>> {

  private static final String ERROR_MESSAGE = "errorMessage";
  private static final String ID = "id";

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    if (!(genericType instanceof ParameterizedType)) {
      return false;
    }
    ParameterizedType parameterizedType = (ParameterizedType) genericType;
    return (parameterizedType.getRawType().equals(List.class)) &&
        parameterizedType.getActualTypeArguments().length == 1 &&
        parameterizedType.getActualTypeArguments()[0].equals(ErrorInfo.class);
  }

  @Override
  public long getSize(List<ErrorInfo> errorInfo,
                      Class<?> type,
                      Type genericType,
                      Annotation[] annotations,
                      MediaType mediaType) {
    return -1; // unknown
  }

  @Override
  public void writeTo(List<ErrorInfo> errorInfos,
                      Class<?> type,
                      Type genericType,
                      Annotation[] annotations,
                      MediaType mediaType,
                      MultivaluedMap<String, Object> httpHeaders,
                      OutputStream entityStream) throws IOException, WebApplicationException {
    List<Map<String, Object>> errors = Lists.newArrayList();
    for (ErrorInfo errorInfo : errorInfos) {
      errors.add(ImmutableMap.<String, Object>of(ERROR_MESSAGE, errorInfo.getMessage(), ID, errorInfo.getId()));
    }
    entityStream.write(new JSONArray(errors).toString().getBytes());
  }
}
