/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import static com.opengamma.financial.view.rest.ViewProcessorServiceNames.VIEWPROCESSOR_AVAILABLEVIEWNAMES;
import static com.opengamma.financial.view.rest.ViewProcessorServiceNames.VIEWPROCESSOR_LIVECALCULATION;
import static com.opengamma.financial.view.rest.ViewProcessorServiceNames.VIEWPROCESSOR_LIVECALCULATION_ACTION;
import static com.opengamma.financial.view.rest.ViewProcessorServiceNames.VIEWPROCESSOR_LIVECALCULATION_ACTION_START;
import static com.opengamma.financial.view.rest.ViewProcessorServiceNames.VIEWPROCESSOR_LIVECALCULATION_ACTION_STOP;
import static com.opengamma.financial.view.rest.ViewProcessorServiceNames.VIEWPROCESSOR_LIVECOMPUTATIONSUPPORTED;
import static com.opengamma.financial.view.rest.ViewProcessorServiceNames.VIEWPROCESSOR_LIVECOMPUTINGVIEWNAMES;
import static com.opengamma.financial.view.rest.ViewProcessorServiceNames.VIEWPROCESSOR_ONEOFFCOMPUTATIONSUPPORTED;
import static com.opengamma.financial.view.rest.ViewProcessorServiceNames.VIEWPROCESSOR_SUPPORTED;
import static com.opengamma.financial.view.rest.ViewProcessorServiceNames.VIEWPROCESSOR_VIEW;
import static com.opengamma.financial.view.rest.ViewProcessorServiceNames.VIEW_NAME;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.jms.ConnectionFactory;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;

import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.client.ViewProcessorClient;
import com.opengamma.livedata.msg.UserPrincipal;
import com.opengamma.transport.jaxrs.RestClient;
import com.opengamma.transport.jaxrs.RestRuntimeException;
import com.opengamma.transport.jaxrs.RestTarget;

/**
 * Implementation of a ViewProcessorClient for working with a remote engine
 */
public class RemoteViewProcessorClient implements ViewProcessorClient {
  
  private static final Logger s_logger = LoggerFactory.getLogger(RemoteViewProcessorClient.class);
  
  private final RestClient _restClient;
  
  private final RestTarget _targetAvailableViewNames;
  private final RestTarget _targetLiveComputingViewNames;
  private final RestTarget _targetSupported;
  private final RestTarget _targetLiveCalculation;
  private final RestTarget _targetViewBase;
  
  private final UserPrincipal _user;
  
  private final ConcurrentMap<String, RemoteViewClient> _remoteViewClients = new ConcurrentHashMap<String, RemoteViewClient>();
  
  private final JmsTemplate _jmsTemplate = new JmsTemplate();
  
  public RemoteViewProcessorClient(final FudgeContext fudgeContext, 
      final ConnectionFactory connectionFactory, 
      final RestTarget baseTarget,
      final UserPrincipal user) {
    _restClient = RestClient.getInstance(fudgeContext, null);
    _targetAvailableViewNames = baseTarget.resolve(VIEWPROCESSOR_AVAILABLEVIEWNAMES);
    _targetLiveComputingViewNames = baseTarget.resolve(VIEWPROCESSOR_LIVECOMPUTINGVIEWNAMES);
    _targetSupported = baseTarget.resolve(VIEWPROCESSOR_SUPPORTED);
    _targetLiveCalculation = baseTarget.resolveBase(VIEWPROCESSOR_LIVECALCULATION);
    _targetViewBase = baseTarget.resolveBase(VIEWPROCESSOR_VIEW);
    _user = user;
    getJmsTemplate().setConnectionFactory(connectionFactory);
    getJmsTemplate().setPubSubDomain(true);
  }
  
  protected FudgeContext getFudgeContext() {
    return getRestClient().getFudgeContext();
  }
  
  protected RestClient getRestClient() {
    return _restClient;
  }
  
  @Override
  public UserPrincipal getUser() {
    return _user;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Set<String> getAvailableViewNames() {
    return getRestClient().getSingleValueNotNull(Set.class, _targetAvailableViewNames, VIEWPROCESSOR_AVAILABLEVIEWNAMES);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Set<String> getLiveComputingViewNames() {
    return getRestClient().getSingleValueNotNull(Set.class, _targetLiveComputingViewNames, VIEWPROCESSOR_LIVECOMPUTINGVIEWNAMES);
  }

  @Override
  public ViewClient getView(String viewName) {
    // asking the server for its correct view name will validate the URI on object construction
    final RestTarget viewBase = _targetViewBase.resolveBase(viewName).resolve(VIEW_NAME);
    s_logger.debug("Attempting to validate remote view '{}' using target '{}'", viewName, viewBase.getURI());
    try {
      viewName = getRestClient().getSingleValueNotNull(String.class, viewBase, VIEW_NAME);
      s_logger.debug("Remote view '{}' validated", viewName);
    } catch (RestRuntimeException e) {
      if (e.getStatusCode() == 404) {
        // Not found
        s_logger.debug("Remote view '{}' does not exist", viewName);
        return null;
      }
      // A genuine problem
      s_logger.debug("Error validating remote view {}", viewName, e);
      throw e;
    }
    // we have the server's version of the name, so now lookup or create
    if (getRemoteViewClients().containsKey(viewName)) {
      return getRemoteViewClients().get(viewName);
    } else {
      synchronized (this) {
        RemoteViewClient viewClient = getRemoteViewClients().get(viewName);
        if (viewClient == null) {
          viewClient = new RemoteViewClient(this, viewName, viewBase, getUser());
          getRemoteViewClients().put(viewName, viewClient);
        }
        return viewClient;
      }
    }
  }
  
  // TODO 2010-03-29 Andrew -- this is a hack; both ends should have a ViewDefinitionRepository they should be referring to (or share one)
  public ViewDefinition getViewDefinition(String viewName) {
    return getRestClient().getSingleValueNotNull(ViewDefinition.class, _targetViewBase.resolveBase(viewName).resolve("viewDefinition"), "viewDefinition");
  }
  
  protected ConcurrentMap<String, RemoteViewClient> getRemoteViewClients() {
    return _remoteViewClients;
  }
  
  @Override
  public boolean isLiveComputationSupported() {
    return getRestClient().getSingleValueNotNull(Boolean.class, _targetSupported, VIEWPROCESSOR_LIVECOMPUTATIONSUPPORTED);
  }

  @Override
  public boolean isOneOffComputationSupported() {
    return getRestClient().getSingleValueNotNull(Boolean.class, _targetSupported, VIEWPROCESSOR_ONEOFFCOMPUTATIONSUPPORTED);
  }
  
  protected void putLiveCalculation(final String action, final String viewName) {
    final MutableFudgeFieldContainer msg = getFudgeContext().newMessage();
    msg.add(VIEWPROCESSOR_LIVECALCULATION_ACTION, action);
    getRestClient().put(_targetLiveCalculation.resolve(viewName), msg);
  }

  @Override
  public void startLiveCalculation(String viewName) {
    putLiveCalculation(VIEWPROCESSOR_LIVECALCULATION_ACTION_START, viewName);
  }

  @Override
  public void stopLiveCalculation(String viewName) {
    putLiveCalculation(VIEWPROCESSOR_LIVECALCULATION_ACTION_STOP, viewName);
  }
  
  protected JmsTemplate getJmsTemplate() {
    return _jmsTemplate;
  }
  
}
