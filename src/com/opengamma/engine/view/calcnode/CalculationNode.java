/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import com.opengamma.engine.view.cache.ViewComputationCache;

/**
 * 
 */
public interface CalculationNode {
  
  ViewComputationCache getCache(CalculationJobSpecification spec);

  String getNodeId();
  

}
