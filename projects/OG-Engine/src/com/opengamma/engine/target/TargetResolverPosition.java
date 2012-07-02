/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target;

import java.util.Collection;
import java.util.Map;

import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.id.UniqueId;

/**
 * A position implementation that defers to a target resolver for the component parts.
 */
/* package */class TargetResolverPosition extends TargetResolverPositionOrTrade implements Position {

  private final UniqueId _parentNodeId;
  private final ComputationTargetSpecification[] _tradeSpecs;
  private transient volatile Collection<Trade> _trades;
  private final Map<String, String> _attributes;

  public TargetResolverPosition(final ComputationTargetResolver targetResolver, final Position copyFrom) {
    super(targetResolver, copyFrom);
    _parentNodeId = copyFrom.getParentNodeId();
    final Collection<Trade> trades = copyFrom.getTrades();
    _tradeSpecs = new ComputationTargetSpecification[trades.size()];
    int i = 0;
    for (Trade trade : trades) {
      _tradeSpecs[i++] = new ComputationTargetSpecification(ComputationTargetType.TRADE, trade.getUniqueId());
    }
    _attributes = copyFrom.getAttributes();
  }

  @Override
  public UniqueId getParentNodeId() {
    return _parentNodeId;
  }

  @Override
  public Collection<Trade> getTrades() {
    if (_trades == null) {
      synchronized (this) {
        if (_trades == null) {
          _trades = new TargetResolverList<Trade>(getTargetResolver(), _tradeSpecs) {
            @Override
            protected Trade createObject(final ComputationTargetSpecification specification) {
              return new LazyTargetResolverTrade(getTargetResolver(), specification);
            }
          };
        }
      }
    }
    return _trades;
  }

  @Override
  public Map<String, String> getAttributes() {
    return _attributes;
  }

}
