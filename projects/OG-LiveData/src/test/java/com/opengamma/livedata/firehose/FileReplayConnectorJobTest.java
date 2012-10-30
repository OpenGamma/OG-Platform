/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.livedata.firehose;

import static org.testng.Assert.assertEquals;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Queue;

import org.testng.annotations.Test;

/**
 * Test.
 */
@Test(groups = {"unit", "slow"})
public class FileReplayConnectorJobTest {

  public void testFileReplay() {
    new AbstractTest.FileReplayConnectorJobTest<Integer>() {

      @Override
      protected void writeFile(final OutputStream out) throws IOException {
        final byte[] buffer = new byte[4096];
        for (int i = 0; i < 4096; i++) {
          buffer[i] = (byte) i;
        }
        for (int i = 0; i < 128; i++) {
          out.write(buffer);
        }
      }

      @Override
      protected RecordStream.Factory<Integer> createStreamFactory() {
        return new RecordStream.Factory<Integer>() {
          @Override
          public RecordStream<Integer> newInstance(final InputStream input) {
            return new RecordStream<Integer>() {
              @Override
              public Integer readRecord() throws IOException {
                final int i = input.read();
                if (i < 0) {
                  throw new EOFException();
                } else {
                  return i;
                }
              }
            };
          }
        };
      }

      @Override
      protected void verify(final Queue<Object> queue) {
        for (int i = 0; i < 128; i++) {
          for (int j = 0; j < 4096; j++) {
            assertEquals(queue.poll(), j & 255);
          }
        }
      }

    }.run();
  }

}
