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
 * An abstract base implementation of {@link SecurityResolver}.
 * <p>
 * This implements the core functionality for obtaining one or more candidate securities
 * using a {@link SecuritySource} and {@link VersionCorrection}. Resolution between
 * candidates is delegated.
 */
public abstract class BaseSecurityResolver implements SecurityResolver {

  /**
   * The underlying source of securities.
   */
  private final SecuritySource _securitySource;
  /**
   * The version-correction at which the resolver operates.
   */
  private final VersionCorrection _versionCorrection;
  
  /**
   * Base constructor.
   * 
   * @param securitySource  a source of securities, not null
   * @param versionCorrection  the version-correction at which the resolver will operate, not null
   * @throws IllegalArgumentException  if the version-correction does not specify both an
   *                                   exact version and correction
   */
  protected BaseSecurityResolver(SecuritySource securitySource, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(securitySource, "securitySource");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    if (versionCorrection.containsLatest()) {
      throw new IllegalArgumentException("The version-correction " + versionCorrection + " does not specify both an exact version and correction");
    }
    _securitySource = securitySource;
    _versionCorrection = versionCorrection;
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
    if (externalId != null && !externalId.isEmpty()) {
      return getSecurity(externalId);
    }
    throw new DataNotFoundException("Link " + link + " does not contain any references");
  }

  @Override
  public Security getSecurity(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    Security security = getSecuritySource().getSecurity(uniqueId);
    if (security == null) {
      throw new DataNotFoundException("Security not found: " + uniqueId);
    }
    return security;
  }

  @Override
  public Security getSecurity(ObjectId objectId) {
    ArgumentChecker.notNull(objectId, "objectId");
    Security security = getSecuritySource().getSecurity(objectId, getVersionCorrection());
    if (security == null) {
      throw new DataNotFoundException("Security not found: " + objectId + " at version-correction " + getVersionCorrection());
    }
    return security;
  }

  @Override
  public Security getSecurity(ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    Collection<Security> securities = getSecuritySource().getSecurities(bundle, getVersionCorrection());
    if (securities == null || securities.isEmpty()) {
      throw new DataNotFoundException("Security not found: " + bundle + " at version-correction " + getVersionCorrection());
    }
    return resolve(securities);
  }
  
  //-------------------------------------------------------------------------
  /**
   * Selects a single security from one or more candidates.
   * <p>
   * The selection of a "best match" distinguishes one implementation from another.
   * 
   * @param candidates  the candidate securities, not null or empty
   * @return the best matching candidate security, not null
   */
  protected abstract Security resolve(Collection<Security> candidates);
  
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

}
