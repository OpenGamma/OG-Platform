/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;

import java.util.Collection;

/**
 * A container for the {@link AnalyticFunctionDefinition} instances available
 * to a particular environment. 
 *
 * @author kirk
 */
public interface AnalyticFunctionRepository {
  
  Collection<AnalyticFunctionDefinition> getAllFunctions();
  
  /**
   * This method <em>must not</em> return {@code null}.
   * @param outputs
   * @return
   */
  Collection<AnalyticFunctionDefinition> getFunctionsProducing(Collection<AnalyticValueDefinition<?>> outputs);
  
  Collection<AnalyticFunctionDefinition> getFunctionsProducing(Collection<AnalyticValueDefinition<?>> outputs, String securityType);
  
  AnalyticFunctionInvoker getInvoker(String uniqueIdentifier);

}
