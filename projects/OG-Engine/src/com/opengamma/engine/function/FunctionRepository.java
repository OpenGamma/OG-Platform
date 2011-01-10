/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
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
