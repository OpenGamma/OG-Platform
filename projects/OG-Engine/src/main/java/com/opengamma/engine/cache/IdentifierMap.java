/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.cache;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.objects.Object2LongMap;

import java.util.Collection;

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
   * Inverse of {@link #getIdentifier}.
   * 
   * @param identifier The identifier to look up
   * @return The specification
   */
  ValueSpecification getValueSpecification(long identifier);

  /**
   * Potentially more efficient version of {@link #getIdentifier} for
   * multiple value requests.
   * 
   * @param specs The specifications to lookup or allocate identifiers for
   * @return The identifiers, not null.
   */
  Object2LongMap<ValueSpecification> getIdentifiers(Collection<ValueSpecification> specs);
  
  /**
   * Inverse of {@link #getIdentifiers}.
   * 
   * @param identifiers The identifiers to look up
   * @return The specifications, not null.
   */
  Long2ObjectMap<ValueSpecification> getValueSpecifications(LongCollection identifiers);

}
