/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.master;

import java.util.Collection;

import javax.time.Instant;
import javax.time.InstantProvider;

import com.opengamma.engine.security.Security;
import com.opengamma.engine.security.SecuritySource;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
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
   * @param securityMaster  the security master, not null
   */
  public MasterSecuritySource(final SecurityMaster securityMaster) {
    this(securityMaster, null, null);
  }

  /**
   * Creates an instance with an underlying security master viewing the version
   * that existed on the specified instant.
   * @param securityMaster  the security master, not null
   * @param versionAsOfInstantProvider  the version instant to retrieve, null for latest version
   */
  public MasterSecuritySource(final SecurityMaster securityMaster, InstantProvider versionAsOfInstantProvider) {
    this(securityMaster, versionAsOfInstantProvider, null);
  }

  /**
   * Creates an instance with an underlying security master viewing the version
   * that existed on the specified instant as corrected to the correction instant.
   * @param securityMaster  the security master, not null
   * @param versionAsOfInstantProvider  the version instant to retrieve, null for latest version
   * @param correctedToInstantProvider  the instant that the data should be corrected to, null for latest correction
   */
  public MasterSecuritySource(final SecurityMaster securityMaster, InstantProvider versionAsOfInstantProvider, InstantProvider correctedToInstantProvider) {
    ArgumentChecker.notNull(securityMaster, "positionMaster");
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
  @Override
  public Security getSecurity(final UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    SecuritySearchHistoricRequest request = new SecuritySearchHistoricRequest(uid, _versionAsOfInstant, _correctedToInstant);
    request.setFullDetail(true);
    SecuritySearchHistoricResult result = _securityMaster.searchHistoric(request);
    if (result.getDocuments().size() == 1) {
      return result.getDocuments().get(0).getSecurity();
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<Security> getSecurities(final IdentifierBundle securityKey) {
    ArgumentChecker.notNull(securityKey, "securityKey");
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.setVersionAsOfInstant(_versionAsOfInstant);
    request.setCorrectedToInstant(_correctedToInstant);
    request.setIdentityKey(securityKey);
    request.setFullDetail(true);
    return (Collection) _securityMaster.search(request).getSecurities();
  }

  @Override
  public Security getSecurity(final IdentifierBundle securityKey) {
    ArgumentChecker.notNull(securityKey, "securityKey");
    final Collection<Security> securities = getSecurities(securityKey);
    // simply picks the first returned security
    return securities.isEmpty() ? null : securities.iterator().next();
  }

}
