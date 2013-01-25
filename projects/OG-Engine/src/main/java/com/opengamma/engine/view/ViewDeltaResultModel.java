/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import org.threeten.bp.Instant;

import com.opengamma.util.PublicAPI;

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
@PublicAPI
public interface ViewDeltaResultModel extends ViewResultModel {

  /**
   * The result timestamp for the previous delta, to chain them together
   * properly.
   * This will correspond with either {@link #getResultTimestamp()} on the previous
   * delta, or {@link ViewComputationResultModel#getResultTimestamp()} on the
   * previous full result model.
   * 
   * @return the timestamp for the previous result in the delta chain
   */
  Instant getPreviousResultTimestamp();
  
  // TODO kirk 2010-03-29 -- Notify on new nodes/positions
  // TODO kirk 2010-03-29 -- Notify on removed nodes/positions
  // TODO kirk 2010-03-29 -- Notify on removed calculation configurations
}
