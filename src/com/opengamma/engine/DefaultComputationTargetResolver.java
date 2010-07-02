/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.position.PortfolioImpl;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.PortfolioNodeImpl;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.position.PositionImpl;
import com.opengamma.engine.position.PositionMaster;
import com.opengamma.engine.security.Security;
import com.opengamma.engine.security.SecurityMaster;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * A standard implementation of {@code ComputationTargetResolver} that resolves
 * from a target specification to a real target.
 * <p>
 * This implementation satisfies results using an injected security and position master.
 */
public class DefaultComputationTargetResolver implements ComputationTargetResolver {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DefaultComputationTargetResolver.class);

  /**
   * The security master.
   */
  private final SecurityMaster _securityMaster;
  /**
   * The position master.
   */
  private final PositionMaster _positionMaster;

  /**
   * Delegate {@code ComputationTargetResolver} for resolving the security for a position, and underlying
   * nodes of multiple-positions. Defaults to this object, but can be changed to the {@code CachingComputationTargetResolver}
   * to improve performance of the cache (e.g. make sure that all deep position and security nodes get cached
   * when a node higher up in the tree is requested).
   */
  private ComputationTargetResolver _recursiveResolver = this;

  /**
   * Creates a resolver using a security and position master.
   * @param securityMaster  the security master, not null
   * @param positionMaster  the position master, not null
   */
  public DefaultComputationTargetResolver(SecurityMaster securityMaster, PositionMaster positionMaster) {
    ArgumentChecker.notNull(securityMaster, "Security Master");
    ArgumentChecker.notNull(positionMaster, "Position master");
    _securityMaster = securityMaster;
    _positionMaster = positionMaster;
  }

  //-------------------------------------------------------------------------

  public void setRecursiveResolver(final ComputationTargetResolver recursiveResolver) {
    ArgumentChecker.notNull(recursiveResolver, "Computation Target Resolver");
    _recursiveResolver = recursiveResolver;
  }

  public ComputationTargetResolver getRecursiveResolver() {
    return _recursiveResolver;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the security master which holds details of all securities in the system.
   * @return the security master, not null
   */
  public SecurityMaster getSecurityMaster() {
    return _securityMaster;
  }

  /**
   * Gets the position master which holds details of all positions in the system.
   * @return the position master, not null
   */
  public PositionMaster getPositionMaster() {
    return _positionMaster;
  }

  //-------------------------------------------------------------------------
  /**
   * Resolves the specification using the security and position masters..
   * @param specification  the specification to resolve, not null
   * @return the resolved target, null if not found
   */
  @Override
  public ComputationTarget resolve(ComputationTargetSpecification specification) {
    UniqueIdentifier uid = specification.getUniqueIdentifier();
    switch (specification.getType()) {
      case PRIMITIVE: {
        return new ComputationTarget(specification.getType(), uid);
      }
      case SECURITY: {
        Security security = getSecurityMaster().getSecurity(uid);
        if (security == null) {
          s_logger.info("Unable to resolve security UID {}", uid);
          return null;
        }
        s_logger.info("Resolved security UID {} to security {}", uid, security);
        return new ComputationTarget(ComputationTargetType.SECURITY, security);
      }
      case POSITION: {
        Position position = getPositionMaster().getPosition(uid);
        if (position == null) {
          s_logger.info("Unable to resolve position UID {}", uid);
          return null;
        }
        s_logger.info("Resolved position UID {} to position {}", uid, position);
        if (position.getSecurity() == null) {
          Security security = getSecurityMaster().getSecurity(position.getSecurityKey());
          if (security == null) {
            s_logger.warn("Unable to resolve security ID {} for position UID {}", position.getSecurityKey(), uid);
          } else {
            s_logger.info("Resolved security ID {} to security {}", position.getSecurityKey(), security);
            position = new PositionImpl(position.getUniqueIdentifier(), position.getQuantity(), position
                .getSecurityKey(), security);
          }
        }
        return new ComputationTarget(ComputationTargetType.POSITION, position);
      }
      case PORTFOLIO_NODE: {
        PortfolioNode node = getPositionMaster().getPortfolioNode(uid);
        if (node != null) {
          s_logger.info("Resolved multiple-position UID {} to portfolio node {}", uid, node);
          return new ComputationTarget(ComputationTargetType.PORTFOLIO_NODE, resolvePortfolioNode(uid, node));
        }
        final Portfolio portfolio = getPositionMaster().getPortfolio(uid);
        if (portfolio != null) {
          s_logger.info("Resolved multiple-position UID {} to portfolio {}", uid, portfolio);
          node = portfolio.getRootNode();
          return new ComputationTarget(ComputationTargetType.PORTFOLIO_NODE, new PortfolioImpl(portfolio
              .getUniqueIdentifier(), portfolio.getName(), resolvePortfolioNode(uid, node)));
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
    final PortfolioNodeImpl newNode = new PortfolioNodeImpl(node.getUniqueIdentifier(), node.getName());
    for (PortfolioNode child : node.getChildNodes()) {
      final ComputationTarget resolvedChild = getRecursiveResolver().resolve(
          new ComputationTargetSpecification(ComputationTargetType.PORTFOLIO_NODE, child.getUniqueIdentifier()));
      if (resolvedChild == null) {
        s_logger.warn("Portfolio node ID {} couldn't be resolved for portfolio node ID {}",
            child.getUniqueIdentifier(), uid);
      } else {
        newNode.addChildNode(resolvedChild.getPortfolioNode());
      }
    }
    for (Position position : node.getPositions()) {
      final ComputationTarget resolvedPosition = getRecursiveResolver().resolve(
          new ComputationTargetSpecification(ComputationTargetType.POSITION, position.getUniqueIdentifier()));
      if (resolvedPosition == null) {
        s_logger.warn("Position ID {} couldn't be resolved for portfolio node ID {}", position.getUniqueIdentifier(),
            uid);
      } else {
        newNode.addPosition(resolvedPosition.getPosition());
      }
    }
    return newNode;
  }

}
