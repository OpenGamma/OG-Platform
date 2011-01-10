/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata;

import org.fudgemsg.FudgeFieldContainer;

import com.opengamma.util.PublicAPI;


/**
 * A market data update sent from server to client. 
 */
@PublicAPI
public interface LiveDataValueUpdate {
  
  /**
   * The first sequence number in a series of
   * LiveData messages. See {@link #getSequenceNumber}.
   */
  long SEQUENCE_START = 0; 
  
  /**
   * The sequence number of the market data update.
   * <p>
   * The sequence number starts from 0 ({@link #SEQUENCE_START})
   * and is incremented by 1 for each message the server sends. There 
   * is a separate sequence number for each market data line a client 
   * subscribes to.
   * <p>
   * A client may see sequence numbers greater than 0 when it subscribes
   * to a market data line if another clients has already subscribed 
   * to the same market data line.
   * <p>
   * Sequence numbers can be reset to 0 on a server restart or on
   * migration of a market data line from one server to another.
   * A client can detect these events by listening for messages 
   * with sequence number 0.
   * <p>
   * A message with sequence number = 0 must be a full update, not a delta.
   *
   * @return The sequence number of the message
   */
  long getSequenceNumber();
  
  /**
   * Gets what market data this value update contains.
   * 
   * @return what market data, in what format, this value update contains.
   */
  LiveDataSpecification getSpecification();
  
  /** 
   * Gets the market data sent from server to client. 
   * 
   * @return market data
   */
  FudgeFieldContainer getFields();

}
