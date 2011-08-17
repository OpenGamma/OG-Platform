/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.security.impl;

import java.util.Collection;

import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.VersionCorrection;

/**
 * Default implementation of {@link SecurityResolver}.
 * <p>
 * Resolution of a single security from multiple candidate securities is performed by
 * selecting the first.
 */
public class DefaultSecurityResolver extends BaseSecurityResolver {

  /**
   * Constructs an instance.
   * 
   * @param securitySource  a source of securities, not null
   * @param versionCorrection  the version-correction at which the resolver will operate, not null
   */
  protected DefaultSecurityResolver(SecuritySource securitySource, VersionCorrection versionCorrection) {
    super(securitySource, versionCorrection);
  }

  //-------------------------------------------------------------------------
  @Override
  protected Security resolve(Collection<Security> candidates) {
    return candidates.iterator().next();
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return "DefaultSecurityResolver[versionCorrection=" + getVersionCorrection() + ", securitySource=" + getSecuritySource() + "]";
  }

}
