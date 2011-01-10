/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.transport;

import java.util.List;

/**
 * 
 *
 * @author kirk
 */
public interface ByteArraySource {
  
  byte[] receiveNoWait();
  
  byte[] receive(long maxWaitInMilliseconds);
  
  List<byte[]> batchReceiveNoWait();
  
  List<byte[]> batchReceive(long maxWaitInMilliseconds);

}
