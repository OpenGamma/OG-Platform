/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.target;

import java.util.Collection;

import com.opengamma.engine.ComputationTargetResolver;

/**
 * Provides an enumeration of available target types. An instance may typically be closely coupled with the {@link ComputationTargetResolver} as that resolver should be capable of resolving any of the
 * listed types.
 */
public interface ComputationTargetTypeProvider {

  /**
   * Fetches all of the known simple types. This is likely to be at a minimum the standard public constants {@link ComputationTargetType#PORTFOLIO_NODE}, {@link ComputationTargetType#POSITION},
   * {@link ComputationTargetType#SECURITY}, and so on. It should also include any additional basic types that aren't part of OG-Engine but appropriate for the attached function repository or
   * repositories. This list should not include union or nested types such as {@link ComputationTargetType#POSITION_OR_TRADE}.
   * 
   * @return the known simple types, not null
   */
  Collection<ComputationTargetType> getSimpleTypes();

  /**
   * Fetches any known union or nested types that are commonly used for convenience. For example, this might include {@link ComputationTargetType#POSITION_OR_TRADE}.
   * 
   * @return any additional types, not null
   */
  Collection<ComputationTargetType> getAdditionalTypes();

  /**
   * Fetches all known simple and any union/nested types. This should be the union of the results of {@link #getAllSimpleTypes} and {@link #getAdditionalTypes}.
   * 
   * @return all known types, not null
   */
  Collection<ComputationTargetType> getAllTypes();

}
