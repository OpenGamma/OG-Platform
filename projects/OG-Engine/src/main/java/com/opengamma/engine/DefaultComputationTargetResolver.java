/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.target.CacheNotifyingSecuritySource;
import com.opengamma.engine.target.LazyResolveContext;
import com.opengamma.engine.target.LazyResolvedPortfolio;
import com.opengamma.engine.target.LazyResolvedPortfolioNode;
import com.opengamma.engine.target.LazyResolvedPosition;
import com.opengamma.engine.target.LazyResolvedTrade;
import com.opengamma.engine.target.LazyResolver;
import com.opengamma.engine.target.LazyResolverPositionSource;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * A computation target resolver implementation that resolves using a security and position source.
 * <p>
 * This is the standard implementation that resolves from a target specification to a real target. It provides results using a security and position source.
 */
public class DefaultComputationTargetResolver implements ComputationTargetResolver, LazyResolver {

  // TODO: move to com.opengamma.engine.target

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DefaultComputationTargetResolver.class);

  /**
   * The security source.
   */
  private final SecuritySource _securitySource;
  /**
   * The position source.
   */
  private final PositionSource _positionSource;

  private LazyResolveContext _lazyResolveContext;

  /**
   * Creates a resolver without access to a security source or a position source. This will only be able to resolve PRIMITIVE computation target types.
   */
  public DefaultComputationTargetResolver() {
    this(null, null);
  }

  /**
   * Creates a resolver using a security source only. This will not be able to resolve POSITION and PORTFOLIO_NODE computation target types.
   * 
   * @param securitySource the security source, null prevents some targets from resolving
   */
  public DefaultComputationTargetResolver(final SecuritySource securitySource) {
    this(securitySource, null);
  }

  /**
   * Creates a resolver using a security and position source This will be able to resolve any type of computation target.
   * 
   * @param securitySource the security source, null prevents some targets from resolving
   * @param positionSource the position source, null prevents some targets from resolving
   */
  public DefaultComputationTargetResolver(final SecuritySource securitySource, final PositionSource positionSource) {
    _securitySource = securitySource;
    _positionSource = positionSource;
    _lazyResolveContext = new LazyResolveContext(securitySource, null);
  }

  @Override
  public void setLazyResolveContext(final LazyResolveContext context) {
    ArgumentChecker.notNull(context, "context");
    _lazyResolveContext = context;
  }

  @Override
  public LazyResolveContext getLazyResolveContext() {
    return _lazyResolveContext;
  }

  /**
   * Gets the security source which provides access to the securities.
   * 
   * @return the security source, may be null
   */
  protected SecuritySource getSecuritySourceImpl() {
    return _securitySource;
  }

  @Override
  public SecuritySource getSecuritySource() {
    if (getLazyResolveContext().getTargetResolver() != null) {
      return new CacheNotifyingSecuritySource(getSecuritySourceImpl(), getLazyResolveContext().getTargetResolver());
    } else {
      return getSecuritySourceImpl();
    }
  }

  /**
   * Gets the position source which provides access to the positions.
   * 
   * @return the position source, may be null
   */
  public PositionSource getPositionSourceImpl() {
    return _positionSource;
  }

  @Override
  public PositionSource getPositionSource() {
    return new LazyResolverPositionSource(getPositionSourceImpl(), getLazyResolveContext());
  }

  //-------------------------------------------------------------------------
  /**
   * Resolves the specification using the security and position sources.
   * <p>
   * The key method of this class, implementing {@code ComputationTargetResolver}. It examines the specification and resolves the most appropriate target.
   * 
   * @param specification the specification to resolve, not null
   * @return the resolved target, null if not found
   */
  @Override
  public ComputationTarget resolve(final ComputationTargetSpecification specification) {
    final UniqueId uid = specification.getUniqueId();
    switch (specification.getType()) {
      case PRIMITIVE:
        return resolvePrimitive(specification, uid);
      case SECURITY:
        return resolveSecurity(specification, uid);
      case POSITION:
        return resolvePosition(specification, uid);
      case TRADE:
        return resolveTrade(specification, uid);
      case PORTFOLIO_NODE:
        return resolveNode(specification, uid);
      default:
        throw new OpenGammaRuntimeException("Unhandled computation target type: " + specification.getType());
    }
  }

  /**
   * Handles the resolve of a primitive. This is only called if the specification is of type primitive.
   * 
   * @param specification the specification being resolved, not null
   * @param uid the unique identifier of the target
   * @return the resolved primitive target, not null
   */
  protected ComputationTarget resolvePrimitive(final ComputationTargetSpecification specification, final UniqueId uid) {
    return new ComputationTarget(specification.getType(), uid);
  }

  /**
   * Handles the resolve of a security. This is only called if the specification is of type security.
   * 
   * @param specification the specification being resolved, not null
   * @param securityId the unique identifier of the target
   * @return the resolved security target, not null
   */
  protected ComputationTarget resolveSecurity(final ComputationTargetSpecification specification, final UniqueId securityId) {
    checkSecuritySource(ComputationTargetType.SECURITY);

    final Security security;
    try {
      security = getSecuritySourceImpl().get(securityId);
    } catch (DataNotFoundException ex) {
      s_logger.info("Unable to resolve security UID {}", securityId);
      return null;
    }
    s_logger.info("Resolved security UID {} to security {}", securityId, security);
    return new ComputationTarget(ComputationTargetType.SECURITY, security);
  }

  /**
   * Handles the resolve of a position. This is only called if the specification is of type position.
   * 
   * @param specification the specification being resolved, not null
   * @param positionId the unique identifier of the target
   * @return the resolved position target, not null
   */
  protected ComputationTarget resolvePosition(final ComputationTargetSpecification specification, final UniqueId positionId) {
    checkSecuritySource(ComputationTargetType.POSITION);
    checkPositionSource(ComputationTargetType.POSITION);

    // resolve position
    Position position;
    try {
      position = getPositionSourceImpl().getPosition(positionId);
    } catch (DataNotFoundException ex) {
      s_logger.info("Unable to resolve position UID {}", positionId);
      return null;
    }
    s_logger.info("Resolved position UID {} to position {}", positionId, position);
    return new ComputationTarget(ComputationTargetType.POSITION, new LazyResolvedPosition(getLazyResolveContext(), position));
  }

  /**
   * Handles the resolve of a trade. This is only called if the specification is of type trade.
   * 
   * @param specification the specification being resolved, not null
   * @param tradeId the unique identifier of the target
   * @return the resolved trade target, not null
   */
  protected ComputationTarget resolveTrade(final ComputationTargetSpecification specification, final UniqueId tradeId) {
    checkSecuritySource(ComputationTargetType.TRADE);
    checkPositionSource(ComputationTargetType.TRADE);

    // resolve trade
    Trade trade;
    try {
      trade = getPositionSourceImpl().getTrade(tradeId);
    } catch (DataNotFoundException ex) {
      s_logger.info("Unable to resolve trade UID {}", tradeId);
      return null;
    }
    s_logger.info("Resolved trade UID {} to trade {}", tradeId, trade);
    return new ComputationTarget(ComputationTargetType.TRADE, new LazyResolvedTrade(getLazyResolveContext(), trade));
  }

  /**
   * Handles the resolve of a node. This is only called if the specification is of type node.
   * 
   * @param specification the specification being resolved, not null
   * @param uniqueId the unique identifier of the target
   * @return the resolved node target, not null
   */
  protected ComputationTarget resolveNode(final ComputationTargetSpecification specification, final UniqueId uniqueId) {
    checkPositionSource(ComputationTargetType.PORTFOLIO_NODE);

    // try node
    try {
      PortfolioNode node = getPositionSourceImpl().getPortfolioNode(uniqueId);
      s_logger.info("Resolved multiple-position UID {} to portfolio node {}", uniqueId, node);
      return new ComputationTarget(ComputationTargetType.PORTFOLIO_NODE, new LazyResolvedPortfolioNode(getLazyResolveContext(), node));
    } catch (DataNotFoundException ex) {
      // try portfolio
      try {
        Portfolio portfolio = getPositionSourceImpl().getPortfolio(uniqueId);
        s_logger.info("Resolved multiple-position UID {} to portfolio {}", uniqueId, portfolio);
        return new ComputationTarget(ComputationTargetType.PORTFOLIO_NODE, new LazyResolvedPortfolio(getLazyResolveContext(), portfolio));
      } catch (DataNotFoundException ex2) {
        s_logger.info("Unable to resolve multiple-position UID {}", uniqueId);
        return null;
      }
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Ensures that the security source is available.
   * 
   * @param attemptedTargetType the target type for the message, not null
   */
  private void checkSecuritySource(ComputationTargetType attemptedTargetType) {
    if (getSecuritySourceImpl() == null) {
      throw new OpenGammaRuntimeException("Unable to resolve " + attemptedTargetType + ", no SecuritySource found");
    }
  }

  /**
   * Ensures that the position source is available.
   * 
   * @param attemptedTargetType the target type for the message, not null
   */
  private void checkPositionSource(ComputationTargetType attemptedTargetType) {
    if (getPositionSourceImpl() == null) {
      throw new OpenGammaRuntimeException("Unable to resolve " + attemptedTargetType + ", no PositionSource found");
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a string suitable for debugging.
   * 
   * @return the string, not null
   */
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[securitySource=" + getSecuritySourceImpl() + ",positionSource=" + getPositionSourceImpl() + "]";
  }

}
