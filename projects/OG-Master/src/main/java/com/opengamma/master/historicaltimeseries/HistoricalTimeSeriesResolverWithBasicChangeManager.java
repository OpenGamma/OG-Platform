/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries;

import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;

/**
 * HistoricalTimeSeriesResolver with basic change manager
 */
public abstract class HistoricalTimeSeriesResolverWithBasicChangeManager implements HistoricalTimeSeriesResolver {

  /** The local change manager. */
  private final ChangeManager _changeManager = new BasicChangeManager();

  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }
}
