/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.lazy;

import java.util.Collection;
import java.util.Map;

import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;

/**
 * A position implementation that defers to a target resolver for the component parts.
 */
public class TargetResolverPosition extends TargetResolverPositionOrTrade implements Position {

  private final ComputationTargetSpecification[] _tradeSpecs;
  private transient volatile Collection<Trade> _trades;
  private final Map<String, String> _attributes;

  public TargetResolverPosition(final ComputationTargetResolver.AtVersionCorrection targetResolver, final Position copyFrom) {
    super(targetResolver, copyFrom);
    final Collection<Trade> trades = copyFrom.getTrades();
    _tradeSpecs = new ComputationTargetSpecification[trades.size()];
    int i = 0;
    for (Trade trade : trades) {
      _tradeSpecs[i++] = ComputationTargetSpecification.of(trade);
    }
    _attributes = copyFrom.getAttributes();
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
