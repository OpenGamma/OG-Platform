/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.client;

import com.opengamma.livedata.LiveDataSpecification;

/**
 * 
 *
 * @author kirk
 */
public class IdentitySpecificationResolver implements LiveDataSpecificationResolver {

  @Override
  public LiveDataSpecification resolve(
      LiveDataSpecification requestedSpecification) {
    return requestedSpecification;
  }

}
