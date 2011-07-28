/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.PortfolioImpl;
import com.opengamma.core.position.impl.PortfolioNodeImpl;
import com.opengamma.core.position.impl.PositionImpl;
import com.opengamma.core.position.impl.TradeImpl;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * A computation target resolver implementation that resolves using a security and position source.
 * <p>
 * This is the standard implementation that resolves from a target specification to a real target.
 * It provides results using a security and position source.
 */
public class DefaultComputationTargetResolver implements ComputationTargetResolver {

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
  /**
   * The recursive resolver.
   */
  private ComputationTargetResolver _recursiveResolver = this;

  /**
   * Creates a resolver without access to a security source or a position source.
   * This will only be able to resolve PRIMITIVE computation target types.
   */
  public DefaultComputationTargetResolver() {
    this(null, null);
  }

  /**
   * Creates a resolver using a security source only.
   * This will not be able to resolve POSITION and PORTFOLIO_NODE computation target types.
   * 
   * @param securitySource  the security source, null prevents some targets from resolving
   */
  public DefaultComputationTargetResolver(final SecuritySource securitySource) {
    _securitySource = securitySource;
    _positionSource = null;
  }

  /**
   * Creates a resolver using a security and position source
   * This will be able to resolve any type of computation target.
   * 
   * @param securitySource  the security source, null prevents some targets from resolving
   * @param positionSource  the position source, null prevents some targets from resolving
   */
  public DefaultComputationTargetResolver(final SecuritySource securitySource, final PositionSource positionSource) {
    _securitySource = securitySource;
    _positionSource = positionSource;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the recursive resolver.
   * <p>
   * The recursive resolver is used to resolve the security for a position and the
   * underlying nodes of multiple-positions. By default, it is simply {@code this}.
   * It can be changed to improve performance, for example using the
   * {@code CachingComputationTargetResolver} to ensure that all deep position and
   * security nodes get cached when a node higher up in the tree is requested.
   * 
   * @return the recursive resolver, not null
   */
  public ComputationTargetResolver getRecursiveResolver() {
    return _recursiveResolver;
  }

  /**
   * Sets the recursive resolver.
   * <p>
   * The recursive resolver is used to resolve the security for a position and the
   * underlying nodes of multiple-positions. By default, it is simply {@code this}.
   * It can be changed to improve performance, for example using the
   * {@code CachingComputationTargetResolver} to ensure that all deep position and
   * security nodes get cached when a node higher up in the tree is requested.
   * 
   * @param recursiveResolver  the recursive resolver, not null
   */
  public void setRecursiveResolver(final ComputationTargetResolver recursiveResolver) {
    ArgumentChecker.notNull(recursiveResolver, "recursiveResolver");
    _recursiveResolver = recursiveResolver;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the security source which provides access to the securities.
   * 
   * @return the security source, may be null
   */
  public SecuritySource getSecuritySource() {
    return _securitySource;
  }

  /**
   * Gets the position source which provides access to the positions.
   * 
   * @return the position source, may be null
   */
  public PositionSource getPositionSource() {
    return _positionSource;
  }

  //-------------------------------------------------------------------------
  /**
   * Resolves the specification using the security and position sources.
   * <p>
   * The key method of this class, implementing {@code ComputationTargetResolver}.
   * It examines the specification and resolves the most appropriate target.
   * 
   * @param specification  the specification to resolve, not null
   * @return the resolved target, null if not found
   */
  @Override
  public ComputationTarget resolve(final ComputationTargetSpecification specification) {
    final UniqueIdentifier uid = specification.getUniqueId();
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
   * Handles the resolve of a primitive.
   * This is only called if the specification is of type primitive.
   * 
   * @param specification  the specification being resolved, not null
   * @param uid  the unique identifier of the target
   * @return the resolved primitive target, not null
   */
  protected ComputationTarget resolvePrimitive(final ComputationTargetSpecification specification, final UniqueIdentifier uid) {
    return new ComputationTarget(specification.getType(), uid);
  }

  /**
   * Handles the resolve of a security.
   * This is only called if the specification is of type security.
   * 
   * @param specification  the specification being resolved, not null
   * @param securityId  the unique identifier of the target
   * @return the resolved security target, not null
   */
  protected ComputationTarget resolveSecurity(final ComputationTargetSpecification specification, final UniqueIdentifier securityId) {
    checkSecuritySource(ComputationTargetType.SECURITY);
    
    final Security security = getSecuritySource().getSecurity(securityId);
    if (security == null) {
      s_logger.info("Unable to resolve security UID {}", securityId);
      return null;
    }
    s_logger.info("Resolved security UID {} to security {}", securityId, security);
    return new ComputationTarget(ComputationTargetType.SECURITY, security);
  }

  /**
   * Handles the resolve of a position.
   * This is only called if the specification is of type position.
   * 
   * @param specification  the specification being resolved, not null
   * @param positionId  the unique identifier of the target
   * @return the resolved position target, not null
   */
  protected ComputationTarget resolvePosition(final ComputationTargetSpecification specification, final UniqueIdentifier positionId) {
    checkSecuritySource(ComputationTargetType.POSITION);
    checkPositionSource(ComputationTargetType.POSITION);
    
    // resolve position
    Position position = getPositionSource().getPosition(positionId);
    if (position == null) {
      s_logger.info("Unable to resolve position UID {}", positionId);
      return null;
    }
    s_logger.info("Resolved position UID {} to position {}", positionId, position);
    
    // resolve linked security
    Security security = position.getSecurityLink().resolve(getSecuritySource());
    if (security == null) {
      s_logger.warn("Unable to resolve security {} for position UID {}", position.getSecurityLink(), positionId);
    } else {
      s_logger.info("Resolved security link {} to security {}", position.getSecurityLink(), security);
    }
    final PositionImpl newPosition = new PositionImpl(position);
    for (Trade trade : position.getTrades()) {
      final ComputationTargetSpecification tradeSpec = new ComputationTargetSpecification(ComputationTargetType.TRADE, trade.getUniqueId());
      final ComputationTarget resolvedTradeTarget = getRecursiveResolver().resolve(tradeSpec);
      newPosition.addTrade(resolvedTradeTarget.getTrade());
    }
    position = newPosition;
    return new ComputationTarget(ComputationTargetType.POSITION, position);
  }

  /**
   * Handles the resolve of a trade.
   * This is only called if the specification is of type trade.
   * 
   * @param specification  the specification being resolved, not null
   * @param tradeId  the unique identifier of the target
   * @return the resolved trade target, not null
   */
  protected ComputationTarget resolveTrade(final ComputationTargetSpecification specification, final UniqueIdentifier tradeId) {
    checkSecuritySource(ComputationTargetType.TRADE);
    checkPositionSource(ComputationTargetType.TRADE);
    
    // resolve trade
    Trade trade = getPositionSource().getTrade(tradeId);
    if (trade == null) {
      s_logger.info("Unable to resolve trade UID {}", tradeId);
      return null;
    }
    s_logger.info("Resolved trade UID {} to trade {}", tradeId, trade);
    
    // resolve linked security
    Security security = trade.getSecurityLink().resolve(getSecuritySource());
    if (security == null) {
      s_logger.warn("Unable to resolve security {} for trade UID {}", trade.getSecurityLink(), tradeId);
    } else {
      s_logger.info("Resolved security link {} to security {}", trade.getSecurityLink(), security);
    }
    trade = new TradeImpl(trade);
    return new ComputationTarget(ComputationTargetType.TRADE, trade);
  }

  /**
   * Handles the resolve of a node.
   * This is only called if the specification is of type node.
   * 
   * @param specification  the specification being resolved, not null
   * @param uid  the unique identifier of the target
   * @return the resolved node target, not null
   */
  protected ComputationTarget resolveNode(final ComputationTargetSpecification specification, final UniqueIdentifier uid) {
    checkPositionSource(ComputationTargetType.PORTFOLIO_NODE);
    
    PortfolioNode node = getPositionSource().getPortfolioNode(uid);
    if (node != null) {
      s_logger.info("Resolved multiple-position UID {} to portfolio node {}", uid, node);
      return new ComputationTarget(ComputationTargetType.PORTFOLIO_NODE, resolveNodeTree(uid, node));
    }
    final Portfolio portfolio = getPositionSource().getPortfolio(uid);
    if (portfolio != null) {
      s_logger.info("Resolved multiple-position UID {} to portfolio {}", uid, portfolio);
      node = portfolio.getRootNode();
      return new ComputationTarget(ComputationTargetType.PORTFOLIO_NODE, new PortfolioImpl(portfolio.getUniqueId(), portfolio.getName(), resolveNodeTree(uid, node)));
    }
    s_logger.info("Unable to resolve multiple-position UID {}", uid);
    return null;
  }

  /**
   * Handles the resolve of a node and its children.
   * 
   * @param uid  the unique identifier of the target
   * @param node  the node
   * @return the resolved node, not null
   */
  private PortfolioNodeImpl resolveNodeTree(final UniqueIdentifier uid, final PortfolioNode node) {
    final PortfolioNodeImpl newNode = new PortfolioNodeImpl(node.getUniqueId(), node.getName());
    newNode.setParentNodeId(node.getParentNodeId());
    for (PortfolioNode child : node.getChildNodes()) {
      final ComputationTarget resolvedChild = getRecursiveResolver().resolve(new ComputationTargetSpecification(ComputationTargetType.PORTFOLIO_NODE, child.getUniqueId()));
      if (resolvedChild == null) {
        s_logger.warn("Portfolio node ID {} couldn't be resolved for portfolio node ID {}", child.getUniqueId(), uid);
      } else {
        newNode.addChildNode(resolvedChild.getPortfolioNode());
      }
    }
    for (Position position : node.getPositions()) {
      final ComputationTarget resolvedPosition = getRecursiveResolver().resolve(new ComputationTargetSpecification(ComputationTargetType.POSITION, position.getUniqueId()));
      if (resolvedPosition == null) {
        s_logger.warn("Position ID {} couldn't be resolved for portfolio node ID {}", position.getUniqueId(), uid);
      } else {
        newNode.addPosition(resolvedPosition.getPosition());
      }
    }
    return newNode;
  }

  //-------------------------------------------------------------------------
  /**
   * Ensures that the security source is available.
   * 
   * @param attemptedTargetType  the target type for the message, not null
   */
  private void checkSecuritySource(ComputationTargetType attemptedTargetType) {
    if (getSecuritySource() == null) {
      throw new OpenGammaRuntimeException("Unable to resolve " + attemptedTargetType + ", no SecuritySource found");
    }
  }

  /**
   * Ensures that the position source is available.
   * 
   * @param attemptedTargetType  the target type for the message, not null
   */
  private void checkPositionSource(ComputationTargetType attemptedTargetType) {
    if (getPositionSource() == null) {
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
    return getClass().getSimpleName() + "[secuitySource=" + getSecuritySource() + ",positionSource=" + getPositionSource() + "]";
  }

}
