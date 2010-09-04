/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */

package com.opengamma.financial.view.rest;


import static com.opengamma.financial.view.rest.ViewProcessorServiceNames.VIEW_ALLSECURITYTYPES;
import static com.opengamma.financial.view.rest.ViewProcessorServiceNames.VIEW_ALLVALUENAMES;
import static com.opengamma.financial.view.rest.ViewProcessorServiceNames.VIEW_COMPUTATIONRESULT;
import static com.opengamma.financial.view.rest.ViewProcessorServiceNames.VIEW_DELTARESULT;
import static com.opengamma.financial.view.rest.ViewProcessorServiceNames.VIEW_LIVECOMPUTATIONRUNNING;
import static com.opengamma.financial.view.rest.ViewProcessorServiceNames.VIEW_LIVE_DATA_INJECTOR;
import static com.opengamma.financial.view.rest.ViewProcessorServiceNames.VIEW_LATESTRESULT;
import static com.opengamma.financial.view.rest.ViewProcessorServiceNames.VIEW_NAME;
import static com.opengamma.financial.view.rest.ViewProcessorServiceNames.VIEW_PERFORMCOMPUTATION;
import static com.opengamma.financial.view.rest.ViewProcessorServiceNames.VIEW_PORTFOLIO;
import static com.opengamma.financial.view.rest.ViewProcessorServiceNames.VIEW_REQUIREMENTNAMES;
import static com.opengamma.financial.view.rest.ViewProcessorServiceNames.VIEW_REQUIRED_LIVE_DATA;
import static com.opengamma.financial.view.rest.ViewProcessorServiceNames.VIEW_RESULTAVAILABLE;
import static com.opengamma.financial.view.rest.ViewProcessorServiceNames.VIEW_STATUS;

import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.client.LocalViewClient;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.financial.livedata.rest.LiveDataInjectorResource;
import com.opengamma.util.ArgumentChecker;

/**
 * REST resource wrapper for ViewClient. These are transients created for the duration of a request.
 */
public class ViewResource {

  private static final Logger s_logger = LoggerFactory.getLogger(ViewResource.class);
  
  private final ViewProcessorResource _viewProcessor;
  private final ViewClient _viewClient;
  
  public ViewResource(final ViewProcessorResource viewProcessor, final ViewClient viewClient) {
    ArgumentChecker.notNull(viewProcessor, "View processor");
    ArgumentChecker.notNull(viewClient, "View client");
    _viewProcessor = viewProcessor;
    _viewClient = viewClient;
    s_logger.debug("created for {} by {}", viewClient, viewProcessor);
  }
  
  protected ViewProcessorResource getViewProcessor() {
    return _viewProcessor;
  }
  
  protected FudgeContext getFudgeContext() {
    return getViewProcessor().getFudgeContext();
  }
  
  protected FudgeSerializationContext getFudgeSerializationContext() {
    return new FudgeSerializationContext(getFudgeContext());
  }
  
  protected ViewClient getViewClient() {
    return _viewClient;
  }
  
  @GET
  @Path (VIEW_ALLSECURITYTYPES)
  public FudgeMsgEnvelope getAllSecurityTypes() {
    final FudgeSerializationContext context = getFudgeSerializationContext();
    final MutableFudgeFieldContainer msg = context.newMessage();
    context.objectToFudgeMsg(msg, VIEW_ALLSECURITYTYPES, null, getViewClient().getAllSecurityTypes());
    return new FudgeMsgEnvelope(msg);
  }
  
  @GET
  @Path (VIEW_ALLVALUENAMES)
  public FudgeMsgEnvelope getAllValueNames() {
    final FudgeSerializationContext context = getFudgeSerializationContext();
    final MutableFudgeFieldContainer msg = context.newMessage();
    context.objectToFudgeMsg(msg, VIEW_ALLVALUENAMES, null, getViewClient().getAllPortfolioRequirementNames());
    return new FudgeMsgEnvelope(msg);
  }
  
  @GET
  @Path (VIEW_LATESTRESULT)
  public FudgeMsgEnvelope getMostRecentResult() {
    final ViewComputationResultModel mostRecentResult = getViewClient().getLatestResult();
    if (mostRecentResult == null) {
      return null;
    }
    final FudgeSerializationContext context = getFudgeSerializationContext();
    final MutableFudgeFieldContainer msg = context.newMessage();
    context.objectToFudgeMsg(msg, VIEW_LATESTRESULT, null, mostRecentResult);
    return new FudgeMsgEnvelope(msg);
  }
  
