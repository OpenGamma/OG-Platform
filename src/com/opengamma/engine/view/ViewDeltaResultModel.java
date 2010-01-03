/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.Collection;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ComputedValue;


/**
 * Contains just the individual pieces of data that are modified during
 * a particular view computation.
 * Thus from a particular {@link ViewComputationResultModel} and a
 * {@link ViewDeltaResultModel} you can compose the equivalent
 * {@link ViewComputationResultModel} for that time sequence.
 * <p/>
 * The differences here are for two purposes:
 * <ol>
 *   <li>Deltas are more efficient to transfer on the wire, and better
 *       for supporting the Snapshot + Delta mode of view synchronization</li>
 *   <li>Deltas are optimal for user-interface logic that depends only on
 *       knowing/updating what's changed (or for providing a usability hint like
 *       highlighting updated values)</li>
 * </ol>
 *
 * @author kirk
 */
public interface ViewDeltaResultModel {

  long getInputDataTimestamp();
  
  long getResultTimestamp();
  
  /**
   * The result timestamp for the previous delta, to chain them together
   * properly.
   * This will correspond with either {@link #getResultTimestamp()} on the previous
   * delta, or {@link ViewComputationResultModel#getResultTimestamp()} on the
   * previous full result model.
   * 
   * @return
   */
  long getPreviousResultTimestamp();

  Collection<ComputationTargetSpecification> getAllTargets();
  
  Collection<ComputedValue> getDeltaValues(ComputationTargetSpecification target);
}
