/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.holiday;

import com.opengamma.core.change.ChangeProvider;
import com.opengamma.master.AbstractMaster;
import com.opengamma.util.PublicSPI;

/**
 * A general-purpose holiday master.
 * <p>
 * The holiday master provides a uniform view over a set of holiday definitions.
 * This interface provides methods that allow the master to be searched and updated.
 */
@PublicSPI
public interface HolidayMaster extends AbstractMaster<HolidayDocument>, ChangeProvider {

  /**
   * Queries the meta-data about the master.
   * <p>
   * This can return information that is useful for drop-down lists.
   * 
   * @param request  the search request, not null
   * @return the requested meta-data, not null
   */
  HolidayMetaDataResult metaData(HolidayMetaDataRequest request);

  /**
   * Searches for holidays matching the specified search criteria.
   * 
   * @param request  the search request, not null
   * @return the search result, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  HolidaySearchResult search(HolidaySearchRequest request);

  /**
   * Queries the history of a single holiday.
   * <p>
   * The request must contain an object identifier to identify the holiday.
   * 
   * @param request  the history request, not null
   * @return the holiday history, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  HolidayHistoryResult history(HolidayHistoryRequest request);

}
