/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

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
