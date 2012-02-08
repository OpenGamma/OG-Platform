/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.Collection;

/**
 * A container for the {@link FunctionDefinition} instances available to a particular environment. 
 */
public interface FunctionRepository {

  /**
   * Gets the complete set of available functions.
   * 
   * @return the functions, not null
   */
  Collection<FunctionDefinition> getAllFunctions();

}
