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
import com.opengamma.web.analytics.GridColumnGroups;
import com.opengamma.web.analytics.GridColumnsJsonWriter;

/**
 *
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
/* package */ public class GridColumnGroupsMessageBodyWriter implements MessageBodyWriter<GridColumnGroups> {

  private final GridColumnsJsonWriter _writer;

  public GridColumnGroupsMessageBodyWriter(GridColumnsJsonWriter writer) {
    ArgumentChecker.notNull(writer, "writer");
    _writer = writer;
  }

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return type.equals(GridColumnGroups.class);
  }

  @Override
  public long getSize(GridColumnGroups gridColumnGroups,
                      Class<?> type,
                      Type genericType,
                      Annotation[] annotations,
                      MediaType mediaType) {
    return -1;
  }

  @Override
  public void writeTo(GridColumnGroups gridColumnGroups,
                      Class<?> type,
                      Type genericType,
                      Annotation[] annotations,
                      MediaType mediaType,
                      MultivaluedMap<String, Object> httpHeaders,
                      OutputStream entityStream) throws IOException, WebApplicationException {
    String json = _writer.getJson(gridColumnGroups.getGroups());
    entityStream.write(json.getBytes());
  }
}
