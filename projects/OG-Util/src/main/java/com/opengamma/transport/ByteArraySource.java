/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport;

import java.util.List;

/**
 * A source of byte arrays.
 */
public interface ByteArraySource {

  byte[] receiveNoWait();

  byte[] receive(long maxWaitInMilliseconds);

  List<byte[]> batchReceiveNoWait();

  List<byte[]> batchReceive(long maxWaitInMilliseconds);

}
