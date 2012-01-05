/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.region;

import com.opengamma.core.change.ChangeProvider;
import com.opengamma.master.AbstractMaster;
import com.opengamma.util.PublicSPI;

/**
 * A general-purpose region master.
 * <p>
 * The region master provides a uniform view over a set of region definitions.
 * This interface provides methods that allow the master to be searched and updated.
 */
@PublicSPI
public interface RegionMaster extends AbstractMaster<RegionDocument>, ChangeProvider {

  /**
   * Searches for regions matching the specified search criteria.
   * 
   * @param request  the search request, not null
   * @return the search result, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  RegionSearchResult search(RegionSearchRequest request);

  /**
   * Queries the history of a single region.
   * <p>
   * The request must contain an object identifier to identify the region.
   * 
   * @param request  the history request, not null
   * @return the region history, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  RegionHistoryResult history(RegionHistoryRequest request);

}
