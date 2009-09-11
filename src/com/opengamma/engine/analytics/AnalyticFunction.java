/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;

import java.util.Collection;

import com.opengamma.engine.position.AggregatePosition;
import com.opengamma.engine.position.Position;

// NOTE kirk 2009-09-03 -- This is the data that we need for the engine to work.
// In general, I would expect that we'll have a metadata source (possibly based
// on configuration files, possibly based on source annotations) that will
// provide all of this into a JavaBean-based implementation.

/**
 * A single unit of work capable of operating on inputs to produce results. 
 *
 * @author kirk
 */
public interface AnalyticFunction {
  
  String getShortName();
  
  boolean isSecuritySpecific();
  
  /**
   * Determine whether this function is applicable to the specified security type
   * in general.
   * 
   * @param securityType The name of the security type to check.
   * @return {@code true} iff this function is potentially applicable to a position
   *         in a security with the specified type.
   */
  boolean isApplicableTo(String securityType);
  
  boolean isPositionSpecific();
  
  /**
   * Determine whether this function is applicable to a position of the type provided.
   * While leaf-level positions will customarily be checked using a call
   * to {@link #isApplicableTo(String)} to check on the security type,
   * this method is more appropriate for checking whether a particular function
   * is suitable to {@link AggregatePosition}s. For example, some operations
   * simply may not make sense in an aggregate position.
   * 
   * @param position The position to check.
   * @return {@code true} iff this function is potentially applicable to
   *         the provided position.
   */
  boolean isApplicableTo(Position position);
  
  Collection<AnalyticValueDefinition> getPossibleResults();
  
  Collection<AnalyticValueDefinition> getInputs();
  
  Collection<AnalyticValue> execute(Collection<AnalyticValue> inputs, Position position);

}
