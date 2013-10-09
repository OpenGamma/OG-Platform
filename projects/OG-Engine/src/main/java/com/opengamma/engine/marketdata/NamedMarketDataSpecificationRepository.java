/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import java.util.List;

import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.util.PublicAPI;

/**
 * A repository of named, pre-built market data specifications.
 * 
 * @deprecated  This is only required for the legacy analytics UI.
 */
@PublicAPI
@Deprecated
public interface NamedMarketDataSpecificationRepository {

  /**
   * Gets a list containing the names of all pre-built specifications in the repository, in the order desired by the
   * repository.
   * 
   * @return the names of all pre-built specifications in the repository, not null
   */
  List<String> getNames();
  
  /**
   * Gets the market data specification associated with a given name.
   * 
   * @param name  the specification name, not null
   * @return the associated market data specification, null if not found
   */
  MarketDataSpecification getSpecification(String name);
  
}
