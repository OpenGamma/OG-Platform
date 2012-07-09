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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.server.push.analytics.AnalyticsColumnGroups;
import com.opengamma.web.server.push.analytics.AnalyticsColumnsJsonWriter;

/**
 *
 */
/* package */ public class AnalyticsColumnGroupsMessageBodyWriter implements MessageBodyWriter<AnalyticsColumnGroups> {

  private final AnalyticsColumnsJsonWriter _writer;

  public AnalyticsColumnGroupsMessageBodyWriter(AnalyticsColumnsJsonWriter writer) {
    ArgumentChecker.notNull(writer, "writer");
    _writer = writer;
  }

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return type.equals(AnalyticsColumnGroups.class);
  }

  @Override
  public long getSize(AnalyticsColumnGroups analyticsColumnGroups,
                      Class<?> type,
                      Type genericType,
                      Annotation[] annotations,
                      MediaType mediaType) {
    return -1;
  }

  @Override
  public void writeTo(AnalyticsColumnGroups analyticsColumnGroups,
                      Class<?> type,
                      Type genericType,
                      Annotation[] annotations,
                      MediaType mediaType,
                      MultivaluedMap<String, Object> httpHeaders,
                      OutputStream entityStream) throws IOException, WebApplicationException {
    String json = _writer.getJson(analyticsColumnGroups.getGroups());
    entityStream.write(json.getBytes());
  }
}
