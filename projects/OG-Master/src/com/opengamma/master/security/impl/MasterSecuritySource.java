/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.security.impl;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Maps;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
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
  public ManageableSecurity getSecurity(UniqueId uniqueId) {
    return getDocument(uniqueId).getSecurity();
  }
  
  @Override
  public Map<UniqueId, Security> getSecurity(Collection<UniqueId> uniqueIds) {
    Map<UniqueId, Security> result = Maps.newHashMap();
    for (UniqueId uniqueId : uniqueIds) {
      try {
        ManageableSecurity security = getSecurity(uniqueId);
        result.put(uniqueId, security);
      } catch (DataNotFoundException ex) {
        // do nothing
      }
    }
    return result;
  }

  @Override
  public Security getSecurity(ObjectId objectId, VersionCorrection versionCorrection) {
    return getDocument(objectId, versionCorrection).getSecurity();
  }

  @Override
  public Collection<Security> getSecurities(final ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    return getSecuritiesInternal(bundle, getVersionCorrection());
  }

  @Override
  public Collection<Security> getSecurities(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    VersionCorrection overrideVersionCorrection = getVersionCorrection();
    return getSecuritiesInternal(bundle, overrideVersionCorrection != null ? overrideVersionCorrection : versionCorrection);
  }

  @Override
  public Security getSecurity(final ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    Collection<Security> securities = getSecuritiesInternal(bundle, getVersionCorrection());
    // simply picks the first returned security
    return securities.isEmpty() ? null : securities.iterator().next();
  }

  @Override
  public Security getSecurity(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    VersionCorrection overrideVersionCorrection = getVersionCorrection();
    final Collection<Security> securities = getSecuritiesInternal(bundle, overrideVersionCorrection != null ? overrideVersionCorrection : versionCorrection);
    // simply picks the first returned security
    return securities.isEmpty() ? null : securities.iterator().next();
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private Collection<Security> getSecuritiesInternal(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalIds(bundle);
    request.setVersionCorrection(versionCorrection);
    return (Collection) search(request).getSecurities();  // cast safe as supplied list will not be altered    
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

  //-------------------------------------------------------------------------
  @Override
  public ChangeManager changeManager() {
    return getMaster().changeManager();
  }

}
