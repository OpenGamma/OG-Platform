/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.resolver;

import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.server.DistributionSpecification;

/**
 * Determines how market data should be distributed to clients.
 */
public interface DistributionSpecificationResolver {

  /**
   * Determines how market data should be distributed to clients.
   * 
   * @param liveDataSpecificationFromClient what the client wants. Different specs from the client can map to the same distribution spec. 
   * @return a valid distribution specification. Not null.
   * @throws IllegalArgumentException if the distribution spec cannot be built.  
   */
  DistributionSpecification getDistributionSpecification(LiveDataSpecification liveDataSpecificationFromClient) throws IllegalArgumentException;
  
}
