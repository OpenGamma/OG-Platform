/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.client;

import java.util.Timer;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.opengamma.engine.view.ComputationResultListener;
import com.opengamma.engine.view.DeltaComputationResultListener;
import com.opengamma.engine.view.View;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.livedata.msg.UserPrincipal;
import com.opengamma.util.ArgumentChecker;

/**
 * Provides client-oriented functionality on top of a {@link ViewProcessor}, including counting of listeners to
 * automatically start and stop processing as required.
 * <p>
 * When a client manager is being used on a particular view processor, it assumes complete control of the view
 * processor. The view processor should not be used directly elsewhere.
 */
public class ClientManager {

  private final Timer _timer;
  private final ViewProcessor _viewProcessor;
  
  private final Multiset<View> _listenerMap = HashMultiset.create();
  
  /**
   * Constructs an instance.
   * 
   * @param viewProcessor  the view processor which this client manager will control, not null 
   */
  public ClientManager(ViewProcessor viewProcessor) {
    this(viewProcessor, new Timer("Client update rate-limiting timer"));
  }
  
  /**
   * Constructs an instance.
   * 
   * @param viewProcessor  the view processor which this client manager will control, not null
   * @param timer  the timer used when limiting the rate of updates
   */
  public ClientManager(ViewProcessor viewProcessor, Timer timer) {
    ArgumentChecker.notNull(viewProcessor, "viewProcessor");
    _viewProcessor = viewProcessor;
    _timer = timer;
  }
  
  /**
   * Gets the view processor controlled by this client manager.
   * 
   * @return  the view processor, not null
   */
  public ViewProcessor getViewProcessor() {
    return _viewProcessor;
  }
  
  /**
   * Creates a new client instance. Each client can operate independently, regardless of the view that the client
   * references.
   *  
   * @param viewName  the name of the client's underlying view, not null
   * @param credentials  the user attempting to access the view, not null
   * @return  a new client which operates from the named view, not null
   */
  public Client createClient(String viewName, UserPrincipal credentials) {
    // Ensure sure the view exists and is accessible
    View view = getViewProcessor().getOrInitializeView(viewName, credentials);
    return new Client(this, view, credentials, _timer);
  }
  
  /*package*/ void addResultListener(Client client, ComputationResultListener listener) {
    View view = client.getView();
    if (view.addResultListener(listener)) {
      addListener(view);
    }
  }
  
  /*package*/ void removeResultListener(Client client, ComputationResultListener listener) {
    View view = client.getView();
    if (view.removeResultListener(listener)) {
      removeListener(view);
    }
  }
  
  /*package*/ void addDeltaResultListener(Client client, DeltaComputationResultListener listener) {
    View view = client.getView();
    if (view.addDeltaResultListener(listener)) {
      addListener(view);
    }
  }
  
  /*package*/ void removeDeltaResultListener(Client client, DeltaComputationResultListener listener) {
    View view = client.getView();
    if (view.removeDeltaResultLister(listener)) {
      removeListener(view);
    }
  }
  
  private void addListener(View view) {
    synchronized (_listenerMap) {
      if (_listenerMap.count(view) == 0) {
        getViewProcessor().startProcessing(view.getName());
      }
      _listenerMap.add(view);
    }
  }
  
  private void removeListener(View view) {
    synchronized (_listenerMap) {
      _listenerMap.remove(view);
      if (_listenerMap.count(view) == 0) {
        getViewProcessor().stopProcessing(view.getName());
      }
    }
  }
  
}
