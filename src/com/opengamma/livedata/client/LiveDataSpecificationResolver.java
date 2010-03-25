/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.client;

import com.opengamma.livedata.LiveDataSpecification;

/**
 *
 * @author kirk
 */
public interface LiveDataSpecificationResolver {
  
  /**
   * Resolves a LiveData Specification. This means that one, probably limited or fuzzy, set of
   * IDs is transformed into another, probably more exact and comprehensive, set of IDs.
   * For example, a Bloomberg ticker is transformed into a Bloomberg unique ID
   * and an OpenGamma distribution ID. 
   * 
   * @return The resolved specification
   */
  LiveDataSpecification resolve(LiveDataSpecification requestedSpecification);

}
