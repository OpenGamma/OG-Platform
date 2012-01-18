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
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeDefinitionSource;
import com.opengamma.id.UniqueId;
import com.opengamma.util.jms.JmsConnector;

/**
 * RESTful resource for view processors.
 * <p>
 * This resource receives and processes RESTful calls.
 */
@Path("viewProcessors")
public class DataViewProcessorsResource {

  /**
   * The map of view processors.
   */
  private final Map<UniqueId, DataViewProcessorResource> _viewProcessorResourceMap;

  /**
   * Creates an instance.
   * 
   * @param viewProcessor  the view processor, not null
   * @param volatilityCubeDefinitionSource  the volatility cube, not null
   * @param jmsConnector  the JMS connector, not null
   * @param fudgeContext  the Fudge context, not null
   * @param scheduler  the scheduler, not null
   */
  public DataViewProcessorsResource(
      ViewProcessor viewProcessor,
      VolatilityCubeDefinitionSource volatilityCubeDefinitionSource,
      JmsConnector jmsConnector,
      FudgeContext fudgeContext,
      ScheduledExecutorService scheduler) {
    this(Collections.singleton(viewProcessor), volatilityCubeDefinitionSource, jmsConnector, fudgeContext, scheduler);
  }

  /**
   * Creates an instance.
   * 
   * @param viewProcessors  the view processor, not null
   * @param volatilityCubeDefinitionSource  the volatility cube, not null
   * @param jmsConnector  the JMS connector, not null
   * @param fudgeContext  the Fudge context, not null
   * @param scheduler  the scheduler, not null
   */
  public DataViewProcessorsResource(
      Collection<ViewProcessor> viewProcessors,
      VolatilityCubeDefinitionSource volatilityCubeDefinitionSource,
      JmsConnector jmsConnector,
      FudgeContext fudgeContext,
      ScheduledExecutorService scheduler) {
    Map<UniqueId, DataViewProcessorResource> viewProcessorResourceMap = new HashMap<UniqueId, DataViewProcessorResource>();
    for (ViewProcessor viewProcessor : viewProcessors) {
      if (viewProcessorResourceMap.get(viewProcessor.getUniqueId()) != null) {
        throw new OpenGammaRuntimeException("A view processor with the ID " + viewProcessor.getUniqueId() + " is already being managed");
      }
      viewProcessorResourceMap.put(viewProcessor.getUniqueId(),
          new DataViewProcessorResource(viewProcessor, volatilityCubeDefinitionSource, jmsConnector, fudgeContext, scheduler));
    }
    _viewProcessorResourceMap = Collections.unmodifiableMap(viewProcessorResourceMap);
  }

  //-------------------------------------------------------------------------
  @Path("{viewProcessorId}")
  public DataViewProcessorResource findViewProcessor(@PathParam("viewProcessorId") String viewProcessorId) {
    return _viewProcessorResourceMap.get(UniqueId.parse(viewProcessorId));
  }

  @GET
  public Response get() {
    return Response.ok(_viewProcessorResourceMap.keySet()).build();
  }

}
