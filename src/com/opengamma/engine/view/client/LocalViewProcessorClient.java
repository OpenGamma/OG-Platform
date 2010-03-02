/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.client;

import java.util.Set;

import com.opengamma.engine.view.View;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 * @author kirk
 */
public class LocalViewProcessorClient implements ViewProcessorClient {
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
    View view = getViewProcessor().getView(viewName);
    if(view == null) {
      return null;
    }
    return new LocalViewClient(view);
  }

  @Override
  public boolean isLiveComputationSupported() {
    return true;
  }

  @Override
  public boolean isOneOffComputationSupported() {
    return false;
  }

}
