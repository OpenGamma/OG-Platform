/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.user;

import com.opengamma.core.change.ChangeProvider;
import com.opengamma.master.AbstractChangeProvidingMaster;
import com.opengamma.util.PublicSPI;

/**
 * A general-purpose role master.
 * <p/>
 * The role master provides a uniform view over a set of role definitions.
 * This interface provides methods that allow the master to be searched and updated.
 */
@PublicSPI
public interface RoleMaster extends AbstractChangeProvidingMaster<RoleDocument>, ChangeProvider {

  /**
   * Searches for roles matching the specified search criteria.
   *
   * @param request the search request, not null
   * @return the search result, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  RoleSearchResult search(RoleSearchRequest request);

  /**
   * Queries the history of a single role.
   * <p/>
   * The request must contain an object identifier to identify the role.
   *
   * @param request the history request, not null
   * @return the role history, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  RoleHistoryResult history(RoleHistoryRequest request);


}
