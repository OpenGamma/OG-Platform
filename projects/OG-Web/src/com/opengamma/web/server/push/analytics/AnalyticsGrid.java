/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.DataNotFoundException;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
/* package */ abstract class AnalyticsGrid {

  protected final AnalyticsGridStructure _gridStructure;

  protected final Map<String, AnalyticsViewport> _viewports = new HashMap<String, AnalyticsViewport>();
  private final String _gridId;

  protected AnalyticsGrid(AnalyticsGridStructure gridStructure, String gridId) {
    ArgumentChecker.notNull(gridStructure, "gridStructure");
    ArgumentChecker.notNull(gridId, "gridId");
    _gridStructure = gridStructure;
    _gridId = gridId;
  }

  public AnalyticsGridStructure getGridStructure() {
    return _gridStructure;
  }

  protected AnalyticsViewport getViewport(String viewportId) {
    AnalyticsViewport viewport = _viewports.get(viewportId);
    if (viewport == null) {
      throw new DataNotFoundException("No viewport found with ID " + viewportId);
    }
    return viewport;
  }

  /* package */ String createViewport(String viewportId,
                                      String dataId,
                                      ViewportSpecification viewportSpecification,
                                      AnalyticsHistory history) {
    if (_viewports.containsKey(viewportId)) {
      throw new IllegalArgumentException("Viewport ID " + viewportId + " is already in use");
    }
    _viewports.put(viewportId, createViewport(_gridStructure, viewportSpecification, history, dataId));
    return viewportId;
  }

  protected abstract AnalyticsViewport createViewport(AnalyticsGridStructure gridStructure,
                                                      ViewportSpecification viewportSpecification,
                                                      AnalyticsHistory history,
                                                      String dataId);

  /* package */ void deleteViewport(String viewportId) {
    AnalyticsViewport viewport = _viewports.remove(viewportId);
    if (viewport == null) {
      throw new DataNotFoundException("No viewport found with ID " + viewportId);
    }
  }

  /* package */ ViewportResults getData(String viewportId) {
    return getViewport(viewportId).getData();
  }

  /* package */ String getGridId() {
    return _gridId;
  }

  /* package */ List<String> getViewportDataIds() {
    List<String> dataIds = new ArrayList<String>();
    for (AnalyticsViewport viewport : _viewports.values()) {
      dataIds.add(viewport.getDataId());
    }
    return dataIds;
  }
}
