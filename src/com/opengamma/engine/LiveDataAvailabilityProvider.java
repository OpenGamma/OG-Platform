/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine;

import com.opengamma.engine.analytics.AnalyticValueDefinition;

// REVIEW kirk 2009-09-04 -- Not sure whether this is the right location,
// but as it depends on stuff in OG-Engine, I can't put it in OG-LiveData.
/**
 * Allows code to determine whether particular pieces of data are
 * theoretically available from any sources of live data.
 *
 * @author kirk
 */
public interface LiveDataAvailabilityProvider {
  
  boolean isAvailable(AnalyticValueDefinition value);

}
