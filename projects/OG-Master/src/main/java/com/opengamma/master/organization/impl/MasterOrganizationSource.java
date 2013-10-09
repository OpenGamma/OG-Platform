/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.organization.impl;

import java.util.Collection;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.organization.Organization;
import com.opengamma.core.organization.OrganizationSource;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.AbstractMasterSource;
import com.opengamma.master.orgs.ManageableOrganization;
import com.opengamma.master.orgs.OrganizationDocument;
import com.opengamma.master.orgs.OrganizationMaster;
import com.opengamma.master.orgs.OrganizationSearchRequest;

/**
 * Organization source based on the master.
 */
public class MasterOrganizationSource
    extends AbstractMasterSource<Organization, OrganizationDocument, OrganizationMaster>
    implements OrganizationSource {

  /**
   * Creates an instance with an underlying master which does not override versions.
   *
   * @param master the master, not null
   */
  public MasterOrganizationSource(final OrganizationMaster master) {
    super(master);
  }

  /**
   * Creates an instance with an underlying master optionally overriding the requested version.
   *
   * @param master the master, not null
   * @param versionCorrection  the version-correction locator to search at, null to not override versions
   */
  public MasterOrganizationSource(final OrganizationMaster master, VersionCorrection versionCorrection) {
    super(master, versionCorrection);
  }

  @Override
  public ChangeManager changeManager() {
    return getMaster().changeManager();
  }

  @Override
  public Organization getOrganizationByRedCode(String redCode) {

    OrganizationSearchRequest request = new OrganizationSearchRequest();
    request.setObligorREDCode(redCode);
    return searchForSingleOrganization(request);
  }

  private ManageableOrganization searchForSingleOrganization(OrganizationSearchRequest request) {
    return getMaster().search(request).getSingleOrganization();
  }

  @Override
  public Organization getOrganizationByTicker(String ticker) {
    OrganizationSearchRequest request = new OrganizationSearchRequest();
    request.setObligorTicker(ticker);
    return searchForSingleOrganization(request);
  }

  @Override
  public Organization get(UniqueId uniqueId) {
    return getMaster().getOrganization(uniqueId);
  }

  @Override
  public Organization get(ObjectId objectId, VersionCorrection versionCorrection) {
    OrganizationDocument document = getMaster().get(objectId, versionCorrection);
    return document == null ? null : document.getOrganization();
  }

  @Override
  public Map<UniqueId, Organization> get(Collection<UniqueId> uniqueIds) {

    Map<UniqueId, OrganizationDocument> documents = getMaster().get(uniqueIds);
    return Maps.transformValues(documents,
        new Function<OrganizationDocument, Organization>() {
          @Override
          public Organization apply(OrganizationDocument organizationDocument) {
            return organizationDocument.getOrganization();
          }
        });
  }

}
