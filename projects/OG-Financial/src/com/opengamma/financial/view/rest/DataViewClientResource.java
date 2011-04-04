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

import com.opengamma.engine.view.calc.ViewCycleReference;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.transport.jaxrs.FudgeRest;
import com.opengamma.transport.jms.JmsByteArrayMessageSenderService;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * RESTful resource for a view client.
 */
@Path("/data/viewprocessors/{viewProcessId}/clients/{viewClientId}")
@Consumes(FudgeRest.MEDIA)
@Produces(FudgeRest.MEDIA)
public class DataViewClientResource {

  //CSOFF: just constants
  public static final String PATH_UNIQUE_IDENTIFIER = "uniqueIdentifier";
  public static final String PATH_USER = "user";
  public static final String PATH_STATE = "state";
  public static final String PATH_IS_ATTACHED = "isAttached";
  public static final String PATH_ATTACH_SEARCH = "attachSearch";
  public static final String PATH_ATTACH_DIRECT = "attachDirect";
  public static final String PATH_DETACH = "detach";
  public static final String PATH_IS_BATCH_CONTROLLER = "isBatchController";
  public static final String PATH_RESUME = "resume";
  public static final String PATH_PAUSE = "pause";
  public static final String PATH_RESULT_AVAILABLE = "resultAvailable";
  public static final String PATH_LATEST_RESULT = "latestResult";
  public static final String PATH_LATEST_COMPILED_DEFINITION = "latestCompiledViewDefinition";
  public static final String PATH_VIEW_CYCLE_ACCESS_SUPPORTED = "viewCycleAccessSupported";
  public static final String PATH_CREATE_LATEST_CYCLE_REFERENCE = "createLatestCycleReference";
  public static final String PATH_SHUTDOWN = "shutdown";
  
  public static final String PATH_START_JMS_RESULT_STREAM = "startJmsResultStream";
  public static final String PATH_STOP_JMS_RESULT_STREAM = "endJmsResultStream";
  public static final String PATH_START_JMS_DELTA_STREAM = "startJmsDeltaStream";
  public static final String PATH_STOP_JMS_DELTA_STREAM = "endJmsDeltaStream";
  public static final String PATH_UPDATE_PERIOD = "updatePeriod";
  //CSON: just constants
  
  private final ViewClient _viewClient;
  private final DataViewCycleManagerResource _viewCycleManagerResource;
  private final JmsResultPublisher _resultPublisher;
  
  public DataViewClientResource(ViewClient viewClient, DataViewCycleManagerResource viewCycleManagerResource, JmsByteArrayMessageSenderService messageSenderService, String topicPrefix) {
    _viewClient = viewClient;
    _viewCycleManagerResource = viewCycleManagerResource;
    _resultPublisher = new JmsResultPublisher(viewClient, OpenGammaFudgeContext.getInstance(), topicPrefix,
        messageSenderService);
  }
  
  private ViewClient getViewClient() {
    return _viewClient;
  }
  
  //-------------------------------------------------------------------------
  @GET
  @Path(PATH_UNIQUE_IDENTIFIER)
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
    getViewClient().attachToViewProcess(request.getViewDefinitionName(), request.getExecutionOptions(),
        request.isNewBatchProcess());
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
  
  @GET
  @Path(PATH_IS_BATCH_CONTROLLER)
  public Response isBatchController() {
    return Response.ok(getViewClient().isBatchController()).build();
  }
  
  @POST
  @Path(PATH_START_JMS_RESULT_STREAM)
  public Response startResultStream() {
    return Response.ok(_resultPublisher.startPublishingResults()).build();
  }
  
  @POST
  @Path(PATH_STOP_JMS_RESULT_STREAM)
  public Response stopResultStream() {
    _resultPublisher.stopPublishingResults();
    return Response.ok().build();
  }
  
  @POST
  @Path(PATH_START_JMS_DELTA_STREAM)
  public Response startDeltaStream() {
    return Response.ok(_resultPublisher.startPublishingDeltas()).build();
  }
  
  @POST
  @Path(PATH_STOP_JMS_DELTA_STREAM)
  public Response endDeltaStream() {
    _resultPublisher.stopPublishingDeltas();
    return Response.ok().build();
  }
  
  @PUT
  @Path(PATH_UPDATE_PERIOD)
  public Response setUpdatePeriod(long periodMillis) {
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
  @Path(PATH_RESULT_AVAILABLE)
  public Response isResultAvailable() {
    return Response.ok(getViewClient().isResultAvailable()).build();
  }
  
  @GET
  @Path(PATH_LATEST_RESULT)
  public Response getLatestResult() {
    return Response.ok(getViewClient().getLatestResult()).build();
  }
  
  @Path(PATH_LATEST_COMPILED_DEFINITION)
  public DataCompiledViewDefinitionResource getLatestCompiledViewDefinition() {
    return new DataCompiledViewDefinitionResource(getViewClient().getLatestCompiledViewDefinition());
  }
  
  @GET
  @Path(PATH_VIEW_CYCLE_ACCESS_SUPPORTED)
  public Response isViewCycleAccessSupported() {
    return Response.ok(getViewClient().isViewCycleAccessSupported()).build();
  }
  
  @POST
  @Path(PATH_VIEW_CYCLE_ACCESS_SUPPORTED)
  public Response setViewCycleAccessSupported(boolean isViewCycleAccessSupported) {
    getViewClient().setViewCycleAccessSupported(isViewCycleAccessSupported);
    return Response.ok().build();
  }
  
  @Path(PATH_CREATE_LATEST_CYCLE_REFERENCE)
  public Response createLatestCycleReference() {
    ViewCycleReference reference = getViewClient().createLatestCycleReference();
    URI referenceUri = _viewCycleManagerResource.manageReference(reference);
    return Response.ok().location(referenceUri).build();
  }
  
  @POST
  @Path(PATH_SHUTDOWN)
  public Response shutdown() {
    getViewClient().shutdown();
    return Response.ok().build();
  }
  
}
