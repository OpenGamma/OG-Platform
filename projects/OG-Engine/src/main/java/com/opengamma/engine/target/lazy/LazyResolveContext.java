/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.lazy;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecurityLink;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.CachingComputationTargetResolver;
import com.opengamma.id.VersionCorrection;

/**
 * Shared state used by {@link LazyResolvedPortfolio}, {@link LazyResolvedPortfolioNode}, {@link LazyResolvedPosition} and {@link LazyResolvedTrade}.
 */
public class LazyResolveContext {

  /**
   * Resolution context bound to a specific resolution version/correction.
   */
  public class AtVersionCorrection extends LazyResolveContext {

    private final CachingComputationTargetResolver.AtVersionCorrection _targetResolver;
    private final VersionCorrection _versionCorrection;

    public AtVersionCorrection(final LazyResolveContext context, final VersionCorrection versionCorrection) {
      super(context.getSecuritySource(), context.getRawTargetResolver());
      _versionCorrection = versionCorrection;
      _targetResolver = (getRawTargetResolver() != null) ? getRawTargetResolver().atVersionCorrection(versionCorrection) : null;
    }

    protected Security resolveLinkImpl(final SecurityLink link) {
      try {
        return link.resolve(getSecuritySource(), _versionCorrection);
      } catch (DataNotFoundException e) {
        return null;
      }
    }

    public Security resolveLink(final SecurityLink link) {
      final Security target = link.getTarget();
      if (target != null) {
        return target;
      }
      final Security security = resolveLinkImpl(link);
      if (security != null) {
        cacheSecurity(security);
      }
      return security;
    }

    public CachingComputationTargetResolver.AtVersionCorrection getTargetResolver() {
      return _targetResolver;
    }

    public void cacheSecurity(final Security security) {
      if (getTargetResolver() != null) {
        getTargetResolver().cacheTargets(Collections.singleton(security));
      }
    }

    public void cachePosition(final Position position) {
      if (getTargetResolver() != null) {
        getTargetResolver().cacheTargets(Collections.singleton(position));
      }
    }

    public void cachePositions(final Collection<Position> positions) {
      if (getTargetResolver() != null) {
        getTargetResolver().cacheTargets(positions);
      }
    }

    public void cachePortfolioNode(final PortfolioNode portfolioNode) {
      if (getTargetResolver() != null) {
        getTargetResolver().cacheTargets(Collections.singleton(portfolioNode));
      }
    }

    public void cachePortfolioNodes(final Collection<PortfolioNode> portfolioNodes) {
      if (getTargetResolver() != null) {
        getTargetResolver().cacheTargets(portfolioNodes);
      }
    }

    public void cacheTrade(final Trade trade) {
      if (getTargetResolver() != null) {
        getTargetResolver().cacheTargets(Collections.singleton(trade));
      }
    }

    public void cacheTrades(final Collection<Trade> trades) {
      if (getTargetResolver() != null) {
        getTargetResolver().cacheTargets(trades);
      }
    }

  }

  private final SecuritySource _securities;
  private final CachingComputationTargetResolver _targetResolver;
  private static final ThreadLocal<AtomicInteger> s_writeCount = new ThreadLocal<AtomicInteger>();

  public LazyResolveContext(final SecuritySource securities, final CachingComputationTargetResolver targetResolver) {
    _securities = securities;
    _targetResolver = targetResolver;
  }

  public AtVersionCorrection atVersionCorrection(final VersionCorrection versionCorrection) {
    return new AtVersionCorrection(this, versionCorrection);
  }

  public SecuritySource getSecuritySource() {
    return _securities;
  }

  public CachingComputationTargetResolver getRawTargetResolver() {
    return _targetResolver;
  }

  /**
   * Called by a thread at the start of any serialization operations which might result in further call backs to the cache. This is to allow a cache to detect such scenarios and work around an
   * re-entrance problems that its underlying implementation might otherwise experience. Each call from a thread must be balanced by a call to {@link #endWrite}.
   */
  public static void beginWrite() {
    AtomicInteger c = s_writeCount.get();
    if (c == null) {
      c = new AtomicInteger(1);
      s_writeCount.set(c);
    } else {
      c.incrementAndGet();
    }
  }

  /**
   * Tests if the calling thread is between calls to {@link #beginWrite} and {@link #endWrite}. The call will be potentially re-entrant to the underlying cache and should be deferred if the cache
   * can't support this.
   * 
   * @return true if the thread is engaged in serialization behavior, false otherwise
   */
  public static boolean isWriting() {
    final AtomicInteger c = s_writeCount.get();
    return (c != null) && (c.get() > 0);
  }

  /**
   * Called by a thread at the end of any serialization operations flagged by a previous call to {@link #beginWrite}.
   */
  public static void endWrite() {
    s_writeCount.get().decrementAndGet();
  }

}
