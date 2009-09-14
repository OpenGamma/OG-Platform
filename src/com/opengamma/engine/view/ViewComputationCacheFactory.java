/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

/**
 * Generates {@link ViewCalculationCache} instances. 
 *
 * @author kirk
 */
public interface ViewComputationCacheFactory {
  
  ViewComputationCache generateCache();

}
