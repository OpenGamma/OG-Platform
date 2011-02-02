/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config;

import com.opengamma.master.AbstractMaster;
import com.opengamma.master.listener.MasterChangeListener;
import com.opengamma.master.listener.NotifyingMaster;
import com.opengamma.util.PublicSPI;

/**
 * A general-purpose configuration master.
 * <p>
 * The configuration master provides a uniform view over storage of configuration elements.
 * This interface provides methods that allow the master to be searched and updated.
 * <p>
 * Many different kinds of configuration element may be stored using this interface.
 * Each element type will be stored using a different instance where the generic
 * parameter represents the type of the element.
 * 
 * @param <T>  the configuration element type
 */
@PublicSPI
public interface ConfigTypeMaster<T> extends AbstractMaster<ConfigDocument<T>>, NotifyingMaster {

  /**
   * Searches for configuration documents matching the specified search criteria.
   * 
   * @param request  the search request, not null
   * @return the search result, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  ConfigSearchResult<T> search(ConfigSearchRequest request);

  /**
   * Queries the history of a single piece of configuration.
   * <p>
   * The request must contain an object identifier to identify the configuration.
   * 
   * @param request  the history request, not null
   * @return the configuration history, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  ConfigHistoryResult<T> history(ConfigHistoryRequest request);
  
  /**
   * Add MasterChangeListener
   * 
   * @param listener the listener to add
   */
  @Override
  void addChangeListener(MasterChangeListener listener);
  
  /**
   * Remove MasterChangeListener
   * 
   * @param listener the listener to remove
   */
  @Override
  void removeChangeListener(MasterChangeListener listener);
  
}
