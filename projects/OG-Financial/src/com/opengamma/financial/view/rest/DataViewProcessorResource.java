/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.fudgemsg.FudgeContext;
import org.springframework.jms.core.JmsTemplate;

import com.opengamma.engine.view.ViewProcess;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.transport.jaxrs.FudgeRest;
import com.opengamma.transport.jms.JmsByteArrayMessageSenderService;

/**
 * RESTful resource for a {@link ViewProcessor}.
 */
@Path("/data/viewProcessors/{viewProcessorId}")
public class DataViewProcessorResource {

  //CSOFF: just constants
  public static final String PATH_DEFINITION_REPOSITORY = "definitions";
  public static final String PATH_UNIQUE_ID = "id";
  public static final String PATH_CLIENTS = "clients";
  public static final String PATH_PROCESSES = "processes";
  public static final String PATH_CYCLES = "cycles";
  //CSON: just constants
  
  private final ViewProcessor _viewProcessor;
  private final JmsTemplate _jmsTemplate;
  private final String _jmsTopicPrefix;
  private final JmsByteArrayMessageSenderService _jmsMessageSenderService;
  private final ScheduledExecutorService _scheduler;
  
  private AtomicReference<DataViewCycleManagerResource> _cycleManagerResource = new AtomicReference<DataViewCycleManagerResource>();
  
  public DataViewProcessorResource(ViewProcessor viewProcessor, ActiveMQConnectionFactory connectionFactory, String jmsTopicPrefix, FudgeContext fudgeContext, ScheduledExecutorService scheduler) {
    _viewProcessor = viewProcessor;
    
    _jmsTemplate = new JmsTemplate(connectionFactory);
    _jmsTemplate.setPubSubDomain(true);
    
    _jmsMessageSenderService = new JmsByteArrayMessageSenderService(_jmsTemplate);
    _jmsTopicPrefix = jmsTopicPrefix;
    _scheduler = scheduler;
  }
  
  //-------------------------------------------------------------------------
  @GET
  @Path(PATH_UNIQUE_ID)
  public Response getUniqueId() {
    return Response.ok(_viewProcessor.getUniqueId()).build();
  }
  
  @Path(PATH_DEFINITION_REPOSITORY)
  public DataViewDefinitionRepositoryResource getViewDefinitionRepository() {
    return new DataViewDefinitionRepositoryResource(_viewProcessor.getViewDefinitionRepository());
  }
  
  //-------------------------------------------------------------------------
  @Path(PATH_PROCESSES + "/{viewProcessId}")
  public DataViewProcessResource getViewProcess(@PathParam("viewProcessId") String viewProcessId) {    
    ViewProcess view = _viewProcessor.getViewProcess(UniqueIdentifier.parse(viewProcessId));
    return new DataViewProcessResource(view);
  }
  
  //-------------------------------------------------------------------------
  @Path(PATH_CLIENTS + "/{viewClientId}")
  public DataViewClientResource getViewClient(@Context UriInfo uriInfo, @PathParam("viewClientId") String viewClientId) {
    ViewClient viewClient = _viewProcessor.getViewClient(UniqueIdentifier.parse(viewClientId));
    URI viewProcessorUri = getViewProcessorUri(uriInfo);
    DataViewCycleManagerResource cycleManagerResource = getOrCreateDataViewCycleManagerResource(viewProcessorUri);
    return new DataViewClientResource(viewClient, cycleManagerResource, _jmsMessageSenderService, _jmsTopicPrefix);
  }
  
  @POST
  @Path(PATH_CLIENTS)
  @Consumes(FudgeRest.MEDIA)
  public Response createViewClient(@Context UriInfo uriInfo, UserPrincipal user) {
    ViewClient client = _viewProcessor.createViewClient(user);
    return Response.created(uriClient(uriInfo.getRequestUri(), client.getUniqueId())).build();
  }
  
  //-------------------------------------------------------------------------
  @Path(PATH_CYCLES)
  public DataViewCycleManagerResource getViewCycleManager(@Context UriInfo uriInfo) {
    return getOrCreateDataViewCycleManagerResource(getViewProcessorUri(uriInfo));
  }
  
  //-------------------------------------------------------------------------
  public static URI uriViewProcess(URI baseUri, UniqueIdentifier viewProcessId) {
    // WARNING: '/' characters could well appear in the view name
    // There is a bug(?) in UriBuilder where, even though segment() is meant to treat the item as a single path segment
    // and therefore encode '/' characters, it does not encode '/' characters which come from a variable substitution.
    return UriBuilder.fromUri(baseUri).path("processes").segment(viewProcessId.toString()).build();
  }
  
  public static URI uriClient(URI clientsBaseUri, UniqueIdentifier viewClientId) {
    return UriBuilder.fromUri(clientsBaseUri).segment(viewClientId.toString()).build();
  }
  
  private URI getViewProcessorUri(UriInfo uriInfo) {
    return UriBuilder.fromUri(uriInfo.getMatchedURIs().get(1)).build();
  }
  
  private DataViewCycleManagerResource getOrCreateDataViewCycleManagerResource(URI viewProcessorUri) {
    DataViewCycleManagerResource resource = _cycleManagerResource.get();
    if (resource == null) {
      URI baseUri = UriBuilder.fromUri(viewProcessorUri).path(PATH_CYCLES).build();
      DataViewCycleManagerResource newResource = new DataViewCycleManagerResource(baseUri, _viewProcessor.getViewCycleManager());
      if (_cycleManagerResource.compareAndSet(null, newResource)) {
        resource = newResource;
        DataViewCycleManagerResource.ReleaseExpiredReferencesRunnable task = newResource.createReleaseExpiredReferencesTask();
        task.setScheduler(_scheduler);
      } else {
        resource = _cycleManagerResource.get();
      }
    }
    return resource;
  }
  
}
