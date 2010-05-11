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
  
  public static final long SEQUENCE_START = 0; 
  
  /**
   * The sequence number starts from 0 and is incremented by 1 
   * for each message the server sends to a JMS topic. The sequence 
   * number is specific to the topic, not global.
   * <p>
   * You can detect a server restart or migration of the topic
   * from one server to another by listening for messages with sequence number 0.
   * <p>
   * A message with sequence number = 0 must be a full update, not a delta.
   *
   * @return
   */
  long getSequenceNumber();
  
  /**
   * @return What market data, in what format, this value update contains.
   * Fully qualified (standardized) by the server.
   */
  LiveDataSpecification getSpecification();
  
  /** 
   * @return Normalized market data update message.
   * @see LiveDataSpecification#getNormalizationRuleSetId()
   */
  FudgeFieldContainer getFields();

}
