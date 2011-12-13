/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.fudgemsg.FudgeContext;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.id.UniqueId;
import com.opengamma.util.jms.JmsConnector;

/**
 * RESTful back-end to provide access to view processors
 */
@Path("/data/viewProcessors")
public class DataViewProcessorsResource {

  private final Map<UniqueId, DataViewProcessorResource> _viewProcessorResourceMap;

  public DataViewProcessorsResource(final ViewProcessor viewProcessor, final JmsConnector jmsConnector, 
      FudgeContext fudgeContext, ScheduledExecutorService scheduler) {
    this(Collections.singleton(viewProcessor), jmsConnector, fudgeContext, scheduler);
  }

  public DataViewProcessorsResource(final Collection<ViewProcessor> viewProcessors,
      final JmsConnector jmsConnector, FudgeContext fudgeContext, ScheduledExecutorService scheduler) {
    Map<UniqueId, DataViewProcessorResource> viewProcessorResourceMap = new HashMap<UniqueId, DataViewProcessorResource>();
    for (ViewProcessor viewProcessor : viewProcessors) {
      if (viewProcessorResourceMap.get(viewProcessor.getUniqueId()) != null) {
        throw new OpenGammaRuntimeException("A view processor with the ID " + viewProcessor.getUniqueId() + " is already being managed");
      }
      viewProcessorResourceMap.put(viewProcessor.getUniqueId(),
          new DataViewProcessorResource(viewProcessor, jmsConnector, fudgeContext, scheduler));
    }
    _viewProcessorResourceMap = Collections.unmodifiableMap(viewProcessorResourceMap);
  }

  @Path("{viewProcessorId}")
  public DataViewProcessorResource findViewProcessor(@PathParam("viewProcessorId") String viewProcessorId) {
    return _viewProcessorResourceMap.get(UniqueId.parse(viewProcessorId));
  }
  
  @GET
  public Response get() {
    return Response.ok(_viewProcessorResourceMap.keySet()).build();
  }
  
}
