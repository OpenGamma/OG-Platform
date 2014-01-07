/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.resolver;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.target.logger.LoggedResolutionPortfolio;
import com.opengamma.engine.target.logger.LoggedResolutionPortfolioNode;
import com.opengamma.engine.target.logger.ResolutionLogger;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * A {@link ObjectResolver} built on a {@link PositionSource}.
 * <p>
 * Note that these resolvers only access the position source for shallow resolution of trades and positions. More specialized forms will combine these instances with {@link SecuritySource} based
 * resolvers to provide the deep-resolution required by a full system.
 */
public class PositionSourceResolver {

  private final PositionSource _underlying;

  private static class TradeResolver extends PositionSourceResolver implements Resolver<Trade> {

    public TradeResolver(final PositionSource underlying) {
      super(underlying);
    }

    // ObjectResolver

    @Override
    public Trade resolveObject(final UniqueId uniqueId, final VersionCorrection versionCorrection) {
      try {
        return getUnderlying().getTrade(uniqueId);
      } catch (DataNotFoundException e) {
        return null;
      }
    }

    @Override
    public DeepResolver deepResolver() {
      return null;
    }

    // IdentifierResolver

    @Override
    public UniqueId resolveExternalId(final ExternalIdBundle identifiers, final VersionCorrection versionCorrection) {
      return null;
    }

    @Override
    public Map<ExternalIdBundle, UniqueId> resolveExternalIds(final Collection<ExternalIdBundle> identifiers, final VersionCorrection versionCorrection) {
      return Collections.emptyMap();
    }

    @Override
    public UniqueId resolveObjectId(final ObjectId identifier, final VersionCorrection versionCorrection) {
      try {
        // [PLAT-4491] TODO: PositionSource doesn't have a trade by OID lookup. This is probably wrong, but no worse than treating the identifier as v/c resolved
        return getUnderlying().getTrade(identifier.atLatestVersion()).getUniqueId();
      } catch (DataNotFoundException e) {
        return null;
      }
    }

    @Override
    public Map<ObjectId, UniqueId> resolveObjectIds(final Collection<ObjectId> identifiers, final VersionCorrection versionCorrection) {
      return AbstractIdentifierResolver.resolveObjectIds(this, identifiers, versionCorrection);
    }

  }

  private static class PositionResolver extends PositionSourceResolver implements Resolver<Position> {

    public PositionResolver(final PositionSource underlying) {
      super(underlying);
    }

    // ObjectResolver

    @Override
    public Position resolveObject(final UniqueId uniqueId, final VersionCorrection versionCorrection) {
      try {
        return getUnderlying().getPosition(uniqueId);
      } catch (DataNotFoundException e) {
        return null;
      }
    }

    @Override
    public DeepResolver deepResolver() {
      return null;
    }

    // IdentifierResolver

    @Override
    public UniqueId resolveExternalId(final ExternalIdBundle identifiers, final VersionCorrection versionCorrection) {
      return null;
    }

    @Override
    public Map<ExternalIdBundle, UniqueId> resolveExternalIds(final Collection<ExternalIdBundle> identifiers, final VersionCorrection versionCorrection) {
      return Collections.emptyMap();
    }

    @Override
    public UniqueId resolveObjectId(final ObjectId identifier, final VersionCorrection versionCorrection) {
      try {
        return getUnderlying().getPosition(identifier, versionCorrection).getUniqueId();
      } catch (DataNotFoundException e) {
        return null;
      }
    }

    @Override
    public Map<ObjectId, UniqueId> resolveObjectIds(final Collection<ObjectId> identifiers, final VersionCorrection versionCorrection) {
      return AbstractIdentifierResolver.resolveObjectIds(this, identifiers, versionCorrection);
    }

  }

  private static class PortfolioResolver extends PositionSourceResolver implements Resolver<Portfolio>, DeepResolver {

    public PortfolioResolver(final PositionSource underlying) {
      super(underlying);
    }

    // ObjectResolver

    @Override
    public Portfolio resolveObject(final UniqueId uniqueId, final VersionCorrection versionCorrection) {
      try {
        return getUnderlying().getPortfolio(uniqueId, versionCorrection);
      } catch (DataNotFoundException e) {
        return null;
      }
    }

