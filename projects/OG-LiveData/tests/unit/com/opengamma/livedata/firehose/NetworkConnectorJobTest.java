/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.livedata.firehose;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.opengamma.transport.socket.AbstractServerSocketProcess;
import com.opengamma.util.test.Timeout;

/**
 * Tests the {@link NetworkConnectorJob} class.
 */
@Test
public class NetworkConnectorJobTest {

  private static final Logger s_logger = LoggerFactory.getLogger(NetworkConnectorJobTest.class);

  public static class Server extends AbstractServerSocketProcess {

    @Override
    protected void socketOpened(final Socket socket) {
      try {
        final BufferedOutputStream output = new BufferedOutputStream(socket.getOutputStream());
        for (int i = 0; i < 128; i++) {
          for (int j = 0; j < 4096; j++) {
            output.write(j);
          }
          output.flush();
        }
        output.close();
      } catch (IOException e) {
        s_logger.warn("I/O exception - {}", e.toString());
        s_logger.debug("I/O exception", e);
      } finally {
        try {
          socket.close();
        } catch (IOException e2) {
          // Ignore
        }
      }
    }

  }

  public void testDummyServer() throws InterruptedException, IOException {
    final Server server = new Server();
    server.start();
    try {
      final NetworkConnectorJob.Factory<Integer> factory = new NetworkConnectorJob.Factory<Integer>();
      s_logger.info("Created server at {}, port {}", server.getBindAddress(), server.getPortNumber());
      factory.setHost(InetAddress.getLocalHost());
      factory.setPort(server.getPortNumber());
      final CountDownLatch latch = new CountDownLatch(1);
      final AbstractConnectorJob<Integer> job = factory.newInstance(new AbstractConnectorJob.Callback<Integer>() {

        private boolean _connected = false;
        private boolean _disconnected = false;
        private int _records;

        @Override
        public void disconnected() {
          assertEquals(_records, 128);
          assertTrue(_connected);
          assertFalse(_disconnected);
          _disconnected = true;
          latch.countDown();
        }

        @Override
        public void received(final Integer record) {
          assertTrue(_connected);
          assertFalse(_disconnected);
          _records++;
        }

        @Override
        public void connected() {
          assertFalse(_disconnected);
          assertFalse(_connected);
          _connected = true;
        }

      }, new RecordStream.Factory<Integer>() {
        @Override
        public RecordStream<Integer> newInstance(final InputStream input) {
          return new RecordStream<Integer>() {
            @Override
            public Integer readRecord() throws IOException {
              final byte[] buffer = new byte[4096];
              int c = 0, r;
              r = input.read(buffer, c, 4096 - c);
              while (r > 0) {
                c += r;
                if (c == 4096) {
                  return 1;
                }
                r = input.read(buffer, c, 4096 - c);
              }
              throw new EOFException();
            }
          };
        }
      }, null);
      new Thread(job).start();
      assertTrue(latch.await(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS));
    } finally {
      server.stop();
    }
  }

}
