/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.client;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.view.View;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.util.ArgumentChecker;

/**
 * A simple in-memory implementation of the view processor client.
 * <p>
 * This client provides a simple wrapper around the underlying view processor.
 */
public class LocalViewProcessorClient implements ViewProcessorClient {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(LocalViewProcessorClient.class);

  /**
   * The view processor that this is a client for.
   */
  private final ViewProcessor _viewProcessor;

  /**
   * Creates the client wrapping a processor.
   * @param viewProcessor  the processor to wrap, not null
   */
  public LocalViewProcessorClient(ViewProcessor viewProcessor) {
    ArgumentChecker.notNull(viewProcessor, "View Processor");
    _viewProcessor = viewProcessor;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the view processor being wrapped.
   * @return the view processor, not null
   */
  public ViewProcessor getViewProcessor() {
    return _viewProcessor;
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
    View view = getOrInitializeView(viewName);
    return new LocalViewClient(view);
  }

  @Override
  public void startLiveCalculation(String viewName) {
    getOrInitializeView(viewName);  // force initialization
    getViewProcessor().startProcessing(viewName);
  }

  @Override
  public void stopLiveCalculation(String viewName) {
    getOrInitializeView(viewName);  // force initialization  // TODO 2010-05-18 SJC: seems a little odd
    getViewProcessor().stopProcessing(viewName);
  }

  // TODO 2010-03-29 Andrew -- this is a hack; both ends should have a ViewDefinitionRepository they should be referring to (or share one)
  public ViewDefinition getViewDefinition(String viewName) {
    return getViewProcessor().getViewDefinitionRepository().getDefinition(viewName);
  }

  /**
   * Attempts to get a view by name, initializing it if necessary.
   * @param viewName  the view name, not null
   * @return the view, not null
   */
  protected View getOrInitializeView(String viewName) {
    View view = getViewProcessor().getView(viewName);
    if (view == null) {
      s_logger.debug("No view available with name {}, initializing", viewName);
      view = getViewProcessor().initializeView(viewName);
    }
    assert view != null : "View should not be null without an exception thrown";
    return view;
  }

}
