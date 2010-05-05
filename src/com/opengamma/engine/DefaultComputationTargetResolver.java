/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine;

import java.util.Collection;

import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.position.PositionMaster;
import com.opengamma.engine.security.Security;
import com.opengamma.engine.security.SecurityMaster;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
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
    Identifier identifier = specification.getIdentifier();
    switch (specification.getType()) {
      case PRIMITIVE: {
        return new ComputationTarget(specification.getType(), identifier);
      }
      case SECURITY: {
        Security security = resolveSecurity(identifier);
        s_logger.info("Resolved security ID {} to security {}", identifier, security);
        if (security != null) {
          return new ComputationTarget(ComputationTargetType.SECURITY, security);
        }
        break;
      }
      case POSITION: {
        UniqueIdentifier uid = UniqueIdentifier.of(identifier.getScheme().getName(), identifier.getValue());
        Position position = getPositionMaster().getPosition(uid);
        s_logger.info("Resolved position ID {} to position {}", identifier, position);
        if (position != null) {
          return new ComputationTarget(ComputationTargetType.POSITION, position);
        }
        break;
      }
      case MULTIPLE_POSITIONS: {
        UniqueIdentifier uid = UniqueIdentifier.of(identifier.getScheme().getName(), identifier.getValue());
        Portfolio portfolio = getPositionMaster().getPortfolio(uid);
        s_logger.info("Resolved portfolio node ID {} to portfolio node {}", identifier, portfolio);
        if (portfolio != null) {
          return new ComputationTarget(ComputationTargetType.MULTIPLE_POSITIONS, portfolio);
        }
        PortfolioNode node = getPositionMaster().getPortfolioNode(uid);
        s_logger.info("Resolved portfolio node ID {} to portfolio node {}", identifier, node);
        if (node != null) {
          return new ComputationTarget(ComputationTargetType.MULTIPLE_POSITIONS, node);
        }
        break;
      }
      default: {
        throw new OpenGammaRuntimeException("Unhandled computation target type: " + specification.getType());
      }
    }
    return null;
  }

  /**
   * Resolves a security from the security master.
   * @param identifier  the identifier to resolve.
   * @return the security, not null
   */
  private Security resolveSecurity(Identifier identifier) {
    if (ObjectUtils.equals(Security.SECURITY_IDENTITY_KEY_DOMAIN, identifier.getScheme())) {
      return getSecurityMaster().getSecurity(identifier);
    }
    // must not be an "identity key", so try a regular identifier in the bundle
    IdentifierBundle bundle = new IdentifierBundle(identifier);
    Collection<Security> securities = getSecurityMaster().getSecurities(bundle);
    if (securities.size() > 1) {
      s_logger.warn("Got more than one result for {}:{}",identifier, securities);
    }
    if (securities.isEmpty()) {
      return null;
    } else {
      return securities.iterator().next();
    }
  }

}
