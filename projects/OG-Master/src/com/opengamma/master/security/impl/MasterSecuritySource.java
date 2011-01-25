/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.security.impl;

import java.util.Collection;

import javax.time.Instant;
import javax.time.InstantProvider;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityHistoryRequest;
import com.opengamma.master.security.SecurityHistoryResult;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.util.ArgumentChecker;

/**
 * A {@code SecuritySource} implemented using an underlying {@code SecurityMaster}.
 * <p>
 * The {@link SecuritySource} interface provides securities to the engine via a narrow API.
 * This class provides the source on top of a standard {@link SecurityMaster}.
 */
public class MasterSecuritySource implements SecuritySource {

  /**
   * The security master.
   */
  private final SecurityMaster _securityMaster;
  /**
   * The instant to search for a version at.
   * Null is treated as the latest version.
   */
  private final Instant _versionAsOfInstant;
  /**
   * The instant to search for corrections for.
   * Null is treated as the latest correction.
   */
  private final Instant _correctedToInstant;

  /**
   * Creates an instance with an underlying security master.
   * 
   * @param securityMaster  the security master, not null
   */
  public MasterSecuritySource(final SecurityMaster securityMaster) {
    this(securityMaster, null, null);
  }

  /**
   * Creates an instance with an underlying security master viewing the version
   * that existed on the specified instant.
   * 
   * @param securityMaster  the security master, not null
   * @param versionAsOfInstantProvider  the version instant to retrieve, null for latest version
   */
  public MasterSecuritySource(final SecurityMaster securityMaster, InstantProvider versionAsOfInstantProvider) {
    this(securityMaster, versionAsOfInstantProvider, null);
  }

  /**
   * Creates an instance with an underlying security master viewing the version
   * that existed on the specified instant as corrected to the correction instant.
   * 
   * @param securityMaster  the security master, not null
   * @param versionAsOfInstantProvider  the version instant to retrieve, null for latest version
   * @param correctedToInstantProvider  the instant that the data should be corrected to, null for latest correction
   */
  public MasterSecuritySource(final SecurityMaster securityMaster, InstantProvider versionAsOfInstantProvider, InstantProvider correctedToInstantProvider) {
    ArgumentChecker.notNull(securityMaster, "securityMaster");
    _securityMaster = securityMaster;
    if (versionAsOfInstantProvider != null) {
      _versionAsOfInstant = Instant.of(versionAsOfInstantProvider);
    } else {
      _versionAsOfInstant = null;
    }
    if (correctedToInstantProvider != null) {
      _correctedToInstant = Instant.of(correctedToInstantProvider);
    } else {
      _correctedToInstant = null;
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the underlying security master.
   * 
   * @return the security master, not null
   */
  public SecurityMaster getSecurityMaster() {
    return _securityMaster;
  }

  /**
   * Gets the version instant to retrieve.
   * 
   * @return the version instant to retrieve, null for latest version
   */
  public Instant getVersionAsOfInstant() {
    return _versionAsOfInstant;
  }

  /**
   * Gets the instant that the data should be corrected to.
   * 
   * @return the instant that the data should be corrected to, null for latest correction
   */
  public Instant getCorrectedToInstant() {
    return _correctedToInstant;
  }

  //-------------------------------------------------------------------------
  @Override
  public ManageableSecurity getSecurity(final UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    if (getVersionAsOfInstant() != null || getCorrectedToInstant() != null) {
      // use defined instants
      SecurityHistoryRequest portfolioSearch = new SecurityHistoryRequest(uid.toLatest(), getVersionAsOfInstant(), getCorrectedToInstant());
      SecurityHistoryResult portfolios = getSecurityMaster().history(portfolioSearch);
      return portfolios.getFirstSecurity();
    } else {
      // match by uid
      try {
        return getSecurityMaster().get(uid).getSecurity();
      } catch (DataNotFoundException e) {
        return null;
      }
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes" })
  @Override
  public Collection<Security> getSecurities(final IdentifierBundle securityKey) {
    ArgumentChecker.notNull(securityKey, "securityKey");
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.addSecurityKeys(securityKey);
    request.setVersionAsOfInstant(_versionAsOfInstant);
    request.setCorrectedToInstant(_correctedToInstant);
    return (Collection) getSecurityMaster().search(request).getSecurities();  // cast safe as supplied list will not be altered
  }

  @Override
  public Security getSecurity(final IdentifierBundle securityKey) {
    ArgumentChecker.notNull(securityKey, "securityKey");
    final Collection<Security> securities = getSecurities(securityKey);
    // simply picks the first returned security
    return securities.isEmpty() ? null : securities.iterator().next();
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    String str = "MasterSecuritySource[" + getSecurityMaster();
    if (_versionAsOfInstant != null) {
      str += ",versionAsOf=" + _versionAsOfInstant;
    }
    if (_versionAsOfInstant != null) {
      str += ",correctedTo=" + _correctedToInstant;
    }
    return str + "]";
  }

}
