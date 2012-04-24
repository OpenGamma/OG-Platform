/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.livedata.firehose;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.testng.annotations.Test;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.transport.socket.AbstractServerSocketProcess;

/**
 * Tests the {@link RecordSurfStream} class.
 */
@Test
public class RecordNetworkStreamTest {
  
  public void testCreateFile () {
    final AbstractServerSocketProcess server = new NetworkConnectorJobTest.Server();
    server.start ();
    try {
      try {
        final File file = File.createTempFile("firehose", ".bin");
        try {
          RecordNetworkStream.main(new String[] {"localhost", Integer.toString(server.getPortNumber()), file.getPath() });
          assertEquals(file.length(), 128 * 4096);
        } finally {
          file.delete();
        }
      } catch (IOException e) {
        throw new OpenGammaRuntimeException ("I/O exception", e);
      }
    } finally {
      server.stop ();
    }
  }
  
}