/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.test;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.opengamma.engine.view.View;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.livedata.msg.UserPrincipal;

/**
 * Mock view processor
 */
public class MockViewProcessor implements ViewProcessor {

  private Map<String, View> _viewsByName = new ConcurrentHashMap<String, View>();
  
  public void addView(View view) {
    _viewsByName.put(view.getName(), view);
  }
  
  @Override
  public View getOrInitializeView(String viewName, UserPrincipal credentials) {
    return _viewsByName.get(viewName);
  }

  @Override
  public View getView(String name, UserPrincipal credentials) {
    return _viewsByName.get(name);
  }

  @Override
  public Set<String> getViewNames() {
    return Collections.unmodifiableSet(_viewsByName.keySet());
  }

  @Override
  public void initializeView(String viewName) {
  }

  @Override
  public void startProcessing(String viewName) {
    _viewsByName.get(viewName).start();
  }

  @Override
  public void stopProcessing(String viewName) {
    _viewsByName.get(viewName).stop();
  }

  @Override
  public boolean isRunning(String viewName) {
    return _viewsByName.get(viewName).isRunning();
  }
  
}
