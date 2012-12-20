/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.livedata.firehose;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Common code for testing infrastructure based on these package classes.
 */
public final class AbstractTest {
  
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractTest.class);
  
  private AbstractTest() {
  }

  public static <T> void readFile(final String filename, final AbstractConnectorJob.Callback<T> callback, final RecordStream.Factory<T> streamFactory) {
    final FileReplayConnectorJob.Factory<T> factory = new FileReplayConnectorJob.Factory<T>();
    factory.setFilename(filename);
    final long startTime = System.nanoTime();
    try {
      final FileReplayConnectorJob<T> job = factory.newInstance(callback, streamFactory, null);
      job.run();
    } catch (OpenGammaRuntimeException e) {
      // Ignore; used to terminate the loop
    }
    s_logger.info("Time = {}ms", (double) (System.nanoTime() - startTime) / 1e6);
  }
  
  public static abstract class FileReplayConnectorJobTest<T> implements Runnable {

    protected abstract void writeFile(OutputStream out) throws IOException;

    protected abstract RecordStream.Factory<T> createStreamFactory();

    protected abstract void verify(Queue<Object> queue);
  
    public final void run() {
      try {
        final File file = File.createTempFile("firehose", ".bin");
        try {
          final OutputStream out = new FileOutputStream(file);
          writeFile(out);
          out.close();
          final Queue<Object> queue = new LinkedList<Object>();
          readFile(file.getPath(), new AbstractConnectorJob.Callback<T>() {

            private int _loop;

            @Override
            public void disconnected() {
              queue.add("DISCONNECTED");
              _loop++;
              if (_loop >= 2) {
                throw new OpenGammaRuntimeException("Terminate test");
              }
            }

            @Override
            public void received(final T record) {
              queue.add(record);
            }

            @Override
            public void connected() {
              queue.add("CONNECTED");
            }

          }, createStreamFactory());
          assertEquals(queue.poll(), "CONNECTED");
          verify(queue);
          assertEquals(queue.poll(), "DISCONNECTED");
          assertEquals(queue.poll(), "CONNECTED");
          verify(queue);
          assertEquals(queue.poll(), "DISCONNECTED");
          assertEquals(queue.size(), 0);
        } finally {
          file.delete();
        }
      } catch (IOException e) {
        throw new OpenGammaRuntimeException("I/O exception", e);
      }
    }

  }

}
