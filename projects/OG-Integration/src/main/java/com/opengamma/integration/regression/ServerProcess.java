/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.component.OpenGammaComponentServer;

/**
 *
 */
public final class ServerProcess implements AutoCloseable {

  /** The server process. */
  private final Process _process;
  
  private ServerProcess(Process process) {
    _process = process;
  }

  public static ServerProcess start() {
    // TODO is it possible to scan the view defs and run all of them?
    // need to be able to choose the snapshot for each view def. if there's only one for each base view def then
    // it should be possible
    // TODO how does the web resource figure out the base view for a snapshot?
    String projectName = "examples-simulated";
    String version = "2.2.0-SNAPSHOT";
    String configFile = "classpath:fullstack/fullstack-examplessimulated-bin.properties";
    String serverJar = projectName + "-" + version + ".jar";
    String classpath = "config:lib/" + serverJar;
    String logbackConfig = "-Dlogback.configurationFile=com/opengamma/util/warn-logback.xml";

    // after 'mvn install' the server is in
    // $PROJECT_DIR/server/target/server-dir
    // TODO ATM this is running from the same location but that won't be the case forever
    // do I want to switch the branch underneath the running installation? seems dodgy but probably OK for testing
    ProcessBuilder processBuilder = new ProcessBuilder("java",
                                                       logbackConfig, // TODO why isn't this getting picked up?
                                                       "-cp",
                                                       classpath,
                                                       "-Xmx2g",
                                                       "-XX:MaxPermSize=256M",
                                                       "com.opengamma.component.OpenGammaComponentServer",
                                                       configFile);
    Process process;
    try {
      process = processBuilder.start();
    } catch (IOException e) {
      throw new OpenGammaRuntimeException("Failed to start server process", e);
    }
    BlockingQueue<Boolean> startupQueue = new ArrayBlockingQueue<>(1);
    consumeStream(process.getInputStream(), OpenGammaComponentServer.STARTUP_COMPLETE_MESSAGE, startupQueue, true, System.out);
    consumeStream(process.getErrorStream(), OpenGammaComponentServer.STARTUP_FAILED_MESSAGE, startupQueue, false, System.err);
    Boolean startupSuccess;
    try {
      startupSuccess = startupQueue.take();
    } catch (InterruptedException e) {
      // not going to happen
      throw new OpenGammaRuntimeException("unexpected exception", e);
    }

    if (!startupSuccess) {
      throw new OpenGammaRuntimeException("startup failed, aborting");
    }
    return new ServerProcess(process);
  }

  /**
   * Starts a thread which consumes a stream line by line and puts a value onto a queue when it encounters
   * a line starting with a particular value. If an exception occurs a value of false it put onto the queue.
   * Every line read from the stream is written to a {@link PrintStream}.
   * @param stream The stream to consume
   * @param value The trigger value - the stream line must <em>startWith</em> this string
   * @param queue The queue
   * @param signalValue The value to put onto the queue when line is encountered in the stream
   * @param output Every line read from the stream is written to this print stream
   */
  private static void consumeStream(final InputStream stream,
                                    final String value,
                                    final BlockingQueue<Boolean> queue,
                                    final Boolean signalValue,
                                    final PrintStream output) {
    Thread thread = new Thread(new Runnable() {
      @Override
      public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
          String nextLine;
          while ((nextLine = reader.readLine()) != null) {
            output.println(nextLine);
            if (nextLine.startsWith(value)) {
              queue.put(signalValue);
            }
          }
        } catch (IOException e) {
          e.printStackTrace();
          try {
            queue.put(false);
          } catch (InterruptedException e1) {
            // not going to happen
          }
        } catch (InterruptedException e) {
          // not going to happen
        }
      }
    });
    thread.setDaemon(true);
    thread.start();
  }

  @Override
  public void close() {
    _process.destroy();
  }
}
