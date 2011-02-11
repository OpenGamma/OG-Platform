/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.security.impl;

import java.util.Collection;

import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.AbstractMasterSource;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicSPI;

/**
 * A {@code SecuritySource} implemented using an underlying {@code SecurityMaster}.
 * <p>
 * The {@link SecuritySource} interface provides securities to the engine via a narrow API.
 * This class provides the source on top of a standard {@link SecurityMaster}.
 */
@PublicSPI
public class MasterSecuritySource extends AbstractMasterSource<SecurityDocument, SecurityMaster> implements SecuritySource {

  /**
   * Creates an instance with an underlying master which does not override versions.
   * 
   * @param master  the master, not null
   */
  public MasterSecuritySource(final SecurityMaster master) {
    super(master);
  }

  /**
   * Creates an instance with an underlying master optionally overriding the requested version.
   * 
   * @param master  the master, not null
   * @param versionCorrection  the version-correction locator to search at, null to not override versions
   */
  public MasterSecuritySource(final SecurityMaster master, VersionCorrection versionCorrection) {
    super(master, versionCorrection);
  }

  //-------------------------------------------------------------------------
  @Override
  public ManageableSecurity getSecurity(UniqueIdentifier uniqueId) {
    SecurityDocument doc = getDocument(uniqueId);
    return (doc != null ? doc.getSecurity() : null);
  }

  @SuppressWarnings({"unchecked", "rawtypes" })
  @Override
  public Collection<Security> getSecurities(final IdentifierBundle securityKey) {
    ArgumentChecker.notNull(securityKey, "securityKey");
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.addSecurityKeys(securityKey);
    request.setVersionCorrection(getVersionCorrection());
    return (Collection) search(request).getSecurities();  // cast safe as supplied list will not be altered
  }

  @Override
  public Security getSecurity(final IdentifierBundle securityKey) {
    ArgumentChecker.notNull(securityKey, "securityKey");
    final Collection<Security> securities = getSecurities(securityKey);
    // simply picks the first returned security
    return securities.isEmpty() ? null : securities.iterator().next();
  }

  //-------------------------------------------------------------------------
  /**
   * Searches for securities matching the specified search criteria.
   * 
   * @param request  the search request, not null
   * @return the search result, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  public SecuritySearchResult search(final SecuritySearchRequest request) {
    return getMaster().search(request);
  }

}
