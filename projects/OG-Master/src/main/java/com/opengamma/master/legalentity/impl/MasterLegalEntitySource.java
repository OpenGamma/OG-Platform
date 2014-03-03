/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.legalentity.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.AbstractSourceWithExternalBundle;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.legalentity.LegalEntity;
import com.opengamma.core.legalentity.LegalEntitySource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.AbstractMasterSource;
import com.opengamma.master.legalentity.LegalEntityDocument;
import com.opengamma.master.legalentity.LegalEntityMaster;
import com.opengamma.master.legalentity.LegalEntitySearchRequest;
import com.opengamma.master.legalentity.ManageableLegalEntity;
import com.opengamma.util.paging.PagingRequest;

/**
 * LegalEntity source based on the master.
 */
public class MasterLegalEntitySource extends AbstractMasterSource<LegalEntity, LegalEntityDocument, LegalEntityMaster> implements LegalEntitySource {

  /**
   * Creates an instance with an underlying master.
   *
   * @param master the master, not null
   */
  public MasterLegalEntitySource(final LegalEntityMaster master) {
    super(master);
  }

  @Override
  public ChangeManager changeManager() {
    return getMaster().changeManager();
  }

  @SuppressWarnings({"unchecked", "rawtypes" })
  @Override
  public Collection<LegalEntity> get(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    LegalEntitySearchRequest searchRequest = new LegalEntitySearchRequest(bundle);
    searchRequest.setVersionCorrection(versionCorrection);
    return (List) getMaster().search(searchRequest).getLegalEntities();
  }

  @Override
  public ManageableLegalEntity getSingle(ExternalId identifier) {
    return getSingle(identifier.toBundle());
  }

  @Override
  public ManageableLegalEntity getSingle(ExternalIdBundle identifiers) {
    return getSingle(identifiers, VersionCorrection.LATEST);
  }

  @Override
  public Map<ExternalIdBundle, Collection<LegalEntity>> getAll(Collection<ExternalIdBundle> bundles, VersionCorrection versionCorrection) {
    return AbstractSourceWithExternalBundle.getAll(this, bundles, versionCorrection);
  }

  @Override
  public Collection<LegalEntity> get(ExternalIdBundle bundle) {
    return get(bundle, VersionCorrection.LATEST);
  }

  @Override
  public ManageableLegalEntity getSingle(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    LegalEntitySearchRequest searchRequest = new LegalEntitySearchRequest(bundle);
    searchRequest.setPagingRequest(PagingRequest.ONE);
    searchRequest.setVersionCorrection(versionCorrection);
    ManageableLegalEntity firstLegalEntity = getMaster().search(searchRequest).getFirstLegalEntity();
    if (firstLegalEntity == null) {
      throw new DataNotFoundException("No legal entity: " + bundle + " " + versionCorrection);
    }
    return firstLegalEntity;
  }

  @Override
  public Map<ExternalIdBundle, LegalEntity> getSingle(Collection<ExternalIdBundle> bundles, VersionCorrection versionCorrection) {
    return AbstractSourceWithExternalBundle.getSingle(this, bundles, versionCorrection);
  }
}
