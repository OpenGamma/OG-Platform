/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.exclusion;

import java.util.Collection;

import com.opengamma.engine.function.FunctionDefinition;

/**
 * Source of {@link ExclusionGroup} instances.
 */
public interface FunctionExclusionGroups {

  /**
   * Returns the exclusion group that a function definition is part of.
   * 
   * @param function the function to test, not null
   * @return the exclusion group, or null if the function is not part of an exclusion group
   */
  FunctionExclusionGroup getExclusionGroup(FunctionDefinition function);

  /**
   * Tests if the current exclusion group is covered by a group already in the collection. This may typically be a {@link Collection#contains} call on the existing collection, or a more elaborate
   * check.
   * 
   * @param current the exclusion group to test, not null
   * @param existing the groups already encountered, not null and not containing nulls
   * @return true if the tested group is to be excluded, false if it may be considered
   */
  boolean isExcluded(FunctionExclusionGroup current, Collection<FunctionExclusionGroup> existing);

  /**
   * Creates a function exclusion group collection.
   * 
   * @param existing the previous exclusion groups, not null
   * @param newGroup the group to also include in the new collection, not null
   */
  Collection<FunctionExclusionGroup> withExclusion(Collection<FunctionExclusionGroup> existing, FunctionExclusionGroup newGroup);

}
