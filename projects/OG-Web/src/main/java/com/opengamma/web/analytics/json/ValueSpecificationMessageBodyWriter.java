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

import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.ImmutableMap;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.web.analytics.ValueSpecificationTargetForCell;
import com.opengamma.web.json.ValueSpecificationJSONBuilder;

/**
 * Writes an instance of {@link ValueSpecification} to JSON.
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class ValueSpecificationMessageBodyWriter implements MessageBodyWriter<ValueSpecificationTargetForCell> {


  @Override
  public boolean isWriteable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
    return type.equals(ValueSpecificationTargetForCell.class);
  }

  @Override
  public long getSize(ValueSpecificationTargetForCell stringValueSpecificationPair,
                      Class<?> aClass,
                      Type type,
                      Annotation[] annotations,
                      MediaType mediaType) {
    // TODO this means unknown size. is it worth encoding it twice to find out the size?
    return -1;
  }

  @Override
  public void writeTo(ValueSpecificationTargetForCell valueSpec,
                      Class<?> aClass,
                      Type type,
                      Annotation[] annotations,
                      MediaType mediaType,
                      MultivaluedMap<String, Object> stringObjectMultivaluedMap,
                      OutputStream outputStream) throws IOException, WebApplicationException {
    ValueSpecificationJSONBuilder jsonBuilder = new ValueSpecificationJSONBuilder();
    String valueSpecStr = jsonBuilder.toJSON(valueSpec.getValuleSpecification());

    JSONObject valueReqJson;
    try {
      // need to convert it to a JSON object instead of a string otherwise it will be inserted into the outer object
      // as an escaped string instead of a child object
      valueReqJson = new JSONObject(valueSpecStr);
    } catch (JSONException e) {
      throw new OpenGammaRuntimeException("Failed to convert ValueRequirement to JSON", e);
    }
    ImmutableMap<String, Object> jsonMap = ImmutableMap.of("columnSet", valueSpec.getColumnSet(),
                                                           "valueSpecification", valueReqJson);
    outputStream.write(new JSONObject(jsonMap).toString().getBytes());
  }

}
