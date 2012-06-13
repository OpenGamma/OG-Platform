/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.rest.json;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

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
    //JSONObject jsonObject = new JSONObject(IOUtils.toString(new BufferedInputStream(entityStream)));
    return new ViewportSpecification();
  }
}
