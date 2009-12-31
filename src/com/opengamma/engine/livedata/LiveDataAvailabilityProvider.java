/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.livedata;

import com.opengamma.engine.value.ValueRequirement;

/**
 * 
 *
 * @author kirk
 */
public interface LiveDataAvailabilityProvider {

  boolean isAvailable(ValueRequirement requirement);
}
