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

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.web.json.ValueSpecificationJSONBuilder;

/**
 * Writes an instance of {@link ValueSpecification} to JSON.
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class ValueSpecificationMessageBodyWriter implements MessageBodyWriter<ValueSpecification> {


  @Override
  public boolean isWriteable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
    return type.equals(ValueSpecification.class);
  }

  @Override
  public long getSize(ValueSpecification valueSpecification,
                      Class<?> aClass,
                      Type type,
                      Annotation[] annotations,
                      MediaType mediaType) {
    // TODO this means unknown size. is it worth encoding it twice to find out the size?
    return -1;
  }

  @Override
  public void writeTo(ValueSpecification valueSpecification,
                      Class<?> aClass,
                      Type type,
                      Annotation[] annotations,
                      MediaType mediaType,
                      MultivaluedMap<String, Object> stringObjectMultivaluedMap,
                      OutputStream outputStream) throws IOException, WebApplicationException {
    ValueSpecificationJSONBuilder jsonBuilder = new ValueSpecificationJSONBuilder();
    String valueSpecStr = jsonBuilder.toJSON(valueSpecification);
    outputStream.write(valueSpecStr.getBytes());
  }
}
