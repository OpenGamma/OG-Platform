/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.client;

import static com.opengamma.engine.view.server.ViewProcessorServiceNames.VIEWPROCESSOR_AVAILABLEVIEWNAMES;
import static com.opengamma.engine.view.server.ViewProcessorServiceNames.VIEWPROCESSOR_LIVECALCULATION;
import static com.opengamma.engine.view.server.ViewProcessorServiceNames.VIEWPROCESSOR_LIVECALCULATION_ACTION;
import static com.opengamma.engine.view.server.ViewProcessorServiceNames.VIEWPROCESSOR_LIVECALCULATION_ACTION_START;
import static com.opengamma.engine.view.server.ViewProcessorServiceNames.VIEWPROCESSOR_LIVECALCULATION_ACTION_STOP;
import static com.opengamma.engine.view.server.ViewProcessorServiceNames.VIEWPROCESSOR_LIVECOMPUTATIONSUPPORTED;
import static com.opengamma.engine.view.server.ViewProcessorServiceNames.VIEWPROCESSOR_LIVECOMPUTINGVIEWNAMES;
import static com.opengamma.engine.view.server.ViewProcessorServiceNames.VIEWPROCESSOR_ONEOFFCOMPUTATIONSUPPORTED;
import static com.opengamma.engine.view.server.ViewProcessorServiceNames.VIEWPROCESSOR_SUPPORTED;
import static com.opengamma.engine.view.server.ViewProcessorServiceNames.VIEWPROCESSOR_VIEW;
import static com.opengamma.engine.view.server.ViewProcessorServiceNames.VIEW_NAME;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.springframework.jms.core.JmsTemplate;

import com.opengamma.transport.jaxrs.RestClient;
import com.opengamma.transport.jaxrs.RestTarget;

/**
 * Implementation of a ViewProcessorClient for working with a remote engine
 *
 * @author Andrew Griffin
 */
public class RemoteViewProcessorClient implements ViewProcessorClient {
  
  private final RestClient _restClient;
  
  private final RestTarget _targetAvailableViewNames;
  private final RestTarget _targetLiveComputingViewNames;
  private final RestTarget _targetSupported;
  private final RestTarget _targetLiveCalculation;
  private final RestTarget _targetViewBase;
  
  private final ConcurrentMap<String,RemoteViewClient> _remoteViewClients = new ConcurrentHashMap<String,RemoteViewClient> ();
  
  public RemoteViewProcessorClient (final FudgeContext fudgeContext, final RestTarget baseTarget) {
    _restClient = RestClient.getInstance (fudgeContext, null);
    _targetAvailableViewNames = baseTarget.resolve (VIEWPROCESSOR_AVAILABLEVIEWNAMES);
    _targetLiveComputingViewNames = baseTarget.resolve (VIEWPROCESSOR_LIVECOMPUTINGVIEWNAMES);
    _targetSupported = baseTarget.resolve (VIEWPROCESSOR_SUPPORTED);
    _targetLiveCalculation = baseTarget.resolveBase (VIEWPROCESSOR_LIVECALCULATION);
    _targetViewBase = baseTarget.resolveBase (VIEWPROCESSOR_VIEW);
  }
  
  protected FudgeContext getFudgeContext () {
    return getRestClient ().getFudgeContext ();
  }
  
  protected RestClient getRestClient () {
    return _restClient;
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public Set<String> getAvailableViewNames() {
    return getRestClient ().getSingleValueNotNull (Set.class, _targetAvailableViewNames, VIEWPROCESSOR_AVAILABLEVIEWNAMES);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Set<String> getLiveComputingViewNames() {
    return getRestClient ().getSingleValueNotNull (Set.class, _targetLiveComputingViewNames, VIEWPROCESSOR_LIVECOMPUTINGVIEWNAMES);
  }

  @Override
  public ViewClient getView(String viewName) {
    // asking the server for it's correct view name will validate the URI on object construction
    final RestTarget viewBase = _targetViewBase.resolveBase (viewName).resolve (VIEW_NAME);
    viewName = getRestClient ().getSingleValueNotNull (String.class, viewBase, VIEW_NAME);
    // we have the server's version of the name, so now lookup or create
    if (getRemoteViewClients ().containsKey (viewName)) {
      return getRemoteViewClients ().get (viewName);
    } else {
      synchronized (this) {
        RemoteViewClient viewClient = getRemoteViewClients ().get (viewName);
        if (viewClient == null) {
          viewClient = new RemoteViewClient (this, viewName, viewBase);
          getRemoteViewClients ().put (viewName, viewClient);
        }
        return viewClient;
      }
    }
  }
  
  protected ConcurrentMap<String,RemoteViewClient> getRemoteViewClients () {
    return _remoteViewClients;
  }
  
  @Override
  public boolean isLiveComputationSupported() {
    return getRestClient ().getSingleValueNotNull (Boolean.class, _targetSupported, VIEWPROCESSOR_LIVECOMPUTATIONSUPPORTED);
  }

  @Override
  public boolean isOneOffComputationSupported() {
    return getRestClient ().getSingleValueNotNull (Boolean.class, _targetSupported, VIEWPROCESSOR_ONEOFFCOMPUTATIONSUPPORTED);
  }
  
  protected void putLiveCalculation (final String action, final String viewName) {
    final MutableFudgeFieldContainer msg = getFudgeContext ().newMessage ();
    msg.add (VIEWPROCESSOR_LIVECALCULATION_ACTION, action);
    getRestClient ().put (_targetLiveCalculation.resolve (viewName), msg);
  }

  @Override
  public void startLiveCalculation(String viewName) {
    putLiveCalculation (VIEWPROCESSOR_LIVECALCULATION_ACTION_START, viewName);
  }

  @Override
  public void stopLiveCalculation(String viewName) {
    putLiveCalculation (VIEWPROCESSOR_LIVECALCULATION_ACTION_STOP, viewName);
  }
  
  protected JmsTemplate getJmsTemplate () {
    // TODO
    return null;
  }
  
}
