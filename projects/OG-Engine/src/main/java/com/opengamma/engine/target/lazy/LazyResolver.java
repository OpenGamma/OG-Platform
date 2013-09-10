/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.lazy;

import java.util.Collection;
import java.util.Map;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.engine.target.logger.LoggedResolutionPortfolio;
import com.opengamma.engine.target.logger.LoggedResolutionPortfolioNode;
import com.opengamma.engine.target.logger.LoggedResolutionPosition;
import com.opengamma.engine.target.logger.LoggedResolutionTrade;
import com.opengamma.engine.target.logger.ResolutionLogger;
import com.opengamma.engine.target.resolver.DeepResolver;
import com.opengamma.engine.target.resolver.ObjectResolver;
import com.opengamma.engine.target.resolver.Resolver;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.VersionCorrection;

/**
 * Marker interface for a target resolver that supports lazy operations.
 */
public interface LazyResolver {

  LazyResolveContext getLazyResolveContext();

  void setLazyResolveContext(LazyResolveContext context);

  /**
   * Base class of {@link ObjectResolver} instances that are owned by a parent {@link LazyResolver}.
   */
  public abstract static class ObjectResolverImpl<T extends UniqueIdentifiable> implements ObjectResolver<T>, DeepResolver {

    private final LazyResolver _parent;
    private final ObjectResolver<T> _underlying;

    public ObjectResolverImpl(final LazyResolver parent, final ObjectResolver<T> underlying) {
      _parent = parent;
      _underlying = underlying;
    }

    protected ObjectResolver<T> getUnderlying() {
      return _underlying;
    }

    protected abstract T lazy(T object, LazyResolveContext.AtVersionCorrection context);

    @Override
    public T resolveObject(final UniqueId uniqueId, final VersionCorrection versionCorrection) {
      final T underlying = _underlying.resolveObject(uniqueId, versionCorrection);
      if (underlying == null) {
        return null;
      }
      return lazy(underlying, _parent.getLazyResolveContext().atVersionCorrection(versionCorrection));
    }

    @Override
    public ChangeManager changeManager() {
      return getUnderlying().changeManager();
    }

    @Override
    public DeepResolver deepResolver() {
      return this;
    }

  }

  /**
   * Base class of {@link Resolver} instances that are owned by a parent {@link LazyResolver}.
   */
  public abstract static class ResolverImpl<T extends UniqueIdentifiable> extends ObjectResolverImpl<T> implements Resolver<T> {

    public ResolverImpl(final LazyResolver parent, final Resolver<T> underlying) {
      super(parent, underlying);
    }

    @Override
    protected Resolver<T> getUnderlying() {
      return (Resolver<T>) super.getUnderlying();
    }

    @Override
    public UniqueId resolveExternalId(final ExternalIdBundle identifiers, final VersionCorrection versionCorrection) {
      return getUnderlying().resolveExternalId(identifiers, versionCorrection);
    }

    @Override
    public Map<ExternalIdBundle, UniqueId> resolveExternalIds(final Collection<ExternalIdBundle> identifiers, final VersionCorrection versionCorrection) {
      return getUnderlying().resolveExternalIds(identifiers, versionCorrection);
    }

    @Override
    public UniqueId resolveObjectId(final ObjectId identifier, final VersionCorrection versionCorrection) {
      return getUnderlying().resolveObjectId(identifier, versionCorrection);
    }

    @Override
    public Map<ObjectId, UniqueId> resolveObjectIds(final Collection<ObjectId> identifiers, final VersionCorrection versionCorrection) {
      return getUnderlying().resolveObjectIds(identifiers, versionCorrection);
    }

  }

  /**
   * Lazy resolution of portfolios.
   */
  public static class LazyPortfolioResolver extends ResolverImpl<Portfolio> {

    public LazyPortfolioResolver(final LazyResolver parent, final Resolver<Portfolio> underlying) {
      super(parent, underlying);
    }

    // ObjectResolverImpl

    @Override
    public Portfolio lazy(final Portfolio object, final LazyResolveContext.AtVersionCorrection context) {
      return new LazyResolvedPortfolio(context, object);
    }

    // DeepResolver

    @Override
    public UniqueIdentifiable withLogger(final UniqueIdentifiable underlying, final ResolutionLogger logger) {
      if (underlying instanceof Portfolio) {
        return new LoggedResolutionPortfolio((Portfolio) underlying, logger);
      } else {
        return null;
      }
    }

  }

  /**
   * Lazy resolution of portfolio nodes.
   */
  public static class LazyPortfolioNodeResolver extends ResolverImpl<PortfolioNode> {

    public LazyPortfolioNodeResolver(final LazyResolver parent, final Resolver<PortfolioNode> underlying) {
      super(parent, underlying);
    }

    // ObjectResolverImpl

    @Override
    public PortfolioNode lazy(final PortfolioNode object, final LazyResolveContext.AtVersionCorrection context) {
      return new LazyResolvedPortfolioNode(context, object);
    }

    // DeepResolver

    @Override
    public UniqueIdentifiable withLogger(final UniqueIdentifiable underlying, final ResolutionLogger logger) {
      if (underlying instanceof PortfolioNode) {
        return new LoggedResolutionPortfolioNode((PortfolioNode) underlying, logger);
      } else {
        return null;
      }
    }

  }

  /**
   * Lazy resolution of positions.
   */
  public static class LazyPositionResolver extends ResolverImpl<Position> {

    public LazyPositionResolver(final LazyResolver parent, final Resolver<Position> underlying) {
      super(parent, underlying);
    }

    // ResolverImpl

    @Override
    public Position lazy(final Position object, final LazyResolveContext.AtVersionCorrection context) {
      return new LazyResolvedPosition(context, object);
    }

    // DeepResolver

    @Override
    public UniqueIdentifiable withLogger(final UniqueIdentifiable underlying, final ResolutionLogger logger) {
      if (underlying instanceof Position) {
        return new LoggedResolutionPosition((Position) underlying, logger);
      } else {
        return null;
      }
    }

  }

  /**
   * Lazy resolution of trades.
   */
  public static class LazyTradeResolver extends ResolverImpl<Trade> {

    public LazyTradeResolver(final LazyResolver parent, final Resolver<Trade> underlying) {
      super(parent, underlying);
    }

    // ObjectResolverImpl

    @Override
    public Trade lazy(final Trade object, final LazyResolveContext.AtVersionCorrection context) {
      return new LazyResolvedTrade(context, object);
    }

    // DeepResolver

    @Override
    public UniqueIdentifiable withLogger(final UniqueIdentifiable underlying, final ResolutionLogger logger) {
      if (underlying instanceof Trade) {
        return new LoggedResolutionTrade((Trade) underlying, logger);
      } else {
        return null;
      }
    }

  }

}
