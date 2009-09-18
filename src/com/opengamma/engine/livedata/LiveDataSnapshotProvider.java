/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.livedata;

import com.opengamma.engine.analytics.AnalyticValue;
import com.opengamma.engine.analytics.AnalyticValueDefinition;

/**
 * Allows a view to snapshot and then query the state of financial data.
 *
 * @author kirk
 */
public interface LiveDataSnapshotProvider {

  void addSubscription(AnalyticValueDefinition<?> definition);
  
  long snapshot();
  // REVIEW jim 18-Sep-09 -- be nice if the ?'s were == but messes other things up.
  AnalyticValue<?> querySnapshot(long snapshot, AnalyticValueDefinition<?> definition); 
  
  void releaseSnapshot(long snapshot);
}
