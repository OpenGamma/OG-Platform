/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport;

/**
 * General interface through which code can respond to a message request.
 *
 * @author kirk
 */
public interface ByteArrayRequestReceiver {

  byte[] requestReceived(byte[] message);
}
