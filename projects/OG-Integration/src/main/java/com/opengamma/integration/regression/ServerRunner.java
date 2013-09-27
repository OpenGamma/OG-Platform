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
import java.util.EnumSet;
import java.util.List;

import org.threeten.bp.Instant;

import com.google.common.collect.Lists;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.component.OpenGammaComponentServer;
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.client.ViewResultMode;
import com.opengamma.engine.view.execution.ExecutionFlags;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.execution.InfiniteViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.execution.ViewExecutionFlags;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.engine.view.listener.AbstractViewResultListener;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.integration.marketdata.manipulator.dsl.RemoteServer;
import com.opengamma.livedata.UserPrincipal;

/**
 *
 */
public final class ServerRunner {

  private ServerRunner() {
  }

  public static void main(String[] args) throws Exception {
    ServerRunner.run();
  }

  public static void run() throws IOException, InterruptedException {
    // after 'mvn install' the server is in
    // $PROJECT_DIR/server/target/server-dir
    // assume this will be run from a script in scripts directory under that directory
    // TODO memory opts
    Process process = new ProcessBuilder("java",
                                         "-Dlogback.configurationFile=com/opengamma/util/warn-logback.xml",
                                         "-cp",
                                         "config:lib/examples-simulated-2.2.0-SNAPSHOT.jar",
                                         "-Xmx2g",
                                         "-XX:MaxPermSize=256M",
                                         "com.opengamma.component.OpenGammaComponentServer",
                                         "classpath:fullstack/fullstack-examplessimulated-dev.properties")
        .start();
    final InputStream stdout = process.getInputStream();
    printStreamInThread(process.getErrorStream(), "stderr");
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(stdout))) {
      String line;
      while ((line = reader.readLine()) != null) {
        System.out.println(line);
        // TODO this comes on stderr
        if (line.startsWith(OpenGammaComponentServer.STARTUP_FAILED_MESSAGE)) {
          throw new OpenGammaRuntimeException("Server startup failed");
        } else if (line.startsWith(OpenGammaComponentServer.STARTUP_COMPLETE_MESSAGE)) {
          break;
        }
      }
    }
    System.out.println("--------------------- server started ---------------------");
    // TODO this needs to be a snapshot
    List<MarketDataSpecification> marketDataSpecs = Lists.<MarketDataSpecification>newArrayList(new LiveMarketDataSpecification("Activ"));
    ViewCycleExecutionOptions defaultOptions =
        ViewCycleExecutionOptions
            .builder()
            .setValuationTime(Instant.now())
            .setMarketDataSpecifications(marketDataSpecs)
            .setResolverVersionCorrection(VersionCorrection.LATEST)
            .create();
    EnumSet<ViewExecutionFlags> flags =
        ExecutionFlags.triggersEnabled().parallelCompilation(ExecutionFlags.ParallelRecompilationMode.PARALLEL_EXECUTION).get();
    ViewExecutionOptions executionOptions =
        ExecutionOptions.of(new InfiniteViewCycleExecutionSequence(), defaultOptions, flags);

    RemoteServer server = RemoteServer.create("http://localhost:8080");
    ViewProcessor viewProcessor = server.getViewProcessor();
    ViewClient viewClient = viewProcessor.createViewClient(UserPrincipal.getLocalUser());
    viewClient.setResultListener(new Listener());
    viewClient.setResultMode(ViewResultMode.FULL_ONLY);
    viewClient.attachToViewProcess(UniqueId.parse("DbCfg~4428"), executionOptions);
    viewClient.waitForCompletion();
    process.destroy();
  }
  
  private static Thread printStreamInThread(final InputStream stream, final String prefix) {
    Thread thread = new Thread(new Runnable() {
      @Override
      public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
          String line;
          while ((line = reader.readLine()) != null) {
            System.out.println(prefix + ": " + line);
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });
    thread.start();
    return thread;
  }
}

class Listener extends AbstractViewResultListener {

  @Override
  public UserPrincipal getUser() {
    return UserPrincipal.getLocalUser();
  }
}
