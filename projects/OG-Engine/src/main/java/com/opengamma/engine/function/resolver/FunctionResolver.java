/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.resolver;

import org.threeten.bp.Instant;

import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.util.PublicAPI;

/**
 * Strategy for resolving that is responsible for matching the requirements of a particular computation target and value requirement to a given function.
 * <p>
 * This behavior is separated as a strategy from the {@code FunctionRepository} to allow different implementations. For example, functions may be matched on given different criteria, such as speed.
 */
@PublicAPI
public interface FunctionResolver {

  /**
   * Provides a resolver that is locked to a specific instant.
   * <p>
   * This creates a {@link CompiledFunctionResolver} that can perform the actual resolution with functions compiled for use at the given snapshot time.
   * 
   * @param atInstant the snapshot time the functions will be used at, not null
   * @return the compiled function resolver, not null
   */
  CompiledFunctionResolver compile(Instant atInstant);

  /**
   * Returns a specific function definition based on an identifier.
   * 
   * @param uniqueId the identifier of the function, not null
   * @return the function definition, or null if the function was not published by a resolution rule used by a compiled form of this resolver
   */
  FunctionDefinition getFunction(String uniqueId);

}
