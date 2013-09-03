/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.lazy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.engine.ComputationTargetResolver;

/**
 * A position implementation that may not be fully resolved at construction but will appear fully resolved when used.
 */
public final class LazyResolvedPosition extends LazyResolvedPositionOrTrade<Position> implements Position {

  private static final long serialVersionUID = 1L;

  private volatile Collection<Trade> _trades;

  /**
   * Creates a new lazily resolved position.
   * 
   * @param underlying the underlying, un-resolved position
   * @param context the lazy resolution context
   */
  public LazyResolvedPosition(final LazyResolveContext.AtVersionCorrection context, final Position underlying) {
    super(context, underlying);
  }

  @Override
  public Collection<Trade> getTrades() {
    if (_trades == null) {
      Collection<Trade> newTrades = null;
      synchronized (this) {
        if (_trades == null) {
          final Collection<Trade> trades = getUnderlying().getTrades();
          if (trades.isEmpty()) {
            _trades = Collections.emptySet();
          } else {
            newTrades = new ArrayList<Trade>(trades.size());
            for (Trade trade : trades) {
              newTrades.add(new LazyResolvedTrade(getLazyResolveContext(), trade));
            }
            _trades = newTrades;
          }
        }
      }
      if (newTrades != null) {
        getLazyResolveContext().cacheTrades(newTrades);
      }
    }
    return _trades;
  }

  @Override
  public Map<String, String> getAttributes() {
    return getUnderlying().getAttributes();
  }

  @Override
  protected TargetResolverPosition targetResolverObject(final ComputationTargetResolver.AtVersionCorrection targetResolver) {
    return new TargetResolverPosition(targetResolver, this);
  }

  @Override
  protected SimplePosition simpleObject() {
    return new SimplePosition(this);
  }

  @Override
  public String toString() {
    return "LazyResolvedPosition[" + getUniqueId() + "]";
  }

}
