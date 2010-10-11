/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.Collection;

/**
 * A container for the {@link CompiledFunctionDefinition} instances available
 * to a particular environment at a particular time.
 */
public interface CompiledFunctionRepository {

  Collection<CompiledFunctionDefinition> getAllFunctions();

  CompiledFunctionDefinition getDefinition(String uniqueIdentifier);

  FunctionInvoker getInvoker(String uniqueIdentifier);

  FunctionCompilationContext getCompilationContext();

}
