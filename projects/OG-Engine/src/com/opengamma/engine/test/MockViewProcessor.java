/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
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
import com.opengamma.livedata.UserPrincipal;

/**
 * Mock view processor
 */
public class MockViewProcessor implements ViewProcessor {

  private Map<String, View> _viewsByName = new ConcurrentHashMap<String, View>();
  
  public void addView(View view) {
    _viewsByName.put(view.getName(), view);
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
  public void reinitAsync() {
  }

}
