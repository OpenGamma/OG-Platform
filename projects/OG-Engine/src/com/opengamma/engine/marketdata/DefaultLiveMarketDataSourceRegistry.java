/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import com.google.common.collect.Lists;

/**
 * A registry containing a single default entry
 */
public class DefaultLiveMarketDataSourceRegistry implements LiveMarketDataSourceRegistry {

  @Override
  public Iterable<String> getDataSources() {
    return Lists.newArrayList((String) null);
  }
}
