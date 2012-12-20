/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.transport;

import java.io.IOException;
import java.io.OutputStream;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;

/**
 * An implementation of {@link ByteArrayMessageSender} which will send all byte arrays
 * unaltered to any {@link OutputStream}.
 */
public class OutputStreamByteArrayMessageSender implements ByteArrayMessageSender {
  private final OutputStream _outputStream;
  
  public OutputStreamByteArrayMessageSender(OutputStream outputStream) {
    ArgumentChecker.notNull(outputStream, "outputStream");
    _outputStream = outputStream;
  }

  /**
   * Gets the outputStream.
   * @return the outputStream
   */
  public OutputStream getOutputStream() {
    return _outputStream;
  }

  @Override
  public void send(byte[] message) {
    try {
      getOutputStream().write(message);
    } catch (IOException ex) {
      throw new OpenGammaRuntimeException("Unable to write to underlying stream", ex);
    }
  }

}
