/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */

package com.opengamma.financial.view.rest;

import static com.opengamma.financial.view.rest.ViewProcessorServiceNames.VIEWPROCESSOR_AVAILABLEVIEWNAMES;
import static com.opengamma.financial.view.rest.ViewProcessorServiceNames.VIEWPROCESSOR_LIVECALCULATION_ACTION;
import static com.opengamma.financial.view.rest.ViewProcessorServiceNames.VIEWPROCESSOR_LIVECALCULATION_ACTION_START;
import static com.opengamma.financial.view.rest.ViewProcessorServiceNames.VIEWPROCESSOR_LIVECALCULATION_ACTION_STOP;
import static com.opengamma.financial.view.rest.ViewProcessorServiceNames.VIEWPROCESSOR_LIVECOMPUTATIONSUPPORTED;
import static com.opengamma.financial.view.rest.ViewProcessorServiceNames.VIEWPROCESSOR_LIVECOMPUTINGVIEWNAMES;
import static com.opengamma.financial.view.rest.ViewProcessorServiceNames.VIEWPROCESSOR_ONEOFFCOMPUTATIONSUPPORTED;

import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.client.ViewProcessorClient;
import com.opengamma.transport.jms.JmsByteArrayMessageSenderService;
import com.opengamma.util.ArgumentChecker;

/**
 * REST resource wrapper for a ViewProcessorClient. These are created by the service for the lifetime of the
 * underlying view processor.
 */
public class ViewProcessorResource {

  private static final Logger s_logger = LoggerFactory.getLogger(ViewProcessorResource.class);

  private final JmsByteArrayMessageSenderService _jmsByteArrayMessageSenderJob;
  private final String _jmsTopicPrefix;
  private final ViewProcessorClient _viewProcessorClient;
  private final FudgeContext _fudgeContext;

  // view name -> listener
  private final ConcurrentMap<String, ResultListener> _listeners = new ConcurrentHashMap<String, ResultListener>();

  public ViewProcessorResource(final JmsTemplate jmsTemplate, final String jmsTopicPrefix,
      final FudgeContext fudgeContext, final ViewProcessorClient viewProcessorClient) {
    ArgumentChecker.notNull(jmsTemplate, "JMS template");
    ArgumentChecker.notNull(jmsTopicPrefix, "JMS topic prefix");
    ArgumentChecker.notNull(fudgeContext, "Fudge context");
    ArgumentChecker.notNull(viewProcessorClient, "view processor client");
    _jmsByteArrayMessageSenderJob = new JmsByteArrayMessageSenderService(jmsTemplate);
    _jmsTopicPrefix = jmsTopicPrefix;
    _fudgeContext = fudgeContext;
    _viewProcessorClient = viewProcessorClient;
    s_logger.debug("created for {} with topicPrefix {}", viewProcessorClient, jmsTopicPrefix);
  }

  protected JmsByteArrayMessageSenderService getJmsByteArrayMessageSenderJob() {
    return _jmsByteArrayMessageSenderJob;
  }

  protected String getJmsTopicPrefix() {
    return _jmsTopicPrefix;
  }

  protected ViewProcessorClient getViewProcessorClient() {
    return _viewProcessorClient;
  }

  protected FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  protected FudgeSerializationContext getFudgeSerializationContext() {
    return new FudgeSerializationContext(getFudgeContext());
  }

  @GET
  @Path("supported")
  public FudgeMsgEnvelope getSupported() {
    final MutableFudgeFieldContainer msg = getFudgeContext().newMessage();
    msg.add(VIEWPROCESSOR_LIVECOMPUTATIONSUPPORTED, getViewProcessorClient().isLiveComputationSupported());
    msg.add(VIEWPROCESSOR_ONEOFFCOMPUTATIONSUPPORTED, getViewProcessorClient().isOneOffComputationSupported());
    return new FudgeMsgEnvelope(msg);
  }

  @GET
  @Path("availableViewNames")
  public FudgeMsgEnvelope getAvailableViewNames() {
    final FudgeSerializationContext context = getFudgeSerializationContext();
    final MutableFudgeFieldContainer msg = context.newMessage();
    context.objectToFudgeMsg(msg, VIEWPROCESSOR_AVAILABLEVIEWNAMES, null, getViewProcessorClient()
        .getAvailableViewNames());
    return new FudgeMsgEnvelope(msg);
  }

  @GET
  @Path("liveComputingViewNames")
  public FudgeMsgEnvelope getLiveComputingViewNames() {
    final FudgeSerializationContext context = getFudgeSerializationContext();
    final MutableFudgeFieldContainer msg = context.newMessage();
    context.objectToFudgeMsg(msg, VIEWPROCESSOR_LIVECOMPUTINGVIEWNAMES, null, getViewProcessorClient()
        .getLiveComputingViewNames());
    return new FudgeMsgEnvelope(msg);
  }

  @Path("view/{viewName}")
  public ViewResource getView(@PathParam("viewName") String viewName) {
    final ViewClient view;
    try {
      view = getViewProcessorClient().getView(viewName);
    } catch (NoSuchElementException e) {
      // cascade this as a 404
      return null;
    }
    return new ViewResource(this, view);
  }

  @PUT
  @Path("liveCalculation/{viewName}")
  public void putLiveCalculation(@PathParam("viewName") String viewName, final FudgeMsgEnvelope envelope) {
    try {
      final FudgeFieldContainer message = envelope.getMessage();
      final String action = message
          .getFieldValue(String.class, message.getByName(VIEWPROCESSOR_LIVECALCULATION_ACTION));
      if (action.equals(VIEWPROCESSOR_LIVECALCULATION_ACTION_START)) {
        getViewProcessorClient().startLiveCalculation(viewName);
      } else if (action.equals(VIEWPROCESSOR_LIVECALCULATION_ACTION_STOP)) {
        getViewProcessorClient().stopLiveCalculation(viewName);
      } else {
        throw new OpenGammaRuntimeException("unexpected action '" + action + "'");
      }
    } catch (NoSuchElementException e) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
  }

  private ConcurrentMap<String, ResultListener> getListeners() {
    return _listeners;
  }

  private ResultListener getOrCreateResultListener(final String viewName) {
    ResultListener resultListener = getListeners().get(viewName);
    if (resultListener == null) {
      ResultListener newListener = new ResultListener(this);
      resultListener = getListeners().putIfAbsent(viewName, newListener);
      if (resultListener == null) {
        resultListener = newListener;
      }
    }
    return resultListener;
  }

  protected String getComputationResultChannel(final ViewClient viewClient) {
    final String viewName = viewClient.getName();
    ResultListener resultListener = getOrCreateResultListener(viewName);
    return resultListener.getComputationResultChannel(viewClient);
  }

  protected String getDeltaResultChannel(final ViewClient viewClient) {
    final String viewName = viewClient.getName();
    ResultListener resultListener = getOrCreateResultListener(viewName);
    return resultListener.getDeltaResultChannel(viewClient);
  }

}