  @GET
  @Path (VIEW_NAME)
  public FudgeMsgEnvelope getName() {
    final MutableFudgeFieldContainer msg = getFudgeContext().newMessage();
    msg.add(VIEW_NAME, getViewClient().getName());
    return new FudgeMsgEnvelope(msg);
  }
  
  @GET
  @Path (VIEW_PORTFOLIO)
  public FudgeMsgEnvelope getPortfolio() {
    final FudgeSerializationContext context = getFudgeSerializationContext();
    final MutableFudgeFieldContainer msg = context.newMessage();
    context.objectToFudgeMsg(msg, VIEW_PORTFOLIO, null, getViewClient().getPortfolio());
    return new FudgeMsgEnvelope(msg);
  }
  
  @GET
  @Path (VIEW_REQUIREMENTNAMES + "/{securityType}")
  public FudgeMsgEnvelope getRequirementNames(@PathParam("securityType") String securityType) {
    final Set<String> requirementNames = getViewClient().getRequirementNames(securityType);
    if (requirementNames == null) {
      return null;
    }
    final FudgeSerializationContext context = getFudgeSerializationContext();
    final MutableFudgeFieldContainer msg = context.newMessage();
    context.objectToFudgeMsg(msg, VIEW_REQUIREMENTNAMES, null, requirementNames);
    return new FudgeMsgEnvelope(msg);
  }
  
  @GET
  @Path(VIEW_REQUIRED_LIVE_DATA)
  public FudgeMsgEnvelope getRequiredLiveData() {
    Set<ValueRequirement> requiredLiveData = getViewClient().getRequiredLiveData();
    if (requiredLiveData == null) {
      return null;
    }
    FudgeSerializationContext context = getFudgeSerializationContext();
    MutableFudgeFieldContainer msg = context.newMessage();
    context.objectToFudgeMsg(msg, VIEW_REQUIRED_LIVE_DATA, null, requiredLiveData);
    return new FudgeMsgEnvelope(msg);
  }
  
  @GET
  @Path (VIEW_STATUS)
  public FudgeMsgEnvelope getStatus() {
    final MutableFudgeFieldContainer msg = getFudgeContext().newMessage();
    msg.add(VIEW_LIVECOMPUTATIONRUNNING, getViewClient().isLiveComputationRunning());
    msg.add(VIEW_RESULTAVAILABLE, getViewClient().isResultAvailable());
    return new FudgeMsgEnvelope(msg);
  }
  
  @GET
  @Path (VIEW_PERFORMCOMPUTATION)
  public void performComputation() {
    getViewClient().performComputation();
  }
  
  @GET
  @Path (VIEW_PERFORMCOMPUTATION + "/{snapshotTime}")
  public void performComputation(@PathParam("snapshotTime") long snapshotTime) {
    getViewClient().performComputation(snapshotTime);
  }
  
  @GET
  @Path (VIEW_COMPUTATIONRESULT)
  public FudgeMsgEnvelope getComputationResultChannel() {
    final MutableFudgeFieldContainer msg = getFudgeContext().newMessage();
    msg.add(VIEW_COMPUTATIONRESULT, getViewProcessor().getComputationResultChannel(getViewClient()));
    return new FudgeMsgEnvelope(msg);
  }
  
  @GET
  @Path (VIEW_DELTARESULT)
  public FudgeMsgEnvelope getDeltaResultChannel() {
    final MutableFudgeFieldContainer msg = getFudgeContext().newMessage();
    msg.add(VIEW_DELTARESULT, getViewProcessor().getDeltaResultChannel(getViewClient()));
    return new FudgeMsgEnvelope(msg);
  }
  
  @Path(VIEW_LIVE_DATA_INJECTOR)
  public LiveDataInjectorResource getCustomLiveDataInjector() {
    return new LiveDataInjectorResource(getViewClient().getLiveDataInjector());
  }
  
  // TODO 2010-03-29 Andrew -- this is a hack; both side should be sharing a ViewDefinitionRepository
  @GET
  @Path("viewDefinition")
  public FudgeMsgEnvelope getViewDefinition() {
    final FudgeSerializationContext context = getFudgeSerializationContext();
    final MutableFudgeFieldContainer msg = context.newMessage();
    context.objectToFudgeMsg(msg, "viewDefinition", null, ((LocalViewClient) getViewClient()).getView().getDefinition());
    return new FudgeMsgEnvelope(msg);
  }

}
