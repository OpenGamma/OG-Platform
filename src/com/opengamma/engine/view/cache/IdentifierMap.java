/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import java.util.Collection;
import java.util.Map;

import com.opengamma.engine.value.ValueSpecification;

/**
 * Allows clients to determine a {@code long} value for a
 * {@link ValueSpecification} for interaction with other caching interfaces.
 */
public interface IdentifierMap {

  /**
   * Obtain the current identifier for the specification provided,
   * or allocate a new one and return if there is not an existing
   * identifier allocated.
   * 
   * @param spec The specification to lookup or allocate an identifier for
   * @return The identifier
   */
  long getIdentifier(ValueSpecification spec);

  /**
   * Potentially more efficient version of {@link #getIdentifier} for
   * multiple value requests.
   * 
   * @param specs The specifications to lookup or allocate identifiers for
   * @return The identifiers
   */
  Map<ValueSpecification, Long> getIdentifiers(Collection<ValueSpecification> specs);

}
