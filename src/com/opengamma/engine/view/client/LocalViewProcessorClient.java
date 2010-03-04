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
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 * @author kirk
 */
public class LocalViewProcessorClient implements ViewProcessorClient {
  private static final Logger s_logger = LoggerFactory.getLogger(LocalViewProcessorClient.class);
  private final ViewProcessor _viewProcessor;
  
  public LocalViewProcessorClient(ViewProcessor viewProcessor) {
    ArgumentChecker.checkNotNull(viewProcessor, "View Processor");
    _viewProcessor = viewProcessor;
  }

  /**
   * @return the viewProcessor
   */
  public ViewProcessor getViewProcessor() {
    return _viewProcessor;
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
  
  protected View getOrInitializeView(String viewName) {
    View view = getViewProcessor().getView(viewName);
    if(view == null) {
      s_logger.debug("No view available with name {}, initializing.", viewName);
      view = getViewProcessor().initializeView(viewName);
    }
    assert view != null : "View should not be null without an exception thrown.";
    return view;
  }

  @Override
  public boolean isLiveComputationSupported() {
    return true;
  }

  @Override
  public boolean isOneOffComputationSupported() {
    return false;
  }

  @Override
  public void startLiveCalculation(String viewName) {
    // Ignore the return value. Just want to make sure initialized.
    getOrInitializeView(viewName);
    getViewProcessor().startProcessing(viewName);
  }

  @Override
  public void stopLiveCalculation(String viewName) {
    getOrInitializeView(viewName);
    getViewProcessor().stopProcessing(viewName);
  }

}
