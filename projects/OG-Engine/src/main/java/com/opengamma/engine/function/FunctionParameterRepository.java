/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
