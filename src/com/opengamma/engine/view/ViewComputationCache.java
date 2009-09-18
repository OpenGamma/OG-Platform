/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import com.opengamma.engine.analytics.AnalyticValue;
import com.opengamma.engine.analytics.AnalyticValueDefinition;

/**
 * The shared cache through which various elements in view recalculation will
 * store and retrieve values.
 *
 * @author kirk
 */
public interface ViewComputationCache {

  <T> AnalyticValue<T> getValue(AnalyticValueDefinition<T> definition);
  
  <T> void putValue(AnalyticValue<T> value);
}
