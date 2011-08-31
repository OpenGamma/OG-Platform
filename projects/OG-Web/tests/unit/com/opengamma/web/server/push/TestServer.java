/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push;

import org.eclipse.jetty.server.Server;

/**
 *
 */
public class TestServer {

  public static void main(String[] args) throws Exception {
    Server server = WebPushTestUtils.createJettyServer("classpath:/com/opengamma/web/marketdatasnapshotlist-test.xml").getFirst();
    //Server server = WebPushTestUtils.createJettyServer("classpath:/com/opengamma/web/viewport-test.xml").getFirst();
    //Server server = WebPushTestUtils.createJettyServer("classpath:/com/opengamma/web/long-poll-test.xml").getFirst();
    server.join();
  }
}
