/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;

import java.util.Collection;

import com.opengamma.engine.position.Position;
import com.opengamma.engine.security.Security;

/**
 * A container for the {@link FunctionDefinition} instances available
 * to a particular environment. 
 *
 * @author kirk
 */
public interface AnalyticFunctionRepository {
  
  Collection<FunctionDefinition> getAllFunctions();
  
  /**
   * This method <em>must not</em> return {@code null}.
   * @param outputs
   * @return
   */
  Collection<FunctionDefinition> getFunctionsProducing(Collection<AnalyticValueDefinition<?>> outputs);
  
  Collection<FunctionDefinition> getFunctionsProducing(Collection<AnalyticValueDefinition<?>> outputs, Security security);
  
  Collection<FunctionDefinition> getFunctionsProducing(Collection<AnalyticValueDefinition<?>> outputs, Position position);
  
  Collection<FunctionDefinition> getFunctionsProducing(Collection<AnalyticValueDefinition<?>> outputs, Collection<Position> positions);
  
  FunctionInvoker getInvoker(String uniqueIdentifier);

}
