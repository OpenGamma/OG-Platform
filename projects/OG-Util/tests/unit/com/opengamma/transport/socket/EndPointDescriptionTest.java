/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.transport.socket;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.testng.annotations.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test.
 */
@Test
public class EndPointDescriptionTest {

  private static final Logger s_logger = LoggerFactory.getLogger(EndPointDescriptionTest.class);

  private void testEndPoints(boolean bind) throws IOException {
    final AbstractServerSocketProcess server = new AbstractServerSocketProcess() {

      @Override
      protected void socketOpened(Socket socket) {
      }

    };
    if (bind) {
      server.setBindAddress(InetAddress.getLocalHost());
    }
    server.start();
    final FudgeFieldContainer serverEndPoint = server.getEndPointDescription(FudgeContext.GLOBAL_DEFAULT);
    assertNotNull(serverEndPoint);
    s_logger.info("Server end point {}", serverEndPoint);
    final AbstractSocketProcess client = new AbstractSocketProcess() {

      @Override
      protected void socketOpened(Socket socket, BufferedOutputStream os, BufferedInputStream is) {
      }

    };
    client.setInetAddress(InetAddress.getLocalHost());
    client.setPortNumber(server.getPortNumber());
    client.start();
    final FudgeFieldContainer clientEndPoint = client.getEndPointDescription(FudgeContext.GLOBAL_DEFAULT);
    assertNotNull(clientEndPoint);
    s_logger.info("Client end point {} ", clientEndPoint);
    assertEquals(serverEndPoint.getString(AbstractServerSocketProcess.TYPE_KEY), clientEndPoint.getString(AbstractServerSocketProcess.TYPE_KEY));
    assertEquals(serverEndPoint.getInt(AbstractServerSocketProcess.PORT_KEY), clientEndPoint.getInt(AbstractServerSocketProcess.PORT_KEY));
    client.stop();
    server.stop();
  }

  public void testEndPointsBound() throws IOException {
    testEndPoints(true);
  }

  public void testEndPointsUnbound() throws IOException {
    testEndPoints(false);
  }

  public void testConnectToEndPoint() throws IOException {
    final AbstractServerSocketProcess server = new AbstractServerSocketProcess() {

      @Override
      protected void socketOpened(Socket socket) {
      }

    };
    server.start();
    final FudgeFieldContainer serverEndPoint = server.getEndPointDescription(FudgeContext.GLOBAL_DEFAULT);
    assertNotNull(serverEndPoint);
    s_logger.info("Server end point {}", serverEndPoint);
    final AbstractSocketProcess client = new AbstractSocketProcess() {

      @Override
      protected void socketOpened(Socket socket, BufferedOutputStream os, BufferedInputStream is) {
      }

    };
    client.setServer(serverEndPoint);
    client.start();
    final FudgeFieldContainer clientEndPoint = client.getEndPointDescription(FudgeContext.GLOBAL_DEFAULT);
    assertNotNull(clientEndPoint);
    s_logger.info("Client end point {} ", clientEndPoint);
    client.stop();
    server.stop();
  }

}
