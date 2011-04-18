/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.fudgemsg.FudgeMsg;
import org.springframework.jms.core.JmsTemplate;

import com.opengamma.engine.view.calc.EngineResourceReference;
import com.opengamma.engine.view.calc.ViewCycle;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.financial.livedata.rest.LiveDataInjectorResource;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.transport.jaxrs.FudgeRest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * RESTful resource for a view client.
 */
@Path("/data/viewProcessors/{viewProcessId}/clients/{viewClientId}")
@Consumes(FudgeRest.MEDIA)
@Produces(FudgeRest.MEDIA)
public class DataViewClientResource {

  //CSOFF: just constants
  public static final String PATH_UNIQUE_ID = "id";
  public static final String PATH_USER = "user";
  public static final String PATH_STATE = "state";
  public static final String PATH_IS_ATTACHED = "isAttached";
  public static final String PATH_ATTACH_SEARCH = "attachSearch";
  public static final String PATH_ATTACH_DIRECT = "attachDirect";
  public static final String PATH_DETACH = "detach";
  public static final String PATH_LIVE_DATA_OVERRIDE_INJECTOR = "overrides";
  public static final String PATH_RESUME = "resume";
  public static final String PATH_PAUSE = "pause";
  public static final String PATH_COMPLETED = "completed";
  public static final String PATH_RESULT_AVAILABLE = "resultAvailable";
  public static final String PATH_LATEST_RESULT = "latestResult";
  public static final String PATH_LATEST_COMPILED_VIEW_DEFINITION = "latestCompiledViewDefinition";
  public static final String PATH_VIEW_CYCLE_ACCESS_SUPPORTED = "viewCycleAccessSupported";
  public static final String PATH_CREATE_LATEST_CYCLE_REFERENCE = "createLatestCycleReference";
  public static final String PATH_CREATE_CYCLE_REFERENCE = "createCycleReference";
  public static final String PATH_SHUTDOWN = "shutdown";
  
  public static final String PATH_START_JMS_COMPILATION_STREAM = "startJmsCompilationStream";
  public static final String PATH_STOP_JMS_COMPILATION_STREAM = "stopJmsCompilationStream";
  public static final String PATH_START_JMS_RESULT_STREAM = "startJmsResultStream";
  public static final String PATH_STOP_JMS_RESULT_STREAM = "stopJmsResultStream";
  public static final String PATH_START_JMS_DELTA_STREAM = "startJmsDeltaStream";
  public static final String PATH_STOP_JMS_DELTA_STREAM = "stopJmsDeltaStream";
  public static final String PATH_UPDATE_PERIOD = "updatePeriod";
  
  public static final String UPDATE_PERIOD_FIELD = "updatePeriod";
  public static final String VIEW_CYCLE_ACCESS_SUPPORTED_FIELD = "isViewCycleAccessSupported";
  //CSON: just constants
  
  private final ViewClient _viewClient;
  private final DataEngineResourceManagerResource<ViewCycle> _viewCycleManagerResource;
  private final JmsResultPublisher _resultPublisher;
  
  public DataViewClientResource(ViewClient viewClient,
      DataEngineResourceManagerResource<ViewCycle> viewCycleManagerResource, JmsTemplate jmsTemplate,
      String topicPrefix) {
    _viewClient = viewClient;
    _viewCycleManagerResource = viewCycleManagerResource;
    _resultPublisher = new JmsResultPublisher(viewClient, OpenGammaFudgeContext.getInstance(), topicPrefix, jmsTemplate);
  }
  
  private ViewClient getViewClient() {
    return _viewClient;
  }
  
  //-------------------------------------------------------------------------
  @GET
  @Path(PATH_UNIQUE_ID)
  public Response getUniqueId() {
    return Response.ok(getViewClient().getUniqueId()).build();
  }
  
  @GET
  @Path(PATH_USER)
  public Response getUser() {
    return Response.ok(getViewClient().getUser()).build();
  }
  
  @GET
  @Path(PATH_STATE)
  public Response getState() {
    return Response.ok(getViewClient().getState()).build();
  }
  
  //-------------------------------------------------------------------------
  @GET
  @Path(PATH_IS_ATTACHED)
  public Response isAttached() {
    return Response.ok(getViewClient().isAttached()).build();
  }
  
  @POST
  @Consumes(FudgeRest.MEDIA)
  @Path(PATH_ATTACH_SEARCH)
  public Response attachToViewProcess(AttachToViewProcessRequest request) {
    ArgumentChecker.notNull(request.getViewDefinitionName(), "viewDefinitionName");
    ArgumentChecker.notNull(request.getExecutionOptions(), "executionOptions");
    ArgumentChecker.notNull(request.isNewBatchProcess(), "isNewBatchProcess");
    getViewClient().attachToViewProcess(request.getViewDefinitionName(), request.getExecutionOptions(), request.isNewBatchProcess());
    return Response.ok().build();
  }
  
