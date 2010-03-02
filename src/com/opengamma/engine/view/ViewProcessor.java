/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 * @author kirk
 */
public class ViewProcessor {
  @SuppressWarnings("unused")
  private static final Logger s_logger = LoggerFactory.getLogger(ViewProcessor.class);
  private final Map<String, View> _viewsByName = new ConcurrentHashMap<String, View>();
  
  public void addView(View view) {
    ArgumentChecker.checkNotNull(view, "View");
    _viewsByName.put(view.getDefinition().getName(), view);
  }
  
  public Set<String> getViewNames() {
    return Collections.unmodifiableSet(_viewsByName.keySet());
  }
  
  public View getView(String name) {
    return _viewsByName.get(name);
  }

}
