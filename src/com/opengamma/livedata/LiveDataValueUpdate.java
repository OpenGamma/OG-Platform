/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata;

import org.fudgemsg.FudgeFieldContainer;


/**
 * A market data update sent from server to client. 
 *
 * @author kirk
 */
public interface LiveDataValueUpdate {
  long getRelevantTimestamp();
  
  /**
   * @return What market data, in what format, this value update contains.
   */
  LiveDataSpecification getSpecification();
  
  /** 
   * @return Normalized market data update message.
   * @see LiveDataSpecification#getNormalizationRuleSetId()
   */
  FudgeFieldContainer getFields();

}
