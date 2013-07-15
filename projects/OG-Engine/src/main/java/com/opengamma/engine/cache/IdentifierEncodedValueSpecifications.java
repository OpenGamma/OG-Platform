/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.cache;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2LongMap;

import java.util.Set;

import com.opengamma.engine.value.ValueSpecification;

/**
 * Marker interface for objects that contain {@link ValueSpecification}s but are Fudge encoded using the shorted long representation for use with an
 * {@link IdentifierMap}.
 */
public interface IdentifierEncodedValueSpecifications {

  /**
   * Collects all identifiers used by this object and any member objects into the set.
   * 
   * @param identifiers the set to collect the identifiers into
   */
  void collectIdentifiers(LongSet identifiers);

  /**
   * Collects all value specifications used by this object and any member objects into the set.
   * 
   * @param valueSpecifications the set to collect the specifications into
   */
  void collectValueSpecifications(Set<ValueSpecification> valueSpecifications);

  /**
   * Converts all numeric identifiers used by this object and any member objects into full specifications using the supplied buffer.
   * 
   * @param identifiers the map of identifiers to value specifications
   */
  void convertIdentifiers(Long2ObjectMap<ValueSpecification> identifiers);
  
  /**
   * Converts all value specifications used by this object and any member objects into numeric identifiers using the supplied buffer.
   * 
   * @param valueSpecifications the map of value specifications to identifiers
   */
  void convertValueSpecifications(Object2LongMap<ValueSpecification> valueSpecifications);

}
