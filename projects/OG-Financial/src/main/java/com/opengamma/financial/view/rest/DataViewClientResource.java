/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.fudgemsg.FudgeMsg;

import com.opengamma.engine.resource.EngineResourceReference;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.client.ViewClientState;
import com.opengamma.engine.view.client.ViewResultMode;
import com.opengamma.engine.view.cycle.ViewCycle;
import com.opengamma.financial.livedata.rest.DataLiveDataInjectorResource;
import com.opengamma.financial.rest.AbstractRestfulJmsResultPublisher;
import com.opengamma.id.UniqueId;
import com.opengamma.transport.jaxrs.FudgeRest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.jms.JmsConnector;

/**
 * RESTful resource for a view client.
 */
@Consumes(FudgeRest.MEDIA)
@Produces(FudgeRest.MEDIA)
public class DataViewClientResource extends AbstractRestfulJmsResultPublisher {

  //CSOFF: just constants
  public static final String PATH_UNIQUE_ID = "id";
  public static final String PATH_USER = "user";
  public static final String PATH_STATE = "state";
  public static final String PATH_IS_ATTACHED = "isAttached";
  public static final String PATH_ATTACH_SEARCH = "attachSearch";
  public static final String PATH_ATTACH_DIRECT = "attachDirect";
  public static final String PATH_DETACH = "detach";
  public static final String PATH_LIVE_DATA_OVERRIDE_INJECTOR = "overrides";
  public static final String PATH_RESULT_MODE = "resultMode";
  public static final String PATH_FRAGMENT_RESULT_MODE = "fragmentResultMode";
  public static final String PATH_RESUME = "resume";
  public static final String PATH_PAUSE = "pause";
  public static final String PATH_COMPLETED = "completed";
  public static final String PATH_RESULT_AVAILABLE = "resultAvailable";
  public static final String PATH_LATEST_RESULT = "latestResult";
  public static final String PATH_VIEW_DEFINITION = "viewDefinition";
  public static final String PATH_VIEW_PROCESS = "viewProcess";
  public static final String PATH_LATEST_COMPILED_VIEW_DEFINITION = "latestCompiledViewDefinition";
  public static final String PATH_VIEW_CYCLE_ACCESS_SUPPORTED = "viewCycleAccessSupported";
  public static final String PATH_CREATE_LATEST_CYCLE_REFERENCE = "createLatestCycleReference";
  public static final String PATH_CREATE_CYCLE_REFERENCE = "createCycleReference";
  public static final String PATH_TRIGGER_CYCLE = "triggerCycle";
  public static final String PATH_SET_MINIMUM_LOG_MODE = "logMode";

  public static final String PATH_UPDATE_PERIOD = "updatePeriod";
  
  public static final String UPDATE_PERIOD_FIELD = "updatePeriod";
  public static final String VIEW_CYCLE_ACCESS_SUPPORTED_FIELD = "isViewCycleAccessSupported";
  public static final String PATH_VIEW_PROCESS_CONTEXT_MAP = "viewProcessContextMap";
  //CSON: just constants
  
  private final ViewClient _viewClient;
  private final DataEngineResourceManagerResource<ViewCycle> _viewCycleManagerResource;

  public DataViewClientResource(ViewClient viewClient, DataEngineResourceManagerResource<ViewCycle> viewCycleManagerResource, JmsConnector jmsConnector, ExecutorService executor) {
    super(createJmsResultPublisher(viewClient, jmsConnector), executor);
    _viewClient = viewClient;
    _viewCycleManagerResource = viewCycleManagerResource;
  }

  private static ViewClientJmsResultPublisher createJmsResultPublisher(ViewClient viewClient, JmsConnector jmsConnector) {
    if (jmsConnector == null) {
      return null;
    } else {
      return new ViewClientJmsResultPublisher(viewClient, OpenGammaFudgeContext.getInstance(), jmsConnector);
    }
  }
  
  /*package*/ ViewClient getViewClient() {
    return _viewClient;
  }
  
  //-------------------------------------------------------------------------
  @Override
  protected boolean isTerminated() {
    return getViewClient().getState() == ViewClientState.TERMINATED;
  }

  @Override
  protected void expire() {
    shutdown();
  }
  
