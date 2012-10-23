/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config;

import com.opengamma.master.AbstractChangeProvidingMaster;
import com.opengamma.util.PublicSPI;

/**
 * A general-purpose configuration master.
 * <p>
 * The configuration master provides a uniform view over storage of configuration elements.
 * This interface provides methods that allow the master to be searched and updated.
 * <p>
 * Many different kinds of configuration element may be stored in a single master.
 */
@PublicSPI
public interface ConfigMaster extends AbstractChangeProvidingMaster<ConfigDocument> {

  /**
   * Searches for configuration documents matching the specified search criteria.
   *
   * @param <R>  the type of the config item
   * @param request  the search request, not null
   * @return the search result, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  <R> ConfigSearchResult<R> search(ConfigSearchRequest<R> request);

  /**
   * Queries the history of a single piece of configuration.
   * <p>
   * The request must contain an object identifier to identify the configuration.
   *
   * @param <R>  the type of the config item
   * @param request  the history request, not null
   * @return the configuration history, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  <R> ConfigHistoryResult<R> history(ConfigHistoryRequest<R> request);  

  /**
   * Queries the meta-data about the master.
   * <p>
   * This can return information that is useful for drop-down lists.
   * 
   * @param request  the search request, not null
   * @return the requested meta-data, not null
   */
  ConfigMetaDataResult metaData(ConfigMetaDataRequest request);

}
