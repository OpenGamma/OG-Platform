/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.legalentity.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.AbstractSourceWithExternalBundle;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.legalentity.LegalEntity;
import com.opengamma.core.legalentity.LegalEntitySource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.AbstractMasterSource;
import com.opengamma.master.legalentity.LegalEntityDocument;
import com.opengamma.master.legalentity.LegalEntityMaster;
import com.opengamma.master.legalentity.LegalEntitySearchRequest;
import com.opengamma.master.legalentity.LegalEntitySearchResult;
import com.opengamma.master.legalentity.ManageableLegalEntity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicSPI;

/**
 * A {@code LegalEntitySource} implemented using an underlying {@code LegalEntityMaster}.
 * <p/>
 * The {@link LegalEntitySource} interface provides legalEntities to the engine via a narrow API.
 * This class provides the source on top of a standard {@link LegalEntityMaster}.
 */
@PublicSPI
public class MasterLegalEntitySource
    extends AbstractMasterSource<LegalEntity, LegalEntityDocument, LegalEntityMaster>
    implements LegalEntitySource {

  /**
   * Creates an instance with an underlying master which does not override versions.
   *
   * @param master the master, not null
   */
  public MasterLegalEntitySource(final LegalEntityMaster master) {
    super(master);
  }

  /**
   * Creates an instance with an underlying master optionally overriding the requested version.
   *
   * @param master            the master, not null
   * @param versionCorrection the version-correction locator to search at, null to not override versions
   */
  public MasterLegalEntitySource(final LegalEntityMaster master, VersionCorrection versionCorrection) {
    super(master, versionCorrection);
  }

  //-------------------------------------------------------------------------
  @Override
  public Collection<LegalEntity> get(ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    return get(bundle, VersionCorrection.LATEST);
  }

  @Override
  public Collection<LegalEntity> get(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    VersionCorrection overrideVersionCorrection = getVersionCorrection();

    Collection<LegalEntity> legalEntities = new ArrayList<LegalEntity>();
    for (ManageableLegalEntity manageableLegalEntity : getSecuritiesInternal(bundle, overrideVersionCorrection != null ? overrideVersionCorrection : versionCorrection)) {
      legalEntities.add(manageableLegalEntity);
    }
    return legalEntities;
  }

  @Override
  public Map<ExternalIdBundle, Collection<LegalEntity>> getAll(final Collection<ExternalIdBundle> bundle, final VersionCorrection versionCorrection) {
    return AbstractSourceWithExternalBundle.getAll(this, bundle, versionCorrection);
  }

  @Override
  public LegalEntity getSingle(ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    return getSingle(bundle, VersionCorrection.LATEST);
  }

  @Override
  public ManageableLegalEntity getSingle(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    VersionCorrection overrideVersionCorrection = getVersionCorrection();
    final Collection<ManageableLegalEntity> legalEntities = getSecuritiesInternal(bundle, overrideVersionCorrection != null ? overrideVersionCorrection : versionCorrection);
    if (legalEntities.isEmpty()) {
      throw new DataNotFoundException("No legalentity found: " + bundle);
    }
    // simply picks the first returned legalentity
    return legalEntities.iterator().next();
  }

  @Override
  public Map<ExternalIdBundle, LegalEntity> getSingle(final Collection<ExternalIdBundle> bundles, final VersionCorrection versionCorrection) {
    return AbstractSourceWithExternalBundle.getSingle(this, bundles, versionCorrection);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private Collection<ManageableLegalEntity> getSecuritiesInternal(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalIds(bundle);
    request.setVersionCorrection(versionCorrection);
    return (Collection) search(request).getLegalEntities(); // cast safe as supplied list will not be altered
  }

  //-------------------------------------------------------------------------
  @Override
  public <T extends LegalEntity> T get(UniqueId uniqueId, Class<T> type) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(type, "type");
    LegalEntity legalentity = get(uniqueId);
    return type.cast(legalentity);
  }

  @Override
  public <T extends LegalEntity> T get(ObjectId objectId, VersionCorrection versionCorrection, Class<T> type) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    ArgumentChecker.notNull(type, "type");
    LegalEntity legalentity = get(objectId, versionCorrection);
    return type.cast(legalentity);
  }

  @Override
  public LegalEntity getSingle(ExternalId externalId) {
    ArgumentChecker.notNull(externalId, "externalId");
    return getSingle(externalId.toBundle());
  }

  @Override
  public <T extends LegalEntity> T getSingle(ExternalId externalId, Class<T> type) {
    ArgumentChecker.notNull(externalId, "externalId");
    ArgumentChecker.notNull(type, "type");
    return getSingle(externalId.toBundle(), type);
  }

  @Override
  public <T extends LegalEntity> T getSingle(ExternalIdBundle bundle, Class<T> type) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(type, "type");
    VersionCorrection overrideVersionCorrection = getVersionCorrection();
    if (overrideVersionCorrection != null) {
      return getSingle(bundle, overrideVersionCorrection, type);
    } else {
      return getSingle(bundle, VersionCorrection.LATEST, type);
    }
  }

  @Override
  public <T extends LegalEntity> T getSingle(ExternalIdBundle bundle, VersionCorrection versionCorrection, Class<T> type) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    ArgumentChecker.notNull(type, "type");
    LegalEntity legalentity = getSingle(bundle, versionCorrection);
    return type.cast(legalentity);
  }

  //-------------------------------------------------------------------------

  /**
   * Searches for legalEntities matching the specified search criteria.
   *
   * @param request the search request, not null
   * @return the search result, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  public LegalEntitySearchResult search(final LegalEntitySearchRequest request) {
    return getMaster().search(request);
  }

  //-------------------------------------------------------------------------
  @Override
  public ChangeManager changeManager() {
    return getMaster().changeManager();
  }

}
