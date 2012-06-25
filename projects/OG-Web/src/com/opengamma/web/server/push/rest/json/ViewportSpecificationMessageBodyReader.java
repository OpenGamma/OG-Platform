/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.rest.json;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.SortedSet;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.opengamma.web.server.push.analytics.ViewportSpecification;

/**
 *
 */
@Provider
@Consumes(MediaType.APPLICATION_JSON)
public class ViewportSpecificationMessageBodyReader implements MessageBodyReader<ViewportSpecification> {

  @Override
  public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return type.equals(ViewportSpecification.class);
  }

  @Override
  public ViewportSpecification readFrom(Class<ViewportSpecification> type,
                              Type genericType,
                              Annotation[] annotations,
                              MediaType mediaType,
                              MultivaluedMap<String, String> httpHeaders,
                              InputStream entityStream) throws IOException, WebApplicationException {
    List<Integer> rows = Lists.newArrayList();
    SortedSet<Integer> columns = Sets.newTreeSet();
    try {
      JSONObject jsonObject = new JSONObject(IOUtils.toString(new BufferedInputStream(entityStream)));
      JSONArray rowArray = jsonObject.getJSONArray("rows");
      for (int i = 0; i < rowArray.length(); i++) {
        rows.add(rowArray.getInt(i));
      }
      JSONArray columnArray = jsonObject.getJSONArray("columns");
      for (int i = 0; i < columnArray.length(); i++) {
        columns.add(columnArray.getInt(i));
      }
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to decode viewport specification", e);
    }
    // TODO JSON writer for ViewportSpecification that lives in the same package
    return new ViewportSpecification(rows, columns);
  }
}
