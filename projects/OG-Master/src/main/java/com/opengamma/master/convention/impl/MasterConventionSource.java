/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.convention.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.AbstractSourceWithExternalBundle;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.convention.Convention;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.AbstractMasterSource;
import com.opengamma.master.convention.ConventionDocument;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.master.convention.ConventionSearchRequest;
import com.opengamma.master.convention.ConventionSearchResult;
import com.opengamma.master.convention.ManageableConvention;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicSPI;

/**
 * A {@code ConventionSource} implemented using an underlying {@code ConventionMaster}.
 * <p>
 * The {@link ConventionSource} interface provides conventions to the engine via a narrow API.
 * This class provides the source on top of a standard {@link ConventionMaster}.
 */
@PublicSPI
public class MasterConventionSource
    extends AbstractMasterSource<Convention, ConventionDocument, ConventionMaster>
    implements ConventionSource {

  /**
   * Creates an instance with an underlying master which does not override versions.
   * 
   * @param master the master, not null
   */
  public MasterConventionSource(final ConventionMaster master) {
    super(master);
  }

  /**
   * Creates an instance with an underlying master optionally overriding the requested version.
   * 
   * @param master the master, not null
   * @param versionCorrection the version-correction locator to search at, null to not override versions
   */
  public MasterConventionSource(final ConventionMaster master, VersionCorrection versionCorrection) {
    super(master, versionCorrection);
  }

  //-------------------------------------------------------------------------
  @Override
  public Collection<Convention> get(ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    return get(bundle, VersionCorrection.LATEST);
  }

  @Override
  public Collection<Convention> get(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    VersionCorrection overrideVersionCorrection = getVersionCorrection();

    Collection<Convention> conventions = new ArrayList<Convention>();
    for (ManageableConvention manageableConvention : getSecuritiesInternal(bundle, overrideVersionCorrection != null ? overrideVersionCorrection : versionCorrection)) {
      conventions.add(manageableConvention);
    }
    return conventions;
  }

  @Override
  public Map<ExternalIdBundle, Collection<Convention>> getAll(final Collection<ExternalIdBundle> bundle, final VersionCorrection versionCorrection) {
    return AbstractSourceWithExternalBundle.getAll(this, bundle, versionCorrection);
  }

  @Override
  public Convention getSingle(ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    return getSingle(bundle, VersionCorrection.LATEST);
  }

  @Override
  public ManageableConvention getSingle(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    VersionCorrection overrideVersionCorrection = getVersionCorrection();
    final Collection<ManageableConvention> conventions = getSecuritiesInternal(bundle, overrideVersionCorrection != null ? overrideVersionCorrection : versionCorrection);
    if (conventions.isEmpty()) {
      throw new DataNotFoundException("No convention found: " + bundle);
    }
    // simply picks the first returned convention
    return conventions.iterator().next();
  }

  @Override
  public Map<ExternalIdBundle, Convention> getSingle(final Collection<ExternalIdBundle> bundles, final VersionCorrection versionCorrection) {
    return AbstractSourceWithExternalBundle.getSingle(this, bundles, versionCorrection);
  }

  @SuppressWarnings({"rawtypes", "unchecked" })
  private Collection<ManageableConvention> getSecuritiesInternal(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalIds(bundle);
    request.setVersionCorrection(versionCorrection);
    return (Collection) search(request).getConventions(); // cast safe as supplied list will not be altered    
  }

  //-------------------------------------------------------------------------
  @Override
  public <T extends Convention> T get(UniqueId uniqueId, Class<T> type) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(type, "type");
    Convention convention = get(uniqueId);
    return type.cast(convention);
  }

  @Override
  public <T extends Convention> T get(ObjectId objectId, VersionCorrection versionCorrection, Class<T> type) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    ArgumentChecker.notNull(type, "type");
    Convention convention = get(objectId, versionCorrection);
    return type.cast(convention);
  }

  @Override
  public <T extends Convention> T getSingle(ExternalIdBundle bundle, VersionCorrection versionCorrection, Class<T> type) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    ArgumentChecker.notNull(type, "type");
    Convention convention = getSingle(bundle, versionCorrection);
    return type.cast(convention);
  }

  //-------------------------------------------------------------------------
  /**
   * Searches for conventions matching the specified search criteria.
   * 
   * @param request the search request, not null
   * @return the search result, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  public ConventionSearchResult search(final ConventionSearchRequest request) {
    return getMaster().search(request);
  }

  //-------------------------------------------------------------------------
  @Override
  public ChangeManager changeManager() {
    return getMaster().changeManager();
  }

}
