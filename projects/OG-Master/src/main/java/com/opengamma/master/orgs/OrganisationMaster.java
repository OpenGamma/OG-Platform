/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.orgs;

import com.opengamma.core.change.ChangeProvider;
import com.opengamma.id.UniqueId;
import com.opengamma.master.AbstractMaster;
import com.opengamma.util.PublicSPI;

/**
 * A general-purpose organisation master.
 * <p/>
 * The organisation master provides a uniform view over a set of organisation definitions.
 * This interface provides methods that allow the master to be searched and updated.
 */
@PublicSPI
public interface OrganisationMaster extends AbstractMaster<OrganisationDocument>, ChangeProvider {

  /**
   * Searches for organisations matching the specified search criteria.
   * <p/>
   *
   * @param request the search request, not null
   * @return the search result, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  OrganisationSearchResult search(OrganisationSearchRequest request);

  /**
   * Queries the history of a single organisation.
   * <p/>
   * The request must contain an object identifier to identify the organisation.
   *
   * @param request the history request, not null
   * @return the organisation history, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  OrganisationHistoryResult history(OrganisationHistoryRequest request);

  /**
   * Gets a organisation by unique identifier.
   * <p/>
   * If the master supports history then the version in the identifier will be used
   * to return the requested historic version.
   *
   * @param uid the organisation unique identifier, not null
   * @return the organisation, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException    if there is no organisation with that unique identifier
   */
  ManageableOrganisation getOrganisation(UniqueId uid);
}
