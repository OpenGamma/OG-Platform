/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.DataNotFoundException;

/**
 *
 */
public class AnalyticsViewManager {

  private static final Logger s_logger = LoggerFactory.getLogger(AnalyticsViewManager.class);

  private final Map<String, AnalyticsView> _views = new ConcurrentHashMap<String, AnalyticsView>();

  public void createView(ViewRequest request, String viewId, String portfolioGridId, String primitivesGridId) {
    // TODO contact the view processor, create view client
    if (_views.containsKey(viewId)) {
      throw new IllegalArgumentException("View ID " + viewId + " is already in use");
    }
    _views.put(viewId, new SimpleAnalyticsView(request/* TODO view client arg */, portfolioGridId, primitivesGridId));
    s_logger.debug("Created new view with ID {}", viewId);
  }

  public void deleteView(String viewId) {
    AnalyticsView view = _views.remove(viewId);
    if (view == null) {
      throw new DataNotFoundException("No view found with ID " + viewId);
    }
    view.close();
  }

  public AnalyticsView getView(String viewId) {
    AnalyticsView view = _views.get(viewId);
    if (view == null) {
      throw new DataNotFoundException("No view found with ID " + viewId);
    }
    return view;
  }
}