    @Override
    public DeepResolver deepResolver() {
      return this;
    }

    // IdentifierResolver

    @Override
    public UniqueId resolveExternalId(final ExternalIdBundle identifiers, final VersionCorrection versionCorrection) {
      return null;
    }

    @Override
    public Map<ExternalIdBundle, UniqueId> resolveExternalIds(final Collection<ExternalIdBundle> identifiers, final VersionCorrection versionCorrection) {
      return Collections.emptyMap();
    }

    @Override
    public UniqueId resolveObjectId(final ObjectId identifier, final VersionCorrection versionCorrection) {
      try {
        return getUnderlying().getPortfolio(identifier, versionCorrection).getUniqueId();
      } catch (DataNotFoundException e) {
        return null;
      }
    }

    @Override
    public Map<ObjectId, UniqueId> resolveObjectIds(final Collection<ObjectId> identifiers, final VersionCorrection versionCorrection) {
      return AbstractIdentifierResolver.resolveObjectIds(this, identifiers, versionCorrection);
    }

    // DeepResolver

    @Override
    public UniqueIdentifiable withLogger(final UniqueIdentifiable resolved, final ResolutionLogger logger) {
      if (resolved instanceof Portfolio) {
        return new LoggedResolutionPortfolio((Portfolio) resolved, logger);
      } else {
        return null;
      }
    }

  }

  private static class PortfolioNodeResolver extends PositionSourceResolver implements Resolver<PortfolioNode>, DeepResolver {

    public PortfolioNodeResolver(final PositionSource underlying) {
      super(underlying);
    }

    // ObjectResolver

    @Override
    public PortfolioNode resolveObject(final UniqueId uniqueId, final VersionCorrection versionCorrection) {
      try {
        return getUnderlying().getPortfolioNode(uniqueId, versionCorrection);
      } catch (DataNotFoundException e) {
        return null;
      }
    }

    @Override
    public DeepResolver deepResolver() {
      return this;
    }

    // DeepResolver

    @Override
    public UniqueIdentifiable withLogger(final UniqueIdentifiable resolved, final ResolutionLogger logger) {
      if (resolved instanceof PortfolioNode) {
        return new LoggedResolutionPortfolioNode((PortfolioNode) resolved, logger);
      } else {
        return null;
      }
    }

    // IdentifierResolver

    @Override
    public UniqueId resolveExternalId(final ExternalIdBundle identifiers, final VersionCorrection versionCorrection) {
      return null;
    }

    @Override
    public Map<ExternalIdBundle, UniqueId> resolveExternalIds(final Collection<ExternalIdBundle> identifiers, final VersionCorrection versionCorrection) {
      return Collections.emptyMap();
    }

    @Override
    public UniqueId resolveObjectId(final ObjectId identifier, final VersionCorrection versionCorrection) {
      try {
        // [PLAT-4491] TODO: PositionSource doesn't have a node by OID lookup. This is probably wrong, but no worse than treating the identifier as v/c resolved
        return getUnderlying().getPortfolioNode(identifier.atLatestVersion(), versionCorrection).getUniqueId();
      } catch (DataNotFoundException e) {
        return null;
      }
    }

    @Override
    public Map<ObjectId, UniqueId> resolveObjectIds(final Collection<ObjectId> identifiers, final VersionCorrection versionCorrection) {
      return AbstractIdentifierResolver.resolveObjectIds(this, identifiers, versionCorrection);
    }

  }

  public PositionSourceResolver(final PositionSource underlying) {
    ArgumentChecker.notNull(underlying, "underlying");
    _underlying = underlying;
  }

  protected PositionSource getUnderlying() {
    return _underlying;
  }

  public Resolver<Trade> trade() {
    return new TradeResolver(getUnderlying());
  }

  public Resolver<Position> position() {
    return new PositionResolver(getUnderlying());
  }

  public Resolver<PortfolioNode> portfolioNode() {
    return new PortfolioNodeResolver(getUnderlying());
  }

  public Resolver<Portfolio> portfolio() {
    return new PortfolioResolver(getUnderlying());
  }

  public ChangeManager changeManager() {
    return getUnderlying().changeManager();
  }

}
