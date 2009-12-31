/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine;

import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.position.Position;
import com.opengamma.engine.position.PositionMaster;
import com.opengamma.engine.security.Security;
import com.opengamma.engine.security.SecurityMaster;
import com.opengamma.engine.view.ViewProcessingContext;
import com.opengamma.util.ArgumentChecker;

/**
 * Adheres to the {@link ComputationTargetResolver} interface, satisfying results using
 * elements of the {@link ViewProcessingContext}.
 *
 * @author kirk
 */
public class DefaultComputationTargetResolver implements ComputationTargetResolver {
  private static final Logger s_logger = LoggerFactory.getLogger(DefaultComputationTargetResolver.class);
  private final SecurityMaster _securityMaster;
  private final PositionMaster _positionMaster;
  
  public DefaultComputationTargetResolver(SecurityMaster securityMaster, PositionMaster positionMaster) {
    ArgumentChecker.checkNotNull(securityMaster, "Security Master");
    ArgumentChecker.checkNotNull(positionMaster, "Position master");
    _securityMaster = securityMaster;
    _positionMaster = positionMaster;
  }

  /**
   * @return the securityMaster
   */
  public SecurityMaster getSecurityMaster() {
    return _securityMaster;
  }

  /**
   * @return the positionMaster
   */
  public PositionMaster getPositionMaster() {
    return _positionMaster;
  }

  @Override
  public ComputationTarget resolve(
      ComputationTargetSpecification targetSpecification) {
    switch(targetSpecification.getType()) {
    case PRIMITIVE:
      return new ComputationTarget(targetSpecification.getType(), targetSpecification.getIdentifier());
    case SECURITY:
      Security security = getSecurityMaster().getSecurity(targetSpecification.getIdentifier());
      s_logger.info("Resolved security ID {} to security {}", targetSpecification.getIdentifier(), security);
      if(security == null) {
        return null;
      } else {
        return new ComputationTarget(ComputationTargetType.SECURITY, security);
      }
    case POSITION:
      Position position = getPositionMaster().getPosition(targetSpecification.getIdentifier());
      s_logger.info("Resolved position ID {} to security {}", targetSpecification.getIdentifier(), position);
      if(position == null) {
        return null;
      } else {
        return new ComputationTarget(ComputationTargetType.POSITION, position);
      }
    default:
      throw new NotImplementedException("Unable to handle more than primitive and security lookups yet.");
    }
  }

}
