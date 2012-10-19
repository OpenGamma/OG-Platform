/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config;

import com.opengamma.core.config.impl.ConfigItem;
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
public interface ConfigMaster extends AbstractChangeProvidingMaster<ConfigItem<?>, ConfigDocument> {

  /**
   * Searches for configuration documents matching the specified search criteria.
   *
   * @param request  the search request, not null
   * @return the search result, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  <T> ConfigSearchResult<T> search(ConfigSearchRequest<T> request);

  /**
   * Queries the history of a single piece of configuration.
   * <p>
   * The request must contain an object identifier to identify the configuration.
   *
   * @param request  the history request, not null
   * @return the configuration history, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  <T> ConfigHistoryResult<T> history(ConfigHistoryRequest<T> request);  
  
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
