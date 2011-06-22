/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.startup;

import com.opengamma.util.PlatformConfigUtils;
import com.opengamma.web.jetty.JettyServer;

/**
 * Example Engine server
 */
public class ExampleServer {
  
  public static void main(String[] args) throws Exception { // CSIGNORE
    String springXML = args.length == 1 ? args[0] : "config/engine-spring.xml";
    JettyServer jettyServer = new JettyServer(PlatformConfigUtils.RunMode.EXAMPLE, PlatformConfigUtils.MarketDataSource.DIRECT);
    jettyServer.run(springXML);
  }

}
