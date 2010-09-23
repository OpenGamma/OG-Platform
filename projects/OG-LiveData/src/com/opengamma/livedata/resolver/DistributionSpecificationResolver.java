/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.resolver;

import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.server.DistributionSpecification;

/**
 * Determines the distribution specification for a particular live data
 * specification.
 *
 * @author kirk
 */
public interface DistributionSpecificationResolver {

  /**
   * @param liveDataSpecificationFromClient What the client wants. Different specs from the client can map to the same distribution spec. 
   * @return A valid distribution specification. Never null.
   * @throws IllegalArgumentException If the distribution spec cannot be built.  
   */
  DistributionSpecification getDistributionSpecification(LiveDataSpecification liveDataSpecificationFromClient) throws IllegalArgumentException;
  
}
