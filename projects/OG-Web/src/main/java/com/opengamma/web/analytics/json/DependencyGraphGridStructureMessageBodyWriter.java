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
import java.util.List;
import java.util.Map;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.json.JSONObject;

import com.google.common.collect.ImmutableMap;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.analytics.AnalyticsNodeJsonWriter;
import com.opengamma.web.analytics.DependencyGraphGridStructure;
import com.opengamma.web.analytics.GridColumnsJsonWriter;
import com.opengamma.web.analytics.PortfolioGridStructure;

/**
 * Writes an instance of {@link PortfolioGridStructure} to JSON.
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class DependencyGraphGridStructureMessageBodyWriter implements MessageBodyWriter<DependencyGraphGridStructure> {

  /** Field name for the JSON. */
  private static final String COLUMN_SETS = "columnSets";
  /** Field name for the JSON. */
  private static final String ROOT_NODE = "rootNode";
  /** Field name for the JSON. */
  private static final String CALC_CONFIG_NAME = "calcConfigName";
  /** Field name for the JSON. */
  @SuppressWarnings("unused")
  private static final String VALUE_REQUIREMENT = "valueRequirement";

  private final GridColumnsJsonWriter _writer;

  public DependencyGraphGridStructureMessageBodyWriter(GridColumnsJsonWriter writer) {
    ArgumentChecker.notNull(writer, "writer");
    _writer = writer;
  }

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return type.equals(DependencyGraphGridStructure.class);
  }

  @Override
  public long getSize(DependencyGraphGridStructure gridStructure,
                      Class<?> type,
                      Type genericType,
                      Annotation[] annotations,
                      MediaType mediaType) {
    // TODO this means unknown size. is it worth encoding it twice to find out the size?
    return -1;
  }

  @Override
  public void writeTo(DependencyGraphGridStructure gridStructure,
                      Class<?> type,
                      Type genericType,
                      Annotation[] annotations,
                      MediaType mediaType,
                      MultivaluedMap<String, Object> httpHeaders,
                      OutputStream entityStream) throws IOException, WebApplicationException {
    Object[] rootNode = AnalyticsNodeJsonWriter.getJsonStructure(gridStructure.getRootNode());
    List<Map<String, Object>> columns = _writer.getJsonStructure(gridStructure.getColumnStructure().getGroups());
    //ValueRequirementJSONBuilder jsonBuilder = new ValueRequirementJSONBuilder();
    //String valueReqStr = jsonBuilder.toJSON(gridStructure.getRootRequirement());
    //JSONObject valueReqJson;
    //try {
    //  // need to convert it to a JSON object instead of a string otherwise it will be inserted into the outer object
    //  // as an escaped string instead of a child object
    //  valueReqJson = new JSONObject(valueReqStr);
    //} catch (JSONException e) {
    //  throw new OpenGammaRuntimeException("Failed to convert ValueRequirement to JSON", e);
    //}
    String calcConfigName = gridStructure.getCalculationConfigurationName();
    ImmutableMap<String, Object> jsonMap = ImmutableMap.of(COLUMN_SETS, columns,
                                                           ROOT_NODE, rootNode,
                                                           CALC_CONFIG_NAME, calcConfigName/*,
                                                           VALUE_REQUIREMENT, valueReqJson*/);
    entityStream.write(new JSONObject(jsonMap).toString().getBytes());
  }
}
