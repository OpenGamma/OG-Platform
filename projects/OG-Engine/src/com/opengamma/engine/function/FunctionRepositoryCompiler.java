/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.concurrent.ExecutorService;

import javax.time.InstantProvider;

/**
 * Compilation service to convert a {@link FunctionRepository} to a {@link CompiledFunctionRepository}. 
 */
public interface FunctionRepositoryCompiler {

  /**
   * Compile all functions in the repository for use with snapshots at the given time.
   * 
   * @param repository the function repository
   * @param context the compilation context
   * @param executor executor service to use for parallel compilation
   * @param atInstant the snapshot time.
   * @return the repository of compiled functions.
   */
  CompiledFunctionRepository compile(FunctionRepository repository, FunctionCompilationContext context, ExecutorService executor, InstantProvider atInstant);

}
