/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.jetty;

import org.eclipse.jetty.server.Server;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.opengamma.util.PlatformConfigUtils;

/**
 * Abstract base class for running a Jetty server
 */
public abstract class AbstractJettyServer {
  
  public static void run(String springConfig) {   
    System.out.println("================================== SETUP LOGGING ============================================");
    
    // Logging
    if (System.getProperty("logback.configurationFile") == null) {
      System.setProperty("logback.configurationFile", "com/opengamma/util/test/warn-logback.xml");
    }
    
    PlatformConfigUtils.configureSystemProperties(PlatformConfigUtils.RunMode.SHAREDDEV, PlatformConfigUtils.MarketDataSource.DIRECT);
    
    // server
    try {
      process(springConfig);
      System.exit(0);
    } catch (Throwable ex) {
      ex.printStackTrace();
      System.exit(1);
    }
  }
  
  public static void process(String springConfig) throws Exception {
    System.out.println("================================== JETTY START BEGINS =======================================");
    ApplicationContext appContext = new FileSystemXmlApplicationContext(springConfig);
    
    Server server = appContext.getBean("server", Server.class);
    System.out.println(server.dump());
    server.start();
    System.out.println("================================== JETTY START COMPLETE =====================================");
    
    server.join();
  }
  
}
