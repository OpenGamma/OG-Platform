/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.livedata;

import com.opengamma.engine.analytics.AnalyticValueDefinition;

/**
 * Allows code to determine whether particular pieces of data are
 * theoretically available from any sources of live data.
 *
 * @author kirk
 */
public interface LiveDataAvailabilityProvider {
  
  boolean isAvailable(AnalyticValueDefinition value);

}
