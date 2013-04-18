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
 * Simple implementation of {@code SecurityResolver} that picks the first option.
 * <p>
 * Resolution of a single security from multiple candidate securities is performed by selecting the first. Since the input is not necessarily sorted, this may be random.
 */
public class SimpleSecurityResolver extends AbstractSecurityResolver {

  /**
   * Creates an instance decorating a {@code SecuritySource}.
   * <p>
   * It is recommended to use a locked version-correction rather than one with "latest" wherever possible.
   * 
   * @param securitySource the source of securities, not null
   * @param versionCorrection the version-correction at which the resolver will operate, not null
   * @throws IllegalArgumentException if either version-correction instant is "latest"
   */
  public SimpleSecurityResolver(SecuritySource securitySource, VersionCorrection versionCorrection) {
    super(securitySource, versionCorrection);
  }

  //-------------------------------------------------------------------------
  @Override
  protected Security selectBestMatch(Collection<? extends Security> candidates) {
    return candidates.iterator().next();
  }

}
