/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
