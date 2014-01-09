/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.organization.impl;

import com.opengamma.id.UniqueId;
import com.opengamma.master.AbstractDataTrackingMaster;
import com.opengamma.master.orgs.ManageableOrganization;
import com.opengamma.master.orgs.OrganizationDocument;
import com.opengamma.master.orgs.OrganizationHistoryRequest;
import com.opengamma.master.orgs.OrganizationHistoryResult;
import com.opengamma.master.orgs.OrganizationMaster;
import com.opengamma.master.orgs.OrganizationSearchRequest;
import com.opengamma.master.orgs.OrganizationSearchResult;

/**
 * Organization master which tracks accesses using UniqueIds.
 */
public class DataTrackingOrganizationMaster extends AbstractDataTrackingMaster<OrganizationDocument, OrganizationMaster> implements OrganizationMaster {
  
  public DataTrackingOrganizationMaster(OrganizationMaster delegate) {
    super(delegate);
  }

  @Override
  public OrganizationSearchResult search(OrganizationSearchRequest request) {
    OrganizationSearchResult searchResult = delegate().search(request);
    trackDocs(searchResult.getDocuments());
    return searchResult;
  }

  @Override
  public OrganizationHistoryResult history(OrganizationHistoryRequest request) {
    OrganizationHistoryResult historyResult = delegate().history(request);
    trackDocs(historyResult.getDocuments());
    return historyResult;
  }

  @Override
  public ManageableOrganization getOrganization(UniqueId uid) {
    ManageableOrganization organization = delegate().getOrganization(uid);
    trackId(organization.getUniqueId());
    return organization;
  }

  
}
