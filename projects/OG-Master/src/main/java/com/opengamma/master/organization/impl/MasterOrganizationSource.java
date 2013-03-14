/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma
 group of companies
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
import com.opengamma.master.orgs.ManageableOrganisation;
import com.opengamma.master.orgs.OrganisationDocument;
import com.opengamma.master.orgs.OrganisationMaster;
import com.opengamma.master.orgs.OrganisationSearchRequest;

public class MasterOrganizationSource extends AbstractMasterSource<Organization, OrganisationDocument, OrganisationMaster>
    implements OrganizationSource {

  /**
   * Creates an instance with an underlying master which does not override versions.
   *
   * @param master the master, not null
   */
  public MasterOrganizationSource(final OrganisationMaster master) {
    super(master);
  }

  /**
   * Creates an instance with an underlying master optionally overriding the requested version.
   *
   * @param master the master, not null
   * @param versionCorrection  the version-correction locator to search at, null to not override versions
   */
  public MasterOrganizationSource(final OrganisationMaster master, VersionCorrection versionCorrection) {
    super(master, versionCorrection);
  }

  @Override
  public ChangeManager changeManager() {
    return getMaster().changeManager();
  }

  @Override
  public Organization getOrganizationByRedCode(String redCode) {

    OrganisationSearchRequest request = new OrganisationSearchRequest();
    request.setObligorREDCode(redCode);
    return searchForSingleOrganization(request);
  }

  private ManageableOrganisation searchForSingleOrganization(OrganisationSearchRequest request) {
    return getMaster().search(request).getSingleOrganisation();
  }

  @Override
  public Organization getOrganizationByTicker(String ticker) {
    OrganisationSearchRequest request = new OrganisationSearchRequest();
    request.setObligorTicker(ticker);
    return searchForSingleOrganization(request);
  }

  @Override
  public Organization get(UniqueId uniqueId) {
    return getMaster().getOrganisation(uniqueId);
  }

  @Override
  public Organization get(ObjectId objectId, VersionCorrection versionCorrection) {
    OrganisationDocument document = getMaster().get(objectId, versionCorrection);
    return document == null ? null : document.getOrganisation();
  }

  @Override
  public Map<UniqueId, Organization> get(Collection<UniqueId> uniqueIds) {

    Map<UniqueId, OrganisationDocument> documents = getMaster().get(uniqueIds);
    return Maps.transformValues(documents,
           new Function<OrganisationDocument, Organization>() {
             @Override
             public Organization apply(OrganisationDocument organisationDocument) {
               return organisationDocument.getOrganisation();
             }
           });
  }
}
