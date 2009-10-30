/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import com.opengamma.livedata.LiveDataSpecification;

/**
 * 
 *
 * @author kirk
 */
public class NaiveDistributionSpecificationResolver implements
    DistributionSpecificationResolver {

  @Override
  public String getDistributionSpecification(
      LiveDataSpecification fullyResolvedSpec) {
    return fullyResolvedSpec.toString();
  }

}
