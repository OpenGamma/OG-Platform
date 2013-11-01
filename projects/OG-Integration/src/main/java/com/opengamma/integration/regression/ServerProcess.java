/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.google.common.collect.ImmutableList;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.component.OpenGammaComponentServer;

/**
 * Runs {@link OpenGammaComponentServer} in a new process
. */
public final class ServerProcess implements AutoCloseable {

  /** The server process. */
  private final Process _process;
  
  private ServerProcess(Process process) {
    _process = process;
  }

  // TODO memory options

  /**
   * Creates a new process that runs {@link OpenGammaComponentServer}.
   * @param workingDir The working directory for the process
   * @param classpath The classpath argument for the process
   * @param configFile The location of the server configuration file
   * @param propertyOverrides Properties to override values in the configuration
   * @param logbackConfig Property to set the logback configuration
   * @return The process
   */
  public static ServerProcess start(String workingDir,
                                    String classpath,
                                    String configFile,
                                    Properties propertyOverrides,
                                    String logbackConfig) {
    ImmutableList.Builder<String> commandBuilder = ImmutableList.builder();
    commandBuilder.add("java",
                       logbackConfig,
                       "-cp",
                       classpath,
                       "-Xmx2g",
                       "-XX:MaxPermSize=256M",
                       "com.opengamma.component.OpenGammaComponentServer",
                       configFile);
    // can override properties in the config on the command line with prop1=value1 prop2=value2 ...
    for (Map.Entry<Object, Object> entry : propertyOverrides.entrySet()) {
      commandBuilder.add(entry.getKey() + "=" + entry.getValue());
    }
    ProcessBuilder processBuilder = new ProcessBuilder(commandBuilder.build()).directory(new File(workingDir));
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
      // TODO timeout mechanism in case the server dies and doesn't log correctly. timer task that interrupts this thread?
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
