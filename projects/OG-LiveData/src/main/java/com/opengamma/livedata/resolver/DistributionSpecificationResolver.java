/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.resolver;

import java.util.Collection;
import java.util.Map;

import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.server.DistributionSpecification;

/**
 * Determines how market data should be distributed to clients.
 */
public interface DistributionSpecificationResolver extends Resolver<LiveDataSpecification, DistributionSpecification> {

  /**
   * Determines how market data should be distributed to clients.
   * 
   * @param liveDataSpecificationFromClient what the client wants. Different specs from the client can map to the same distribution spec. 
   * @return a valid distribution specification. Null if the distribution spec cannot be built.  
   */
  DistributionSpecification resolve(LiveDataSpecification liveDataSpecificationFromClient);
  
  /**
   * Same as calling {@link #resolve(LiveDataSpecification)} 
   * individually, but since it works in bulk, may be more efficient. 
   * 
   * @param liveDataSpecifications what the client wants. Different specs from the client can map to the same distribution spec. 
   * @return map from request to result.  
   * For each input spec, there must be an entry in the map.
   * The value will be null if the distribution spec cannot be build for that spec  
   */
  Map<LiveDataSpecification, DistributionSpecification> resolve(Collection<LiveDataSpecification> liveDataSpecifications);
  
}
