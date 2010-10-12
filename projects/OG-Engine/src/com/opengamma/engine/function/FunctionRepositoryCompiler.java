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
   * @param context the function repository, compilation contexts and other services to use
   * @param atInstant the snapshot time.
   * @return the repository of compiled functions.
   */
  CompiledFunctionRepository compile(FunctionCompilationService context, InstantProvider atInstant);

}
