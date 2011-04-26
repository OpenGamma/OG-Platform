/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.startup;

import com.opengamma.web.jetty.AbstractJettyServer;

/**
 * Example Engine server
 */
public class ExampleServer extends AbstractJettyServer {
  
  public static void main(String[] args) throws Exception { // CSIGNORE
    String springXML = args.length == 1 ? args[0] : "config/engine-spring.xml";
    run(springXML);
  }

}
