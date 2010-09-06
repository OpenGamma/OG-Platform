/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.client;

import java.util.Set;

import com.opengamma.engine.view.View;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.ViewProcessorImpl;
import com.opengamma.livedata.msg.UserPrincipal;
import com.opengamma.util.ArgumentChecker;

/**
 * A simple in-memory implementation of the view processor client.
 * <p>
 * This client provides a simple wrapper around the underlying view processor.
 */
public class LocalViewProcessorClient implements ViewProcessorClient {

  /**
   * The view processor that this is a client for.
   */
  private final ViewProcessor _viewProcessor;
  
  /**
   * The user that this client is acting as.
   */
  private final UserPrincipal _user;

  /**
   * Creates the client wrapping a processor.
   * @param viewProcessor  the processor to wrap, not null
   * @param user the user that this client is acting as, not null
   */
  public LocalViewProcessorClient(ViewProcessor viewProcessor, UserPrincipal user) {
    ArgumentChecker.notNull(viewProcessor, "View Processor");
    ArgumentChecker.notNull(user, "User Credentials");
    _viewProcessor = viewProcessor;
    _user = user;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the view processor being wrapped.
   * @return the view processor, not null
   */
  public ViewProcessor getViewProcessor() {
    return _viewProcessor;
  }
  
  @Override
  public UserPrincipal getUser() {
    return _user;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean isLiveComputationSupported() {
    return true;
  }

  @Override
  public boolean isOneOffComputationSupported() {
    return false;
  }

  @Override
  public Set<String> getAvailableViewNames() {
    return getViewProcessor().getViewNames();
  }

  @Override
  public Set<String> getLiveComputingViewNames() {
    return getViewProcessor().getViewNames();
  }

  @Override
  public ViewClient getView(String viewName) {
    View view = getViewProcessor().getOrInitializeView(viewName, _user);
    return new LocalViewClient(view, getUser());
  }

  @Override
  public void startLiveCalculation(String viewName) {
    getViewProcessor().getOrInitializeView(viewName, _user);  // force initialization
    getViewProcessor().startProcessing(viewName);
  }

  @Override
  public void stopLiveCalculation(String viewName) {
    getViewProcessor().getOrInitializeView(viewName, _user);  // force initialization  // TODO 2010-05-18 SJC: seems a little odd
    getViewProcessor().stopProcessing(viewName);
  }

  // TODO 2010-03-29 Andrew -- this is a hack; both ends should have a ViewDefinitionRepository they should be referring to (or share one)
  public ViewDefinition getViewDefinition(String viewName) {
    return ((ViewProcessorImpl) getViewProcessor()).getViewDefinitionRepository().getDefinition(viewName);
  }

}