  @POST
  @Consumes(FudgeRest.MEDIA)
  @Path(PATH_ATTACH_DIRECT)
  public Response attachToViewProcess(UniqueIdentifier viewProcessId) {
    ArgumentChecker.notNull(viewProcessId, "viewProcessId");
    getViewClient().attachToViewProcess(viewProcessId);
    return Response.ok().build();
  }
  
  @POST
  @Path(PATH_DETACH)
  public Response detachFromViewProcess() {
    getViewClient().detachFromViewProcess();
    return Response.ok().build();
  }
  
  @Path(PATH_LIVE_DATA_OVERRIDE_INJECTOR)
  public LiveDataInjectorResource getLiveDataOverrideInjector() {
    return new LiveDataInjectorResource(getViewClient().getLiveDataOverrideInjector());
  }
  
  //-------------------------------------------------------------------------  
  @POST
  @Path(PATH_START_JMS_RESULT_STREAM)
  public Response startResultStream() {
    _resultPublisher.startPublishingResults();
    return Response.ok(_resultPublisher.getDestinationName()).build();
  }
  
  @POST
  @Path(PATH_STOP_JMS_RESULT_STREAM)
  public Response stopResultStream() {
    _resultPublisher.stopPublishingResults();
    return Response.ok().build();
  }
  
  //-------------------------------------------------------------------------
  @PUT
  @Path(PATH_UPDATE_PERIOD)
  @Consumes(FudgeRest.MEDIA)
  public Response setUpdatePeriod(FudgeMsg msg) {
    long periodMillis = msg.getLong(UPDATE_PERIOD_FIELD);
    getViewClient().setUpdatePeriod(periodMillis);
    return Response.ok().build();
  }

  //-------------------------------------------------------------------------
  @POST
  @Path(PATH_PAUSE)
  public Response pause() {
    getViewClient().pause();
    return Response.ok().build();
  }
  
  @POST
  @Path(PATH_RESUME)
  public Response resume() {
    getViewClient().resume();
    return Response.ok().build();
  }
  
  @GET
  @Path(PATH_COMPLETED)
  public Response isCompleted() {
    return Response.ok(getViewClient().isCompleted()).build();
  }
  
  @GET
  @Path(PATH_RESULT_AVAILABLE)
  public Response isResultAvailable() {
    return Response.ok(getViewClient().isResultAvailable()).build();
  }
  
  @GET
  @Path(PATH_LATEST_RESULT)
  public Response getLatestResult() {
    return Response.ok(getViewClient().getLatestResult()).build();
  }
  
  @GET
  @Path(PATH_LATEST_COMPILED_VIEW_DEFINITION)
  public Response getLatestCompiledViewDefinition() {
    return Response.ok(getViewClient().getLatestCompiledViewDefinition()).build();
  }
  
  //-------------------------------------------------------------------------
  @GET
  @Path(PATH_VIEW_CYCLE_ACCESS_SUPPORTED)
  public Response isViewCycleAccessSupported() {
    return Response.ok(getViewClient().isViewCycleAccessSupported()).build();
  }
  
  @POST
  @Path(PATH_VIEW_CYCLE_ACCESS_SUPPORTED)
  public Response setViewCycleAccessSupported(FudgeMsg msg) {
    boolean isViewCycleAccessSupported = msg.getBoolean(VIEW_CYCLE_ACCESS_SUPPORTED_FIELD);
    getViewClient().setViewCycleAccessSupported(isViewCycleAccessSupported);
    return Response.ok().build();
  }
  
  @POST
  @Path(PATH_CREATE_LATEST_CYCLE_REFERENCE)
  public Response createLatestCycleReference() {
    EngineResourceReference<? extends ViewCycle> reference = getViewClient().createLatestCycleReference();
    return getReferenceResponse(reference);
  }
  
  @POST
  @Path(PATH_CREATE_CYCLE_REFERENCE)
  @Consumes(FudgeRest.MEDIA)
  public Response createCycleReference(UniqueIdentifier cycleId) {
    EngineResourceReference<? extends ViewCycle> reference = getViewClient().createCycleReference(cycleId);
    return getReferenceResponse(reference);
  }

  private Response getReferenceResponse(EngineResourceReference<? extends ViewCycle> reference) {
    if (reference == null) {
      return Response.ok().status(Status.NO_CONTENT).build();
    }
    URI referenceUri = _viewCycleManagerResource.manageReference(reference);
    return Response.ok().location(referenceUri).build();
  }
  
  //-------------------------------------------------------------------------
  @POST
  @Path(PATH_SHUTDOWN)
  public Response shutdown() {
    getViewClient().shutdown();
    stopResultStream();
    return Response.ok().build();
  }
  
}
