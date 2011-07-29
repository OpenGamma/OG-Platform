/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.resolver;

import com.opengamma.engine.function.CompiledFunctionDefinition;

/**
 * Strategy to provide the priority of a function.
 */
public interface FunctionPriority {

  /**
   * Gets the integer priority of the function, where larger is higher priority.
   * 
   * @param function  the function to examine, not null
   * @return the priority, larger is higher priority
   */
  int getPriority(CompiledFunctionDefinition function);

}
