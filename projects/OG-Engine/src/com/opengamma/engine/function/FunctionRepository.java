/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.Collection;

/**
 * A container for the {@link FunctionDefinition} instances available
 * to a particular environment. 
 *
 * @author kirk
 */
public interface FunctionRepository {

  Collection<FunctionDefinition> getAllFunctions();

}
