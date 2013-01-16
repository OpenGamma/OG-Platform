/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.lazy;

import java.util.Collections;
import java.util.List;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.id.UniqueId;

/**
 * A portfolio node implementation that may not be fully resolved at construction but will appear fully resolved when used.
 */
public final class LazyResolvedPortfolioNode extends LazyResolvedObject<PortfolioNode> implements PortfolioNode {

  private static final long serialVersionUID = 1L;

  private volatile List<Position> _positions;
  private volatile List<PortfolioNode> _childNodes;

  public LazyResolvedPortfolioNode(final LazyResolveContext.AtVersionCorrection context, final PortfolioNode underlying) {
    super(context, underlying);
  }

  @Override
  public UniqueId getUniqueId() {
    return getUnderlying().getUniqueId();
  }

  @Override
  public UniqueId getParentNodeId() {
    return getUnderlying().getParentNodeId();
  }

  @Override
  public int size() {
    return getUnderlying().size();
  }

  @Override
  public List<PortfolioNode> getChildNodes() {
    if (_childNodes == null) {
      synchronized (this) {
        if (_childNodes == null) {
          final List<PortfolioNode> childNodes = getUnderlying().getChildNodes();
          if (childNodes.isEmpty()) {
            _childNodes = Collections.emptyList();
          } else {
            _childNodes = new LazyArrayList<PortfolioNode>(childNodes) {
              @Override
              public PortfolioNode resolve(final PortfolioNode childNode) {
                final PortfolioNode newChildNode = new LazyResolvedPortfolioNode(getLazyResolveContext(), childNode);
                getLazyResolveContext().cachePortfolioNode(newChildNode);
                return newChildNode;
              }
            };
          }
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
          final List<Position> positions = getUnderlying().getPositions();
          if (positions.isEmpty()) {
            _positions = Collections.emptyList();
          } else {
            _positions = new LazyArrayList<Position>(positions) {
              @Override
              public Position resolve(final Position position) {
                final Position newPosition = new LazyResolvedPosition(getLazyResolveContext(), position);
                getLazyResolveContext().cachePosition(newPosition);
                return newPosition;
              }
            };
          }
        }
      }
    }
    return _positions;
  }

  @Override
  public String getName() {
    return getUnderlying().getName();
  }

  @Override
  protected TargetResolverPortfolioNode targetResolverObject(final ComputationTargetResolver.AtVersionCorrection resolver) {
    return new TargetResolverPortfolioNode(resolver, this);
  }

  @Override
  protected SimplePortfolioNode simpleObject() {
    return new SimplePortfolioNode(this);
  }

}
