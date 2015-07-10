/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.security.impl;

import java.util.Collection;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.Link;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecurityResolver;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * An abstract base implementation of {@code SecurityResolver}.
 * <p>
 * This resolver implementation obtains the securities from {@link SecuritySource}. It uses a single fixed instance variable of {@link VersionCorrection}. Resolution between the candidate options
 * returned for an external identifier bundle is determined by the subclass.
 */
public abstract class AbstractSecurityResolver implements SecurityResolver {

  /**
   * The underlying source of securities.
   */
  private final SecuritySource _securitySource;
  /**
   * The version-correction at which the resolver operates.
   */
  private final VersionCorrection _versionCorrection;

  /**
   * Creates an instance decorating a {@code SecuritySource}.
   * <p>
   * It is recommended to use a locked version-correction rather than one with "latest" wherever possible.
   * 
   * @param securitySource the source of securities, not null
   * @param versionCorrection the version-correction at which the resolver will operate, not null
   * @throws IllegalArgumentException if either version-correction instant is "latest"
   */
  protected AbstractSecurityResolver(SecuritySource securitySource, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(securitySource, "securitySource");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    _securitySource = securitySource;
    _versionCorrection = versionCorrection;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the underlying security source.
   * 
   * @return the security source, not null
   */
  public SecuritySource getSecuritySource() {
    return _securitySource;
  }

  /**
   * Gets the version-correction at which the resolver operates.
   * 
   * @return the version-correction, not null
   */
  public VersionCorrection getVersionCorrection() {
    return _versionCorrection;
  }

  //-------------------------------------------------------------------------
  @Override
  public Security resolve(Link<Security> link) {
    ArgumentChecker.notNull(link, "link");
    ObjectId objectId = link.getObjectId();
    if (objectId != null) {
      return getSecurity(objectId);
    }
    ExternalIdBundle externalId = link.getExternalId();
    if (externalId.isEmpty() == false) {
      return getSecurity(externalId);
    }
    throw new DataNotFoundException("Link " + link + " does not contain any references");
  }

  @Override
  public Security getSecurity(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    return getSecuritySource().get(uniqueId);
  }

  @Override
  public Security getSecurity(ObjectId objectId) {
    ArgumentChecker.notNull(objectId, "objectId");
    return getSecuritySource().get(objectId, getVersionCorrection());
  }

  @Override
  public Security getSecurity(ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    Collection<? extends Security> securities = getSecuritySource().get(bundle, getVersionCorrection());
    if (securities.isEmpty()) {
      throw new DataNotFoundException("Security not found: " + bundle + " at " + getVersionCorrection());
    }
    return selectBestMatch(securities);
  }

  //-------------------------------------------------------------------------
  /**
   * Selects a single security from one or more candidates.
   * <p>
   * The selection of a "best match" distinguishes one implementation from another.
   * 
   * @param candidates the candidate securities, not empty, not null
   * @return the best matching security, not null
   */
  protected abstract Security selectBestMatch(Collection<? extends Security> candidates);

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return getClass().getSimpleName() +
        "[versionCorrection=" + getVersionCorrection() +
        ", securitySource=" + getSecuritySource() + "]";
  }

}
