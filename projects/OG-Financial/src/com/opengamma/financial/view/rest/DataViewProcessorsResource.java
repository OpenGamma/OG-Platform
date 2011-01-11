/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.fudgemsg.FudgeContext;

import com.opengamma.engine.view.ViewProcessor;

/**
 * RESTful back-end to provide access to view processors
 */
@Path("viewProcessor")
public class DataViewProcessorsResource {

  // CSOFF: just constants
  public static final String DEFAULT_VIEW_PROCESSOR_NAME = "0";
  // CSON: just constants
  
  private final Map<String, DataViewProcessorResource> _viewProcessorResourceMap = new HashMap<String, DataViewProcessorResource>();
  
  public DataViewProcessorsResource() {
  }

  public DataViewProcessorsResource(final ViewProcessor viewProcessor, final ActiveMQConnectionFactory connectionFactory, final String topicPrefix, FudgeContext fudgeContext) {
    addViewProcessor(DEFAULT_VIEW_PROCESSOR_NAME, viewProcessor, connectionFactory, topicPrefix, fudgeContext);
  }

  public DataViewProcessorsResource(final Map<String, ViewProcessor> viewProcessors, final ActiveMQConnectionFactory connectionFactory, final String topicPrefix, FudgeContext fudgeContext) {
    for (Map.Entry<String, ViewProcessor> viewProcessor : viewProcessors.entrySet()) {
      addViewProcessor(viewProcessor.getKey(), viewProcessor.getValue(), connectionFactory, topicPrefix, fudgeContext);
    }
  }

  public DataViewProcessorsResource(final Collection<ViewProcessor> viewProcessors, final ActiveMQConnectionFactory connectionFactory, final String topicPrefix, FudgeContext fudgeContext) {
    int i = 0;
    for (ViewProcessor viewProcessor : viewProcessors) {
      addViewProcessor(Integer.toString(i++), viewProcessor, connectionFactory, topicPrefix, fudgeContext);
    }
  }

  //-------------------------------------------------------------------------
  public void addViewProcessor(String name, ViewProcessor viewProcessor, ActiveMQConnectionFactory connectionFactory, String topicPrefix, FudgeContext fudgeContext) {
    _viewProcessorResourceMap.put(name, new DataViewProcessorResource(viewProcessor, connectionFactory, topicPrefix, fudgeContext));
  }
  
  //-------------------------------------------------------------------------
  @Path("{name}")
  public DataViewProcessorResource findViewProcessor(@PathParam("name") String name) {
    return _viewProcessorResourceMap.get(name);
  }
  
  @GET
  public Response get() {
    return Response.ok(_viewProcessorResourceMap.keySet()).build();
  }
  
}
