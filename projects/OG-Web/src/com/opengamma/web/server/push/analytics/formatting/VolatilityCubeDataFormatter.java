/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics.formatting;

import com.opengamma.core.marketdatasnapshot.VolatilityCubeData;
import com.opengamma.engine.value.ValueSpecification;

/**
 *
 */
/* package */ class VolatilityCubeDataFormatter implements Formatter<VolatilityCubeData> {

  @Override
  public Object formatForDisplay(VolatilityCubeData value, ValueSpecification valueSpec) {
    // TODO implement formatForDisplay()
    throw new UnsupportedOperationException("formatForDisplay not implemented");
  }

  @Override
  public Object formatForExpandedDisplay(VolatilityCubeData value, ValueSpecification valueSpec) {
    // TODO implement formatForExpandedDisplay()
    throw new UnsupportedOperationException("formatForExpandedDisplay not implemented");
  }

  @Override
  public Object formatForHistory(VolatilityCubeData history, ValueSpecification valueSpec) {
    return null;
  }

  @Override
  public String getName() {
    // TODO implement getName()
    throw new UnsupportedOperationException("getName not implemented");
  }
}