  //-------------------------------------------------------------------------
  @GET
  @Path(PATH_UNIQUE_ID)
  public Response getUniqueId() {
    updateLastAccessed();
    return responseOkObject(getViewClient().getUniqueId());
  }
  
  @GET
  @Path(PATH_USER)
  public Response getUser() {
    updateLastAccessed();
    return responseOkObject(getViewClient().getUser());
  }
  
  @GET
  @Path(PATH_STATE)
  public Response getState() {
    updateLastAccessed();
    return responseOkObject(getViewClient().getState());
  }
  
  //-------------------------------------------------------------------------
  @GET
  @Path(PATH_IS_ATTACHED)
  public Response isAttached() {
    updateLastAccessed();
    return responseOk(getViewClient().isAttached());
  }
  
  @POST
  @Consumes(FudgeRest.MEDIA)
  @Path(PATH_ATTACH_SEARCH)
  public Response attachToViewProcess(AttachToViewProcessRequest request) {
    updateLastAccessed();
    ArgumentChecker.notNull(request.getViewDefinitionId(), "viewDefinitionId");
    ArgumentChecker.notNull(request.getExecutionOptions(), "executionOptions");
    ArgumentChecker.notNull(request.isNewBatchProcess(), "isNewBatchProcess");
    getViewClient().attachToViewProcess(request.getViewDefinitionId(), request.getExecutionOptions(), request.isNewBatchProcess());
    return responseOk();
  }
  
  @POST
  @Consumes(FudgeRest.MEDIA)
  @Path(PATH_ATTACH_DIRECT)
  public Response attachToViewProcess(UniqueId viewProcessId) {
    updateLastAccessed();
    ArgumentChecker.notNull(viewProcessId, "viewProcessId");
    getViewClient().attachToViewProcess(viewProcessId);
    return responseOk();
  }
  
  @POST
  @Path(PATH_DETACH)
  public Response detachFromViewProcess() {
    updateLastAccessed();
    getViewClient().detachFromViewProcess();
    return responseOk();
  }
  
  @Path(PATH_LIVE_DATA_OVERRIDE_INJECTOR)
  public DataLiveDataInjectorResource getLiveDataOverrideInjector() {
    updateLastAccessed();
    return new DataLiveDataInjectorResource(getViewClient().getLiveDataOverrideInjector());
  }
  
  @GET
  @Path(PATH_VIEW_DEFINITION)
  public Response getLatestViewDefinition() {
    ViewDefinition result = getViewClient().getLatestViewDefinition();
    return responseOkObject(result);
  }
  
  @Path(PATH_VIEW_PROCESS)
  public DataViewProcessResource getViewProcess() {
    updateLastAccessed();
    return new DataViewProcessResource(getViewClient().getViewProcess());
  }
  
  //-------------------------------------------------------------------------
  @PUT
  @Path(PATH_UPDATE_PERIOD)
  @Consumes(FudgeRest.MEDIA)
  public Response setUpdatePeriod(FudgeMsg msg) {
    updateLastAccessed();
    long periodMillis = msg.getLong(UPDATE_PERIOD_FIELD);
    getViewClient().setUpdatePeriod(periodMillis);
    return responseOk();
  }

  //-------------------------------------------------------------------------
  @GET
  @Path(PATH_RESULT_MODE)
  public Response getResultMode() {
    updateLastAccessed();
    return responseOkObject(getViewClient().getResultMode());
  }
  
  @PUT
  @Path(PATH_RESULT_MODE)
  public Response setResultMode(ViewResultMode viewResultMode) {
    updateLastAccessed();
    getViewClient().setResultMode(viewResultMode);
    return responseOk();
  }

  //-------------------------------------------------------------------------
  @PUT
  @Path(PATH_VIEW_PROCESS_CONTEXT_MAP)
  public Response setViewProcessContextMap(Map<String, String> viewProcessContextMap) {
    updateLastAccessed();
    getViewClient().setViewProcessContextMap(viewProcessContextMap);
    return responseOk();
  }

  //-------------------------------------------------------------------------
  @GET
  @Path(PATH_FRAGMENT_RESULT_MODE)
  public Response getFragmentResultMode() {
    updateLastAccessed();
    return responseOkObject(getViewClient().getFragmentResultMode());
  }

