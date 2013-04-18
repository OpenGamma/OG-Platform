/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import org.threeten.bp.Instant;

import com.opengamma.util.PoolExecutor;

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
  CompiledFunctionRepository compile(FunctionRepository repository, FunctionCompilationContext context, PoolExecutor executor, Instant atInstant);

}
