/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.reports;

import com.opengamma.id.UniqueId;

import javax.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * Contains a snapshot of raw data from a view client.
 */
public class ViewportData {

  private final GridData _portfolioData;
  private final GridData _primitivesData;
  private final List<DependencyGraphGridData> _portfolioDependencyGraphData;
  private final List<DependencyGraphGridData> _primitivesDependencyGraphData;
  private final Instant _valuationTime;
  private final UniqueId _viewClientId;
  // TODO aggregator name?

  public ViewportData(GridData portfolioData,
                      GridData primitivesData,
                      List<DependencyGraphGridData> portfolioDependencyGraphData,
                      List<DependencyGraphGridData> primitivesDependencyGraphData,
                      Instant valuationTime,
                      UniqueId viewClientId) {
    _portfolioDependencyGraphData = portfolioDependencyGraphData;
    _primitivesDependencyGraphData = primitivesDependencyGraphData;
    _portfolioData = portfolioData;
    _primitivesData = primitivesData;
    _valuationTime = valuationTime;
    _viewClientId = viewClientId;
  }

  /**
   * @return An empty set of data
   */
  public static ViewportData empty(UniqueId viewClientId) {
    return new ViewportData(GridData.empty(),
                            GridData.empty(),
                            Collections.<DependencyGraphGridData>emptyList(),
                            Collections.<DependencyGraphGridData>emptyList(),
                            Instant.now(),
                            viewClientId);
  }

  /**
   * @return Data from the portfoio grid
   */
  public GridData getPortfolioData() {
    return _portfolioData;
  }

  /**
   * @return Data from the primitives grid
   */
  public GridData getPrimitivesData() {
    return _primitivesData;
  }

  /**
   * @return An empty list - dependency graphs don't support export of their raw data (yet)
   */
  public List<DependencyGraphGridData> getPortfolioDependencyGraphData() {
    return _portfolioDependencyGraphData;
  }

  /**
   * @return An empty list - dependency graphs don't support export of their raw data (yet)
   */
  public List<DependencyGraphGridData> getPrimitivesDependencyGraphData() {
    return _primitivesDependencyGraphData;
  }

  /**
   * @return The timestamp of the results used to build this data set
   */
  public Instant getValuationTime() {
    return _valuationTime;
  }

  /**
   * @return ID of the view client that calculated this data
   */
  public UniqueId getViewClientId() {
    return _viewClientId;
  }
}
