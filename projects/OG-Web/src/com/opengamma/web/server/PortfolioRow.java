/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server;

import com.opengamma.core.position.Position;
import com.opengamma.engine.ComputationTargetSpecification;

public class PortfolioRow {

  private final int _depth;
  private final PortfolioRow _parentRow;
  private final ComputationTargetSpecification _target;
  private final Position _position;
  private final String _aggregateName;

  public PortfolioRow(int depth, PortfolioRow parentRow, ComputationTargetSpecification target, Position position, String aggregateName) {
    _depth = depth;
    _parentRow = parentRow;
    _target = target;
    _position = position;
    _aggregateName = aggregateName;
  }

  public int getDepth() {
    return _depth;
  }

  public PortfolioRow getParentRow() {
    return _parentRow;
  }

  public ComputationTargetSpecification getTarget() {
    return _target;
  }

  public Position getPosition() {
    return _position;
  }

  public String getAggregateName() {
    return _aggregateName;
  }
}
