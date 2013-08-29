/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.orgs;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.UniqueId;
import com.opengamma.master.AbstractChangeProvidingMaster;
import com.opengamma.util.PublicSPI;

/**
 * A general-purpose organization master.
 * <p/>
 * The organization master provides a uniform view over a set of organization definitions.
 * This interface provides methods that allow the master to be searched and updated.
 */
@PublicSPI
public interface OrganizationMaster extends AbstractChangeProvidingMaster<OrganizationDocument> {

  /**
   * Searches for organizations matching the specified search criteria.
   * <p/>
   *
   * @param request the search request, not null
   * @return the search result, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  OrganizationSearchResult search(OrganizationSearchRequest request);

  /**
   * Queries the history of a single organization.
   * <p/>
   * The request must contain an object identifier to identify the organization.
   *
   * @param request the history request, not null
   * @return the organization history, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  OrganizationHistoryResult history(OrganizationHistoryRequest request);

  /**
   * Gets a organization by unique identifier.
   * <p/>
   * If the master supports history then the version in the identifier will be used
   * to return the requested historic version.
   *
   * @param uid the organization unique identifier, not null
   * @return the organization, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no organization with that unique identifier
   */
  ManageableOrganization getOrganization(UniqueId uid);

}
