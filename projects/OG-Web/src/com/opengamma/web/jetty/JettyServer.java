/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.jetty;

import java.io.File;
import java.io.IOException;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PlatformConfigUtils;
import com.opengamma.util.PlatformConfigUtils.MarketDataSource;
import com.opengamma.util.PlatformConfigUtils.RunMode;

/**
 * Starts a jetty server configured from spring
 */
public class JettyServer {
  
  private final RunMode _runMode;
  
  private final MarketDataSource _marketDataSource;
  
  public JettyServer(RunMode runMode, MarketDataSource marketDataSource) {
    ArgumentChecker.notNull(runMode, "runMode");
    ArgumentChecker.notNull(marketDataSource, "marketDataSource");
    _runMode = runMode;
    _marketDataSource = marketDataSource;
  }

  public void run(final String springConfig) throws IOException {
    ArgumentChecker.notNull(springConfig, "spring config");
    
    // Logging
    if (System.getProperty("logback.configurationFile") == null) {
      System.setProperty("logback.configurationFile", "jetty-logback.xml");
    }
    
    PlatformConfigUtils.configureSystemProperties(_runMode, _marketDataSource);
    
    // server
    try {
      process(getRelativePath(springConfig));
      System.exit(0);
    } catch (Throwable ex) {
      ex.printStackTrace();
      System.exit(1);
    }
  }

  private static String getRelativePath(final String springConfig) throws IOException {
    String absolutePath = new File(springConfig).getCanonicalPath();
    String baseDir = new File(".").getCanonicalPath();
    return absolutePath.substring(baseDir.length() + 1);
  }
  
  private static void process(String springConfig) throws Exception {
    System.out.println("================================== JETTY START BEGINS =======================================");
    ApplicationContext appContext = new FileSystemXmlApplicationContext(springConfig);
    Server server = appContext.getBean("server", Server.class);
    System.out.println(server.dump());
    server.start();
    System.out.println();
    System.out.println("Server started on port " + getServerPort(appContext));
    System.out.println();
    System.out.println("================================== JETTY START COMPLETE =====================================");
    System.out.println();
    server.join();
  }

  private static int getServerPort(final ApplicationContext appContext) {
    SelectChannelConnector connector = appContext.getBean("connector", SelectChannelConnector.class);
    return connector.getPort();
  }
  
}
