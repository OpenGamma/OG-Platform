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

  /**
   * Returns a specific function definition based on an identifier.
   * 
   * @param uniqueId the identifier of the function, not null
   * @return the function definition, or null if the function is not in the repository.
   */
  FunctionDefinition getFunction(String uniqueId);

}
