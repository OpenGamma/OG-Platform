/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import javax.time.InstantProvider;

/**
 * Compilation service to convert a {@link FunctionRepository} to a {@link CompiledFunctionRepository}. 
 */
public interface FunctionRepositoryCompiler {

  /**
   * Compile all functions in the repository for use with snapshots at the given time.
   * 
   * @param context the function compilation context.
   * @param functions the function repository.
   * @param atInstant the snapshot time.
   * @return the repository of compiled functions.
   */
  CompiledFunctionRepository compile(FunctionCompilationContext context, FunctionRepository functions, InstantProvider atInstant);

}