  @PUT
  @Path(PATH_FRAGMENT_RESULT_MODE)
  public Response setFragmentResultMode(ViewResultMode viewResultMode) {
    updateLastAccessed();
    getViewClient().setFragmentResultMode(viewResultMode);
    return responseOk();
  }

  //-------------------------------------------------------------------------
  @POST
  @Path(PATH_PAUSE)
  public Response pause() {
    updateLastAccessed();
    getViewClient().pause();
    return responseOk();
  }
  
  @POST
  @Path(PATH_RESUME)
  public Response resume() {
    updateLastAccessed();
    getViewClient().resume();
    return responseOk();
  }
  
  @POST
  @Path(PATH_TRIGGER_CYCLE)
  public Response triggerCycle() {
    updateLastAccessed();
    getViewClient().triggerCycle();
    return responseOk();
  }
  
  @GET
  @Path(PATH_COMPLETED)
  public Response isCompleted() {
    updateLastAccessed();
    return responseOk(getViewClient().isCompleted());
  }
  
  @GET
  @Path(PATH_RESULT_AVAILABLE)
  public Response isResultAvailable() {
    updateLastAccessed();
    return responseOk(getViewClient().isResultAvailable());
  }
  
  @GET
  @Path(PATH_LATEST_RESULT)
  public Response getLatestResult() {
    updateLastAccessed();
    return responseOkObject(getViewClient().getLatestResult());
  }
  
  @GET
  @Path(PATH_LATEST_COMPILED_VIEW_DEFINITION)
  public Response getLatestCompiledViewDefinition() {
    updateLastAccessed();
    return responseOkObject(getViewClient().getLatestCompiledViewDefinition());
  }
  
  @GET
  @Path(PATH_VIEW_CYCLE_ACCESS_SUPPORTED)
  public Response isViewCycleAccessSupported() {
    updateLastAccessed();
    return responseOk(getViewClient().isViewCycleAccessSupported());
  }
  
  @POST
  @Path(PATH_VIEW_CYCLE_ACCESS_SUPPORTED)
  public Response setViewCycleAccessSupported(FudgeMsg msg) {
    updateLastAccessed();
    boolean isViewCycleAccessSupported = msg.getBoolean(VIEW_CYCLE_ACCESS_SUPPORTED_FIELD);
    getViewClient().setViewCycleAccessSupported(isViewCycleAccessSupported);
    return responseOk();
  }
  
  @POST
  @Path(PATH_CREATE_LATEST_CYCLE_REFERENCE)
  public Response createLatestCycleReference() {
    updateLastAccessed();
    EngineResourceReference<? extends ViewCycle> reference = getViewClient().createLatestCycleReference();
    return getReferenceResponse(reference);
  }
  
  @POST
  @Path(PATH_CREATE_CYCLE_REFERENCE)
  @Consumes(FudgeRest.MEDIA)
  public Response createCycleReference(UniqueId cycleId) {
    updateLastAccessed();
    EngineResourceReference<? extends ViewCycle> reference = getViewClient().createCycleReference(cycleId);
    return getReferenceResponse(reference);
  }

  private Response getReferenceResponse(EngineResourceReference<? extends ViewCycle> reference) {
    updateLastAccessed();
    if (reference == null) {
      return responseOkNoContent();
    }
    URI referenceUri = _viewCycleManagerResource.manageReference(reference);
    return responseCreated(referenceUri);
  }
  
  //-------------------------------------------------------------------------
  @POST
  @Path(PATH_SET_MINIMUM_LOG_MODE)
  @Consumes(FudgeRest.MEDIA)
  public Response setMinimumLogMode(SetMinimumLogModeRequest request) {
    updateLastAccessed();
    ArgumentChecker.notNull(request.getMinimumLogMode(), "minimumLogMode");
    ArgumentChecker.notNull(request.getTargets(), "targets");
    getViewClient().setMinimumLogMode(request.getMinimumLogMode(), request.getTargets());
    return responseOk();
  }
  
  //-------------------------------------------------------------------------
  @DELETE
  public void shutdown() {
    getViewClient().shutdown();
    stopResultStream();
  }
  
}
