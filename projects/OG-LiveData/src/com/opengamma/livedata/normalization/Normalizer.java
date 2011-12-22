/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import java.util.Map;

import org.fudgemsg.FudgeMsg;

import com.opengamma.livedata.LiveDataSpecification;

/**
 * A normalization service.
 */
public interface Normalizer {

  /**
   * Normalizes the value(s) described by the live data specification and contained within
   * the Fudge message are normalized to the ruleset requested in the specification.
   * 
   * @param specification the live data specification, not null
   * @param data the unnormalized, partially normalized, or fully normalized data, not null
   * @return the normalized result, or null if it was incorrect or rejected
   */
  FudgeMsg normalizeValues(LiveDataSpecification specification, FudgeMsg data);

  /**
   * Bulk operation equivalent of {@link #normalizeValues(LiveDataSpecification,FudgeMsg)}.
   * 
   * @param data a map of specifications to data that should be normalized, not null
   * @return the normalized results, specifications omitted if any were rejected
   */
  Map<LiveDataSpecification, FudgeMsg> normalizeValues(Map<LiveDataSpecification, ? extends FudgeMsg> data);

}
