/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
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
   * Delegate {@code ComputationTargetResolver} for resolving the security for a position, and underlying
   * nodes of multiple-positions. Defaults to this object, but can be changed to the {@code CachingComputationTargetResolver}
   * to improve performance of the cache (e.g. make sure that all deep position and security nodes get cached
   * when a node higher up in the tree is requested).
   */
  private ComputationTargetResolver _recursiveResolver = this;

  /**
   * Creates a resolver without access to a security source or a position source. This will only be able to resolve
   * PRIMITIVE computation target types.
   */
  public DefaultComputationTargetResolver() {
    this(null, null);
  }

  /**
   * Creates a resolver using a security source only. This will not be able to resolve POSITION and PORTFOLIO_NODE
   * computation target types.
   * 
   * @param securitySource  the security source
   */
  public DefaultComputationTargetResolver(SecuritySource securitySource) {
    _securitySource = securitySource;
    _positionSource = null;
  }

  /**
   * Creates a resolver using a security and position source, for resolving any type of computation target.
   * 
   * @param securitySource  the security source
   * @param positionSource  the position source
   */
  public DefaultComputationTargetResolver(SecuritySource securitySource, PositionSource positionSource) {
    _securitySource = securitySource;
    _positionSource = positionSource;
  }

  // -------------------------------------------------------------------------
  /**
   * Gets the recursive resolver.
   * @return the recursive resolver, not null
   */
  public ComputationTargetResolver getRecursiveResolver() {
    return _recursiveResolver;
  }

  /**
   * Sets the recursive resolver.
   * This might be used to add a caching resolver.
   * @param recursiveResolver  the recursive resolver, not null
   */
  public void setRecursiveResolver(final ComputationTargetResolver recursiveResolver) {
    ArgumentChecker.notNull(recursiveResolver, "recursiveResolver");
    _recursiveResolver = recursiveResolver;
  }

  // -------------------------------------------------------------------------
  /**
   * Gets the security source which provides access to the securities.
   * @return the security source, not null
   */
  public SecuritySource getSecuritySource() {
    return _securitySource;
  }

  /**
   * Gets the position source which provides access to the positions.
   * @return the position source, not null
   */
  public PositionSource getPositionSource() {
    return _positionSource;
  }

  // -------------------------------------------------------------------------
  /**
   * Resolves the specification using the security and position sources.
   * @param specification  the specification to resolve, not null
   * @return the resolved target, null if not found
   */
  @Override
  public ComputationTarget resolve(final ComputationTargetSpecification specification) {
    UniqueIdentifier uid = specification.getUniqueIdentifier();
    switch (specification.getType()) {
      case PRIMITIVE: {
        return new ComputationTarget(specification.getType(), uid);
      }
      case SECURITY: {
        checkSecuritySource(ComputationTargetType.SECURITY);
        final Security security = getSecuritySource().getSecurity(uid);
        if (security == null) {
          s_logger.info("Unable to resolve security UID {}", uid);
          return null;
        }
        s_logger.info("Resolved security UID {} to security {}", uid, security);
        return new ComputationTarget(ComputationTargetType.SECURITY, security);
      }
      case POSITION: {
        checkSecuritySource(ComputationTargetType.POSITION);
        checkPositionSource(ComputationTargetType.POSITION);
        Position position = getPositionSource().getPosition(uid);
        if (position == null) {
          s_logger.info("Unable to resolve position UID {}", uid);
          return null;
        }
        s_logger.info("Resolved position UID {} to position {}", uid, position);
        if (position.getSecurity() == null) {
          Security security = getSecuritySource().getSecurity(position.getSecurityKey());
          if (security == null) {
            s_logger.warn("Unable to resolve security ID {} for position UID {}", position.getSecurityKey(), uid);
          } else {
            s_logger.info("Resolved security ID {} to security {}", position.getSecurityKey(), security);
            final PositionImpl newPosition = new PositionImpl(position);
            newPosition.setSecurity(security);
            Set<Trade> newTrades = Sets.newHashSet();
            for (Trade trade : position.getTrades()) {
              final ComputationTarget resolvedTrade = getRecursiveResolver().resolve(new ComputationTargetSpecification(ComputationTargetType.TRADE, trade.getUniqueId()));
              newTrades.add(resolvedTrade.getTrade());
            }
            newPosition.setTrades(newTrades);
            position = newPosition;
          }
        }
        return new ComputationTarget(ComputationTargetType.POSITION, position);
      }
      case TRADE: {
        checkSecuritySource(ComputationTargetType.TRADE);
        checkPositionSource(ComputationTargetType.TRADE);
        Trade trade = getPositionSource().getTrade(uid);
        if (trade == null) {
          s_logger.info("Unable to resolve trade UID {}", uid);
          return null;
        }
        s_logger.info("Resolved trade UID {} to trade {}", uid, trade);
        if (trade.getSecurity() == null) {
          Security security = getSecuritySource().getSecurity(trade.getSecurityKey());
          if (security == null) {
            s_logger.warn("Unable to resolve security ID {} for trade UID {}", trade.getSecurityKey(), uid);
          } else {
            s_logger.info("Resolved security ID {} to security {}", trade.getSecurityKey(), security);
            final TradeImpl newTrade = new TradeImpl(trade);
            newTrade.setSecurity(security);
            trade = newTrade;
          }
        }
        return new ComputationTarget(ComputationTargetType.TRADE, trade);
      }
      case PORTFOLIO_NODE: {
        checkPositionSource(ComputationTargetType.PORTFOLIO_NODE);
        PortfolioNode node = getPositionSource().getPortfolioNode(uid);
        if (node != null) {
          s_logger.info("Resolved multiple-position UID {} to portfolio node {}", uid, node);
          return new ComputationTarget(ComputationTargetType.PORTFOLIO_NODE, resolvePortfolioNode(uid, node));
        }
        final Portfolio portfolio = getPositionSource().getPortfolio(uid);
        if (portfolio != null) {
          s_logger.info("Resolved multiple-position UID {} to portfolio {}", uid, portfolio);
          node = portfolio.getRootNode();
          return new ComputationTarget(ComputationTargetType.PORTFOLIO_NODE, new PortfolioImpl(portfolio.getUniqueId(), portfolio.getName(), resolvePortfolioNode(uid, node)));
        }
        s_logger.info("Unable to resolve multiple-position UID {}", uid);
        return null;
      }
      default: {
        throw new OpenGammaRuntimeException("Unhandled computation target type: " + specification.getType());
      }
    }
  }

  private PortfolioNodeImpl resolvePortfolioNode(final UniqueIdentifier uid, final PortfolioNode node) {
    final PortfolioNodeImpl newNode = new PortfolioNodeImpl(node.getUniqueId(), node.getName());
    newNode.setParentNode(node.getParentNode());
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

  private void checkSecuritySource(ComputationTargetType attemptedTargetType) {
    if (getSecuritySource() == null) {
      throw new OpenGammaRuntimeException("Access to a security source is required in order to resolve computation targets of type " + attemptedTargetType);
    }
  }

  private void checkPositionSource(ComputationTargetType attemptedTargetType) {
    if (getPositionSource() == null) {
      throw new OpenGammaRuntimeException("Access to a position source is required in order to resolve computation targets of type " + attemptedTargetType);
    }
  }

}
