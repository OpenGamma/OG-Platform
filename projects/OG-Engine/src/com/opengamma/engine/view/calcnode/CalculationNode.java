/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
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
