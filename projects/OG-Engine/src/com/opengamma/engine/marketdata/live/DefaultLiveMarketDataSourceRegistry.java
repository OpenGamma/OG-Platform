/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.live;

import java.util.Collection;
import java.util.Collections;


/**
 * A registry containing a single default entry
 */
public class DefaultLiveMarketDataSourceRegistry implements LiveMarketDataSourceRegistry {

  @Override
  public Collection<String> getDataSources() {
    return Collections.singleton(null);
  }
}
