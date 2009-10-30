/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import com.opengamma.livedata.LiveDataSpecification;

/**
 * Determines the distribution specification for a particular live data
 * specification.
 *
 * @author kirk
 */
public interface DistributionSpecificationResolver {

  String getDistributionSpecification(LiveDataSpecification fullyResolvedSpec);
}
