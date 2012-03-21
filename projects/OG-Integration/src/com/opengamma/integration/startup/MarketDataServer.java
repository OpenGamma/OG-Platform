/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.startup;

import com.opengamma.util.PlatformConfigUtils;
import com.opengamma.web.jetty.JettyServer;

/**
 * Starts a market data server
 */
public class MarketDataServer {

  public static void main(String[] args) throws Exception {  // CSIGNORE
    JettyServer jettyServer = new JettyServer(PlatformConfigUtils.MarketDataSource.DIRECT);
    jettyServer.run("config/marketdata-spring.xml");
  }

}
