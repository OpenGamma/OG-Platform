/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.security.Security;
import com.opengamma.engine.security.SecurityMaster;
import com.opengamma.util.ArgumentChecker;

/**
 * Adheres to the {@link ComputationTargetResolver} interface, satisfying results using
 * elements of the {@link ViewProcessingContext}.
 *
 * @author kirk
 */
public class ViewComputationTargetResolver implements ComputationTargetResolver {
  private static final Logger s_logger = LoggerFactory.getLogger(ViewComputationTargetResolver.class);
  private final SecurityMaster _securityMaster;
  
  public ViewComputationTargetResolver(SecurityMaster securityMaster) {
    ArgumentChecker.checkNotNull(securityMaster, "Security Master");
    _securityMaster = securityMaster;
  }

  /**
   * @return the securityMaster
   */
  public SecurityMaster getSecurityMaster() {
    return _securityMaster;
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
    default:
      throw new NotImplementedException("Unable to handle more than primitive and security lookups yet.");
    }
  }

}
