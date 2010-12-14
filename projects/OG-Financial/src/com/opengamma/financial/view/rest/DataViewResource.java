/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.fudgemsg.FudgeContext;

import com.opengamma.engine.view.View;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.financial.livedata.rest.LiveDataInjectorResource;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.transport.jaxrs.FudgeRest;
import com.opengamma.transport.jms.JmsByteArrayMessageSenderService;
import com.opengamma.util.ArgumentChecker;

/**
 * RESTful resource for a {@link View}.
 */
public class DataViewResource {

  private final View _view;
  private final JmsByteArrayMessageSenderService _jmsMessageSenderService;
  private final String _jmsTopicPrefix;
  private final FudgeContext _fudgeContext;
  
  //CSOFF: just constants
  public static final String PATH_NAME = "name";
  public static final String PATH_DEFINITION = "definition";
  public static final String PATH_INIT = "init";
  public static final String PATH_PORTFOLIO = "portfolio";
  public static final String PATH_ACCESS_TO_LIVE_DATA = "hasAccessToLiveData";
  public static final String PATH_ALL_SECURITY_TYPES = "allSecurityTypes";
  public static final String PATH_ALL_PORTFOLIO_REQUIREMENT_NAMES = "allPortfolioRequirementNames";
  public static final String PATH_REQUIRED_LIVE_DATA = "requiredLiveData";
  public static final String PATH_LIVE_DATA_OVERRIDE_INJECTOR = "liveDataOverrideInjector";
  public static final String PATH_LATEST_RESULT = "latestResult";
  public static final String PATH_LIVE_COMPUTATION_RUNNING = "liveComputationRunning";
  
  public static final String PARAM_USER_IP = "userIp";
  public static final String PARAM_USER_NAME = "userName";
  //CSON: just constants
  
  /**
   * Creates the resource.
   * 
   * @param view  the underlying view
   * @param jmsMessageSenderService  the JMS message sender service
   * @param jmsTopicPrefix  a JMS topic prefix
   */
  public DataViewResource(View view, JmsByteArrayMessageSenderService jmsMessageSenderService, String jmsTopicPrefix, FudgeContext fudgeContext) {
    ArgumentChecker.notNull(view, "view");
    _view = view;
    _jmsMessageSenderService = jmsMessageSenderService;
    _jmsTopicPrefix = jmsTopicPrefix;
    _fudgeContext = fudgeContext;
  }
  
  //-------------------------------------------------------------------------
  @GET
  @Path(PATH_NAME)
  public Response getName() {
    return Response.ok(_view.getName()).type(MediaType.TEXT_PLAIN).build();
  }
  
  @GET
  @Path(PATH_DEFINITION)
  public Response getDefinition() {
    return Response.ok(_view.getDefinition()).build();
  }
  
  @POST
  @Path(PATH_INIT)
  public Response init() {
    _view.init();
    return Response.ok().build();
  }
  
  @GET
  @Path(PATH_PORTFOLIO)
  public Response getPortfolio() {
    return Response.ok(_view.getPortfolio()).build();
  }
  
  @POST
  @Path("clients")
  @Consumes(FudgeRest.MEDIA)
  public Response createClient(@Context UriInfo uriInfo, UserPrincipal user) {
    ViewClient client = _view.createClient(user);
    return Response.created(DataViewResource.uriClient(uriInfo.getRequestUri(), client.getUniqueIdentifier())).build();
  }
  
  @Path("clients/{viewClientId}")
  public DataViewClientResource getClient(@PathParam("viewClientId") String idStr) {
    UniqueIdentifier id = UniqueIdentifier.parse(idStr);
    return new DataViewClientResource(_view.getClient(id), _jmsMessageSenderService, _jmsTopicPrefix, _fudgeContext);
  }
  
  @POST
  @Path(PATH_ACCESS_TO_LIVE_DATA)
  public Response assertAccessToLiveDataRequirements(@QueryParam(PARAM_USER_NAME) String userName, @QueryParam(PARAM_USER_IP) String ipAddress) {
    UserPrincipal user = new UserPrincipal(userName, ipAddress);
    _view.assertAccessToLiveDataRequirements(user);
    return Response.ok().build();
  }
  
  @GET
  @Path(PATH_LATEST_RESULT)
  public Response getLatestResult() {
    return Response.ok(_view.getLatestResult()).build();
  }
  
  @GET
  @Path(PATH_REQUIRED_LIVE_DATA)
  public Response getRequiredLiveData() {
    return Response.ok(_view.getRequiredLiveData()).build();
  }
  
  @GET
  @Path(PATH_ALL_SECURITY_TYPES)
  public Response getAllSecurityTypes() {
    return Response.ok(_view.getAllSecurityTypes()).build();
  }
  
  @GET
  @Path(PATH_LIVE_COMPUTATION_RUNNING)
  public Response isLiveComputationRunning() {
    return Response.ok(_view.isLiveComputationRunning()).build();
  }
  
  @Path(PATH_LIVE_DATA_OVERRIDE_INJECTOR)
  public LiveDataInjectorResource getLiveDataOverrideInjector() {
    return new LiveDataInjectorResource(_view.getLiveDataOverrideInjector());
  }
  
  //-------------------------------------------------------------------------
  public static URI uriClient(URI baseUri, UniqueIdentifier uid) {
    return UriBuilder.fromUri(baseUri).segment(uid.toLatest().toString()).build();
  }
  
  public static URI uriClients(URI baseUri) {
    return UriBuilder.fromUri(baseUri).path("clients").build();
  }
  
}
