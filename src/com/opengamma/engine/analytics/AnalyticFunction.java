/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;

import java.util.Collection;

/**
 * A single unit of work capable of operating on inputs to produce results. 
 *
 * @author kirk
 */
public interface AnalyticFunction {
  
  Collection<AnalyticValueDefinition> getPossibleResults();

}
