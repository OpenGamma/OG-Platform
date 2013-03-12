/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode;

import com.opengamma.engine.cache.ViewComputationCache;

/**
 * 
 */
public interface CalculationNode {
  
  ViewComputationCache getCache(CalculationJobSpecification spec);

  String getNodeId();

}
