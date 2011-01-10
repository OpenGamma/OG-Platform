/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.livedata;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.util.PublicSPI;

/**
 * Interface that returns availability of live data
 */
@PublicSPI
public interface LiveDataAvailabilityProvider {

  boolean isAvailable(ValueRequirement requirement);
}
