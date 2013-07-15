/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.lazy;

import java.util.List;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.id.UniqueId;

/**
 * A portfolio node implementation that defers to a target resolver for the component parts.
 */
public final class TargetResolverPortfolioNode extends TargetResolverObject implements PortfolioNode {

  private static final long serialVersionUID = 1L;

  private final UniqueId _uniqueId;
  private final UniqueId _parentNodeId;
  private final ComputationTargetSpecification[] _childNodeSpecs;
  private transient volatile List<PortfolioNode> _childNodes;
  private final ComputationTargetSpecification[] _positionSpecs;
  private transient volatile List<Position> _positions;
  private final String _name;

  public TargetResolverPortfolioNode(final ComputationTargetResolver.AtVersionCorrection targetResolver, final PortfolioNode copyFrom) {
    super(targetResolver);
    _uniqueId = copyFrom.getUniqueId();
    _parentNodeId = copyFrom.getParentNodeId();
    final List<PortfolioNode> childNodes = copyFrom.getChildNodes();
    _childNodeSpecs = new ComputationTargetSpecification[childNodes.size()];
    int i = 0;
    for (PortfolioNode childNode : childNodes) {
      _childNodeSpecs[i++] = ComputationTargetSpecification.of(childNode);
    }
    final List<Position> positions = copyFrom.getPositions();
    _positionSpecs = new ComputationTargetSpecification[positions.size()];
    i = 0;
    for (Position position : positions) {
      _positionSpecs[i++] = ComputationTargetSpecification.of(position);
    }
    _name = copyFrom.getName();
  }

  @Override
  public UniqueId getUniqueId() {
    return _uniqueId;
  }

  @Override
  public UniqueId getParentNodeId() {
    return _parentNodeId;
  }

  @Override
  public int size() {
    return _childNodeSpecs.length + _positionSpecs.length;
  }

  @Override
  public List<PortfolioNode> getChildNodes() {
    if (_childNodes == null) {
      synchronized (this) {
        if (_childNodes == null) {
          _childNodes = new TargetResolverList<PortfolioNode>(getTargetResolver(), _childNodeSpecs) {
            @Override
            protected PortfolioNode createObject(final ComputationTargetSpecification target) {
              return new LazyTargetResolverPortfolioNode(getTargetResolver(), target);
            }
          };
        }
      }
    }
    return _childNodes;
  }

  @Override
  public List<Position> getPositions() {
    if (_positions == null) {
      synchronized (this) {
        if (_positions == null) {
          _positions = new TargetResolverList<Position>(getTargetResolver(), _positionSpecs) {
            @Override
            protected Position createObject(final ComputationTargetSpecification target) {
              return new LazyTargetResolverPosition(getTargetResolver(), target);
            }
          };
        }
      }
    }
    return _positions;
  }

  @Override
  public String getName() {
    return _name;
  }

}
