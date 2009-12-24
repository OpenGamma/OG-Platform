/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import com.opengamma.engine.analytics.ComputedValue;
import com.opengamma.engine.analytics.AnalyticValueDefinition;

/**
 * The shared cache through which various elements in view recalculation will
 * store and retrieve values.
 *
 * @author kirk
 */
public interface ViewComputationCache {

  <T> ComputedValue<T> getValue(AnalyticValueDefinition<T> definition);
  
  <T> void putValue(ComputedValue<T> value);
}
