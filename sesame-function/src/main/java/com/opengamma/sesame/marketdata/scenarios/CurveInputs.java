/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata.scenarios;

import java.util.Set;

import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * Inputs used when building a curve, includes the node definitions and market data at each curve node.
 */
public class CurveInputs {

  private final Set<CurveNodeWithIdentifier> _nodes;
  private final SnapshotDataBundle _nodeData;

  /**
   * @param nodes the curve nodes
   * @param nodeData the market data for the curve nodes
   */
  public CurveInputs(Set<CurveNodeWithIdentifier> nodes, SnapshotDataBundle nodeData) {
    _nodes = ArgumentChecker.notNull(nodes, "nodes");
    _nodeData = ArgumentChecker.notNull(nodeData, "nodeData");
  }

  /**
   * @return the curve nodes
   */
  public Set<CurveNodeWithIdentifier> getNodes() {
    return _nodes;
  }

  /**
   * @return the market data for the curve nodes
   */
  public SnapshotDataBundle getNodeData() {
    return _nodeData;
  }
}
