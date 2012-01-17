/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries;

import java.util.Collection;

/**
 * Selects the best match from multiple time-series candidates based on rules determined by the implementation. 
 */
public interface HistoricalTimeSeriesSelector {

  /**
   * Selects the best-matching time-series from a series of candidates.
   * 
   * @param candidates  the candidates, not null
   * @param selectionKey  a key defining how the selection is to occur, null for the default best match
   * @return the best matching candidate, null if unable to find a match
   */
  ManageableHistoricalTimeSeriesInfo select(Collection<ManageableHistoricalTimeSeriesInfo> candidates, String selectionKey);
  
}
