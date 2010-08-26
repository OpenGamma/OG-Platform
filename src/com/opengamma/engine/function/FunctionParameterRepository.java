/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.Collection;

/**
 * 
 */
public interface FunctionParameterRepository {
  
  Collection<ParameterizedFunction> getAllFunctionParameters();

}
