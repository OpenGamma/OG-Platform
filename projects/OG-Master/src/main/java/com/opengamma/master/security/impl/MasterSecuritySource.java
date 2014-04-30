/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.security.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.security.AbstractSecuritySource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.ExternalIdBundle;
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
 * The {@link SecuritySource} interface provides securities to the engine via a narrow API. This class provides the source on top of a standard {@link SecurityMaster}.
 */
@PublicSPI
public class MasterSecuritySource extends AbstractMasterSource<Security, SecurityDocument, SecurityMaster> implements SecuritySource {

  /**
   * Creates an instance with an underlying master.
   * 
   * @param master the master, not null
   */
  public MasterSecuritySource(final SecurityMaster master) {
    super(master);
  }

  //-------------------------------------------------------------------------
  @Override
  public Collection<Security> get(final ExternalIdBundle bundle) {
    return get(bundle, VersionCorrection.LATEST);
  }

  @Override
  public Collection<Security> get(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    Collection<Security> securities = new ArrayList<Security>();
    for (ManageableSecurity manageableSecurity : getSecuritiesInternal(bundle, versionCorrection)) {
      securities.add(manageableSecurity);
    }
    return securities;
  }

  @Override
  public Map<ExternalIdBundle, Collection<Security>> getAll(final Collection<ExternalIdBundle> bundle, final VersionCorrection versionCorrection) {
    return AbstractSecuritySource.getAll(this, bundle, versionCorrection);
  }

  @Override
  public ManageableSecurity getSingle(final ExternalIdBundle bundle) {
    return getSingle(bundle, VersionCorrection.LATEST);
  }

  @Override
  public ManageableSecurity getSingle(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    final Collection<ManageableSecurity> securities = getSecuritiesInternal(bundle, versionCorrection);
    // simply picks the first returned security
    return securities.isEmpty() ? null : securities.iterator().next();
  }

  @Override
  public Map<ExternalIdBundle, Security> getSingle(final Collection<ExternalIdBundle> bundle, final VersionCorrection versionCorrection) {
    return AbstractSecuritySource.getSingle(this, bundle, versionCorrection);
  }

  @SuppressWarnings({"rawtypes", "unchecked" })
  private Collection<ManageableSecurity> getSecuritiesInternal(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalIds(bundle);
    request.setVersionCorrection(versionCorrection);
    return (Collection) search(request).getSecurities(); // cast safe as supplied list will not be altered    
  }

  //-------------------------------------------------------------------------
  /**
   * Searches for securities matching the specified search criteria.
   * 
   * @param request the search request, not null
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
