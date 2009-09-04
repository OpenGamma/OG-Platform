/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;

import java.util.Collection;

/**
 * A container for the {@link AnalyticFunction} instances available
 * to a particular environment. 
 *
 * @author kirk
 */
public interface AnalyticFunctionRepository {
  
  Collection<AnalyticFunction> getAllFunctions();
  
  Collection<AnalyticFunction> getFunctionsProducing(Collection<AnalyticValueDefinition> outputs);

}
